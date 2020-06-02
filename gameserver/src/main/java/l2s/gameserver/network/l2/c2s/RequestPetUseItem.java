package l2s.gameserver.network.l2.c2s;


import org.apache.commons.lang3.ArrayUtils;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.utils.ItemFunctions;

public class RequestPetUseItem extends L2GameClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}

		activeChar.setActive();

		PetInstance pet = (PetInstance) activeChar.getServitor();
		if(pet == null)
			return;

		ItemInstance item = pet.getInventory().getItemByObjectId(_objectId);

		if(item == null || item.getCount() < 1)
			return;

		if(activeChar.isAlikeDead() || pet.isDead() || pet.isOutOfControl())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(item.getItemId()));
			return;
		}

		// manual pet feeding
		if(pet.tryFeedItem(item))
			return;

		if(ArrayUtils.contains(Config.ALT_ALLOWED_PET_POTIONS, item.getItemId()))
		{
			Skill[] skills = item.getTemplate().getAttachedSkills();
			if(skills.length > 0)
			{
				for(Skill skill : skills)
				{
					Creature aimingTarget = skill.getAimingTarget(pet, pet.getTarget());
					if(skill.checkCondition(pet, aimingTarget, false, false, true))
						pet.getAI().Cast(skill, aimingTarget, false, false);
				}
			}
			return;
		}
		
		SystemMessage sm = ItemFunctions.checkIfCanEquip(pet, item);
		if(sm == null)
		{
			if(item.isEquipped())
				pet.getInventory().unEquipItem(item);
			else
				pet.getInventory().equipItem(item);
			pet.broadcastCharInfo();
			return;
		}

		activeChar.sendPacket(sm);
	}
}
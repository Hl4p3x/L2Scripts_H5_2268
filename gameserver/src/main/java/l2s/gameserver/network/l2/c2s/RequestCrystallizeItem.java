package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Log;

public class RequestCrystallizeItem extends L2GameClientPacket
{
	//Format: cdd

	private int _objectId;
	@SuppressWarnings("unused")
	private long unk;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		unk = readQ(); //FIXME: count??
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

		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		if(activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}

		ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		if(item == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(item.isHeroWeapon())
		{
			activeChar.sendPacket(Msg.HERO_WEAPONS_CANNOT_BE_DESTROYED);
			return;
		}

		if(!item.canBeCrystallized(activeChar))
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}

		if(activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}

		int crystalId = item.getTemplate().getCrystalType().cry;

		//can player crystallize?
		int level = activeChar.getSkillLevel(Skill.SKILL_CRYSTALLIZE);
		if(level < 1 || crystalId - ItemTemplate.CRYSTAL_D + 1 > level)
		{
			activeChar.sendPacket(Msg.CANNOT_CRYSTALLIZE_CRYSTALLIZATION_SKILL_LEVEL_TOO_LOW);
			activeChar.sendActionFailed();
			return;
		}

		int crystalAmount = item.getCrystalCountOnCrystallize();

		Log.LogEvent(activeChar.getName(), activeChar.getIP(), "Crystalization", "Crystalized Item:", item.getName(),"(objId: "+_objectId+")", "to "+crystalAmount+" crystals ID: "+crystalId+"");

		if(!activeChar.getInventory().destroyItemByObjectId(_objectId, 1L))
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.sendPacket(Msg.THE_ITEM_HAS_BEEN_SUCCESSFULLY_CRYSTALLIZED);

		if(crystalId > 0 && crystalAmount > 0)
			ItemFunctions.addItem(activeChar, crystalId, crystalAmount, true, "Crystallization item result reward");

		activeChar.sendChanges();
	}
}
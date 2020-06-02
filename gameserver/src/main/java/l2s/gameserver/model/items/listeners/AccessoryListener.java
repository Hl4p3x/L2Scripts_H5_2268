package l2s.gameserver.model.items.listeners;

import l2s.gameserver.listener.inventory.OnEquipListener;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Skill.SkillType;
import l2s.gameserver.model.items.ItemEquipResult;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.item.ItemTemplate;

public final class AccessoryListener implements OnEquipListener
{
	private static final AccessoryListener _instance = new AccessoryListener();

	public static AccessoryListener getInstance()
	{
		return _instance;
	}

	@Override
	public void onUnequip(ItemEquipResult result, int slot, ItemInstance item, Playable actor)
	{
		if(!item.isEquipable())
			return;

		Player player = (Player)actor;

		if(item.getBodyPart() == ItemTemplate.SLOT_L_BRACELET && item.getTemplate().getAttachedSkills().length > 0)
		{
			int agathionId = player.getAgathionId();
			int transformNpcId = player.getTransformationTemplate();
			for(Skill skill : item.getTemplate().getAttachedSkills())
			{
				if(agathionId > 0 && skill.getNpcId() == agathionId)
					player.setAgathion(0);
				if(skill.getNpcId() == transformNpcId && skill.getSkillType() == SkillType.TRANSFORMATION)
					player.setTransformation(0);
			}
		}

		if(item.isAccessory() || item.getTemplate().isTalisman() || item.getTemplate().isBracelet())
			player.sendUserInfo(true);
		// TODO [G1ta0] отладить отображение аксессуаров
		//player.sendPacket(new ItemList(player, false));
		else
			player.broadcastCharInfo();
	}

	@Override
	public void onEquip(ItemEquipResult result, int slot, ItemInstance item, Playable actor)
	{
		if(!item.isEquipable())
			return;

		Player player = (Player)actor;

		if(item.isAccessory() || item.getTemplate().isTalisman() || item.getTemplate().isBracelet())
			player.sendUserInfo(true);
		// TODO [G1ta0] отладить отображение аксессуаров
		//player.sendPacket(new ItemList(player, false));
		else
			player.broadcastCharInfo();
	}
}
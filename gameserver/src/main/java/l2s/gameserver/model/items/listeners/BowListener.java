package l2s.gameserver.model.items.listeners;

import l2s.gameserver.listener.inventory.OnEquipListener;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemEquipResult;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType;

public final class BowListener implements OnEquipListener
{
	private static final BowListener _instance = new BowListener();

	public static BowListener getInstance()
	{
		return _instance;
	}

	@Override
	public void onUnequip(ItemEquipResult result, int slot, ItemInstance item, Playable actor)
	{
		if(!item.isEquipable() || slot != Inventory.PAPERDOLL_RHAND)
			return;

		Player player = (Player)actor;

		if(item.getItemType() == WeaponType.BOW || item.getItemType() == WeaponType.CROSSBOW || item.getItemType() == WeaponType.ROD)
			player.getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, null);
	}

	@Override
	public void onEquip(ItemEquipResult result, int slot, ItemInstance item, Playable actor)
	{
		if(!item.isEquipable() || slot != Inventory.PAPERDOLL_RHAND)
			return;

		Player player = (Player)actor;

		if(item.getItemType() == WeaponType.BOW)
		{
			ItemInstance arrow = player.getInventory().findArrowForBow(item.getTemplate());
			if(arrow != null)
				player.getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, arrow);
		}
		if(item.getItemType() == WeaponType.CROSSBOW)
		{
			ItemInstance bolt = player.getInventory().findArrowForCrossbow(item.getTemplate());
			if(bolt != null)
				player.getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, bolt);
		}
		if(item.getItemType() == WeaponType.ROD)
		{
			ItemInstance bait = player.getInventory().findEquippedLure();
			if(bait != null)
				player.getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, bait);
		}
	}
}
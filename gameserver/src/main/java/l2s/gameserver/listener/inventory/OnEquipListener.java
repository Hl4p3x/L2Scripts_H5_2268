package l2s.gameserver.listener.inventory;

import l2s.commons.listener.Listener;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.items.ItemEquipResult;
import l2s.gameserver.model.items.ItemInstance;

public interface OnEquipListener extends Listener<Playable>
{
	public void onEquip(ItemEquipResult result, int slot, ItemInstance item, Playable actor);

	public void onUnequip(ItemEquipResult result, int slot, ItemInstance item, Playable actor);
}

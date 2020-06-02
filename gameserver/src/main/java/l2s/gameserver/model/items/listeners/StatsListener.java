package l2s.gameserver.model.items.listeners;

import l2s.gameserver.listener.inventory.OnEquipListener;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.items.ItemEquipResult;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.stats.funcs.Func;

public final class StatsListener implements OnEquipListener
{
	private static final StatsListener _instance = new StatsListener();

	public static StatsListener getInstance()
	{
		return _instance;
	}

	@Override
	public void onUnequip(ItemEquipResult result, int slot, ItemInstance item, Playable actor)
	{
		actor.removeStatsOwner(item);
		result.sendChanges = true;
	}

	@Override
	public void onEquip(ItemEquipResult result, int slot, ItemInstance item, Playable actor)
	{
		Func[] funcs = item.getStatFuncs();
		actor.addStatFuncs(funcs);
		result.sendChanges = true;
	}
}
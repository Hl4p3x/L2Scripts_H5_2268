package handler.items;

import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.ShowMinimapPacket;

public class WorldMap extends ScriptItemHandler
{
	// all the items ids that this handler knowns
	private static final int[] _itemIds = { 1665, 1863, 9994 };

	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
			return false;
		Player player = (Player) playable;

		player.sendPacket(new ShowMinimapPacket(player, item.getItemId()));
		return true;
	}

	@Override
	public final int[] getItemIds()
	{
		return _itemIds;
	}
}
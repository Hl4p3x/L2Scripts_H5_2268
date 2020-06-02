package handler.items;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;

public class BookMark extends SimpleItemHandler
{
	private static final int ADD_CAPACITY = 3;
	private static final int[] ITEM_IDS = {
		13015,	// My Teleport Spellbook - Increases by 3 slots the free teleport list, which can remember designated locations.
		13301	// My Teleport Spellbook (Event) - Increases by 3 slots the free teleport list, which can remember designated locations.
	};

	@Override
	protected boolean useItemImpl(Player player, ItemInstance item, boolean ctrl)
	{
		if(player == null)
			return false;

		if(!tryUseItem(player, item, 1, true))
			return false;

		player.bookmarks.incCapacity(ADD_CAPACITY);
		return true;
	}

	@Override
	public final int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
package handler.items;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;

public class NevitVoice extends SimpleItemHandler
{
	private static final int[] ITEM_IDS = new int[] { 17094 };

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}

	@Override
	protected boolean useItemImpl(Player player, ItemInstance item, boolean ctrl)
	{
		int itemId = item.getItemId();

		if(!tryUseItem(player, item, 1, true))
			return false;

		switch(itemId)
		{
			case 17094:
				player.addRecomHave(10);
				break;
			default:
				return false;
		}

		return true;
	}
}

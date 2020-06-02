package handler.items;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.SSQStatusPacket;
import l2s.gameserver.network.l2.s2c.ShowXMasSealPacket;

public class Books extends SimpleItemHandler
{
	private static final int[] ITEM_IDS = new int[] { 5555, 5707 };

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}

	@Override
	protected boolean useItemImpl(Player player, ItemInstance item, boolean ctrl)
	{
		int itemId = item.getItemId();

		switch(itemId)
		{
			case 5555:
				player.sendPacket(new ShowXMasSealPacket(5555));
				break;
			case 5707:
				player.sendPacket(new SSQStatusPacket(player, 1));
				break;
			default:
				return false;
		}

		return true;
	}
}

package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.PremiumItem;
import l2s.gameserver.network.l2.s2c.ExGetPremiumItemListPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;

//FIXME [G1ta0] item-API
public final class RequestWithDrawPremiumItem extends L2GameClientPacket
{
	private int _itemNum;
	private int _charId;
	private long _itemcount;

	@Override
	protected void readImpl()
	{
		_itemNum = readD();
		_charId = readD();
		_itemcount = readQ();
	}

	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;
		if(_itemcount <= 0)
			return;

		if(activeChar.getObjectId() != _charId)
			// audit
			return;
		if(activeChar.getPremiumItemList().isEmpty())
			// audit
			return;
		if(activeChar.getWeightPenalty() >= 3 || activeChar.getInventoryLimit() * 0.8 <= activeChar.getInventory().getSize())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_RECEIVE_THE_VITAMIN_ITEM_BECAUSE_YOU_HAVE_EXCEED_YOUR_INVENTORY_WEIGHT_QUANTITY_LIMIT);
			return;
		}
		if(activeChar.isProcessingRequest())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_RECEIVE_A_VITAMIN_ITEM_DURING_AN_EXCHANGE);
			return;
		}

		PremiumItem _item = activeChar.getPremiumItemList().get(_itemNum);
		if(_item == null)
			return;
		boolean stackable = ItemHolder.getInstance().getTemplate(_item.getItemId()).isStackable();
		if(_item.getCount() < _itemcount)
			return;
		if(!stackable)
			for(int i = 0; i < _itemcount; i++)
				addItem(activeChar, _item.getItemId(), 1);
		else
			addItem(activeChar, _item.getItemId(), _itemcount);
		if(_itemcount < _item.getCount())
		{
			activeChar.getPremiumItemList().get(_itemNum).updateCount(_item.getCount() - _itemcount);
			activeChar.updatePremiumItem(_itemNum, _item.getCount() - _itemcount);
		}
		else
		{
			activeChar.getPremiumItemList().remove(_itemNum);
			activeChar.deletePremiumItem(_itemNum);
		}

		if(activeChar.getPremiumItemList().isEmpty())
			activeChar.sendPacket(Msg.THERE_ARE_NO_MORE_VITAMIN_ITEMS_TO_BE_FOUND);
		else
			activeChar.sendPacket(new ExGetPremiumItemListPacket(activeChar));
	}

	private void addItem(Player player, int itemId, long count)
	{
		player.getInventory().addItem(itemId, count);
		player.sendPacket(SystemMessagePacket.obtainItems(itemId, count, 0));
	}
}
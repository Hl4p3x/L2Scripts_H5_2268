package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.BuyListHolder.NpcTradeList;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.TradeItem;
import l2s.gameserver.network.l2.ServerPacketOpcodes;

public abstract class ExBuySellListPacket extends L2GameServerPacket
{
	@Override
	protected ServerPacketOpcodes getOpcodes()
	{
		return ServerPacketOpcodes.ExBuySellListPacket;
	}

	public static class BuyList extends ExBuySellListPacket
	{
		private final int _listId;
		private final List<TradeItem> _buyList;
		private final long _adena;
		private final double _taxRate;
		private final int _inventoryUsedSlots;

		public BuyList(NpcTradeList tradeList, Player activeChar, double taxRate)
		{
			_adena = activeChar.getAdena();
			_taxRate = taxRate;
			_inventoryUsedSlots = activeChar.getInventory().getSize();

			if(tradeList != null)
			{
				_listId = tradeList.getListId();
				_buyList = tradeList.getItems();
				activeChar.setBuyListId(_listId);
			}
			else
			{
				_listId = 0;
				_buyList = Collections.emptyList();
				activeChar.setBuyListId(0);
			}
		}

		@Override
		protected void writeImpl()
		{
			writeD(0x00); // BUY LIST TYPE
			writeQ(_adena); // current money
			writeD(_listId);
			writeD(_inventoryUsedSlots); //TODO [Bonux] Awakening
			writeH(_buyList.size());
			for(TradeItem item : _buyList)
			{
				writeItemInfo(item, item.getCurrentValue());
				writeQ((long) (item.getOwnersPrice() * (1. + _taxRate)));
			}
		}

		@Override
		protected void writeImplHF()
		{
			writeD(0x00); // BUY LIST TYPE
			writeQ(_adena); // current money
			writeD(_listId);
			writeH(_buyList.size());
			for(TradeItem item : _buyList)
			{
				writeItemInfo(item, item.getCurrentValue());
				writeQ((long) (item.getOwnersPrice() * (1. + _taxRate)));
			}
		}
	}

	public static class SellRefundList extends ExBuySellListPacket
	{
		private final List<TradeItem> _sellList;
		private final List<TradeItem> _refundList;
		private final int _inventoryUsedSlots;
		private int _done;

		public SellRefundList(Player activeChar, boolean done)
		{
			_done = done ? 1 : 0;
			_inventoryUsedSlots = activeChar.getInventory().getSize();

			if(done)
			{
				_refundList = Collections.emptyList();
				_sellList = Collections.emptyList();
			}
			else
			{
				ItemInstance[] items = activeChar.getRefund().getItems();
				_refundList = new ArrayList<TradeItem>(items.length);
				for(ItemInstance item : items)
					_refundList.add(new TradeItem(item));

				items = activeChar.getInventory().getItems();
				_sellList = new ArrayList<TradeItem>(items.length);
				for(ItemInstance item : items)
					if(item.canBeSold(activeChar) && !item.isEquipped())
						_sellList.add(new TradeItem(item, item.getTemplate().isBlocked(activeChar, item)));
			}
		}

		@Override
		protected void writeImpl()
		{
			writeD(0x01); // SELL/REFUND LIST TYPE
			writeD(_inventoryUsedSlots); //TODO [Bonux] Awakening
			writeH(_sellList.size());
			for(TradeItem item : _sellList)
			{
				writeItemInfo(item);
				writeQ(item.getReferencePrice() / Config.ALT_SELL_PRICE_DIV);
			}
			writeH(_refundList.size());
			for(TradeItem item : _refundList)
			{
				writeItemInfo(item);
				writeD(item.getObjectId());
				writeQ(item.getCount() * item.getReferencePrice() / Config.ALT_SELL_PRICE_DIV);
			}
			writeC(_done);
		}

		@Override
		protected void writeImplHF()
		{
			writeD(0x01); // SELL/REFUND LIST TYPE
			writeH(_sellList.size());
			for(TradeItem item : _sellList)
			{
				writeItemInfo(item);
				writeQ(item.getReferencePrice() / Config.ALT_SELL_PRICE_DIV);
			}
			writeH(_refundList.size());
			for(TradeItem item : _refundList)
			{
				writeItemInfo(item);
				writeD(item.getObjectId());
				writeQ(item.getCount() * item.getReferencePrice() / Config.ALT_SELL_PRICE_DIV);
			}
			writeC(_done);
		}
	}
}
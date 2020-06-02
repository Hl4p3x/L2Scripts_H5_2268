package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.data.xml.holder.BuyListHolder.NpcTradeList;
import l2s.gameserver.model.items.TradeItem;


/**
 * Format: c ddh[hdddhhd]
 * c - id (0xE8)
 *
 * d - money
 * d - manor id
 * h - size
 * [
 * h - item type 1
 * d - object id
 * d - item id
 * d - count
 * h - item type 2
 * h
 * d - price
 * ]
 */
public final class BuyListSeedPacket extends L2GameServerPacket
{
	private int _manorId;
	private List<TradeItem> _list = new ArrayList<TradeItem>();
	private long _money;

	public BuyListSeedPacket(NpcTradeList list, int manorId, long currentMoney)
	{
		_money = currentMoney;
		_manorId = manorId;
		_list = list.getItems();
	}

	@Override
	protected final void writeImpl()
	{
		writeQ(_money); // current money
		writeD(0x00); // God UNK
		writeD(_manorId); // manor id

		writeH(_list.size()); // list length

		for(TradeItem item : _list)
		{
			writeItemInfo(item);
			writeQ(item.getOwnersPrice());
		}
	}

	@Override
	protected final void writeImplHF()
	{
		writeQ(_money); // current money
		writeD(_manorId); // manor id

		writeH(_list.size()); // list length

		for(TradeItem item : _list)
		{
			writeItemInfo(item);
			writeQ(item.getOwnersPrice());
		}
	}
}
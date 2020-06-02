package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.Warehouse.ItemClassComparator;
import l2s.gameserver.model.items.Warehouse.WarehouseType;

public class WareHouseDepositListPacket extends L2GameServerPacket
{
	private final int _type;
	private final int _whType;
	private final long _adena;
	private final List<ItemInfo> _itemList;
	private final int _depositedItemsCount;

	public WareHouseDepositListPacket(int type, Player cha, WarehouseType whtype)
	{
		_type = type;
		_whType = whtype.ordinal();
		_adena = cha.getAdena();

		ItemInstance[] items = cha.getInventory().getItems();
		Arrays.sort(items, ItemClassComparator.getInstance());
		_itemList = new ArrayList<ItemInfo>(items.length);
		for(ItemInstance item : items)
			if(item.canBeStored(cha, _whType == 1))
				_itemList.add(new ItemInfo(item, item.getTemplate().isBlocked(cha, item)));

		switch(whtype)
		{
			case PRIVATE:
				_depositedItemsCount = cha.getWarehouse().getSize();
				break;
			case FREIGHT:
				_depositedItemsCount = cha.getFreight().getSize();
				break;
			case CLAN:
			case CASTLE:
				_depositedItemsCount = cha.getClan().getWarehouse().getSize();
				break;
			default:
				_depositedItemsCount = 0;
				return;
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_type);
		writeH(_whType);
		if(_type == 1)
		{
			writeQ(_adena);
			writeH(_depositedItemsCount); //Количество вещей которые уже есть в банке.
			writeD(0x00);
			writeD(0x00);
		}
		else if(_type == 2)
		{
			writeH(0);//TODO [Bonux]
			writeD(_itemList.size());
			for(ItemInfo item : _itemList)
			{
				writeItemInfo(item);
				writeD(item.getObjectId());
			}
		}
	}

	@Override
	protected boolean canWriteHF()
	{
		return _type == 2;
	}

	@Override
	protected final void writeImplHF()
	{
		writeH(_whType);
		writeQ(_adena);
		writeH(_itemList.size());
		for(ItemInfo item : _itemList)
		{
			writeItemInfo(item);
			writeD(item.getObjectId());
		}
	}
}
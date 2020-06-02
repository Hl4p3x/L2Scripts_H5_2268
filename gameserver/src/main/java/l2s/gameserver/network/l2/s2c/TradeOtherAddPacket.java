package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.items.ItemInfo;

public class TradeOtherAddPacket extends L2GameServerPacket
{
	private final int _type;
	private final ItemInfo _item;
	private final long _amount;

	public TradeOtherAddPacket(int type, ItemInfo item, long amount)
	{
		_type = type;
		_item = item;
		_amount = amount;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_type);
		writeD(1);	// Count
		if(_type == 2)
		{
			writeH(1);	// Count
			writeC(0x00); // UNK 140
			writeC(0x00); // UNK 140
			writeItemInfo(_item, _amount);
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
		writeH(1); // item count
		writeH(_item.getItem().getType1());
		writeD(_item.getObjectId());
		writeD(_item.getItemId());
		writeQ(_amount);
		writeH(_item.getItem().getType2ForPackets());
		writeH(_item.getCustomType1());
		writeD(_item.getItem().getBodyPart());
		writeH(_item.getEnchantLevel());
		writeH(0x00);
		writeH(_item.getCustomType2());
		writeH(_item.getAttackElement());
		writeH(_item.getAttackElementValue());
		writeH(_item.getDefenceFire());
		writeH(_item.getDefenceWater());
		writeH(_item.getDefenceWind());
		writeH(_item.getDefenceEarth());
		writeH(_item.getDefenceHoly());
		writeH(_item.getDefenceUnholy());
		writeH(0);
		writeH(0);
		writeH(0);
	}
}
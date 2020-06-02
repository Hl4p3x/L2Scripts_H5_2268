package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;

public class GMViewItemListPacket extends L2GameServerPacket
{
	private final int _type;
	private int _size;
	private ItemInstance[] _items;
	private int _limit;
	private String _name;
	private Player _player;

	public GMViewItemListPacket(int type, Player cha, ItemInstance[] items, int size)
	{
		_type = type;
		_size = size;
		_items = items;
		_name = cha.getName();
		_limit = cha.getInventoryLimit();
		_player = cha;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_type);
		if(_type == 1)
		{
			writeS(_name);
			writeD(_limit);
			writeD(_size);
		}
		else if(_type == 2)
		{
			writeD(_size);
			writeD(_size);
			for(ItemInstance temp : _items)
			{
				if(!temp.getTemplate().isQuest())
					writeItemInfo(_player, temp);
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
		writeS(_name);
		writeD(_limit); //c4?
		writeH(1); // show window ??

		writeH(_size);
		for(ItemInstance temp : _items)
			if(!temp.getTemplate().isQuest())
				writeItemInfo(_player, temp);
	}
}
package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.LockType;

public class ItemListPacket extends L2GameServerPacket
{
	private final int _size;
	private final ItemInstance[] _items;
	private final boolean _showWindow;
	private final int _type;

	private LockType _lockType;
	private int[] _lockItems;

	private Player _player;

	private final int _specialItemCount = 0;	// TODO

	public ItemListPacket(int type, Player player, int size, ItemInstance[] items, boolean showWindow, LockType lockType, int[] lockItems)
	{
		_type = type;
		_player = player;
		_size = size;
		_items = items;
		_showWindow = showWindow;
		_lockType = lockType;
		_lockItems = lockItems;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_type);
		if(_type == 1)
		{
			writeH(_showWindow);
			writeH(_specialItemCount);
			if(_specialItemCount > 0)
			{
				writeC(0x00);	// Restriction
				for(int i = 0; i < _specialItemCount; i++)
					writeD(0x00);	// Item Id
			}
			writeD(_size);	// Total items
		}
		else if(_type == 2)
		{
			writeD(_size);	// Total items
			writeD(_size);	// Items in this page
			for(ItemInstance temp : _items)
			{
				if(temp.getTemplate().isQuest())
					continue;

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
		writeH(_showWindow ? 1 : 0);

		writeH(_size);
		for(ItemInstance temp : _items)
		{
			if(temp.getTemplate().isQuest())
				continue;

			writeItemInfo(_player, temp);
		}

		writeH(_lockItems.length);
		if(_lockItems.length > 0)
		{
			writeC(_lockType.ordinal());
			for(int i : _lockItems)
				writeD(i);
		}
	}
}
package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.c2s.RequestExPostItemList;

/**
 * Ответ на запрос создания нового письма.
 * Отсылается при получении {@link RequestExPostItemList}
 * Содержит список вещей, которые можно приложить к письму.
 */
public class ExReplyPostItemList extends L2GameServerPacket
{
	private final int _type;
	private List<ItemInfo> _itemsList = new ArrayList<ItemInfo>();

	public ExReplyPostItemList(int type, Player activeChar)
	{
		_type = type;

		ItemInstance[] items = activeChar.getInventory().getItems();
		for(ItemInstance item : items)
			if(item.canBeTraded(activeChar))
				_itemsList.add(new ItemInfo(item, item.getTemplate().isBlocked(activeChar, item)));
	}

	@Override
	protected void writeImpl()
	{
		writeC(_type);
		writeD(_itemsList.size());
		if(_type == 2)
		{		
			writeD(_itemsList.size());
			for(ItemInfo item : _itemsList)
				writeItemInfo(item);
		}
	}

	@Override
	protected boolean canWriteHF()
	{
		return _type == 2;
	}

	@Override
	protected void writeImplHF()
	{
		writeD(_itemsList.size());
		for(ItemInfo item : _itemsList)
			writeItemInfo(item);
	}
}
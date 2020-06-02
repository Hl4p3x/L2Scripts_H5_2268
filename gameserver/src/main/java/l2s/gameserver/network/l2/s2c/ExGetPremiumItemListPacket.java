package l2s.gameserver.network.l2.s2c;

import java.util.Map;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.PremiumItem;


/**
 * @author Gnacik
 * @corrected by n0nam3
 **/
public class ExGetPremiumItemListPacket extends L2GameServerPacket
{
	private int _objectId;
	private Map<Integer, PremiumItem> _list;

	public ExGetPremiumItemListPacket(Player activeChar)
	{
		_objectId = activeChar.getObjectId();
		_list = activeChar.getPremiumItemList();
	}

	@Override
	protected void writeImpl()
	{
		if(!_list.isEmpty())
		{
			writeD(_list.size());
			for(Map.Entry<Integer, PremiumItem> entry : _list.entrySet())
			{
				writeD(entry.getKey());
				writeD(_objectId);
				writeD(entry.getValue().getItemId());
				writeQ(entry.getValue().getCount());
				writeD(0);
				writeS(entry.getValue().getSender());
			}
		}
	}

}
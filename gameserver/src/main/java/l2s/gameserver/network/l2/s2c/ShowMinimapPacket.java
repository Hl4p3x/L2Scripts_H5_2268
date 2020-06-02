package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.SevenSigns;

public class ShowMinimapPacket extends L2GameServerPacket
{
	private int _mapId, _period;

	public ShowMinimapPacket(Player player, int mapId)
	{
		_mapId = mapId;
		_period = SevenSigns.getInstance().getCurrentPeriod();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_mapId);
		writeC(_period);
	}
}
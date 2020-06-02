package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.entity.boat.Boat;
import l2s.gameserver.utils.Location;

public class ExStopMoveAirShipPacket extends L2GameServerPacket
{
	private int boat_id;
	private Location _loc;

	public ExStopMoveAirShipPacket(Boat boat)
	{
		boat_id = boat.getObjectId();
		_loc = boat.getLoc();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(boat_id);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(_loc.h);
	}
}
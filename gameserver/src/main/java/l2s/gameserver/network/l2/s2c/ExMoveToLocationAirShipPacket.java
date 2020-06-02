package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.entity.boat.Boat;
import l2s.gameserver.utils.Location;

public class ExMoveToLocationAirShipPacket extends L2GameServerPacket
{
	private int _objectId;
	private Location _origin, _destination;

	public ExMoveToLocationAirShipPacket(Boat boat)
	{
		_objectId = boat.getObjectId();
		_origin = boat.getLoc();
		_destination = boat.getDestination();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objectId);

		writeD(_destination.x);
		writeD(_destination.y);
		writeD(_destination.z);
		writeD(_origin.x);
		writeD(_origin.y);
		writeD(_origin.z);
	}
}
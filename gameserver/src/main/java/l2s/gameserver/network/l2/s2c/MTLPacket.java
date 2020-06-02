package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;
import l2s.gameserver.utils.Location;

public class MTLPacket extends L2GameServerPacket
{
	private int _objectId;
	private Location _current;
	private Location _destination;

	public MTLPacket(Creature cha)
	{
		_objectId = cha.getObjectId();
		_current = cha.getLoc();
		_destination = cha.getDestination();

		if(_destination == null)
		{
			_destination = _current;
		}
	}

	public MTLPacket(int objectId, Location from, Location to)
	{
		_objectId = objectId;
		_current = from;
		_destination = to;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objectId);

		writeD(_destination.x);
		writeD(_destination.y);
		writeD(_destination.z);

		writeD(_current.x);
		writeD(_current.y);
		writeD(_current.z);
	}
}
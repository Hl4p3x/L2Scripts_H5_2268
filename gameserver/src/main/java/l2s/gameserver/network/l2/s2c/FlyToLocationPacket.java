package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;
import l2s.gameserver.utils.Location;

public class FlyToLocationPacket extends L2GameServerPacket
{
	private int _chaObjId;
	private final FlyType _type;
	private Location _loc;
	private Location _destLoc;

	public enum FlyType
	{
		THROW_UP,
		THROW_HORIZONTAL,
		DUMMY,
		CHARGE,
		NONE
	}

	public FlyToLocationPacket(Creature cha, Location destLoc, FlyType type)
	{
		_destLoc = destLoc;
		_type = type;
		_chaObjId = cha.getObjectId();
		_loc = cha.getLoc();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_chaObjId);
		writeD(_destLoc.x);
		writeD(_destLoc.y);
		writeD(_destLoc.z);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(_type.ordinal());
		writeD(600);
		writeD(600);
		writeD(0);
	}

	@Override
	protected void writeImplHF()
	{
		writeD(_chaObjId);
		writeD(_destLoc.x);
		writeD(_destLoc.y);
		writeD(_destLoc.z);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(_type.ordinal());
	}
}
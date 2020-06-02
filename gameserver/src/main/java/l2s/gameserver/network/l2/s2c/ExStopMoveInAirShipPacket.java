package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.utils.Location;

public class ExStopMoveInAirShipPacket extends L2GameServerPacket
{
	private int char_id, boat_id, char_heading;
	private Location _loc;

	public ExStopMoveInAirShipPacket(Player cha)
	{
		char_id = cha.getObjectId();
		boat_id = cha.getBoat().getObjectId();
		_loc = cha.getInBoatPosition();
		char_heading = cha.getHeading();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(char_id);
		writeD(boat_id);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(char_heading);
	}
}
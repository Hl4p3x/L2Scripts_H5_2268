package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.boat.Boat;
import l2s.gameserver.utils.Location;

public class ExMoveToTargetInAirShipPacket extends L2GameServerPacket
{
	private int char_id, boat_id, target_id, _dist;
	private Location _loc;

	public ExMoveToTargetInAirShipPacket(Player cha, Boat boat, int targetId, int dist, Location origin)
	{
		char_id = cha.getObjectId();
		boat_id = boat.getObjectId();
		target_id = targetId;
		_dist = dist;
		_loc = origin;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(char_id); // ID:%d
		writeD(target_id); // TargetID:%d
		writeD(_dist); //Dist:%d
		writeD(_loc.y); //OriginX:%d
		writeD(_loc.z); //OriginY:%d
		writeD(_loc.h); //OriginZ:%d
		writeD(boat_id); //AirShipID:%d
	}
}
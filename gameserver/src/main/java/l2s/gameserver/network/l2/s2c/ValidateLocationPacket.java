package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.GameObject;
import l2s.gameserver.utils.Location;

/**
 * format   dddddd		(player id, target id, distance, startx, starty, startz)<p>
 */
public class ValidateLocationPacket extends L2GameServerPacket
{
	private int _chaObjId;
	private Location _loc;

	public ValidateLocationPacket(GameObject cha)
	{
		_chaObjId = cha.getObjectId();
		_loc = cha.getLoc();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x79);
		writeD(_chaObjId);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(_loc.h);
	}
}
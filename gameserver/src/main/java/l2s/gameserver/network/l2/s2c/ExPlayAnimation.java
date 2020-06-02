package l2s.gameserver.network.l2.s2c;

public class ExPlayAnimation extends L2GameServerPacket
{
	private final int _objectId;
	private final int _actionId;

	public ExPlayAnimation(int objectId, int actionId)
	{
		_objectId = objectId;
		_actionId = actionId;
	}
	@Override
	protected void writeImpl()
	{
		writeD(_objectId); // ObjectId
		writeC(0); // Does social action if set to 0. 1 does ???
		writeD(_actionId); // Social Action Id, Starts from 2 on players. 0 and 1 work on NPCs
		writeS(""); // TODO ???
	}
}
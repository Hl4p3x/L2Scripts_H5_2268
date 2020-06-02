package l2s.gameserver.network.l2.s2c;

public class ExNavitAdventPointInfoPacket extends L2GameServerPacket
{
	private int _points;

	public ExNavitAdventPointInfoPacket(int points)
	{
		_points = points;
	}

	//TODOGOD

	@Override
	protected boolean canWrite()
	{
		return false;
	}

	@Override
	protected boolean canWriteHF()
	{
		return true;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_points);
	}
}
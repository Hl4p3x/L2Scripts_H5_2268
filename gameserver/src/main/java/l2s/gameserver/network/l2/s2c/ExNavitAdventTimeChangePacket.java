package l2s.gameserver.network.l2.s2c;

public class ExNavitAdventTimeChangePacket extends L2GameServerPacket
{
	private int _active;
	private int _time;

	public ExNavitAdventTimeChangePacket(boolean active, int time)
	{
		_active = active ? 1 : 0;
		_time = 14400 - time;
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
		writeC(_active);
		writeD(_time); // in minutes
	}
}

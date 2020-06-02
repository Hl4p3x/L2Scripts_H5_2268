package l2s.gameserver.network.l2.s2c;

public class ExNavitAdventEffectPacket extends L2GameServerPacket
{
	private int _time;

	public ExNavitAdventEffectPacket(int time)
	{
		_time = time;
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
		writeD(_time);
	}
}
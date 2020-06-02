package l2s.gameserver.network.l2.s2c;

/**
 * @author Bonux
**/
public class ExEnterWorldPacket extends L2GameServerPacket
{
	private final int _serverTime;

	public ExEnterWorldPacket()
	{
		_serverTime = (int) (System.currentTimeMillis() / 1000);
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_serverTime);
	}

	@Override
	protected boolean canWriteHF() {
		return false;
	}
}
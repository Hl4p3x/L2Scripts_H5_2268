package l2s.gameserver.network.l2.s2c;

public class NetPingPacket extends L2GameServerPacket
{
	private final int _clientId;

	public NetPingPacket(int clientId)
	{
		_clientId = clientId;
	}

	protected void writeImpl()
	{
		writeD(_clientId);
	}
}

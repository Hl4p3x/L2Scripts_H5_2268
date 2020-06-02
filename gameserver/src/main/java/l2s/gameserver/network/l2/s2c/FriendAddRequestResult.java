package l2s.gameserver.network.l2.s2c;

/**
 * format: cS
 */
public class FriendAddRequestResult extends L2GameServerPacket
{
	private String _requestorName;

	public FriendAddRequestResult(String requestorName)
	{
		_requestorName = requestorName;
	}

	@Override
	protected final void writeImpl()
	{
		writeS(_requestorName);
	}
}
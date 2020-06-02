package l2s.gameserver.model.base;

/**
 * @author Nik64
 */
public enum FenceState
{
	HIDDEN(0),
	OPENED(1),
	CLOSED(2),
	CLOSED_HIDDEN(0);

	public static final FenceState[] VALUES = FenceState.values();

	final int _clientId;

	private FenceState(int clientId)
	{
		_clientId = clientId;
	}

	public int getClientId()
	{
		return _clientId;
	}

	public boolean isGeodataEnabled()
	{
		return (this == CLOSED_HIDDEN) || (this == CLOSED);
	}
}

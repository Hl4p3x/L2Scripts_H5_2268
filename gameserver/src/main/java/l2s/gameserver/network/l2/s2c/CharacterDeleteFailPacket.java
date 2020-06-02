package l2s.gameserver.network.l2.s2c;

public class CharacterDeleteFailPacket extends L2GameServerPacket
{
	public static final L2GameServerPacket REASON_DELETION_FAILED = new CharacterDeleteFailPacket(0x01);
	public static final L2GameServerPacket REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER = new CharacterDeleteFailPacket(0x02);
	public static final L2GameServerPacket REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED = new CharacterDeleteFailPacket(0x03);

	private final int _error;

	private CharacterDeleteFailPacket(int error)
	{
		_error = error;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_error);
	}
}
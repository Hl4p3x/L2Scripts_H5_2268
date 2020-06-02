package l2s.gameserver.network.l2.s2c;

public class SurrenderPledgeWarPacket extends L2GameServerPacket
{
	private String _pledgeName;
	private String _char;

	public SurrenderPledgeWarPacket(String pledge, String charName)
	{
		_pledgeName = pledge;
		_char = charName;
	}

	@Override
	protected final void writeImpl()
	{
		writeS(_pledgeName);
		writeS(_char);
	}
}
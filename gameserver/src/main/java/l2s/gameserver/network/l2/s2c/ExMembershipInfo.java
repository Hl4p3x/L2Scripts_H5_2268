package l2s.gameserver.network.l2.s2c;

public class ExMembershipInfo extends L2GameServerPacket
{
	private int i;

	public ExMembershipInfo(int paramInt)
	{
		i = paramInt;
	}

	@Override
	protected void writeImpl()
	{
		writeD(0);
		writeD(0);
		writeD(i);
	}
}
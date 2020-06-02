package l2s.gameserver.network.l2.s2c;

public class ExPeriodicItemList extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeD(0); // count of DD
		//writeD(0);
		//writeD(0);
	}
}
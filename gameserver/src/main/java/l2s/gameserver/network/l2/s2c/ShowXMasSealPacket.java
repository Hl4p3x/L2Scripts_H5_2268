package l2s.gameserver.network.l2.s2c;

public class ShowXMasSealPacket extends L2GameServerPacket
{
	private int _item;

	public ShowXMasSealPacket(int item)
	{
		_item = item;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_item);
	}
}
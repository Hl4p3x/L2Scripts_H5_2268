package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Servitor;

public class SetSummonRemainTimePacket extends L2GameServerPacket
{
	private final int _maxFed;
	private final int _curFed;

	public SetSummonRemainTimePacket(Servitor servitor)
	{
		_curFed = servitor.getCurrentFed();
		_maxFed = servitor.getMaxFed();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_maxFed);
		writeD(_curFed);
	}
}
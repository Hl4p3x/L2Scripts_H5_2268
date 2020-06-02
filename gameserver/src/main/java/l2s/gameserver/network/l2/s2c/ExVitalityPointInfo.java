package l2s.gameserver.network.l2.s2c;

public class ExVitalityPointInfo extends L2GameServerPacket
{
	private final int _vitality;

	public ExVitalityPointInfo(int vitality)
	{
		_vitality = vitality;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_vitality * 7);
	}

	@Override
	protected void writeImplHF()
	{
		writeD(_vitality);
	}
}
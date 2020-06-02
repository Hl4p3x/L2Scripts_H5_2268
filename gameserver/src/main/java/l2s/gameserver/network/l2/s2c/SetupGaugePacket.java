package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;

public class SetupGaugePacket extends L2GameServerPacket
{
	public static final int BLUE = 0;
	public static final int RED = 1;
	public static final int CYAN = 2;

	private int _charId;
	private int _dat1;
	private int _time;

	public SetupGaugePacket(Creature character, int dat1, int time)
	{
		_charId = character.getObjectId();
		_dat1 = dat1;// color  0-blue   1-red  2-cyan  3-
		_time = time;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_charId);
		writeD(_dat1);
		writeD(_time);

		writeD(_time); //c2
	}
}
package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.templates.Henna;

//ccccccdd[dd]
public class GMHennaInfoPacket extends L2GameServerPacket
{
	private int _count, _str, _con, _dex, _int, _wit, _men;
	private final Henna[] _hennas = new Henna[3];

	public GMHennaInfoPacket(final Player cha)
	{
		_str = cha.getHennaStatSTR();
		_con = cha.getHennaStatCON();
		_dex = cha.getHennaStatDEX();
		_int = cha.getHennaStatINT();
		_wit = cha.getHennaStatWIT();
		_men = cha.getHennaStatMEN();

		int j = 0;
		for(int i = 0; i < 3; i++)
		{
			Henna h = cha.getHenna(i + 1);
			if(h != null)
				_hennas[j++] = h;
		}
		_count = j;
	}

	@Override
	protected final void writeImpl()
	{
		writeH(_int);
		writeH(_str);
		writeH(_con);
		writeH(_men);
		writeH(_dex);
		writeH(_wit);
		writeH(0); //equip LUC
		writeH(0); //equip CHA
		writeD(3); //interlude, slots?
		writeD(_count);
		for(int i = 0; i < _count; i++)
		{
			writeD(_hennas[i].getSymbolId());
			writeD(_hennas[i].getSymbolId());
		}
		writeD(0x00);	// Premium symbol ID
		writeD(0x00);	// Premium symbol active
		writeD(0x00);	// Premium symbol left time
	}

	@Override
	protected final void writeImplHF()
	{
		writeC(_int);
		writeC(_str);
		writeC(_con);
		writeC(_men);
		writeC(_dex);
		writeC(_wit);
		writeD(3);
		writeD(_count);
		for(int i = 0; i < _count; i++)
		{
			writeD(_hennas[i].getSymbolId());
			writeD(_hennas[i].getSymbolId());
		}
	}
}
package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

public class HennaInfoPacket extends L2GameServerPacket
{
	private final Henna[] _hennas = new Henna[3];
	private final int _str, _con, _dex, _int, _wit, _men;
	private int _count;

	public HennaInfoPacket(Player player)
	{
		_count = 0;
		l2s.gameserver.templates.Henna h;
		for(int i = 0; i < 3; i++)
			if((h = player.getHenna(i + 1)) != null)
				_hennas[_count++] = new Henna(h.getSymbolId(), h.isForThisClass(player));

		_str = player.getHennaStatSTR();
		_con = player.getHennaStatCON();
		_dex = player.getHennaStatDEX();
		_int = player.getHennaStatINT();
		_wit = player.getHennaStatWIT();
		_men = player.getHennaStatMEN();
	}

	@Override
	protected final void writeImpl()
	{
		writeH(_int); //equip INT
		writeH(_str); //equip STR
		writeH(_con); //equip CON
		writeH(_men); //equip MEM
		writeH(_dex); //equip DEX
		writeH(_wit); //equip WIT
		writeH(0); //equip LUC
		writeH(0); //equip CHA
		writeD(3); //interlude, slots?
		writeD(_count);
		for (int i = 0; i < _count; i++)
		{
			writeD(_hennas[i]._symbolId);
			writeD(true);
		}

		writeD(0x00);	// Premium symbol ID
		writeD(0x00);	// Premium symbol left time
		writeD(0x00);	// Premium symbol active
	}

	@Override
	protected final void writeImplHF()
	{
		writeC(_int); //equip INT
		writeC(_str); //equip STR
		writeC(_con); //equip CON
		writeC(_men); //equip MEM
		writeC(_dex); //equip DEX
		writeC(_wit); //equip WIT
		writeD(3); //interlude, slots?
		writeD(_count);
		for(int i = 0; i < _count; i++)
		{
			writeD(_hennas[i]._symbolId);
			writeD(_hennas[i]._valid ? _hennas[i]._symbolId : 0);
		}
	}

	private static class Henna
	{
		private int _symbolId;
		private boolean _valid;

		public Henna(int sy, boolean valid)
		{
			_symbolId = sy;
			_valid = valid;
		}
	}
}
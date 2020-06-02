package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.templates.Henna;

public class HennaUnequipInfoPacket extends L2GameServerPacket
{
	private int _str, _con, _dex, _int, _wit, _men;
	private long _adena;
	private Henna _henna;

	public HennaUnequipInfoPacket(Henna henna, Player player)
	{
		_henna = henna;
		_adena = player.getAdena();
		_str = player.getSTR();
		_dex = player.getDEX();
		_con = player.getCON();
		_int = player.getINT();
		_wit = player.getWIT();
		_men = player.getMEN();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_henna.getSymbolId()); //symbol Id
		writeD(_henna.getDyeId()); //item id of dye

		writeQ(_henna.getDrawCount());
		writeQ(_henna.getPrice());
		writeD(1); //able to draw or not 0 is false and 1 is true
		writeQ(_adena);

		writeD(_int); //current INT
		writeD(_int + _henna.getStatINT()); //equip INT
		writeD(_str); //current STR
		writeD(_str + _henna.getStatSTR()); //equip STR
		writeD(_con); //current CON
		writeD(_con + _henna.getStatCON()); //equip CON
		writeD(_men); //current MEM
		writeD(_men + _henna.getStatMEN()); //equip MEM
		writeD(_dex); //current DEX
		writeD(_dex + _henna.getStatDEX()); //equip DEX
		writeD(_wit); //current WIT
		writeD(_wit + _henna.getStatWIT()); //equip WIT
		writeD(0); //current LUC
		writeD(0); //equip LUC
		writeD(0); //current CHA
		writeD(0); //equip CHA
		writeD(0); // UNK
	}

	@Override
	protected final void writeImplHF()
	{
		writeD(_henna.getSymbolId()); //symbol Id
		writeD(_henna.getDyeId()); //item id of dye

		writeQ(_henna.getDrawCount());
		writeQ(_henna.getPrice());
		writeD(1); //able to draw or not 0 is false and 1 is true
		writeQ(_adena);

		writeD(_int); //current INT
		writeC(_int + _henna.getStatINT()); //equip INT
		writeD(_str); //current STR
		writeC(_str + _henna.getStatSTR()); //equip STR
		writeD(_con); //current CON
		writeC(_con + _henna.getStatCON()); //equip CON
		writeD(_men); //current MEM
		writeC(_men + _henna.getStatMEN()); //equip MEM
		writeD(_dex); //current DEX
		writeC(_dex + _henna.getStatDEX()); //equip DEX
		writeD(_wit); //current WIT
		writeC(_wit + _henna.getStatWIT()); //equip WIT
	}
}
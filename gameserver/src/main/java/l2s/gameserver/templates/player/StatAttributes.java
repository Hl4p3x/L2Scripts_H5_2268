package l2s.gameserver.templates.player;

/**
 * @author Bonux
 */
public class StatAttributes
{
	private final int _int, _str, _con, _men, _dex, _wit;

	public StatAttributes(int _int, int str, int con, int men, int dex, int wit)
	{
		this._int = _int;
		_str = str;
		_con = con;
		_men = men;
		_dex = dex;
		_wit = wit;
	}

	public int getINT()
	{
		return _int;
	}

	public int getSTR()
	{
		return _str;
	}

	public int getCON()
	{
		return _con;
	}

	public int getMEN()
	{
		return _men;
	}

	public int getDEX()
	{
		return _dex;
	}

	public int getWIT()
	{
		return _wit;
	}
}
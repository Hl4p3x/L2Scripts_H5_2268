package l2s.gameserver.templates.premiumaccount;

/**
 * @author Bonux
**/
public class PremiumAccountRates
{
	private final double _exp;
	private final double _sp;
	private final double _adena;
	private final double _drop;
	private final double _spoil;
	private final double _questDrop;
	private final double _questReward;
	private final double _siegeFameReward;

	public PremiumAccountRates(double exp, double sp, double adena, double drop, double spoil, double questDrop, double questReward, double siegeFameReward)
	{
		_exp = exp;
		_sp = sp;
		_adena = adena;
		_drop = drop;
		_spoil = spoil;
		_questDrop = questDrop;
		_questReward = questReward;
		_siegeFameReward = siegeFameReward;
	}

	public double getExp()
	{
		return _exp;
	}

	public double getSp()
	{
		return _sp;
	}

	public double getAdena()
	{
		return _adena;
	}

	public double getDrop()
	{
		return _drop;
	}

	public double getSpoil()
	{
		return _spoil;
	}

	public double getQuestDrop()
	{
		return _questDrop;
	}

	public double getQuestReward()
	{
		return _questReward;
	}

	public double getSiegeFameReward()
	{
		return _siegeFameReward;
	}
}
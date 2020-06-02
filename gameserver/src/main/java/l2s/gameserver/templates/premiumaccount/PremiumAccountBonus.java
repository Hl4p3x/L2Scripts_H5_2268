package l2s.gameserver.templates.premiumaccount;

/**
 * @author Bonux
**/
public class PremiumAccountBonus
{
	private final double _enchantChance;

	public PremiumAccountBonus(double enchantChance)
	{
		_enchantChance = enchantChance;
	}

	public double getEnchantChance()
	{
		return _enchantChance;
	}
}
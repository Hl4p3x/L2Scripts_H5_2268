package l2s.gameserver.templates.item.support;

import java.util.Set;

import l2s.gameserver.templates.item.ItemTemplate.Grade;

/**
 * @author Bonux
**/
public class EnchantStone
{
	private final int _itemId;
	private final double _chance;
	private final EnchantType _type;
	private final Set<Grade> _grades;
	private final FailResultType _resultType;
	private final int _enchantDropCount;
	private final int _minEnchantLevel;
	private final int _minFullbodyEnchantLevel;
	private final int _maxEnchantLevel;
	private final int _minEnchantStep;
	private final int _maxEnchantStep;

	public EnchantStone(int itemId, double chance, EnchantType type, Set<Grade> grades, FailResultType resultType, int enchantDropCount, int minEnchantLevel, int minFullbodyEnchantLevel, int maxEnchantLevel, int minEnchantStep, int maxEnchantStep)
	{
		_itemId = itemId;
		_chance = chance;
		_type = type;
		_grades = grades;
		_resultType = resultType;
		_enchantDropCount = enchantDropCount;
		_minEnchantLevel = minEnchantLevel;
		_minFullbodyEnchantLevel = minFullbodyEnchantLevel;
		_maxEnchantLevel = maxEnchantLevel;
		_minEnchantStep = minEnchantStep;
		_maxEnchantStep = maxEnchantStep;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public double getChance()
	{
		return _chance;
	}

	public boolean containsGrade(Grade grade)
	{
		return _grades.contains(grade);
	}

	public EnchantType getType()
	{
		return _type;
	}

	public FailResultType getResultType()
	{
		return _resultType;
	}

	public int getEnchantDropCount()
	{
		return _enchantDropCount;
	}

	public double getMinEnchantLevel()
	{
		return _minEnchantLevel;
	}

	public double getMinFullbodyEnchantLevel()
	{
		return _minFullbodyEnchantLevel;
	}

	public double getMaxEnchantLevel()
	{
		return _maxEnchantLevel;
	}

	public int getMinEnchantStep()
	{
		return _minEnchantStep;
	}

	public int getMaxEnchantStep()
	{
		return _maxEnchantStep;
	}
}

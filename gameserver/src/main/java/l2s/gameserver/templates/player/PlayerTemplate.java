package l2s.gameserver.templates.player;

import java.util.List;

import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.util.Rnd;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.base.Sex;
import l2s.gameserver.templates.CharTemplate;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.item.StartItem;
import l2s.gameserver.utils.Location;


public final class PlayerTemplate extends CharTemplate
{
	private final Race _race;
	private final Sex _sex;
	private final StatAttributes _minAttr;
	private final StatAttributes _maxAttr;
	private final StatAttributes _baseAttr;
	private final BaseArmorDefence _armDef;
	private final BaseJewelDefence _jewlDef;
	private final int _baseRandDam;
	private final double _baseSafeFallHeight;
	private final double _baseBreathBonus;
	private final double _baseFlyRunSpd;
	private final double _baseFlyWalkSpd;
	private final double _baseRideRunSpd;
	private final double _baseRideWalkSpd;
	private final List<Location> _startLocs;
	private final List<StartItem> _startItems;
	private final TIntObjectHashMap<LvlUpData> _lvlUpData;

	public PlayerTemplate(StatsSet set, Race race, Sex sex, StatAttributes minAttr, StatAttributes maxAttr, StatAttributes baseAttr, BaseArmorDefence armDef, BaseJewelDefence jewlDef, List<Location> startLocs, List<StartItem> startItems, TIntObjectHashMap<LvlUpData> lvlUpData)
	{
		super(set);

		_race = race;
		_sex = sex;
		_minAttr = minAttr;
		_maxAttr = maxAttr;
		_baseAttr = baseAttr;
		_armDef = armDef;
		_jewlDef = jewlDef;
		_startLocs = startLocs;
		_startItems = startItems;
		_lvlUpData = lvlUpData;

		_baseRandDam = set.getInteger("baseRandDam");
		_baseSafeFallHeight = set.getDouble("baseSafeFallHeight");
		_baseBreathBonus = set.getDouble("baseBreathBonus");
		_baseFlyRunSpd = set.getDouble("baseFlyRunSpd");
		_baseFlyWalkSpd = set.getDouble("baseFlyWalkSpd");
		_baseRideRunSpd = set.getDouble("baseRideRunSpd");
		_baseRideWalkSpd = set.getDouble("baseRideWalkSpd");
	}

	public Race getRace()
	{
		return _race;
	}

	public Sex getSex()
	{
		return _sex;
	}

	public StatAttributes getMinAttr()
	{
		return _minAttr;
	}

	public StatAttributes getMaxAttr()
	{
		return _maxAttr;
	}

	@Override
	public StatAttributes getBaseAttr()
	{
		return _baseAttr;
	}

	public BaseArmorDefence getArmDef()
	{
		return _armDef;
	}

	public BaseJewelDefence getJewlDef()
	{
		return _jewlDef;
	}

	public int getBaseRandDam()
	{
		return _baseRandDam;
	}

	public double getBaseBreathBonus()
	{
		return _baseBreathBonus;
	}

	public double getBaseSafeFallHeight()
	{
		return _baseSafeFallHeight;
	}

	public double getBaseHpReg(int lvl)
	{
		return getLvlUpData(lvl).getHP();
	}

	public double getBaseMpReg(int lvl)
	{
		return getLvlUpData(lvl).getMP();
	}

	public double getBaseCpReg(int lvl)
	{
		return getLvlUpData(lvl).getCP();
	}

	public double getBaseFlyRunSpd()
	{
		return _baseFlyRunSpd;
	}

	public double getBaseFlyWalkSpd()
	{
		return _baseFlyWalkSpd;
	}

	public double getBaseRideRunSpd()
	{
		return _baseRideRunSpd;
	}

	public double getBaseRideWalkSpd()
	{
		return _baseRideWalkSpd;
	}

	public LvlUpData getLvlUpData(int lvl)
	{
		return _lvlUpData.get(lvl);
	}

	public StartItem[] getStartItems()
	{
		return _startItems.toArray(new StartItem[_startItems.size()]);
	}

	public Location getStartLocation()
	{
		return _startLocs.get(Rnd.get(_startLocs.size()));
	}
}
package l2s.gameserver.data.xml.holder;

import gnu.trove.map.hash.TIntDoubleHashMap;
import l2s.commons.data.xml.AbstractHolder;

public final class LevelBonusHolder extends AbstractHolder
{
	private static final LevelBonusHolder _instance = new LevelBonusHolder();
	private final TIntDoubleHashMap _bonusList = new TIntDoubleHashMap();

	public static LevelBonusHolder getInstance()
	{
		return _instance;
	}

	public void addLevelBonus(int lvl, double bonus)
	{
		_bonusList.put(lvl, bonus);
	}

	public double getLevelBonus(int lvl)
	{
		return _bonusList.get(lvl);
	}

	@Override
	public int size()
	{
		return _bonusList.size();
	}

	@Override
	public void clear()
	{
		_bonusList.clear();
	}
}

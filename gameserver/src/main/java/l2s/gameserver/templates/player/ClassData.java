package l2s.gameserver.templates.player;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * @author Bonux
 */
public class ClassData
{
	private final int _classId;

	private final TIntObjectHashMap<LvlUpData> _lvlsUpData = new TIntObjectHashMap<LvlUpData>();

	public ClassData(int classId)
	{
		_classId = classId;
	}

	public void addLvlUpData(int lvl, double hp, double mp, double cp)
	{
		_lvlsUpData.put(lvl, new LvlUpData(hp, mp, cp));
	}

	public LvlUpData getLvlUpData(int lvl)
	{
		return _lvlsUpData.get(lvl);
	}

	public int getClassId()
	{
		return _classId;
	}
}
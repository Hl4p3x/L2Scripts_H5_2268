package l2s.gameserver.data.xml.holder;

import java.util.Map;

import gnu.trove.map.hash.TIntIntHashMap;
import l2s.gameserver.model.Skill;
import l2s.gameserver.skills.SkillsEngine;

public class SkillHolder
{
	private static final SkillHolder _instance = new SkillHolder();

	private Map<Integer, Skill> _skills;
	private TIntIntHashMap _maxLevelsTable;
	private TIntIntHashMap _baseLevelsTable;

	public static final SkillHolder getInstance()
	{
		return _instance;
	}

	public void load()
	{
		_skills = SkillsEngine.getInstance().loadAllSkills();
		makeLevelsTable();
	}

	public void reload()
	{
		load();
	}

	public Skill getSkill(int skillId, int level)
	{
		return _skills.get(getSkillHashCode(skillId, level));
	}

	public int getMaxLevel(int skillId)
	{
		return _maxLevelsTable.get(skillId);
	}

	public int getBaseLevel(int skillId)
	{
		return _baseLevelsTable.get(skillId);
	}

	public static int getSkillHashCode(Skill skill)
	{
		return SkillHolder.getSkillHashCode(skill.getId(), skill.getLevel());
	}

	public static int getSkillHashCode(int skillId, int skillLevel)
	{
		return skillId * 1000 + skillLevel;
	}

	private void makeLevelsTable()
	{
		_maxLevelsTable = new TIntIntHashMap();
		_baseLevelsTable = new TIntIntHashMap();
		for(Skill s : _skills.values())
		{
			int skillId = s.getId();
			int level = s.getLevel();
			int maxLevel = _maxLevelsTable.get(skillId);
			if(level > maxLevel)
				_maxLevelsTable.put(skillId, level);
			if(_baseLevelsTable.get(skillId) == 0)
				_baseLevelsTable.put(skillId, s.getBaseLevel());
		}
	}
}
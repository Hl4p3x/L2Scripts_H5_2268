package l2s.gameserver.utils;

import l2s.gameserver.data.xml.holder.SkillHolder;

/**
 * @author Bonux
**/
public final class SkillUtils
{
	public static int generateSkillHashCode(int id, int level)
	{
		return id * 1000 + level;
	}

	public static int getSkillLevel(int enchantType, int enchantLevel)
	{
		return (100 * enchantType) + enchantLevel;
	}

	public static boolean isEnchantedSkill(int level)
	{
		return getSkillEnchantLevel(level) > 0;
	}

	public static int getSkillEnchantType(int level)
	{
		return level / 100;
	}

	public static int getSkillEnchantLevel(int level)
	{
		if(level > 100)
			return level % 100;
		return 0;
	}

	public static int getSkillLevelMask(int skillLevel, int subSkillLevel)
	{
		return skillLevel | (subSkillLevel << 16);
	}

	public static int getSkillLevelFromMask(int skillLevelMask)
	{	
		final int mask = 0b1111111111111111;
		return mask & skillLevelMask;
	}

	public static int getSubSkillLevelFromMask(int skillLevelMask)
	{	
		final int mask = 0b1111111111111111;
		return mask & skillLevelMask >>> 16;
	}

	public static int convertHFSkillLevelToGODMask(int id, int level)
	{
		int enchantLevel = getSkillEnchantLevel(level);
		if(enchantLevel != 0)
		{
			int baseLevel = SkillHolder.getInstance().getBaseLevel(id);
			int subLevel = getSkillEnchantType(level) * 1000 + enchantLevel;
			return getSkillLevelMask(baseLevel, subLevel);
		}
		return level;
	}

	public static int convertGODSkillLevelToHF(int id, int levelMask)
	{
		return convertGODSkillLevelToHF(id, getSkillLevelFromMask(levelMask), getSubSkillLevelFromMask(levelMask));
	}

	public static int convertGODSkillLevelToHF(int id, int level, int subLevel)
	{
		if(subLevel == 0 || SkillHolder.getInstance().getBaseLevel(id) != level)
			return level;
		return (subLevel / 1000) * 100 + (subLevel % 1000);
	}
}

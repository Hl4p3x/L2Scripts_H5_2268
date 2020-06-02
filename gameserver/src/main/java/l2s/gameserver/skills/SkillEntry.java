package l2s.gameserver.skills;

import java.util.AbstractMap;

import l2s.gameserver.model.Skill;

/**
 * @author VISTALL
 * @date 0:15/03.06.2011
 */
public class SkillEntry extends AbstractMap.SimpleImmutableEntry<SkillEntryType, Skill>
{
	private boolean _disabled;

	public SkillEntry(SkillEntryType key, Skill value)
	{
		super(key, value);
	}

	public boolean isDisabled()
	{
		return _disabled;
	}

	public void setDisabled(boolean disabled)
	{
		_disabled = disabled;
	}
}

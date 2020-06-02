package l2s.gameserver.listener.actor;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;

/**
 * @author Bonux
**/
public interface OnSkillAddListener extends CharListener
{
	public void onSkillAdd(Creature actor, Skill newSkill, Skill oldSkill);
}

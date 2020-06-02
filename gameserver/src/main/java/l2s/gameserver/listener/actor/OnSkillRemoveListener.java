package l2s.gameserver.listener.actor;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;

/**
 * @author Bonux
**/
public interface OnSkillRemoveListener extends CharListener
{
	public void onSkillRemove(Creature actor, Skill skill);
}

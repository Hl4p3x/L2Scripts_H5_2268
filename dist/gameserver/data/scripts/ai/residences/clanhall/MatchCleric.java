package ai.residences.clanhall;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @author VISTALL
 * @date 16:38/22.04.2011
 */
public class MatchCleric extends MatchFighter
{
	public static final Skill HEAL = SkillHolder.getInstance().getSkill(4056, 6);

	public MatchCleric(NpcInstance actor)
	{
		super(actor);
	}

	public void heal()
	{
		NpcInstance actor = getActor();
		addTaskCast(actor, HEAL);
		doTask();
	}
}

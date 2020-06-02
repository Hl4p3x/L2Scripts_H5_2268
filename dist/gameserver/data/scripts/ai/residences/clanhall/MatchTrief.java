package ai.residences.clanhall;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @author VISTALL
 * @date 16:38/22.04.2011
 */
public class MatchTrief extends MatchFighter
{
	public static final Skill HOLD = SkillHolder.getInstance().getSkill(4047, 6);

	public MatchTrief(NpcInstance actor)
	{
		super(actor);
	}

	public void hold()
	{
		NpcInstance actor = getActor();
		addTaskCast(actor, HOLD);
		doTask();
	}
}

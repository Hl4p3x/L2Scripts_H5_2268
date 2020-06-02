package ai.residences.clanhall;

import l2s.commons.util.Rnd;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @author VISTALL
 * @date 16:29/22.04.2011
 */
public class MatchBerserker extends MatchFighter
{
	public static final Skill ATTACK_SKILL = SkillHolder.getInstance().getSkill(4032, 6);

	public MatchBerserker(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	public void onEvtAttacked(Creature attacker, int dam)
	{
		super.onEvtAttacked(attacker, dam);

		if(Rnd.chance(10))
			addTaskCast(attacker, ATTACK_SKILL);
	}
}

package ai.other.PailakaDevilsLegacy;

import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;

public class PowderKeg extends DefaultAI
{
	public PowderKeg(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return;

		if(damage > 0)
		{
			actor.setTarget(actor);
			Skill skill = SkillHolder.getInstance().getSkill(5714, 1);
			if(skill != null)
			{
				Creature target = skill.getAimingTarget(actor, attacker);
				actor.doCast(skill, target, true);
			}
		}
		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}

	@Override
	protected boolean randomAnimation()
	{
		return false;
	}
}
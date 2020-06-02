package npc.model;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * @author Bonux
**/
public class PowderKegInstance extends NpcInstance
{
	public PowderKegInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return true;
	}

	@Override
	public void onMagicUseTimer(Creature aimingTarget, Skill skill, boolean forceUse)
	{
		super.onMagicUseTimer(aimingTarget, skill, forceUse);

		if(skill.getId() == 5714)
			doDie(null);
	}
}
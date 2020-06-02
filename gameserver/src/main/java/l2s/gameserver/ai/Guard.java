package l2s.gameserver.ai;

import l2s.gameserver.Config;
import l2s.gameserver.model.AggroList.AggroInfo;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.NpcInstance;

public class Guard extends Fighter
{
	public Guard(NpcInstance actor)
	{
		super(actor);
	}

	public boolean canAttackCharacter(Creature target)
	{
		NpcInstance actor = getActor();
		if(getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
		{
			AggroInfo ai = actor.getAggroList().get(target);
			return ai != null && ai.hate > 0;
		}
		return target.isMonster() || target.isPlayable();
	}
	
	public boolean checkAggression(Creature target)
	{
		NpcInstance actor = getActor();
		if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE || !isGlobalAggro())
			return false;

		if(target.isPlayable())
		{
			if(target.getKarma() == 0 || (actor.getParameter("evilGuard", false) && target.getPvpFlag() > 0))
				return false;
		}
		if(target.isMonster())
		{
			if(!((MonsterInstance)target).isAggressive() || !Config.GUARD_CAN_KILL_AGGRO)
				return false;
		}

		return super.checkAggression(target);
	}

	public int getMaxAttackTimeout()
	{
		return 0;
	}
	
	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}
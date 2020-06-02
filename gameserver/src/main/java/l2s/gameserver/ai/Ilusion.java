package l2s.gameserver.ai;

import l2s.gameserver.model.AggroList.AggroInfo;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;

public class Ilusion extends Fighter
{
	public Ilusion(NpcInstance actor)
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
		Player player = actor.getPlayer();
		if(player == null || player.getTarget() == null || player.getTarget() != target)
			return false;
		if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
			return false;
		
		if(target.isPlayable())
		{
			if(target == player)
				return false;
			return true;	
		}
		if(target.isMonster())
		{
			return true;
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
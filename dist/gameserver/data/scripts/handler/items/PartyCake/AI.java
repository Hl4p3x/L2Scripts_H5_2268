package handler.items.PartyCake;

import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.NpcInstance;

public class AI extends DefaultAI
{
	public AI(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor == null)
		{
			return true;
		}

		int skillId = 22035;
		for(Player player : World.getAroundPlayers(actor, 200, 200))
		{
			if(player != null && !player.isInZonePeace() && player.getEffectList().getEffectsBySkillId(skillId) == null)
			{
				actor.doCast(SkillHolder.getInstance().getSkill(skillId, 1), player, true);
			}
		}
		return false;
	}

	@Override
	protected boolean randomAnimation()
	{
		return false;
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}
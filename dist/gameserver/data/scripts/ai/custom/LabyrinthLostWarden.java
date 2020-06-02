package ai.custom;

import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.funcs.FuncSet;
import npc.model.LostCaptainInstance;

/**
 * @author pchayka
 */
public class LabyrinthLostWarden extends Fighter
{

	public LabyrinthLostWarden(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();
		Reflection r = actor.getReflection();
		if(!r.isDefault())
			if(checkMates(actor.getNpcId()))
				if(findLostCaptain() != null)
					findLostCaptain().addStatFunc(new FuncSet(Stats.POWER_ATTACK, 0x30, this, findLostCaptain().getTemplate().getBasePAtk() * 0.66));
		super.onEvtDead(killer);
	}

	private boolean checkMates(int id)
	{
		for(NpcInstance n : getActor().getReflection().getNpcs())
			if(n.getNpcId() == id && !n.isDead())
				return false;
		return true;
	}

	private NpcInstance findLostCaptain()
	{
		for(NpcInstance n : getActor().getReflection().getNpcs())
			if(n instanceof LostCaptainInstance)
				return n;
		return null;
	}
}
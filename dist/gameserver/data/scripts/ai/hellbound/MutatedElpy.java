package ai.hellbound;

import bosses.BelethManager;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.instancemanager.naia.NaiaCoreManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;
public class MutatedElpy extends Fighter
{
	public MutatedElpy(NpcInstance actor)
	{
		super(actor);
		actor.startImmobilized();
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		NaiaCoreManager.launchNaiaCore();
		BelethManager.setElpyDead();
		super.onEvtDead(killer);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		actor.doDie(attacker);
	}

}
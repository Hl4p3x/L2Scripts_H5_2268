package ai.freya;

import java.util.concurrent.ScheduledFuture;

import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @author pchayka
 */

public class IceKnightNormal extends Fighter
{
	private boolean iced;
	private ScheduledFuture<?> task;

	public IceKnightNormal(NpcInstance actor)
	{
		super(actor);
		setMaxPursueRange(6000);
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		NpcInstance actor = getActor();
		iced = true;
		actor.setNpcState(1);
		actor.block();
		Reflection r = actor.getReflection();
		if(r != null && r.getPlayers() != null)
			for(Player p : r.getPlayers())
				this.notifyEvent(CtrlEvent.EVT_AGGRESSION, p, 300);

		task = ThreadPoolManager.getInstance().schedule(new ReleaseFromIce(), 6000L);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();

		if(iced)
		{
			iced = false;
			if(task != null)
				task.cancel(false);
			actor.unblock();
			actor.setNpcState(2);
		}
		super.onEvtAttacked(attacker, damage);
	}

	private class ReleaseFromIce extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			if(iced)
			{
				iced = false;
				getActor().setNpcState(2);
				getActor().unblock();
			}
		}
	}

	@Override
	protected boolean teleportHome()
	{
		return false;
	}
}
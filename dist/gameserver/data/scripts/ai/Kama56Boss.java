package ai;

import java.util.List;

import l2s.commons.collections.LazyArrayList;
import l2s.commons.lang.reference.HardReference;
import l2s.commons.lang.reference.HardReferences;
import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.MinionList;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.MinionInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.scripts.Functions;


public class Kama56Boss extends Fighter
{
	private long _nextOrderTime = 0;
	private HardReference<Player> _lastMinionsTargetRef = HardReferences.emptyRef();

	public Kama56Boss(NpcInstance actor)
	{
		super(actor);
	}

	private void sendOrderToMinions(NpcInstance actor)
	{
		if(!actor.isInCombat())
		{
			_lastMinionsTargetRef = HardReferences.emptyRef();
			return;
		}

		MinionList ml = actor.getMinionList();
		if(ml == null || !ml.hasMinions())
		{
			_lastMinionsTargetRef = HardReferences.emptyRef();
			return;
		}

		long now = System.currentTimeMillis();
		if(_nextOrderTime > now && _lastMinionsTargetRef.get() != null)
		{
			Player old_target = _lastMinionsTargetRef.get();
			if(old_target != null && !old_target.isAlikeDead())
			{
				for(MinionInstance m : ml.getAliveMinions())
					if(m.getAI().getAttackTarget() != old_target)
						m.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, old_target, 10000000);
				return;
			}
		}

		_nextOrderTime = now + 30000;

		List<Player> pl = World.getAroundPlayers(actor);
		if(pl.isEmpty())
		{
			_lastMinionsTargetRef = HardReferences.emptyRef();
			return;
		}

		List<Player> alive = new LazyArrayList<Player>();
		for(Player p : pl)
			if(!p.isAlikeDead())
				alive.add(p);
		if(alive.isEmpty())
		{
			_lastMinionsTargetRef = HardReferences.emptyRef();
			return;
		}

		Player target = alive.get(Rnd.get(alive.size()));
		_lastMinionsTargetRef = target.getRef();

		Functions.npcSay(actor, NpcString.YOU_S1_ATTACK_THEM, target.getName());
		for(MinionInstance m : ml.getAliveMinions())
		{
			m.getAggroList().clear();
			m.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, target, 10000000);
		}
	}

	@Override
	protected void thinkAttack()
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return;

		sendOrderToMinions(actor);
		super.thinkAttack();
	}
}
package ai.SkyshadowMeadow;

import java.util.ArrayList;
import java.util.List;

import l2s.commons.threading.RunnableImpl;
import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.SocialActionPacket;
import l2s.gameserver.scripts.Functions;

/**
 * @author PaInKiLlEr
 *         - AI для моба Sel Mahum Drill Sergeant (22775), Sel Mahum Training Officer (22776), Sel Mahum Drill Sergeant (22778).
 *         - При атаке ругается в чат, агрит тренерующихся мобов.
 *         - Юзает рандомные социалки и заставляет тренерующихся мобов повторять за ним 3 раза.
 *         - AI проверен и работает.
 */
public class SelMahumTrainer extends Fighter
{
	private long _wait_timeout = System.currentTimeMillis() + 20000;
	private List<NpcInstance> _arm = new ArrayList<NpcInstance>();
	private boolean _firstTimeAttacked = true;

	public static final NpcString[] _text = {NpcString.SCHOOL7, NpcString.SCHOOL8};

	public SelMahumTrainer(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		final int social = Rnd.get(4, 7);
		NpcInstance actor = getActor();
		if(actor == null)
			return true;

		if(_wait_timeout < System.currentTimeMillis())
		{
			if(_arm == null || _arm.isEmpty())
			{
				for(NpcInstance npc : getActor().getAroundNpc(750, 750))
					_arm.add(npc);
			}

			_wait_timeout = (System.currentTimeMillis() + Rnd.get(20, 30) * 1000);

			actor.broadcastPacket(new SocialActionPacket(actor.getObjectId(), social));
			actor.setHeading(actor.getSpawnedLoc().h);

			int time = 2000;
			for(int i = 0; i <= 2; i++)
			{
				ThreadPoolManager.getInstance().schedule(new RunnableImpl()
				{
					@Override
					public void runImpl()
					{
						for(NpcInstance voin : _arm)
						{
							voin.setHeading(voin.getSpawnedLoc().h);
							voin.broadcastPacket(new SocialActionPacket(voin.getObjectId(), social));
						}
					}
				}, time);
				time += 2000;
			}
		}
		return true;
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance npc = null;
		if(_arm != null && !_arm.isEmpty()) 
			npc = _arm.get(_arm.size()-1);
		NpcInstance actor = getActor();
		if(actor == null)
			return;
		if(npc != null)
			npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Rnd.get(1, 100));

		if(attacker.isDead())
			actor.moveToLocation(actor.getSpawnedLoc(), 0, true);

		if(_firstTimeAttacked)
		{
			_firstTimeAttacked = false;
			Functions.npcSay(actor, _text[Rnd.get(_text.length)]);
		}

		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		_firstTimeAttacked = true;
		super.onEvtDead(killer);
	}
}
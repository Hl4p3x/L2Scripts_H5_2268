package ai.hellbound;

import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * AI Миньона боса Ranku.<br>
 * При смерти превращается в злого миньона Eidolon
 *
 * @author pchayka
 */
public class RankuScapegoat extends DefaultAI
{
	private static final int Eidolon_ID = 25543;

	public RankuScapegoat(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();
		NpcInstance mob = actor.getReflection().addSpawnWithoutRespawn(Eidolon_ID, actor.getLoc(), 0);
		NpcInstance boss = getBoss();
		if(mob != null && boss != null)
		{
			Creature cha = boss.getAggroList().getTopDamager(killer);
			if(cha != null)
				mob.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, cha, 100000);
		}
		super.onEvtDead(killer);
	}

	private NpcInstance getBoss()
	{
		Reflection r = getActor().getReflection();
		if(!r.isDefault())
			for(NpcInstance n : r.getNpcs())
				if(n.getNpcId() == 25542 && !n.isDead())
					return n;
		return null;
	}
}
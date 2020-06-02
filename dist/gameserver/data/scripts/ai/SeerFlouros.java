package ai;

import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.Mystic;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.SimpleSpawner;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.Location;

/**
 * AI Seer Flouros.<br>
 * - Спавнит "миньонов" при атаке.<br>
 * - _hps - таблица процентов hp, после которых спавнит "миньонов".<br>
 * @author n0nam3
 */
public class SeerFlouros extends Mystic
{
	private int _hpCount = 0;
	private static final int MOB = 18560;
	private static final int MOBS_COUNT = 2;
	private static final int[] _hps = { 80, 60, 40, 30, 20, 10, 5, -5 };

	public SeerFlouros(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(!actor.isDead())
			if(actor.getCurrentHpPercents() < _hps[_hpCount])
			{
				spawnMobs(attacker);
				_hpCount++;
			}
		super.onEvtAttacked(attacker, damage);
	}

	private void spawnMobs(Creature attacker)
	{
		NpcInstance actor = getActor();
		for(int i = 0; i < MOBS_COUNT; i++)
			try
			{
				SimpleSpawner sp = new SimpleSpawner(NpcHolder.getInstance().getTemplate(MOB));
				sp.setLoc(Location.findPointToStay(actor, 100, 120));
				sp.setReflection(actor.getReflection());
				NpcInstance npc = sp.doSpawn(true);
				npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 100);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		_hpCount = 0;
		super.onEvtDead(killer);
	}
}
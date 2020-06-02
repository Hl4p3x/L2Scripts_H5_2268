package ai;

import java.util.ArrayList;
import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.ReflectionUtils;

/**
 * @author PaInKiLlEr
 *         - AI РњРѕРЅСЃС‚СЂР° Archangel (29021).
 *         - Р”РµСЂС‘С‚СЃСЏ СЃ РёРіСЂРѕРєР°РјРё Рё Р‘Р°СЋРјРѕРј.
 *         - Р�СЃРїРѕР»СЊР·СѓРµС‚ СЃРєРёР»С‹ РїРѕ С€Р°РЅСЃР°Рј.
 *         - AI РїСЂРѕРІРµСЂРµРЅ Рё СЂР°Р±РѕС‚Р°РµС‚.
 */
public class Archangel extends Fighter
{
	private long _new_target = System.currentTimeMillis() + 20000;
	private Zone _zone = ReflectionUtils.getZone("[baium_epic]");

	public Archangel(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}

	@Override
	protected void thinkAttack()
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return;

		if(_new_target < System.currentTimeMillis())
		{
			List<Creature> alive = new ArrayList<Creature>();
			for(Creature target : actor.getAroundCharacters(2000, 200))
			{
				if( !target.isDead())
				{
					if(target.getNpcId() == 29020)
					{
						if(Rnd.chance(5))
							alive.add(target);
					}
					else
						alive.add(target);
				}
			}
			if( !alive.isEmpty())
			{
				Creature rndTarget = alive.get(Rnd.get(alive.size()));
				if(rndTarget != null && (rndTarget.getNpcId() == 29020 || rndTarget.isPlayer()))
				{
					setIntention(CtrlIntention.AI_INTENTION_ATTACK, rndTarget);
					actor.getAggroList().addDamageHate(rndTarget, 100, 10);
				}
			}

			_new_target = (System.currentTimeMillis() + 20000);
		}
		super.thinkAttack();
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(actor != null && !actor.isDead())
		{
			if(attacker != null)
			{
				if(attacker.getNpcId() == 29020)
				{
					actor.getAggroList().addDamageHate(attacker, damage, 10);
					setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
				}
			}
		}
		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected boolean maybeMoveToHome(boolean force)
	{
		NpcInstance actor = getActor();
		if(actor != null && !_zone.checkIfInZone(actor))
			returnHome(actor.isRunning());
		return false;
	}

	@Override
	protected boolean returnHome(boolean running)
	{
		return returnHome(true, true, running, false);
	}
}
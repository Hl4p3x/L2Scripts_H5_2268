package ai.dragonvalley;

import org.apache.commons.lang3.ArrayUtils;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.Location;

/**
 * @author pchayka
 */

public class Patrollers extends Fighter
{
	protected Location[] _points;
	private int[] _teleporters = {22857, 22833, 22834, 22835};

	private int _lastPoint = 0;
	private boolean _firstThought = true;

	public Patrollers(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}

	@Override
	public boolean checkAggression(Creature target)
	{
		NpcInstance actor = getActor();
		if(target.isPlayable() && !target.isDead() && !target.isInvisible(actor))
		{
			actor.getAggroList().addDamageHate(target, 0, 1);
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		}
		return true;
	}

	@Override
	protected boolean thinkActive()
	{
		if(super.thinkActive())
			return true;

		if(!getActor().isMoving)
			startMoveTask();

		return true;
	}

	@Override
	protected void onEvtArrived()
	{
		if(getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
			startMoveTask();
		super.onEvtArrived();
	}

	private void startMoveTask()
	{
		NpcInstance npc = getActor();
		if(_firstThought)
		{
			_lastPoint = getIndex(Location.findNearest(npc, _points));
			_firstThought = false;
		}
		else
			_lastPoint++;

		if(_lastPoint >= _points.length)
		{
			_lastPoint = 0;
			if(ArrayUtils.contains(_teleporters, npc.getNpcId()))
			{
				npc.setSpawnedLoc(_points[_lastPoint]);
				npc.teleToLocation(_points[_lastPoint]);
			}
		}

		npc.setRunning();

		if(Rnd.chance(30))
			npc.altOnMagicUseTimer(npc, SkillHolder.getInstance().getSkill(6757, 1));

		Location loc = Location.findPointToStay(_points[_lastPoint], 250, npc.getGeoIndex());
		npc.setSpawnedLoc(loc);
		addTaskMove(loc, true, false);

		if(npc instanceof MonsterInstance)
		{
			MonsterInstance _monster = (MonsterInstance)npc;
			if(_monster.getMinionList() != null && _monster.getMinionList().hasMinions())
			{
				for(NpcInstance _npc : _monster.getMinionList().getAliveMinions())
				{
					_npc.setRunning();
					loc = Location.findPointToStay(_points[_lastPoint], 250, _npc.getGeoIndex());
					_npc.setSpawnedLoc(loc);
					((Fighter) _npc.getAI()).addTaskMove(loc, true, false);
				}
			}
		}
		doTask();
	}

	private int getIndex(Location loc)
	{
		for(int i = 0; i < _points.length; i++)
		{
			if(_points[i] == loc)
				return i;
		}
		return 0;
	}
}

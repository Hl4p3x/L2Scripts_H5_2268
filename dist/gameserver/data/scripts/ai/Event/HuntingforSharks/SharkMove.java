package ai.Event.HuntingforSharks;

import org.apache.commons.lang3.ArrayUtils;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.Location;

public class SharkMove extends Fighter
{
	protected Location[] _points;
	private int[] _teleporters = {36610};

	private int _lastPoint = 0;
	private boolean _firstThought = true;

	public SharkMove(NpcInstance actor)
	{
		super(actor);
		setMaxPursueRange(Integer.MAX_VALUE - 10);
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
		{
			return true;
		}

		if(!getActor().isMoving)
		{
			startMoveTask();
		}

		return true;
	}

	@Override
	protected void onEvtArrived()
	{
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
		{
			_lastPoint++;
		}
		if(_lastPoint >= _points.length)
		{
			_lastPoint = 0;
			if(ArrayUtils.contains(_teleporters, npc.getNpcId()))
			{
				npc.teleToLocation(_points[_lastPoint]);
			}
		}
		npc.setWalking();
		npc.isInWater();
		if(Rnd.chance(100))
		{
			npc.altOnMagicUseTimer(npc, SkillHolder.getInstance().getSkill(6757, 1));
		}
		addTaskMove(Location.findPointToStay(_points[_lastPoint], 250, npc.getGeoIndex()), true, false);
		doTask();
	}

	private int getIndex(Location loc)
	{
		for(int i = 0; i < _points.length; i++)
		{
			if(_points[i] == loc)
			{
				return i;
			}
		}
		return 0;
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}

	@Override
	protected boolean maybeMoveToHome(boolean force)
	{
		return false;
	}

	@Override
	protected boolean teleportHome()
	{
		return false;
	}

	@Override
	protected boolean returnHome(boolean clearAggro, boolean teleport, boolean running, boolean force)
	{
		boolean result = super.returnHome(clearAggro, teleport, running, force);
		clearTasks();
		_firstThought = true;
		startMoveTask();
		return result;
	}
}
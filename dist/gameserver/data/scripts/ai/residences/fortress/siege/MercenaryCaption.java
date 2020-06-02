package ai.residences.fortress.siege;

import java.util.Collections;
import java.util.List;

import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.entity.events.impl.FortressSiegeEvent;
import l2s.gameserver.model.entity.residence.Fortress;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.Location;
import npc.model.residences.fortress.siege.MercenaryCaptionInstance;

/**
 * @author VISTALL
 * @date 10:58/19.04.2011
 */
public class MercenaryCaption extends Fighter
{
	private List<Location> _points = Collections.emptyList();
	private int _tick = -1;

	public MercenaryCaption(NpcInstance actor)
	{
		super(actor);
		setMaxPursueRange(100);
	}

	@Override
	public void onEvtSpawn()
	{
		super.onEvtSpawn();
		NpcInstance actor = getActor();

		Fortress f = actor.getFortress();
		FortressSiegeEvent event = f.getSiegeEvent();

		_points = event.getObjects(FortressSiegeEvent.MERCENARY_POINTS);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor.isActionsDisabled())
			return true;

		if(_def_think)
		{
			if(doTask())
				clearTasks();
			return true;
		}

		if(randomWalk())
			return true;

		return false;
	}

	@Override
	public void onEvtArrived()
	{
		if(_tick != -1)
			startMove(false);
	}

	@Override
	public void onEvtAttacked(Creature attacker, int damage)
	{
		_tick = -1;
		super.onEvtAttacked(attacker, damage);
	}

	public void startMove(boolean init)
	{
		if(init)
			_tick = 0;

		if(_tick == -1)
			return;

		if(_tick < _points.size())
		{
			addTaskMove(_points.get(_tick ++), true, false);
			doTask();
		}
	}

	@Override
	public MercenaryCaptionInstance getActor()
	{
		return (MercenaryCaptionInstance)super.getActor();
	}
}

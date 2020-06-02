package ai.monastery_of_silence;

import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @author PaInKiLlEr
 * @reworked by Bonux
 */
public class EventFurnance extends DefaultAI
{
	private final static int OFF_TIMER = 5567;
	private final static int OFF_TIME = 2;

	private final int _potNumber;

	public EventFurnance(NpcInstance actor)
	{
		super(actor);

		actor.setIsInvul(true);
		actor.setTargetable(false);

		_potNumber = actor.getParameter("pot_number", 0);
	}

	@Override
	protected void onEvtSeeSpell(Skill skill, Creature caster)
	{
		NpcInstance actor = getActor();
		if(skill.getId() == 9059)
		{
			actor.setNpcState(1);
			actor.setTargetable(false);
			actor.doCast(SkillHolder.getInstance().getSkill(5144, 1), caster, true);
			broadCastScriptEvent("2114005", _potNumber, 1000);
			addTimer(OFF_TIMER, OFF_TIME * 1000L);
			actor.setTargetable(true);
		}
	}

	@Override
	protected void onEvtTimer(int timerId, Object arg1, Object arg2)
	{
		if(timerId == OFF_TIMER)
		{
			NpcInstance actor = getActor();
			actor.setNpcState(2);
		}
		super.onEvtTimer(timerId, arg1, arg2);
	}

	@Override
	protected void onEvtScriptEvent(String event, Object arg1, Object arg2)
	{
		NpcInstance actor = getActor();
		if(event.equalsIgnoreCase("2114001"))
		{
			actor.setTargetable(false);

			int potNumber = ((Integer) arg1).intValue();
			if(potNumber == _potNumber)
			{
				actor.setNpcState(1);
				addTimer(OFF_TIMER, OFF_TIME * 1000L);
			}
		}
		else if(event.equalsIgnoreCase("2114002"))
		{
			actor.setTargetable(false);
			actor.setNpcState(1);
			addTimer(OFF_TIMER, OFF_TIME * 1000L);
		}
		else if(event.equalsIgnoreCase("2114003"))
		{
			actor.setNpcState(1);
			addTimer(OFF_TIMER, OFF_TIME * 1000L);
			actor.setTargetable(false);
		}
		else if(event.equalsIgnoreCase("2114004"))
		{
			actor.setTargetable(false);
			actor.setNpcState(1);
			addTimer(OFF_TIMER, OFF_TIME * 1000L);
		}
		else if(event.equalsIgnoreCase("21140015"))
		{
			actor.setNpcState(1);
			addTimer(OFF_TIMER, OFF_TIME * 1000L);
			actor.setTargetable(true);
		}
		super.onEvtScriptEvent(event, arg1, arg2);
	}
}
package l2s.gameserver.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.CArrayIntSet;

import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.PlayableAI.nextAction;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.DiePacket;
import l2s.gameserver.utils.Location;

public class CharacterAI extends AbstractAI
{
	private final IntSet _blockedTimers = new CArrayIntSet();
	private final List<ScheduledFuture<?>> _timers = new ArrayList<ScheduledFuture<?>>();
	private final IntObjectMap<ScheduledFuture<?>> _tasks = new CHashIntObjectMap<ScheduledFuture<?>>();

	public CharacterAI(Creature actor)
	{
		super(actor);
	}

	@Override
	protected void onIntentionIdle()
	{
		clientStopMoving();
		changeIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
	}

	@Override
	protected void onIntentionActive()
	{
		clientStopMoving();
		changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
		onEvtThink();
	}

	@Override
	protected void onIntentionAttack(Creature target)
	{
		setAttackTarget(target);
		clientStopMoving();
		changeIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
		onEvtThink();
	}

	@Override
	protected void onIntentionCast(Skill skill, Creature target)
	{
		setCastTarget(target);
		changeIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
		onEvtThink();
	}

	@Override
	protected void onIntentionFollow(Creature target, Integer offset)
	{
		changeIntention(CtrlIntention.AI_INTENTION_FOLLOW, target, offset);
		onEvtThink();
	}

	@Override
	protected void onIntentionInteract(GameObject object)
	{}

	@Override
	protected void onIntentionPickUp(GameObject item)
	{}

	@Override
	protected void onIntentionRest()
	{}

	@Override
	protected void onIntentionCoupleAction(Player player, Integer socialId)
	{}

	@Override
	protected void onIntentionReturnHome(boolean running)
	{}

	@Override
	protected void onIntentionWalkerRoute()
	{}

	@Override
	protected void onEvtArrivedBlocked(Location blocked_at_pos)
	{
		// If the Intention was AI_INTENTION_MOVE_TO, set the Intention to AI_INTENTION_ACTIVE
		if(/*(getIntention() == CtrlIntention.AI_INTENTION_MOVE_TO) || */(getIntention() == CtrlIntention.AI_INTENTION_CAST))
		{
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}

		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving();

		// Launch actions corresponding to the Event Think
		onEvtThink();
	}

	@Override
	protected void onEvtForgetObject(GameObject object)
	{
		if(object == null)
			return;

		Creature actor = getActor();

		if(actor.isAttackingNow() && getAttackTarget() == object)
			actor.abortAttack(true, true);

		if(actor.isCastingNow() && getCastTarget() == object)
			actor.abortCast(true, false);

		if(getAttackTarget() == object)
			setAttackTarget(null);

		if(getCastTarget() == object)
			setCastTarget(null);

		if(actor.getTargetId() == object.getObjectId())
			actor.setTarget(null);

		if(actor.getFollowTarget() == object)
			actor.setFollowTarget(null);

		if(actor.getServitor() != null)
			actor.getServitor().getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		Creature actor = getActor();

		actor.abortAttack(true, true);
		actor.abortCast(true, true);
		actor.stopMove();
		actor.broadcastPacket(new DiePacket(actor));

		setIntention(CtrlIntention.AI_INTENTION_IDLE);
	}

	@Override
	protected void onEvtFakeDeath()
	{
		clientStopMoving();
		setIntention(CtrlIntention.AI_INTENTION_IDLE);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{

	}

	@Override
	protected void onEvtClanAttacked(Creature attacked_member, Creature attacker, int damage)
	{}

	public void Attack(GameObject target, boolean forceUse, boolean dontMove)
	{
		setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
	}

	public void Cast(Skill skill, Creature target)
	{
		Cast(skill, target, false, false);
	}

	public void Cast(Skill skill, Creature target, boolean forceUse, boolean dontMove)
	{
		setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
	}

	@Override
	protected void onEvtThink()
	{}

	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{}

	@Override
	protected void onEvtFinishCasting(Skill skill, Creature target, boolean success)
	{}

	@Override
	protected void onEvtReadyToAct()
	{}

	@Override
	protected void onEvtArrived()
	{}

	@Override
	protected void onEvtArrivedTarget()
	{}

	@Override
	protected void onEvtTeleported()
	{
		//
	}

	@Override
	protected void onEvtSeeSpell(Skill skill, Creature caster)
	{}

	@Override
	protected void onEvtSpawn()
	{}

	@Override
	public void onEvtDeSpawn()
	{}

	public void stopAITask()
	{}

	public void startAITask()
	{}

	public void setNextAction(nextAction action, Object arg0, Object arg1, boolean arg2, boolean arg3)
	{}

	public void clearNextAction()
	{}

	public boolean isActive()
	{
		return true;
	}

	@Override
	protected void onEvtTimer(int timerId, Object arg1, Object arg2)
	{
		stopTask(timerId);

		Creature actor = getActor();
		if(actor == null)
			return;

		actor.onEvtTimer(timerId, arg1, arg2);
	}

	@Override
	protected void onEvtScriptEvent(String event, Object arg1, Object arg2)
	{
		Creature actor = getActor();
		if(actor == null)
			return;

		actor.onEvtScriptEvent(event, arg1, arg2);
	}

	@Override
	protected void onEvtSeeCreatue(Creature creature)
	{
		//
	}

	@Override
	protected void onEvtDisappearCreatue(Creature creature)
	{
		//
	}

	@Override
	protected void onEvtDelete()
	{
		//
	}

	public void addTimer(int timerId, long delay)
	{
		addTimer(timerId, null, null, delay);
	}

	public void addTimer(int timerId, Object arg1, long delay)
	{
		addTimer(timerId, arg1, null, delay);
	}

	public void addTimer(int timerId, Object arg1, Object arg2, long delay)
	{
		ScheduledFuture<?> timer = ThreadPoolManager.getInstance().schedule(new Timer(timerId, arg1, arg2), delay);
		if(timer != null)
			_timers.add(timer);
	}

	public void addTask(int timerId, long delay)
	{
		addTask(timerId, null, null, delay);
	}

	public void addTask(int timerId, Object arg1, long delay)
	{
		addTask(timerId, arg1, null, delay);
	}

	public void addTask(int timerId, Object arg1, Object arg2, long delay)
	{
		stopTask(timerId);

		ScheduledFuture<?> task = ThreadPoolManager.getInstance().schedule(new Timer(timerId, arg1, arg2), delay);
		if(task != null)
			_tasks.put(timerId, task);
	}

	public boolean haveTask(int timerId)
	{
		ScheduledFuture<?> task = _tasks.get(timerId);
		return task != null && !task.isCancelled() && !task.isDone();
	}

	public void stopTask(int timerId)
	{
		ScheduledFuture<?> task = _tasks.remove(timerId);
		if(task != null)
		{
			task.cancel(false);
			task = null;
		}
	}

	public void stopAllTaskAndTimers()
	{
		for(ScheduledFuture<?> timer : _timers)
			timer.cancel(false);

		for(ScheduledFuture<?> task : _tasks.valueCollection())
			task.cancel(false);

		_blockedTimers.clear();
		_timers.clear();
		_tasks.clear();
	}

	public void blockTimer(int timerId)
	{
		_blockedTimers.add(timerId);
	}

	public void unblockTimer(int timerId)
	{
		_blockedTimers.remove(timerId);
	}

	protected class Timer extends RunnableImpl
	{
		private int _timerId;
		private Object _arg1;
		private Object _arg2;

		public Timer(int timerId, Object arg1, Object arg2)
		{
			_timerId = timerId;
			_arg1 = arg1;
			_arg2 = arg2;
		}

		public void runImpl()
		{
			if(_blockedTimers.contains(_timerId))
				return;

			notifyEvent(CtrlEvent.EVT_TIMER, _timerId, _arg1, _arg2);
		}
	}

	public void broadCastScriptEvent(String event, int radius)
	{
		broadCastScriptEvent(event, null, null, radius);
	}

	public void broadCastScriptEvent(String event, Object arg1, int radius)
	{
		broadCastScriptEvent(event, arg1, null, radius);
	}

	public void broadCastScriptEvent(String event, Object arg1, Object arg2, int radius)
	{
		List<NpcInstance> npcs = World.getAroundNpc(getActor(), radius, radius);
		for(NpcInstance npc : npcs)
		{
			npc.getAI().notifyEvent(CtrlEvent.EVT_SCRIPT_EVENT, event, arg1, arg2);
		}
	}

	public int getMaxHateRange()
	{
		return 0;
	}
}
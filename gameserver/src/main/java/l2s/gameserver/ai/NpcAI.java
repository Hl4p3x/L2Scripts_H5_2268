package l2s.gameserver.ai;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.WorldRegion;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.SocialActionPacket;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.taskmanager.AiTaskManager;
import l2s.gameserver.templates.npc.RandomActions;
import l2s.gameserver.templates.npc.WalkerRoute;
import l2s.gameserver.templates.npc.WalkerRoutePoint;
import l2s.gameserver.utils.Location;

/**
 * @author Bonux
 */
public class NpcAI extends CharacterAI
{
	public static final String WALKER_ROUTE_PARAM = "walker_route_id";

	private static final int WALKER_ROUTE_TIMER_ID = -1000;
	private static final int RANDOM_ACTION_TIMER_ID = -2000;

	protected ScheduledFuture<?> _aiTask;

	protected long _attackAITaskDelay = Config.AI_TASK_ATTACK_DELAY;
	protected long _activeAITaskDelay = Config.AI_TASK_ACTIVE_DELAY;
	protected long _currentAITaskDelay = _activeAITaskDelay;

	/** The flag used to indicate that a thinking action is in progress */
	protected Lock _thinking = new ReentrantLock();

	//random actions params
	private final RandomActions _randomActions;
	private final boolean _haveRandomActions;
	private int _currentActionId;

	//Walker Routes params
	private WalkerRoute _walkerRoute;
	private boolean _haveWalkerRoute;
	private boolean _toBackWay;
	private int _currentWalkerPoint;
	private boolean _delete;

	private final List<Creature> _neighbors = new ArrayList<Creature>();
	private long _lastNeighborsClean = 0;

	protected boolean _isGlobal;
	protected long _lastActiveCheck;

	private int _walkTryCount = 0;

	private long _lookNeighborTimestamp = 0;
	private final Lock _lockLookNeighbor = new ReentrantLock();
	private boolean _isActive;
	
	public NpcAI(NpcInstance actor)
	{
		super(actor);

		//initialize random actions params
		_randomActions = actor.getTemplate().getRandomActions();
		_haveRandomActions = _randomActions != null && _randomActions.getActionsCount() > 0;
		_currentActionId = 0; //При спавне начинаем действия с 1го действия.

		//initialize Walker Routes params
		setWalkerRoute(actor.getParameter(WALKER_ROUTE_PARAM, -1));

		_isGlobal = actor.getParameter("GlobalAI", false);
		
		_isActive = false;
	}

	public void setWalkerRoute(WalkerRoute walkerRoute)
	{
		_walkerRoute = walkerRoute;
		_haveWalkerRoute = _walkerRoute != null && _walkerRoute.isValid();
		_toBackWay = false;
		_currentWalkerPoint = -1;
		_delete = false;

		if(isActive())
			setIntention(CtrlIntention.AI_INTENTION_WALKER_ROUTE);
	}

	public void setWalkerRoute(int id)
	{
		setWalkerRoute(getActor().getTemplate().getWalkerRoute(id));
	}

	@Override
	protected void onEvtArrived()
	{
		if (!_isActive)
		{
			return;
		}
		continueWalkerRoute();
	}

	@Override
	protected void onEvtTeleported()
	{
		continueWalkerRoute();
	}

	@Override
	protected void onEvtSeeCreatue(Creature creature)
	{
		getActor().onSeeCreatue(creature);
	}

	@Override
	protected void onEvtDisappearCreatue(Creature creature)
	{
		getActor().onDisappearCreatue(creature);
	}

	@Override
	protected void onIntentionIdle()
	{
		_lockLookNeighbor.lock();
		try
		{
			_neighbors.clear();
		}
		finally
		{
			_lockLookNeighbor.unlock();
		}
		changeIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
	}

	@Override
	protected void onIntentionWalkerRoute()
	{
		if(_haveWalkerRoute)
		{
			clientStopMoving();
			moveToNextPoint(0);
			changeIntention(CtrlIntention.AI_INTENTION_WALKER_ROUTE, null, null);
		}
	}

	@Override
	protected void onEvtTimer(int timerId, Object arg1, Object arg2)
	{
		NpcInstance actor = getActor();
		if(timerId == WALKER_ROUTE_TIMER_ID)
		{
			if(_haveWalkerRoute)
				moveToNextPoint(0);
		}
		else if(timerId == RANDOM_ACTION_TIMER_ID)
		{
			if(_haveRandomActions)
				makeRandomAction();
		}
		actor.onTimerFired(timerId);
	}
	
	@Override
	protected void onEvtThink()
	{
		NpcInstance actor = getActor();
		if(actor == null || actor.isActionsDisabled())
			return;

		if(!_thinking.tryLock())
			return;

		try
		{
			lookNeighbor(actor.getAggroRange(), false);

			if(getIntention() == CtrlIntention.AI_INTENTION_ACTIVE || getIntention() == CtrlIntention.AI_INTENTION_WALKER_ROUTE)
				thinkActive();
		}
		finally
		{
			_thinking.unlock();
		}
	}

	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return false;

		if(_haveWalkerRoute)
		{
			if(getIntention() == CtrlIntention.AI_INTENTION_WALKER_ROUTE)
			{
				if(!actor.isMoving && !haveTask(WALKER_ROUTE_TIMER_ID))
				{
					// Если по какой-то причине моб сбился и не идет, то с 10й попытки принуждаем его идти дальше.
					_walkTryCount++;
					if(_walkTryCount >= 10)
					{
						moveToNextPoint(0);
						return true;
					}
				}
			}
			else
			{
				changeIntention(CtrlIntention.AI_INTENTION_WALKER_ROUTE, null, null);
				moveToLocation(actor.getSpawnedLoc());
				return true;
			}
		}
		return false;
	}

	public synchronized void startAITask()
	{
		if(_aiTask == null)
		{
			_currentAITaskDelay = _activeAITaskDelay;
			_aiTask = AiTaskManager.getInstance().scheduleAtFixedRate(this, 0L, _currentAITaskDelay);
		}

		if(_haveWalkerRoute)
			setIntention(CtrlIntention.AI_INTENTION_WALKER_ROUTE);

		if(_haveRandomActions)
		{
			RandomActions.Action action = _randomActions.getAction(1);
			if(action != null)
			{
				//При спауне начинаем делать действия через случайное время, иначе все нпс будут одновременно начинать, что будет не очень красиво.
				addTask(RANDOM_ACTION_TIMER_ID, Rnd.get(0, action.getDelay()) * 1000L);
			}
		}
	}

	protected synchronized void switchAITask(long delay)
	{
		if(_aiTask != null)
		{
			if(_currentAITaskDelay == delay)
				return;
			_aiTask.cancel(false);
		}

		_currentAITaskDelay = delay;
		_aiTask = AiTaskManager.getInstance().scheduleAtFixedRate(this, 0L, _currentAITaskDelay);
	}

	public final synchronized void stopAITask()
	{
		if(_aiTask != null)
		{
			_aiTask.cancel(false);
			_aiTask = null;
		}
	}

	@Override
	public boolean isGlobalAI()
	{
		return _isGlobal;
	}

	@Override
	public boolean isActive()
	{
		return _aiTask != null;
	}

	@Override
	public void runImpl() throws Exception
	{
		if(!isActive())
			return;

		// проверяем, если NPC вышел в неактивный регион, отключаем AI
		if(!isGlobalAI() && System.currentTimeMillis() - _lastActiveCheck > 60000L)
		{
			_lastActiveCheck = System.currentTimeMillis();
			NpcInstance actor = getActor();
			WorldRegion region = actor == null ? null : actor.getCurrentRegion();
			if(region == null || !region.isActive())
			{
				stopAITask();
				return;
			}
		}
		onEvtThink();
	}

	private void continueWalkerRoute()
	{
		if(!isActive() || getIntention() != CtrlIntention.AI_INTENTION_WALKER_ROUTE)
			return;

		//Когда дошли, говорим фразу, делаем социальное действие, и через указаный промежуток времени начием идти дальше
		if(_haveWalkerRoute)
		{
			if(_currentWalkerPoint >= 0)
			{
				WalkerRoutePoint route = _walkerRoute.getPoint(_currentWalkerPoint);
				if(route == null)
				{
					moveToNextPoint(0);
					return; //todo
				}

				NpcInstance actor = getActor();

				int socialActionId = route.getSocialActionId();
				if(socialActionId >= 0)
					actor.broadcastPacket(new SocialActionPacket(actor.getObjectId(), socialActionId));

				NpcString phrase = Rnd.get(route.getPhrases());
				if(phrase != null)
					Functions.npcSay(actor, phrase);

				moveToNextPoint(route.getDelay());
			}
			else
				moveToNextPoint(0);
		}
	}

	private void moveToNextPoint(int delay)
	{
		if(!isActive() || getIntention() != CtrlIntention.AI_INTENTION_WALKER_ROUTE)
			return;

		if(!_haveWalkerRoute)
			return;

		if(delay > 0)
		{
			addTask(WALKER_ROUTE_TIMER_ID, delay * 1000L);
			return;
		}

		_walkTryCount = 0;

		NpcInstance actor = getActor();
		if(actor == null)
			return;

		switch(_walkerRoute.getType())
		{
			case LENGTH:
			{
				if(_toBackWay)
					_currentWalkerPoint--;
				else
					_currentWalkerPoint++;

				if(_currentWalkerPoint >= _walkerRoute.size() - 1)
					_toBackWay = true;

				if(_currentWalkerPoint == 0)
					_toBackWay = false;
				break;
			}
			case ROUND:
			{
				_currentWalkerPoint++;

				if(_currentWalkerPoint >= _walkerRoute.size())
					_currentWalkerPoint = 0;
				break;
			}
			case RANDOM:
			{
				if(_walkerRoute.size() > 1)
				{
					int oldPoint = _currentWalkerPoint;
					while(oldPoint == _currentWalkerPoint)
						_currentWalkerPoint = Rnd.get(_walkerRoute.size() - 1);
				}
				break;
			}
			case DELETE:
			{
				if(_delete)
				{
					actor.deleteMe(); // TODO: [Bonux] Мб сделать, чтобы он респаунился? Если респаун указан в спавне.
					return;
				}
				_currentWalkerPoint++;
				if(_currentWalkerPoint >= _walkerRoute.size())
					_delete = true;
				break;
			}
			case FINISH:
			{
				_currentWalkerPoint++;
				if(_currentWalkerPoint >= _walkerRoute.size())
				{
					actor.stopMove();
					int routeId = _walkerRoute.getId();
					setWalkerRoute(null);
					notifyEvent(CtrlEvent.EVT_FINISH_WALKER_ROUTE, routeId);
					return;
				}
				break;
			}
		}

		WalkerRoutePoint route = _walkerRoute.getPoint(_currentWalkerPoint);
		if(route == null)
			return; //todo

		if(route.isRunning())
			actor.setRunning();
		else
			actor.setWalking();

		if(route.isTeleport())
		{
			actor.teleToLocation(route.getLocation());
			continueWalkerRoute();
		}
		else
			moveToLocation(route.getLocation());
	}

	private void makeRandomAction()
	{
		if(!isActive())
			return;

		if(!_haveRandomActions)
			return;

		NpcInstance actor = getActor();
		if(actor == null)
			return;

		if(getIntention() == CtrlIntention.AI_INTENTION_ACTIVE || getIntention() == CtrlIntention.AI_INTENTION_WALKER_ROUTE)
		{
			_currentActionId++;
			if(_currentActionId > _randomActions.getActionsCount())
				_currentActionId = 1;

			RandomActions.Action action = _randomActions.getAction(_currentActionId);
			if(action == null)
				return; //todo

			int socialActionId = action.getSocialActionId();
			if(socialActionId >= 0)
				actor.broadcastPacket(new SocialActionPacket(actor.getObjectId(), socialActionId));

			NpcString phrase = action.getPhrase();
			if(phrase != null)
				Functions.npcSay(actor, phrase);

			addTask(RANDOM_ACTION_TIMER_ID, action.getDelay() * 1000L);
		}
		else
			addTask(RANDOM_ACTION_TIMER_ID, 1000L);
	}

	private void moveToLocation(Location loc)
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return;

		if(getIntention() == CtrlIntention.AI_INTENTION_WALKER_ROUTE)
		{
			loc = Location.findPointToStay(loc, 50, actor.getGeoIndex());
			loc.h = -1;
			actor.setSpawnedLoc(loc);
			if(!actor.moveToLocation(loc, 0, true))
			{
				clientStopMoving();
				actor.teleToLocation(loc);
			}
		}
	}

	@Override
	public NpcInstance getActor()
	{
		return (NpcInstance) super.getActor();
	}

	protected boolean isHaveRandomActions()
	{
		return _haveRandomActions;
	}

	protected boolean isHaveWalkerRoute()
	{
		return _haveWalkerRoute;
	}

	protected boolean lookNeighbor(int range, boolean force)
	{
		if(!isActive())
			return false;

		if(range <= 0)
			return false;

		NpcInstance actor = getActor();
		if(actor == null)
			return false;

		_lockLookNeighbor.lock();
		try
		{
			long now = System.currentTimeMillis();
			if((now - _lookNeighborTimestamp) > 500L)
			{
				_lookNeighborTimestamp = now;

				/* TODO: Должна ли быть очистка каждые 30 сек?
				if(force || _lastNeighborsClean + 30000 < System.currentTimeMillis()) 
				{
					_lastNeighborsClean = System.currentTimeMillis();
					_neighbors.clear();
				}*/

				for(Creature creature : actor.getAroundCharacters(range, 250))
				{
					if(!_neighbors.contains(creature) && !creature.isInvisible(actor))
					{
						notifyEvent(CtrlEvent.EVT_SEE_CREATURE, creature);
						_neighbors.add(creature);
					}
				}

				for(Iterator<Creature> itr = _neighbors.iterator(); itr.hasNext();)
				{
					Creature creature = itr.next();
					if(!actor.isInRangeZ(creature, range) || creature.isInvisible(actor))
					{
						itr.remove();
						notifyEvent(CtrlEvent.EVT_DISAPPEAR_CREATURE, creature);
					}
				}
				return true;
			}
		}
		finally
		{
			_lockLookNeighbor.unlock();
		}
		return false;
	}

	protected void removeNeighbor(Creature creature)
	{
		_lockLookNeighbor.lock();
		try
		{
			if(_neighbors.remove(creature))
				notifyEvent(CtrlEvent.EVT_DISAPPEAR_CREATURE, creature);
		}
		finally
		{
			_lockLookNeighbor.unlock();
		}
	}

	@Override
	protected void onEvtForgetObject(GameObject object)
	{
		super.onEvtForgetObject(object);

		if(object.isCreature())
			removeNeighbor((Creature) object);
	}

	protected boolean hasRandomWalk()
	{
		return !_haveWalkerRoute && getActor().hasRandomWalk();
	}

	public boolean returnHomeAndRestore(boolean running)
	{
		return false;
	}

	public void addTaskCast(Creature target, Skill skill)
	{
		//
	}

	public void addTaskBuff(Creature target, Skill skill)
	{
		//
	}

	public void addTaskAttack(Creature target)
	{
		addTaskAttack(target, null, DefaultAI.TaskDefaultWeight);
	}

	public void addTaskAttack(Creature target, Skill skill, int weight)
	{
		//
	}

	public void addTaskMove(Location loc, boolean pathfind, boolean teleportIfCantMove)
	{
		//
	}

	public void addTaskMove(int locX, int locY, int locZ, boolean pathfind, boolean teleportIfCantMove)
	{
		addTaskMove(new Location(locX, locY, locZ), pathfind, teleportIfCantMove);
	}

	public void addUseSkillDesire(Creature target, Skill skill, int p1, int p2, long desire)
	{
		//
	}

	public void addAttackDesire(Creature target, int p1, long desire)
	{
		//
	}

	public void addMoveAroundDesire(int p1, long desire)
	{
		//
	}
}
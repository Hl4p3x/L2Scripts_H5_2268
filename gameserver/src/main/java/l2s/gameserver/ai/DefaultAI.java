package l2s.gameserver.ai;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.collections.LazyArrayList;
import l2s.commons.lang.reference.HardReference;
import l2s.commons.math.random.RndSelector;
import l2s.commons.threading.RunnableImpl;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.AggroList.AggroInfo;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.MinionList;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.World;
import l2s.gameserver.model.entity.SevenSigns;
import l2s.gameserver.model.instances.MinionInstance;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestEventType;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.StatusUpdatePacket;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.NpcUtils;

public class DefaultAI extends NpcAI
{
	protected static final Logger _log = LoggerFactory.getLogger(DefaultAI.class);
	
	public static String namechar;


	public static enum TaskType
	{
		MOVE,
		ATTACK,
		CAST,
		BUFF
	}

	public static final int TaskDefaultWeight = 10000;


	public static class Task
	{
		public TaskType type;
		public Skill skill;
		public HardReference<? extends Creature> target;
		public Location loc;
		public boolean pathfind;
		public boolean teleportIfCantMove;
		public int weight = TaskDefaultWeight;
	}

	@Override
	public void addTaskCast(Creature target, Skill skill)
	{
		Task task = new Task();
		task.type = TaskType.CAST;
		task.target = target.getRef();
		task.skill = skill;
		_tasks.add(task);
		_def_think = true;
	}

	@Override
	public void addTaskBuff(Creature target, Skill skill)
	{
		Task task = new Task();
		task.type = TaskType.BUFF;
		task.target = target.getRef();
		task.skill = skill;
		_tasks.add(task);
		_def_think = true;
	}

	@Override
	public void addTaskAttack(Creature target)
	{
		Task task = new Task();
		task.type = TaskType.ATTACK;
		task.target = target.getRef();
		_tasks.add(task);
		_def_think = true;
	}

	@Override
	public void addTaskAttack(Creature target, Skill skill, int weight)
	{
		Task task = new Task();
		task.type = skill.isOffensive() ? TaskType.CAST : TaskType.BUFF;
		task.target = target.getRef();
		task.skill = skill;
		task.weight = weight;
		_tasks.add(task);
		_def_think = true;
	}

	@Override
	public void addTaskMove(Location loc, boolean pathfind, boolean teleportIfCantMove)
	{
		Task task = new Task();
		task.type = TaskType.MOVE;
		task.loc = loc;
		task.pathfind = pathfind;
		task.teleportIfCantMove = teleportIfCantMove;
		_tasks.add(task);
		_def_think = true;
	}

	@Override
	public void addTaskMove(int locX, int locY, int locZ, boolean pathfind, boolean teleportIfCantMove)
	{
		addTaskMove(new Location(locX, locY, locZ), pathfind, teleportIfCantMove);
	}

	private static class TaskComparator implements Comparator<Task>
	{
		private static final Comparator<Task> instance = new TaskComparator();

		public static final Comparator<Task> getInstance()
		{
			return instance;
		}

		@Override
		public int compare(Task o1, Task o2)
		{
			if(o1 == null || o2 == null)
				return 0;
			return o2.weight - o1.weight;
		}
	}

	protected class Teleport extends RunnableImpl
	{
		Location _destination;

		public Teleport(Location destination)
		{
			_destination = destination;
		}

		@Override
		public void runImpl() throws Exception
		{
			clientStopMoving();
			_pathfindFails = 0;

			NpcInstance actor = getActor();
			if(actor != null)
				actor.teleToLocation(_destination);
		}
	}

	protected class RunningTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			NpcInstance actor = getActor();
			if(actor != null)
				actor.setRunning();
			_runningTask = null;
		}
	}

	protected class MadnessTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			NpcInstance actor = getActor();
			if(actor != null)
				actor.stopConfused();
			_madnessTask = null;
		}
	}

	public static class NearestTargetComparator implements Comparator<Creature>
	{
		private final HardReference<? extends Creature> _creatureRef;

		public NearestTargetComparator(Creature creature)
		{
			_creatureRef = creature.getRef();
		}

		@Override
		public int compare(Creature o1, Creature o2)
		{
			if(o1 == null || o2 == null)
				return 0;

			if(o1 == o2)
				return 0;

			if(o1.getObjectId() == o2.getObjectId())
				return 0;

			Creature creature = _creatureRef.get();
			if(creature == null)
				return 0;

			return Integer.compare(creature.getDistance3D(o1), creature.getDistance3D(o2));
		}
	}

	protected long AI_TASK_ATTACK_DELAY = Config.AI_TASK_ATTACK_DELAY;
	protected long AI_TASK_ACTIVE_DELAY = Config.AI_TASK_ACTIVE_DELAY;

	private final int MAX_HATE_RANGE = 2000;

	private int _maxPursueRange;

	protected ScheduledFuture<?> _runningTask;
	protected ScheduledFuture<?> _madnessTask;

	/** The flag used to indicate that a thinking action is in progress */
	private boolean _thinking = false;
	/** Показывает, есть ли задания */
	protected boolean _def_think = false;

	/** The L2NpcInstance aggro counter */
	protected long _globalAggro;

	protected long _randomAnimationEnd;
	protected int _pathfindFails;

	/** Список заданий */
	protected final NavigableSet<Task> _tasks = new ConcurrentSkipListSet<Task>(TaskComparator.getInstance());

	protected final Skill[] _damSkills, _dotSkills, _debuffSkills, _healSkills, _buffSkills, _stunSkills;

	protected long _lastActiveCheck;
	protected long _checkAggroTimestamp = 0;

	protected long _lastFactionNotifyTime = 0;
	protected long _minFactionNotifyInterval = 10000;

	protected final Comparator<Creature> _nearestTargetComparator;

	protected Object _intention_arg0 = null, _intention_arg1 = null;

	private static final int MAX_PATHFIND_FAILS = 3;
	private static final int TELEPORT_TIMEOUT = 10000;
	private static final int MAX_ATTACK_TIMEOUT = 15000;

	private final Skill UD_SKILL = SkillHolder.getInstance().getSkill(5044,3);
	private static final int UD_NONE = 0;
	private static final int UD_CAST = 1;
	private static final int UD_CASTED = 2;
	private static final int UD_DISTANCE = 150;
	private volatile int _UDFlag = UD_NONE;
	private int _UDRate;

	private boolean _canRestoreOnReturnHome;

	public DefaultAI(NpcInstance actor)
	{
		super(actor);

		NpcInstance npc = getActor();
		_damSkills = npc.getTemplate().getDamageSkills();
		_dotSkills = npc.getTemplate().getDotSkills();
		_debuffSkills = npc.getTemplate().getDebuffSkills();
		_buffSkills = npc.getTemplate().getBuffSkills();
		_stunSkills = npc.getTemplate().getStunSkills();
		_healSkills = npc.getTemplate().getHealSkills();

		_nearestTargetComparator = new NearestTargetComparator(actor);

		// Preload some AI params
		_maxPursueRange = Math.max(actor.getAggroRange(), actor.getParameter("max_pursue_range", actor.isRaid() ? Config.MAX_PURSUE_RANGE_RAID : actor.isUnderground() ? Config.MAX_PURSUE_UNDERGROUND_RANGE : Config.MAX_PURSUE_RANGE));
		_minFactionNotifyInterval = actor.getParameter("FactionNotifyInterval", 10000);
		_UDRate = actor.getParameter("LongRangeGuardRate", 0);
		_canRestoreOnReturnHome = actor.getParameter("restore_on_return_home", false);
	}

	@Override
	public void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		super.changeIntention(intention, arg0, arg1);
		_intention_arg0 = arg0;
		_intention_arg1 = arg1;
	}

	@Override
	public void setIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		_intention_arg0 = null;
		_intention_arg1 = null;
		super.setIntention(intention, arg0, arg1);
	}

	/**
	 * Определяет, может ли этот тип АИ видеть персонажей в режиме Silent Move.
	 * @param target L2Playable цель
	 * @return true если цель видна в режиме Silent Move
	 */
	protected boolean canSeeInSilentMove(Playable target)
	{
		if(getActor().getParameter("canSeeInSilentMove", false))
			return true;
		return !target.isSilentMoving();
	}

	protected boolean canSeeInHide(Playable target)
	{
		NpcInstance actor = getActor();
		if(actor.getParameter("canSeeInHide", false))
			return true;

		return !target.isInvisible(actor);
	}

	protected boolean checkAggression(Creature target)
	{
		NpcInstance actor = getActor();
		if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE && getIntention() != CtrlIntention.AI_INTENTION_WALKER_ROUTE || !isGlobalAggro())
			return false;
		if(target.isAlikeDead())
			return false;
		if(target.isNpc() && target.isInvul())
			return false;
		if(target.isPlayer() && target.getPlayer().isInAwayingMode() && !Config.AWAY_PLAYER_TAKE_AGGRO)
			return false;

		if(target.isPlayable())
		{
			if(!canSeeInSilentMove((Playable) target))
				return false;
			if(!canSeeInHide((Playable) target))
				return false;
			if(actor.getFaction().containsName("varka_silenos_clan") && target.getPlayer().getVarka() > 0)
				return false;
			if(actor.getFaction().containsName("ketra_orc_clan") && target.getPlayer().getKetra() > 0)
				return false;
			/*if(target.isFollow && !target.isPlayer() && target.getFollowTarget() != null && target.getFollowTarget().isPlayer())
					return;*/
			if(target.isPlayer() && target.getPlayer().isGMInvisible())
				return false;
			if(((Playable) target).isInNonAggroTime())
				return false;
			if(target.isPlayer() && !target.getPlayer().isActive())
				return false;
			//if(actor.isMonster() && target.isInZonePeace())
				//return false;
			if(actor.isMonster())
				if(target.isPlayer() && (target.getPlayer().isInStoreMode() || target.getPlayer().isInOfflineMode()))
					return false;
		}

		AggroInfo ai = actor.getAggroList().get(target);
		if(ai != null && ai.hate > 0)
		{
			if(!target.isInRangeZ(actor.getSpawnedLoc(), getMaxHateRange()))
				return false;
		}
		else if(!actor.isAggressive() || !target.isInRangeZ(actor.getSpawnedLoc(), actor.getAggroRange()))
			return false;

		if(!canAttackCharacter(target))
			return false;
		if(!GeoEngine.canSeeTarget(actor, target))
			return false;

		actor.getAggroList().addDamageHate(target, 0, 2);

		if((target.isSummon() || target.isPet()))
			actor.getAggroList().addDamageHate(target.getPlayer(), 0, 1);

		startRunningTask(AI_TASK_ATTACK_DELAY);
		setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);

		return true;
	}

	protected void setIsInRandomAnimation(long time)
	{
		_randomAnimationEnd = System.currentTimeMillis() + time;
	}

	protected boolean randomAnimation()
	{
		NpcInstance actor = getActor();

		if(actor.getParameter("noRandomAnimation", false))
			return false;

		if(actor.hasRandomAnimation() && !actor.isActionsDisabled() && !actor.isMoving && !actor.isInCombat() && Rnd.chance(Config.RND_ANIMATION_RATE))
		{
			setIsInRandomAnimation(3000);
			actor.onRandomAnimation();
			return true;
		}
		return false;
	}

	protected boolean randomWalk()
	{
		NpcInstance actor = getActor();

		if(actor.getParameter("noRandomWalk", false))
			return false;

		return !actor.isMoving && maybeMoveToHome(false);
	}

	/**
	 * @return true если действие выполнено, false если нет
	 */
	protected boolean thinkActive()
	{
		if(super.thinkActive())
			return true;

		NpcInstance actor = getActor();
		if(actor.isActionsDisabled())
			return true;

		if(_randomAnimationEnd > System.currentTimeMillis())
			return true;

		if(_def_think)
		{
			if(doTask())
				clearTasks();
			return true;
		}


		long now = System.currentTimeMillis();
		if(now - _checkAggroTimestamp > Config.AGGRO_CHECK_INTERVAL)
		{
			_checkAggroTimestamp = now;

			boolean aggressive = Rnd.chance(actor.getParameter("SelfAggressive", actor.isAggressive() ? 100 : 0));
			if(!actor.getAggroList().isEmpty() || aggressive)
			{
				List<Creature> chars = World.getAroundCharacters(actor);
				try
				{
					Collections.sort(chars, _nearestTargetComparator);
				}
				catch(Exception e)
				{
					// Заглушка против глюка явы: Comparison method violates its general contract!
				}

				for(Creature cha : chars)
				{
					if(aggressive || actor.getAggroList().get(cha) != null)
						if(checkAggression(cha))
							return true;
				}
			}
		}

		if(actor.isMinion())
		{
			MonsterInstance leader = ((MinionInstance) actor).getLeader();
			if(leader != null)
			{
				double distance = actor.getDistance(leader);
				if(distance > _maxPursueRange || !GeoEngine.canSeeTarget(actor, leader))
				{
					actor.teleToLocation(leader.getMinionPosition());
					return true;
				}
				else if(distance > 200)
				{
					addTaskMove(leader.getMinionPosition(), true, false);
					return true;
				}
			}
		}

		if(randomAnimation())
			return true;

		if(randomWalk())
			return true;

		return false;
	}

	@Override
	protected void onIntentionIdle()
	{
		NpcInstance actor = getActor();

		// Удаляем все задания
		clearTasks();

		actor.stopMove();
		actor.getAggroList().clear(true);
		setAttackTarget(null);

		super.onIntentionIdle();
	}

	@Override
	protected void onIntentionActive()
	{
		NpcInstance actor = getActor();

		actor.stopMove();
		actor.setLastAttackTime(-1);

		if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
		{
			switchAITask(AI_TASK_ACTIVE_DELAY);
			changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
		}

		onEvtThink();
	}

	@Override
	protected void onIntentionAttack(Creature target)
	{
		NpcInstance actor = getActor();

		// Удаляем все задания
		clearTasks();

		actor.stopMove();
		setAttackTarget(target);
		setGlobalAggro(0);

		if(getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
		{
			changeIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
			switchAITask(AI_TASK_ATTACK_DELAY);
		}

		onEvtThink();
	}

	protected boolean canAttackCharacter(Creature target)
	{
		return target.isPlayable();
	}

	protected boolean checkTarget(Creature target, int range)
	{
		NpcInstance actor = getActor();
		if(target == null || target.isAlikeDead() || !actor.isInRangeZ(target, range))
			return false;

		if(target.isPlayable() && ((Playable) target).isInNonAggroTime())
			return false;

		// если не видим чаров в хайде - не атакуем их
		final boolean hided = target.isPlayable() && !canSeeInHide((Playable)target);

		if(!hided && actor.isConfused())
			return true;

		//В состоянии атаки атакуем всех, на кого у нас есть хейт
		if(getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
		{
			AggroInfo ai = actor.getAggroList().get(target);
			if (ai != null)
			{
				if (hided)
				{
					ai.hate = 0; // очищаем хейт
					return false;
				}
				return ai.hate > 0;
			}
			return false;
		}

		return canAttackCharacter(target);
	}

	protected void thinkAttack()
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
			return;

		if(!actor.isInRange(actor.getSpawnedLoc(), _maxPursueRange))
		{
			returnHomeAndRestore(actor.isRunning());
			return;
		}

		if(!actor.isRunning() && _runningTask == null) // Если моб не бежит атаковать, то меняем ходьбу на бег.
			actor.setRunning();

		if(doTask())
		{
			if(!actor.isAttackingNow() && !actor.isCastingNow())
			{
				if(!createNewTask())
				{
					if(actor.getLastAttackTime() > 0 && System.currentTimeMillis() > (actor.getLastAttackTime() + MAX_ATTACK_TIMEOUT))
						returnHome(false);
				}
			}
		}
	}

	@Override
	protected void onEvtSpawn()
	{
		setGlobalAggro(System.currentTimeMillis() + getActor().getParameter("globalAggro", 10000L));

		_UDFlag = UD_NONE;

		setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	}

	@Override
	protected void onEvtReadyToAct()
	{
		onEvtThink();
	}

	@Override
	protected void onEvtArrivedTarget()
	{
		onEvtThink();
	}

	@Override
	protected void onEvtArrived()
	{
		onEvtThink();
		super.onEvtArrived();
	}

	protected boolean tryMoveToTarget(Creature target)
	{
		return tryMoveToTarget(target, 0);
	}

	protected boolean tryMoveToTarget(Creature target, int range)
	{
		NpcInstance actor = getActor();

		if(target.isInvisible(actor)) // TODO: Что это?
		{
			notifyEvent(CtrlEvent.EVT_THINK);
			return false;
		}

		if(!actor.isInRange(actor.getSpawnedLoc(), _maxPursueRange))
		{
			returnHomeAndRestore(actor.isRunning());
			return false;
		}

		if(actor.followToCharacter(target, actor.getPhysicalAttackRange(), true))
			return true;

		// Не гонимся за персонажем, если мы его уже не видем.
		if(!GeoEngine.canSeeTarget(actor, target))
			return false;

		_pathfindFails++;

		if(_pathfindFails >= getMaxPathfindFails() && (System.currentTimeMillis() > (actor.getLastAttackTime() + TELEPORT_TIMEOUT)))
		{
			_pathfindFails = 0;

			if(target.isPlayable())
			{
				AggroInfo hate = actor.getAggroList().get(target);
				if(hate == null || hate.hate < 100)
				{
					returnHome(false);
					return false;
				}
			}

			Location targetLoc = target.getLoc();
			Location loc = GeoEngine.moveCheckForAI(targetLoc, actor.getLoc(), actor.getGeoIndex());
			if(loc == null || !GeoEngine.canMoveToCoord(actor.getX(), actor.getY(), actor.getZ(), loc.x, loc.y, loc.z, actor.getGeoIndex())) // Для подстраховки
				loc = targetLoc;
			actor.teleToLocation(loc);
			return true;
		}

		return false;
	}

	protected boolean maybeNextTask(Task currentTask)
	{
		// Следующее задание
		_tasks.remove(currentTask);
		// Если заданий больше нет - определить новое
		if(_tasks.size() == 0)
			return true;
		return false;
	}

	protected boolean doTask()
	{
		NpcInstance actor = getActor();

		if(!_def_think)
			return true;

		Task currentTask = _tasks.pollFirst();
		if(currentTask == null)
		{
			clearTasks();
			return true;
		}

		if(actor.isDead() || actor.isAttackingNow() || actor.isCastingNow())
			return false;

		switch(currentTask.type)
		{
			// Задание "прибежать в заданные координаты"
			case MOVE:
			{
				if(actor.isMovementDisabled() || !getIsMobile())
					return true;

				if(actor.isInRange(currentTask.loc, 100))
					return maybeNextTask(currentTask);

				if(actor.isMoving)
					return false;

				if(!actor.moveToLocation(currentTask.loc, 0, currentTask.pathfind))
				{
					if(currentTask.teleportIfCantMove) {
						actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, 2036, 1, 500, 0));
						ThreadPoolManager.getInstance().schedule(new Teleport(currentTask.loc), 500L);
						return false;
					} else
						return maybeNextTask(currentTask);
				}
			}
				break;
			// Задание "добежать - ударить"
			case ATTACK:
			{
				Creature target = currentTask.target.get();

				if(!checkTarget(target, getMaxHateRange()))
					return true;

				setAttackTarget(target);

				if(actor.isMoving)
					return Rnd.chance(25);

				if(actor.getRealDistance3D(target) <= actor.getPhysicalAttackRange() + 40 && GeoEngine.canSeeTarget(actor, target))
				{
					if(actor.isAttackingDisabled())
						return false;

					clientStopMoving();
					_pathfindFails = 0;
					actor.doAttack(target);
					return maybeNextTask(currentTask);
				}

				// Меняем цель, если это возможно. Иначе так и будет бегать за первой целью, пока не остановится.
				if(prepareTarget() != target)
					return true;

				if(actor.isMovementDisabled() || !getIsMobile())
					return true;

				tryMoveToTarget(target);
			}
				break;
			// Задание "добежать - атаковать скиллом"
			case CAST:
			{
				Creature target = currentTask.target.get();

				if(actor.isMuted(currentTask.skill) || actor.isSkillDisabled(currentTask.skill) || actor.isUnActiveSkill(currentTask.skill.getId()))
					return true;

				boolean isAoE = currentTask.skill.getTargetType() == Skill.SkillTargetType.TARGET_AURA;
				int castRange = currentTask.skill.getAOECastRange();

				if(!checkTarget(target, getMaxHateRange() + castRange))
					return true;

				setCastTarget(target);

				if(actor.getRealDistance3D(target) <= castRange + 60 && GeoEngine.canSeeTarget(actor, target))
				{
					clientStopMoving();
					_pathfindFails = 0;
					actor.doCast(currentTask.skill, isAoE ? actor : target, !target.isPlayable());
					return maybeNextTask(currentTask);
				}

				if(actor.isMoving)
					return Rnd.chance(10);

				if(actor.isMovementDisabled() || !getIsMobile())
					return true;

				tryMoveToTarget(target, castRange);
			}
				break;
			// Задание "добежать - применить скилл"
			case BUFF:
			{
				Creature target = currentTask.target.get();

				if(actor.isMuted(currentTask.skill) || actor.isSkillDisabled(currentTask.skill) || actor.isUnActiveSkill(currentTask.skill.getId()))
					return true;

				if(target == null || target.isAlikeDead() || !actor.isInRange(target, 2000))
					return true;

				boolean isAoE = currentTask.skill.getTargetType() == Skill.SkillTargetType.TARGET_AURA;
				int castRange = currentTask.skill.getAOECastRange();

				if(actor.isMoving)
					return Rnd.chance(10);

				if(actor.getRealDistance3D(target) <= castRange + 60 && GeoEngine.canSeeTarget(actor, target))
				{
					clientStopMoving();
					_pathfindFails = 0;
					actor.doCast(currentTask.skill, isAoE ? actor : target, !target.isPlayable());
					return maybeNextTask(currentTask);
				}

				if(actor.isMovementDisabled() || !getIsMobile())
					return true;

				tryMoveToTarget(target);
			}
				break;
		}

		return false;
	}

	protected boolean createNewTask()
	{
		return false;
	}

	protected boolean defaultNewTask()
	{
		clearTasks();

		NpcInstance actor = getActor();
		Creature target;
		if(actor == null || (target = prepareTarget()) == null)
		{
			if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
			{
				changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
				return maybeMoveToHome(true);
			}
			return false;
		}
		return chooseTaskAndTargets(null, target, actor.getDistance(target));
	}

	@Override
	protected void onEvtThink()
	{
		NpcInstance actor = getActor();
		if(_thinking || actor == null || actor.isActionsDisabled() || actor.isAfraid())
			return;

		if(_randomAnimationEnd > System.currentTimeMillis())
			return;

		_thinking = true;
		try
		{
			if(!Config.BLOCK_ACTIVE_TASKS && (getIntention() == CtrlIntention.AI_INTENTION_ACTIVE || getIntention() == CtrlIntention.AI_INTENTION_WALKER_ROUTE))
				thinkActive();
			else if(getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
				thinkAttack();
			else if(getIntention() == CtrlIntention.AI_INTENTION_RETURN_HOME)
				thinkReturnHome();
		}
		finally
		{
			_thinking = false;
		}
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();

		int transformer = actor.getParameter("transformOnDead", 0);
		int chance = actor.getParameter("transformChance", 100);
		if(transformer > 0 && Rnd.chance(chance))
		{
			NpcInstance npc = NpcUtils.spawnSingle(transformer, actor.getLoc(), actor.getReflection()) ;

			if(killer != null && killer.isPlayable())
			{
				npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, 100);
				killer.setTarget(npc);
				killer.sendPacket(npc.makeStatusUpdate(StatusUpdatePacket.CUR_HP, StatusUpdatePacket.MAX_HP));
			}
		}

		super.onEvtDead(killer);
	}

	@Override
	protected void onEvtClanAttacked(Creature attacked, Creature attacker, int damage)
	{
		if(getIntention() == CtrlIntention.AI_INTENTION_RETURN_HOME)
			return;

		if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE || !isGlobalAggro())
			return;

		if(damage <= 0)
			return;

		notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 2);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		if(getIntention() == CtrlIntention.AI_INTENTION_RETURN_HOME)
			return;

		NpcInstance actor = getActor();
		if(attacker == null || actor.isDead())
			return;

		Player player = attacker.getPlayer();

		if(player != null)
		{
			List<QuestState> quests = player.getQuestsForEvent(actor, QuestEventType.ATTACKED_WITH_QUEST);
			if(quests != null)
			{
				for(QuestState qs : quests)
					qs.getQuest().notifyAttack(actor, qs);
			}
		}

		if(damage <= 0)
			return;

		if(attacker.isInvisible(actor))
			return;

		int transformer = actor.getParameter("transformOnUnderAttack", 0);
		if(transformer > 0)
		{
			int chance = actor.getParameter("transformChance", 5);
			if(chance == 100 || ((MonsterInstance) actor).getChampion() == 0 && actor.getCurrentHpPercents() > 50 && Rnd.chance(chance))
			{
				MonsterInstance npc = (MonsterInstance) NpcHolder.getInstance().getTemplate(transformer).getNewInstance();
				npc.setSpawnedLoc(actor.getLoc());
				npc.setReflection(actor.getReflection());
				npc.setChampion(((MonsterInstance) actor).getChampion());
				npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp(), true);
				npc.spawnMe(npc.getSpawnedLoc());
				npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 100);
				actor.doDie(actor);
				actor.decayMe();
				attacker.setTarget(npc);
				attacker.sendPacket(npc.makeStatusUpdate(StatusUpdatePacket.CUR_HP, StatusUpdatePacket.MAX_HP));
				return;
			}
		}

		if(Config.RETAIL_SS)
		{

			if(player != null)
			{
				//FIXME [G1ta0] затычка для 7 печатей, при атаке монстра 7 печатей телепортирует персонажа в ближайший город
				if(((SevenSigns.getInstance().isSealValidationPeriod()) || (SevenSigns.getInstance().isCompResultsPeriod())) && (actor.isSevenSignsMonster()) && (!Config.ENABLE_CAT_NEC_FREE_FARM))
				{
					int pcabal = SevenSigns.getInstance().getPlayerCabal(player);
					int wcabal = SevenSigns.getInstance().getCabalHighestScore();
					if((pcabal != wcabal) && (wcabal != SevenSigns.CABAL_NULL))
					{
						//player.sendMessage(new CustomMessage("l2s.gameserver.ai.DefaultAI.TpCabal", player));
						player.sendMessage("You have been teleported to the nearest town because you not signed for winning cabal.");
						player.teleToClosestTown();
						return;
					}
				}
			}
		}

		//Добавляем только хейт, урон, если атакующий - игровой персонаж, будет добавлен в L2NpcInstance.onReduceCurrentHp
		actor.getAggroList().addDamageHate(attacker, 0, damage);

		// Обычно 1 хейт добавляется хозяину суммона, чтобы после смерти суммона моб накинулся на хозяина.
		if(attacker.isServitor())
			actor.getAggroList().addDamageHate(attacker.getPlayer(), 0, actor.getParameter("searchingMaster", false) ? damage : 1);

		if(getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
		{
			if(!actor.isRunning())
				startRunningTask(AI_TASK_ATTACK_DELAY);
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		}

		notifyFriends(attacker, damage);

		checkUD(attacker);
	}

	@Override
	protected void onEvtAggression(Creature attacker, int aggro)
	{
		if(getIntention() == CtrlIntention.AI_INTENTION_RETURN_HOME)
			return;

		NpcInstance actor = getActor();
		if(attacker == null || actor.isDead())
			return;

		actor.getAggroList().addDamageHate(attacker, 0, aggro);

		// Обычно 1 хейт добавляется хозяину суммона, чтобы после смерти суммона моб накинулся на хозяина.
		if(aggro > 0 && (attacker.isSummon() || attacker.isPet()))
			actor.getAggroList().addDamageHate(attacker.getPlayer(), 0, actor.getParameter("searchingMaster", false) ? aggro : 1);

		if(getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
		{
			if(!actor.isRunning())
				startRunningTask(AI_TASK_ATTACK_DELAY);
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		}
		if(actor.isRaid() || actor.hasMinions())
			notifyFriends(attacker, 100);
	}

	@Override
	protected void onEvtFinishCasting(Skill skill, Creature target, boolean success)
	{
		if(success && skill == UD_SKILL)
			_UDFlag = UD_CASTED;
	}

	protected boolean maybeMoveToHome(boolean force)
	{
		NpcInstance actor = getActor();
		if(actor.isDead() || actor.isMovementDisabled())
			return false;

		Location sloc = actor.getSpawnedLoc();
		boolean isInRange = actor.isInRangeZ(sloc, Config.MAX_DRIFT_RANGE);

		if(!force)
		{
			boolean randomWalk = actor.hasRandomWalk();

			// Random walk or not?
			if(randomWalk && (!Config.RND_WALK || !Rnd.chance(Config.RND_WALK_RATE)))
				return false;


			if(!randomWalk && isInRange)
				return false;
		}

		Location pos = Location.findPointToStay(actor, sloc, 0, Config.MAX_DRIFT_RANGE);

		actor.setWalking();

		// Телепортируемся домой, только если далеко от дома
		if(!actor.moveToLocation(pos.x, pos.y, pos.z, 0, true) && !isInRange)
		{
			actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, 2036, 1, 500, 0));
			ThreadPoolManager.getInstance().schedule(new Teleport(sloc), 500L);
		}
		return true;
	}

	public boolean returnHomeAndRestore(boolean running)
	{
		NpcInstance actor = getActor();
		if(returnHome(running, actor.isRaid() ? Config.ALWAYS_TELEPORT_HOME_RB : Config.ALWAYS_TELEPORT_HOME, running, true))
		{
			if(canRestoreOnReturnHome())
			{
				actor.setCurrentHpMp(actor.getMaxHp(), actor.getMaxMp());
			}
			return true;
		}
		return false;
	}

	protected boolean returnHome(boolean running)
	{
		NpcInstance actor = getActor();
		return returnHome(true, actor.isRaid() ? Config.ALWAYS_TELEPORT_HOME_RB : Config.ALWAYS_TELEPORT_HOME, running, false);
	}

	protected boolean teleportHome()
	{
		return returnHome(true, true, false, false);
	}

	protected boolean returnHome(boolean clearAggro, boolean teleport, boolean running, boolean force)
	{
		NpcInstance actor = getActor();
		if(actor.isDead() || actor.isMovementDisabled())
			return false;

		if(actor.isMinion())
		{
			// Миньон удаляется при переходе в активный режим, если лидер мертв (оффлайк).
			NpcInstance leader = actor.getLeader();
			if(leader != null && !leader.isVisible())
			{
				actor.deleteMe();
				return false;
			}
		}

		Location sloc = actor.getSpawnedLoc();
		if(actor.isInRangeZ(sloc, 32))
			return false;

		if(!teleport && getIntention() == CtrlIntention.AI_INTENTION_RETURN_HOME)
			return false;

		// Удаляем все задания
		clearTasks();
		actor.stopMove();

		if(clearAggro)
			actor.getAggroList().clear(true);

		setAttackTarget(null);

		if(teleport)
		{
			changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
			actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, 2036, 1, 500, 0));
			ThreadPoolManager.getInstance().schedule(new Teleport(sloc), 500L);
		}
		else if(force)
		{
			setIntention(CtrlIntention.AI_INTENTION_RETURN_HOME, running);
		}
		else
		{
			changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);

			if(running)
				actor.setRunning();
			else
				actor.setWalking();

			addTaskMove(sloc, true, false);
		}
		return true;
	}

	@Override
	protected void onIntentionReturnHome(boolean running)
	{
		NpcInstance actor = getActor();

		if(running)
			actor.setRunning();
		else
			actor.setWalking();

		changeIntention(CtrlIntention.AI_INTENTION_RETURN_HOME, null, null);

		onEvtThink();
	}

	private void thinkReturnHome()
	{
		clearTasks();

		NpcInstance actor = getActor();
		Location spawnLoc = actor.getSpawnedLoc();
		if(actor.isInRange(spawnLoc, Math.min(_maxPursueRange, 100)))
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		else
		{
			addTaskMove(spawnLoc, true, true);
			doTask();
		}
	}

	protected boolean canRestoreOnReturnHome()
	{
		return _canRestoreOnReturnHome;
	}

	protected void checkUD(Creature attacker)
	{
		if(_UDRate == 0/* || (skill != null && skill.getTemplate().isUDSafe())*/)
			return;

		if(getActor().getDistance(attacker) > UD_DISTANCE)
		{
			if(_UDFlag == UD_NONE || _UDFlag == UD_CASTED)
			{
				if(Rnd.chance(_UDRate) && canUseSkill(UD_SKILL, getActor(), 0))
					_UDFlag = UD_CAST;
			}
		}
		else
		{
			if(_UDFlag == UD_CASTED || _UDFlag == UD_CAST)
			{
				getActor().getEffectList().stopEffect(UD_SKILL);
				_UDFlag = UD_NONE;
			}
		}
	}

	protected boolean applyUD()
	{
		if(_UDRate == 0 || _UDFlag == UD_NONE)
			return false;

		if(_UDFlag == UD_CAST)
		{
			addTaskBuff(getActor(), UD_SKILL);
			return true;
		}
		return false;
	}

	protected Creature prepareTarget()
	{
		NpcInstance actor = getActor();

		if(actor.isConfused())
			return getAttackTarget();

		// Для "двинутых" боссов, иногда, выбираем случайную цель
		if(Rnd.chance(actor.getParameter("isMadness", 0)))
		{
			Creature randomHated = actor.getAggroList().getRandomHated(getMaxHateRange());
			if(randomHated != null)
			{
				setAttackTarget(randomHated);
				if(_madnessTask == null && !actor.isConfused())
				{
					actor.startConfused();
					_madnessTask = ThreadPoolManager.getInstance().schedule(new MadnessTask(), 10000);
				}
				return randomHated;
			}
		}

		// Новая цель исходя из агрессивности
		List<Creature> hateList = actor.getAggroList().getHateList(-1);
		Creature hated = null;
		for(Creature cha : hateList)
		{
			// Если у Монстра есть скилл "Searching Master" он должен атаковать хозяина пета в первую очередь.
			if(cha.isServitor() && cha.getPlayer() != null)
			{
				if(getActor().getSkillLevel(6019) == 1 && checkTarget(cha.getPlayer(), getMaxHateRange()))
					cha = cha.getPlayer();
			}

			//Не подходит, очищаем хейт
			if(!checkTarget(cha, getMaxHateRange()))
			{
				actor.getAggroList().remove(cha, true);
				continue;
			}
			hated = cha;
			break;
		}

		if(hated != null)
		{
			setAttackTarget(hated);
			return hated;
		}

		return null;
	}

	protected boolean canUseSkill(Skill skill, Creature target, double distance)
	{
		NpcInstance actor = getActor();
		if(skill == null || skill.isNotUsedByAI())
			return false;

		if(skill.getTargetType() == Skill.SkillTargetType.TARGET_SELF && target != actor)
			return false;

		int castRange = skill.getAOECastRange();
		if(castRange <= 200 && distance > 200)
			return false;

		if(actor.isSkillDisabled(skill) || actor.isMuted(skill) || actor.isUnActiveSkill(skill.getId()))
			return false;

		double mpConsume2 = skill.getMpConsume2();
		if(skill.isMagic())
			mpConsume2 = actor.calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, target, skill);
		else
			mpConsume2 = actor.calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, target, skill);
		if(actor.getCurrentMp() < mpConsume2)
			return false;

		if(target.getEffectList().getEffectsCountForSkill(skill.getId()) != 0)
			return false;

		return true;
	}

	protected boolean canUseSkill(Skill sk, Creature target)
	{
		return canUseSkill(sk, target, 0);
	}

	protected Skill[] selectUsableSkills(Creature target, double distance, Skill[] skills)
	{
		if(skills == null || skills.length == 0 || target == null)
			return null;

		Skill[] ret = null;
		int usable = 0;

		for(Skill skill : skills)
			if(canUseSkill(skill, target, distance))
			{
				if(ret == null)
					ret = new Skill[skills.length];
				ret[usable++] = skill;
			}

		if(ret == null || usable == skills.length)
			return ret;

		if(usable == 0)
			return null;

		ret = Arrays.copyOf(ret, usable);
		return ret;
	}

	protected static Skill selectTopSkillByDamage(Creature actor, Creature target, double distance, Skill[] skills)
	{
		if(skills == null || skills.length == 0)
			return null;

		if(skills.length == 1)
			return skills[0];

		Skill oneTargetSkill = null;
		for(Skill skill : skills)
		{
			if(skill.oneTarget())
			{
				if(oneTargetSkill == null || skill.getCastRange() >= distance && (distance / oneTargetSkill.getCastRange()) < (distance / skill.getCastRange()))
					oneTargetSkill = skill;
			}
		}

		RndSelector<Skill> rnd = new RndSelector<Skill>(skills.length);

		double weight;
		for(Skill skill : skills)
		{
			if(!skill.oneTarget())
			{
				weight = skill.getSimpleDamage(actor, target) / 10 + (distance / skill.getCastRange() * 100);
				if(weight < 1.)
					weight = 1.;
				rnd.add(skill, (int) weight);
			}
		}

		Skill aoeSkill = rnd.select();

		if(aoeSkill == null)
			return oneTargetSkill;

		if(oneTargetSkill == null)
			return aoeSkill;

		if(Rnd.chance(90))
			return oneTargetSkill;
		else
			return aoeSkill;
	}

	protected static Skill selectTopSkillByDebuff(Creature actor, Creature target, double distance, Skill[] skills) //FIXME
	{
		if(skills == null || skills.length == 0)
			return null;

		if(skills.length == 1)
			return skills[0];

		RndSelector<Skill> rnd = new RndSelector<Skill>(skills.length);
		double weight;
		for(Skill skill : skills)
		{
			if(skill.getSameByStackType(target) != null)
				continue;
			if((weight = 100. * skill.getAOECastRange() / distance) <= 0)
				weight = 1;
			rnd.add(skill, (int) weight);
		}
		return rnd.select();
	}

	protected static Skill selectTopSkillByBuff(Creature target, Skill[] skills)
	{
		if(skills == null || skills.length == 0)
			return null;

		if(skills.length == 1)
			return skills[0];

		RndSelector<Skill> rnd = new RndSelector<Skill>(skills.length);
		double weight;
		for(Skill skill : skills)
		{
			if(skill.getSameByStackType(target) != null)
				continue;
			if((weight = skill.getPower()) <= 0)
				weight = 1;
			rnd.add(skill, (int) weight);
		}
		return rnd.select();
	}

	protected static Skill selectTopSkillByHeal(Creature target, Skill[] skills)
	{
		if(skills == null || skills.length == 0)
			return null;

		double hpReduced = target.getMaxHp() - target.getCurrentHp();
		if(hpReduced < 1)
			return null;

		if(skills.length == 1)
			return skills[0];

		RndSelector<Skill> rnd = new RndSelector<Skill>(skills.length);
		double weight;
		for(Skill skill : skills)
		{
			if((weight = Math.abs(skill.getPower() - hpReduced)) <= 0)
				weight = 1;
			rnd.add(skill, (int) weight);
		}
		return rnd.select();
	}

	protected void addDesiredSkill(Map<Skill, Integer> skillMap, Creature target, double distance, Skill[] skills)
	{
		if(skills == null || skills.length == 0 || target == null)
			return;
		for(Skill sk : skills)
			addDesiredSkill(skillMap, target, distance, sk);
	}

	protected void addDesiredSkill(Map<Skill, Integer> skillMap, Creature target, double distance, Skill skill)
	{
		if(skill == null || target == null || !canUseSkill(skill, target))
			return;
		int weight = (int) -Math.abs(skill.getAOECastRange() - distance);
		if(skill.getAOECastRange() >= distance)
			weight += 1000000;
		else if(skill.isNotTargetAoE() && skill.getTargets(getActor(), target, false).size() == 0)
			return;
		skillMap.put(skill, weight);
	}

	protected void addDesiredHeal(Map<Skill, Integer> skillMap, Skill[] skills)
	{
		if(skills == null || skills.length == 0)
			return;
		NpcInstance actor = getActor();
		double hpReduced = actor.getMaxHp() - actor.getCurrentHp();
		double hpPercent = actor.getCurrentHpPercents();
		if(hpReduced < 1)
			return;
		int weight;
		for(Skill sk : skills)
			if(canUseSkill(sk, actor) && sk.getPower() <= hpReduced)
			{
				weight = (int) sk.getPower();
				if(hpPercent < 50)
					weight += 1000000;
				skillMap.put(sk, weight);
			}
	}

	protected void addDesiredBuff(Map<Skill, Integer> skillMap, Skill[] skills)
	{
		if(skills == null || skills.length == 0)
			return;
		NpcInstance actor = getActor();
		for(Skill sk : skills)
			if(canUseSkill(sk, actor))
				skillMap.put(sk, 1000000);
	}

	protected Skill selectTopSkill(Map<Skill, Integer> skillMap)
	{
		if(skillMap == null || skillMap.isEmpty())
			return null;
		int nWeight, topWeight = Integer.MIN_VALUE;
		for(Skill next : skillMap.keySet())
			if((nWeight = skillMap.get(next)) > topWeight)
				topWeight = nWeight;
		if(topWeight == Integer.MIN_VALUE)
			return null;

		Skill[] skills = new Skill[skillMap.size()];
		nWeight = 0;
		for(Map.Entry<Skill, Integer> e : skillMap.entrySet())
		{
			if(e.getValue() < topWeight)
				continue;
			skills[nWeight++] = e.getKey();
		}
		return skills[Rnd.get(nWeight)];
	}

	protected boolean chooseTaskAndTargets(Skill skill, Creature target, double distance)
	{
		NpcInstance actor = getActor();

		// Использовать скилл если можно, иначе атаковать
		if(skill != null)
		{
			// Проверка цели, и смена если необходимо
			if(actor.isMovementDisabled() && distance > skill.getAOECastRange() + 60)
			{
				target = null;
				if(skill.isOffensive())
				{
					LazyArrayList<Creature> targets = LazyArrayList.newInstance();
					for(Creature cha : actor.getAggroList().getHateList(getMaxHateRange()))
					{
						if(!checkTarget(cha, skill.getAOECastRange() + 60) || !canUseSkill(skill, cha))
							continue;
						targets.add(cha);
					}
					if(!targets.isEmpty())
						target = targets.get(Rnd.get(targets.size()));
					LazyArrayList.recycle(targets);
				}
			}

			if(target == null)
				return false;

			// Добавить новое задание
			if(skill.isOffensive())
				addTaskCast(target, skill);
			else
				addTaskBuff(target, skill);
			return true;
		}

		// Смена цели, если необходимо
		if(actor.isMovementDisabled() && distance > actor.getPhysicalAttackRange() + 40)
		{
			target = null;
			LazyArrayList<Creature> targets = LazyArrayList.newInstance();
			for(Creature cha : actor.getAggroList().getHateList(getMaxHateRange()))
			{
				if(!checkTarget(cha, actor.getPhysicalAttackRange() + 40))
					continue;
				targets.add(cha);
			}
			if(!targets.isEmpty())
				target = targets.get(Rnd.get(targets.size()));
			LazyArrayList.recycle(targets);
		}

		if(target == null)
			return false;

		// Добавить новое задание
		addTaskAttack(target);
		return true;
	}

	protected void clearTasks()
	{
		_def_think = false;
		_tasks.clear();
	}

	/** переход в режим бега через определенный интервал времени */
	protected void startRunningTask(long interval)
	{
		NpcInstance actor = getActor();
		if(actor != null && _runningTask == null && !actor.isRunning())
			_runningTask = ThreadPoolManager.getInstance().schedule(new RunningTask(), interval);
	}

	protected boolean isGlobalAggro()
	{
		if(_globalAggro == 0)
			return true;
		if(_globalAggro <= System.currentTimeMillis())
		{
			_globalAggro = 0;
			return true;
		}
		return false;
	}

	public void setGlobalAggro(long value)
	{
		_globalAggro = value;
	}

	protected boolean defaultThinkBuff(int rateSelf)
	{
		return defaultThinkBuff(rateSelf, 0);
	}

	/**
	 * Оповестить дружественные цели об атаке.
	 * @param attacker
	 * @param damage
	 */
	protected void notifyFriends(Creature attacker, int damage)
	{
		if(damage <= 0)
			return;

		NpcInstance actor = getActor();
		if(System.currentTimeMillis() - _lastFactionNotifyTime > _minFactionNotifyInterval)
		{
			_lastFactionNotifyTime = System.currentTimeMillis();
			if(actor.isMinion())
			{
				//Оповестить лидера об атаке
				MonsterInstance master = ((MinionInstance) actor).getLeader();
				if(master != null)
				{
					if(!master.isDead() && master.isVisible())
						master.getAI().notifyEvent(CtrlEvent.EVT_CLAN_ATTACKED, new Object[] { actor, attacker, damage });

					//Оповестить минионов лидера об атаке
					MinionList minionList = master.getMinionList();
					if(minionList != null)
					{
						for(MinionInstance minion : minionList.getAliveMinions())
						{
							if(minion != actor)
								minion.getAI().notifyEvent(CtrlEvent.EVT_CLAN_ATTACKED, new Object[] { actor, attacker, damage });
						}
					}
				}
			}

			//Оповестить своих минионов об атаке
			MinionList minionList = actor.getMinionList();
			if(minionList != null && minionList.hasAliveMinions())
			{
				for(MinionInstance minion : minionList.getAliveMinions())
					minion.getAI().notifyEvent(CtrlEvent.EVT_CLAN_ATTACKED, new Object[] { actor, attacker, damage });
			}

			//Оповестить социальных мобов
			for(NpcInstance npc : activeFactionTargets())
				npc.getAI().notifyEvent(CtrlEvent.EVT_CLAN_ATTACKED, new Object[] { actor, attacker, damage });
		}
	}

	protected List<NpcInstance> activeFactionTargets()
	{
		NpcInstance actor = getActor();
		if(actor.getFaction().isNone())
			return Collections.emptyList();

		List<NpcInstance> npcFriends = new LazyArrayList<NpcInstance>();
		for(NpcInstance npc : World.getAroundNpc(actor))
		{
			if(!npc.isDead())
			{
				if(npc.isInFaction(actor))
				{
					if(npc.isInRangeZ(actor, npc.getFaction().getRange()))
					{
						if(GeoEngine.canSeeTarget(npc, actor))
							npcFriends.add(npc);
					}
				}
			}
		}
		return npcFriends;
	}

	protected boolean defaultThinkBuff(int rateSelf, int rateFriends)
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
			return true;

		//TODO сделать более разумный выбор баффа, сначала выбирать подходящие а потом уже рандомно 1 из них
		if(Rnd.chance(rateSelf))
		{
			double actorHp = actor.getCurrentHpPercents();

			Skill[] skills = actorHp < 50 ? selectUsableSkills(actor, 0, _healSkills) : selectUsableSkills(actor, 0, _buffSkills);
			if(skills == null || skills.length == 0)
				return false;

			Skill skill = skills[Rnd.get(skills.length)];
			addTaskBuff(actor, skill);
			return true;
		}

		if(Rnd.chance(rateFriends))
		{
			for(NpcInstance npc : activeFactionTargets())
			{
				double targetHp = npc.getCurrentHpPercents();

				Skill[] skills = targetHp < 50 ? selectUsableSkills(actor, 0, _healSkills) : selectUsableSkills(actor, 0, _buffSkills);
				if(skills == null || skills.length == 0)
					continue;

				Skill skill = skills[Rnd.get(skills.length)];
				addTaskBuff(actor, skill);
				return true;
			}
		}

		return false;
	}

	protected boolean defaultFightTask()
	{
		clearTasks();

		NpcInstance actor = getActor();
		if(actor.isDead() || actor.isAMuted())
			return false;

		Creature target;
		if((target = prepareTarget()) == null)
		{
			if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
			{
				changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
				return maybeMoveToHome(true);
			}
			return false;
		}

		if(applyUD())
			return true;

		double distance = actor.getDistance(target);
		double targetHp = target.getCurrentHpPercents();
		double actorHp = actor.getCurrentHpPercents();

		Skill[] dam = Rnd.chance(getRateDAM()) ? selectUsableSkills(target, distance, _damSkills) : null;
		Skill[] dot = Rnd.chance(getRateDOT()) ? selectUsableSkills(target, distance, _dotSkills) : null;
		Skill[] debuff = targetHp > 10 ? Rnd.chance(getRateDEBUFF()) ? selectUsableSkills(target, distance, _debuffSkills) : null : null;
		Skill[] stun = Rnd.chance(getRateSTUN()) ? selectUsableSkills(target, distance, _stunSkills) : null;
		Skill[] heal = actorHp < 50 ? Rnd.chance(getRateHEAL()) ? selectUsableSkills(actor, 0, _healSkills) : null : null;
		Skill[] buff = Rnd.chance(getRateBUFF()) ? selectUsableSkills(actor, 0, _buffSkills) : null;

		RndSelector<Skill[]> rnd = new RndSelector<Skill[]>();
		if(!actor.isAMuted())
			rnd.add(null, getRatePHYS());
		rnd.add(dam, getRateDAM());
		rnd.add(dot, getRateDOT());
		rnd.add(debuff, getRateDEBUFF());
		rnd.add(heal, getRateHEAL());
		rnd.add(buff, getRateBUFF());
		rnd.add(stun, getRateSTUN());

		Skill[] selected = rnd.select();
		if(selected != null)
		{
			if(selected == dam || selected == dot)
				return chooseTaskAndTargets(selectTopSkillByDamage(actor, target, distance, selected), target, distance);

			if(selected == debuff || selected == stun)
				return chooseTaskAndTargets(selectTopSkillByDebuff(actor, target, distance, selected), target, distance);

			if(selected == buff)
				return chooseTaskAndTargets(selectTopSkillByBuff(actor, selected), actor, distance);

			if(selected == heal)
				return chooseTaskAndTargets(selectTopSkillByHeal(actor, selected), actor, distance);
		}

		// TODO сделать лечение и баф дружественных целей

		return chooseTaskAndTargets(null, target, distance);
	}

	public int getRatePHYS()
	{
		return 100;
	}

	public int getRateDOT()
	{
		return 0;
	}

	public int getRateDEBUFF()
	{
		return 0;
	}

	public int getRateDAM()
	{
		return 0;
	}

	public int getRateSTUN()
	{
		return 0;
	}

	public int getRateBUFF()
	{
		return 0;
	}

	public int getRateHEAL()
	{
		return 0;
	}

	public boolean getIsMobile()
	{
		return !getActor().getParameter("isImmobilized", false);
	}

	public int getMaxPathfindFails()
	{
		return MAX_PATHFIND_FAILS;
	}

	/**
	 * Задержка, перед переключением в активный режим после атаки, если цель не найдена (вне зоны досягаемости, убита, очищен хейт)
	 * @return
	 */
	public int getMaxAttackTimeout()
	{
		return 15000;
	}

	/**
	 * Задержка, перед телепортом к цели, если не удается дойти
	 * @return
	 */
	public int getTeleportTimeout()
	{
		return 10000;
	}

	public void setMaxPursueRange(int value)
	{
		_maxPursueRange = value;
	}

	@Override
	public int getMaxHateRange()
	{
		return Math.max(getActor().getAggroRange(), MAX_HATE_RANGE);
	}
}
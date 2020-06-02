package l2s.gameserver.model;

import static l2s.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.set.hash.TIntHashSet;
import l2s.commons.collections.LazyArrayList;
import l2s.commons.geometry.Circle;
import l2s.commons.geometry.Shape;
import l2s.commons.lang.reference.HardReference;
import l2s.commons.lang.reference.HardReferences;
import l2s.commons.listener.Listener;
import l2s.commons.threading.RunnableImpl;
import l2s.commons.util.Rnd;
import l2s.commons.util.concurrent.atomic.AtomicState;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CharacterAI;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.ai.PlayableAI.nextAction;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.geodata.GeoMove;
import l2s.gameserver.instancemanager.DimensionalRiftManager;
import l2s.gameserver.model.GameObjectTasks.AltMagicUseTask;
import l2s.gameserver.model.GameObjectTasks.CastEndTimeTask;
import l2s.gameserver.model.GameObjectTasks.HitTask;
import l2s.gameserver.model.GameObjectTasks.MagicLaunchedTask;
import l2s.gameserver.model.GameObjectTasks.MagicUseTask;
import l2s.gameserver.model.GameObjectTasks.NotifyAITask;
import l2s.gameserver.model.GameObjectTasks.SeventeethPecentTimer;
import l2s.gameserver.model.Skill.SkillTargetType;
import l2s.gameserver.model.Skill.SkillType;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.actor.flags.CreatureFlags;
import l2s.gameserver.model.actor.listener.CharListenerList;
import l2s.gameserver.model.actor.recorder.CharStatsChangeRecorder;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.instances.MinionInstance;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.quest.QuestEventType;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.model.reference.L2Reference;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.IStaticPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.network.l2.s2c.AttackPacket;
import l2s.gameserver.network.l2.s2c.AutoAttackStartPacket;
import l2s.gameserver.network.l2.s2c.AutoAttackStopPacket;
import l2s.gameserver.network.l2.s2c.ChangeMoveTypePacket;
import l2s.gameserver.network.l2.s2c.DamageTextPacket;
import l2s.gameserver.network.l2.s2c.ExTeleportToLocationActivate;
import l2s.gameserver.network.l2.s2c.FlyToLocationPacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.MTLPacket;
import l2s.gameserver.network.l2.s2c.MagicSkillCanceled;
import l2s.gameserver.network.l2.s2c.MagicSkillLaunchedPacket;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.SetupGaugePacket;
import l2s.gameserver.network.l2.s2c.StatusUpdatePacket;
import l2s.gameserver.network.l2.s2c.StopMovePacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.network.l2.s2c.TeleportToLocationPacket;
import l2s.gameserver.network.l2.s2c.updatetype.IUpdateTypeComponent;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.skills.TimeStamp;
import l2s.gameserver.stats.Calculator;
import l2s.gameserver.stats.Env;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.Formulas.AttackInfo;
import l2s.gameserver.stats.StatFunctions;
import l2s.gameserver.stats.StatTemplate;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.funcs.Func;
import l2s.gameserver.stats.triggers.TriggerInfo;
import l2s.gameserver.stats.triggers.TriggerType;
import l2s.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2s.gameserver.taskmanager.RegenTaskManager;
import l2s.gameserver.templates.CharTemplate;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.PositionUtils;

public abstract class Creature extends GameObject
{
	public class MoveNextTask extends RunnableImpl
	{
		private double alldist, donedist;

		public MoveNextTask setDist(double dist)
		{
			alldist = dist;
			donedist = 0.;
			return this;
		}

		@Override
		public void runImpl() throws Exception
		{
			if(!isMoving)
				return;

			moveLock.lock();
			try
			{
				if(!isMoving)
					return;

				if(isMovementDisabled())
				{
					stopMove();
					return;
				}

				Creature follow = null;
				int speed = getMoveSpeed();
				if(speed <= 0)
				{
					stopMove();
					return;
				}
				long now = System.currentTimeMillis();

				if(isFollow)
				{
					follow = getFollowTarget();
					if(follow == null || follow.isInvisible(Creature.this))
					{
						stopMove();
						return;
					}
					if(isInRangeZ(follow, _offset) && GeoEngine.canSeeTarget(Creature.this, follow))
					{
						stopMove();
						ThreadPoolManager.getInstance().execute(new NotifyAITask(Creature.this, CtrlEvent.EVT_ARRIVED_TARGET));
						return;
					}
				}

				if(alldist <= 0)
				{
					moveNext(false);
					return;
				}

				donedist += (now - _startMoveTime) * _previousSpeed / 1000.;
				double done = donedist / alldist;

				if(done < 0)
					done = 0;
				if(done >= 1)
				{
					moveNext(false);
					return;
				}

				if(isMovementDisabled())
				{
					stopMove();
					return;
				}

				Location loc = null;

				int index = (int) (moveList.size() * done);
				if(index >= moveList.size())
					index = moveList.size() - 1;
				if(index < 0)
					index = 0;

				loc = moveList.get(index).clone().geo2world();

				if(!isFlying() && !isInBoat() && !isInWater() && !isBoat())
					if(loc.z - getZ() > 256)
					{
						String bug_text = "geo bug 1 at: " + getLoc() + " => " + loc.x + "," + loc.y + "," + loc.z + "\tAll path: " + moveList.get(0) + " => " + moveList.get(moveList.size() - 1);
						Log.add(bug_text, "geo");
						stopMove();
						return;
					}

				// Проверяем, на всякий случай
				if(loc == null || isMovementDisabled())
				{
					stopMove();
					return;
				}

				setLoc(loc, true);

				// В процессе изменения координат, мы остановились
				if(isMovementDisabled())
				{
					stopMove();
					return;
				}

				if(isFollow && now - _followTimestamp > (_forestalling ? 500 : 1000) && follow != null && !follow.isInRange(movingDestTempPos, Math.max(50, _offset)))
				{
					if(Math.abs(getZ() - loc.z) > 1000 && !isFlying())
					{
						sendPacket(SystemMsg.CANNOT_SEE_TARGET);
						stopMove();
						return;
					}

					if(buildPathTo(follow.getX(), follow.getY(), follow.getZ(), _offset, follow, true, true) != null)
						movingDestTempPos.set(follow.getX(), follow.getY(), follow.getZ());
					else
					{
						stopMove();
						return;
					}
					moveNext(true);
					return;
				}

				_previousSpeed = speed;
				_startMoveTime = now;
				_moveTask = ThreadPoolManager.getInstance().schedule(this, getMoveTickInterval());
			}
			catch(Exception e)
			{
				_log.error("", e);
			}
			finally
			{
				moveLock.unlock();
			}
		}
	}

	private static final Logger _log = LoggerFactory.getLogger(Creature.class);

	public static final double HEADINGS_IN_PI = 10430.378350470452724949566316381;
	public static final int INTERACTION_DISTANCE = 200;

	private Skill _castingSkill;
	private boolean _didntSeeOn70Percent = false;
	
	private long _castInterruptTime;
	private long _animationEndTime;

	public int _scheduledCastCount;
	public int _scheduledCastInterval;

	public ScheduledFuture<?> _skillTask;
	public Future<?> _skillLaunchedTask;

	private Future<?> _stanceTask;
	private Runnable _stanceTaskRunnable;
	private long _stanceEndTime;

	public final static int CLIENT_BAR_SIZE = 352; // 352 - размер полоски CP/HP/MP в клиенте, в пикселях

	private int _lastCpBarUpdate = -1;
	private int _lastHpBarUpdate = -1;
	private int _lastMpBarUpdate = -1;

	protected double _currentCp = 0;
	protected double _currentHp = 1;
	protected double _currentMp = 1;

	protected boolean _isAttackAborted;
	protected long _attackEndTime;
	protected long _attackReuseEndTime;
	private long _lastAttackTime = -1;
	private int _poleAttackCount = 0;
	private static final double[] POLE_VAMPIRIC_MOD = { 1, 0.9, 0,7, 0.2, 0.01 };

	/** HashMap(Integer, L2Skill) containing all skills of the L2Character */
	protected final Map<Integer, Skill> _skills = new ConcurrentSkipListMap<Integer, Skill> ();
	protected Map<TriggerType, Set<TriggerInfo>> _triggers;

	protected IntObjectMap<TimeStamp> _skillReuses = new CHashIntObjectMap<TimeStamp>();

	protected volatile EffectList _effectList;

	protected volatile CharStatsChangeRecorder<? extends Creature> _statsRecorder;

	private List<Stats> _blockedStats;

	/** Map 32 bits (0x00000000) containing all abnormal effect in progress */
	private Set<AbnormalEffect> _abnormalEffects = new CopyOnWriteArraySet<AbnormalEffect>();
	private int _abnormalEffectsMask;
	private int _abnormalEffectsMask2;
	private int _abnormalEffectsMask3;

	protected AtomicBoolean isDead = new AtomicBoolean();
	protected AtomicBoolean isTeleporting = new AtomicBoolean();

	private Map<Integer, Integer> _skillMastery;

	protected boolean _isInvul;

	private boolean _fakeDeath;
	private boolean _isBlessedByNoblesse; // Восстанавливает все бафы после смерти
	private boolean _isSalvation; // Восстанавливает все бафы после смерти и полностью CP, MP, HP

	private boolean _meditated;
	private boolean _lockedTarget;

	private boolean _blocked;
	private double _enchantedDef = 1.;

	private AtomicState _afraid = new AtomicState();
	private AtomicState _muted = new AtomicState();
	private AtomicState _pmuted = new AtomicState();
	private AtomicState _amuted = new AtomicState();
	private AtomicState _paralyzed = new AtomicState();
	private AtomicState _rooted = new AtomicState();
	private AtomicState _sleeping = new AtomicState();
	private AtomicState _stunned = new AtomicState();
	private AtomicState _immobilized = new AtomicState();
	private AtomicState _confused = new AtomicState();
	private AtomicState _frozen = new AtomicState();

	private AtomicState _healBlocked = new AtomicState();
	private AtomicState _damageBlocked = new AtomicState();
	private AtomicState _buffImmunity = new AtomicState(); // Иммунитет к бафам
	private AtomicState _debuffImmunity = new AtomicState(); // Иммунитет к дебафам
	private AtomicState _effectImmunity = new AtomicState(); // Иммунитет ко всем эффектам
	protected AtomicState _deathImmunity = new AtomicState();

	private AtomicState _weaponEquipBlocked = new AtomicState();

	private boolean _flying;

	private boolean _running;

	public boolean isMoving;
	public boolean isFollow;
	public boolean isKeyboardMoving;
	private final Lock moveLock = new ReentrantLock();
	private Future<?> _moveTask;
	private MoveNextTask _moveTaskRunnable;
	private List<Location> moveList;
	private Location destination;
	/**
	 * при moveToLocation используется для хранения геокоординат в которые мы двигаемся для того что бы избежать повторного построения одного и того же пути
	 * при followToCharacter используется для хранения мировых координат в которых находилась последний раз преследуемая цель для отслеживания необходимости перестраивания пути
	 */
	private final Location movingDestTempPos = new Location();
	private int _offset;

	private boolean _forestalling;

	private volatile HardReference<? extends GameObject> target = HardReferences.emptyRef();
	private volatile HardReference<? extends Creature> castingTarget = HardReferences.emptyRef();
	private volatile HardReference<? extends Creature> followTarget = HardReferences.emptyRef();
	private volatile HardReference<? extends Creature> _aggressionTarget = HardReferences.emptyRef();

	private final List<List<Location>> _targetRecorder = new ArrayList<List<Location>>();
	private long _followTimestamp, _startMoveTime;
	private int _previousSpeed = 0;

	private final Calculator[] _calculators;

	protected CharTemplate _template;

	protected volatile CharacterAI _ai;

	protected String _name;
	protected String _title;
	protected TeamType _team = TeamType.NONE;

	private boolean _isRegenerating;
	private final Lock regenLock = new ReentrantLock();
	private Future<?> _regenTask;
	private Runnable _regenTaskRunnable;

	private List<Zone> _zones = new LazyArrayList<Zone>();
	/** Блокировка для чтения/записи объектов из региона */
	private final ReadWriteLock zonesLock = new ReentrantReadWriteLock();
	private final Lock zonesRead = zonesLock.readLock();
	private final Lock zonesWrite = zonesLock.writeLock();

	protected volatile CharListenerList listeners;

	/** Список игроков, которым необходимо отсылать информацию об изменении состояния персонажа */
	private List<Player> _statusListeners;
	private final Lock statusListenersLock = new ReentrantLock();

	protected CreatureFlags _statuses = null;

	protected Long _storedId;

	public final Long getStoredId()
	{
		return _storedId;
	}

	protected HardReference<? extends Creature> reference;

	public Creature(int objectId, CharTemplate template)
	{
		super(objectId);

		_template = template;

		_calculators = new Calculator[Stats.NUM_STATS];

		StatFunctions.addPredefinedFuncs(this);

		reference = new L2Reference<Creature>(this);

		_storedId = GameObjectsStorage.put(this);
	}

	@Override
	public HardReference<? extends Creature> getRef()
	{
		return reference;
	}

	public boolean isAttackAborted()
	{
		return _isAttackAborted;
	}

	public final void abortAttack(boolean force, boolean message)
	{
		if(isAttackingNow())
		{
			_attackEndTime = 0;
			if(force)
				_isAttackAborted = true;

			getAI().setIntention(AI_INTENTION_ACTIVE);

			if(isPlayer() && message)
			{
				sendActionFailed();
				sendPacket(new SystemMessage(SystemMessage.C1S_ATTACK_FAILED).addName(this));
			}
		}
	}

	public final void abortCast(boolean force, boolean message)
	{
		if(isCastingNow() && (force || canAbortCast()))
		{
			final Skill castingSkill = _castingSkill;
			final Future<?> skillTask = _skillTask;
			final Future<?> skillLaunchedTask = _skillLaunchedTask;

			finishFly(); // Броадкаст пакета FlyToLoc уже выполнен, устанавливаем координаты чтобы не было визуальных глюков
			clearCastVars();

			if(skillTask != null)
				skillTask.cancel(false); // cancels the skill hit scheduled task

			if(skillLaunchedTask != null)
				skillLaunchedTask.cancel(false); // cancels the skill hit scheduled task

			if(castingSkill != null)
			{
				if(castingSkill.isUsingWhileCasting())
				{
					Creature target = getAI().getCastTarget();
					if(target != null)
						target.getEffectList().stopEffect(castingSkill.getId());
				}

				removeSkillMastery(castingSkill.getId());
			}

			broadcastPacket(new MagicSkillCanceled(getObjectId())); // broadcast packet to stop animations client-side

			getAI().setIntention(AI_INTENTION_ACTIVE);

			if(isPlayer() && message)
				sendPacket(Msg.CASTING_HAS_BEEN_INTERRUPTED);
		}
	}

	public final boolean canAbortCast()
	{
		return _castInterruptTime > System.currentTimeMillis();
	}

	public boolean absorbAndReflect(Creature target, Skill skill, double damage)
	{
		if(target.isDead())
			return false;

		boolean bow = getBaseAttackType() == WeaponType.BOW || getBaseAttackType() == WeaponType.CROSSBOW;

		double value = 0;
	
		final double _finHealth = target.getCurrentCp() + target.getCurrentHp();
		
		if(skill != null && skill.isMagic())
			value = target.calcStat(Stats.REFLECT_AND_BLOCK_MSKILL_DAMAGE_CHANCE, 0, this, skill);
		else if(skill != null && skill.getCastRange() <= 200)
			value = target.calcStat(Stats.REFLECT_AND_BLOCK_PSKILL_DAMAGE_CHANCE, 0, this, skill);
		else if(skill == null && !bow)
			value = target.calcStat(Stats.REFLECT_AND_BLOCK_DAMAGE_CHANCE, 0, this, null);

		//Цель отразила весь урон
		if(value > 0 && Rnd.chance(value) && _finHealth - damage > 0)
		{
			reduceCurrentHp(damage, target, null, true, true, false, false, false, false, true);
			return true;
		}

		if(skill != null && skill.isMagic())
			value = target.calcStat(Stats.REFLECT_MSKILL_DAMAGE_PERCENT, 0, this, skill);
		else if(skill != null && skill.getCastRange() <= 200)
			value = target.calcStat(Stats.REFLECT_PSKILL_DAMAGE_PERCENT, 0, this, skill);
		else if(skill == null && !bow)
			value = target.calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0, this, null);

		if(value > 0 && _finHealth - damage > 0)
		{
			//Цель в состоянии отразить часть урона
			if(target.getCurrentHp() + target.getCurrentCp() > damage)
				reduceCurrentHp(value / 100. * damage, target, null, true, true, false, false, false, false, true);
		}

		if(skill != null || bow)
			return false;

		// вампирик
		damage = (int) (damage - target.getCurrentCp());

		if(damage <= 0)
			return false;

		final double poleMod = _poleAttackCount < POLE_VAMPIRIC_MOD.length ? POLE_VAMPIRIC_MOD[_poleAttackCount] : 0;
		double absorb = poleMod * calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0, target, null);
		double limit;
		if(absorb > 0 && !target.isDamageBlocked() && !target.isServitor() && !target.isInvul() && !target.isDeathImmune())
		{
			limit = calcStat(Stats.HP_LIMIT, null, null) * getMaxHp() / 100.;
			if (getCurrentHp() < limit)
				setCurrentHp(Math.min(_currentHp + damage * absorb * Config.ALT_ABSORB_DAMAGE_MODIFIER / 100., limit), false);
		}

		absorb = poleMod * calcStat(Stats.ABSORB_DAMAGEMP_PERCENT, 0, target, null);
		if(absorb > 0 && !target.isDamageBlocked() && !target.isServitor() && !target.isInvul() && !target.isDeathImmune())
		{
			limit = calcStat(Stats.MP_LIMIT, null, null) * getMaxMp() / 100.;
			if (getCurrentMp() < limit)
				setCurrentMp(Math.min(_currentMp + damage * absorb * Config.ALT_ABSORB_DAMAGE_MODIFIER / 100., limit));
		}

		return false;
	}

	public double absorbToEffector(Creature attacker, double damage)
	{
		if(damage == 0)
			return 0;

		double transferToEffectorDam = calcStat(Stats.TRANSFER_TO_EFFECTOR_DAMAGE_PERCENT, 0.);
		if(transferToEffectorDam > 0)
		{
			Effect effect = getEffectList().getEffectByType(EffectType.AbsorbDamageToEffector);
			if(effect == null)
				return damage;

			Creature effector = effect.getEffector();
			// на мертвого чара, не онлайн игрока - не даем абсорб, и не на самого себя
			if(effector == this || effector.isDead() || !isInRange(effector, 1200))
				return damage;

			Player thisPlayer = getPlayer();
			Player effectorPlayer = effector.getPlayer();
			if(thisPlayer != null && effectorPlayer != null)
			{
				if(thisPlayer != effectorPlayer && (!thisPlayer.isOnline() || !thisPlayer.isInParty() || thisPlayer.getParty() != effectorPlayer.getParty()))
					return damage;
			}
			else
				return damage;

			double transferDamage = (damage * transferToEffectorDam) * .01;
			//damage -= transferDamage;

			effector.reduceCurrentHp(transferDamage, effector, null, false, false, !attacker.isPlayable(), false, true, false, true);
		}
		return damage;
	}

	public double absorbToMp(Creature attacker, double damage)
	{
		if(damage == 0)
			return 0;

		double transferToMpDamPercent = calcStat(Stats.TRANSFER_TO_MP_DAMAGE_PERCENT, 0.);
		if(transferToMpDamPercent > 0)
		{
			double transferDamage = (damage * transferToMpDamPercent) * .01;

			double currentMp = getCurrentMp();
			if(currentMp > transferDamage)
			{
				setCurrentMp(getCurrentMp() - transferDamage);
				return 0;
			}
			else
			{
				if(currentMp > 0)
				{
					damage -= currentMp;
					setCurrentMp(0);
					sendPacket(SystemMsg.MP_BECAME_0_AND_THE_ARCANE_SHIELD_IS_DISAPPEARING);
				}
				getEffectList().stopEffects(EffectType.AbsorbDamageToMp);
			}

			return damage;
		}
		return damage;
	}

	public double absorbToSummon(Creature attacker, double damage)
	{
		double transferToSummonDam = calcStat(Stats.TRANSFER_TO_SUMMON_DAMAGE_PERCENT, 0.);
		if(transferToSummonDam > 0)
		{
			Servitor servitor = getServitor();
			double transferDamage = (damage * transferToSummonDam) * .01;
			if(servitor == null || servitor.isDead() || servitor.getCurrentHp() < transferDamage)
				getEffectList().stopEffects(EffectType.AbsorbDamageToSummon);
			else if(servitor.isSummon() && servitor.isInRangeZ(this, 1200))
			{
				damage -= transferDamage;

				servitor.reduceCurrentHp(transferDamage, servitor, null, false, false, false, false, true, false, true);
			}
		}
		return damage;
	}

	public void addBlockStats(List<Stats> stats)
	{
		if(_blockedStats == null)
			_blockedStats = new ArrayList<Stats>();
		_blockedStats.addAll(stats);
	}

	public Skill addSkill(Skill newSkill)
	{
		if(newSkill == null)
			return null;

		Skill oldSkill = _skills.get(newSkill.getId());

		if(newSkill.equals(oldSkill))
			return newSkill;

		// Replace oldSkill by newSkill or Add the newSkill
		_skills.put(newSkill.getId(), newSkill);

		if(oldSkill != null)
		{
			removeStatsOwner(oldSkill);
			removeTriggers(oldSkill);
			getListeners().onSkillRemove(oldSkill);
		}

		addTriggers(newSkill);

		// Add Func objects of newSkill to the calculator set of the L2Character
		addStatFuncs(newSkill.getStatFuncs());

		getListeners().onSkillAdd(newSkill, oldSkill);
		return oldSkill;
	}

	public Calculator[] getCalculators()
	{
		return _calculators;
	}

	public final void addStatFunc(Func f)
	{
		if(f == null)
			return;
		int stat = f.stat.ordinal();
		synchronized (_calculators)
		{
			if(_calculators[stat] == null)
				_calculators[stat] = new Calculator(f.stat, this);
			_calculators[stat].addFunc(f);
		}
	}

	public final void addStatFuncs(Func[] funcs)
	{
		for(Func f : funcs)
			addStatFunc(f);
	}

	public final void removeStatFunc(Func f)
	{
		if(f == null)
			return;
		int stat = f.stat.ordinal();
		synchronized (_calculators)
		{
			if(_calculators[stat] != null)
				_calculators[stat].removeFunc(f);
		}
	}

	public final void removeStatFuncs(Func[] funcs)
	{
		for(Func f : funcs)
			removeStatFunc(f);
	}

	public final void removeStatsOwner(Object owner)
	{
		synchronized (_calculators)
		{
			for(int i = 0; i < _calculators.length; i++)
				if(_calculators[i] != null)
					_calculators[i].removeOwner(owner);
		}
	}

	public void altOnMagicUseTimer(Creature aimingTarget, Skill skill)
	{
		if(isAlikeDead())
			return;
		int magicId = skill.getDisplayId();
		int level = Math.max(1, getSkillDisplayLevel(skill.getId()));
		List<Creature> targets = skill.getTargets(this, aimingTarget, true);
		broadcastPacket(new MagicSkillLaunchedPacket(getObjectId(), magicId, level, targets));
		double mpConsume2 = skill.getMpConsume2();
		if(mpConsume2 > 0)
		{
			if(_currentMp < mpConsume2)
			{
				sendPacket(Msg.NOT_ENOUGH_MP);
				return;
			}
			if(skill.isMagic())
				reduceCurrentMp(calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, aimingTarget, skill), null);
			else
				reduceCurrentMp(calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, aimingTarget, skill), null);
		}
		callSkill(skill, targets, false);
	}

	public void altUseSkill(Skill skill, Creature target)
	{
		if(skill == null)
			return;
		
		boolean disallowSkill = false;
		
		if(getPlayer() != null && getPlayer().isInPvPEvent()) 
		{
			for(String skillId : Config.EVENTS_DISALLOWED_SKILLS) 
			{
				if(skill.getId() == Integer.parseInt(skillId))
					disallowSkill = true;
				else 
					continue;
			}
		}
		
		if(disallowSkill) 
		{
			getPlayer().sendMessage(getPlayer().isLangRus() ? "Действие запрещено" : "Action is not allowed");
			return;
		}			
			
		int magicId = skill.getId();
		if(isUnActiveSkill(magicId))
			return;
		if(isSkillDisabled(skill))
		{
			sendReuseMessage(skill);
			return;
		}
		if(target == null)
		{
			target = skill.getAimingTarget(this, getTarget());
			if(target == null)
				return;
		}

		getListeners().onMagicUse(skill, target, true);

		int itemConsume[] = skill.getItemConsume();

		if(itemConsume[0] > 0 && canConsumeSkillItem(skill))
			for(int i = 0; i < itemConsume.length; i++)
				if(!consumeItem(skill.getItemConsumeId()[i], itemConsume[i]))
				{
					sendPacket(skill.isHandler() ? SystemMsg.INCORRECT_ITEM_COUNT : SystemMsg.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
					return;
				}

		if (skill.getReferenceItemId() > 0)
			if (!consumeItemMp(skill.getReferenceItemId(), skill.getReferenceItemMpConsume()))
				return;

		if(skill.getSoulsConsume() > getConsumedSouls())
		{
			sendPacket(Msg.THERE_IS_NOT_ENOUGHT_SOUL);
			return;
		}

		if(skill.getEnergyConsume() > getAgathionEnergy())
		{
			sendPacket(SystemMsg.THE_SKILL_HAS_BEEN_CANCELED_BECAUSE_YOU_HAVE_INSUFFICIENT_ENERGY);
			return;
		}

		if(skill.getSoulsConsume() > 0)
			setConsumedSouls(getConsumedSouls() - skill.getSoulsConsume(), null);
		if(skill.getEnergyConsume() > 0)
			setAgathionEnergy(getAgathionEnergy() - skill.getEnergyConsume());

		int level = Math.max(1, getSkillDisplayLevel(magicId));
		Formulas.calcSkillMastery(skill, this);
		long reuseDelay = Math.max(0, Formulas.calcSkillReuseDelay(this, skill));
		if(!skill.isToggle())
			broadcastPacket(new MagicSkillUse(this, target, skill.getDisplayId(), level, skill.getHitTime(), reuseDelay));
		// Не показывать сообщение для хербов и кубиков
		if(!skill.isHideUseMessage())
			if(skill.getSkillType() == SkillType.PET_SUMMON)
				sendPacket(new SystemMessage(SystemMessage.SUMMON_A_PET));
			else if(!skill.isHandler())
				sendPacket(new SystemMessage(SystemMessage.YOU_USE_S1).addSkillName(magicId, level));
			else
				sendPacket(new SystemMessage(SystemMessage.YOU_USE_S1).addItemName(skill.getItemConsumeId()[0]));

		if(!skill.isHandler())
			disableSkill(skill, reuseDelay);

		ThreadPoolManager.getInstance().schedule(new AltMagicUseTask(this, target, skill), skill.getHitTime());
	}

	public void sendReuseMessage(Skill skill)
	{
	}

	public void broadcastPacket(L2GameServerPacket... packets)
	{
		sendPacket(packets);
		broadcastPacketToOthers(packets);
	}

	public void broadcastPacket(List<L2GameServerPacket> packets)
	{
		sendPacket(packets);
		broadcastPacketToOthers(packets);
	}

	public void broadcastPacketToOthers(L2GameServerPacket... packets)
	{
		if(!isVisible() || packets.length == 0)
			return;

		List<Player> players = World.getAroundPlayers(this);
		Player target;
		for(int i = 0; i < players.size(); i++)
		{
			target = players.get(i);
			target.sendPacket(packets);
		}
	}

	public void broadcastPacketToOthers(List<L2GameServerPacket> packets)
	{
		if(!isVisible() || packets.isEmpty())
			return;

		List<Player> players = World.getAroundPlayers(this);
		Player target;
		for(int i = 0; i < players.size(); i++)
		{
			target = players.get(i);
			target.sendPacket(packets);
		}
	}

	public void broadcastToStatusListeners(L2GameServerPacket... packets)
	{
		if(!isVisible() || packets.length == 0)
			return;

		statusListenersLock.lock();
		try
		{
			if(_statusListeners == null || _statusListeners.isEmpty())
				return;

			Player player;
			for(int i = 0; i < _statusListeners.size(); i++)
			{
				player = _statusListeners.get(i);
				player.sendPacket(packets);
			}
		}
		finally
		{
			statusListenersLock.unlock();
		}
	}

	public void addStatusListener(Player cha)
	{
		if(cha == this)
			return;

		statusListenersLock.lock();
		try
		{
			if(_statusListeners == null)
				_statusListeners = new LazyArrayList<Player>();
			if(!_statusListeners.contains(cha))
				_statusListeners.add(cha);
		}
		finally
		{
			statusListenersLock.unlock();
		}
	}

	public void removeStatusListener(Creature cha)
	{
		statusListenersLock.lock();
		try
		{
			if(_statusListeners == null)
				return;
			_statusListeners.remove(cha);
		}
		finally
		{
			statusListenersLock.unlock();
		}
	}

	public void clearStatusListeners()
	{
		statusListenersLock.lock();
		try
		{
			if(_statusListeners == null)
				return;
			_statusListeners.clear();
		}
		finally
		{
			statusListenersLock.unlock();
		}
	}

	public StatusUpdatePacket makeStatusUpdate(int... fields)
	{
		StatusUpdatePacket su = new StatusUpdatePacket(getObjectId());
		for(int field : fields)
			switch(field)
			{
				case StatusUpdatePacket.CUR_HP:
					su.addAttribute(field, (int) getCurrentHp());
					break;
				case StatusUpdatePacket.MAX_HP:
					su.addAttribute(field, getMaxHp());
					break;
				case StatusUpdatePacket.CUR_MP:
					su.addAttribute(field, (int) getCurrentMp());
					break;
				case StatusUpdatePacket.MAX_MP:
					su.addAttribute(field, getMaxMp());
					break;
				case StatusUpdatePacket.KARMA:
					su.addAttribute(field, getKarma());
					break;
				case StatusUpdatePacket.CUR_CP:
					su.addAttribute(field, (int) getCurrentCp());
					break;
				case StatusUpdatePacket.MAX_CP:
					su.addAttribute(field, getMaxCp());
					break;
				case StatusUpdatePacket.PVP_FLAG:
					su.addAttribute(field, getPvpFlag());
					break;
			}
		return su;
	}

	public void broadcastStatusUpdate()
	{
		if(!needStatusUpdate())
			return;

		StatusUpdatePacket su = makeStatusUpdate(StatusUpdatePacket.MAX_HP, StatusUpdatePacket.MAX_MP, StatusUpdatePacket.CUR_HP, StatusUpdatePacket.CUR_MP);
		broadcastToStatusListeners(su);
	}

	public int calcHeading(int x_dest, int y_dest)
	{
		return (int) (Math.atan2(getY() - y_dest, getX() - x_dest) * HEADINGS_IN_PI) + 32768;
	}

	public final double calcStat(Stats stat, double init)
	{
		return calcStat(stat, init, null, null);
	}

	public final double calcStat(Stats stat, double init, Creature target, Skill skill)
	{
		int id = stat.ordinal();
		Calculator c = _calculators[id];
		if(c == null)
			return init;
		Env env = new Env();
		env.character = this;
		//env.target = target;
		
		if(skill != null && skill.getId() == 1557)
			env.target = (Creature)getPlayer().getServitor();
		else	
			env.target = target;
			
		env.skill = skill;
		env.value = init;
		c.calc(env);
		return env.value;
	}

	public final double calcStat(Stats stat, Creature target, Skill skill)
	{

		if(skill != null && skill.getId() == 1557)
			target = (Creature)this.getPlayer().getServitor();

		Env env = new Env(this, target, skill);
		if(stat == null)
			System.out.println("FIX ME FAST(!) My name IS "+this.getName()+" and I'm a player?("+this.isPlayer()+") my target is "+target.getName()+" skill:"+skill.getName()+"");
		env.value = stat.getInit();
		int id = stat.ordinal();
		Calculator c = _calculators[id];
		if(c != null)
			c.calc(env);
		return env.value;
	}

	/**
	 * Return the Attack Speed of the L2Character (delay (in milliseconds) before next attack).
	 */
	public int calculateAttackDelay()
	{
		return Formulas.calcPAtkSpd(getPAtkSpd());
	}

	public void callSkill(Skill skill, List<Creature> targets, boolean useActionSkills)
	{
		try
		{
			if(useActionSkills && !skill.isUsingWhileCasting() && _triggers != null) {
				if (skill.isOffensive()) {
					useTriggers(getTarget(), TriggerType.OFFENSIVE_SKILL_USE, null, skill, 0);

					if (skill.isMagic())
						useTriggers(getTarget(), TriggerType.OFFENSIVE_MAGICAL_SKILL_USE, null, skill, 0);
					else
						useTriggers(getTarget(), TriggerType.OFFENSIVE_PHYSICAL_SKILL_USE, null, skill, 0);
				} else {
					// для АоЕ, пати/клан бафов и селфов триггер накладывается на кастера
					final boolean targetSelf = skill.isAoE() || skill.isNotTargetAoE() || skill.getTargetType() == Skill.SkillTargetType.TARGET_SELF;

					useTriggers(targetSelf ? this : getTarget(), TriggerType.SUPPORT_SKILL_USE, null, skill, 0);

					if (skill.isMagic())
						useTriggers(targetSelf ? this : getTarget(), TriggerType.SUPPORT_MAGICAL_SKILL_USE, null, skill, 0);
					else
						useTriggers(targetSelf ? this : getTarget(), TriggerType.SUPPORT_PHYSICAL_SKILL_USE, null, skill, 0);
				}
			}

			Player pl = getPlayer();
			Creature target;
			Iterator<Creature> itr = targets.iterator();
			while(itr.hasNext())
			{
				target = itr.next();

				//Фильтруем неуязвимые цели
				if(skill.isOffensive() && target.isInvul())
				{
					Player pcTarget = target.getPlayer();
					if((!skill.isIgnoreInvul() || pcTarget != null && pcTarget.isGM()) && !target.isArtefact())
					{
						itr.remove();
						continue;
					}
				}
				//Рассчитываем игрорируемые скилы из спец.эффекта
				Effect ie = target.getEffectList().getEffectByType(EffectType.IgnoreSkill);
				if(ie != null)
					if(ArrayUtils.contains(ie.getTemplate().getParam().getIntegerArray("skillId"), skill.getId()))
					{
						itr.remove();
						continue;
					}

				target.getListeners().onMagicHit(skill, this);

				if(pl != null)
					if(target != null && target.isNpc())
					{
						NpcInstance npc = (NpcInstance) target;
						List<QuestState> ql = pl.getQuestsForEvent(npc, QuestEventType.MOB_TARGETED_BY_SKILL);
						if(ql != null)
							for(QuestState qs : ql)
								qs.getQuest().notifySkillUse(npc, skill, qs);
					}

				if(skill.getNegateSkill() > 0)
					for(Effect e : target.getEffectList().getAllEffects())
					{
						Skill efs = e.getSkill();
						if(efs.getId() == skill.getNegateSkill() && e.isCancelable() && (skill.getNegatePower() <= 0 || efs.getPower() <= skill.getNegatePower()))
							e.exit();
					}

				if(skill.getCancelTarget() > 0)
					if(Rnd.chance(skill.getCancelTarget()))
						if((target.getCastingSkill() == null || !(target.getCastingSkill().getSkillType() == SkillType.TAKECASTLE || target.getCastingSkill().getSkillType() == SkillType.TAKEFORTRESS || target.getCastingSkill().getSkillType() == SkillType.TAKEFLAG)) && !target.isRaid())
						{
							target.abortAttack(true, true);
							target.abortCast(true, true);
							target.setTarget(null);
						}
			}

			if(skill.isOffensive())
				startAttackStanceTask();

			// Применяем селфэффекты на кастера
			// Особое условие для атакующих аура-скиллов (Vengeance 368):
			// если ни одна цель не задета то селфэффекты не накладываются
			if (!(skill.isNotTargetAoE() && skill.isOffensive()&& targets.size() == 0))
			{
				skill.useInstantEffects(this, this, false, true);
				skill.getEffects(this, this, false, true);
			}

			skill.useSkill(this, targets);
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
	}

	public void useTriggers(GameObject target, TriggerType type, Skill ex, Skill owner, double damage)
	{
		useTriggers(target, null, type, ex, owner, owner, damage);
	}

	public void useTriggers(GameObject target, List<Creature> targets, TriggerType type, Skill ex, Skill owner, double damage)
	{
		useTriggers(target, targets, type, ex, owner, owner, damage);
	}

	public void useTriggers(GameObject target, TriggerType type, Skill ex, Skill owner, StatTemplate triggersOwner, double damage)
	{
		useTriggers(target, null, type, ex, owner, triggersOwner, damage);
	}

	public void useTriggers(GameObject target, List<Creature> targets, TriggerType type, Skill ex, Skill owner, StatTemplate triggersOwner, double damage)
	{
		Set<TriggerInfo> triggers = null;
		switch(type)
		{
			//case ON_START_CAST:
			//case ON_END_CAST:
			//case ON_FINISH_CAST:
			case ON_START_EFFECT:
			case ON_EXIT_EFFECT:
			case ON_FINISH_EFFECT:
			//case ON_REVIVE:
				if(triggersOwner != null)
				{
					triggers = new CopyOnWriteArraySet<TriggerInfo>();
					for(TriggerInfo t : triggersOwner.getTriggerList())
					{
						if(t.getType() == type)
							triggers.add(t);
					}
				}
				break;
			default:
				if(_triggers != null)
					triggers = _triggers.get(type);
				break;
		}

		if(triggers != null && !triggers.isEmpty())
		{
			for(TriggerInfo t : triggers)
			{
				if(t.getSkill() != ex)
					useTriggerSkill(target == null ? getTarget() : target, targets, t, owner, damage);
			}
		}
	}

	public void useTriggerSkill(GameObject target, List<Creature> targets, TriggerInfo trigger, Skill owner, double damage)
	{
		Skill skill = trigger.getSkill();
		if(skill.getReuseDelay() > 0 && isSkillDisabled(skill))
			return;

		Creature aimTarget = skill.getAimingTarget(this, target);
		// DS: Для шансовых скиллов с TARGET_SELF и условием "пвп" сам кастер будет являться aimTarget,
		// поэтому в условиях для триггера проверяем реальную цель.
		Creature realTarget = target != null && target.isCreature() ? (Creature)target : null;
		if(Rnd.chance(trigger.getChance()) && trigger.checkCondition(this, realTarget, aimTarget, owner, damage) && skill.checkCondition(this, aimTarget, true, true, true, true))
		{
			if(targets == null)
				targets = skill.getTargets(this, aimTarget, false);

			int displayId = 0, displayLevel = 0;

			if(skill.hasEffects())
			{
				displayId = skill.getEffectTemplates()[0]._displayId;
				displayLevel = skill.getEffectTemplates()[0]._displayLevel;
			}

			if(displayId == 0)
				displayId = skill.getDisplayId();
			if(displayLevel == 0)
				displayLevel = skill.getDisplayLevel();

			for(Creature cha : targets)
				broadcastPacket(new MagicSkillUse(this, cha, displayId, displayLevel, 0, 0));

			Formulas.calcSkillMastery(skill, this);
			callSkill(skill, targets, false);
			disableSkill(skill, skill.getReuseDelay());
		}
	}

	public boolean checkBlockedStat(Stats stat)
	{
		return _blockedStats != null && _blockedStats.contains(stat);
	}

	public boolean checkReflectSkill(Creature attacker, Skill skill)
	{
		if(!skill.isReflectable())
			return false;
		// Не отражаем, если есть неуязвимость, иначе она может отмениться
		if(isInvul() || attacker.isInvul() || !skill.isOffensive())
			return false;
		// Из магических скилов отражаются только скилы наносящие урон по ХП.
		if(skill.isMagic() && skill.getSkillType() != SkillType.MDAM)
			return false;
		if(Rnd.chance(calcStat(skill.isMagic() ? Stats.REFLECT_MAGIC_SKILL : Stats.REFLECT_PHYSIC_SKILL, 0, attacker, skill)))
		{
			sendPacket(new SystemMessage(SystemMessage.YOU_COUNTERED_C1S_ATTACK).addName(attacker));
			attacker.sendPacket(new SystemMessage(SystemMessage.C1_DODGES_THE_ATTACK).addName(this));
			return true;
		}
		return false;
	}

	public void doCounterAttack(Skill skill, Creature attacker, boolean blow)
	{
		if(isDead()) // если персонаж уже мертв, контратаки быть не должно
			return;
		if(isDamageBlocked() || attacker.isDamageBlocked()) // Не контратакуем, если есть неуязвимость, иначе она может отмениться
			return;
		if(skill == null || skill.hasEffects() || skill.isMagic() || !skill.isOffensive() || skill.getCastRange() > 200)
			return;
		final int numberOfAttacks = skill.getNumberOfCounterAttacks();
		if(numberOfAttacks <= 0)
			return;		
		if(Rnd.chance(calcStat(Stats.COUNTER_ATTACK, 0, attacker, skill)))
		{
			double damage = 1189 * getPAtk(attacker) / Math.max(attacker.getPDef(this), 1);
			attacker.sendPacket(new SystemMessage(SystemMessage.C1S_IS_PERFORMING_A_COUNTERATTACK).addName(this));
			sendPacket(new SystemMessage(SystemMessage.C1S_IS_PERFORMING_A_COUNTERATTACK).addName(this));
			if(numberOfAttacks > 1)
				for(int i = 0 ; i < skill.getNumberOfCounterAttacks(); i++)
					attacker.reduceCurrentHp(damage, this, skill, true, true, false, false, false, false, true);
			else
				attacker.reduceCurrentHp(damage, this, skill, true, true, false, false, false, false, true);
			sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_DONE_S3_POINTS_OF_DAMAGE_TO_C2).addName(this).addName(attacker).addInteger((int) damage).addHpChange(getObjectId(), attacker.getObjectId(), (int) -damage));
		}
	}

	/**
	 * Disable this skill id for the duration of the delay in milliseconds.
	 *
	 * @param skill
	 * @param delay (seconds * 1000)
	 */
	public void disableSkill(Skill skill, long delay)
	{
		_skillReuses.put(skill.getReuseHash(), new TimeStamp(skill, delay));
	}

	public abstract boolean isAutoAttackable(Creature attacker);

	public void doAttack(Creature target)
	{
		if(target == null || isAMuted() || isAttackingNow() || isAlikeDead() || target.isAlikeDead() || !isInRange(target, 2000) || isPlayer() && getPlayer().isInMountTransform())
			return;

		// Если цель в инвизе, то атака не должна продолжаться.
		if(target.isInvisible(this) && getAI() instanceof DefaultAI)
		{
			getAI().notifyEvent(CtrlEvent.EVT_THINK);
			return;
		}

		getListeners().onAttack(target);

		// Get the Attack Speed of the L2Character (delay (in milliseconds) before next attack)
		int sAtk = calculateAttackDelay();
		int ssGrade = 0;
		int attackReuseDelay = 0;
		boolean ssEnabled = false;

		if(isNpc())
		{
			attackReuseDelay = getTemplate().getBaseAtkRange() > 100 ? 1500 : 0; // TODO: Вынести в датапак.
			NpcTemplate.ShotsType shotType = ((NpcTemplate) getTemplate()).getShots();
			if(shotType != NpcTemplate.ShotsType.NONE && shotType != NpcTemplate.ShotsType.BSPIRIT && shotType != NpcTemplate.ShotsType.SPIRIT)
				ssEnabled = true;
		}
		else
		{
			WeaponTemplate weaponItem = getActiveWeaponTemplate();
			if(weaponItem != null)
			{
				attackReuseDelay = weaponItem.getAttackReuseDelay();
				ssGrade = weaponItem.getCrystalType().externalOrdinal;
			}
			ssEnabled = getChargedSoulShot();
		}

		if(attackReuseDelay > 0)
		{
			int reuse = (int) (attackReuseDelay * getReuseModifier(target) * 666 * calcStat(Stats.ATK_BASE, getTemplate().getBasePAtkSpd(), target, null) / 293. / getPAtkSpd());
			if(reuse > 0)
			{
				sendPacket(new SetupGaugePacket(this, SetupGaugePacket.RED, reuse));
				_attackReuseEndTime = reuse + System.currentTimeMillis() - 75;
				if(reuse > sAtk)
					ThreadPoolManager.getInstance().schedule(new NotifyAITask(this, CtrlEvent.EVT_READY_TO_ACT, null, null), reuse);
			}
		}

		// DS: скорректировано на 1/100 секунды поскольку AI task вызывается с небольшой погрешностью
		// особенно на слабых машинах и происходит обрыв автоатаки по isAttackingNow() == true
		_attackEndTime = sAtk + System.currentTimeMillis() - 10;
		_isAttackAborted = false;
		_lastAttackTime = System.currentTimeMillis();

		AttackPacket attack = new AttackPacket(this, target, ssEnabled, ssGrade);
		
		setHeading(PositionUtils.calculateHeadingFrom(this, target), true);

		switch(getBaseAttackType())
		{
			case BOW:
			case CROSSBOW:
				doAttackHitByBow(attack, target, sAtk);
				break;
			case POLE:
				doAttackHitByPole(attack, target, sAtk);
				break;
			case DUAL:
			case DUALFIST:
			case DUALDAGGER:
				doAttackHitByDual(attack, target, sAtk);
				break;
			default:
				doAttackHitSimple(attack, target, 1., true, sAtk, true);
		}

		if(attack.hasHits())
			broadcastPacket(attack);
	}

	private void doAttackHitSimple(AttackPacket attack, Creature target, double multiplier, boolean unchargeSS, int sAtk, boolean notify)
	{
		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;
		boolean miss1 = Formulas.calcHitMiss(this, target);

		if(!miss1)
		{
			AttackInfo info = Formulas.calcPhysDam(this, target, null, false, false, attack._soulshot, false, false);
			damage1 = (int) (info.damage * multiplier);
			shld1 = info.shld;
			crit1 = info.crit;
		}

		ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage1, crit1, miss1, attack._soulshot, shld1, unchargeSS, notify, sAtk), sAtk / 2);

		attack.addHit(target, damage1, miss1, crit1, shld1);
	}

	private void doAttackHitByBow(AttackPacket attack, Creature target, int sAtk)
	{
		WeaponTemplate activeWeapon = getActiveWeaponTemplate();
		if(activeWeapon == null)
			return;

		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;

		// Calculate if hit is missed or not
		boolean miss1 = Formulas.calcHitMiss(this, target);

		reduceArrowCount();

		if(!miss1)
		{
			AttackInfo info = Formulas.calcPhysDam(this, target, null, false, false, attack._soulshot, false, true);
			damage1 = (int) info.damage;
			shld1 = info.shld;
			crit1 = info.crit;

			int range = activeWeapon.getAttackRange();
			damage1 *= Math.min(range, getDistance(target)) / range * .4 + 0.8; // разброс 20% в обе стороны
		}

		ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage1, crit1, miss1, attack._soulshot, shld1, true, true, sAtk), sAtk / 2);

		attack.addHit(target, damage1, miss1, crit1, shld1);
	}

	private void doAttackHitByDual(AttackPacket attack, Creature target, int sAtk)
	{
		int damage1 = 0;
		int damage2 = 0;
		boolean shld1 = false;
		boolean shld2 = false;
		boolean crit1 = false;
		boolean crit2 = false;

		boolean miss1 = Formulas.calcHitMiss(this, target);
		boolean miss2 = Formulas.calcHitMiss(this, target);

		if(!miss1)
		{
			AttackInfo info = Formulas.calcPhysDam(this, target, null, true, false, attack._soulshot, false, false);
			damage1 = (int) info.damage;
			shld1 = info.shld;
			crit1 = info.crit;
		}

		if(!miss2)
		{
			AttackInfo info = Formulas.calcPhysDam(this, target, null, true, false, attack._soulshot, false, false);
			damage2 = (int) info.damage;
			shld2 = info.shld;
			crit2 = info.crit;
		}

		// Create a new hit task with Medium priority for hit 1 and for hit 2 with a higher delay
		ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage1, crit1, miss1, attack._soulshot, shld1, true, false, sAtk / 2), sAtk / 4);
		ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage2, crit2, miss2, attack._soulshot, shld2, false, true, sAtk), sAtk / 2);

		attack.addHit(target, damage1, miss1, crit1, shld1);
		attack.addHit(target, damage2, miss2, crit2, shld2);
	}

	private void doAttackHitByPole(AttackPacket attack, Creature target, int sAtk)
	{
		int angle = (int) calcStat(Stats.POLE_ATTACK_ANGLE, 120, target, null); // TODO: Вынести в датапак.
		int range = (int) calcStat(Stats.POWER_ATTACK_RANGE, getTemplate().getBaseAtkRange(), target, null);

		// Используем Math.round т.к. обычный кастинг обрезает к меньшему
		// double d = 2.95. int i = (int)d, выйдет что i = 2
		// если 1% угла или 1 дистанции не играет огромной роли, то для
		// количества целей это критично
		int attackcountmax = (int) Math.round(calcStat(Stats.POLE_TARGET_COUNT, 0, target, null));

		if(isBoss())
			attackcountmax += 27;
		else if(isRaid())
			attackcountmax += 12;
		else if(isMonster() && getLevel() > 0)
			attackcountmax += getLevel() / 7.5;

		double mult = 1.;
		_poleAttackCount = 1;

		if(!isInZonePeace())// Гварды с пикой, будут атаковать только одиночные цели в городе
			for(Creature t : getAroundCharacters(range, 200))
				if(_poleAttackCount <= attackcountmax)
				{
					if(t == target || t.isDead() || !PositionUtils.isFacing(this, t, angle))
						continue;

					if(t.isAutoAttackable(this))
					{
						doAttackHitSimple(attack, t, mult, false, sAtk, false);
						mult *= Config.ALT_POLE_DAMAGE_MODIFIER;
						_poleAttackCount++;
					}
				}
				else
					break;

		_poleAttackCount = 0;
		doAttackHitSimple(attack, target, 1., true, sAtk, true);
	}

	public long getAnimationEndTime()
	{
		return _animationEndTime;
	}

	public void doCast(Skill skill, Creature target, boolean forceUse)
	{
		if(skill == null)
			return;

		int itemConsume[] = skill.getItemConsume();

		if(itemConsume[0] > 0 && canConsumeSkillItem(skill))
			for(int i = 0; i < itemConsume.length; i++)
				if(!consumeItem(skill.getItemConsumeId()[i], itemConsume[i]))
				{
					sendPacket(skill.isHandler() ? SystemMsg.INCORRECT_ITEM_COUNT : SystemMsg.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
					return;
				}

		if (skill.getReferenceItemId() > 0)
			if (!consumeItemMp(skill.getReferenceItemId(), skill.getReferenceItemMpConsume()))
				return;

		int magicId = skill.getId();

		if(target == null)
			target = skill.getAimingTarget(this, getTarget());
		if(target == null)
			return;

		getListeners().onMagicUse(skill, target, false);

		if(this != target)
			setHeading(PositionUtils.calculateHeadingFrom(this, target), true);

		int level = Math.max(1, getSkillDisplayLevel(magicId));

		int skillTime = skill.isSkillTimePermanent() ? skill.getHitTime() : Formulas.calcMAtkSpd(this, skill, skill.getHitTime());
		int skillInterruptTime = skill.isMagic() ? Formulas.calcMAtkSpd(this, skill, skill.getSkillInterruptTime()) : 0;

		int minCastTime = Math.min(Config.SKILLS_CAST_TIME_MIN, skill.getHitTime());
		if(skillTime < minCastTime)
		{
			skillTime = minCastTime;
			skillInterruptTime = 0;
		}

		_animationEndTime = System.currentTimeMillis() + skillTime;

		if(skill.isMagic() && !skill.isSkillTimePermanent() && getChargedSpiritShot() > 0)
		{
			skillTime = (int) (0.70 * skillTime);
			skillInterruptTime = (int) (0.70 * skillInterruptTime);
		}

		Formulas.calcSkillMastery(skill, this); // Calculate skill mastery for current cast
		long reuseDelay = Math.max(0, Formulas.calcSkillReuseDelay(this, skill));

		broadcastPacket(new MagicSkillUse(this, target, skill.getDisplayId(), level, skillTime, reuseDelay));

        if (this.isNpc()) 
		{
            if (getChargedSpiritShot() > 0) 
			{
                int ssGrade = 0;
                int[] _ssskills = {2061, 2155, 2156, 2157, 2158, 2159};
                int[] _bsskills = {2061, 2160, 2161, 2162, 2163, 2164};
                broadcastPacket(new MagicSkillUse(this, this, _ssskills[ssGrade], 1, 0, 0));
            }
        }
		
		if(!skill.isHandler())
			disableSkill(skill, reuseDelay);

		if(isPlayer())
			if(skill.getSkillType() == SkillType.PET_SUMMON)
				sendPacket(Msg.SUMMON_A_PET);
			else if(!skill.isHandler())
				sendPacket(new SystemMessage(SystemMessage.YOU_USE_S1).addSkillName(magicId, level));
			else
				sendPacket(new SystemMessage(SystemMessage.YOU_USE_S1).addItemName(skill.getItemConsumeId()[0]));

		if(skill.getTargetType() == SkillTargetType.TARGET_HOLY)
			target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this, 1);

		double mpConsume1 = skill.isUsingWhileCasting() ? skill.getMpConsume() : skill.getMpConsume1();
		if(mpConsume1 > 0)
		{
			if(_currentMp < mpConsume1)
			{
				sendPacket(Msg.NOT_ENOUGH_MP);
				onCastEndTime(null, false);
				return;
			}
			reduceCurrentMp(mpConsume1, null);
		}

		_flyLoc = null;
		switch (skill.getFlyType())
		{
			case DUMMY:
			case CHARGE:
				Location flyLoc = getFlyLocation(target, skill);
				if(flyLoc != null)
				{
					_flyLoc = flyLoc;
					broadcastPacket(new FlyToLocationPacket(this, flyLoc, skill.getFlyType()));
				}
				else
				{
					sendPacket(SystemMsg.CANNOT_SEE_TARGET);
					return;
				}
		}

		_castingSkill = skill;
		_castInterruptTime = System.currentTimeMillis() + skillInterruptTime;
		setCastingTarget(target);

		if(skill.isUsingWhileCasting())
			callSkill(skill, skill.getTargets(this, target, forceUse), true);

		if(isPlayer())
			sendPacket(new SetupGaugePacket(this, SetupGaugePacket.BLUE, skillTime));

		_scheduledCastCount = skill.getCastCount();
		_scheduledCastInterval = skill.getCastCount() > 0 ? skillTime / _scheduledCastCount : skillTime;

		// Create a task MagicUseTask with Medium priority to launch the MagicSkill at the end of the casting time
		_skillLaunchedTask = ThreadPoolManager.getInstance().schedule(new MagicLaunchedTask(this, forceUse), skillInterruptTime);
		_skillTask = ThreadPoolManager.getInstance().schedule(new MagicUseTask(this, forceUse), skill.getCastCount() > 0 ? skillTime / skill.getCastCount() : skillTime);
		ThreadPoolManager.getInstance().schedule(new SeventeethPecentTimer(this), skill.getCastCount() > 0 ? (long)(skillTime / skill.getCastCount() * 0.66) : (long)(skillTime * 0.66));
		
	}
	private Location _flyLoc;

	public Location getFlyLocation(GameObject target, Skill skill)
	{
		if(target != null && target != this)
		{
			Location loc;

			double radian = PositionUtils.convertHeadingToRadian(target.getHeading());
			if(skill.isFlyToBack())
				loc = new Location(target.getX() + (int) (Math.sin(radian) * 40), target.getY() - (int) (Math.cos(radian) * 40), target.getZ());
			else
				loc = new Location(target.getX() - (int) (Math.sin(radian) * 40), target.getY() + (int) (Math.cos(radian) * 40), target.getZ());

			if(isFlying())
			{
				if(isPlayer() && ((Player) this).isInFlyingTransform() && (loc.z <= 0 || loc.z >= 6000))
					return null;
				if(GeoEngine.moveCheckInAir(this, loc.x, loc.y, loc.z) == null)
					return null;
			}
			else
			{
				loc.correctGeoZ(getGeoIndex());

				if(!GeoEngine.canMoveToCoord(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getGeoIndex()))
				{
					loc = target.getLoc(); // Если не получается встать рядом с объектом, пробуем встать прямо в него
					if(!GeoEngine.canMoveToCoord(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getGeoIndex()))
						return null;
				}
			}

			return loc;
		}

		double radian = PositionUtils.convertHeadingToRadian(getHeading());
		int x1 = -(int) (Math.sin(radian) * skill.getFlyRadius());
		int y1 = (int) (Math.cos(radian) * skill.getFlyRadius());

		if(isFlying())
			return GeoEngine.moveCheckInAir(this, getX() + x1, getY() + y1, getZ());
		return GeoEngine.moveCheck(getX(), getY(), getZ(), getX() + x1, getY() + y1, getGeoIndex());
	}
	
	@Override
	public void doDie(Creature killer)
	{
		// killing is only possible one time
		if(!isDead.compareAndSet(false, true))
			return;

		onDeath(killer);
	}

	protected void onDeath(Creature killer)
	{
		if(killer != null)
		{
			Player killerPlayer = killer.getPlayer();
			if(killerPlayer != null)
				killerPlayer.getListeners().onKillIgnorePetOrSummon(this);

			killer.getListeners().onKill(this);

			if(isPlayer() && killer.isPlayable())
				_currentCp = 0;
		}

		setTarget(null);
		stopMove();
		stopAttackStanceTask();
		stopRegeneration();

		_currentHp = 0;

		if(isPlayable())
		{
			boolean fightClubKeepBuffs = isPlayable() && getPlayer().isInFightClub() && !getPlayer().getFightClubEvent().loseBuffsOnDeath(getPlayer());

			// Stop all active skills effects in progress on the L2Character
			if(isBlessedByNoblesse() || isSalvation() || fightClubKeepBuffs)
			{
				if(isSalvation() && isPlayer() && !getPlayer().isInOlympiadMode() && getPlayer().getLfcGame() == null)
					getPlayer().reviveRequest(getPlayer(), 100, false);
				for(Effect e : getEffectList().getAllEffects())
					// Noblesse Blessing Buff/debuff effects are retained after
					// death. However, Noblesse Blessing and Lucky Charm are lost as normal.
					if(e.getEffectType() == EffectType.BlessNoblesse || e.getSkill().getId() == Skill.SKILL_FORTUNE_OF_NOBLESSE ||
							e.getSkill().getId() == Skill.SKILL_RAID_BLESSING )
						e.exit();
					else if(e.getEffectType() == EffectType.AgathionResurrect)
					{
						if(isPlayer())
							getPlayer().setAgathionRes(true);
						e.exit();
					}
			}
			else
			{
				for(Effect e : getEffectList().getAllEffects())
				{
					// Некоторые эффекты сохраняются при смерти
					if(e.getEffectType() != EffectType.Transformation && !e.getSkill().isPreservedOnDeath())
						e.exit();
				}
			}
		}

		if(killer != null && killer.isPlayable() && killer.getPlayer().isInFightClub())
			killer.getPlayer().getFightClubEvent().onKilled(killer, this);
		else if(isPlayable() && getPlayer().isInFightClub())
			getPlayer().getFightClubEvent().onKilled(killer, this);

		ThreadPoolManager.getInstance().execute(new NotifyAITask(this, CtrlEvent.EVT_DEAD, killer, null));

		getListeners().onDeath(killer);

		updateEffectIcons();
		updateStats();
		broadcastStatusUpdate();
	}

	protected void onRevive()
	{

	}

	public void enableSkill(Skill skill)
	{
		_skillReuses.remove(skill.getReuseHash());
	}

	/**
	 * Return a map of 32 bits (0x00000000) containing all abnormal effects
	 */
	public Set<AbnormalEffect> getAbnormalEffects()
	{
		return _abnormalEffects;
	}

	public AbnormalEffect[] getAbnormalEffectsArray()
	{
		return _abnormalEffects.toArray(new AbnormalEffect[_abnormalEffects.size()]);
	}

	public int getAbnormalEffectMask()
	{
		return _abnormalEffectsMask;
	}

	/**
	 * Return a map of 32 bits (0x00000000) containing all special effects
	 */
	public int getAbnormalEffectMask2()
	{
		return _abnormalEffectsMask2;
	}

	/**
	 * Return a map of 32 bits (0x00000000) containing all event effects
	 */
	public int getAbnormalEffectMask3()
	{
		return _abnormalEffectsMask3;
	}

	public int getAccuracy()
	{
		return (int) calcStat(Stats.ACCURACY_COMBAT, 0, null, null);
	}

	/**
	 * Возвращает коллекцию скиллов для быстрого перебора
	 */
	public Collection<Skill> getAllSkills()
	{
		return _skills.values();
	}

	/**
	 * Возвращает массив скиллов для безопасного перебора
	 */
	public final Skill[] getAllSkillsArray()
	{
		Collection<Skill> vals = _skills.values();
		return vals.toArray(new Skill[vals.size()]);
	}

	public final double getAttackSpeedMultiplier()
	{
		return 1.1 * getPAtkSpd() / getTemplate().getBasePAtkSpd();
	}

	public int getBuffLimit()
	{
		return (int) calcStat(Stats.BUFF_LIMIT, Config.ALT_BUFF_LIMIT, null, null);
	}

	public Skill getCastingSkill()
	{
		return _castingSkill;
	}

	/**
	 * Возвращает шанс физического крита (1000 == 100%)
	 */
	public int getCriticalHit(Creature target, Skill skill)
	{
		return (int) Math.round(calcStat(Stats.CRITICAL_BASE, _template.getBasePCritRate(), target, skill));
	}

	/**
	 * Возвращает шанс магического крита в процентах
	 */
	public double getMagicCriticalRate(Creature target, Skill skill)
	{
		return calcStat(Stats.MCRITICAL_RATE, target, skill);
	}

	/**
	 * Return the current CP of the L2Character.
	 *
	 */
	public final double getCurrentCp()
	{
		return _currentCp;
	}

	public final double getCurrentCpRatio()
	{
		return getCurrentCp() / getMaxCp();
	}

	public final double getCurrentCpPercents()
	{
		return getCurrentCpRatio() * 100.;
	}

	public final boolean isCurrentCpFull()
	{
		return getCurrentCp() >= getMaxCp();
	}

	public final boolean isCurrentCpZero()
	{
		return getCurrentCp() < 1;
	}

	public final double getCurrentHp()
	{
		return _currentHp;
	}

	public final double getCurrentHpRatio()
	{
		return getCurrentHp() / getMaxHp();
	}

	public final double getCurrentHpPercents()
	{
		return getCurrentHpRatio() * 100.;
	}

	public final boolean isCurrentHpFull()
	{
		return getCurrentHp() >= getMaxHp();
	}

	public final boolean isCurrentHpZero()
	{
		return getCurrentHp() < 1;
	}

	public final double getCurrentMp()
	{
		return _currentMp;
	}

	public final double getCurrentMpRatio()
	{
		return getCurrentMp() / getMaxMp();
	}

	public final double getCurrentMpPercents()
	{
		return getCurrentMpRatio() * 100.;
	}

	public final boolean isCurrentMpFull()
	{
		return getCurrentMp() >= getMaxMp();
	}

	public final boolean isCurrentMpZero()
	{
		return getCurrentMp() < 1;
	}

	public Location getDestination()
	{
		return destination;
	}

	public int getINT()
	{
		return (int) calcStat(Stats.STAT_INT, _template.getBaseAttr().getINT(), null, null);
	}

	public int getSTR()
	{
		return (int) calcStat(Stats.STAT_STR, _template.getBaseAttr().getSTR(), null, null);
	}

	public int getCON()
	{
		return (int) calcStat(Stats.STAT_CON, _template.getBaseAttr().getCON(), null, null);
	}

	public int getMEN()
	{
		return (int) calcStat(Stats.STAT_MEN, _template.getBaseAttr().getMEN(), null, null);
	}

	public int getDEX()
	{
		return (int) calcStat(Stats.STAT_DEX, _template.getBaseAttr().getDEX(), null, null);
	}

	public int getWIT()
	{
		return (int) calcStat(Stats.STAT_WIT, _template.getBaseAttr().getWIT(), null, null);
	}

	public int getEvasionRate(Creature target)
	{
		return (int) calcStat(Stats.EVASION_RATE, 0, target, null);
	}

	public List<Creature> getAroundCharacters(int radius, int height)
	{
		if(!isVisible())
			return Collections.emptyList();
		return World.getAroundCharacters(this, radius, height);
	}

	public List<NpcInstance> getAroundNpc(int range, int height)
	{
		if(!isVisible())
			return Collections.emptyList();
		return World.getAroundNpc(this, range, height);
	}

	public boolean knowsObject(GameObject obj)
	{
		return World.getAroundObjectById(this, obj.getObjectId()) != null;
	}

	public final Skill getKnownSkill(int skillId)
	{
		return _skills.get(skillId);
	}

	public final int getMagicalAttackRange(Skill skill)
	{
		if(skill != null)
			return (int) calcStat(Stats.MAGIC_ATTACK_RANGE, skill.getCastRange(), null, skill);
		return getTemplate().getBaseAtkRange();
	}

	public int getMAtk(Creature target, Skill skill)
	{
		if(skill != null && skill.getMatak() > 0)
			return skill.getMatak();
		return (int) calcStat(Stats.MAGIC_ATTACK, _template.getBaseMAtk(), target, skill);
	}

	public int getMAtkSpd()
	{
		return (int) (calcStat(Stats.MAGIC_ATTACK_SPEED, _template.getBaseMAtkSpd(), null, null));
	}

	public int getMaxCp()
	{
		return Math.max(1, (int) calcStat(Stats.MAX_CP, _template.getBaseCpMax(), null, null));
	}

	public int getMaxHp()
	{
		return Math.max(1, (int) calcStat(Stats.MAX_HP, _template.getBaseHpMax(), null, null));
	}

	public int getMaxMp()
	{
		return Math.max(1, (int) calcStat(Stats.MAX_MP, _template.getBaseMpMax(), null, null));
	}

	public int getMDef(Creature target, Skill skill)
	{
		return Math.max((int) calcStat(Stats.MAGIC_DEFENCE, _template.getBaseMDef(), target, skill), 1);
	}

	public double getMinDistance(GameObject obj)
	{
		double distance = getCurrentCollisionRadius();

		if(obj != null && obj.isCreature())
			distance += ((Creature) obj).getCurrentCollisionRadius();

		return distance;
	}
	
	public String getName(boolean stored)
	{
		return StringUtils.defaultString(_name);
	}
	@Override
	public String getName()
	{
		if(isPlayer() && isInLastHero() && Config.LAST_HERO_HIDE_NAMES)
			return StringUtils.defaultString(Config.LAST_HERO_GLOBAL_NAME);
		return StringUtils.defaultString(_name);
	}

	public int getPAtk(Creature target)
	{
		return (int) calcStat(Stats.POWER_ATTACK, _template.getBasePAtk(), target, null);
	}

	public int getPAtkSpd()
	{
		return (int) calcStat(Stats.POWER_ATTACK_SPEED, _template.getBasePAtkSpd(), null, null);
	}

	public int getPDef(Creature target)
	{
		return (int) calcStat(Stats.POWER_DEFENCE, _template.getBasePDef(), target, null);
	}

	public final int getPhysicalAttackRange()
	{
		return (int) calcStat(Stats.POWER_ATTACK_RANGE, getTemplate().getBaseAtkRange(), null, null);
	}

	public int getRandomDamage()
	{
		WeaponTemplate weaponItem = getActiveWeaponTemplate();
		if(weaponItem == null)
			return 5 + (int) Math.sqrt(getLevel());
		return weaponItem.getRandomDamage();
	}

	public double getReuseModifier(Creature target)
	{
		return calcStat(Stats.ATK_REUSE, 1, target, null);
	}

	public final int getShldDef()
	{
		if(isPlayer())
			return (int) calcStat(Stats.SHIELD_DEFENCE, 0, null, null);
		return (int) calcStat(Stats.SHIELD_DEFENCE, _template.getBaseShldDef(), null, null);
	}

	public final int getSkillDisplayLevel(Integer skillId)
	{
		Skill skill = _skills.get(skillId);
		if(skill == null)
			return -1;
		return skill.getDisplayLevel();
	}

	public final int getSkillLevel(Integer skillId)
	{
		return getSkillLevel(skillId, -1);
	}

	public final int getSkillLevel(Integer skillId, int def)
	{
		Skill skill = _skills.get(skillId);
		if(skill == null)
			return def;
		return skill.getLevel();
	}

	public int getSkillMastery(Integer skillId)
	{
		if(_skillMastery == null)
			return 0;
		Integer val = _skillMastery.get(skillId);
		return val == null ? 0 : val.intValue();
	}

	public void removeSkillMastery(Integer skillId)
	{
		if(_skillMastery != null)
			_skillMastery.remove(skillId);
	}

	public GameObject getTarget()
	{
		return target.get();
	}

	public final int getTargetId()
	{
		GameObject target = getTarget();
		return target == null ? -1 : target.getObjectId();
	}

	public CharTemplate getTemplate()
	{
		return _template;
	}

	public String getTitle()
	{
		if(isPlayer() && isInLastHero() && Config.LAST_HERO_HIDE_NAMES)
			return StringUtils.defaultString(Config.LAST_HERO_GLOBAL_NAME);	
		return StringUtils.defaultString(_title);
	}

	public double headingToRadians(int heading)
	{
		return (heading - 32768) / HEADINGS_IN_PI;
	}

	public boolean isAlikeDead()
	{
		return _fakeDeath || isDead();
	}

	public final boolean isAttackingNow()
	{
		return _attackEndTime > System.currentTimeMillis();
	}

	public final long getLastAttackTime()
	{
		return _lastAttackTime;
	}

	public final void setLastAttackTime(long value)
	{
		_lastAttackTime = value;
	}

	public final boolean isBlessedByNoblesse()
	{
		return _isBlessedByNoblesse;
	}

	public final boolean isSalvation()
	{
		return _isSalvation;
	}

	public boolean isEffectImmune()
	{
		return _effectImmunity.get() || getFlags().getEffectImmunity().get();
	}

	public boolean isBuffImmune()
	{
		return _buffImmunity.get();
	}

	public boolean isDebuffImmune()
	{
		return _debuffImmunity.get() || isPeaceNpc();
	}

	public boolean isDead()
	{
		return _currentHp < 0.5 || isDead.get();
	}

	@Override
	public final boolean isFlying()
	{
		return _flying;
	}

	/**
	 * Находится ли персонаж в боевой позе
	 * @return true, если персонаж в боевой позе, атакован или атакует
	 */
	public final boolean isInCombat()
	{
		return System.currentTimeMillis() < _stanceEndTime;
	}

	public boolean isInvul()
	{
		return _isInvul || getFlags().getInvulnerable().get();
	}

	public boolean isMageClass()
	{
		return getTemplate().getBaseMAtk() > 3;
	}

	public final boolean isRunning()
	{
		return _running;
	}

	protected long _reuseDelay = 0;

	public final void setReuseDelay(long newReuseDelay)
	{
		_reuseDelay = newReuseDelay + System.currentTimeMillis();
	}

	public final long getReuseDelay()
	{
		return _reuseDelay;
	}

	public boolean isSkillDisabled(Skill skill)
	{
		TimeStamp sts = _skillReuses.get(skill.getReuseHash());
		if(sts == null)
			return false;
		if(_reuseDelay != 0 && System.currentTimeMillis() >= _reuseDelay)
		{
			_reuseDelay = 0;
			return false;
		}
		if(sts.hasNotPassed())
			return true;
		_skillReuses.remove(skill.getReuseHash());
		return false;
	}

	public final boolean isTeleporting()
	{
		return isTeleporting.get();
	}

	/**
	 * Возвращает позицию цели, в которой она будет через пол секунды.
	 */
	public Location getIntersectionPoint(Creature target)
	{
		if(!PositionUtils.isFacing(this, target, 90))
			return new Location(target.getX(), target.getY(), target.getZ());
		double angle = PositionUtils.convertHeadingToDegree(target.getHeading()); // угол в градусах
		double radian = Math.toRadians(angle - 90); // угол в радианах
		double range = target.getMoveSpeed() / 2; // расстояние, пройденное за 1 секунду, равно скорости. Берем половину.
		return new Location((int) (target.getX() - range * Math.sin(radian)), (int) (target.getY() + range * Math.cos(radian)), target.getZ());
	}

	public Location applyOffset(Location point, int offset)
	{
		if(offset <= 0)
			return point;

		long dx = point.x - getX();
		long dy = point.y - getY();
		long dz = point.z - getZ();

		double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

		if(distance <= offset)
		{
			point.set(getX(), getY(), getZ());
			return point;
		}

		if(distance >= 1)
		{
			double cut = offset / distance;
			point.x -= (int) (dx * cut + 0.5);
			point.y -= (int) (dy * cut + 0.5);
			point.z -= (int) (dz * cut + 0.5);

			if(!isFlying() && !isInBoat() && !isInWater() && !isBoat())
				point.correctGeoZ(getGeoIndex());
		}

		return point;
	}

	public List<Location> applyOffset(List<Location> points, int offset)
	{
		offset = offset >> 4;
		if(offset <= 0)
			return points;

		long dx = points.get(points.size() - 1).x - points.get(0).x;
		long dy = points.get(points.size() - 1).y - points.get(0).y;
		long dz = points.get(points.size() - 1).z - points.get(0).z;

		double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if(distance <= offset)
		{
			Location point = points.get(0);
			points.clear();
			points.add(point);
			return points;
		}

		if(distance >= 1)
		{
			double cut = offset / distance;
			int num = (int) (points.size() * cut + 0.5);
			for(int i = 1; i <= num && points.size() > 0; i++)
				points.remove(points.size() - 1);
		}

		return points;
	}

	private Location setSimplePath(Location dest)
	{
		List<Location> moveList = GeoMove.constructMoveList(getLoc(), dest);
		if(moveList.isEmpty())
			return null;
		_targetRecorder.clear();
		_targetRecorder.add(moveList);
		return moveList.get(moveList.size() - 1);
	}

	private Location buildPathTo(int x, int y, int z, int offset, boolean pathFind)
	{
		return buildPathTo(x, y, z, offset, null, false, pathFind);
	}

	private Location buildPathTo(int x, int y, int z, int offset, Creature follow, boolean forestalling, boolean pathFind)
	{
		int geoIndex = getGeoIndex();

		Location dest;

		if(forestalling && follow != null && follow.isMoving)
			dest = getIntersectionPoint(follow);
		else
			dest = new Location(x, y, z);

		if(isInBoat() || isBoat() || !Config.ALLOW_GEODATA)
		{
			applyOffset(dest, offset);
			return setSimplePath(dest);
		}

		if(isFlying() || isInWater())
		{
			applyOffset(dest, offset);

			Location nextloc;

			if(isFlying())
			{
				if(GeoEngine.canSeeCoord(this, dest.x, dest.y, dest.z, true))
					return setSimplePath(dest);

				nextloc = GeoEngine.moveCheckInAir(this, dest.x, dest.y, dest.z);
				if(nextloc != null && !nextloc.equals(getX(), getY(), getZ()))
					return setSimplePath(nextloc);
			}
			else
			{
				nextloc = GeoEngine.moveInWaterCheck(this, dest.x, dest.y, dest.z, getWaterZ());
				if(nextloc == null)
					return null;

				List<Location> moveList = GeoMove.constructMoveList(getLoc(), nextloc.clone());
				_targetRecorder.clear();
				if(!moveList.isEmpty())
					_targetRecorder.add(moveList);

				int dz = dest.z - nextloc.z;
				// если пытаемся выбратся на берег, считаем путь с точки выхода до точки назначения
				if(dz > 0 && dz < 128)
				{
					moveList = GeoEngine.MoveList(nextloc.x, nextloc.y, nextloc.z, dest.x, dest.y, geoIndex, false);
					if(moveList != null) // null - до конца пути дойти нельзя
					{
						if(!moveList.isEmpty()) // уже стоим на нужной клетке
							_targetRecorder.add(moveList);
					}
				}

				if(!moveList.isEmpty())
					return moveList.get(moveList.size() - 1);
			}
			return null;
		}

		List<Location> moveList = GeoEngine.MoveList(getX(), getY(), getZ(), dest.x, dest.y, geoIndex, true); // onlyFullPath = true - проверяем весь путь до конца
		if(moveList != null) // null - до конца пути дойти нельзя
		{
			if(moveList.size() < 2) // уже стоим на нужной клетке
				return null;
			applyOffset(moveList, offset);
			if(moveList.size() < 2) // уже стоим на нужной клетке
				return null;
			_targetRecorder.clear();
			_targetRecorder.add(moveList);
			return moveList.get(moveList.size() - 1);
		}

		// Фейковые игроки ВСЕГДА передвигаются с поиском пути.
		if(pathFind || isFakePlayer())
		{
			List<List<Location>> targets = GeoMove.findMovePath(getX(), getY(), getZ(), dest.getX(), dest.getY(), dest.getZ(), this, geoIndex);
			if(!targets.isEmpty())
			{
				moveList = targets.remove(targets.size() - 1);
				applyOffset(moveList, offset);
				if(!moveList.isEmpty())
					targets.add(moveList);
				if(!targets.isEmpty())
				{
					_targetRecorder.clear();
					_targetRecorder.addAll(targets);
					for(int i = targets.size() - 1; i >= 0; i--)
					{
						List<Location> target = targets.get(i);
						if(!target.isEmpty())
							return target.get(target.size() - 1);
					}
					return null;
				}
			}
		}

		if(isPlayable() && !isFakePlayer()) // расчитываем путь куда сможем дойти, только для игровых персонажей
		{
			applyOffset(dest, offset);

			moveList = GeoEngine.MoveList(getX(), getY(), getZ(), dest.x, dest.y, geoIndex, false); // onlyFullPath = false - идем до куда можем
			if(moveList != null && moveList.size() > 1) // null - нет геодаты, empty - уже стоим на нужной клетке
			{
				_targetRecorder.clear();
				_targetRecorder.add(moveList);
				return moveList.get(moveList.size() - 1);
			}
		}

		return null;
	}

	public Creature getFollowTarget()
	{
		return followTarget.get();
	}

	public void setFollowTarget(Creature target)
	{
		followTarget = target == null ? HardReferences.<Creature> emptyRef() : target.getRef();
	}

	public boolean followToCharacter(Creature target, int offset, boolean forestalling)
	{
		return followToCharacter(target.getLoc(), target, offset, forestalling);
	}

	public boolean followToCharacter(Location loc, Creature target, int offset, boolean forestalling)
	{
		moveLock.lock();
		try
		{
			if(isMovementDisabled() || target == null || isInBoat() || target.isInvisible(this))
				return false;

			if(getReflection() != target.getReflection())
				return false;

			if(getDistance(target) > 5000) // TODO: Вынести в конфиг?!?
				return false;

			offset = Math.max(offset, 10);
			if(isFollow && target == getFollowTarget() && offset == _offset)
				return true;

			if(Math.abs(getZ() - target.getZ()) > 1000 && !isFlying())
				return false;

			getAI().clearNextAction();

			stopMove(false);

			deactivateGeoControl();

			if(buildPathTo(loc.x, loc.y, loc.z, offset, target, forestalling, !target.isDoor()) != null)
				movingDestTempPos.set(loc.x, loc.y, loc.z);
			else
			{
				activateGeoControl();
				return false;
			}

			isMoving = true;
			isKeyboardMoving = false;
			isFollow = true;
			_forestalling = forestalling;
			_offset = offset;
			setFollowTarget(target);

			moveNext(true);

			return true;
		}
		finally
		{
			moveLock.unlock();
		}
	}

	public boolean moveToLocation(Location loc, int offset, boolean pathfinding)
	{
		return moveToLocation(loc.x, loc.y, loc.z, offset, pathfinding, true, false, -1);
	}

	public boolean moveToLocation(Location loc, int offset, boolean pathfinding, int maxDestRange)
	{
		return moveToLocation(loc.x, loc.y, loc.z, offset, pathfinding, true, false, maxDestRange);
	}

	public boolean moveToLocation(Location loc, int offset, boolean pathfinding, boolean cancelNextAction, boolean keyboard)
	{
		return moveToLocation(loc.x, loc.y, loc.z, offset, pathfinding, cancelNextAction, keyboard, -1);
	}

	public boolean moveToLocation(int x_dest, int y_dest, int z_dest, int offset, boolean pathfinding)
	{
		return moveToLocation(x_dest, y_dest, z_dest, offset, pathfinding, true, false, -1);
	}

	public boolean moveToLocation(int x_dest, int y_dest, int z_dest, int offset, boolean pathfinding, boolean cancelNextAction, boolean keyboard)
	{
		return moveToLocation(x_dest, y_dest, z_dest, offset, pathfinding, cancelNextAction, keyboard, -1);
	}

	public boolean moveToLocation(int x_dest, int y_dest, int z_dest, int offset, boolean pathfinding, boolean cancelNextAction, boolean keyboard, int maxDestRange)
	{
		moveLock.lock();
		try
		{
			offset = Math.max(offset, 0);
			Location dst_geoloc = new Location(x_dest, y_dest, z_dest).world2geo();
			if(isMoving && !isFollow && movingDestTempPos.equals(dst_geoloc))
			{
				sendActionFailed();
				return true;
			}

			if(isMovementDisabled())
			{
				getAI().setNextAction(nextAction.MOVE, new Location(x_dest, y_dest, z_dest), offset, pathfinding && !keyboard, false);
				sendActionFailed();
				return false;
			}

			getAI().clearNextAction();

			if(isPlayer())
			{
				if(cancelNextAction)
					getAI().changeIntention(AI_INTENTION_ACTIVE, null, null);
			}

			stopMove(false);

			deactivateGeoControl();

			dst_geoloc = buildPathTo(x_dest, y_dest, z_dest, offset, pathfinding && !keyboard);
			if(dst_geoloc != null)
			{
				if(maxDestRange == -1)
					movingDestTempPos.set(dst_geoloc);
				else
				{
					Location dst_loc = dst_geoloc.geo2world();
					if(PositionUtils.checkIfInRange(maxDestRange + offset, x_dest, y_dest, z_dest, dst_loc.x, dst_loc.y, dst_loc.z, true))
						movingDestTempPos.set(dst_geoloc);
					else
					{
						activateGeoControl();
						sendActionFailed();
						return false;
					}
				}
			}
			else
			{
				activateGeoControl();
				sendActionFailed();
				return false;
			}

			isMoving = true;
			isKeyboardMoving = keyboard;

			moveNext(true);

			return true;
		}
		finally
		{
			moveLock.unlock();
		}
	}

	private void moveNext(boolean firstMove)
	{
		if(!isMoving || isMovementDisabled())
		{
			stopMove();
			return;
		}

		_previousSpeed = getMoveSpeed();
		if(_previousSpeed <= 0)
		{
			stopMove();
			return;
		}

		if(!firstMove)
		{
			Location dest = destination.clone();
			if(dest != null)
				setLoc(dest, true);
		}

		if(_targetRecorder.isEmpty())
		{
			CtrlEvent ctrlEvent = isFollow ? CtrlEvent.EVT_ARRIVED_TARGET : CtrlEvent.EVT_ARRIVED;
			stopMove(false);
			ThreadPoolManager.getInstance().execute(new NotifyAITask(this, ctrlEvent));
			return;
		}

		moveList = _targetRecorder.remove(0);
		Location begin = moveList.get(0).clone().geo2world();
		Location end = moveList.get(moveList.size() - 1).clone().geo2world();

		// TODO: Придумать лучше способ.
		if(!isFlying() && !isInBoat() && !isInWater() && !isBoat() && !GeoEngine.canMoveToCoord(getX(), getY(), getZ(), end.x, end.y, end.z, getGeoIndex()))
		{
			stopMove();
			return;
		}

		destination = end;
		double distance = (isFlying() || isInWater()) ? begin.distance3D(end) : begin.distance(end); //клиент при передвижении не учитывает поверхность

		if(distance != 0)
			setHeading(PositionUtils.calculateHeadingFrom(getX(), getY(), destination.x, destination.y));

		broadcastMove();

		_startMoveTime = _followTimestamp = System.currentTimeMillis();
		if(_moveTaskRunnable == null)
			_moveTaskRunnable = new MoveNextTask();
		_moveTask = ThreadPoolManager.getInstance().schedule(_moveTaskRunnable.setDist(distance), getMoveTickInterval());
	}

	protected void broadcastMove()
	{
		broadcastPacket(movePacket());
	}

	public void broadcastStopMove()
	{
		broadcastPacket(stopMovePacket());
	}

	/**
	 * Останавливает движение и рассылает StopMove
	 */
	public void stopMove()
	{
		stopMove(true);
	}

	/**
	 * Останавливает движение
	 *
	 * @param stop - рассылать ли StopMove
	 */
	public void stopMove(boolean stop)
	{
		if(!isMoving)
			return;

		moveLock.lock();
		try
		{
			if(!isMoving)
				return;

			isMoving = false;
			isKeyboardMoving = false;
			isFollow = false;

			if(_moveTask != null)
			{
				_moveTask.cancel(false);
				_moveTask = null;
			}

			destination = null;
			moveList = null;

			_targetRecorder.clear();

			if(stop)
				broadcastStopMove();
			else
				sendActionFailed();

			activateGeoControl();
		}
		finally
		{
			moveLock.unlock();
		}
	}

	/** Возвращает координаты поверхности воды, если мы находимся в ней, или над ней. */
	public int[] getWaterZ()
	{
		int[] waterZ = new int[]{ Integer.MIN_VALUE, Integer.MAX_VALUE };
		if(!isInWater())
			return waterZ;

		zonesRead.lock();
		try
		{
			Zone zone;
			for(int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if(zone.getType() == ZoneType.water)
				{
					if(waterZ[0] == Integer.MIN_VALUE || waterZ[0] > zone.getTerritory().getZmin())
						waterZ[0] = zone.getTerritory().getZmin();
					if(waterZ[1] == Integer.MAX_VALUE || waterZ[1] < zone.getTerritory().getZmax())
						waterZ[1] = zone.getTerritory().getZmax();
				}
			}
		}
		finally
		{
			zonesRead.unlock();
		}
		return waterZ;
	}

	protected L2GameServerPacket stopMovePacket()
	{
		return new StopMovePacket(this);
	}

	public L2GameServerPacket movePacket()
	{
		return new MTLPacket(this);
	}

	public void updateZones()
	{
		if(isInObserverMode())
			return;

		Zone[] zones = isVisible() ? getCurrentRegion().getZones() : Zone.EMPTY_L2ZONE_ARRAY;

		LazyArrayList<Zone> entering = null;
		LazyArrayList<Zone> leaving = null;

		Zone zone;

		zonesWrite.lock();
		try
		{
			if(!_zones.isEmpty())
			{
				leaving = LazyArrayList.newInstance();
				for(int i = 0; i < _zones.size(); i++)
				{
					zone = _zones.get(i);
					// зоны больше нет в регионе, либо вышли за территорию зоны
					if(!ArrayUtils.contains(zones, zone) || !zone.checkIfInZone(getX(), getY(), getZ(), getReflection()))
						leaving.add(zone);
				}

				//Покинули зоны, убираем из списка зон персонажа
				if(!leaving.isEmpty())
				{
					for(int i = 0; i < leaving.size(); i++)
					{
						zone = leaving.get(i);
						_zones.remove(zone);
					}
				}
			}

			if(zones.length > 0)
			{
				entering = LazyArrayList.newInstance();
				for(int i = 0; i < zones.length; i++)
				{
					zone = zones[i];
					// в зону еще не заходили и зашли на территорию зоны
					if(!_zones.contains(zone) && zone.checkIfInZone(getX(), getY(), getZ(), getReflection()))
						entering.add(zone);
				}

				//Вошли в зоны, добавим в список зон персонажа
				if(!entering.isEmpty())
				{
					for(int i = 0; i < entering.size(); i++)
					{
						zone = entering.get(i);
						_zones.add(zone);
					}
				}
			}
		}
		finally
		{
			zonesWrite.unlock();
		}

		onUpdateZones(leaving, entering);

		if(leaving != null)
			LazyArrayList.recycle(leaving);

		if(entering != null)
			LazyArrayList.recycle(entering);

	}

	protected void onUpdateZones(List<Zone> leaving, List<Zone> entering)
	{
		Zone zone;

		if(leaving != null && !leaving.isEmpty())
		{
			for(int i = 0; i < leaving.size(); i++)
			{
				zone = leaving.get(i);
				zone.doLeave(this);
			}
		}

		if(entering != null && !entering.isEmpty())
		{
			for(int i = 0; i < entering.size(); i++)
			{
				zone = entering.get(i);
				zone.doEnter(this);
			}
		}
	}

	public boolean isInZonePeace()
	{
		return isInZone(ZoneType.peace_zone) && !isInZoneBattle();
	}

	public boolean isInZoneBattle()
	{
		return isInZone(ZoneType.battle_zone);
	}

	public boolean isInZonePvP()
	{
		for(Zone zone : _zones)
			if(zone.getTemplate().isEpicPvP())
				return true;
		return false;
	}

	public boolean isInSpecialPvPZone()
	{
		if(!Config.ENABLE_NEW_PVP_SYSTEM)
			return false; //mandatory avoid to ask this config everytime	
		for(Zone zone : _zones)
			if(zone.getTemplate().isSpecialPvP())
				return true;
		return false;	
	}
	
	public int getSpecialZoneMinLevel()
	{
		for(Zone zone : _zones)
			if(zone.getTemplate().isSpecialPvP() && zone.getTemplate().isEnabled())
				return zone.getTemplate().getPvPLevelMin();
		return 1;		
	}
	
	public int getSpecialZoneMaxLevel()
	{
		for(Zone zone : _zones)
			if(zone.getTemplate().isSpecialPvP() && zone.getTemplate().isEnabled())
				return zone.getTemplate().getPvPLevelMax();
		return 85;		
	}
	
	@Override
	public boolean isInWater()
	{
		return isInZone(ZoneType.water) && !(isInBoat() || isBoat() || isFlying());
	}

	public boolean isInZone(ZoneType type)
	{
		zonesRead.lock();
		try
		{
			Zone zone;
			for(int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if(zone.getType() == type)
					return true;
			}
		}
		finally
		{
			zonesRead.unlock();
		}

		return false;
	}

	public boolean isInZone(String name)
	{
		zonesRead.lock();
		try
		{
			Zone zone;
			for(int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if(zone.getName().equals(name))
					return true;
			}
		}
		finally
		{
			zonesRead.unlock();
		}

		return false;
	}

	public boolean isInZone(Zone zone)
	{
		zonesRead.lock();
		try
		{
			return _zones.contains(zone);
		}
		finally
		{
			zonesRead.unlock();
		}
	}

	public Zone getZone(ZoneType type)
	{
		zonesRead.lock();
		try
		{
			Zone zone;
			for(int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if(zone.getType() == type)
					return zone;
			}
		}
		finally
		{
			zonesRead.unlock();
		}
		return null;
	}

	public List<Zone> getZones()
	{
		return _zones;
	}

	public Location getRestartPoint()
	{
		zonesRead.lock();
		try
		{
			Zone zone;
			for(int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if(zone.getRestartPoints() != null)
				{
					ZoneType type = zone.getType();
					if(type == ZoneType.battle_zone || type == ZoneType.peace_zone || type == ZoneType.offshore || type == ZoneType.dummy)
						return zone.getSpawn();
				}
			}
		}
		finally
		{
			zonesRead.unlock();
		}

		return null;
	}

	public Location getPKRestartPoint()
	{
		zonesRead.lock();
		try
		{
			Zone zone;
			for(int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if(zone.getRestartPoints() != null)
				{
					ZoneType type = zone.getType();
					if(type == ZoneType.battle_zone || type == ZoneType.peace_zone || type == ZoneType.offshore || type == ZoneType.dummy)
						return zone.getPKSpawn();
				}
			}
		}
		finally
		{
			zonesRead.unlock();
		}

		return null;
	}

	@Override
	public int getGeoZ(int x, int y, int z)
	{
		if(isFlying() || isInWater() || isInBoat() || isBoat() || isDoor())
			return z;

		return super.getGeoZ(x, y, z);
	}

	protected boolean needStatusUpdate()
	{
		if(!isVisible())
			return false;

		boolean result = false;

		int bar;
		bar = (int) (getCurrentHp() * CLIENT_BAR_SIZE / getMaxHp());
		if(bar == 0 || bar != _lastHpBarUpdate)
		{
			_lastHpBarUpdate = bar;
			result = true;
		}

		bar = (int) (getCurrentMp() * CLIENT_BAR_SIZE / getMaxMp());
		if(bar == 0 || bar != _lastMpBarUpdate)
		{
			_lastMpBarUpdate = bar;
			result = true;
		}

		if(isPlayer())
		{
			bar = (int) (getCurrentCp() * CLIENT_BAR_SIZE / getMaxCp());
			if(bar == 0 || bar != _lastCpBarUpdate)
			{
				_lastCpBarUpdate = bar;
				result = true;
			}
		}

		return result;
	}

	@Override
	public void onForcedAttack(Player player, boolean shift)
	{
		if(!isAttackable(player) || player.isConfused() || player.isBlocked())
		{
			player.sendActionFailed();
			return;
		}

		player.getAI().Attack(this, true, shift);
	}

	public void onHitTimer(Creature target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld, boolean unchargeSS)
	{
		if(isAlikeDead())
		{
			sendActionFailed();
			return;
		}

		if(target.isDead() || !isInRange(target, 2000))
		{
			sendActionFailed();
			return;
		}

		if(isPlayable() && target.isPlayable() && isInZoneBattle() != target.isInZoneBattle())
		{
			Player player = getPlayer();
			if(player != null)
			{
				player.sendPacket(Msg.INVALID_TARGET);
				player.sendActionFailed();
			}
			return;
		}

		target.getListeners().onAttackHit(this);

		// if hitted by a cursed weapon, Cp is reduced to 0, if a cursed weapon is hitted by a Hero, Cp is reduced to 0
		if(!miss && target.isPlayer() && (isCursedWeaponEquipped() || getActiveWeaponInstance() != null && getActiveWeaponInstance().isHeroWeapon() && target.isCursedWeaponEquipped()))
			target.setCurrentCp(0);

		Formulas.calcStunBreak(target, crit);

		displayGiveDamageMessage(target, damage, crit, miss, shld, false);

		ThreadPoolManager.getInstance().execute(new NotifyAITask(target, CtrlEvent.EVT_ATTACKED, this, damage));

		boolean checkPvP = checkPvP(target, null);
		// Reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary
		if(!miss && damage > 0)
		{
			target.reduceCurrentHp(damage, this, null, true, true, false, true, false, false, true);

			// Скиллы, кастуемые при физ атаке
			if(!target.isDead())
			{
				if(crit)
					useTriggers(target, TriggerType.CRIT, null, null, damage);

				useTriggers(target, TriggerType.ATTACK, null, null, damage);

				// Manage attack or cast break of the target (calculating rate, sending message...)
				if(Formulas.calcCastBreak(target, crit))
					target.abortCast(false, true);
			}

			if(soulshot && unchargeSS)
				unChargeShots(false);
		}

		if(miss)
			target.useTriggers(this, TriggerType.UNDER_MISSED_ATTACK, null, null, damage);

		startAttackStanceTask();

		if(checkPvP)
			startPvPFlag(target);
	}

	public void onMagicUseTimer(Creature aimingTarget, Skill skill, boolean forceUse)
	{
		_castInterruptTime = 0;

		if(skill.isUsingWhileCasting())
		{
			aimingTarget.getEffectList().stopEffect(skill.getId());
			onCastEndTime(null, false);
			return;
		}

		if(!skill.isOffensive() && getAggressionTarget() != null)
			forceUse = true;

		if(!skill.checkCondition(this, aimingTarget, forceUse, false, false))
		{
			if(skill.getSkillType() == SkillType.PET_SUMMON && isPlayer())
				getPlayer().setPetControlItem(null);
			onCastEndTime(null, false);
			return;
		}
		int skillTime = skill.isSkillTimePermanent() ? skill.getHitTime() : Formulas.calcMAtkSpd(this, skill, skill.getHitTime());

		int minCastTime = Math.min(Config.SKILLS_CAST_TIME_MIN, skill.getHitTime());
		if(skillTime < minCastTime)
		{
			skillTime = minCastTime;
		}
		
		//if(_skillTask == null)
			//System.out.println("BAD SKILL with 0 reuse? "+skill.getName()+" from "+this.getName()+"");
		if(skill.getCastRange() < 32767 && skill.getSkillType() != SkillType.TAKECASTLE && skill.getSkillType() != SkillType.TAKEFORTRESS && !GeoEngine.canSeeTarget(this, aimingTarget) && _didntSeeOn70Percent)
		{
			sendPacket(SystemMsg.CANNOT_SEE_TARGET);
			broadcastPacket(new MagicSkillCanceled(getObjectId()));
			onCastEndTime(null, false);
			return;
		}

		List<Creature> targets = skill.getTargets(this, aimingTarget, forceUse);

		int vitalityConsume = skill.getVitConsume();
		if(vitalityConsume > 0 && this.isPlayer())
		{
			Player p = (Player) this;
			p.setVitality(Math.max(0, p.getVitality() - vitalityConsume));		
		}	
		int hpConsume = skill.getHpConsume();
		if(hpConsume > 0)
			setCurrentHp(Math.max(0, _currentHp - hpConsume), false);

		double mpConsume2 = skill.getMpConsume2();
		if(mpConsume2 > 0)
		{
			if(skill.isMusic())
			{
				double inc = mpConsume2 / 2;
				double add = 0;
				for(Effect e : getEffectList().getAllEffects())
					if(e.getSkill().getId() != skill.getId() && e.getSkill().isMusic() && e.getTimeLeft() > 30)
						add += inc;
				mpConsume2 += add;
				mpConsume2 = calcStat(Stats.MP_DANCE_SKILL_CONSUME, mpConsume2, aimingTarget, skill);
			}
			else if(skill.isMagic())
				mpConsume2 = calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, aimingTarget, skill);
			else
				mpConsume2 = calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, aimingTarget, skill);

			if(_currentMp < mpConsume2 && isPlayable())
			{
				sendPacket(Msg.NOT_ENOUGH_MP);
				onCastEndTime(null, false);
				return;
			}
			reduceCurrentMp(mpConsume2, null);
		}

		callSkill(skill, targets, true);

		if(skill.getNumCharges() > 0)
			setIncreasedForce(getIncreasedForce() - skill.getNumCharges());

		if(skill.isSoulBoost())
			setConsumedSouls(getConsumedSouls() - Math.min(getConsumedSouls(), 5), null);
		else if(skill.getSoulsConsume() > 0)
			setConsumedSouls(getConsumedSouls() - skill.getSoulsConsume(), null);

		switch(skill.getFlyType())
		{
			case THROW_UP:
			case THROW_HORIZONTAL:
				Location flyLoc;
				for(Creature target : targets)
				{
					//target.setHeading(this, false); //TODO [VISTALL] set heading of target ? Oo
					flyLoc = getFlyLocation(null, skill);
					target.setLoc(flyLoc);
					broadcastPacket(new FlyToLocationPacket(target, flyLoc, skill.getFlyType()));
				}
				break;
		}

		if(_scheduledCastCount > 0)
		{
			_scheduledCastCount--;
			_skillLaunchedTask = ThreadPoolManager.getInstance().schedule(new MagicLaunchedTask(this, forceUse), _scheduledCastInterval);
			_skillTask = ThreadPoolManager.getInstance().schedule(new MagicUseTask(this, forceUse), _scheduledCastInterval);
			ThreadPoolManager.getInstance().schedule(new SeventeethPecentTimer(this), (long)(_scheduledCastInterval * 0.66));
			return;
		}

		int skillCoolTime = Formulas.calcMAtkSpd(this, skill, skill.getCoolTime());
		if(skillCoolTime > 0)
			ThreadPoolManager.getInstance().schedule(new CastEndTimeTask(this, targets), skillCoolTime);
		else
			onCastEndTime(targets, true);
	}
	
	public void NotPass70PercentCheck(boolean answer)
	{
		_didntSeeOn70Percent = answer;
	}
	
	public void onCastEndTime(List<Creature> targets, boolean success)
	{
		final Skill castingSkill = getCastingSkill();
		final Creature castingTarget = getCastingTarget();

		finishFly();
		clearCastVars();

		if(castingSkill != null)
		{
			getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING, castingSkill, castingTarget, success);

			if(success)
				castingSkill.onFinishCast(this, targets);
		}
	}

	public void clearCastVars()
	{
		_animationEndTime = 0;
		_castInterruptTime = 0;
		_scheduledCastCount = 0;
		_castingSkill = null;
		_skillTask = null;
		_skillLaunchedTask = null;
		_flyLoc = null;
		_didntSeeOn70Percent = false;
	}

	private void finishFly()
	{
		Location flyLoc = _flyLoc;
		_flyLoc = null;
		if(flyLoc != null)
		{
			setLoc(flyLoc);
		}
	}

	public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
	{
		if(attacker == null || isDead() || (attacker.isDead() && !isDot))
			return;

		if(isDamageBlocked() && transferDamage)
			return;

		if(isDamageBlocked() && attacker != this)
		{
			if (sendMessage)
				attacker.sendPacket(Msg.THE_ATTACK_HAS_BEEN_BLOCKED);
			return;
		}
		
		if(isSummon())
			damage = damage*Config.ALT_SUMMONS_DAMAGE;
		
		if(canReflect)
		{
			if(attacker.absorbAndReflect(this, skill, damage))
				return;

			damage = absorbToEffector(attacker, damage);
			damage = absorbToMp(attacker, damage);
			damage = absorbToSummon(attacker, damage);
		}

		getListeners().onCurrentHpDamage(damage, attacker, skill);

		if(attacker != this)
		{
			if (sendMessage)
				displayReceiveDamageMessage(attacker, (int) damage);

			if(!isDot)
				useTriggers(attacker, TriggerType.RECEIVE_DAMAGE, null, null, damage);
		}
        /*---------------------------------------------------------------------------------*/
        /* Ancient Herb - Slayer (14824) allows you to kill mobs marsh with a single blow. */
        /*---------------------------------------------------------------------------------*/
        int[] fields_npc = 
		{
            18805, //Waste Landfill Machine
            18806, //Brazier Of Purity
            22656, //Suppressor
            22657, //Exterminator
            22658, //Evil Spirits Magic Force
            22659, //Guardian Waterspirit
            22655, //Awakened Mucrokian
            22654, //Contaminated Mucrokian
            22650, //Mucrokian Fanatic
            22651, //Mucrokian Ascetic
            22652, //Mucrokian Savior
            22653 //Mucrokian Preacher
        };

        /*---------------------------------------------------------------------------------*/
        /*---------------------------------------------------------------------------------*/
		
		if (attacker.getEffectList().getEffectsBySkillId(2900) != null && isMonster()) 
		{
            if(ArrayUtils.contains(fields_npc, getNpcId()) && attacker.getLevel() >= 78)
                onReduceCurrentHp(getCurrentHp() + 1, attacker, skill, awake, standUp, directHp); 
			else 
                onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
        } 
		else 
		{
            onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
        }
	}

	protected void onReduceCurrentHp(final double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp)
	{
		if(awake && isSleeping())
			getEffectList().stopEffects(EffectType.Sleep);

		if(attacker != this || (skill != null && skill.isOffensive()))
		{
			if(isMeditated())
			{
				Effect effect = getEffectList().getEffectByType(EffectType.Meditation);
				if(effect != null)
					getEffectList().stopEffect(effect.getSkill());
			}

			startAttackStanceTask();
			checkAndRemoveInvisible();
		}

		if(damage <= 0)
			return;

		boolean isUndying = isUndying();

		setCurrentHp(Math.max(getCurrentHp() - damage, isUndying ? 0.5 : 0), false);

		if(isUndying)
		{
			if(getCurrentHp() == 0.5 && (!isPlayer() || !getPlayer().isGMUndying()))
				if(getFlags().getUndying().getFlag().compareAndSet(false, true))
					getListeners().onDeathFromUndying(attacker);
		}
		else if(getCurrentHp() < 0.5)
		{
			if(attacker != this || (skill != null && skill.isOffensive()))
				useTriggers(attacker, TriggerType.DIE, null, null, damage);

			doDie(attacker);
		}
	}

	public void reduceCurrentMp(double i, Creature attacker)
	{
		if(attacker != null && attacker != this)
		{
			if(isSleeping())
				getEffectList().stopEffects(EffectType.Sleep);

			if(isMeditated())
			{
				Effect effect = getEffectList().getEffectByType(EffectType.Meditation);
				if(effect != null)
					getEffectList().stopEffect(effect.getSkill());
			}
		}

		if(isDamageBlocked() && attacker != null && attacker != this)
		{
			attacker.sendPacket(Msg.THE_ATTACK_HAS_BEEN_BLOCKED);
			return;
		}

		// 5182 = Blessing of protection, работает если разница уровней больше 10 и не в зоне осады
		if(attacker != null && attacker.isPlayer() && Math.abs(attacker.getLevel() - getLevel()) > 10)
		{
			// ПК не может нанести урон чару с блессингом
			if(attacker.getKarma() > 0 && getEffectList().getEffectsBySkillId(5182) != null && !isInZone(ZoneType.SIEGE))
				return;
			// чар с блессингом не может нанести урон ПК
			if(getKarma() > 0 && attacker.getEffectList().getEffectsBySkillId(5182) != null && !attacker.isInZone(ZoneType.SIEGE))
				return;
		}

		i = _currentMp - i;

		if(i < 0)
			i = 0;

		setCurrentMp(i);

		if(attacker != null && attacker != this)
			startAttackStanceTask();
	}

	public void removeAllSkills()
	{
		for(Skill s : getAllSkillsArray())
			removeSkill(s);
	}

	public void removeBlockStats(List<Stats> stats)
	{
		if(_blockedStats != null)
		{
			_blockedStats.removeAll(stats);
			if(_blockedStats.isEmpty())
				_blockedStats = null;
		}
	}

	public Skill removeSkill(Skill skill)
	{
		if(skill == null)
			return null;
		return removeSkillById(skill.getId());
	}

	public Skill removeSkillById(Integer id)
	{
		// Remove the skill from the L2Character _skills
		Skill oldSkill = _skills.remove(id);

		// Remove all its Func objects from the L2Character calculator set
		if(oldSkill != null)
		{
			removeTriggers(oldSkill);
			removeStatsOwner(oldSkill);
			if(Config.ALT_DELETE_SA_BUFFS && (oldSkill.isItemSkill() || oldSkill.isHandler() || oldSkill.getName().startsWith("Item Skill")))
			{
				// Завершаем все эффекты, принадлежащие старому скиллу
				List<Effect> effects = getEffectList().getEffectsBySkill(oldSkill);
				if(effects != null)
					for(Effect effect : effects)
						effect.exit();
				// И с петов тоже
				Servitor pet = getServitor();
				if(pet != null)
				{
					effects = pet.getEffectList().getEffectsBySkill(oldSkill);
					if(effects != null)
						for(Effect effect : effects)
							effect.exit();
				}
			}
			getListeners().onSkillRemove(oldSkill);
		}

		return oldSkill;
	}

	public void addTriggers(StatTemplate f)
	{
		if(f.getTriggerList().isEmpty())
			return;

		for(TriggerInfo t : f.getTriggerList())
		{
			addTrigger(t);
		}
	}

	public void addTrigger(TriggerInfo t)
	{
		if(_triggers == null)
			_triggers = new ConcurrentHashMap<TriggerType, Set<TriggerInfo>>();

		Set<TriggerInfo> hs = _triggers.get(t.getType());
		if(hs == null)
		{
			hs = new CopyOnWriteArraySet<TriggerInfo> ();
			_triggers.put(t.getType(), hs);
		}

		hs.add(t);

		if(t.getType() == TriggerType.ADD)
			useTriggerSkill(this, null, t, null, 0);
	}

	public void removeTriggers(StatTemplate f)
	{
		if(_triggers == null || f.getTriggerList().isEmpty())
			return;

		for(TriggerInfo t : f.getTriggerList())
			removeTrigger(t);
	}

	public void removeTrigger(TriggerInfo t)
	{
		if(_triggers == null)
			return;
		Set<TriggerInfo> hs = _triggers.get(t.getType());
		if(hs == null)
			return;
		hs.remove(t);
	}

	public void sendActionFailed()
	{
		sendPacket(ActionFailPacket.STATIC);
	}

	public boolean hasAI()
	{
		return _ai != null;
	}

	public CharacterAI getAI()
	{
		if(_ai == null)
			synchronized (this)
			{
				if(_ai == null)
					_ai = new CharacterAI(this);
			}

		return _ai;
	}

	public void setAI(CharacterAI newAI)
	{
		if(newAI == null)
			return;

		CharacterAI oldAI = _ai;

		synchronized (this)
		{
			_ai = newAI;
		}

		if(oldAI != null)
		{
			if(oldAI.isActive())
			{
				oldAI.stopAITask();
				newAI.startAITask();
				newAI.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			}
		}
	}

	public final void setCurrentHp(double newHp, boolean canRessurect, boolean sendInfo)
	{
		int maxHp = getMaxHp();

		newHp = Math.min(maxHp, Math.max(0, newHp));

		if(isDeathImmune())
			newHp = Math.max(1.1, newHp); // Ставим 1.1, потому что на олимпиаде 1 == Поражение, что вызовет зависание.

		if(_currentHp == newHp)
			return;

		if(newHp >= 0.5 && isDead() && !canRessurect)
			return;

		double hpStart = _currentHp;

		_currentHp = newHp;

		if(isDead.compareAndSet(true, false))
			onRevive();

		checkHpMessages(hpStart, _currentHp);

		if (sendInfo)
		{
			broadcastStatusUpdate();
			sendChanges();
		}

		if(_currentHp < maxHp)
			startRegeneration();

		getListeners().onChangeCurrentHp(hpStart, newHp);
	}

	public final void setCurrentHp(double newHp, boolean canRessurect)
	{
		setCurrentHp(newHp, canRessurect, true);
	}

	public final void setCurrentMp(double newMp, boolean sendInfo)
	{
		int maxMp = getMaxMp();

		newMp = Math.min(maxMp, Math.max(0, newMp));

		if(_currentMp == newMp)
			return;

		if(newMp >= 0.5 && isDead())
			return;

		double mpStart = _currentMp;

		_currentMp = newMp;

		if (sendInfo)
		{
			broadcastStatusUpdate();
			sendChanges();
		}

		if(_currentMp < maxMp)
			startRegeneration();

		getListeners().onChangeCurrentMp(mpStart, newMp);
	}

	public final void setCurrentMp(double newMp)
	{
		setCurrentMp(newMp, true);
	}

	public final void setCurrentCp(double newCp, boolean sendInfo)
	{
		if(!isPlayer())
			return;

		int maxCp = getMaxCp();
		newCp = Math.min(maxCp, Math.max(0, newCp));

		if(_currentCp == newCp)
			return;

		if(newCp >= 0.5 && isDead())
			return;

		double cpStart = _currentCp;

		_currentCp = newCp;

		if (sendInfo)
		{
			broadcastStatusUpdate();
			sendChanges();
		}

		if(_currentCp < maxCp)
			startRegeneration();

		getListeners().onChangeCurrentCp(cpStart, newCp);
	}

	public final void setCurrentCp(double newCp)
	{
		setCurrentCp(newCp, true);
	}

	public void setCurrentHpMp(double newHp, double newMp, boolean canRessurect)
	{
		int maxHp = getMaxHp();
		int maxMp = getMaxMp();

		newHp = Math.min(maxHp, Math.max(0, newHp));
		newMp = Math.min(maxMp, Math.max(0, newMp));

		if(isDeathImmune())
			newHp = Math.max(1.1, newHp); // Ставим 1.1, потому что на олимпиаде 1 == Поражение, что вызовет зависание.

		if(_currentHp == newHp && _currentMp == newMp)
			return;

		if(newHp >= 0.5 && isDead() && !canRessurect)
			return;

		double hpStart = _currentHp;
		double mpStart = _currentMp;

		_currentHp = newHp;
		_currentMp = newMp;

		if(isDead.compareAndSet(true, false))
			onRevive();

		checkHpMessages(hpStart, _currentHp);

		broadcastStatusUpdate();
		sendChanges();

		if(_currentHp < maxHp || _currentMp < maxMp)
			startRegeneration();

		getListeners().onChangeCurrentHp(hpStart, newHp);
		getListeners().onChangeCurrentMp(mpStart, newMp);
	}

	public void setCurrentHpMp(double newHp, double newMp)
	{
		setCurrentHpMp(newHp, newMp, false);
	}

	public final void setFlying(boolean mode)
	{
		_flying = mode;
	}

	public final void setHeading(int heading, boolean broadcast)
	{
		super.setHeading(heading);
	}

	public final void setIsTeleporting(boolean value)
	{
		isTeleporting.compareAndSet(!value, value);
	}

	public final void setName(String name)
	{
		_name = name;
	}

	public Creature getCastingTarget()
	{
		return castingTarget.get();
	}

	public void setCastingTarget(Creature target)
	{
		if(target == null)
			castingTarget = HardReferences.emptyRef();
		else
			castingTarget = target.getRef();
	}

	public final void setRunning()
	{
		if(!_running)
		{
			_running = true;
			broadcastPacket(changeMovePacket());
		}
	}

	public void setSkillMastery(Integer skill, int mastery)
	{
		if(_skillMastery == null)
			_skillMastery = new HashMap<Integer, Integer>();
		_skillMastery.put(skill, mastery);
	}

	public void setAggressionTarget(Creature target)
	{
		if(target == null)
			_aggressionTarget = HardReferences.emptyRef();
		else
			_aggressionTarget = target.getRef();
	}

	public Creature getAggressionTarget()
	{
		return _aggressionTarget.get();
	}

	public void setTarget(GameObject object)
	{
		if(object != null && !object.isVisible())
		{
			if(isInOlympiadMode())
			{
				System.out.println("problem 6 + "+object.getState0()+" from creature");	
				object.toggleVisible0();
				System.out.println("problem 7 + "+object.getState0()+" from creature");	
				setTarget(this);
				return;	
			}	
			else		
				object = null;
		}

		if(object == null)
			target = HardReferences.emptyRef();		
			
		else
			target = object.getRef();			
	}

	public void setTitle(String title)
	{
		_title = title;
	}

	public void setWalking()
	{
		if(_running)
		{
			_running = false;
			broadcastPacket(changeMovePacket());
		}
	}

	protected L2GameServerPacket changeMovePacket()
	{
		return new ChangeMoveTypePacket(this);
	}

	public void startAbnormalEffect(AbnormalEffect ae)
	{
		if(ae == AbnormalEffect.NONE)
			return;

		if(ae != AbnormalEffect.NAVIT_ADVENT) // Игнорируем эффекты, которые вызывают крит ГоД клиента.
			_abnormalEffects.add(ae);

		if(ae.isSpecial())
			_abnormalEffectsMask2 |= ae.getMask();
		else if(ae.isEvent())
			_abnormalEffectsMask3 |= ae.getMask();
		else
			_abnormalEffectsMask |= ae.getMask();

		sendChanges();
	}

	public void startAttackStanceTask()
	{
		startAttackStanceTask0();
	}

	/**
	 * Запускаем задачу анимации боевой позы. Если задача уже запущена, увеличиваем время, которое персонаж будет в боевой позе на 15с
	 */
	protected void startAttackStanceTask0()
	{
		// предыдущая задача еще не закончена, увеличиваем время
		if(isInCombat())
		{
			_stanceEndTime = System.currentTimeMillis() + 15000L;
			return;
		}

		_stanceEndTime = System.currentTimeMillis() + 15000L;

		broadcastPacket(new AutoAttackStartPacket(getObjectId()));

		// отменяем предыдущую
		final Future<?> task = _stanceTask;
		if(task != null)
			task.cancel(false);

		// Добавляем задачу, которая будет проверять, если истекло время нахождения персонажа в боевой позе,
		// отменяет задачу и останаливает анимацию.
		_stanceTask = LazyPrecisionTaskManager.getInstance().scheduleAtFixedRate(_stanceTaskRunnable == null ? _stanceTaskRunnable = new AttackStanceTask() : _stanceTaskRunnable, 1000L, 1000L);
	}

	/**
	 * Останавливаем задачу анимации боевой позы.
	 */
	public void stopAttackStanceTask()
	{
		_stanceEndTime = 0L;

		final Future<?> task = _stanceTask;
		if(task != null)
		{
			task.cancel(false);
			_stanceTask = null;

			broadcastPacket(new AutoAttackStopPacket(getObjectId()));
		}
	}

	private class AttackStanceTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			if(!isInCombat())
				stopAttackStanceTask();
		}
	}

	/**
	 * Остановить регенерацию
	 */
	protected void stopRegeneration()
	{
		regenLock.lock();
		try
		{
			if(_isRegenerating)
			{
				_isRegenerating = false;

				if(_regenTask != null)
				{
					_regenTask.cancel(false);
					_regenTask = null;
				}
			}
		}
		finally
		{
			regenLock.unlock();
		}
	}

	/**
	 * Запустить регенерацию
	 */
	protected void startRegeneration()
	{
		if(!isVisible() || isDead() || getRegenTick() == 0L)
			return;

		if(_isRegenerating)
			return;

		regenLock.lock();
		try
		{
			if(!_isRegenerating)
			{
				_isRegenerating = true;
				_regenTask = RegenTaskManager.getInstance().scheduleAtFixedRate(_regenTaskRunnable == null ? _regenTaskRunnable = new RegenTask() : _regenTaskRunnable, 0, getRegenTick());
			}
		}
		finally
		{
			regenLock.unlock();
		}
	}

	public long getRegenTick()
	{
		return 3000L;
	}

	private class RegenTask implements Runnable
	{
		@Override
		public void run()
		{
			if(isAlikeDead() || getRegenTick() == 0L)
				return;

			double hpStart = _currentHp;
			double mpStart = _currentMp;
			double cpStart = _currentCp;

			int maxHp = getMaxHp();
			int maxMp = getMaxMp();
			int maxCp = isPlayer() ? getMaxCp() : 0;

			double addHp = 0.;
			double addMp = 0.;

			regenLock.lock();
			try
			{
				if(_currentHp < maxHp)
					addHp += Formulas.calcHpRegen(Creature.this);

				if(_currentMp < maxMp)
					addMp += Formulas.calcMpRegen(Creature.this);

				// Added regen bonus when character is sitting
				if(isPlayer() && Config.REGEN_SIT_WAIT)
				{
					Player pl = (Player) Creature.this;
					if(pl.isSitting())
					{
						pl.updateWaitSitTime();
						if(pl.getWaitSitTime() > 5)
						{
							addHp += pl.getWaitSitTime();
							addMp += pl.getWaitSitTime();
						}
					}
				}
				else if(isRaid())
				{
					addHp *= Config.RATE_RAID_REGEN;
					addMp *= Config.RATE_RAID_REGEN;
				}

				_currentHp += Math.max(0, Math.min(addHp, calcStat(Stats.HP_LIMIT, null, null) * maxHp / 100. - _currentHp));
				_currentMp += Math.max(0, Math.min(addMp, calcStat(Stats.MP_LIMIT, null, null) * maxMp / 100. - _currentMp));

				_currentHp = Math.min(maxHp, _currentHp);
				_currentMp = Math.min(maxMp, _currentMp);

				getListeners().onChangeCurrentHp(hpStart, _currentHp);
				getListeners().onChangeCurrentMp(mpStart, _currentMp);

				if(isPlayer())
				{
					_currentCp += Math.max(0, Math.min(Formulas.calcCpRegen(Creature.this), calcStat(Stats.CP_LIMIT, null, null) * maxCp / 100. - _currentCp));
					_currentCp = Math.min(maxCp, _currentCp);
					getListeners().onChangeCurrentCp(cpStart, _currentCp);
				}

				//отрегенились, останавливаем задачу
				if(_currentHp == maxHp && _currentMp == maxMp && _currentCp == maxCp)
					stopRegeneration();
			}
			finally
			{
				regenLock.unlock();
			}

			broadcastStatusUpdate();
			sendChanges();

			checkHpMessages(hpStart, _currentHp);
		}
	}

	public void stopAbnormalEffect(AbnormalEffect ae)
	{
		_abnormalEffects.remove(ae);

		if(ae.isSpecial())
			_abnormalEffectsMask2 &= ~ae.getMask();
		if(ae.isEvent())
			_abnormalEffectsMask3 &= ~ae.getMask();
		else
			_abnormalEffectsMask &= ~ae.getMask();

		sendChanges();
	}

	public final void stopAllAbnormalEffects()
	{
		_abnormalEffects.clear();

		_abnormalEffectsMask = 0;
		_abnormalEffectsMask2 = 0;
		_abnormalEffectsMask3 = 0;

		sendChanges();
	}

	/**
	 * Блокируем персонажа
	 */
	public void block()
	{
		_blocked = true;
	}

	/**
	 * Разблокируем персонажа
	 */
	public void unblock()
	{
		_blocked = false;
	}

	@Override
	public boolean isInvisible(GameObject observer)
	{
		if(observer != null && getObjectId() == observer.getObjectId())
			return false;

		for(Event event : getEvents())
		{
			Boolean result = event.isInvisible(this, observer);
			if(result != null)
				return result;
		}
		return getFlags().getInvisible().get();
	}

	public boolean startInvisible(Object owner, boolean withServitors)
	{
		boolean result;
		if(owner == null)
			result = getFlags().getInvisible().start();
		else
			result = getFlags().getInvisible().start(owner);

		if(result)
		{
			for(Player p : World.getAroundPlayers(this))
			{
				if(isInvisible(p))
					p.sendPacket(p.removeVisibleObject(this, null));
			}

			if(withServitors)
			{
				Servitor servitor = getServitor();
				if(servitor != null)
					servitor.startInvisible(owner, false);
			}
		}
		return result;
	}

	public final boolean startInvisible(boolean withServitors)
	{
		return startInvisible(null, withServitors);
	}

	public boolean stopInvisible(Object owner, boolean withServitors)
	{
		boolean result;
		if(owner == null)
			result = getFlags().getInvisible().stop();
		else
			result = getFlags().getInvisible().stop(owner);

		if(result)
		{
			List<Player> players = World.getAroundPlayers(this);
			for(Player p : players)
			{
				if(isVisible() && !isInvisible(p))
					p.sendPacket(p.addVisibleObject(this, null));
			}

			if(withServitors)
			{
				Servitor servitor = getServitor();
				if(servitor != null)
					servitor.stopInvisible(owner, false);
			}
		}
		return result;
	}

	public final boolean stopInvisible(boolean withServitors)
	{
		return stopInvisible(null, withServitors);
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startConfused()
	{
		return _confused.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopConfused()
	{
		return _confused.setAndGet(false);
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startFear()
	{
		return _afraid.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopFear()
	{
		return _afraid.setAndGet(false);
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startMuted()
	{
		return _muted.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopMuted()
	{
		return _muted.setAndGet(false);
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startPMuted()
	{
		return _pmuted.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopPMuted()
	{
		return _pmuted.setAndGet(false);
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startAMuted()
	{
		return _amuted.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopAMuted()
	{
		return _amuted.setAndGet(false);
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startRooted()
	{
		return _rooted.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopRooted()
	{
		return _rooted.setAndGet(false);
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startSleeping()
	{
		return _sleeping.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopSleeping()
	{
		return _sleeping.setAndGet(false);
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startStunning()
	{
		return _stunned.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopStunning()
	{
		return _stunned.setAndGet(false);
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startParalyzed()
	{
		return _paralyzed.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopParalyzed()
	{
		return _paralyzed.setAndGet(false);
	}
	public void startEffect()
	{
		if(isPlayer() && getPlayer() != null)
			getPlayer().startAbnormalEffect(AbnormalEffect.FLOATING_ROOT);
	}
	
	
	public void stopEffect()
	{
		if(isPlayer() && getPlayer() != null)
			getPlayer().stopAbnormalEffect(AbnormalEffect.FLOATING_ROOT);
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startImmobilized()
	{
		return _immobilized.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopImmobilized()
	{
		return _immobilized.setAndGet(false);
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startHealBlocked()
	{
		return _healBlocked.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopHealBlocked()
	{
		return _healBlocked.setAndGet(false);
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startDamageBlocked()
	{
		return _damageBlocked.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopDamageBlocked()
	{
		return _damageBlocked.setAndGet(false);
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startBuffImmunity()
	{
		return _buffImmunity.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopBuffImmunity()
	{
		return _buffImmunity.setAndGet(false);
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startDebuffImmunity()
	{
		return _debuffImmunity.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopDebuffImmunity()
	{
		return _debuffImmunity.setAndGet(false);
	}

	/**
	 *
	 * @return предыдущее состояние
	 */
	public boolean startEffectImmunity()
	{
		return _effectImmunity.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopEffectImmunity()
	{
		return _effectImmunity.setAndGet(false);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean startWeaponEquipBlocked()
	{
		return _weaponEquipBlocked.getAndSet(true);
	}

	/**
	 *
	 * @return текущее состояние
	 */
	public boolean stopWeaponEquipBlocked()
	{
		return _weaponEquipBlocked.getAndSet(false);
	}

	public boolean startFrozen()
	{
		return _frozen.getAndSet(true);
	}

	public boolean stopFrozen()
	{
		return _frozen.setAndGet(false);
	}

	public boolean isUndying()
	{
		return getFlags().getUndying().get();
	}

	public void setFakeDeath(boolean value)
	{
		_fakeDeath = value;
	}

	public void breakFakeDeath()
	{
		getEffectList().stopAllSkillEffects(EffectType.FakeDeath);
	}

	public void setMeditated(boolean value)
	{
		_meditated = value;
	}

	public final void setIsBlessedByNoblesse(boolean value)
	{
		_isBlessedByNoblesse = value;
	}

	public final void setIsSalvation(boolean value)
	{
		_isSalvation = value;
	}

	public void setIsInvul(boolean value)
	{
		_isInvul = value;
	}

	public void setLockedTarget(boolean value)
	{
		_lockedTarget = value;
	}

	public boolean isConfused()
	{
		return _confused.get();
	}

	public boolean isFakeDeath()
	{
		return _fakeDeath;
	}

	public boolean isAfraid()
	{
		return _afraid.get();
	}

	public boolean isBlocked()
	{
		return _blocked;
	}

	public boolean isMuted(Skill skill)
	{
		if(skill == null || skill.isNotAffectedByMute())
			return false;
		return isMMuted() && skill.isMagic() || isPMuted() && !skill.isMagic();
	}

	public boolean isPMuted()
	{
		return _pmuted.get();
	}

	public boolean isMMuted()
	{
		return _muted.get();
	}

	public boolean isAMuted()
	{
		return _amuted.get();
	}

	public boolean isRooted()
	{
		return _rooted.get();
	}

	public boolean isSleeping()
	{
		return _sleeping.get();
	}

	public boolean isStunned()
	{
		return _stunned.get();
	}

	public boolean isMeditated()
	{
		return _meditated;
	}

	public boolean isWeaponEquipBlocked()
	{
		return _weaponEquipBlocked.get();
	}

	public boolean isParalyzed()
	{
		return _paralyzed.get();
	}

	public boolean isFrozen()
	{
		return _frozen.get();
	}

	public boolean isImmobilized()
	{
		return _immobilized.get() || getRunSpeed() < 1 || getFlags().getImmobilized().get();
	}

	public boolean isHealBlocked()
	{
		return isAlikeDead() || _healBlocked.get();
	}

	public boolean isDamageBlocked()
	{
		return isInvul() || _damageBlocked.get();
	}

	public boolean isCastingNow()
	{
		return _skillTask != null;
	}

	public boolean isLockedTarget()
	{
		return _lockedTarget;
	}

	public boolean isMovementDisabled()
	{
		if(isBlocked() || isRooted() || isImmobilized() || isAlikeDead() || isStunned() || isSleeping() || isParalyzed() || isAttackingNow() || isCastingNow() || isFrozen())
			return true;
		
		if(isCastingNow())
		{
			final Skill skill = getCastingSkill();
			if (skill != null && skill.stopActor())
				return true;
		}
		return false;		
	}

	public boolean isActionsDisabled()
	{
		return isBlocked() || isAlikeDead() || isStunned() || isSleeping() || isParalyzed() || isAttackingNow() || isCastingNow() || isFrozen();
	}
	
	public boolean isUseItemDisabled()
	{
		return isAlikeDead() || isStunned() || isSleeping() || isParalyzed() || isFrozen();
	}
	
	public final boolean isAttackingDisabled()
	{
		return _attackReuseEndTime > System.currentTimeMillis();
	}

	public boolean isOutOfControl()
	{
		return isBlocked() || isConfused() || isAfraid();
	}

	public void checkAndRemoveInvisible()
	{
		getEffectList().stopEffects(EffectType.Invisible);
	}
	
	@Override
	public void teleToLocation(Location loc)
	{
		teleToLocation(loc.x, loc.y, loc.z, getReflection());
	}

	public void teleToLocation(Location loc, Reflection r)
	{
		if(loc == null)
			teleToLocation(-80684, 149770, -3040, r); //to gludin
		else	
			teleToLocation(loc.x, loc.y, loc.z, r);
	}

	public void teleToLocation(int x, int y, int z)
	{
		teleToLocation(x, y, z, getReflection());
	}

	public void teleToLocation(Location location, int min, int max)
	{
		teleToLocation(Location.findAroundPosition(location, min, max, 0), getReflection());
	}

	public void teleToLocation(int x, int y, int z, Reflection r)
	{
		if(!isTeleporting.compareAndSet(false, true))
			return;

		if(isFakeDeath())
			breakFakeDeath();

		abortCast(true, false);
		if(!isLockedTarget())
			setTarget(null);
		stopMove();

		if(!isBoat() && !isFlying() && !World.isWater(new Location(x, y, z), r))
			z = GeoEngine.getLowerHeight(x, y, z, r.getGeoIndex());

		//TODO [G1ta0] убрать DimensionalRiftManager.teleToLocation
		if(isPlayer() && DimensionalRiftManager.getInstance().checkIfInRiftZone(getLoc(), true))
		{
			Player player = (Player) this;
			if(player.isInParty() && player.getParty().isInDimensionalRift())
			{
				Location newCoords = DimensionalRiftManager.getInstance().getRoom(0, 0).getTeleportCoords();
				x = newCoords.x;
				y = newCoords.y;
				z = newCoords.z;
				player.getParty().getDimensionalRift().usedTeleport(player);
			}
		}

		if(isPlayer())
		{
			Player player = (Player) this;

			if(!isHFClient())
				sendPacket(new TeleportToLocationPacket(player, x, y, z));

			player.getListeners().onTeleport(x, y, z, r);

			decayMe();

			setXYZ(x, y, z);

			setReflection(r);

			if(isHFClient())
				player.sendPacket(new TeleportToLocationPacket(player, x, y, z));
			else
				player.sendPacket(new ExTeleportToLocationActivate(player, x, y, z));
			
			if (player.getServitor() != null)
				player.getServitor().teleportToOwner();

			if(isFakePlayer())
				onTeleported();
		}
		else
		{
			if(!isHFClient())
				broadcastPacket(new TeleportToLocationPacket(this, x, y, z));

			setXYZ(x, y, z);

			setReflection(r);

			if(isHFClient())
				broadcastPacket(new TeleportToLocationPacket(this, x, y, z));
			else
				sendPacket(new ExTeleportToLocationActivate(this, x, y, z));

			onTeleported();
		}
	}

	public boolean onTeleported()
	{
		return isTeleporting.compareAndSet(true, false);
	}

	public void sendMessage(CustomMessage message)
	{

	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + getObjectId() + "]";
	}

	@Override
	public double getCollisionRadius()
	{
		return getTemplate().getCollisionRadius();
	}

	@Override
	public double getCollisionHeight()
	{
		return getTemplate().getCollisionHeight();
	}

	public EffectList getEffectList()
	{
		if(_effectList == null)
			synchronized (this)
			{
				if(_effectList == null)
					_effectList = new EffectList(this);
			}

		return _effectList;
	}

	public boolean paralizeOnAttack(Creature attacker)
	{
		int max_attacker_level = 0xFFFF;

		MonsterInstance leader;
		if(isRaid() || isMinion() && (leader = ((MinionInstance) this).getLeader()) != null && leader.isRaid())
			max_attacker_level = getLevel() + Config.RAID_MAX_LEVEL_DIFF;
		else if(isNpc())
		{
			int max_level_diff = ((NpcInstance) this).getParameter("ParalizeOnAttack", -1000);
			if(max_level_diff != -1000)
				max_attacker_level = getLevel() + max_level_diff;
		}

		if(attacker.getLevel() > max_attacker_level)
			return true;

		return false;
	}

	@Override
	protected void onDelete()
	{
		CharacterAI ai = getAI();
		if(ai != null)
		{
			ai.stopAllTaskAndTimers();
			ai.notifyEvent(CtrlEvent.EVT_DELETE);
		}

		GameObjectsStorage.remove(_storedId);

		getEffectList().stopAllEffects();

		super.onDelete();
	}

	// ---------------------------- Not Implemented -------------------------------

	public void addExpAndSp(long exp, long sp)
	{}

	public void broadcastCharInfo()
	{}

	public void broadcastCharInfoImpl(IUpdateTypeComponent... components)
	{}

	public void checkHpMessages(double currentHp, double newHp)
	{}

	public boolean checkPvP(Creature target, Skill skill)
	{
		return false;
	}

	public boolean consumeItem(int itemConsumeId, long itemCount)
	{
		return true;
	}

	public boolean haveItemForConsume(int itemConsumeId, long itemCount)
	{
		return true;
	}

	public boolean consumeItemMp(int itemId, int mp)
	{
		return true;
	}

	public boolean isFearImmune()
	{
		return isPeaceNpc();
	}

	public boolean isLethalImmune()
	{
		return isBoss() || isRaid();
	}

	public boolean getChargedSoulShot()
	{
		return false;
	}

	public int getChargedSpiritShot()
	{
		return 0;
	}

	public int getIncreasedForce()
	{
		return 0;
	}

	public int getConsumedSouls()
	{
		return 0;
	}

	public int getAgathionEnergy()
	{
		return 0;
	}

	public void setAgathionEnergy(int val)
	{
		//
	}

	public int getKarma()
	{
		return 0;
	}

	public boolean isPK()
	{
		return getKarma() > 0;
	}

	public double getLevelMod()
	{
		return 1;
	}

	public int getNpcId()
	{
		return 0;
	}

	public boolean isMyServitor(int objId)
	{
		return false;
	}

	public Servitor getServitor()
	{
		return null;
	}

	public int getPvpFlag()
	{
		return 0;
	}

	public void setTeam(TeamType t)
	{
		_team = t;
		sendChanges();
	}

	public TeamType getTeam()
	{
		return _team;
	}

	public boolean isUndead()
	{
		return false;
	}

	public boolean isParalyzeImmune()
	{
		return false;
	}

	public void reduceArrowCount()
	{}

	public void sendChanges()
	{
		getStatsRecorder().sendChanges();
	}

	public void sendMessage(String message)
	{}

	public void sendPacket(IStaticPacket mov)
	{}

	public void sendPacket(IStaticPacket... mov)
	{}

	public void sendPacket(List<? extends IStaticPacket> mov)
	{}

	public void setIncreasedForce(int i)
	{}

	public void setConsumedSouls(int i, NpcInstance monster)
	{}

	public void startPvPFlag(Creature target)
	{}

	public boolean unChargeShots(boolean spirit)
	{
		return false;
	}

	public void updateEffectIcons()
	{}

	/**
	 * Выставить предельные значения HP/MP/CP и запустить регенерацию, если в этом есть необходимость
	 */
	protected void refreshHpMpCp()
	{
		final int maxHp = getMaxHp();
		final int maxMp = getMaxMp();
		final int maxCp = isPlayer() ? getMaxCp() : 0;

		if(_currentHp > maxHp)
			setCurrentHp(maxHp, false);
		if(_currentMp > maxMp)
			setCurrentMp(maxMp, false);
		if(_currentCp > maxCp)
			setCurrentCp(maxCp, false);

		if(_currentHp < maxHp || _currentMp < maxMp || _currentCp < maxCp)
			startRegeneration();
	}

	public void updateStats()
	{
		refreshHpMpCp();
		sendChanges();
	}

	public void setOverhitAttacker(Creature attacker)
	{}

	public void setOverhitDamage(double damage)
	{}

	public boolean isCursedWeaponEquipped()
	{
		return false;
	}

	public boolean isHero()
	{
		return false;
	}

	public int getAccessLevel()
	{
		return 0;
	}

	public Clan getClan()
	{
		return null;
	}

	public double getRateAdena()
	{
		return 1.;
	}

	public double getRateItems()
	{
		return 1.;
	}

	public double getRateExp()
	{
		return 1.;
	}

	public double getRateSp()
	{
		return 1.;
	}

	public double getRateSpoil()
	{
		return 1.;
	}

	public int getFormId()
	{
		return 0;
	}

	public boolean isNameAbove()
	{
		return true;
	}

	@Override
	public boolean setLoc(Location loc)
	{
		return setXYZ(loc.x, loc.y, loc.z);
	}

	public boolean setLoc(Location loc, boolean MoveTask)
	{
		return setXYZ(loc.x, loc.y, loc.z, MoveTask);
	}

	@Override
	public boolean setXYZ(int x, int y, int z)
	{
		return setXYZ(x, y, z, false);
	}

	public boolean setXYZ(int x, int y, int z, boolean MoveTask)
	{
		if(!MoveTask)
			stopMove();

		moveLock.lock();
		try
		{
			if(!super.setXYZ(x, y, z))
				return false;
		}
		finally
		{
			moveLock.unlock();
		}

		updateZones();
		return true;
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();

		updateStats();
		updateZones();
	}

	@Override
	public void spawnMe(Location loc)
	{
		if(loc.h > 0)
			setHeading(loc.h);
		super.spawnMe(loc);
	}

	@Override
	protected void onDespawn()
	{
		if(!isLockedTarget())
			setTarget(null);
		stopMove();
		stopAttackStanceTask();
		stopRegeneration();

		updateZones();
		clearStatusListeners();

		super.onDespawn();
	}

	public final void doDecay()
	{
		if(!isDead())
			return;

		onDecay();
	}

	protected void onDecay()
	{
		decayMe();
	}

	// Функция для дизактивации умений персонажа (если умение не активно, то он не дает статтов и имеет серую иконку).
	private TIntHashSet _unActiveSkills = new TIntHashSet();

	public void addUnActiveSkill(Skill skill)
	{
		if(skill == null || isUnActiveSkill(skill.getId()))
			return;

		removeStatsOwner(skill);
		removeTriggers(skill);

		_unActiveSkills.add(skill.getId());
	}

	public void removeUnActiveSkill(Skill skill)
	{
		if(skill == null || !isUnActiveSkill(skill.getId()))
			return;

		addStatFuncs(skill.getStatFuncs());
		addTriggers(skill);

		_unActiveSkills.remove(skill.getId());
	}

	public boolean isUnActiveSkill(int id)
	{
		return _unActiveSkills.contains(id);
	}

	public abstract int getLevel();

	public abstract ItemInstance getActiveWeaponInstance();

	public abstract WeaponTemplate getActiveWeaponTemplate();

	public abstract ItemInstance getSecondaryWeaponInstance();

	public abstract WeaponTemplate getSecondaryWeaponTemplate();

	public WeaponType getBaseAttackType()
	{
		if(getActiveWeaponTemplate() != null)
			return getActiveWeaponTemplate().getItemType();

		return getTemplate().getBaseAttackType();
	}

	public CharListenerList getListeners()
	{
		if(listeners == null)
			synchronized (this)
			{
				if(listeners == null)
					listeners = new CharListenerList(this);
			}
		return listeners;
	}

	public <T extends Listener<Creature>> boolean addListener(T listener)
	{
		return getListeners().add(listener);
	}

	public <T extends Listener<Creature>> boolean removeListener(T listener)
	{
		return getListeners().remove(listener);
	}

	public CharStatsChangeRecorder<? extends Creature> getStatsRecorder()
	{
		if(_statsRecorder == null)
			synchronized (this)
			{
				if(_statsRecorder == null)
					_statsRecorder = new CharStatsChangeRecorder<Creature>(this);
			}

		return _statsRecorder;
	}

	@Override
	public boolean isCreature()
	{
		return true;
	}

	public void displayGiveDamageMessage(Creature target, int damage, boolean crit, boolean miss, boolean shld, boolean magic)
	{
		if(miss && target.isPlayer() && !target.isDamageBlocked())
			target.sendPacket(new SystemMessage(SystemMessage.C1_HAS_EVADED_C2S_ATTACK).addName(target).addName(this));

		if(Config.ENABLE_DAM_ON_SCREEN)
		{
			sendPacket(new DamageTextPacket(target.getObjectId(), damage, crit, miss, shld, magic, Config.DAM_ON_SCREEN_FONT, Config.DAM_ON_SCREEN_FONT_COLOR_ATTACKER, "", "", 0,0,0,0));
			target.sendPacket(new DamageTextPacket(target.getObjectId(), damage, crit, miss, shld, magic, Config.DAM_ON_SCREEN_FONT, Config.DAM_ON_SCREEN_FONT_COLOR_TARGET, "", "", 0,0,0,0));      
		}
	}

	public void displayReceiveDamageMessage(Creature attacker, int damage)
	{
		//
	}

	public Collection<TimeStamp> getSkillReuses()
	{
		return _skillReuses.valueCollection();
	}

	public TimeStamp getSkillReuse(Skill skill)
	{
		return _skillReuses.get(skill.getReuseHash());
	}

	public boolean isDeathImmune()
	{
		return _deathImmunity.get() || isPeaceNpc();
	}

	public boolean startDeathImmunity()
	{
		return _deathImmunity.getAndSet(true);
	}

	public boolean stopDeathImmunity()
	{
		return _deathImmunity.setAndGet(false);
	}

	public int getMoveTickInterval()
	{
		return (isPlayer() ? 16000 : 32000) / Math.max(getMoveSpeed(), 1);
	}

	public double getMovementSpeedMultiplier()
	{
		return getRunSpeed() * 1. / _template.getBaseRunSpd();
	}

	@Override
	public int getMoveSpeed()
	{
		if(isRunning())
			return getRunSpeed();

		return getWalkSpeed();
	}

	public int getRunSpeed()
	{
		if(isInWater())
			return getSwimRunSpeed();

		return getSpeed(_template.getBaseRunSpd());
	}

	public final int getWalkSpeed()
	{
		if(isInWater())
			return getSwimWalkSpeed();

		return getSpeed(_template.getBaseWalkSpd());
	}

	public final int getSwimRunSpeed()
	{
		return getSpeed(_template.getBaseWaterRunSpd());
	}

	public final int getSwimWalkSpeed()
	{
		return getSpeed(_template.getBaseWaterWalkSpd());
	}

	public double relativeSpeed(GameObject target)
	{
		return getMoveSpeed() - target.getMoveSpeed() * Math.cos(headingToRadians(getHeading()) - headingToRadians(target.getHeading()));
	}

	public int getSpeed(double baseSpeed)
	{
		return (int) calcStat(Stats.RUN_SPEED, baseSpeed, null, null);
	}

	public boolean isInTvT()
	{
		return false;
	}

	public boolean isInZombieVsHumans()
	{
		return false;
	}
	
	public boolean isInCtF()
	{
		return false;
	}
	
	public boolean isInLastHero() 
	{
		return false;
	}	

	public boolean isInHunterClub()
	{
		return false;
	}
	
	public boolean isInMassPvp()
	{
		return false;
	}	
	
	public boolean isInCrumaRace()
	{
		return false;
	}

	public boolean isInEventModelEvent()
	{
		return false;
	}

	public CreatureFlags getFlags()
	{
		if(_statuses == null)
			_statuses = new CreatureFlags(this);
		return _statuses;
	}

	public void onEvtTimer(int timerId, Object arg1, Object arg2)
	{
		//
	}

	public void onEvtScriptEvent(String event, Object arg1, Object arg2)
	{
		//
	}
	
	public double getEnchantedDefense()
	{
		return _enchantedDef;
	}
	
	public void setEnchantedDeffense(double dff)
	{
		_enchantedDef = dff;
	}

	public boolean isPeaceNpc()
	{
		return false;
	}

	public boolean checkInteractionDistance(GameObject target)
	{
		return getRealDistance3D(target) < INTERACTION_DISTANCE;
	}

	public boolean isFakePlayer()
	{
		return false;
	}

	public boolean canConsumeSkillItem(Skill skill)
	{
		return false;
	}

	@Override
	protected Shape makeGeoShape()
	{
		int x = getX();
		int y = getY();
		int z = getZ();
		Circle circle = new Circle(x, y, (int) getCollisionRadius());
		circle.setZmin(z - Config.MAX_Z_DIFF);
		circle.setZmax(z + (int) getCollisionHeight());
		return circle;
	}

	public boolean isHFClient()
	{
		return true;
	}
}
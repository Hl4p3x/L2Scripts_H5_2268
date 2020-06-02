package l2s.gameserver.model.instances;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.iterator.TIntObjectIterator;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.geometry.Circle;
import l2s.commons.geometry.Shape;
import l2s.commons.lang.reference.HardReference;
import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.NpcAI;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.QuestHolder;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.data.xml.holder.SkillAcquireHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.handler.bypass.BypassHolder;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.instancemanager.DimensionalRiftManager;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.listener.NpcListener;
import l2s.gameserver.listener.hooks.ListenerHook;
import l2s.gameserver.listener.hooks.ListenerHookType;
import l2s.gameserver.model.AggroList;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObjectTasks.NotifyAITask;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.MinionList;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.SkillLearn;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.TeleportLocation;
import l2s.gameserver.model.World;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.actor.listener.NpcListenerList;
import l2s.gameserver.model.actor.recorder.NpcStatsChangeRecorder;
import l2s.gameserver.model.base.AcquireType;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.DimensionalRift;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.SevenSigns;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.objects.TerritoryWardObject;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.entity.residence.Dominion;
import l2s.gameserver.model.entity.residence.Fortress;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.SubUnit;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestEventType;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.AcquireSkillDonePacket;
import l2s.gameserver.network.l2.s2c.AutoAttackStartPacket;
import l2s.gameserver.network.l2.s2c.ExAcquirableSkillListByClass;
import l2s.gameserver.network.l2.s2c.ExChangeNPCState;
import l2s.gameserver.network.l2.s2c.ExShowBaseAttributeCancelWindow;
import l2s.gameserver.network.l2.s2c.ExShowVariationCancelWindow;
import l2s.gameserver.network.l2.s2c.ExShowVariationMakeWindow;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;
import l2s.gameserver.network.l2.s2c.NpcInfoPacket;
import l2s.gameserver.network.l2.s2c.NpcInfoState;
import l2s.gameserver.network.l2.s2c.SocialActionPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.network.l2.s2c.updatetype.IUpdateTypeComponent;
import l2s.gameserver.network.l2.s2c.updatetype.NpcInfoType;
import l2s.gameserver.scripts.Events;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.taskmanager.DecayTaskManager;
import l2s.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.templates.item.data.ItemData;
import l2s.gameserver.templates.npc.Faction;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.templates.spawn.SpawnRange;
import l2s.gameserver.utils.CertificationFunctions;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.ReflectionUtils;

public class NpcInstance extends Creature
{
	public static final String NO_CHAT_WINDOW = "noChatWindow";
	public static final String NO_RANDOM_WALK = "noRandomWalk";
	public static final String NO_RANDOM_ANIMATION = "noRandomAnimation";
	public static final String TARGETABLE = "TargetEnabled";
	public static final String SHOW_NAME = "showName";

	private static final Logger _log = LoggerFactory.getLogger(NpcInstance.class);

	private int _personalAggroRange = -1;
	private int _level = 0;

	private long _dieTime = 0L;


	protected int _spawnAnimation = 2;

	private int _currentLHandId;
	private int _currentRHandId;

	private double _currentCollisionRadius;
	private double _currentCollisionHeight;

	private int npcState = 0;

	protected boolean _hasRandomAnimation;
	protected boolean _hasRandomWalk;
	protected boolean _hasChatWindow;

	private Future<?> _decayTask;
	private Future<?> _animationTask;

	private AggroList _aggroList;

	private boolean _isTargetable;

	private boolean _showName;

	private Castle _nearestCastle;
	private Fortress _nearestFortress;
	private ClanHall _nearestClanHall;
	private Dominion _nearestDominion;

	private NpcString _nameNpcString = NpcString.NONE;
	private NpcString _titleNpcString = NpcString.NONE;

	private Spawner _spawn;
	private Location _spawnedLoc = new Location();
	private SpawnRange _spawnRange;

	private MultiValueSet<String> _parameters = StatsSet.EMPTY;

	private boolean _geoControlEnabled;

	private final int _geoRadius;
	private final int _geoHeight;

	public NpcInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);

		if(template == null)
			throw new NullPointerException("No template for Npc. Please check your datapack is setup correctly.");

		setParameters(template.getAIParams());

		_hasRandomAnimation = !getParameter(NO_RANDOM_ANIMATION, false) && Config.MAX_NPC_ANIMATION > 0;
		_hasRandomWalk = !getParameter(NO_RANDOM_WALK, false);
		setHasChatWindow(!getParameter(NO_CHAT_WINDOW, false));
		setTargetable(getParameter(TARGETABLE, true));
		setShowName(getParameter(SHOW_NAME, true));

		if(template.getSkills().size() > 0)
			for(TIntObjectIterator<Skill> iterator = template.getSkills().iterator(); iterator.hasNext();)
			{
				iterator.advance();
				addSkill(iterator.value());
			}

		setName(template.name);
		String customTitle = template.title;
		if(this.isMonster() && Config.ALT_SHOW_MONSTERS_LVL)
		{
			customTitle = "LvL: " + this.getLevel();
			if(Config.ALT_SHOW_MONSTERS_AGRESSION && this.isAggressive())
				customTitle += " A";
		}
		setTitle(customTitle);

		// initialization parameters weapons
		setLHandId(getTemplate().lhand);
		setRHandId(getTemplate().rhand);

		// инициализация коллизий
		setCurrentCollisionHeight(getTemplate().getCollisionHeight());
		setCurrentCollisionRadius(getTemplate().getCollisionRadius());

		_aggroList = new AggroList(this);

		setFlying(getParameter("isFlying", false));

		_geoControlEnabled = getParameter("geodata_enabled", false);
		_geoRadius = getParameter("geodata_radius", (int) getTemplate().getCollisionRadius());
		_geoHeight = getParameter("geodata_height", (int) getTemplate().getCollisionHeight());
	}

	@SuppressWarnings("unchecked")
	@Override
	public HardReference<NpcInstance> getRef()
	{
		return (HardReference<NpcInstance>) super.getRef();
	}

	@Override
	public NpcAI getAI()
	{
		if(_ai == null)
			synchronized (this)
			{
				if(_ai == null)
					_ai = getTemplate().getNewAI(this);
			}

		return (NpcAI) _ai;
	}

	/**
	 * Return the position of the spawned point.<BR><BR>
	 * Может возвращать случайную точку, поэтому всегда следует кешировать результат вызова!
	 */
	public Location getSpawnedLoc()
	{
		return _spawnedLoc;
	}

	public void setSpawnedLoc(Location loc)
	{
		_spawnedLoc = loc;
	}

	public int getRightHandItem()
	{
		return _currentRHandId;
	}

	public int getLeftHandItem()
	{
		return _currentLHandId;
	}

	public void setLHandId(int newWeaponId)
	{
		_currentLHandId = newWeaponId;
	}

	public void setRHandId(int newWeaponId)
	{
		_currentRHandId = newWeaponId;
	}

	@Override
	public double getCurrentCollisionHeight()
	{
		return _currentCollisionHeight;
	}

	public void setCurrentCollisionHeight(double offset)
	{
		_currentCollisionHeight = offset;
	}

	@Override
	public double getCurrentCollisionRadius()
	{
		return _currentCollisionRadius;
	}

	public void setCurrentCollisionRadius(double collisionRadius)
	{
		_currentCollisionRadius = collisionRadius;
	}

	@Override
	protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp)
	{
		if(attacker.isPlayable())
			getAggroList().addDamageHate(attacker, (int) damage, 0);

		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
	}

	@Override
	protected void onDeath(Creature killer)
	{
		_dieTime = System.currentTimeMillis();

		if(isMonster() && (((MonsterInstance) this).isSeeded() || ((MonsterInstance) this).isSpoiled()))
			startDecay(20000L);
		else if(isBoss())
			startDecay(20000L);
		else if(isFlying())
			startDecay(4500L);
		else
			startDecay(8500L);

		// установка параметров оружия и коллизий по умолчанию
		setLHandId(getTemplate().lhand);
		setRHandId(getTemplate().rhand);
		setCurrentCollisionHeight(getTemplate().getCollisionHeight());
		setCurrentCollisionRadius(getTemplate().getCollisionRadius());

		getAI().stopAITask();
		stopRandomAnimation();

		for(ListenerHook hook : getTemplate().getListenerHooks(ListenerHookType.NPC_KILL))
		{
			if(killer != null && killer.getPlayer() != null)
			{
				hook.onNpcKill(this, killer.getPlayer());
			}
		}

		super.onDeath(killer);

		broadcastPacket(new NpcInfoState(this)); // TODO: Нужно ли здесь?
	}

	public long getDeadTime()
	{
		if(_dieTime <= 0L)
			return 0L;
		return System.currentTimeMillis() - _dieTime;
	}

	public AggroList getAggroList()
	{
		return _aggroList;
	}

	public MinionList getMinionList()
	{
		return null;
	}

	public boolean hasMinions()
	{
		return false;
	}

	public void dropItem(Player lastAttacker, int itemId, long itemCount)
	{
		if(itemCount == 0 || lastAttacker == null)
			return;

		ItemInstance item;

		for(long i = 0; i < itemCount; i++)
		{
			item = ItemFunctions.createItem(itemId);
			for(Event e : getEvents())
				item.addEvent(e);

			// Set the Item quantity dropped if L2ItemInstance is stackable
			if(item.isStackable())
			{
				i = itemCount; // Set so loop won't happent again
				item.setCount(itemCount); // Set item count
			}

			if(isRaid() || this instanceof ReflectionBossInstance)
			{
				SystemMessagePacket sm;
				if(itemId == 57)
				{
					sm = new SystemMessagePacket(SystemMsg.C1_HAS_DIED_AND_DROPPED_S2_ADENA);
					sm.addName(this);
					sm.addLong(item.getCount());
				}
				else
				{
					sm = new SystemMessagePacket(SystemMsg.C1_DIED_AND_DROPPED_S3_S2);
					sm.addName(this);
					sm.addItemName(itemId);
					sm.addLong(item.getCount());
				}
				broadcastPacket(sm);
			}

			lastAttacker.doAutoLootOrDrop(item, this);
		}
	}

	public void dropItem(Player lastAttacker, ItemInstance item)
	{
		if(item.getCount() == 0)
			return;

		if(isRaid() || this instanceof ReflectionBossInstance)
		{
			SystemMessagePacket sm;
			if(item.getItemId() == 57)
			{
				sm = new SystemMessagePacket(SystemMsg.C1_HAS_DIED_AND_DROPPED_S2_ADENA);
				sm.addName(this);
				sm.addLong(item.getCount());
			}
			else
			{
				sm = new SystemMessagePacket(SystemMsg.C1_DIED_AND_DROPPED_S3_S2);
				sm.addName(this);
				sm.addItemName(item.getItemId());
				sm.addLong(item.getCount());
			}
			broadcastPacket(sm);
		}

		lastAttacker.doAutoLootOrDrop(item, this);
	}

	@Override
	public boolean isAttackable(Creature attacker)
	{
		return true;
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();

		_dieTime = 0L;
		_spawnAnimation = 0;

		if(getAI().isGlobalAI() || getCurrentRegion() != null && getCurrentRegion().isActive())
		{
			getAI().startAITask();
			startRandomAnimation();
		}

		ThreadPoolManager.getInstance().execute(new NotifyAITask(this, CtrlEvent.EVT_SPAWN));

		getListeners().onSpawn();
	}

	@Override
	protected void onDespawn()
	{
		ThreadPoolManager.getInstance().execute(new NotifyAITask(this, CtrlEvent.EVT_DESPAWN));

		getAggroList().clear();

		getAI().onEvtDeSpawn();
		getAI().stopAITask();
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		stopRandomAnimation();

		super.onDespawn();
	}

	@Override
	public NpcTemplate getTemplate()
	{
		return (NpcTemplate) _template;
	}

	@Override
	public int getNpcId()
	{
		return getTemplate().npcId;
	}

	protected boolean _unAggred = false;

	public void setUnAggred(boolean state)
	{
		_unAggred = state;
	}

	/**
	 * Return True if the L2NpcInstance is aggressive (ex : L2MonsterInstance in function of aggroRange).<BR><BR>
	 */
	public boolean isAggressive()
	{
		return getAggroRange() > 0;
	}

	public int getAggroRange()
	{
		if(_unAggred)
			return 0;

		if(_personalAggroRange >= 0)
			return _personalAggroRange;

		return getTemplate().aggroRange;
	}

	/**
	 * Устанавливает данному npc новый aggroRange.
	 * Если установленый aggroRange < 0, то будет братся аггрорейндж с темплейта.
	 * @param aggroRange новый agrroRange
	 */
	public void setAggroRange(int aggroRange)
	{
		_personalAggroRange = aggroRange;
	}

	/**
	 * Возвращает группу социальности
	 */
	public Faction getFaction()
	{
		return getTemplate().getFaction();
	}

	public boolean isInFaction(NpcInstance npc)
	{
		return getFaction().equals(npc.getFaction()) && !getFaction().isIgnoreNpcId(npc.getNpcId());
	}	

	@Override
	public int getMAtk(Creature target, Skill skill)
	{
		return (int) (super.getMAtk(target, skill) * Config.ALT_NPC_MATK_MODIFIER);
	}

	@Override
	public int getPAtk(Creature target)
	{
		return (int) (super.getPAtk(target) * Config.ALT_NPC_PATK_MODIFIER);
	}

	@Override
	public int getMaxHp()
	{
		return (int) (super.getMaxHp() * Config.ALT_NPC_MAXHP_MODIFIER);
	}

	@Override
	public int getMaxMp()
	{
		return (int) (super.getMaxMp() * Config.ALT_NPC_MAXMP_MODIFIER);
	}

	public long getExpReward()
	{
		return (long) calcStat(Stats.EXP, getTemplate().rewardExp, null, null);
	}

	public long getSpReward()
	{
		return (long) calcStat(Stats.SP, getTemplate().rewardSp, null, null);
	}

	@Override
	protected void onDelete()
	{
		getAI().stopAllTaskAndTimers();

		stopDecay();
		if(_spawn != null)
			_spawn.stopRespawn();
		setSpawn(null);

		super.onDelete();
	}

	public Spawner getSpawn()
	{
		return _spawn;
	}

	public void setSpawn(Spawner spawn)
	{
		_spawn = spawn;
	}

	@Override
	protected void onDecay()
	{
		super.onDecay();

		_spawnAnimation = 2;

		if(_spawn != null)
			_spawn.decreaseCount(this);
		else
			deleteMe(); // Если этот моб заспавнен не через стандартный механизм спавна значит посмертие ему не положено и он умирает насовсем
	}

	/**
	 * Запустить задачу "исчезновения" после смерти
	 */
	protected void startDecay(long delay)
	{
		stopDecay();
		_decayTask = DecayTaskManager.getInstance().addDecayTask(this, delay);
	}

	/**
	 * Отменить задачу "исчезновения" после смерти
	 */
	public void stopDecay()
	{
		if(_decayTask != null)
		{
			_decayTask.cancel(false);
			_decayTask = null;
		}
	}

	/**
	 * Отменить и завершить задачу "исчезновения" после смерти
	 */
	public void endDecayTask()
	{
		if(_decayTask != null)
		{
			_decayTask.cancel(false);
			_decayTask = null;
		}
		doDecay();
	}

	@Override
	public boolean isUndead()
	{
		return getTemplate().isUndead();
	}

	public void setLevel(int level)
	{
		_level = level;
	}

	@Override
	public int getLevel()
	{
		return _level == 0 ? getTemplate().level : _level;
	}

	private int _displayId = 0;

	public void setDisplayId(int displayId)
	{
		_displayId = displayId;
	}

	public int getDisplayId()
	{
		return _displayId > 0 ? _displayId : getTemplate().displayId;
	}

	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		// regular NPCs dont have weapons instancies
		return null;
	}

	@Override
	public WeaponTemplate getActiveWeaponTemplate()
	{
		// Get the weapon identifier equipped in the right hand of the L2NpcInstance
		int weaponId = getTemplate().rhand;

		if(weaponId < 1)
			return null;

		// Get the weapon item equipped in the right hand of the L2NpcInstance
		ItemTemplate item = ItemHolder.getInstance().getTemplate(getTemplate().rhand);

		if(!(item instanceof WeaponTemplate))
			return null;

		return (WeaponTemplate) item;
	}

	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		// regular NPCs dont have weapons instances
		return null;
	}

	@Override
	public WeaponTemplate getSecondaryWeaponTemplate()
	{
		// Get the weapon identifier equipped in the right hand of the L2NpcInstance
		int weaponId = getTemplate().lhand;

		if(weaponId < 1)
			return null;

		// Get the weapon item equipped in the right hand of the L2NpcInstance
		ItemTemplate item = ItemHolder.getInstance().getTemplate(getTemplate().lhand);

		if(!(item instanceof WeaponTemplate))
			return null;

		return (WeaponTemplate) item;
	}

	@Override
	public void sendChanges()
	{
		if(isFlying()) // FIXME
			return;
		super.sendChanges();
	}

	private ScheduledFuture<?> _broadcastCharInfoTask;

	public void onMenuSelect(Player player, int ask, int reply)
	{
		if(!canBypassCheck(player, this))
			return;

		for(QuestState qs : player.getAllQuestsStates())
		{
			if(qs.getQuest().getId() == ask && !qs.isCompleted())
			{
				qs.getQuest().notifyMenuSelect(reply, qs, this);
				return;
			}
		}
	}

	public class BroadcastCharInfoTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			broadcastCharInfoImpl(NpcInfoType.VALUES);
			_broadcastCharInfoTask = null;
		}
	}

	@Override
	public void broadcastCharInfo()
	{
		if(!isVisible())
			return;

		if(_broadcastCharInfoTask != null)
			return;

		_broadcastCharInfoTask = ThreadPoolManager.getInstance().schedule(new BroadcastCharInfoTask(), Config.BROADCAST_CHAR_INFO_INTERVAL);
	}

	@Override
	public void broadcastCharInfoImpl(IUpdateTypeComponent... components)
	{
		if(components.length == 0)
		{
			_log.warn(getClass().getSimpleName() + ": Trying broadcast char info without components!", new Exception());
			return;
		}

		for(Player player : World.getAroundPlayers(this))
			player.sendPacket(new NpcInfoPacket(this, player).update(components));
	}

	// У NPC всегда 2
	public void onRandomAnimation()
	{
		if(System.currentTimeMillis() - _lastSocialAction > 10000L)
		{
			broadcastPacket(new SocialActionPacket(getObjectId(), 2));
			_lastSocialAction = System.currentTimeMillis();
		}
	}

	public void startRandomAnimation()
	{
		if(!hasRandomAnimation())
			return;
		_animationTask = LazyPrecisionTaskManager.getInstance().addNpcAnimationTask(this);
	}

	public void stopRandomAnimation()
	{
		if(_animationTask != null)
		{
			_animationTask.cancel(false);
			_animationTask = null;
		}
	}

	public boolean hasRandomAnimation()
	{
		return _hasRandomAnimation;
	}
	public void setRandomWalk(boolean value)
	{
		_hasRandomWalk = value;
	}

	public boolean hasRandomWalk()
	{
		return _hasRandomWalk;
	}

	public Castle getCastle()
	{
		if(isInZone(ZoneType.offshore))
			return null;
		if(_nearestCastle == null)
			_nearestCastle = ResidenceHolder.getInstance().getResidence(getTemplate().getCastleId());
		return _nearestCastle;
	}

	public Castle getCastle(Player player)
	{
		return getCastle();
	}

	public Fortress getFortress()
	{
		if(_nearestFortress == null)
			_nearestFortress = ResidenceHolder.getInstance().findNearestResidence(Fortress.class, getX(), getY(), getZ(), getReflection(), 32768);

		return _nearestFortress;
	}

	public ClanHall getClanHall()
	{
		if(_nearestClanHall == null)
			_nearestClanHall = ResidenceHolder.getInstance().findNearestResidence(ClanHall.class, getX(), getY(), getZ(), getReflection(), 32768);

		return _nearestClanHall;
	}

	public Dominion getDominion()
	{
		if(getReflection() != ReflectionManager.DEFAULT)
			return null;

		if(_nearestDominion == null)
		{
			if(getTemplate().getCastleId() == 0)
				return null;

			Castle castle = ResidenceHolder.getInstance().getResidence(getTemplate().getCastleId());
			_nearestDominion = castle.getDominion();
		}

		return _nearestDominion;
	}

	protected long _lastSocialAction;

	@Override
	public void onAction(Player player, boolean shift)
	{
		if(!isTargetable())
		{
			player.sendActionFailed();
			return;
		}

		if(player.getTarget() != this)
		{
			player.setTarget(this);
			return;
		}

		if(Events.onAction(player, this, shift))
		{
			player.sendActionFailed();
			return;
		}

		if(isAutoAttackable(player))
		{
			player.getAI().Attack(this, false, shift);
			return;
		}

		if(!checkInteractionDistance(player))
		{
			if(player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
			return;
		}
		
		if(player.isInDuel())
		{
			player.sendActionFailed();
			return;
		}
		
		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0 && !player.isGM() && !(this instanceof WarehouseInstance))
		{
			player.sendActionFailed();
			return;
		}

		// С NPC нельзя разговаривать мертвым и сидя
		if(!Config.ALLOW_TALK_WHILE_SITTING && player.isSitting() || player.isAlikeDead())
			return;

		if(hasRandomAnimation())
			onRandomAnimation();

		player.sendActionFailed();
		player.stopMove();
		
		getListeners().onShowChat();
		
		if(_isBusy)
			showBusyWindow(player);
		else if(isHasChatWindow())
		{
			boolean flag = false;
			Quest[] qlst = getTemplate().getEventQuests(QuestEventType.NPC_FIRST_TALK);
			if(qlst != null && qlst.length > 0)
				for(Quest element : qlst)
				{
					QuestState qs = player.getQuestState(element);
					if((qs == null || !qs.isCompleted()) && element.notifyFirstTalk(this, player))
						flag = true;
				}
			if(!flag)
				showChatWindow(player, 0);
		}
	}

	public void showQuestWindow(Player player, int questId)
	{
		if(!player.isQuestContinuationPossible(true))
			return;

		int count = 0;
		for(QuestState quest : player.getAllQuestsStates())
		{
			if(quest != null && quest.getQuest().isVisible() && quest.isStarted() && quest.getCond() > 0)
				count++;
		}

		if(count > 40)
		{
			showChatWindow(player, "quest-limit.htm");
			return;
		}

		try
		{
			// Get the state of the selected quest
			QuestState qs = player.getQuestState(questId);
			if(qs != null)
			{
				if(qs.isCompleted())
				{
					if(qs.getQuest().notifyCompleted(this, qs))
						return;
				}
				if(qs.getQuest().notifyTalk(this, qs))
					return;
			}
			else
			{
				Quest q = QuestHolder.getInstance().getQuest(questId);
				if(q != null)
				{
					// check for start point
					Quest[] qlst = getTemplate().getEventQuests(QuestEventType.QUEST_START);
					if(qlst != null && qlst.length > 0)
					{
						for(Quest element : qlst)
						{
							if(element == q)
							{
								qs = q.newQuestState(player);
								if(qs.getQuest().notifyTalk(this, qs))
									return;
								break;
							}
						}
					}
				}
			}

			showChatWindow(player, "no-quest.htm");
		}
		catch(Exception e)
		{
			_log.warn("problem with npc text(QUEST ID[" + questId + "]" + e);
			_log.error("", e);
		}

		player.sendActionFailed();
	}

	public static boolean canBypassCheck(Player player, NpcInstance npc)
	{
		if(npc == null || player.isActionsDisabled() || !Config.ALLOW_TALK_WHILE_SITTING && player.isSitting() || !npc.checkInteractionDistance(player))
		{
			player.sendActionFailed();
			return false;
		}
		if(player.isInDuel() || player.getTeam() != TeamType.NONE)
		{
			player.sendActionFailed();
			return false;
		}	
		return true;
	}

	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(getTemplate().getTeleportList().size() > 0 && checkForDominionWard(player))
			return;

		try
		{
			if(command.equalsIgnoreCase("TerritoryStatus"))
			{
				NpcHtmlMessagePacket html = new NpcHtmlMessagePacket(player, this);
				html.setFile("merchant/territorystatus.htm");
				html.replace("%npcname%", getName());

				Castle castle = getCastle(player);
				if(castle != null && castle.getId() > 0)
				{
					html.replace("%castlename%",  HtmlUtils.htmlResidenceName(castle.getId()));
					html.replace("%taxpercent%", String.valueOf(castle.getTaxPercent()));

					if(castle.getOwnerId() > 0)
					{
						Clan clan = ClanTable.getInstance().getClan(castle.getOwnerId());
						if(clan != null)
						{
							html.replace("%clanname%", clan.getName());
							html.replace("%clanleadername%", clan.getLeaderName());
						}
						else
						{
							html.replace("%clanname%", "unexistant clan");
							html.replace("%clanleadername%", "None");
						}
					}
					else
					{
						html.replace("%clanname%", "NPC");
						html.replace("%clanleadername%", "None");
					}
				}
				else
				{
					html.replace("%castlename%", "Open");
					html.replace("%taxpercent%", "0");

					html.replace("%clanname%", "No");
					html.replace("%clanleadername%", getName());
				}

				player.sendPacket(html);
			}
			else if(command.startsWith("QuestEvent"))
			{
				StringTokenizer tokenizer = new StringTokenizer(command);
				tokenizer.nextToken();

				String questName = tokenizer.nextToken();

				player.processQuestEvent(Integer.parseInt(questName), command.substring(12 + questName.length()), this);
			}
			else if(command.startsWith("Quest"))
			{
				String quest = command.substring(5).trim();
				if(quest.length() == 0)
					showQuestWindow(player);
				else
				{
					try
					{
						int questId = Integer.parseInt(quest);
						showQuestWindow(player, questId);
					}
					catch(NumberFormatException nfe)
					{
						_log.error("", nfe);
					}
				}
			}
			else if(command.startsWith("Chat"))
				try
				{
					int val = Integer.parseInt(command.substring(5));
					showChatWindow(player, val);
				}
				catch(NumberFormatException nfe)
				{
					String filename = command.substring(5).trim();
					if(filename.length() == 0)
						showChatWindow(player, "npcdefault.htm");
					else
						showChatWindow(player, filename);
				}
			else if(command.startsWith("AttributeCancel"))
				player.sendPacket(new ExShowBaseAttributeCancelWindow(player));
			else if(command.startsWith("NpcLocationInfo"))
			{
				int val = Integer.parseInt(command.substring(16));
				NpcInstance npc = GameObjectsStorage.getByNpcId(val);
				if(npc != null)
				{
					// Ставим флажок на карте и стрелку на компасе
					player.addRadar(npc.getLoc());
				}
			}
			else if(command.startsWith("Multisell") || command.startsWith("multisell"))
			{
				String listId = command.substring(9).trim();
				Castle castle = getCastle(player);
				MultiSellHolder.getInstance().SeparateAndSend(Integer.parseInt(listId), player, castle != null ? castle.getTaxRate() : 0);
			}
			else if(command.startsWith("EnterRift"))
			{
				if(checkForDominionWard(player))
					return;

				StringTokenizer st = new StringTokenizer(command);
				st.nextToken(); //no need for "enterRift"

				Integer b1 = Integer.parseInt(st.nextToken()); //type

				DimensionalRiftManager.getInstance().start(player, b1, this);
			}
			else if(command.startsWith("ChangeRiftRoom"))
			{
				if(player.isInParty() && player.getParty().isInReflection() && player.getParty().getReflection() instanceof DimensionalRift)
					((DimensionalRift) player.getParty().getReflection()).manualTeleport(player, this);
				else
					DimensionalRiftManager.getInstance().teleportToWaitingRoom(player);
			}
			else if(command.startsWith("ExitRift"))
			{
				if(player.isInParty() && player.getParty().isInReflection() && player.getParty().getReflection() instanceof DimensionalRift)
					((DimensionalRift) player.getParty().getReflection()).manualExitRift(player, this);
				else
					DimensionalRiftManager.getInstance().teleportToWaitingRoom(player);
			}
			else if(command.equalsIgnoreCase("SkillList"))
				showSkillList(player);
			else if(command.equalsIgnoreCase("ClanSkillList"))
				showClanSkillList(player);
			else if(command.startsWith("SubUnitSkillList"))
				showSubUnitSkillList(player);
			else if(command.equalsIgnoreCase("TransformationSkillList"))
				showTransformationSkillList(player, AcquireType.TRANSFORMATION);
			else if(command.equalsIgnoreCase("CertificationSkillList"))
				showTransformationSkillList(player, AcquireType.CERTIFICATION);
			else if(command.equalsIgnoreCase("CollectionSkillList"))
				showCollectionSkillList(player);
			else if(command.equalsIgnoreCase("BuyTransformation"))
				showTransformationMultisell(player);
			else if(command.startsWith("Augment"))
			{
				int cmdChoice = Integer.parseInt(command.substring(8, 9).trim());
				if(cmdChoice == 1)
					player.sendPacket(Msg.SELECT_THE_ITEM_TO_BE_AUGMENTED, ExShowVariationMakeWindow.STATIC);
				else if(cmdChoice == 2)
					player.sendPacket(Msg.SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION, ExShowVariationCancelWindow.STATIC);
			}
			else if(command.startsWith("Link"))
				showChatWindow(player, command.substring(5));
			else if(command.startsWith("Teleport"))
			{
				int cmdChoice = Integer.parseInt(command.substring(9));
				TeleportLocation[] list = getTemplate().getTeleportList(cmdChoice);
				if(list != null)
					showTeleportList(player, list);
				else
				{
					if(player.isLangRus())
						player.sendMessage("Ссылка неисправна, сообщите администратору."); //TODO [G1ta0] custom message
					else
						player.sendMessage("This link is broken - report to administrator");//TODO [G1ta0] custom message
				}		
			}
			else if(command.startsWith("Tele20Lvl"))
			{
				int cmdChoice = Integer.parseInt(command.substring(10, 11).trim());
				TeleportLocation[] list = getTemplate().getTeleportList(cmdChoice);
				if(player.getLevel() > 20)
					showChatWindow(player, "teleporter/" + getNpcId() + "-no.htm");
				else if(list != null)
					showTeleportList(player, list);
				else
				{
					if(player.isLangRus())
						player.sendMessage("Ссылка неисправна, сообщите администратору."); //TODO [G1ta0] custom message
					else
						player.sendMessage("This link is broken - report to administrator");//TODO [G1ta0] custom message				
				}
			}
			else if(command.startsWith("open_gate"))
			{
				int val = Integer.parseInt(command.substring(10));
				ReflectionUtils.getDoor(val).openMe();
				player.sendActionFailed();
			}
			else if(command.equalsIgnoreCase("TransferSkillList"))
				showTransferSkillList(player);
			else if(command.equalsIgnoreCase("CertificationCancel"))
				CertificationFunctions.cancelCertification(this, player);
			else if(command.startsWith("RemoveTransferSkill"))
			{
				AcquireType type = AcquireType.transferType(player.getActiveClassId());
				if(type == null)
					return;

				Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(null, type);
				if(skills.isEmpty())
				{
					player.sendActionFailed();
					return;
				}

				boolean reset = false;
				for(SkillLearn skill : skills)
					if(player.getKnownSkill(skill.getId()) != null)
					{
						reset = true;
						break;
					}

				if(!reset)
				{
					player.sendActionFailed();
					return;
				}

				if(!player.reduceAdena(10000000L, true))
				{
					showChatWindow(player, "common/skill_share_healer_no_adena.htm");
					return;
				}

				for(SkillLearn skill : skills)
				{
					if(player.removeSkill(skill.getId(), true) != null)
					{
						for(ItemData item : skill.getRequiredItemsForLearn(type))
							ItemFunctions.addItem(player, item.getId(), item.getCount(), true, "Remove transfer skill cashback");
					}
				}
			}
			else if(command.startsWith("ExitFromQuestInstance"))
			{
				Reflection r = player.getReflection();
				r.startCollapseTimer(60000);
				player.teleToLocation(r.getReturnLoc(), ReflectionManager.DEFAULT);
				if(command.length() > 22)
					try
					{
						int val = Integer.parseInt(command.substring(22));
						showChatWindow(player, val);
					}
					catch(NumberFormatException nfe)
					{
						String filename = command.substring(22).trim();
						if(filename.length() > 0)
							showChatWindow(player, filename);
					}
			}
			else
			{
				String word = command.split("\\s+")[0];
				String args = command.substring(word.length()).trim();

				Pair<Object, Method> b = BypassHolder.getInstance().getBypass(word);
				if(b != null)
					b.getValue().invoke(b.getKey(), player, this, StringUtils.isEmpty(args) ? new String[0] : args.split("\\s+"));
				else
					_log.warn("Unknown command=[" + command + "] npcId:" + getTemplate().getNpcId());
			}
		}
		catch(NumberFormatException nfe)
		{
			_log.warn("Invalid bypass to Server command parameter! npcId=" + getTemplate().getNpcId() + " command=[" + command + "]", nfe);
		}
		catch(Exception sioobe)
		{
			_log.warn("Incorrect htm bypass! npcId=" + getTemplate().getNpcId() + " command=[" + command + "]", sioobe);
		}
	}

	public void showTeleportList(Player player, TeleportLocation[] list)
	{
		StringBuilder sb = new StringBuilder();

		sb.append("&$556;").append("<br><br>");

		if(list != null && player.getPlayerAccess().UseTeleport)
		{
			for(TeleportLocation tl : list)
				if(tl.getItem().getItemId() == ItemTemplate.ITEM_ID_ADENA)
				{
					double pricemod = player.getLevel() <= Config.GATEKEEPER_FREE ? 0. : Config.GATEKEEPER_MODIFIER;
					if(tl.getPrice() > 0 && pricemod > 0)
					{
						//On Saturdays and Sundays from 8 PM to 12 AM, gatekeeper teleport fees decrease by 50%.
						Calendar calendar = Calendar.getInstance();
						int day = calendar.get(Calendar.DAY_OF_WEEK);
						int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
						if((day == Calendar.SUNDAY || day == Calendar.SATURDAY) && (hour >= 20 && hour <= 12))
							pricemod /= 2;
					}
					sb.append("<a action=\"bypass -h scripts_Util:Gatekeeper ").append(tl.getX()).append(" ").append(tl.getY()).append(" ").append(tl.getZ());
					if(tl.getCastleId() != 0)
						sb.append(" ").append(tl.getCastleId());
					sb.append(" ").append((long) (tl.getPrice() * pricemod)).append("\" msg=\"811;F;").append(tl.getName()).append("\">").append(HtmlUtils.htmlNpcString(tl.getName()));
					if(tl.getPrice() * pricemod > 0)
						sb.append(" - ").append((long) (tl.getPrice() * pricemod)).append(" ").append(HtmlUtils.htmlItemName(ItemTemplate.ITEM_ID_ADENA));
					sb.append("</a><br1>");
				}
				else
					sb.append("<a action=\"bypass -h scripts_Util:QuestGatekeeper ").append(tl.getX()).append(" ").append(tl.getY()).append(" ").append(tl.getZ()).append(" ").append(tl.getPrice()).append(" ").append(tl.getItem().getItemId()).append("\" msg=\"811;F;").append("\">").append(HtmlUtils.htmlNpcString(tl.getName())).append(" - ").append(tl.getPrice()).append(" ").append(HtmlUtils.htmlItemName(tl.getItem().getItemId())).append("</a><br1>");
		}
		else
			sb.append("No teleports available for you.");

		NpcHtmlMessagePacket html = new NpcHtmlMessagePacket(player, this);
		html.setHtml(HtmlUtils.bbParse(sb.toString()));
		player.sendPacket(html);
	}

	public void showQuestWindow(Player player)
	{
		// collect awaiting quests and start points
		List<Quest> options = new ArrayList<Quest>();

		List<QuestState> awaits = player.getQuestsForEvent(this, QuestEventType.QUEST_TALK);
		Quest[] starts = getTemplate().getEventQuests(QuestEventType.QUEST_START);

		if(awaits != null)
			for(QuestState x : awaits)
				if(!options.contains(x.getQuest()))
					if(x.getQuest().getId() > 0)
						options.add(x.getQuest());

		if(starts != null)
			for(Quest x : starts)
				if(!options.contains(x))
					if(x.getId() > 0)
						options.add(x);

		// Display a QuestChooseWindow (if several quests are available) or QuestWindow
		if(options.size() > 1)
			showQuestChooseWindow(player, options.toArray(new Quest[options.size()]));
		else if(options.size() == 1)
			showQuestWindow(player, options.get(0).getId());
		else
			showQuestWindow(player, 0);
	}

	public void showQuestChooseWindow(Player player, Quest[] quests)
	{
		StringBuilder sb = new StringBuilder();

		sb.append("<html><br>");

		for(Quest q : quests)
		{
			if(!q.isVisible())
				continue;

			sb.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Quest ").append(q.getId()).append("\">[").append(q.getDescr(player)).append("]</a><br>");
		}

		sb.append("</body></html>");

		NpcHtmlMessagePacket html = new NpcHtmlMessagePacket(player, this);
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}

	public void showChatWindow(Player player, int val, Object... replace)
	{
		if(getTemplate().getTeleportList().size() > 0 && checkForDominionWard(player))
			return;

		String filename = SevenSigns.SEVEN_SIGNS_HTML_PATH;
		int npcId = getNpcId();
		switch(npcId)
		{
			case 31111: // Gatekeeper Spirit (Disciples)
				int sealAvariceOwner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_AVARICE);
				int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
				int compWinner = SevenSigns.getInstance().getCabalHighestScore();
				if(playerCabal == sealAvariceOwner && playerCabal == compWinner)
					switch(sealAvariceOwner)
					{
						case SevenSigns.CABAL_DAWN:
							filename += "spirit_dawn.htm";
							break;
						case SevenSigns.CABAL_DUSK:
							filename += "spirit_dusk.htm";
							break;
						case SevenSigns.CABAL_NULL:
							filename += "spirit_null.htm";
							break;
					}
				else
					filename += "spirit_null.htm";
				break;
			case 31112: // Gatekeeper Spirit (Disciples)
				filename += "spirit_exit.htm";
				break;
			case 30298:
				if(player.getPledgeType() == Clan.SUBUNIT_ACADEMY)
					filename = getHtmlPath(npcId, 1, player);
				else
					filename = getHtmlPath(npcId, 0, player);
				break;
			default:
				if(npcId >= 31093 && npcId <= 31094 || npcId >= 31172 && npcId <= 31201 || npcId >= 31239 && npcId <= 31254)
					return;
				// Get the text of the selected HTML file in function of the npcId and of the page number
				filename = getHtmlPath(npcId, val, player);
				break;
		}

		NpcHtmlMessagePacket packet = new NpcHtmlMessagePacket(player, this, filename, val);
		if(replace.length % 2 == 0)
			for(int i = 0; i < replace.length; i+= 2)
				packet.replace(String.valueOf(replace[i]), String.valueOf(replace[i + 1]));
		player.sendPacket(packet);
	}

	public void showChatWindow(Player player, String filename, Object... replace)
	{
		NpcHtmlMessagePacket packet;
		if(filename.endsWith(".htm"))
			packet = new NpcHtmlMessagePacket(player, this, filename, 0);
		else
		{
			packet = new NpcHtmlMessagePacket(player, this);
			packet.setHtml(filename);
		}

		if(replace.length % 2 == 0)
		{
			for(int i = 0; i < replace.length; i+= 2)
				packet.replace(String.valueOf(replace[i]), String.valueOf(replace[i + 1]));
		}

		player.sendPacket(packet);
	}

	public String getHtmlPath(int npcId, int val, Player player)
	{
		String pom;
		if(val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;

 		if(getTemplate().getHtmRoot() != null)
			return getTemplate().getHtmRoot() + pom + ".htm";

		String temp = "default/" + pom + ".htm";
		if(HtmCache.getInstance().getIfExists(temp, player) != null)
			return temp;

		temp = "trainer/" + pom + ".htm";
		if(HtmCache.getInstance().getIfExists(temp, player) != null)
			return temp;

		// If the file is not found, the standard message "I have nothing to say to you" is returned
		return "npcdefault.htm";
	}

	private boolean _isBusy;
	private String _busyMessage = "";

	public final boolean isBusy()
	{
		return _isBusy;
	}

	public void setBusy(boolean isBusy)
	{
		_isBusy = isBusy;
	}

	public final String getBusyMessage()
	{
		return _busyMessage;
	}

	public void setBusyMessage(String message)
	{
		_busyMessage = message;
	}

	public void showBusyWindow(Player player)
	{
		NpcHtmlMessagePacket html = new NpcHtmlMessagePacket(player, this);
		html.setFile("npcbusy.htm");
		html.replace("%npcname%", getName());
		html.replace("%playername%", player.getName());
		html.replace("%busymessage%", _busyMessage);
		player.sendPacket(html);
	}

	public void showSkillList(Player player)
	{
		ClassId classId = player.getClassId();

		if(classId == null)
			return;

		int npcId = getTemplate().npcId;

		if(getTemplate().getTeachInfo().isEmpty())
		{
			NpcHtmlMessagePacket html = new NpcHtmlMessagePacket(player, this);
			StringBuilder sb = new StringBuilder();
			sb.append("<html><head><body>");
			if(player.isLangRus())
				sb.append("Я не могу обучить тебя. Для твоего класса мой список пуст.<br> Свяжись с админом для фикса этого. <br>NpcId:" + npcId + ", твой classId:" + player.getClassId().getId() + "<br>");
			else
				sb.append("I cannot teach you. My class list is empty.<br> Ask admin to fix it. <br>NpcId:" + npcId + ", Your classId:" + player.getClassId().getId() + "<br>");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);

			return;
		}

		if(!(getTemplate().canTeach(classId) || getTemplate().canTeach(classId.getParent(player.getSex()))))
		{
			if(this instanceof WarehouseInstance)
				showChatWindow(player, "warehouse/" + getNpcId() + "-noteach.htm");
			else if(this instanceof TrainerInstance)
				showChatWindow(player, "trainer/" + getNpcId() + "-noteach.htm");
			else
			{
				NpcHtmlMessagePacket html = new NpcHtmlMessagePacket(player, this);
				StringBuilder sb = new StringBuilder();
				sb.append("<html><head><body>");
				sb.append(new CustomMessage("l2s.gameserver.model.instances.L2NpcInstance.WrongTeacherClass", player));
				sb.append("</body></html>");
				html.setHtml(sb.toString());
				player.sendPacket(html);
			}
			return;
		}

		final Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(player, AcquireType.NORMAL);

		final ExAcquirableSkillListByClass asl = new ExAcquirableSkillListByClass(AcquireType.NORMAL, skills.size());
		int counts = 0;

		for(SkillLearn s : skills)
		{
			if(s.isClicked())
				continue;

			Skill sk = SkillHolder.getInstance().getSkill(s.getId(), s.getLevel());
			if(sk == null || !sk.getCanLearn(player.getClassId()) || !sk.canTeachBy(npcId))
				continue;

			counts++;

			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getCost(), 0);
		}

		if(counts == 0)
		{
			int minlevel = SkillAcquireHolder.getInstance().getMinLevelForNewSkill(player, null, AcquireType.NORMAL);

			if(minlevel > 0)
			{
				SystemMessagePacket sm = new SystemMessagePacket(SystemMsg.YOU_DO_NOT_HAVE_ANY_FURTHER_SKILLS_TO_LEARN__COME_BACK_WHEN_YOU_HAVE_REACHED_LEVEL_S1);
				sm.addInteger(minlevel);
				player.sendPacket(sm);
			}
			else
				player.sendPacket(SystemMsg.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
			player.sendPacket(AcquireSkillDonePacket.STATIC);
		}
		else
			player.sendPacket(asl);

		player.sendActionFailed();
	}

	public void showTransferSkillList(Player player)
	{
		ClassId classId = player.getClassId();
		if(classId == null)
			return;

		if(player.getLevel() < 76 || !classId.isOfLevel(ClassLevel.THIRD))
		{
			NpcHtmlMessagePacket html = new NpcHtmlMessagePacket(player, this);
			StringBuilder sb = new StringBuilder();
			sb.append("<html><head><body>");
			sb.append("You must have 3rd class change quest completed.");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			return;
		}

		AcquireType type = AcquireType.transferType(player.getActiveClassId());
		if(type == null)
			return;

		showAcquireList(type, player);
	}

	public static void showCollectionSkillList(Player player)
	{
		showAcquireList(AcquireType.COLLECTION, player);
	}

	public void showTransformationMultisell(Player player)
	{
		if(!Config.ALLOW_LEARN_TRANS_SKILLS_WO_QUEST)
			if(!player.isQuestCompleted(136))
			{
				showChatWindow(player, "trainer/" + getNpcId() + "-nobuy.htm");
				return;
			}

		Castle castle = getCastle(player);
		MultiSellHolder.getInstance().SeparateAndSend(32323, player, castle != null ? castle.getTaxRate() : 0);
		player.sendActionFailed();
	}

	public void showTransformationSkillList(Player player, AcquireType type)
	{
		if(!Config.ALLOW_LEARN_TRANS_SKILLS_WO_QUEST)
			if(!player.isQuestCompleted(136))
			{
				showChatWindow(player, "trainer/" + getNpcId() + "-noquest.htm");
				return;
			}

		showAcquireList(type, player);
	}

	public static void showFishingSkillList(Player player)
	{
		showAcquireList(AcquireType.FISHING, player);
	}

	public static void showClanSkillList(Player player)
	{
		if(player.getClan() == null || !player.isClanLeader())
		{
			player.sendPacket(SystemMsg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
			player.sendActionFailed();
			return;
		}

		showAcquireList(AcquireType.CLAN, player);
	}

	public static void showAcquireList(AcquireType t, Player player)
	{
		final Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(player, t);

		final ExAcquirableSkillListByClass asl = new ExAcquirableSkillListByClass(t, skills.size());

		for(SkillLearn s : skills)
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getCost(), 0);

		if(skills.size() == 0)
		{
			player.sendPacket(AcquireSkillDonePacket.STATIC);
			player.sendPacket(SystemMsg.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
		}
		else
			player.sendPacket(asl);

		player.sendActionFailed();
	}

	public static void showSubUnitSkillList(Player player)
	{
		Clan clan = player.getClan();
		if(clan == null)
			return;

		if((player.getClanPrivileges() & Clan.CP_CL_TROOPS_FAME) != Clan.CP_CL_TROOPS_FAME)
		{
			player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}

		Set<SkillLearn> learns = new TreeSet<SkillLearn>();
		for(SubUnit sub : player.getClan().getAllSubUnits())
			learns.addAll(SkillAcquireHolder.getInstance().getAvailableSkills(player, AcquireType.SUB_UNIT, sub));

		final ExAcquirableSkillListByClass asl = new ExAcquirableSkillListByClass(AcquireType.SUB_UNIT, learns.size());

		for(SkillLearn s : learns)
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getCost(), 1, Clan.SUBUNIT_KNIGHT4);

		if(learns.size() == 0)
		{
			player.sendPacket(AcquireSkillDonePacket.STATIC);
			player.sendPacket(SystemMsg.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
		}
		else
			player.sendPacket(asl);

		player.sendActionFailed();
	}
	/**
	 * Нужно для отображения анимации спауна, используется в пакете NpcInfo:
	 * 0=false, 1=true, 2=summoned (only works if model has a summon animation)
	 **/
	public int getSpawnAnimation()
	{
		return _spawnAnimation;
	}

	public int calculateLevelDiffForDrop(int charLevel)
	{
		return calculateLevelDiffForDrop(getLevel(), charLevel, isRaid());
	}
  
	public static int calculateLevelDiffForDrop(int mobLevel, int charLevel, boolean boss)
	{
		if(!Config.DEEPBLUE_DROP_RULES)
			return 0;

		// According to official data (Prima), deep blue mobs are 9 or more levels below players
		int deepblue_maxdiff = boss ? Config.DEEPBLUE_DROP_RAID_MAXDIFF : Config.DEEPBLUE_DROP_MAXDIFF;

		return Math.max(charLevel - mobLevel - deepblue_maxdiff, 0);
	}

	public boolean isSevenSignsMonster()
	{
		return getFaction().containsName("c_dungeon_clan");
	}

	@Override
	public String toString()
	{
		return getNpcId() + " " + getName();
	}

	public void refreshID()
	{
		objectId = IdFactory.getInstance().getNextId();
		_storedId = GameObjectsStorage.refreshId(this);
	}

	private boolean _isUnderground = false;

	public void setUnderground(boolean b)
	{
		_isUnderground = b;
	}

	public boolean isUnderground()
	{
		return _isUnderground;
	}

	public boolean isTargetable()
	{
		return _isTargetable;
	}

	public void setTargetable(boolean value)
	{
		_isTargetable = value;
	}

	public boolean isShowName()
	{
		return _showName;
	}

	public void setShowName(boolean value)
	{
		_showName = value;
	}

	@Override
	public NpcListenerList getListeners()
	{
		if(listeners == null)
			synchronized (this)
			{
				if(listeners == null)
					listeners = new NpcListenerList(this);
			}

		return (NpcListenerList) listeners;
	}

	public <T extends NpcListener> boolean addListener(T listener)
	{
		return getListeners().add(listener);
	}

	public <T extends NpcListener> boolean removeListener(T listener)
	{
		return getListeners().remove(listener);
	}

	@Override
	public NpcStatsChangeRecorder getStatsRecorder()
	{
		if(_statsRecorder == null)
			synchronized (this)
			{
				if(_statsRecorder == null)
					_statsRecorder = new NpcStatsChangeRecorder(this);
			}

		return (NpcStatsChangeRecorder) _statsRecorder;
	}

	public void setNpcState(int stateId)
	{
		broadcastPacket(new ExChangeNPCState(getObjectId(), stateId));
		npcState = stateId;
	}

	public int getNpcState()
	{
		return npcState;
	}

	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		List<L2GameServerPacket> list = new ArrayList<L2GameServerPacket>(3);

		list.add(new NpcInfoPacket(this, forPlayer).init());

		if(isInCombat())
			list.add(new AutoAttackStartPacket(getObjectId()));

		if(isMoving || isFollow)
			list.add(movePacket());

		return list;
	}

	@Override
	public boolean isNpc()
	{
		return true;
	}

	@Override
	public int getGeoZ(int x, int y, int z)
	{
		int geoZ = super.getGeoZ(x, y, z);

		Location spawnedLoc = getSpawnedLoc();
		if(spawnedLoc.equals(x, y, z))
		{
			// Заглушка для точечного спавна. Некоторые НПС заспавнены в местах, где нет геодаты.
			if(Math.abs(geoZ - z) > Config.MIN_LAYER_HEIGHT)
				return z;
		}
		return geoZ;
	}

	@Override
	public Clan getClan()
	{
		Dominion dominion = getDominion();
		if(dominion == null)
			return null;
		int lordObjectId = dominion.getLordObjectId();
		return lordObjectId == 0 ? null : dominion.getOwner();
	}

	public NpcString getNameNpcString()
	{
		return _nameNpcString;
	}

	public NpcString getTitleNpcString()
	{
		return _titleNpcString;
	}

	public void setNameNpcString(NpcString nameNpcString)
	{
		_nameNpcString = nameNpcString;
	}

	public void setTitleNpcString(NpcString titleNpcString)
	{
		_titleNpcString = titleNpcString;
	}

	public boolean isMerchantNpc()
	{
		return false;
	}

	public SpawnRange getSpawnRange()
	{
		return _spawnRange;
	}

	public void setSpawnRange(SpawnRange spawnRange)
	{
		_spawnRange = spawnRange;
	}

	public boolean checkForDominionWard(Player player)
	{
		ItemInstance item = getActiveWeaponInstance();
		if(item != null && item.getAttachment() instanceof TerritoryWardObject)
		{
			showChatWindow(player, "flagman.htm");
			return true;
		}
		return false;
	}

	public void setParameter(String str, Object val)
	{
		if(_parameters == StatsSet.EMPTY)
			_parameters = new StatsSet();

		_parameters.set(str, val);
	}

	public void setParameters(MultiValueSet<String> set)
	{
		if(set.isEmpty())
			return;

		if(_parameters == StatsSet.EMPTY)
			_parameters = new MultiValueSet<String>(set.size());

		_parameters.putAll(set);
	}

	public int getParameter(String str, int val)
	{
		return _parameters.getInteger(str, val);
	}

	public long getParameter(String str, long val)
	{
		return _parameters.getLong(str, val);
	}

	public boolean getParameter(String str, boolean val)
	{
		return _parameters.getBool(str, val);
	}

	public String getParameter(String str, String val)
	{
		return _parameters.getString(str, val);
	}

	public MultiValueSet<String> getParameters()
	{
		return _parameters;
	}

	@Override
	public boolean isPeaceNpc()
	{
		return true;
	}

	public boolean isHasChatWindow()
	{
		return _hasChatWindow;
	}

	public void setHasChatWindow(boolean hasChatWindow)
	{
		_hasChatWindow = hasChatWindow;
	}

	public void setAI(NpcAI ai)
	{
		_ai = ai;
	}

	/**
	 * 
	 * Возвращает базовый множитель из конфига для дропа предметов, при необходимости умноженный на ПА.
	 * Не используется для адены, спойла и эвентового дропа. Не учитывает разницу уровней.
	 * 
	 */
	public double getRewardRate(Player player)
	{
		return Config.RATE_DROP_ITEMS * player.getRateItems();
	}

	public double getDropChanceMod(Player player)
	{
		return player.getDropChanceMod();
	}

	public MonsterInstance getLeader()
	{
		return null;
	}

	@Override
	public boolean isInvul()
	{
		return super.isInvul() || getAI().getIntention() == CtrlIntention.AI_INTENTION_RETURN_HOME;
	}

	@Override
	public boolean onTeleported()
	{
		if(!super.onTeleported())
			return false;

		getAI().notifyEvent(CtrlEvent.EVT_TELEPORTED);
		return true;
	}

	public void onTimerFired(int timerId)
	{
		//
	}

	public void onSeeCreatue(Creature creature)
	{
		//
	}

	public void onDisappearCreatue(Creature creature)
	{
		//
	}

	@Override
	protected Shape makeGeoShape()
	{
		int x = getX();
		int y = getY();
		int z = getZ();
		Circle circle = new Circle(x, y, _geoRadius);
		circle.setZmin(z - Config.MAX_Z_DIFF);
		circle.setZmax(z + _geoHeight);
		return circle;
	}

	@Override
	protected boolean isGeoControlEnabled()
	{
		return _geoControlEnabled;
	}

	public void setGeoControlEnabled(boolean value)
	{
		_geoControlEnabled = value;
	}

	@Override
	protected L2GameServerPacket changeMovePacket()
	{
		return new NpcInfoState(this);
	}
}
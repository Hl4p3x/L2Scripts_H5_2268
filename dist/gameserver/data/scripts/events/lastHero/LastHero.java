package events.lastHero;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.CArrayIntSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.threading.RunnableImpl;
import l2s.commons.util.Rnd;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.EffectsDAO;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.listener.actor.player.OnPlayerExitListener;
import l2s.gameserver.listener.actor.player.OnTeleportListener;
import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Territory;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.impl.DuelEvent;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.instances.DoorInstance;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.RevivePacket;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.scripts.ScriptFile;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.templates.DoorTemplate;
import l2s.gameserver.templates.ZoneTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.ReflectionUtils;

public class LastHero extends Functions implements ScriptFile, OnDeathListener, OnTeleportListener, OnPlayerExitListener
{
	private static final Logger _log = LoggerFactory.getLogger(LastHero.class);

	private static final int[] doors = new int[] { 24190001, 24190002, 24190003, 24190004 };

	private static ScheduledFuture<?> _startTask;

	private static final IntSet players_list = new CArrayIntSet();
	private static final IntSet live_list = new CArrayIntSet();
	private static final int[][] mage_buffs = new int[Config.EVENT_LHMageBuffs.length][2];
	private static final int[][] fighter_buffs = new int[Config.EVENT_LHFighterBuffs.length][2];

	private static final IntObjectMap<String> boxes = new CHashIntObjectMap<String>();

	private static final int PROGRESS_DISABLED = 0;
	private static final int PROGRESS_REGISTRATION = 1;
	private static final int PROGRESS_PREPARE = 2;
	private static final int PROGRESS_BATTLE = 3;
	
	private static final AtomicInteger _progress = new AtomicInteger(PROGRESS_DISABLED);

	private static int _time_to_start;
	private static int _category;
	private static int _minLevel;
	private static int _maxLevel;
	private static int _autoContinue = 0;
	private static boolean _active = false;
	private static Skill buff;

	private static Reflection reflection = ReflectionManager.LAST_HERO;

	private static ScheduledFuture<?> _endTask;

	private static Zone _zone;
	private static Zone _zone1;
	private static Zone _zone2;
	private static Zone _zone3;
	private static Zone _zone4;
	private static Zone _zone5;
	//new
	private static Zone _zone6;
	private static Zone _zone7;
	private static Zone _zone8;
	private static Zone _zone9;
	private static Zone _zone10;
	private static Zone _myZone = null;
	private static Territory territory = null;

	private static final Map<String, ZoneTemplate> _zones = new HashMap<String, ZoneTemplate>();
	private static final IntObjectMap<DoorTemplate> _doors = new HashIntObjectMap<DoorTemplate>();

	private final ZoneListener _zoneListener = new ZoneListener();

	private static final Location _enter = new Location(149505, 46719, -3417);

	private static LastHero _instance;

	@Override
	public void onLoad()
	{
		_instance = this;

		_zones.put("[baylor_tvt]", ReflectionUtils.getZone("[hellbound_quarry_tvt]").getTemplate());
		_zones.put("[baylor_tvt]", ReflectionUtils.getZone("[baylor_tvt]").getTemplate());
		_zones.put("[emerald_square_tvt]", ReflectionUtils.getZone("[hellbound_quarry_tvt]").getTemplate());
		_zones.put("[beleth_tvt]", ReflectionUtils.getZone("[beleth_tvt]").getTemplate());
		_zones.put("[emerald_square_tvt]", ReflectionUtils.getZone("[emerald_square_tvt]").getTemplate());
		_zones.put("[baium_lair_tvt]", ReflectionUtils.getZone("[ti_new_tvt]").getTemplate());
		//new
		_zones.put("[baium_lair_tvt]", ReflectionUtils.getZone("[baium_lair_tvt]").getTemplate());
		_zones.put("[tully_tvt]", ReflectionUtils.getZone("[parnas_tvt]").getTemplate());
		_zones.put("[tully_tvt]", ReflectionUtils.getZone("[tully_tvt]").getTemplate());
		//new
		_zones.put("[destruction_tvt]", ReflectionUtils.getZone("[destruction_tvt]").getTemplate());
		_zones.put("[cleft_tvt]", ReflectionUtils.getZone("[cleft_tvt]").getTemplate());
		for(final int doorId : doors)
			_doors.put(doorId, ReflectionUtils.getDoor(doorId).getTemplate());

		reflection.init(_doors, _zones);
		_zone = reflection.getZone("[baylor_tvt]");
		_zone1 = reflection.getZone("[baylor_tvt]");
		_zone2 = reflection.getZone("[emerald_square_tvt]");
		_zone3 = reflection.getZone("[beleth_tvt]");
		_zone4 = reflection.getZone("[emerald_square_tvt]");
		_zone5 = reflection.getZone("[baium_lair_tvt]");
		//new
		_zone6 = reflection.getZone("[baium_lair_tvt]");
		_zone7 = reflection.getZone("[tully_tvt]");
		_zone8 = reflection.getZone("[tully_tvt]");
		//new
		_zone9 = reflection.getZone("[destruction_tvt]");
		_zone10 = reflection.getZone("[cleft_tvt]");
		_active = ServerVariables.getString("LastHero", "off").equalsIgnoreCase("on");

		if(isActive())
			scheduleEventStart();
		_zone.addListener(_zoneListener);
		_zone1.addListener(_zoneListener);
		_zone2.addListener(_zoneListener);
		_zone3.addListener(_zoneListener);
		_zone4.addListener(_zoneListener);
		_zone5.addListener(_zoneListener);
		_zone6.addListener(_zoneListener);
		_zone7.addListener(_zoneListener);
		_zone8.addListener(_zoneListener);
		_zone9.addListener(_zoneListener);
		_zone10.addListener(_zoneListener);

		int i = 0;

		if(Config.EVENT_LHBuffPlayers && Config.EVENT_LHMageBuffs.length != 0)
			for(String skill : Config.EVENT_LHMageBuffs)
			{
				String[] splitSkill = skill.split(",");
				mage_buffs[i][0] = Integer.parseInt(splitSkill[0]);
				mage_buffs[i][1] = Integer.parseInt(splitSkill[1]);
				i++;
			}

		i = 0;

		if(Config.EVENT_LHBuffPlayers && Config.EVENT_LHFighterBuffs.length != 0)
			for(String skill : Config.EVENT_LHFighterBuffs)
			{
				String[] splitSkill = skill.split(",");
				fighter_buffs[i][0] = Integer.parseInt(splitSkill[0]);
				fighter_buffs[i][1] = Integer.parseInt(splitSkill[1]);
				i++;
			}

		_log.info("Loaded Event: Last Hero");
	}

	@Override
	public void onReload()
	{
		_zone.removeListener(_zoneListener);
		if(_startTask != null)
		{
			_startTask.cancel(false);
			_startTask = null;
		}
	}

	@Override
	public void onShutdown()
	{
		onReload();
	}

	private static boolean isActive()
	{
		return _active;
	}

	public void activateEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;

		if(!isActive())
		{
			if(_startTask == null)
				scheduleEventStart();
			ServerVariables.set("LastHero", "on");
			_log.info("Event 'Last Hero' activated.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.LastHero.AnnounceEventStarted", null);
		}
		else
			player.sendMessage("Event 'Last Hero' already active.");

		_active = true;

		show("admin/events/events.htm", player);
	}

	public static void teleportPlayers()
	{
		for(Player player : getPlayers(players_list))
		{
			player.teleToLocation(getBackLocation(player), ReflectionManager.DEFAULT);
		}
	}

	public void deactivateEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;

		if(isActive())
		{
			if(_startTask != null)
			{
				_startTask.cancel(false);
				_startTask = null;
			}
			ServerVariables.unset("LastHero");
			_log.info("Event 'Last Hero' deactivated.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.LastHero.AnnounceEventStoped", null);
		}
		else
			player.sendMessage("Event 'LastHero' not active.");

		_active = false;

		show("admin/events/events.htm", player);
	}

	public static boolean isRunned()
	{
		return _progress.get() > 0;
	}

	public static int getMinLevelForCategory(int category)
	{
		switch(category)
		{
			case 1:
				return 20;
			case 2:
				return 30;
			case 3:
				return 40;
			case 4:
				return 52;
			case 5:
				return 62;
			case 6:
				return 76;
		}
		return 0;
	}

	public static int getMaxLevelForCategory(int category)
	{
		switch(category)
		{
			case 1:
				return 29;
			case 2:
				return 39;
			case 3:
				return 51;
			case 4:
				return 61;
			case 5:
				return 75;
			case 6:
				return 85;
		}
		return 0;
	}

	public static int getCategory(int level)
	{
		if(level >= 20 && level <= 29)
			return 1;
		else if(level >= 30 && level <= 39)
			return 2;
		else if(level >= 40 && level <= 51)
			return 3;
		else if(level >= 52 && level <= 61)
			return 4;
		else if(level >= 62 && level <= 75)
			return 5;
		else if(level >= 76)
			return 6;
		return 0;
	}

	public void start(String[] var)
	{
		Player player = getSelf();
		if(var.length != 2)
		{
			show(new CustomMessage("common.Error", player), player);
			return;
		}

		if(_endTask != null)
		{
			show(new CustomMessage("common.TryLater", player), player);
			return;
		}

		int category;
		int autoContinue;
		try
		{
			category = Integer.parseInt(var[0]);
			autoContinue = Integer.parseInt(var[1]);
		}
		catch(Exception e)
		{
			show(new CustomMessage("common.Error", player), player);
			return;
		}

		if(!_progress.compareAndSet(PROGRESS_DISABLED, PROGRESS_REGISTRATION))
			return;

		_category = category;
		_autoContinue = autoContinue;

		if(_category == -1)
		{
			_minLevel = 1;
			_maxLevel = 85;
		}
		else
		{
			_minLevel = getMinLevelForCategory(_category);
			_maxLevel = getMaxLevelForCategory(_category);
		}

		_time_to_start = Config.EVENT_LHTime;

		players_list.clear();
		live_list.clear();
		String[] param = { String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel) };
		sayToAll("scripts.events.LastHero.AnnouncePreStart", param);

		ThreadPoolManager.getInstance().schedule(() -> question(), 10000L);
		ThreadPoolManager.getInstance().schedule(() -> announce(), 60000L);
	}

	public static void sayToAll(String address, String[] replacements)
	{
		Announcements.getInstance().announceByCustomMessage(address, replacements, ChatType.CRITICAL_ANNOUNCE);
	}

	public static void question()
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			if(player != null && !player.isDead() && player.getLevel() >= _minLevel && player.getLevel() <= _maxLevel && player.getReflection().isDefault() && !player.isInOlympiadMode() && !player.isInObserverMode() && player.getVar("jailed") == null)
				player.scriptRequest(new CustomMessage("scripts.events.LastHero.AskPlayer", player).toString(), "events.lastHero.LastHero:addPlayer", new Object[0]);
	}

	public static void announce()
	{
		if(_time_to_start > 1)
		{
			_time_to_start--;
			String[] param = { String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel) };
			sayToAll("scripts.events.LastHero.AnnouncePreStart", param);
			ThreadPoolManager.getInstance().schedule(() -> announce(), 60000L);
		}
		else if(players_list.size() < 2)
		{
			sayToAll("scripts.events.LastHero.AnnounceEventCancelled", null);
			boxes.clear();
			_progress.set(PROGRESS_DISABLED);
			ThreadPoolManager.getInstance().schedule(() -> autoContinue(), 10000L);
		}
		else
		{
			if(!_progress.compareAndSet(PROGRESS_REGISTRATION, PROGRESS_PREPARE))
				return;
			sayToAll("scripts.events.LastHero.AnnounceEventStarting", null);
			ThreadPoolManager.getInstance().schedule(() -> prepare(), 5000L);
		}
	}

	public void addPlayer()
	{
		Player player = getSelf();
		if(player == null || !checkPlayer(player, true) || !checkDualBox(player))
			return;

		players_list.add(player.getObjectId());
		live_list.add(player.getObjectId());

		if(!Config.EVENT_TvTAllowMultiReg)
		{
			if("IP".equalsIgnoreCase(Config.EVENT_TvTCheckWindowMethod))
				boxes.put(player.getObjectId(), player.getIP());

			String hwid = player.getNetConnection().getHWID();
			if(hwid != null && "HWid".equalsIgnoreCase(Config.EVENT_TvTCheckWindowMethod))
				boxes.put(player.getObjectId(), hwid);
		}

		show(new CustomMessage("scripts.events.LastHero.Registered", player), player);
	}

	public static boolean checkPlayer(Player player, boolean first)
	{
		if(first && (_progress.get() != PROGRESS_REGISTRATION || player.isDead()))
		{
			show(new CustomMessage("scripts.events.Late", player), player);
			return false;
		}

		if(first && players_list.contains(player.getObjectId()))
		{
			show(new CustomMessage("scripts.events.LastHero.Cancelled", player), player);
			players_list.remove(player.getObjectId());
			live_list.remove(player.getObjectId());
			boxes.remove(player.getObjectId());
			return false;
		}

		if(player.getLevel() < _minLevel || player.getLevel() > _maxLevel)
		{
			show(new CustomMessage("scripts.events.LastHero.CancelledLevel", player), player);
			return false;
		}

		if(player.isMounted())
		{
			show(new CustomMessage("scripts.events.LastHero.Cancelled", player), player);
			return false;
		}

		if(player.isCursedWeaponEquipped())
		{
			show(new CustomMessage("scripts.events.CtF.Cancelled", player), player);
			return false;
		}

		if(player.isInDuel())
		{
			show(new CustomMessage("scripts.events.LastHero.CancelledDuel", player), player);
			return false;
		}

		if(player.getTeam() != TeamType.NONE)
		{
			show(new CustomMessage("scripts.events.CtF.CancelledOtherEvent", player), player);
			return false;
		}

		if(player.getOlympiadGame() != null || first && Olympiad.isRegistered(player))
		{
			show(new CustomMessage("scripts.events.LastHero.CancelledOlympiad", player), player);
			return false;
		}

		if(player.isInParty() && player.getParty().isInDimensionalRift())
		{
			show(new CustomMessage("scripts.events.LastHero.CancelledOtherEvent", player), player);
			return false;
		}

		if(player.isTeleporting())
		{
			show(new CustomMessage("scripts.events.LastHero.CancelledTeleport", player), player);
			return false;
		}

		if(player.isInObserverMode())
		{
			show(new CustomMessage("scripts.event.LastHero.CancelledObserver", player), player);
			return false;
		}
		if(!Config.ALLOW_HEROES_LASTHERO && player.isHero())
		{
			show(new CustomMessage("scripts.event.LastHero.CancelledHero", player), player);
			return false;
		}

		return true;
	}

	public static void prepare()
	{

		for(DoorInstance door : reflection.getDoors())
			door.closeMe();

		for(Zone z : reflection.getZones())
			z.setType(ZoneType.peace_zone);

		cleanPlayers();
		clearArena();

		ThreadPoolManager.getInstance().schedule(() -> ressurectPlayers(), 1000L);
		ThreadPoolManager.getInstance().schedule(() -> healPlayers(), 2000L);
		ThreadPoolManager.getInstance().schedule(() -> paralyzePlayers(), 4000L);
		ThreadPoolManager.getInstance().schedule(() -> teleportPlayersToColiseum(), 3000L);
		ThreadPoolManager.getInstance().schedule(() -> buffPlayers(), 5000L);
		ThreadPoolManager.getInstance().schedule(() -> go(), 60000L);

		sayToAll("scripts.events.LastHero.AnnounceFinalCountdown", null);
	}

	public static void go()
	{
		if(!_progress.compareAndSet(PROGRESS_PREPARE, PROGRESS_BATTLE))
			return;

		upParalyzePlayers();
		checkLive();
		clearArena();
		sayToAll("scripts.events.LastHero.AnnounceFight", null);
		for(Zone z : reflection.getZones())
			z.setType(ZoneType.battle_zone);
		_endTask = ThreadPoolManager.getInstance().schedule(() -> endBattle(), Config.EVENT_LHEventRunningTime * 60000L);
	}

	public static void endBattle()
	{
		if(!_progress.compareAndSet(PROGRESS_BATTLE, PROGRESS_DISABLED))
			return;

		removeAura();

		for(Zone z : reflection.getZones())
			z.setType(ZoneType.peace_zone);

		boxes.clear();

		if(live_list.size() == 1)
			for(Player player : getPlayers(live_list))
			{
				String[] repl = { player.getName() };
				sayToAll("scripts.events.LastHero.AnnounceWiner", repl);
				if(Config.EVENT_LastHeroItemID > 0)
				{
					long itemCount = Math.round(Config.EVENT_LastHeroRateFinal ? player.getLevel() * Config.EVENT_LastHeroItemCOUNTFinal : 1 * Config.EVENT_LastHeroItemCOUNTFinal);
					if(itemCount > 0)
						ItemFunctions.addItem(player, Config.EVENT_LastHeroItemID, itemCount, "Last Hero event on win reward");
				}
				if(Config.LH_WINCHAR_HERO)
				{
					player.setHero(true);
					player.updatePledgeClass();
					player.addSkill(SkillHolder.getInstance().getSkill(395, 1));
					player.addSkill(SkillHolder.getInstance().getSkill(396, 1));
					player.addSkill(SkillHolder.getInstance().getSkill(1374, 1));
					player.addSkill(SkillHolder.getInstance().getSkill(1375, 1));
					player.addSkill(SkillHolder.getInstance().getSkill(1376, 1));
					player.sendSkillList();
					player.broadcastUserInfo(true);
				}
				break;
			}

		sayToAll("scripts.events.LastHero.AnnounceEnd", null);
		ThreadPoolManager.getInstance().schedule(() -> end(), 30000L);

		if(_endTask != null)
		{
			_endTask.cancel(false);
			_endTask = null;
		}
		_myZone = null;
		territory = null;
	}

	public static void end()
	{
		ThreadPoolManager.getInstance().schedule(() -> ressurectPlayers(), 1000L);
		ThreadPoolManager.getInstance().schedule(() -> healPlayers(), 2000L);
		ThreadPoolManager.getInstance().schedule(() -> teleportPlayers(), 3000L);
		ThreadPoolManager.getInstance().schedule(() -> autoContinue(), 10000L);
	}

	public static void autoContinue()
	{
		if(_autoContinue > 0)
		{
			if(_autoContinue >= 6)
			{
				_autoContinue = 0;
				return;
			}
			_instance.start(new String[] { "" + (_autoContinue + 1), "" + (_autoContinue + 1) });
		}
		else
			scheduleEventStart();
	}

	public static void teleportPlayersToColiseum()
	{
		switch(Rnd.get(1, 11))
		{
			case 1:
				_myZone = _zone;
				break;
			case 2:
				_myZone = _zone1;
				break;
			case 3:
				_myZone = _zone2;
				break;
			case 4:
				_myZone = _zone3;
				break;
			case 5:
				_myZone = _zone4;
				break;
			case 6:
				_myZone = _zone5;
				break;
			case 7:
				_myZone = _zone6;
				break;
			case 8:
				_myZone = _zone7;
				break;
			case 9:
				_myZone = _zone8;
				break;
			case 10:
				_myZone = _zone9;
				break;
			case 11:
				_myZone = _zone10;
				break;
			default:
				_myZone = _zone;
		}
		territory = _myZone.getTerritory();
		for(Player player : getPlayers(players_list))
		{
			unRide(player);
			if(!Config.EVENT_LHAllowSummons)
				unSummonPet(player, true);

			DuelEvent duel = player.getEvent(DuelEvent.class);
			if(duel != null)
				duel.abortDuel(player);
			if(player.isInParty())
				player.leaveParty();
			player.addListener(_instance);
			player.setVar("backCoords", player.getLoc().toXYZString(), -1);
			player.teleToLocation(Territory.getRandomLoc(territory, player.isFlying()), reflection);
			player.setIsInLastHero(true);
			if(!Config.EVENT_LHAllowBuffs)
			{
				player.getEffectList().stopAllEffects();
				EffectsDAO.getInstance().deleteSummonsEffects(player); // Удаляем все эффекты непризванных саммонов.
				if(player.getServitor() != null)
					player.getServitor().getEffectList().stopAllEffects();
			}
		}
	}

	public static void paralyzePlayers()
	{
		for(Player player : getPlayers(players_list))
		{
			if(!player.isRooted())
			{
				player.getEffectList().stopEffect(Skill.SKILL_MYSTIC_IMMUNITY);
				player.startRooted();
				player.startDamageBlocked();
				player.startAbnormalEffect(AbnormalEffect.ROOT);
			}

			if(player.getServitor() != null && !player.getServitor().isRooted())
			{
				player.getServitor().startRooted();
				player.getServitor().startDamageBlocked();
				player.getServitor().startAbnormalEffect(AbnormalEffect.ROOT);
			}
		}
	}

	public static void upParalyzePlayers()
	{
		for(Player player : getPlayers(players_list))
		{
			if(player.isRooted())
			{
				player.stopRooted();
				player.stopDamageBlocked();
				player.stopAbnormalEffect(AbnormalEffect.ROOT);
			}

			if(player.getServitor() != null && player.getServitor().isRooted())
			{
				player.getServitor().stopRooted();
				player.getServitor().stopDamageBlocked();
				player.getServitor().stopAbnormalEffect(AbnormalEffect.ROOT);
			}
		}
	}

	public static void ressurectPlayers()
	{
		for(Player player : getPlayers(players_list))
			if(player.isDead())
			{
				player.restoreExp();
				player.setCurrentCp(player.getMaxCp());
				player.setCurrentHp(player.getMaxHp(), true);
				player.setCurrentMp(player.getMaxMp());
				player.broadcastPacket(new RevivePacket(player));
			}
	}

	public static void healPlayers()
	{
		for(Player player : getPlayers(players_list))
		{
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
		}
	}

	public static void cleanPlayers()
	{
		for(Player player : getPlayers(players_list))
			if(!checkPlayer(player, false))
				removePlayer(player);
	}

	public static synchronized void checkLive()
	{
		for(int objectId : live_list.toArray())
		{
			Player player = GameObjectsStorage.getPlayer(objectId);
			if(player == null)
				live_list.remove(objectId);
		}

		for(Player player : getPlayers(live_list))
		{
			if(!player.isDead() && !player.isLogoutStarted())
				player.setTeam(TeamType.RED);
			else
				loosePlayer(player);
		}

		if(live_list.size() <= 1)
			endBattle();
	}

	public static void removeAura()
	{
		for(Player player : getPlayers(live_list))
		{
			player.setTeam(TeamType.NONE);
			player.setIsInLastHero(false);
		}
	}

	public static void clearArena()
	{
		for(GameObject obj : _zone.getObjects())
			if(obj != null)
			{
				Player player = obj.getPlayer();
				if(player != null && !live_list.contains(player.getObjectId()))
				{
					player.teleToLocation(getBackLocation(player), ReflectionManager.DEFAULT);
				}
			}
	}

	@Override
	public void onDeath(Creature self, Creature killer)
	{
		if(_progress.get() == PROGRESS_BATTLE && self.isPlayer() && self.getTeam() != TeamType.NONE && live_list.contains(self.getObjectId()))
		{
			Player player = (Player) self;
			loosePlayer(player);
			checkLive();
			if(killer != null && killer.isPlayer())
			{
				Player killerPlayer = killer.getPlayer();
				if(killerPlayer.expertiseIndex - player.expertiseIndex > 2 && !killerPlayer.getIP().equals(player.getIP()))
					if(Config.EVENT_LastHeroItemID > 0)
					{
						long itemCount = Math.round(Config.EVENT_LastHeroRate ? player.getLevel() * Config.EVENT_LastHeroItemCOUNT : 1 * Config.EVENT_LastHeroItemCOUNT);
						if(itemCount > 0)
							ItemFunctions.addItem((Player) killer, Config.EVENT_LastHeroItemID, itemCount, "Last Hero event on death reward");
					}
			}
			self.getPlayer().setIsInLastHero(false);
		}
	}

	@Override
	public void onTeleport(Player player, int x, int y, int z, Reflection reflection)
	{
		for(Zone zone : reflection.getZones())
		{
			if(zone.checkIfInZone(x, y, z, reflection))
				return;
		}
		onPlayerExit(player);
	}

	@Override
	public void onPlayerExit(Player player)
	{
		player.setIsInLastHero(false);

		if(player.getTeam() == TeamType.NONE)
			return;

		// Вышел или вылетел во время регистрации
		if(_progress.get() == PROGRESS_REGISTRATION && live_list.contains(player.getObjectId()))
		{
			removePlayer(player);
			return;
		}

		// Вышел или вылетел во время телепортации
		if(_progress.get() == PROGRESS_PREPARE && live_list.contains(player.getObjectId()))
		{
			removePlayer(player);
			return;
		}

		// Вышел или вылетел во время эвента
		if(_progress.get() == PROGRESS_BATTLE && live_list.contains(player.getObjectId()))
		{
			removePlayer(player);
			checkLive();
		}
	}

	private class ZoneListener implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature cha)
		{
			//
		}

		@Override
		public void onZoneLeave(Zone zone, Creature cha)
		{
			if(!cha.isPlayer())
				return;

			for(Zone z : reflection.getZones())
			{
				if(z.checkIfInZone(cha.getX(), cha.getY(), cha.getZ(), cha.getReflection()))
					return;
			}

			onPlayerExit(cha.getPlayer());
		}
	}

	private static void loosePlayer(Player player)
	{
		if(player != null)
		{
			live_list.remove(player.getObjectId());
			player.setTeam(TeamType.NONE);
			show(new CustomMessage("scripts.events.LastHero.YouLose", player), player);
		}
	}

	private static void removePlayer(Player player)
	{
		if(player != null)
		{
			live_list.remove(player.getObjectId());
			players_list.remove(player.getObjectId());
			boxes.remove(player.getObjectId());

			player.removeListener(_instance);
			player.setIsInLastHero(false);
			player.setTeam(TeamType.NONE);
			player.teleToLocation(getBackLocation(player), ReflectionManager.DEFAULT);
		}
	}

	private static List<Player> getPlayers(IntSet list)
	{
		List<Player> result = new ArrayList<Player>(list.size());
		for(int objectId : list.toArray())
		{
			Player player = GameObjectsStorage.getPlayer(objectId);
			if(player != null)
				result.add(player);
		}
		return result;
	}

	public static void buffPlayers()
	{
		for(Player player : getPlayers(players_list))
			if(player.isMageClass())
				mageBuff(player);
			else
				fighterBuff(player);

		for(Player player : getPlayers(live_list))
			if(player.isMageClass())
				mageBuff(player);
			else
				fighterBuff(player);
	}

	public static void scheduleEventStart()
	{
		try
		{
			Calendar currentTime = Calendar.getInstance();
			Calendar nextStartTime = null;
			Calendar testStartTime = null;

			for(String timeOfDay : Config.EVENT_LHStartTime)
			{
				testStartTime = Calendar.getInstance();
				testStartTime.setLenient(true);

				String[] splitTimeOfDay = timeOfDay.split(":");

				testStartTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTimeOfDay[0]));
				testStartTime.set(Calendar.MINUTE, Integer.parseInt(splitTimeOfDay[1]));

				if(testStartTime.getTimeInMillis() < currentTime.getTimeInMillis())
					testStartTime.add(Calendar.DAY_OF_MONTH, 1);

				if(nextStartTime == null || testStartTime.getTimeInMillis() < nextStartTime.getTimeInMillis())
					nextStartTime = testStartTime;

				if(_startTask != null)
				{
					_startTask.cancel(false);
					_startTask = null;
				}
				_startTask = ThreadPoolManager.getInstance().schedule(new StartTask(), nextStartTime.getTimeInMillis() - System.currentTimeMillis());

			}

			currentTime = null;
			nextStartTime = null;
			testStartTime = null;

		}
		catch(Exception e)
		{
			_log.warn("LH: Error figuring out a start time. Check TvTEventInterval in config file.");
		}
	}

	public static void mageBuff(Player player)
	{
		for(int i = 0; i < mage_buffs.length; i++)
		{
			buff = SkillHolder.getInstance().getSkill(mage_buffs[i][0], mage_buffs[i][1]);
			/*
			 * for(EffectTemplate et : buff.getEffectTemplates()) { Env env =
			 * new Env(player, player, buff); Effect effect = et.getEffect(env);
			 * effect.setPeriod(1200000); //20 минут player. getEffectList
			 * ().addEffect(effect); }
			 */
			if(player != null && buff != null)
				buff.getEffects(player, player, false, false);
		}
	}

	public static void fighterBuff(Player player)
	{
		for(int i = 0; i < fighter_buffs.length; i++)
		{
			buff = SkillHolder.getInstance().getSkill(fighter_buffs[i][0], fighter_buffs[i][1]);
			/*
			 * for(EffectTemplate et : buff.getEffectTemplates()) { Env env =
			 * new Env(player, player, buff); Effect effect = et.getEffect(env);
			 * effect.setPeriod(1200000); //20 минут player. getEffectList
			 * ().addEffect(effect); }
			 */
			if(player != null && buff != null)
				buff.getEffects(player, player, false, false);
		}
	}

	private static boolean checkDualBox(Player player)
	{
		if(!Config.EVENT_LHAllowMultiReg)
			if("IP".equalsIgnoreCase(Config.EVENT_LHCheckWindowMethod))
			{
				if(boxes.containsValue(player.getIP()))
				{
					show(new CustomMessage("scripts.events.LH.CancelledBox", player), player);
					return false;
				}
			}

			else if("HWid".equalsIgnoreCase(Config.EVENT_LHCheckWindowMethod))
			{
				String hwid = player.getNetConnection().getHWID();
				if(hwid != null && boxes.containsValue(hwid))
				{
					show(new CustomMessage("scripts.events.TvT.CancelledBox", player), player);
					return false;
				}
			}
		return true;
	}

	public static class StartTask extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			if(!_active)
				return;

			for(Residence c : ResidenceHolder.getInstance().getResidenceList(Castle.class))
				if(c.getSiegeEvent() != null && c.getSiegeEvent().isInProgress())
				{
					_log.debug("LH not started: CastleSiege in progress");
					return;
				}

			/*
			 * if(TerritorySiege.isInProgress()) {
			 * _log.debug("TvT not started: TerritorySiege in progress");
			 * return; }
			 */

			if(Config.EVENT_LHCategories)
				_instance.start(new String[] { "1", "1" });
			else
				_instance.start(new String[] { "-1", "-1" });
		}
	}

	public static final Location STABLE_LOCATION = new Location(-119664, 246306, 1232);

	private static Location getBackLocation(Player player)
	{
		String back = player.getVar("backCoords");
		if(back != null)
		{
			player.unsetVar("backCoords");
			return Location.parseLoc(back);
		}
		else
			return STABLE_LOCATION;
	}
}
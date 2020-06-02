package events.TvT;

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

import data.xml.holder.TvTArenaHolder;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
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
import l2s.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2s.gameserver.listener.actor.player.OnPlayerExitListener;
import l2s.gameserver.listener.actor.player.OnTeleportListener;
import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
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
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExBlockUpSetList;
import l2s.gameserver.network.l2.s2c.ExBlockUpSetState;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;
import l2s.gameserver.network.l2.s2c.RevivePacket;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.scripts.ScriptFile;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.templates.DoorTemplate;
import l2s.gameserver.templates.ZoneTemplate;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.ReflectionUtils;
import templates.TvTArena;

public class TvT extends Functions implements ScriptFile, OnDeathListener, OnTeleportListener, OnPlayerExitListener, OnPlayerEnterListener
{
	private static final Logger _log = LoggerFactory.getLogger(TvT.class);

	private static ScheduledFuture<?> _startTask;
	private static ScheduledFuture<?> _endTask;

	private static final IntSet players_list1 = new CArrayIntSet();
	private static final IntSet players_list2 = new CArrayIntSet();
	private static final IntSet live_list1 = new CArrayIntSet();
	private static final IntSet live_list2 = new CArrayIntSet();

	private static int[][] mage_buffs = new int[Config.EVENT_TvTMageBuffs.length][2];
	private static int[][] fighter_buffs = new int[Config.EVENT_TvTFighterBuffs.length][2];
	private static long _startedTime = 0;

	private static int[][] winnerRewards = new int[Config.EVENT_TvTRewards.length][2];
	private static int[][] looserRewards = new int[][]{
			//{ ItemTemplate.ITEM_ID_FAME, 225 },
			//{ 25000, 1 }
	};

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

	private static Reflection _reflection = null;
	private static TvTArena _arena = null;


	private static final TIntIntMap _killScore = new TIntIntHashMap();
	private static final TIntIntMap _playerScore = new TIntIntHashMap();

	private final ZoneListener _zoneListener = new ZoneListener();

	private static int bluePoints = 0;
	private static int redPoints = 0;

	private static TvT _instance;

	private static ScheduledFuture<?> _afkCheckTask = null;

	@Override
	public void onLoad()
	{
		_instance = this;

		_active = ServerVariables.getString("TvT", "off").equalsIgnoreCase("on");
		if(isActive())
			scheduleEventStart();

		int i = 0;

		if(Config.EVENT_TvTBuffPlayers && Config.EVENT_TvTMageBuffs.length != 0)
			for(String skill : Config.EVENT_TvTMageBuffs)
			{
				String[] splitSkill = skill.split(",");
				mage_buffs[i][0] = Integer.parseInt(splitSkill[0]);
				mage_buffs[i][1] = Integer.parseInt(splitSkill[1]);
				i++;
			}

		i = 0;

		if(Config.EVENT_TvTBuffPlayers && Config.EVENT_TvTMageBuffs.length != 0)
			for(String skill : Config.EVENT_TvTFighterBuffs)
			{
				String[] splitSkill = skill.split(",");
				fighter_buffs[i][0] = Integer.parseInt(splitSkill[0]);
				fighter_buffs[i][1] = Integer.parseInt(splitSkill[1]);
				i++;
			}

		i = 0;
		if(Config.EVENT_TvTRewards.length != 0)
			for(String reward : Config.EVENT_TvTRewards)
			{
				String[] splitReward = reward.split(",");
				winnerRewards[i][0] = Integer.parseInt(splitReward[0]);
				winnerRewards[i][1] = Integer.parseInt(splitReward[1]);
				i++;
			}

		_log.info("Loaded Event: TvT");
	}

	@Override
	public void onReload()
	{
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

	private static long getStarterTime()
	{
		return _startedTime;
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
			ServerVariables.set("TvT", "on");
			_log.info("Event 'TvT' activated.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.TvT.AnnounceEventStarted", null);
		}
		else
			player.sendMessage("Event 'TvT' already active.");

		_active = true;

		show("admin/events/events.htm", player);
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
			ServerVariables.unset("TvT");
			_log.info("Event 'TvT' deactivated.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.TvT.AnnounceEventStoped", null);
		}
		else
			player.sendMessage("Event 'TvT' not active.");

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
				return 99;
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

		TvTArena[] arenas = TvTArenaHolder.getInstance().getArenas();
		if(arenas == null || arenas.length == 0)
		{
			show(new CustomMessage("common.Error", player), player);
			return;
		}

		TvTArena arena = Rnd.get(arenas);
		if(arena == null)
		{
			show(new CustomMessage("common.Error", player), player);
			return;
		}

		if(!_progress.compareAndSet(PROGRESS_DISABLED, PROGRESS_REGISTRATION))
			return;

		_category = category;
		_autoContinue = autoContinue;
		_arena = arena;

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

		_time_to_start = Config.EVENT_TvTTime;

		_reflection = new Reflection();

		Map<String, ZoneTemplate> zones = new HashMap<String, ZoneTemplate>();
		for(String zone : _arena.getZones())
			zones.put(zone, ReflectionUtils.getZone(zone).getTemplate());

		IntObjectMap<DoorTemplate> doors = new HashIntObjectMap<DoorTemplate>();
		for(int doorId : _arena.getDoors())
			doors.put(doorId, ReflectionUtils.getDoor(doorId).getTemplate());

		_reflection.init(doors, zones);

		for(Zone zone : _reflection.getZones())
			zone.addListener(_zoneListener);

		players_list1.clear();
		players_list2.clear();
		live_list1.clear();
		live_list2.clear();

		_killScore.clear();
		_playerScore.clear();

		String[] param = { String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel) };
		sayToAll("scripts.events.TvT.AnnouncePreStart", param);

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
				player.scriptRequest(new CustomMessage("scripts.events.TvT.AskPlayer", player).toString(), "events.TvT.TvT:addPlayer", new Object[0]);
	}

	public static void announce()
	{
		if(_time_to_start > 1)
		{
			_time_to_start--;
			String[] param = { String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel) };
			sayToAll("scripts.events.TvT.AnnouncePreStart", param);
			ThreadPoolManager.getInstance().schedule(() -> announce(), 60000L);
		}
		else if(players_list1.isEmpty() || players_list2.isEmpty() || players_list1.size() < Config.EVENT_TvTMinPlayerInTeam || players_list2.size() < Config.EVENT_TvTMinPlayerInTeam)
		{
			sayToAll("scripts.events.TvT.AnnounceEventCancelled", null);
			boxes.clear();
			_progress.set(PROGRESS_DISABLED);
			ThreadPoolManager.getInstance().schedule(() -> autoContinue(), 10000L);
		}
		else
		{
			if(!_progress.compareAndSet(PROGRESS_REGISTRATION, PROGRESS_PREPARE))
				return;
			sayToAll("scripts.events.TvT.AnnounceEventStarting", null);
			ThreadPoolManager.getInstance().schedule(() -> prepare(), 5000L);
		}
	}

	public void addPlayer()
	{
		Player player = getSelf();
		if(player == null || !checkPlayer(player, true) || !checkDualBox(player))
			return;

		int team = 0, size1 = players_list1.size(), size2 = players_list2.size();

		if(size1 == Config.EVENT_TvTMaxPlayerInTeam && size2 == Config.EVENT_TvTMaxPlayerInTeam)
		{
			show(new CustomMessage("scripts.events.TvT.CancelledCount", player), player);
			return;
		}

		if(!Config.EVENT_TvTAllowMultiReg)
		{
			if("IP".equalsIgnoreCase(Config.EVENT_TvTCheckWindowMethod))
				boxes.put(player.getObjectId(), player.getIP());

			String hwid = player.getNetConnection().getHWID();
			if(hwid != null && "HWid".equalsIgnoreCase(Config.EVENT_TvTCheckWindowMethod))
				boxes.put(player.getObjectId(), hwid);
		}

		if(size1 > size2)
			team = 2;
		else if(size1 < size2)
			team = 1;
		else
			team = Rnd.get(1, 2);

		if(team == 1)
		{
			players_list1.add(player.getObjectId());
			live_list1.add(player.getObjectId());
			show(new CustomMessage("scripts.events.TvT.Registered", player), player);
		}
		else if(team == 2)
		{
			players_list2.add(player.getObjectId());
			live_list2.add(player.getObjectId());
			show(new CustomMessage("scripts.events.TvT.Registered", player), player);
		}
		else
			_log.info("WTF??? Command id 0 in TvT...");
	}

	public static boolean checkPlayer(Player player, boolean first)
	{
		if(first && (_progress.get() != PROGRESS_REGISTRATION || player.isDead()))
		{
			show(new CustomMessage("scripts.events.Late", player), player);
			return false;
		}

		if(first && (players_list1.contains(player.getObjectId()) || players_list2.contains(player.getObjectId())))
		{
			show(new CustomMessage("scripts.events.TvT.Cancelled", player), player);
			players_list1.remove(player.getObjectId());
			players_list2.remove(player.getObjectId());
			live_list1.remove(player.getObjectId());
			live_list2.remove(player.getObjectId());
			boxes.remove(player.getObjectId());
			return false;
		}

		if(player.getLevel() < _minLevel || player.getLevel() > _maxLevel)
		{
			show(new CustomMessage("scripts.events.TvT.CancelledLevel", player), player);
			return false;
		}

		if(player.isMounted())
		{
			show(new CustomMessage("scripts.events.TvT.Cancelled", player), player);
			return false;
		}

		if(player.isCursedWeaponEquipped())
		{
			show(new CustomMessage("scripts.events.CtF.Cancelled", player), player);
			return false;
		}

		if(player.isInDuel())
		{
			show(new CustomMessage("scripts.events.TvT.CancelledDuel", player), player);
			return false;
		}

		if(player.getTeam() != TeamType.NONE)
		{
			show(new CustomMessage("scripts.events.TvT.CancelledOtherEvent", player), player);
			return false;
		}

		if(player.getOlympiadGame() != null || first && Olympiad.isRegistered(player))
		{
			show(new CustomMessage("scripts.events.TvT.CancelledOlympiad", player), player);
			return false;
		}

		if(player.isInParty() && player.getParty().isInDimensionalRift())
		{
			show(new CustomMessage("scripts.events.TvT.CancelledOtherEvent", player), player);
			return false;
		}

		if(player.isInObserverMode())
		{
			show(new CustomMessage("scripts.event.TvT.CancelledObserver", player), player);
			return false;
		}

		if(player.isTeleporting())
		{
			show(new CustomMessage("scripts.events.TvT.CancelledTeleport", player), player);
			return false;
		}
		return true;
	}

	public static void prepare()
	{
		for(DoorInstance door : _reflection.getDoors())
			door.openMe();

		for(Zone z : _reflection.getZones())
			z.setType(ZoneType.peace_zone);

		cleanPlayers();
		ThreadPoolManager.getInstance().schedule(() -> ressurectPlayers(), 1000L);
		ThreadPoolManager.getInstance().schedule(() -> healPlayers(), 2000L);
		ThreadPoolManager.getInstance().schedule(() -> teleportPlayersToColiseum(), 3000L);
		ThreadPoolManager.getInstance().schedule(() -> paralyzePlayers(), 4000L);
		ThreadPoolManager.getInstance().schedule(() -> buffPlayers(), 5000L);
		ThreadPoolManager.getInstance().schedule(() -> go(), 60000L);

		sayToAll("scripts.events.TvT.AnnounceFinalCountdown", null);
	}

	public static void go()
	{
		if(!_progress.compareAndSet(PROGRESS_PREPARE, PROGRESS_BATTLE))
			return;

		upParalyzePlayers();
		checkLive();
		sayToAll("scripts.events.TvT.AnnounceFight", null);
		for(Zone z : _reflection.getZones())
			z.setType(ZoneType.battle_zone);
		_endTask = ThreadPoolManager.getInstance().schedule(() -> endBattle(), Config.EVENT_TvTEventRunningTime * 60000L);
		_startedTime = System.currentTimeMillis() + (Config.EVENT_TvTEventRunningTime * 60000);

		final ExBlockUpSetState.PointsInfo initialPoints = new ExBlockUpSetState.PointsInfo(600, bluePoints, redPoints);
		final ExBlockUpSetList.CloseUI cui = new ExBlockUpSetList.CloseUI();
		ExBlockUpSetState.ChangePoints clientSetUp;

		for(Player player : getPlayers(players_list1))
		{
			if(player == null)
				continue;

			player.setCurrentCp(player.getMaxCp());
			player.setCurrentHp(player.getMaxHp(), false);
			player.setCurrentMp(player.getMaxMp());

			clientSetUp = new ExBlockUpSetState.ChangePoints(600, bluePoints, redPoints, isRedTeam(player), player, 0);
			player.sendPacket(clientSetUp);
			player.sendActionFailed();
			player.sendPacket(initialPoints);
			player.sendPacket(cui);
			player.broadcastCharInfo();
			for(Player player1 : getPlayers(players_list1))
				player1.sendPacket(new ExBlockUpSetList.AddPlayer(player, isRedTeam(player)));
			for(Player player2 : getPlayers(players_list2))
				player2.sendPacket(new ExBlockUpSetList.AddPlayer(player, isRedTeam(player)));
		}

		for(Player player2 : getPlayers(players_list2))
		{
			if(player2 == null)
				continue;

			player2.setCurrentCp(player2.getMaxCp());
			player2.setCurrentHp(player2.getMaxHp(), false);
			player2.setCurrentMp(player2.getMaxMp());

			clientSetUp = new ExBlockUpSetState.ChangePoints(600, bluePoints, redPoints, isRedTeam(player2), player2, 0);
			player2.sendPacket(clientSetUp);
			player2.sendActionFailed();
			player2.sendPacket(initialPoints);
			player2.sendPacket(cui);
			player2.broadcastCharInfo();
			player2.sendPacket(new ExBlockUpSetList.AddPlayer(player2, isRedTeam(player2)));
			for(Player player : getPlayers(players_list1))
				player2.sendPacket(new ExBlockUpSetList.AddPlayer(player2, isRedTeam(player2)));
		}

		startAfkCheckTask();
	}

	public static void endBattle()
	{
		if(!_progress.compareAndSet(PROGRESS_BATTLE, PROGRESS_DISABLED))
			return;

		stopAfkCheckTask();
		removeAura();
		for(Zone z : _reflection.getZones())
			z.setType(ZoneType.peace_zone);
		boxes.clear();

		List<Player> players1 = getPlayers(players_list1);
		List<Player> players2 = getPlayers(players_list2);
		if(bluePoints > redPoints)
		{
			sayToAll("scripts.events.TvT.AnnounceFinishedBlueWins", null);
			giveItems(players1, winnerRewards, 1);
			giveItems(players2, looserRewards, 1);
		}
		else if(bluePoints < redPoints)
		{
			sayToAll("scripts.events.TvT.AnnounceFinishedRedWins", null);
			giveItems(players2, winnerRewards, 1);
			giveItems(players1, looserRewards, 1);
		}
		else if(bluePoints == redPoints)
		{
			sayToAll("scripts.events.TvT.AnnounceFinishedDraw", null);
			giveItems(players1, looserRewards, 1);
			giveItems(players2, looserRewards, 1);
		}

		sayToAll("scripts.events.TvT.AnnounceEnd", null);
		ThreadPoolManager.getInstance().schedule(() -> end(), 30000L);

		if(_endTask != null)
		{
			_endTask.cancel(false);
			_endTask = null;
		}
		boolean _isRedWinner = bluePoints < redPoints ? true : false;
		final ExBlockUpSetState.GameEnd end = new ExBlockUpSetState.GameEnd(_isRedWinner);

		for(Player player : getPlayers(players_list1))
			player.sendPacket(end);

		for(Player player : getPlayers(players_list2))
			player.sendPacket(end);
		bluePoints = 0;
		redPoints = 0;
		_startedTime = 0;
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
		live_list1.clear();
		live_list2.clear();
		players_list1.clear();
		players_list2.clear();

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

	public static void giveItems(List<Player> players, int[][] rewards, double rate)
	{
		for(Player player : players)
		{
			for(int i = 0; i < rewards.length; i++)
			{
				long itemCount = Math.round((Config.EVENT_TvTrate ? player.getLevel() : 1) * rewards[i][1] * rate);
				if(itemCount > 0) {
					int itemId = rewards[i][0];
					if(itemId > 0)
						ItemFunctions.addItem(player, itemId, itemCount, "TvT event win reward");
					else if(itemId == ItemTemplate.ITEM_ID_FAME)
						player.getPlayer().setFame((int) (itemCount + player.getPlayer().getFame()), "TvT reward");
				}
			}
		}
	}

	public static void teleportPlayersToColiseum()
	{
		List<Location> teleportLocations = _arena.getTeleportLocations(TeamType.RED.ordinal());
		for(Player player : getPlayers(players_list1))
		{
			unRide(player);

			if(!Config.EVENT_TvTAllowSummons)
				unSummonPet(player, true);

			DuelEvent duel = player.getEvent(DuelEvent.class);
			if(duel != null)
				duel.abortDuel(player);

			Location loc = Location.findPointToStay(teleportLocations.get(Rnd.get(teleportLocations.size())), 50, _reflection.getGeoIndex());
			player.addListener(_instance);
			player.setVar("backCoords", player.getLoc().toXYZString(), -1);
			player.teleToLocation(loc, _reflection);
			player.setIsInTvT(true);
			player.setTeam(TeamType.RED);
			if(!Config.EVENT_TvTAllowBuffs)
			{
				player.getEffectList().stopAllEffects();
				EffectsDAO.getInstance().deleteSummonsEffects(player); // Удаляем все эффекты непризванных саммонов.
				if(player.getServitor() != null)
					player.getServitor().getEffectList().stopAllEffects();
			}
		}

		teleportLocations = _arena.getTeleportLocations(TeamType.BLUE.ordinal());
		for(Player player : getPlayers(players_list2))
		{
			unRide(player);

			if(!Config.EVENT_TvTAllowSummons)
				unSummonPet(player, true);

			DuelEvent duel = player.getEvent(DuelEvent.class);
			if(duel != null)
				duel.abortDuel(player);

			Location loc = Location.findPointToStay(teleportLocations.get(Rnd.get(teleportLocations.size())), 50, _reflection.getGeoIndex());
			player.addListener(_instance);
			player.setVar("backCoords", player.getLoc().toXYZString(), -1);
			player.teleToLocation(loc, _reflection);
			player.setIsInTvT(true);
			player.setTeam(TeamType.BLUE);
			if(!Config.EVENT_TvTAllowBuffs)
			{
				player.getEffectList().stopAllEffects();
				EffectsDAO.getInstance().deleteSummonsEffects(player); // Удаляем все эффекты непризванных саммонов.
				if(player.getServitor() != null)
					player.getServitor().getEffectList().stopAllEffects();
			}
		}
	}

	public static void teleportPlayers()
	{
		for(Player player : getPlayers(players_list1))
		{
			player.teleToLocation(getBackLocation(player), ReflectionManager.DEFAULT);
		}

		for(Player player : getPlayers(players_list2))
		{
			player.teleToLocation(getBackLocation(player), ReflectionManager.DEFAULT);
		}
	}

	public static void paralyzePlayers()
	{
		for(Player player : getPlayers(players_list1))
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
		for(Player player : getPlayers(players_list2))
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
		for(Player player : getPlayers(players_list1))
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
		for(Player player : getPlayers(players_list2))
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
		for(Player player : getPlayers(players_list1))
			if(player.isDead())
			{
				player.restoreExp();
				player.setCurrentCp(player.getMaxCp());
				player.setCurrentHp(player.getMaxHp(), true);
				player.setCurrentMp(player.getMaxMp());
				player.broadcastPacket(new RevivePacket(player));
			}
		for(Player player : getPlayers(players_list2))
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
		for(Player player : getPlayers(players_list1))
		{
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
		}
		for(Player player : getPlayers(players_list2))
		{
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
		}
	}

	public static void cleanPlayers()
	{
		for(Player player : getPlayers(players_list1))
			if(!checkPlayer(player, false))
				removePlayer(player);
		for(Player player : getPlayers(players_list2))
			if(!checkPlayer(player, false))
				removePlayer(player);
	}

	public static synchronized void checkLive()
	{
		for(int objectId : live_list1.toArray())
		{
			Player player = GameObjectsStorage.getPlayer(objectId);
			if(player == null)
				live_list1.remove(objectId);
		}

		for(int objectId : live_list2.toArray())
		{
			Player player = GameObjectsStorage.getPlayer(objectId);
			if(player == null)
				live_list2.remove(objectId);
		}

		for(Player player : getPlayers(live_list1))
		{
			if(!player.isDead() && !player.isLogoutStarted())
				player.setTeam(TeamType.RED);
			else
				removePlayer(player);
		}

		for(Player player : getPlayers(live_list2))
		{
			if(!player.isDead() && !player.isLogoutStarted())
				player.setTeam(TeamType.BLUE);
			else
				removePlayer(player);
		}

		if(live_list1.size() < 1 || live_list2.size() < 1)
			endBattle();
	}

	public static void removeAura()
	{
		for(Player player : getPlayers(live_list1))
		{
			player.setTeam(TeamType.NONE);
			if(player.getServitor() != null)
				player.getServitor().setTeam(TeamType.NONE);
			player.setIsInTvT(false);
		}
		for(Player player : getPlayers(live_list2))
		{
			player.setTeam(TeamType.NONE);
			if(player.getServitor() != null)
				player.getServitor().setTeam(TeamType.NONE);
			player.setIsInTvT(false);
		}
	}

	@Override
	public void onDeath(Creature self, Creature killer)
	{
		if(_progress.get() == PROGRESS_BATTLE && self.isPlayer() && self.getTeam() != TeamType.NONE && (live_list1.contains(self.getObjectId()) || live_list2.contains(self.getObjectId())))
		{
			checkKillsAndAnnounce(killer.getPlayer());
			increasePoints(killer);
			ThreadPoolManager.getInstance().schedule(() -> resurrectAtBase(self), 10000L);
			_killScore.remove(self.getPlayer().getObjectId());
		}

	}

	private static void checkKillsAndAnnounce(Player player)
	{
		if(player == null)
			return;

		int killScore = _killScore.get(player.getObjectId()) + 1;

		_killScore.put(player.getObjectId(), killScore);
		_playerScore.put(player.getObjectId(), _playerScore.get(player.getObjectId()) + 1);

		String text = "";

		switch(killScore)
		{
			case 0:
			case 1:
				return;
			case 10:
				text = "" + player.getName() + ": Killing Spree";
				break;
			case 20:
				text = "" + player.getName() + ": Rampage";
				break;
			case 30:
				text = "" + player.getName() + ": Unstoppable";
				break;
			case 40:
				text = "" + player.getName() + ": Dominating";
				break;
			case 50:
				text = "" + player.getName() + ": Godlike";
				break;
			case 60:
				text = "" + player.getName() + ": Legendary";
				break;
			case 70:
				text = "" + player.getName() + ": Arena Master";
				break;
			case 80:
				text = "" + player.getName() + ": Best Player";
				break;
			default:
				return;
		}
		for(Player player1 : getPlayers(players_list1))
			player1.sendPacket(new ExShowScreenMessage(NpcString.NONE, 3000, ScreenMessageAlign.TOP_CENTER, true, text));

		for(Player player2 : getPlayers(players_list2))
			player2.sendPacket(new ExShowScreenMessage(NpcString.NONE, 3000, ScreenMessageAlign.TOP_CENTER, true, text));
	}

	public static void resurrectAtBase(Creature self)
	{
		Player player = self.getPlayer();
		if(player == null)
			return;
		if(player.getTeam() == TeamType.NONE)
			return;
		if(player.isDead())
		{
			player.setCurrentCp(player.getMaxCp());
			player.setCurrentHp(player.getMaxHp(), true);
			player.setCurrentMp(player.getMaxMp());
			player.broadcastPacket(new RevivePacket(player));
			buffPlayer(player);
		}
		List<Location> teleportLocations = _arena.getTeleportLocations(player.getTeam().ordinal());
		Location loc = Location.findPointToStay(teleportLocations.get(Rnd.get(teleportLocations.size())), 50, _reflection.getGeoIndex());
		player.teleToLocation(loc, _reflection);
	}

	public static void buffPlayer(Player player)
	{
		if(player.isMageClass())
			mageBuff(player);
		else
			fighterBuff(player);
	}

	private static void increasePoints(Creature killer)
	{
		Player player = killer.getPlayer();
		if(player == null)
			return;

		if(player.getTeam() == TeamType.BLUE)
			bluePoints++;
		else if(player.getTeam() == TeamType.RED)
			redPoints++;
		else
			return;

		int timeLeft = (int) ((getStarterTime() - System.currentTimeMillis()) / 1000);

		if(player.getTeam() == TeamType.RED)
		{
			for(Player player1 : getPlayers(players_list1))
				player1.sendPacket(new ExBlockUpSetState.ChangePoints(timeLeft, bluePoints, redPoints, true, player, getPlayerScore(player)));
			for(Player player2 : getPlayers(players_list2))
				player2.sendPacket(new ExBlockUpSetState.ChangePoints(timeLeft, bluePoints, redPoints, true, player, getPlayerScore(player)));
			for(Player player1 : getPlayers(players_list1))
				player1.sendPacket(new ExBlockUpSetState.ChangePoints(timeLeft, bluePoints, redPoints, false, player, getPlayerScore(player)));
			for(Player player2 : getPlayers(players_list2))
				player2.sendPacket(new ExBlockUpSetState.ChangePoints(timeLeft, bluePoints, redPoints, false, player, getPlayerScore(player)));
		}
		else
		{
			for(Player player1 : getPlayers(players_list1))
				player1.sendPacket(new ExBlockUpSetState.ChangePoints(timeLeft, bluePoints, redPoints, false, player, getPlayerScore(player)));
			for(Player player2 : getPlayers(players_list2))
				player2.sendPacket(new ExBlockUpSetState.ChangePoints(timeLeft, bluePoints, redPoints, false, player, getPlayerScore(player)));
			for(Player player1 : getPlayers(players_list1))
				player1.sendPacket(new ExBlockUpSetState.ChangePoints(timeLeft, bluePoints, redPoints, true, player, getPlayerScore(player)));
			for(Player player2 : getPlayers(players_list2))
				player2.sendPacket(new ExBlockUpSetState.ChangePoints(timeLeft, bluePoints, redPoints, true, player, getPlayerScore(player)));
		}
	}

	@Override
	public void onTeleport(Player player, int x, int y, int z, Reflection reflection)
	{
		for(Zone zone : _reflection.getZones())
		{
			if(zone.checkIfInZone(x, y, z, reflection))
				return;
		}
		onPlayerExit(player);
	}

	@Override
	public void onPlayerExit(Player player)
	{
		if(_progress.get() == PROGRESS_PREPARE && (live_list1.contains(player.getObjectId()) || live_list2.contains(player.getObjectId())))
			player.setVar("TvTParaVar", "1", 300000); //5min is enough

		if(player.getTeam() == TeamType.NONE)
			return;

		if(_progress.get() == PROGRESS_REGISTRATION && (live_list1.contains(player.getObjectId()) || live_list2.contains(player.getObjectId())))
		{
			removePlayer(player);
			return;
		}

		if(_progress.get() == PROGRESS_PREPARE && (live_list1.contains(player.getObjectId()) || live_list2.contains(player.getObjectId())))
		{
			removePlayer(player);
			return;
		}

		if(_progress.get() == PROGRESS_BATTLE && (live_list1.contains(player.getObjectId()) || live_list2.contains(player.getObjectId())))
		{
			removePlayer(player);
			checkLive();
		}
	}

	@Override
	public void onPlayerEnter(Player player)
	{
		if(_progress.get() == PROGRESS_PREPARE && player.getVar("TvTParaVar") != null)
			player.startParalyzed();
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

			for(Zone z : _reflection.getZones())
			{
				if(z.checkIfInZone(cha.getX(), cha.getY(), cha.getZ(), cha.getReflection()))
					return;
			}

			onPlayerExit(cha.getPlayer());
		}
	}

	private static void removePlayer(Player player)
	{
		if(player != null)
		{
			live_list1.remove(player.getObjectId());
			live_list2.remove(player.getObjectId());
			players_list1.remove(player.getObjectId());
			players_list2.remove(player.getObjectId());
			boxes.remove(player.getObjectId());

			player.removeListener(_instance);
			player.setIsInTvT(false);

			for(Player player1 : getPlayers(players_list1))
				player1.sendPacket(new ExBlockUpSetList.RemovePlayer(player, player.getTeam() == TeamType.RED ? true : false));

			for(Player player2 : getPlayers(players_list2))
				player2.sendPacket(new ExBlockUpSetList.RemovePlayer(player, player.getTeam() == TeamType.RED ? true : false));

			player.sendPacket(new ExBlockUpSetState.GameEnd(false));
			player.setTeam(TeamType.NONE);
			player.teleToLocation(getBackLocation(player), ReflectionManager.DEFAULT);
		}
	}

	private static List<Player> getPlayers(IntSet list)
	{
		List<Player> result = new ArrayList<Player>();
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
		for(Player player : getPlayers(players_list1))
			if(player.isMageClass())
				mageBuff(player);
			else
				fighterBuff(player);

		for(Player player : getPlayers(players_list2))
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

			for(String timeOfDay : Config.EVENT_TvTStartTime)
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
			_log.warn("TvT: Error figuring out a start time. Check TvTEventInterval in config file.");
		}
	}

	public static void mageBuff(Player player)
	{
		for(int i = 0; i < mage_buffs.length; i++)
		{
			buff = SkillHolder.getInstance().getSkill(mage_buffs[i][0], mage_buffs[i][1]);
			if(buff == null)
				return;
			buff.getEffects(player, player, false, false);
		}
	}

	public static void fighterBuff(Player player)
	{
		for(int i = 0; i < fighter_buffs.length; i++)
		{
			buff = SkillHolder.getInstance().getSkill(fighter_buffs[i][0], fighter_buffs[i][1]);
			if(buff == null)
				return;
			buff.getEffects(player, player, false, false);
		}
	}

	private static boolean checkDualBox(Player player)
	{
		if(!Config.EVENT_TvTAllowMultiReg)
			if("IP".equalsIgnoreCase(Config.EVENT_TvTCheckWindowMethod))
			{
				if(boxes.containsValue(player.getIP()))
				{
					show(new CustomMessage("scripts.events.TvT.CancelledBox", player), player);
					return false;
				}
			}
			else if("HWid".equalsIgnoreCase(Config.EVENT_TvTCheckWindowMethod))
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
					_log.debug("TvT not started: CastleSiege in progress");
					return;
				}

			if(Config.EVENT_TvTCategories)
				_instance.start(new String[] { "1", "1" });
			else
				_instance.start(new String[] { "-1", "-1" });
		}
	}

	/**
	 * @param player
	 * @return Returns personal player score
	 */
	public static int getPlayerScore(Player player)
	{
		return _playerScore.get(player.getObjectId());
	}

	private static boolean isRedTeam(Player player)
	{
		if(player.getTeam() == TeamType.RED)
			return true;
		return false;
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

	private static void startAfkCheckTask() {
		if(_afkCheckTask != null)
			return;

		_afkCheckTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if(_progress.get() == PROGRESS_BATTLE) {
				for(Player player : getPlayers(players_list1)) {
					if((System.currentTimeMillis() - player.getLastNotAfkTime()) > 30000L) {
						if (live_list1.contains(player.getObjectId())) {
							player.sendMessage(player.isLangRus() ? "Вы выкинуты с ивента из-за долгого бездействия." : "You are thrown out of the event due to long inactivity.");
							removePlayer(player);
							checkLive();
						}
					}
				}

				for(Player player : getPlayers(players_list2)) {
					if((System.currentTimeMillis() - player.getLastNotAfkTime()) > 30000L) {
						if (live_list2.contains(player.getObjectId())) {
							player.sendMessage(player.isLangRus() ? "Вы выкинуты с ивента из-за долгого бездействия." : "You are thrown out of the event due to long inactivity.");
							removePlayer(player);
							checkLive();
						}
					}
				}
			}
		}, 1000L, 1000L);
	}

	private static void stopAfkCheckTask() {
		if(_afkCheckTask != null) {
			_afkCheckTask.cancel(false);
			_afkCheckTask = null;
		}
	}
}
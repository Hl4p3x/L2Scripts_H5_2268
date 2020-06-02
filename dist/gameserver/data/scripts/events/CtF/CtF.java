package events.CtF;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.ArrayUtils;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.CArrayIntSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.geometry.Polygon;
import l2s.commons.threading.RunnableImpl;
import l2s.commons.util.Rnd;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.dao.EffectsDAO;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.listener.actor.player.OnPlayerExitListener;
import l2s.gameserver.listener.actor.player.OnTeleportListener;
import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Territory;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.impl.DuelEvent;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.instances.DoorInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.attachment.FlagItemAttachment;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.RevivePacket;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.scripts.ScriptFile;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.templates.DoorTemplate;
import l2s.gameserver.templates.ZoneTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.ReflectionUtils;

public class CtF extends Functions implements ScriptFile, OnDeathListener, OnTeleportListener, OnPlayerExitListener
{
	private static class CtFCombatFlagObject implements FlagItemAttachment
	{
		private ItemInstance _item = null;

		@Override
		public void setItem(ItemInstance item)
		{
			_item = item;
		}

		@Override
		public boolean canPickUp(Player player)
		{
			return true;
		}

		@Override
		public void pickUp(Player player)
		{
			//
		}

		@Override
		public void onLogout(Player player)
		{
			dropFlag(player);
		}

		@Override
		public void onDeath(Player owner, Creature killer)
		{
			dropFlag(owner);
		}

		@Override
		public boolean canAttack(Player player)
		{
			player.sendPacket(SystemMsg.THAT_WEAPON_CANNOT_PERFORM_ANY_ATTACKS);
			return false;
		}

		@Override
		public boolean canCast(Player player, Skill skill)
		{
			if(_item != null)
			{
				Skill[] skills = _item.getTemplate().getAttachedSkills();
				if(!ArrayUtils.contains(skills, skill))
				{
					player.sendPacket(SystemMsg.THAT_WEAPON_CANNOT_USE_ANY_OTHER_SKILL_EXCEPT_THE_WEAPONS_SKILL);
					return false;
				}
			}
			return true;
		}
	}

	private static final Logger _log = LoggerFactory.getLogger(CtF.class);

	private static ScheduledFuture<?> _startTask;

	private static final int[] DOOR_IDS = { 24190001, 24190002, 24190003, 24190004 };

	/** <font color=blue>Blue</font> */
	private static final IntSet players_list1 = new CArrayIntSet();
	/** <font color=red>Red</font> */
	private static final IntSet players_list2 = new CArrayIntSet();

	private static NpcInstance redFlag = null;
	private static NpcInstance blueFlag = null;

	private static Skill buff;

	private static int[][] rewards = new int[Config.EVENT_CtFRewards.length][2];

	private static int[][] mage_buffs;
	private static int[][] fighter_buffs;


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

	private static ScheduledFuture<?> _endTask;

	private static Reflection _reflection = ReflectionManager.CTF_EVENT;

	private static final List<Zone> _zones = new ArrayList<Zone>();
	private static Zone _blueBaseZone;
	private static Zone _redBaseZone;

	private static BlueBaseZoneListener _blueBaseZoneListener = new BlueBaseZoneListener();
	private static RedBaseZoneListener _redBaseZoneListener = new RedBaseZoneListener();

	private final ZoneListener _zoneListener = new ZoneListener();

	private static final IntObjectMap<String> boxes = new CHashIntObjectMap<String>();

	private static Territory team1spawn = new Territory().add(new Polygon().add(149878, 47505).add(150262, 47513).add(150502, 47233).add(150507, 46300).add(150256, 46002).add(149903, 46005).setZmin(-3408).setZmax(-3308));

	private static Territory team2spawn = new Territory().add(new Polygon().add(149027, 46005).add(148686, 46003).add(148448, 46302).add(148449, 47231).add(148712, 47516).add(149014, 47527).setZmin(-3408).setZmax(-3308));

	private static Location blueFlagLoc = new Location(150760, 45848, -3408);
	private static Location redFlagLoc = new Location(148232, 47688, -3408);

	private static CtF _instance;

	@Override
	public void onLoad()
	{
		_instance = this;

		IntObjectMap<DoorTemplate> doors = new HashIntObjectMap<DoorTemplate>();
		Map<String, ZoneTemplate> zones = new HashMap<String, ZoneTemplate>();
		zones.put("[colosseum_battle_1]", ReflectionUtils.getZone("[colosseum_battle_1]").getTemplate());
		zones.put("[colosseum_battle_2]", ReflectionUtils.getZone("[colosseum_battle_2]").getTemplate());
		zones.put("[colosseum_battle_3]", ReflectionUtils.getZone("[colosseum_battle_3]").getTemplate());
		zones.put("[colosseum_ctf_blue_base]", ReflectionUtils.getZone("[colosseum_ctf_blue_base]").getTemplate());
		zones.put("[colosseum_ctf_red_base]", ReflectionUtils.getZone("[colosseum_ctf_red_base]").getTemplate());
		for(final int doorId : DOOR_IDS)
			doors.put(doorId, ReflectionUtils.getDoor(doorId).getTemplate());

		_reflection.init(doors, zones);

		_zones.add(_reflection.getZone("[colosseum_battle_1]"));
		_zones.add(_reflection.getZone("[colosseum_battle_2]"));
		_zones.add(_reflection.getZone("[colosseum_battle_3]"));
		_blueBaseZone = _reflection.getZone("[colosseum_ctf_blue_base]");
		_redBaseZone = _reflection.getZone("[colosseum_ctf_red_base]");

		for(Zone zone : _zones)
			zone.addListener(_zoneListener);
		_blueBaseZone.addListener(_blueBaseZoneListener);
		_redBaseZone.addListener(_redBaseZoneListener);

		for(final int doorId : DOOR_IDS)
			_reflection.getDoor(doorId).closeMe();

		_active = ServerVariables.getString("CtF", "off").equalsIgnoreCase("on");

		if(isActive())
			scheduleEventStart();

		if(Config.EVENT_CtFBuffPlayers && Config.EVENT_CtFMageBuffs.length > 0)
			mage_buffs = new int[Config.EVENT_CtFMageBuffs.length][2];

		if(Config.EVENT_CtFBuffPlayers && Config.EVENT_CtFFighterBuffs.length > 0)
			fighter_buffs = new int[Config.EVENT_CtFFighterBuffs.length][2];

		int i = 0;

		if(Config.EVENT_CtFBuffPlayers && Config.EVENT_CtFMageBuffs.length > 0)
			for(String skill : Config.EVENT_CtFMageBuffs)
			{
				String[] splitSkill = skill.split(",");
				mage_buffs[i][0] = Integer.parseInt(splitSkill[0]);
				mage_buffs[i][1] = Integer.parseInt(splitSkill[1]);
				i++;
			}

		i = 0;

		if(Config.EVENT_CtFBuffPlayers && Config.EVENT_CtFMageBuffs.length != 0)
			for(String skill : Config.EVENT_CtFFighterBuffs)
			{
				String[] splitSkill = skill.split(",");
				fighter_buffs[i][0] = Integer.parseInt(splitSkill[0]);
				fighter_buffs[i][1] = Integer.parseInt(splitSkill[1]);
				i++;
			}

		i = 0;
		if(Config.EVENT_CtFRewards.length != 0)
			for(String reward : Config.EVENT_CtFRewards)
			{
				String[] splitReward = reward.split(",");
				rewards[i][0] = Integer.parseInt(splitReward[0]);
				rewards[i][1] = Integer.parseInt(splitReward[1]);
				i++;
			}

		_log.info("Loaded Event: CtF [" + _active + "]");
	}

	@Override
	public void onReload()
	{
		for(Zone zone : _zones)
			zone.removeListener(_zoneListener);
		_redBaseZone.removeListener(_redBaseZoneListener);
		_blueBaseZone.removeListener(_blueBaseZoneListener);
		if(_startTask != null)
			_startTask.cancel(true);
	}

	@Override
	public void onShutdown()
	{
		onReload();
	}

	private static boolean _active = false;

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
			// Activation Event, if it is not activated, then try to start. Since both TASK starts only at boot
			if(_startTask == null)
				scheduleEventStart();

			ServerVariables.set("CtF", "on");
			_log.info("Event 'CtF' activated.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.CtF.AnnounceEventStarted", null);
		}
		else
			player.sendMessage("Event 'CtF' already active.");

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
				_startTask.cancel(true);
				_startTask = null;
			}
			ServerVariables.unset("CtF");
			_log.info("Event 'CtF' deactivated.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.CtF.AnnounceEventStoped", null);
		}
		else
			player.sendMessage("Event 'CtF' not active.");

		_active = false;

		show("admin/events/events.htm", player);
	}

	public boolean isRunned()
	{
		return _progress.get() > 0;
	}

	/*	public String DialogAppend_31225(Integer val) {
			if(val == 0) {
				Player player = getSelf();
				return HtmCache.getInstance().getHtml("scripts/events/ctf/31225.htm", player);
			}
			return "";
		}
	*/
	public String DialogAppend_35423(Integer val)
	{
		Player player = getSelf();
		if(player.getTeam() != TeamType.BLUE)
			return "";
		if(val == 0)
			return HtmCache.getInstance().getHtml("scripts/events/ctf/35423.htm", player).replaceAll("n1", "" + Rnd.get(100, 999)).replaceAll("n2", "" + Rnd.get(100, 999));
		return "";
	}

	// Blue flag
	public String DialogAppend_35426(Integer val)
	{
		Player player = getSelf();
		if(player.getTeam() != TeamType.RED)
			return "";
		if(val == 0)
			return HtmCache.getInstance().getHtml("scripts/events/ctf/35426.htm", player).replaceAll("n1", "" + Rnd.get(100, 999)).replaceAll("n2", "" + Rnd.get(100, 999));
		return "";
	}

	public void capture(String[] var)
	{
		Player player = getSelf();
		if(var.length != 4)
		{
			_log.info("var length problem " + var.length);
			show(new CustomMessage("common.Error", player), player);
			return;
		}

		NpcInstance npc = getNpc();

		if(player.isDead() || npc == null || !player.isInRange(npc, 200))
		{
			_log.info("Radius problem");
			show(new CustomMessage("common.Error", player), player);
			return;
		}

		Integer base;
		Integer add1;
		Integer add2;
		Integer summ;
		try
		{
			base = Integer.valueOf(var[0]);
			add1 = Integer.valueOf(var[1]);
			add2 = Integer.valueOf(var[2]);
			summ = Integer.valueOf(var[3]);
		}
		catch(Exception e)
		{
			_log.info("Exception");
			e.printStackTrace();
			show(new CustomMessage("common.Error", player), player);
			return;
		}

		if(add1.intValue() + add2.intValue() != summ.intValue())
		{
			show(new CustomMessage("common.Error", player), player);
			return;
		}

		if(base == 1 && blueFlag.isVisible()) // Blue base
		{
			blueFlag.decayMe();
			addFlag(player, 13561);
		}

		if(base == 2 && redFlag.isVisible()) // Red base
		{
			redFlag.decayMe();
			addFlag(player, 13560);
		}

		player.getEffectList().stopAllSkillEffects(EffectType.Invisible);
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

		_time_to_start = Config.EVENT_CtfTime;

		players_list1.clear();
		players_list2.clear();

		if(redFlag != null)
			redFlag.deleteMe();
		if(blueFlag != null)
			blueFlag.deleteMe();

		redFlag = spawn(redFlagLoc, 35423, _reflection);
		blueFlag = spawn(blueFlagLoc, 35426, _reflection);
		redFlag.decayMe();
		blueFlag.decayMe();

		String[] param = { String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel) };
		sayToAll("scripts.events.CtF.AnnouncePreStart", param);

		ThreadPoolManager.getInstance().schedule(() -> question(), 10000);
		ThreadPoolManager.getInstance().schedule(() -> announce(), 60000);
		_log.info("CtF: start event [" + _category + "-" + _autoContinue + "]");
	}

	public static void sayToAll(String address, String[] replacements)
	{
		Announcements.getInstance().announceByCustomMessage(address, replacements, ChatType.CRITICAL_ANNOUNCE);
	}

	public static void question()
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			if(player != null && !player.isDead() && player.getLevel() >= _minLevel && player.getLevel() <= _maxLevel && player.getReflection().isDefault() && !player.isInOlympiadMode() && !player.isInObserverMode())
				player.scriptRequest(new CustomMessage("scripts.events.CtF.AskPlayer", player).toString(), "events.CtF.CtF:addPlayer", new Object[0]);
	}

	public static void announce()
	{
		if(_time_to_start > 1)
		{
			_time_to_start--;
			String[] param = { String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel) };
			sayToAll("scripts.events.CtF.AnnouncePreStart", param);
			ThreadPoolManager.getInstance().schedule(() -> announce(), 60000);
		}
		else if(players_list1.isEmpty() || players_list2.isEmpty())
		{
			sayToAll("scripts.events.CtF.AnnounceEventCancelled", null);
			boxes.clear();
			_progress.set(PROGRESS_DISABLED);
			ThreadPoolManager.getInstance().schedule(() -> autoContinue(), 10000);
			return;
		}
		else
		{
			if(!_progress.compareAndSet(PROGRESS_REGISTRATION, PROGRESS_PREPARE))
				return;
			sayToAll("scripts.events.CtF.AnnounceEventStarting", null);
			ThreadPoolManager.getInstance().schedule(() -> prepare(), 5000);
		}
	}

	public void addPlayer()
	{
		Player player = getSelf();
		if(player == null || !checkPlayer(player, true) || !checkDualBox(player))
			return;

		int team = 0, size1 = players_list1.size(), size2 = players_list2.size();

		if(!Config.EVENT_TvTAllowMultiReg)
		{
			if("IP".equalsIgnoreCase(Config.EVENT_CtFCheckWindowMethod))
				boxes.put(player.getObjectId(), player.getIP());

			String hwid = player.getNetConnection().getHWID();
			if(hwid != null && "HWid".equalsIgnoreCase(Config.EVENT_CtFCheckWindowMethod))
				boxes.put(player.getObjectId(), hwid);
		}

		if(size1 > size2)
			team = 2;
		else if(size1 < size2)
			team = 1;
		else
			team = Rnd.get(1, 2);

		if(!checkCountTeam(team))
		{
			show(new CustomMessage("scripts.events.CtF.MaxCountTeam", player), player);
			return;
		}

		if(team == 1)
		{
			players_list1.add(player.getObjectId());
			show(new CustomMessage("scripts.events.CtF.Registered", player), player);
		}
		else if(team == 2)
		{
			players_list2.add(player.getObjectId());
			show(new CustomMessage("scripts.events.CtF.Registered", player), player);
		}
		else
			_log.info("WTF??? Command id 0 in CtF...");
	}

	private static boolean checkCountTeam(int team)
	{
		if(team == 1 && players_list1.size() >= Config.EVENT_CtFMaxPlayerInTeam)
			return false;
		else if(team == 2 && players_list2.size() >= Config.EVENT_CtFMaxPlayerInTeam)
			return false;

		return true;
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
			show(new CustomMessage("scripts.events.CtF.Cancelled", player), player);
			players_list1.remove(player.getObjectId());
			players_list2.remove(player.getObjectId());
			boxes.remove(player.getObjectId());
			return false;
		}

		if(first && (players_list1.contains(player.getObjectId()) || players_list2.contains(player.getObjectId())))
		{
			show(new CustomMessage("scripts.events.CtF.Cancelled", player), player);
			return false;
		}

		if(player.getLevel() < _minLevel || player.getLevel() > _maxLevel)
		{
			show(new CustomMessage("scripts.events.CtF.CancelledLevel", player), player);
			return false;
		}

		if(player.isMounted())
		{
			show(new CustomMessage("scripts.events.CtF.Cancelled", player), player);
			return false;
		}

		if(player.isCursedWeaponEquipped())
		{
			show(new CustomMessage("scripts.events.CtF.Cancelled", player), player);
			return false;
		}

		if(player.isInDuel())
		{
			show(new CustomMessage("scripts.events.CtF.CancelledDuel", player), player);
			return false;
		}

		if(player.getTeam() != TeamType.NONE)
		{
			show(new CustomMessage("scripts.events.CtF.CancelledOtherEvent", player), player);
			return false;
		}

		if(player.isInOlympiadMode() || first && Olympiad.isRegistered(player))
		{
			show(new CustomMessage("scripts.events.CtF.CancelledOlympiad", player), player);
			return false;
		}

		if(player.isInParty() && player.getParty().isInDimensionalRift())
		{
			show(new CustomMessage("scripts.events.CtF.CancelledOtherEvent", player), player);
			return false;
		}

		if(player.isTeleporting())
		{
			show(new CustomMessage("scripts.events.CtF.CancelledTeleport", player), player);
			return false;
		}

		return true;
	}

	public static void prepare()
	{
		closeColiseumDoors();

		cleanPlayers();
		clearArena();

		redFlag.spawnMe();
		blueFlag.spawnMe();

		ThreadPoolManager.getInstance().schedule(() -> ressurectPlayers(), 1000L);
		ThreadPoolManager.getInstance().schedule(() -> healPlayers(), 2000L);
		ThreadPoolManager.getInstance().schedule(() -> teleportPlayersToColiseum(), 4000L);
		ThreadPoolManager.getInstance().schedule(() -> paralyzePlayers(), 5000L);
		if(Config.EVENT_CtFBuffPlayers && Config.EVENT_CtFFighterBuffs.length > 0 && Config.EVENT_CtFMageBuffs.length > 0)
			ThreadPoolManager.getInstance().schedule(() -> buffPlayers(), 6000L);
		ThreadPoolManager.getInstance().schedule(() -> go(), 60000L);

		sayToAll("scripts.events.CtF.AnnounceFinalCountdown", null);
	}

	public static void go()
	{
		if(!_progress.compareAndSet(PROGRESS_PREPARE, PROGRESS_BATTLE))
			return;

		upParalyzePlayers();
		clearArena();
		sayToAll("scripts.events.CtF.AnnounceFight", null);
		_endTask = ThreadPoolManager.getInstance().schedule(() -> endOfTime(), Config.EVENT_CtFEventRunningTime * 60000L);
	}

	public static void endOfTime()
	{
		endBattle(3); // ничья
	}

	public static void endBattle(int win)
	{
		if(!_progress.compareAndSet(PROGRESS_BATTLE, PROGRESS_DISABLED))
			return;

		boxes.clear();
		if(_endTask != null)
		{
			_endTask.cancel(false);
			_endTask = null;
		}

		removeFlags();

		if(redFlag != null)
		{
			redFlag.deleteMe();
			redFlag = null;
		}

		if(blueFlag != null)
		{
			blueFlag.deleteMe();
			blueFlag = null;
		}

		removeAura();

		openColiseumDoors();

		switch(win)
		{
			case 1:
				sayToAll("scripts.events.CtF.AnnounceFinishedRedWins", null);
				giveItemsToWinner(false, true, 1);
				break;
			case 2:
				sayToAll("scripts.events.CtF.AnnounceFinishedBlueWins", null);
				giveItemsToWinner(true, false, 1);
				break;
			case 3:
				sayToAll("scripts.events.CtF.AnnounceFinishedDraw", null);
				giveItemsToWinner(true, true, 0);
				break;
		}

		sayToAll("scripts.events.CtF.AnnounceEnd", null);
		ThreadPoolManager.getInstance().schedule(() -> end(), 30000L);
	}

	public static void end()
	{
		ThreadPoolManager.getInstance().schedule(() -> ressurectPlayers(), 1000L);
		ThreadPoolManager.getInstance().schedule(() -> healPlayers(), 2000L);
		ThreadPoolManager.getInstance().schedule(() -> teleportPlayersToSavedCoords(), 3000L);
		ThreadPoolManager.getInstance().schedule(() -> autoContinue(), 10000L);
	}

	public static void autoContinue()
	{
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
			// если нет, то пробуем зашедулить по времени из конфигов
			scheduleEventStart();
	}

	public static void scheduleEventStart()
	{
		try
		{
			Calendar currentTime = Calendar.getInstance();
			Calendar nextStartTime = null;
			Calendar testStartTime = null;

			for(String timeOfDay : Config.EVENT_CtFStartTime)
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
			_log.info("CtF: Error figuring out a start time. Check CtFEventInterval in config file.");
		}
	}

	public static void giveItemsToWinner(boolean team1, boolean team2, double rate)
	{
		if(team1)
		{
			for(Player player : getPlayers(players_list1))
			{
				for(int i = 0; i < rewards.length; i++)
				{
					int itemId = rewards[i][0];
					if(itemId > 0)
					{
						long itemCount = Math.round((Config.EVENT_CtFrate ? player.getLevel() : 1) * rewards[i][1] * rate);
						if(itemCount > 0)
							ItemFunctions.addItem(player, itemId, itemCount, "CtF event rewards");
					}
				}
			}
		}
		if(team2)
		{
			for(Player player : getPlayers(players_list2))
			{
				for(int i = 0; i < rewards.length; i++)
				{
					int itemId = rewards[i][0];
					if(itemId > 0)
					{
						long itemCount = Math.round((Config.EVENT_CtFrate ? player.getLevel() : 1) * rewards[i][1] * rate);
						if(itemCount > 0)
							ItemFunctions.addItem(player, itemId, itemCount, "CtF event rewards");
					}
				}
			}
		}
	}

	public static void teleportPlayersToColiseum()
	{
		for(Player player : getPlayers(players_list1))
		{
			unRide(player);
			if(!Config.EVENT_CtFAllowSummons)
				unSummonPet(player, true);
			DuelEvent duel = player.getEvent(DuelEvent.class);
			if(duel != null)
				duel.abortDuel(player);
			player.addListener(_instance);
			player.setVar("backCoords", player.getLoc().toXYZString(), -1);
			player.teleToLocation(Territory.getRandomLoc(team1spawn, player.isFlying()), _reflection);
			player.setTeam(TeamType.BLUE);
			if(!Config.EVENT_CtFAllowBuffs)
			{
				player.getEffectList().stopAllEffects();
				EffectsDAO.getInstance().deleteSummonsEffects(player); // Удаляем все эффекты непризванных саммонов.
				if(player.getServitor() != null)
					player.getServitor().getEffectList().stopAllEffects();
			}
		}

		for(Player player : getPlayers(players_list2))
		{
			unRide(player);
			if(!Config.EVENT_CtFAllowSummons)
				unSummonPet(player, true);
			DuelEvent duel = player.getEvent(DuelEvent.class);
			if(duel != null)
				duel.abortDuel(player);
			player.addListener(_instance);
			player.setVar("backCoords", player.getLoc().toXYZString(), -1);
			player.teleToLocation(Territory.getRandomLoc(team2spawn, player.isFlying()), _reflection);
			player.setTeam(TeamType.RED);
			if(!Config.EVENT_CtFAllowBuffs)
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
			else
			{
				player.setTeam(TeamType.BLUE);
				player.setIsInCtF(true);
			}
		for(Player player : getPlayers(players_list2))
			if(!checkPlayer(player, false))
				removePlayer(player);
			else
			{
				player.setTeam(TeamType.RED);
				player.setIsInCtF(true);
			}
	}

	public static void removeAura()
	{
		for(Player player : getPlayers(players_list1))
		{
			player.setTeam(TeamType.NONE);
			player.setIsInCtF(false);
		}
		for(Player player : getPlayers(players_list2))
		{
			player.setTeam(TeamType.NONE);
			player.setIsInCtF(false);
		}
	}

	/**
	 * чистим арену от мусора
	 */
	public static void clearArena()
	{
		HashSet<Player> players = new HashSet<Player>();
		for(Zone zone : _zones)
			players.addAll(zone.getInsidePlayers());

		for(Player player : players)
		{
			if(!players_list1.contains(player.getObjectId()) && !players_list2.contains(player.getObjectId()))
				player.teleToLocation(147451, 46728, -3410, _reflection);
		}
	}

	public static void resurrectAtBase(Player player)
	{
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
		if(player.getTeam() == TeamType.BLUE)
			player.teleToLocation(Territory.getRandomLoc(team1spawn, player.isFlying()), _reflection);
		else if(player.getTeam() == TeamType.RED)
			player.teleToLocation(Territory.getRandomLoc(team2spawn, player.isFlying()), _reflection);
	}

	public static Location OnEscape(Player player)
	{
		if(_progress.get() == PROGRESS_BATTLE && player.getTeam() != TeamType.NONE && (players_list1.contains(player.getObjectId()) || players_list2.contains(player.getObjectId())))
			removePlayer(player);
		return null;
	}

	private class ZoneListener implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature cha)
		{
			if(cha == null)
				return;
			Player player = cha.getPlayer();
			if((_progress.get() == PROGRESS_PREPARE || _progress.get() == PROGRESS_BATTLE) && player != null && !players_list1.contains(player.getObjectId()) && !players_list2.contains(player.getObjectId()))
				player.teleToLocation(147451, 46728, -3410, ReflectionManager.DEFAULT);
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

	private static class RedBaseZoneListener implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature actor)
		{
			if(actor == null)
				return;
			Player player = actor.getPlayer();
			if((_progress.get() == PROGRESS_PREPARE || _progress.get() == PROGRESS_BATTLE) && player != null && players_list2.contains(player.getObjectId()) && player.isTerritoryFlagEquipped())
				endBattle(1);

		}

		@Override
		public void onZoneLeave(Zone zone, Creature actor)
		{}
	}

	private static class BlueBaseZoneListener implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature actor)
		{
			if(actor == null)
				return;
			Player player = actor.getPlayer();
			if((_progress.get() == PROGRESS_PREPARE || _progress.get() == PROGRESS_BATTLE) && player != null && players_list1.contains(player.getObjectId()) && player.isTerritoryFlagEquipped())
				endBattle(2);
		}

		@Override
		public void onZoneLeave(Zone zone, Creature actor)
		{}
	}

	private static void removePlayer(Player player)
	{
		if(player != null)
		{
			players_list1.remove(player.getObjectId());
			players_list2.remove(player.getObjectId());
			player.setTeam(TeamType.NONE);
			dropFlag(player);
			player.removeListener(_instance);
			player.setIsInCtF(false);
			player.teleToLocation(147451, 46728, -3410, ReflectionManager.DEFAULT);

			if(!Config.EVENT_CtFAllowMultiReg)
				boxes.remove(player.getObjectId());
		}
	}

	private static void addFlag(Player player, int flagId)
	{
		ItemInstance item = ItemFunctions.createItem(flagId);
		item.setCustomType1(77);
		item.setCustomFlags(ItemInstance.FLAG_NO_DESTROY | ItemInstance.FLAG_NO_TRADE | ItemInstance.FLAG_NO_DROP | ItemInstance.FLAG_NO_TRANSFER);
		item.setAttachment(new CtFCombatFlagObject());
		player.getInventory().addItem(item);
		player.getInventory().equipItem(item);
		player.sendChanges();
		player.sendPacket(Msg.YOU_VE_ACQUIRED_THE_WARD_MOVE_QUICKLY_TO_YOUR_FORCES__OUTPOST);
	}

	private static void removeFlags()
	{
		for(Player player : getPlayers(players_list1))
			removeFlag(player);
		for(Player player : getPlayers(players_list2))
			removeFlag(player);
	}

	private static void removeFlag(Player player)
	{
		if(player != null && player.isTerritoryFlagEquipped())
		{
			ItemInstance flag = player.getActiveWeaponInstance();
			if(flag != null) // 77 это эвентовый флаг
			{
				flag.setCustomFlags(0);
				player.getInventory().destroyItem(flag, 1);
				player.broadcastUserInfo(true);
			}
		}
	}

	private static void dropFlag(Player player)
	{
		if(player != null && player.isTerritoryFlagEquipped())
		{
			ItemInstance flag = player.getActiveWeaponInstance();
			if(flag != null && flag.getCustomType1() == 77) // 77 это эвентовый флаг
			{
				flag.setCustomFlags(0);
				player.getInventory().destroyItem(flag, 1);
				player.broadcastUserInfo(true);
				if(flag.getItemId() == 13560)
				{
					if(Config.EVENT_CtFOnDropSpawnFlagInStartLoc)
						redFlag.setLoc(redFlagLoc);
					else
						redFlag.setXYZ(player.getLoc().getX(), player.getLoc().getY(), player.getLoc().getZ());
					redFlag.setReflection(_reflection);
					redFlag.spawnMe();
				}
				else if(flag.getItemId() == 13561)
				{
					if(Config.EVENT_CtFOnDropSpawnFlagInStartLoc)
						blueFlag.setLoc(blueFlagLoc);
					else
						blueFlag.setXYZ(player.getLoc().getX(), player.getLoc().getY(), player.getLoc().getZ());
					blueFlag.setXYZ(player.getLoc().getX(), player.getLoc().getY(), player.getLoc().getZ());
					blueFlag.setReflection(_reflection);
					blueFlag.spawnMe();
				}
			}
		}
	}

	private static List<Player> getPlayers(IntSet list)
	{
		List<Player> result = new ArrayList<Player>();
		for(int objId : list.toArray())
		{
			Player player = GameObjectsStorage.getPlayer(objId);
			if(player != null)
				result.add(player);
		}
		return result;
	}

	private static void openColiseumDoors()
	{
		for(DoorInstance door : _reflection.getDoors())
			door.openMe();
	}

	private static void closeColiseumDoors()
	{
		for(DoorInstance door : _reflection.getDoors())
			door.closeMe();
	}

	@Override
	public void onDeath(Creature self, Creature killer)
	{
		if(_progress.get() == PROGRESS_BATTLE && self != null && self.isPlayer() && self.getTeam() != TeamType.NONE && (players_list1.contains(self.getObjectId()) || players_list2.contains(self.getObjectId())))
		{
			Player player = self.getPlayer();
			dropFlag(player);
			ThreadPoolManager.getInstance().schedule(() -> resurrectAtBase(player), 10000L);
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
		if(player == null || player.getTeam() == TeamType.NONE)
			return;

		// Вышел или вылетел во время регистрации
		if(_progress.get() == PROGRESS_REGISTRATION && (players_list1.contains(player.getObjectId()) || players_list2.contains(player.getObjectId())))
		{
			removePlayer(player);
			return;
		}

		// Вышел или вылетел во время телепортации
		if(_progress.get() == PROGRESS_PREPARE && (players_list1.contains(player.getObjectId()) || players_list2.contains(player.getObjectId())))
		{
			removePlayer(player);
			return;
		}

		// Вышел или вылетел во время эвента
		OnEscape(player);
	}

	public static void mageBuff(Player player)
	{
		for(int i = 0; i < mage_buffs.length; i++)
		{
			buff = SkillHolder.getInstance().getSkill(mage_buffs[i][0], mage_buffs[i][1]);
			buff.getEffects(player, player, false, false);
		}
	}

	public static void fighterBuff(Player player)
	{
		for(int i = 0; i < fighter_buffs.length; i++)
		{
			buff = SkillHolder.getInstance().getSkill(fighter_buffs[i][0], fighter_buffs[i][1]);
			/*for(EffectTemplate et : buff.getEffectTemplates()) {
				Env env = new Env(player, player, buff);
				Effect effect = et.getEffect(env);
				effect.setPeriod(1200000); //20 минут
				player. getEffectList ().addEffect(effect);
			}*/
			buff.getEffects(player, player, false, false);
		}
	}

	private static boolean checkDualBox(Player player)
	{
		if(!Config.EVENT_CtFAllowMultiReg)
			if("IP".equalsIgnoreCase(Config.EVENT_CtFCheckWindowMethod))
			{
				if(boxes.containsValue(player.getIP()))
				{
					show(new CustomMessage("scripts.events.CtF.CancelledBox", player), player);
					return false;
				}
			}
			else if("HWid".equalsIgnoreCase(Config.EVENT_CtFCheckWindowMethod))
			{
				String hwid = player.getNetConnection().getHWID();
				if(hwid != null && boxes.containsValue(hwid))
				{
					show(new CustomMessage("scripts.events.CtF.CancelledBox", player), player);
					return false;
				}
			}
		return true;
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

	public static void buffPlayer(Player player)
	{
		if(player.isMageClass())
			mageBuff(player);
		else
			fighterBuff(player);
	}

	public static class StartTask extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			if(!_active)
				return;

			if(isPvPEventStarted())
			{
				_log.info("CtF not started: another event is already running");
				return;
			}

			for(Residence c : ResidenceHolder.getInstance().getResidenceList(Castle.class))
				if(c.getSiegeEvent() != null && c.getSiegeEvent().isInProgress())
				{
					_log.debug("CtF not started: CastleSiege in progress");
					return;
				}

			if(Config.EVENT_CtFCategories)
				_instance.start(new String[] { "1", "1" });
			else
				_instance.start(new String[] { "-1", "-1" });
		}
	}

	public static void teleportPlayersToSavedCoords()
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
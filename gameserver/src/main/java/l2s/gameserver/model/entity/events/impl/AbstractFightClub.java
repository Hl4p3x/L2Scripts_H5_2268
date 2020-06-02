package l2s.gameserver.model.entity.events.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

import javolution.util.FastMap;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.threading.RunnableImpl;
import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.InstantZoneHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.listener.actor.player.OnPlayerExitListener;
import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Effect;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.SimpleSpawner;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.fightclubmanager.FightClubEventManager;
import l2s.gameserver.model.entity.events.fightclubmanager.FightClubGameRoom;
import l2s.gameserver.model.entity.events.fightclubmanager.FightClubLastStatsManager;
import l2s.gameserver.model.entity.events.fightclubmanager.FightClubMap;
import l2s.gameserver.model.entity.events.fightclubmanager.FightClubPlayer;
import l2s.gameserver.model.entity.events.fightclubmanager.FightClubTeam;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.EarthQuakePacket;
import l2s.gameserver.network.l2.s2c.ExPVPMatchCCRecord;
import l2s.gameserver.network.l2.s2c.ExPVPMatchCCRetire;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.RevivePacket;
import l2s.gameserver.network.l2.s2c.SayPacket2;
import l2s.gameserver.network.l2.s2c.ShowTutorialMarkPacket;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.templates.DoorTemplate;
import l2s.gameserver.templates.InstantZone;
import l2s.gameserver.templates.ZoneTemplate;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.TimeUtils;
import l2s.gameserver.utils.Util;

public abstract class AbstractFightClub extends Event
{
	public static final String REGISTERED_PLAYERS = "registered_players";
	public static final String LOGGED_OFF_PLAYERS = "logged_off_players";
	public static final String FIGHTING_PLAYERS = "fighting_players";
	public static final int INSTANT_ZONE_ID = 400;
	private static final int CLOSE_LOCATIONS_VALUE = 80;
	private static int LAST_OBJECT_ID = 1;
	private static final int BADGES_FOR_MINUTE_OF_AFK = -1;
	private static final int TIME_FIRST_TELEPORT = 10;
	private static final int TIME_PLAYER_TELEPORTING = 5;
	private static final int TIME_PREPARATION_BEFORE_FIRST_ROUND = 30;
	private static final int TIME_PREPARATION_BETWEEN_NEXT_ROUNDS = 30;
	private static final int TIME_AFTER_ROUND_END_TO_RETURN_SPAWN = 15;
	private static final int TIME_TELEPORT_BACK_TOWN = 30;
	private static final int TIME_MAX_SECONDS_OUTSIDE_ZONE = 10;
	private static final int TIME_TO_BE_AFK = 30;
	private static final String[] ROUND_NUMBER_IN_STRING = { "", "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th", "10th" };

	private final int _objId;
	private final String _desc;
	private final String _icon;
	private final int _roundRunTime;
	private final boolean _isAutoTimed;
	private final int[][] _autoStartTimes;
	private final boolean _teamed;
	private final boolean _buffer;
	private final int[][] _fighterBuffs;
	private final int[][] _mageBuffs;
	private final boolean _rootBetweenRounds;
	private final FightClubEventManager.CLASSES[] _excludedClasses;
	private final int[] _excludedSkills;
	private final boolean _roundEvent;
	private final int _rounds;
	private final int _respawnTime;
	private final boolean _ressAllowed;
	private final boolean _instanced;
	private final boolean _showPersonality;
	private final double _badgesKillPlayer;
	private final int _badgesId;
	private final double _badgesKillPet;
	private final double _badgesDie;
	protected final double _badgeWin;
	private final int topKillerReward;
	private EVENT_STATE _state = EVENT_STATE.NOT_ACTIVE;
	private ExitListener _exitListener = new ExitListener();
	private ZoneListener _zoneListener = new ZoneListener();
	private FightClubMap _map;
	private Reflection _reflection;
	private List<FightClubTeam> _teams = new ArrayList<FightClubTeam>();
	private Map<FightClubPlayer, Zone> _leftZone = new ConcurrentHashMap<FightClubPlayer, Zone>();
	private int _currentRound = 0;
	private boolean _dontLetAnyoneIn = false;
	private FightClubGameRoom _room;
	private MultiValueSet<String> _set;
	private Map<String, Integer> _scores = new ConcurrentHashMap<String, Integer>();
	private Map<String, Integer> _bestScores = new ConcurrentHashMap<String, Integer>();
	private boolean _scoredUpdated = true;
	private ScheduledFuture<?> _timer;

	public AbstractFightClub(MultiValueSet<String> set)
	{
		super(set);
		_objId = (LAST_OBJECT_ID++);
		_desc = set.getString("desc");
		_icon = set.getString("icon");
		_roundRunTime = set.getInteger("roundRunTime", -1);
		_teamed = set.getBool("teamed");
		_buffer = set.getBool("buffer");
		_fighterBuffs = parseBuffs(set.getString("fighterBuffs", null));
		_mageBuffs = parseBuffs(set.getString("mageBuffs", null));
		_rootBetweenRounds = set.getBool("rootBetweenRounds");
		_excludedClasses = parseExcludedClasses(set.getString("excludedClasses", ""));
		_excludedSkills = parseExcludedSkills(set.getString("excludedSkills", null));
		_isAutoTimed = set.getBool("isAutoTimed", false);
		_autoStartTimes = parseAutoStartTimes(set.getString("autoTimes", ""));
		_roundEvent = set.getBool("roundEvent");
		_rounds = set.getInteger("rounds", -1);
		_respawnTime = set.getInteger("respawnTime");
		_ressAllowed = set.getBool("ressAllowed");
		_instanced = set.getBool("instanced", true);
		_showPersonality = set.getBool("showPersonality", true);

		_badgesKillPlayer = set.getDouble("badgesKillPlayer", 0.0D);
		_badgesId = set.getInteger("badgeID", 57);
		_badgesKillPet = set.getDouble("badgesKillPet", 0.0D);
		_badgesDie = set.getDouble("badgesDie", 0.0D);
		_badgeWin = set.getDouble("badgesWin", 0.0D);
		topKillerReward = set.getInteger("topKillerReward", 0);

		_set = set;
	}

	public void prepareEvent(FightClubGameRoom room)
	{
		_map = room.getMap();
		_room = room;

		for(Player player : room.getAllPlayers())
		{
			addObject("registered_players", new FightClubPlayer(player));
			player.addEvent(this);
		}

		startTeleportTimer(room);
	}

	@Override
	public void startEvent()
	{
		super.startEvent();

		_state = EVENT_STATE.PREPARATION;

		IntObjectMap<DoorTemplate> doors = new HashIntObjectMap<DoorTemplate>(0);
		Map<String, ZoneTemplate> zones = new HashMap<String, ZoneTemplate>();
		for(Map.Entry<Integer, Map<String, ZoneTemplate>> entry : getMap().getTerritories().entrySet())
		{
			for(Map.Entry<String, ZoneTemplate> team : entry.getValue().entrySet())
				zones.put(team.getKey(), team.getValue());
		}

		if(isInstanced())
			createReflection(doors, zones);

		List<FightClubPlayer> playersToRemove = new ArrayList<FightClubPlayer>();
		for(FightClubPlayer iFPlayer : getPlayers(new String[] { "registered_players" }))
		{
			stopInvisibility(iFPlayer.getPlayer());
			if(!checkIfRegisteredPlayerMeetCriteria(iFPlayer))
			{
				playersToRemove.add(iFPlayer);
				continue;
			}

			if(isHidePersonality())
				iFPlayer.getPlayer().setPolyId(FightClubGameRoom.getPlayerClassGroup(iFPlayer.getPlayer()).getTransformId());
		}

		for(FightClubPlayer playerToRemove : playersToRemove)
			unregister(playerToRemove.getPlayer());

		if(isTeamed())
			spreadIntoTeamsAndPartys();

		teleportRegisteredPlayers();

		updateEveryScore();

		for(FightClubPlayer iFPlayer : getPlayers(new String[] { "fighting_players", "registered_players" }))
		{
			iFPlayer.getPlayer().isntAfk();
			iFPlayer.getPlayer().setFightClubGameRoom(null);
		}

		startNewTimer(true, 5000, "startRoundTimer", new Object[] { Integer.valueOf(30) });

		ThreadPoolManager.getInstance().schedule(new LeftZoneThread(), 5000L);
	}

	public void startRound()
	{
		_state = EVENT_STATE.STARTED;

		_currentRound++;

		if(isRoundEvent())
		{
			if(_currentRound == _rounds)
				sendMessageToFighting(MESSAGE_TYPES.SCREEN_BIG, "Last Round STARTED!", true); // TODO: Вынести в ДП.
			else
				sendMessageToFighting(MESSAGE_TYPES.SCREEN_BIG, new StringBuilder().append("Round ").append(_currentRound).append(" STARTED!").toString(), true); // TODO: Вынести в ДП.
		}
		else
			sendMessageToFighting(MESSAGE_TYPES.SCREEN_BIG, "Fight!", true); // TODO: Вынести в ДП.

		unrootPlayers();

		if(getRoundRuntime() > 0)
			startNewTimer(true, (int)(getRoundRuntime() / 2.0D * 60000.0D), "endRoundTimer", new Object[] { Integer.valueOf((int)(getRoundRuntime() / 2.0D * 60.0D)) });

		if(_currentRound == 1)
		{
			ThreadPoolManager.getInstance().schedule(new TimeSpentOnEventThread(), 10000L);
			ThreadPoolManager.getInstance().schedule(new CheckAfkThread(), 1000L);
		}

		for(FightClubPlayer iFPlayer : getPlayers(new String[] { "fighting_players" }))
		{
			hideScores(iFPlayer.getPlayer());
			iFPlayer.getPlayer().broadcastUserInfo(true);
		}
	}

	public void endRound()
	{
		_state = EVENT_STATE.OVER;

		if(!isLastRound())
			sendMessageToFighting(MESSAGE_TYPES.SCREEN_BIG, new StringBuilder().append("Round ").append(_currentRound).append(" is over!").toString(), false); // TODO: Вынести в ДП.
		else
			sendMessageToFighting(MESSAGE_TYPES.SCREEN_BIG, "Event is now Over!", false); // TODO: Вынести в ДП.

		ressAndHealPlayers();

		for(FightClubPlayer iFPlayer : getPlayers(new String[] { "fighting_players" }))
			showScores(iFPlayer.getPlayer());

		if(!isLastRound())
		{
			if(isTeamed())
			{
				for(FightClubTeam team : getTeams())
					team.setSpawnLoc(null);
			}

			ThreadPoolManager.getInstance().schedule(new Runnable()
			{
				@Override
				public void run()
				{
					for(FightClubPlayer iFPlayer : getPlayers(new String[] { "fighting_players" }))
						teleportSinglePlayer(iFPlayer, false, true);

					startNewTimer(true, 0, "startRoundTimer", new Object[] { Integer.valueOf(30) });
				}
			}
			, 15000L);
		}
		else
		{
			ThreadPoolManager.getInstance().schedule(new Runnable()
			{
				@Override
				public void run()
				{
					stopEvent(false);
				}
			}
			, 10000L);

			if(isTeamed())
				announceWinnerTeam(true, null);
			else
				announceWinnerPlayer(true, null);
		}
		for(FightClubPlayer iFPlayer : getPlayers(new String[] { "fighting_players" }))
			iFPlayer.getPlayer().broadcastUserInfo(true);
	}

	private void announceTopKillers(FightClubPlayer[] topKillers)
	{
		if (topKillers == null) {
			return;
		}
		for (FightClubPlayer fPlayer : topKillers)
      if (fPlayer != null)
      {
        String message = fPlayer.getPlayer().getName() + " had most kills" + " on " + getName() + " Event!";
        FightClubEventManager.getInstance().sendToAllMsg(this, message);
      }
  }
  
	@Override
	public void stopEvent(boolean force)
	{
		_state = EVENT_STATE.NOT_ACTIVE;
		super.stopEvent(force);
		reCalcNextTime(false);
		_room = null;

		showLastAFkMessage();
		FightClubPlayer[] topKillers = getTopKillers();
		announceTopKillers(topKillers);		
		giveRewards(topKillers);

		for(FightClubPlayer iFPlayer : getPlayers(new String[] { "fighting_players" }))
		{
			iFPlayer.getPlayer().broadcastCharInfo();
			if(iFPlayer.getPlayer().getServitor() != null)
				iFPlayer.getPlayer().getServitor().broadcastCharInfo();
		}

		for(Player player : getAllFightingPlayers())
			showScores(player);
			
		FightClubEventManager.clearBoxes();
		
		ThreadPoolManager.getInstance().schedule(new Runnable()
		{
			@Override
			public void run()
			{
				for(Player player : getAllFightingPlayers())
				{
					leaveEvent(player, true);
					player.sendPacket(new ExShowScreenMessage("", 10, ExShowScreenMessage.ScreenMessageAlign.TOP_LEFT, false));
				}
				destroyMe();
			}
		}
		, 10000L);
	}

	public void destroyMe()
	{
		if(getReflection() != null)
		{
			for(Zone zone : getReflection().getZones())
				zone.removeListener(_zoneListener);
			getReflection().collapse();
		}

		if(_timer != null)
			_timer.cancel(false);

		_timer = null;
		_bestScores.clear();
		_scores.clear();
		_leftZone.clear();
		getObjects().clear();
		_set = null;
		_room = null;
		_zoneListener = null;

		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			player.removeListener(_exitListener);

		_exitListener = null;
	}

	public void onKilled(Creature actor, Creature victim)
	{
		if(victim.isPlayer() && getRespawnTime() > 0)
			showScores(victim);

		if(actor != null && actor.isPlayer() && getFightClubPlayer(actor) != null)
			FightClubLastStatsManager.getInstance().updateStat(actor.getPlayer(), FightClubLastStatsManager.FightClubStatType.KILL_PLAYER, getFightClubPlayer(actor).getKills(true));

		if(victim.isPlayer() && getRespawnTime() > 0 && !_ressAllowed && getFightClubPlayer(victim.getPlayer()) != null)
			startNewTimer(false, 0, "ressurectionTimer", new Object[] { Integer.valueOf(getRespawnTime()), getFightClubPlayer(victim) });
	}

	public void requestRespawn(Player activeChar, RestartType restartType)
	{
		if(getRespawnTime() > 0)
			startNewTimer(false, 0, "ressurectionTimer", new Object[] { Integer.valueOf(getRespawnTime()), getFightClubPlayer(activeChar) });
	}

	public boolean canAttack(Creature target, Creature attacker, Skill skill, boolean force)
	{
		if(_state != EVENT_STATE.STARTED)
			return false;

		Player player = attacker.getPlayer();
		if(player == null)
			return true;

		if(isTeamed())
		{
			FightClubPlayer targetFPlayer = getFightClubPlayer(target);
			FightClubPlayer attackerFPlayer = getFightClubPlayer(attacker);

			if(targetFPlayer == null || attackerFPlayer == null || targetFPlayer.getTeam() == attackerFPlayer.getTeam())
				return false;
		}

		return !isInvisible(player, player);
	}

	public boolean canUseSkill(Creature caster, Creature target, Skill skill)
	{
		if(_excludedSkills != null)
		{
			for(int id : _excludedSkills)
			{
				if(skill.getId() == id)
					return false;
			}
		}
		return true;
	}

	public SystemMsg checkForAttack(Creature target, Creature attacker, Skill skill, boolean force)
	{
		if(!canAttack(target, attacker, skill, force))
			return SystemMsg.INVALID_TARGET;
		return null;
	}

	public boolean canRessurect(Player player, Creature creature, boolean force)
	{
		return _ressAllowed;
	}

	public int getMySpeed(Player player)
	{
		return -1;
	}

	public int getPAtkSpd(Player player)
	{
		return -1;
	}

	public void checkRestartLocs(Player player, Map<RestartType, Boolean> r)
	{
		r.clear();
		if(isTeamed() && getRespawnTime() > 0 && getFightClubPlayer(player) != null && _ressAllowed)
			r.put(RestartType.TO_FLAG, Boolean.valueOf(true));
	}

	public boolean canUseBuffer(Player player, boolean heal)
	{
		FightClubPlayer fPlayer = getFightClubPlayer(player);
		if(!getBuffer())
			return false;
		if(player.isInCombat())
			return false;
		if(heal)
		{
			if(player.isDead())
				return false;
			if(_state != EVENT_STATE.STARTED)
				return true;

			return fPlayer.isInvisible();
		}

		return true;
	}

	public boolean canUsePositiveMagic(Creature user, Creature target)
	{
		Player player = user.getPlayer();
		if(player == null)
			return true;
		if(!isFriend(user, target))
			return false;

		return !isInvisible(player, player);
	}

	public int getRelation(Player thisPlayer, Player target, int oldRelation)
	{
		if(_state == EVENT_STATE.STARTED)
			return isFriend(thisPlayer, target) ? getFriendRelation() : getWarRelation();
		return oldRelation;
	}

	public boolean canJoinParty(Player sender, Player receiver)
	{
		return isFriend(sender, receiver);
	}

	public boolean canReceiveInvitations(Player sender, Player receiver)
	{
		return true;
	}

	public boolean canOpenStore(Player player)
	{
		return false;
	}

	public boolean loseBuffsOnDeath(Player player)
	{
		return false;
	}

	protected boolean inScreenShowBeScoreNotKills()
	{
		return true;
	}

	protected boolean inScreenShowBeTeamNotInvidual()
	{
		return isTeamed();
	}

	public boolean isFriend(Creature c1, Creature c2)
	{
		if(c1.equals(c2))
			return true;
		if(!c1.isPlayable() || !c2.isPlayable())
			return true;

		if((c1.isSummon()) && (c2.isPlayer()) && (c2.getPlayer().getServitor() != null) && (c2.getPlayer().getServitor().equals(c1)))
			return true;

		if(c2.isSummon() && c1.isPlayer() && c1.getPlayer().getServitor() != null && c1.getPlayer().getServitor().equals(c2))
			return true;

		FightClubPlayer fPlayer1 = getFightClubPlayer(c1.getPlayer());
		FightClubPlayer fPlayer2 = getFightClubPlayer(c2.getPlayer());

		if(isTeamed())
			return fPlayer1 != null && fPlayer2 != null && fPlayer1.getTeam() == fPlayer2.getTeam();

		return false;
	}

	public boolean isInvisible(Player actor, Player watcher)
	{
		return actor.getFlags().getInvisible().get();
	}

	public String getVisibleName(Player player, String currentName, boolean toMe)
	{
		if(isHidePersonality() && !toMe)
			return "Player";
		return currentName;
	}

	public String getVisibleTitle(Player player, String currentTitle, boolean toMe)
	{
		return currentTitle;
	}

	public int getVisibleTitleColor(Player player, int currentTitleColor, boolean toMe)
	{
		return currentTitleColor;
	}

	public int getVisibleNameColor(Player player, int currentNameColor, boolean toMe)
	{
		if(isTeamed())
		{
			FightClubPlayer fPlayer = getFightClubPlayer(player);
			return fPlayer.getTeam().getNickColor();
		}
		return currentNameColor;
	}

	protected int getBadgesEarned(FightClubPlayer fPlayer, int currentValue, boolean topKiller)
	{
		if (fPlayer == null)
			return 0;
		currentValue += addMultipleBadgeToPlayer(fPlayer.getKills(true), _badgesKillPlayer);
    
		currentValue += getRewardForWinningTeam(fPlayer, true);
    
		int minutesAFK = (int)Math.round(fPlayer.getTotalAfkSeconds() / 60.0D);
		currentValue += minutesAFK * -1;
    
		if (topKiller)
		{
			currentValue += topKillerReward;
		}
    
		return currentValue;
	}
  
	protected int addMultipleBadgeToPlayer(int score, double badgePerScore)
	{
		return (int)Math.floor(score * badgePerScore);
	}

	protected int addMultipleBadgeToPlayer(FightClubPlayer fPlayer, FightClubLastStatsManager.FightClubStatType whatFor, int score, double badgePerScore, int secondsSpent)
	{
		int badgesEarned = (int) Math.floor(score * badgePerScore);
		return badgesEarned;
	}

	private int getEndEventBadges(FightClubPlayer fPlayer)
	{
		return 0;
	}

	public void startTeleportTimer(FightClubGameRoom room)
	{
		setState(EVENT_STATE.COUNT_DOWN);

		startNewTimer(true, 0, "teleportWholeRoomTimer", new Object[] { Integer.valueOf(10) });
	}

	protected void teleportRegisteredPlayers()
	{
		for(FightClubPlayer player : getPlayers(new String[] { "registered_players" }))
			teleportSinglePlayer(player, true, true);
	}

	protected void teleportSinglePlayer(FightClubPlayer fPlayer, boolean firstTime, boolean healAndRess)
	{
		Player player = fPlayer.getPlayer();

		if(healAndRess)
		{
			ressurectPlayer(player);
		}

		Location[] spawns = null;
		Location loc = null;

		if(!isTeamed())
			spawns = getMap().getPlayerSpawns();
		else
			loc = getTeamSpawn(fPlayer, true);

		if(!isTeamed())
			loc = getSafeLocation(spawns);

		loc = Location.findPointToStay(loc, 0, 40, fPlayer.getPlayer().getGeoIndex());

		if(isInstanced())
			player.teleToLocation(loc, getReflection());
		else
			player.teleToLocation(loc);

		if(_state == EVENT_STATE.PREPARATION || _state == EVENT_STATE.OVER)
			rootPlayer(player);

		cancelNegativeEffects(player);
		if(player.getServitor() != null)
			cancelNegativeEffects(player.getServitor());

		if(firstTime)
		{
			removeObject("registered_players", fPlayer);
			addObject("fighting_players", fPlayer);

			player.getEffectList().stopAllEffects();
			if(player.getServitor() != null)
				player.getServitor().getEffectList().stopAllEffects();

			player.store(true);
			player.sendPacket(new ShowTutorialMarkPacket(false, 100));

			player.sendPacket(new SayPacket2(0, ChatType.ALL, getName(), "Normal Chat is visible for every player in event.")); // TODO: Вынести в ДП.
			if(isTeamed())
			{
				player.sendPacket(new SayPacket2(0, ChatType.ALL, getName(), "Battlefield(^) Chat is visible only to your team!")); // TODO: Вынести в ДП.
				player.sendPacket(new SayPacket2(0, ChatType.BATTLEFIELD, getName(), "Battlefield(^) Chat is visible only to your team!")); // TODO: Вынести в ДП.
			}
		}

		if(healAndRess)
			buffPlayer(fPlayer.getPlayer());
	}

	public void unregister(Player player)
	{
		FightClubPlayer fPlayer = getFightClubPlayer(player, new String[] { "registered_players" });
		player.removeEvent(this);
		removeObject("registered_players", fPlayer);
		player.sendMessage("You are no longer registered!"); // TODO: Вынести в ДП.
	}

	public boolean leaveEvent(Player player, boolean teleportTown)
	{
		FightClubPlayer fPlayer = getFightClubPlayer(player);

		if(_state == EVENT_STATE.NOT_ACTIVE)
		{
			if(fPlayer.isInvisible())
				stopInvisibility(player);
			removeObject("fighting_players", fPlayer);
			if(isTeamed())
				fPlayer.getTeam().removePlayer(fPlayer);
			player.removeEvent(this);

			if(teleportTown)
				teleportBackToTown(player);
			else
				ressurectPlayer(player);
		}
		else
		{
			rewardPlayer(fPlayer, false);
			if(teleportTown)
				setInvisible(player, 30, false);
			else
				setInvisible(player, -1, false);
			removeObject("fighting_players", fPlayer);

			player.doDie(null);
			player.removeEvent(this);

			if(teleportTown)
				startNewTimer(false, 0, "teleportBackSinglePlayerTimer", new Object[] { Integer.valueOf(30), player });
			else
				ressurectPlayer(player);
		}
		hideScores(player);
		updateScreenScores();

		if(getPlayers(new String[] { "fighting_players", "registered_players" }).isEmpty())
			destroyMe();

		if(player.getParty() != null)
			player.getParty().removePartyMember(player, true);

		return true;
	}

	private static void ressurectPlayer(Player player)
	{
		if(player.isDead())
		{
			player.restoreExp();
			player.setCurrentCp(player.getMaxCp());
			player.setCurrentHp(player.getMaxHp(), true);
			player.setCurrentMp(player.getMaxMp());
			player.broadcastPacket(new RevivePacket(player));
		}
	}

	public void loggedOut(Player player)
	{
		player.doDie(null);

		FightClubPlayer regFPlayer = getFightClubPlayer(player, new String[] { "registered_players" });
		if(regFPlayer != null)
		{
			removeObject("registered_players", regFPlayer);

			if(isTeamed() && regFPlayer.getTeam() != null)
				regFPlayer.getTeam().removePlayer(regFPlayer);
		}

		FightClubPlayer figFPlayer = getFightClubPlayer(player, new String[] { "fighting_players" });
		if(figFPlayer != null)
		{
			removeObject("fighting_players", figFPlayer);

			if(isTeamed())
				figFPlayer.getTeam().removePlayer(figFPlayer);
		}
		updateScreenScores();
	}

	protected void teleportBackToTown(Player player)
	{
		player.setPolyId(0);
		Location loc = Location.findPointToStay(FightClubEventManager.RETURN_LOC, 0, 100, ReflectionManager.DEFAULT.getGeoIndex());
		player.teleToLocation(loc, ReflectionManager.DEFAULT);
		System.out.println("DEBUG: Returning players to -> "+loc.getX()+" "+loc.getY()+""+ loc.getZ());
		ressurectPlayer(player);
	}

	protected void rewardPlayer(FightClubPlayer fPlayer, boolean isTopKiller)
	{
		int badgesToGive = getBadgesEarned(fPlayer, 0, isTopKiller);

		if(getState() == EVENT_STATE.NOT_ACTIVE)
			badgesToGive += getEndEventBadges(fPlayer);

		badgesToGive = Math.max(0, badgesToGive);

		fPlayer.getPlayer().getInventory().addItem(_badgesId, badgesToGive);
		sendMessageToPlayer(fPlayer, MESSAGE_TYPES.SCREEN_BIG, new StringBuilder().append("You have earned ").append(badgesToGive).append(" Festival Adena!").toString()); // TODO: Вынести в ДП.
		sendMessageToPlayer(fPlayer, MESSAGE_TYPES.NORMAL_MESSAGE, new StringBuilder().append("You have earned ").append(badgesToGive).append(" Festival Adena!").toString()); // TODO: Вынести в ДП.
	}

	protected void announceWinnerTeam(boolean wholeEvent, FightClubTeam winnerOfTheRound)
	{
		int bestScore = -1;
		FightClubTeam bestTeam = null;
		boolean draw = false;
		if(wholeEvent)
		{
			for(FightClubTeam team : getTeams())
				if(team.getScore() > bestScore)
				{
					draw = false;
					bestScore = team.getScore();
					bestTeam = team;
				}
				else if(team.getScore() == bestScore)
					draw = true;
		}
		else
			bestTeam = winnerOfTheRound;

		SayPacket2 packet;
		if(!draw)
		{
			packet = new SayPacket2(0, ChatType.COMMANDCHANNEL_ALL, new StringBuilder().append(bestTeam.getName()).append(" Team").toString(), new StringBuilder().append("We won ").append(wholeEvent ? getName() : " Round").append("!").toString()); // TODO: Вынести в ДП.
			for(FightClubPlayer iFPlayer : getPlayers(new String[] { "fighting_players" }))
				iFPlayer.getPlayer().sendPacket(packet);
		}
		updateScreenScores();
	}

	protected void announceWinnerPlayer(boolean wholeEvent, FightClubPlayer winnerOfTheRound)
	{
		int bestScore = -1;
		FightClubPlayer bestPlayer = null;
		boolean draw = false;
		if(wholeEvent)
		{
			for(FightClubPlayer iFPlayer : getPlayers(new String[] { "fighting_players" }))
				if((iFPlayer.getPlayer() != null) && (iFPlayer.getPlayer().isOnline()))
					if(iFPlayer.getScore() > bestScore)
					{
						bestScore = iFPlayer.getScore();
						bestPlayer = iFPlayer;
					}
					else if(iFPlayer.getScore() == bestScore)
					{
						draw = true;
					}
		}
		else
			bestPlayer = winnerOfTheRound;
		SayPacket2 packet;
		if(!draw)
		{
			packet = new SayPacket2(0, ChatType.COMMANDCHANNEL_ALL, bestPlayer.getPlayer().getName(), new StringBuilder().append("I Won ").append(wholeEvent ? getName() : "Round").append("!").toString()); // TODO: Вынести в ДП.
			for(FightClubPlayer iFPlayer : getPlayers(new String[] { "fighting_players" })) {
				iFPlayer.getPlayer().sendPacket(packet);
			}
		}
		updateScreenScores();
	}

	protected void updateScreenScores()
	{
		String msg = getScreenScores(inScreenShowBeScoreNotKills(), inScreenShowBeTeamNotInvidual());

		for(FightClubPlayer iFPlayer : getPlayers(new String[] { "fighting_players" }))
			sendMessageToPlayer(iFPlayer, MESSAGE_TYPES.SCREEN_SMALL, msg);
	}

	protected void updateScreenScores(Player player)
	{
		if(getFightClubPlayer(player) != null)
			sendMessageToPlayer(getFightClubPlayer(player), MESSAGE_TYPES.SCREEN_SMALL, getScreenScores(inScreenShowBeScoreNotKills(), inScreenShowBeTeamNotInvidual()));
	}

	protected String getScorePlayerName(FightClubPlayer fPlayer)
	{
		return new StringBuilder().append(fPlayer.getPlayer().getName()).append(isTeamed() ? new StringBuilder().append(" (").append(fPlayer.getTeam().getName()).append(" Team)").toString() : "").toString();
	}

	protected void updatePlayerScore(FightClubPlayer fPlayer)
	{
		_scores.put(getScorePlayerName(fPlayer), Integer.valueOf(fPlayer.getKills(true)));
		_scoredUpdated = true;

		if(!isTeamed())
			updateScreenScores();
	}

	protected void showScores(Creature c)
	{
		Map<String, Integer> scores = getBestScores();

		FightClubPlayer fPlayer = getFightClubPlayer(c);
		if(fPlayer != null)
			fPlayer.setShowRank(true);

		c.sendPacket(new ExPVPMatchCCRecord(scores));
	}

	protected void hideScores(Creature c)
	{
		c.sendPacket(ExPVPMatchCCRetire.STATIC);
	}

	private void handleAfk(FightClubPlayer fPlayer, boolean setAsAfk)
	{
		Player player = fPlayer.getPlayer();

		if(setAsAfk)
		{
			fPlayer.setAfk(true);
			fPlayer.setAfkStartTime(player.getLastNotAfkTime());

			sendMessageToPlayer(player, MESSAGE_TYPES.CRITICAL, "You are considered as AFK Player!"); // TODO: Вынести в ДП.
		}
		else if(fPlayer.isAfk())
		{
			int totalAfkTime = (int)((System.currentTimeMillis() - fPlayer.getAfkStartTime()) / 1000L);
			totalAfkTime -= 30;
			if(totalAfkTime > 5)
			{
				fPlayer.setAfk(false);

				fPlayer.addTotalAfkSeconds(totalAfkTime);
				sendMessageToPlayer(player, MESSAGE_TYPES.CRITICAL, new StringBuilder().append("You were afk for ").append(totalAfkTime).append(" seconds!").toString()); // TODO: Вынести в ДП.
			}
		}
	}

	protected void setInvisible(Player player, int seconds, boolean sendMessages)
	{
		FightClubPlayer fPlayer = getFightClubPlayer(player);
		fPlayer.setInvisible(true);

		player.startAbnormalEffect(AbnormalEffect.STEALTH);
		player.startInvisible(this, true);
		player.sendUserInfo(true);

		if(seconds > 0)
			startNewTimer(false, 0, "setInvisible", new Object[] { Integer.valueOf(seconds), fPlayer, Boolean.valueOf(sendMessages) });
	}

	protected void stopInvisibility(Player player)
	{
		FightClubPlayer fPlayer = getFightClubPlayer(player);
		if(fPlayer != null)
			fPlayer.setInvisible(false);

		player.stopAbnormalEffect(AbnormalEffect.STEALTH);
		player.stopInvisible(this, true);
	}

	protected void rootPlayer(Player player)
	{
		if(!isRootBetweenRounds())
			return;

		List<Playable> toRoot = new ArrayList<Playable>();
		toRoot.add(player);
		if(player.getServitor() != null)
			toRoot.add(player.getServitor());

		if(!player.isRooted())
			player.startRooted();

		player.stopMove();
		player.startAbnormalEffect(AbnormalEffect.ROOT);
	}

	protected void unrootPlayers()
	{
		if(!isRootBetweenRounds()) {
			return;
		}
		for(FightClubPlayer iFPlayer : getPlayers(new String[] { "fighting_players" }))
		{
			Player player = iFPlayer.getPlayer();
			if(player.isRooted())
			{
				player.stopRooted();
				player.stopAbnormalEffect(AbnormalEffect.ROOT);
			}
		}
	}

	protected void ressAndHealPlayers()
	{
		for(FightClubPlayer fPlayer : getPlayers(new String[] { "fighting_players" }))
		{
			Player player = fPlayer.getPlayer();

			ressurectPlayer(player);
			cancelNegativeEffects(player);
			if(player.getServitor() != null) {
				cancelNegativeEffects(player.getServitor());
			}
			buffPlayer(player);
		}
	}

	protected int getWarRelation()
	{
		int result = 0;

		result |= 64;
		result |= 32768;
		result |= 16384;

		return result;
	}

	protected int getFriendRelation()
	{
		int result = 0;

		result |= 64;
		result |= 256;

		return result;
	}

	protected NpcInstance chooseLocAndSpawnNpc(int id, Location[] locs, int respawnInSeconds)
	{
		return spawnNpc(id, getSafeLocation(locs), respawnInSeconds);
	}

	protected NpcInstance spawnNpc(int id, Location loc, int respawnInSeconds)
	{
		SimpleSpawner spawn = new SimpleSpawner(id);
		spawn.setLoc(loc);
		spawn.setAmount(1);
		spawn.setHeading(loc.h);
		spawn.setRespawnDelay(Math.max(0, respawnInSeconds));
		spawn.setReflection(getReflection());
		List<NpcInstance> npcs = spawn.initAndReturn();

		if(respawnInSeconds <= 0)
			spawn.stopRespawn();

		return npcs.get(0);
	}

	protected static String getFixedTime(int seconds)
	{
		int minutes = seconds / 60;
		String result = "";
		if(seconds >= 60)
		{
			result = new StringBuilder().append(minutes).append(" minute").append(minutes > 1 ? "s" : "").toString(); // TODO: Вынести в ДП.
		}
		else
		{
			result = new StringBuilder().append(seconds).append(" second").append(seconds > 1 ? "s" : "").toString(); // TODO: Вынести в ДП.
		}
		return result;
	}

	private void buffPlayer(Player player)
	{
		if(getBuffer())
		{
			giveBuffs(player, player.isMageClass() ? _mageBuffs : _fighterBuffs);

			if(player.getServitor() != null)
				giveBuffs(player.getServitor(), _fighterBuffs);
		}
	}

	private static void giveBuffs(final Playable playable, int[][] buffs)
	{
		for(int i = 0; i < buffs.length; i++)
		{
			Skill buff = SkillHolder.getInstance().getSkill(buffs[i][0], buffs[i][1]);
			if(buff == null)
				continue;
			buff.getEffects(playable, playable, false, false);
		}

		ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			@Override
			public void runImpl() throws Exception
			{
				playable.setCurrentHp(playable.getMaxHp(), true);
				playable.setCurrentMp(playable.getMaxMp());
				playable.setCurrentCp(playable.getMaxCp());
			}
		}
		, 1000L);
	}

	protected void sendMessageToFightingAndRegistered(MESSAGE_TYPES type, String msg)
	{
		sendMessageToFighting(type, msg, false);
		sendMessageToRegistered(type, msg);
	}

	protected void sendMessageToTeam(FightClubTeam team, MESSAGE_TYPES type, String msg)
	{
		for(FightClubPlayer iFPlayer : team.getPlayers())
			sendMessageToPlayer(iFPlayer, type, msg);
	}

	protected void sendMessageToFighting(MESSAGE_TYPES type, String msg, boolean skipJustTeleported)
	{
		for(FightClubPlayer iFPlayer : getPlayers(new String[] { "fighting_players" }))
			if(!skipJustTeleported || !iFPlayer.isInvisible())
				sendMessageToPlayer(iFPlayer, type, msg);
	}

	protected void sendMessageToRegistered(MESSAGE_TYPES type, String msg)
	{
		for(FightClubPlayer iFPlayer : getPlayers(new String[] { "registered_players" }))
			sendMessageToPlayer(iFPlayer, type, msg);
	}

	public void sendMessageToPlayer(FightClubPlayer fPlayer, MESSAGE_TYPES type, String msg)
	{
		sendMessageToPlayer(fPlayer.getPlayer(), type, msg);
	}

	protected void sendMessageToPlayer(Player player, MESSAGE_TYPES type, String msg)
	{
		switch(type)
		{
			case GM:
				player.sendPacket(new SayPacket2(0, ChatType.CRITICAL_ANNOUNCE, player.getName(), msg));
				updateScreenScores(player);
				break;
			case NORMAL_MESSAGE:
				player.sendMessage(msg);
				break;
			case SCREEN_BIG:
				player.sendPacket(new ExShowScreenMessage(msg, 3000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true));
				updateScreenScores(player);
				break;
			case SCREEN_SMALL:
				player.sendPacket(new ExShowScreenMessage(msg, 600000, ExShowScreenMessage.ScreenMessageAlign.TOP_LEFT, false));
				break;
			case CRITICAL:
				player.sendPacket(new SayPacket2(0, ChatType.COMMANDCHANNEL_ALL, player.getName(), msg));
				updateScreenScores(player);
				break;
		}
	}

	public void setState(EVENT_STATE state)
	{
		_state = state;
	}

	public EVENT_STATE getState()
	{
		return _state;
	}

	public int getObjectId()
	{
		return _objId;
	}

	public int getEventId()
	{
		return getId();
	}

	public String getDescription()
	{
		return _desc;
	}

	public String getIcon()
	{
		return _icon;
	}

	public boolean isAutoTimed()
	{
		return _isAutoTimed;
	}

	public int[][] getAutoStartTimes()
	{
		return _autoStartTimes;
	}

	public FightClubMap getMap()
	{
		return _map;
	}

	public boolean isTeamed()
	{
		return _teamed;
	}

	protected boolean isInstanced()
	{
		return _instanced;
	}

	public Reflection getReflection()
	{
		return _reflection;
	}

	public int getRoundRuntime()
	{
		return _roundRunTime;
	}

	public int getRespawnTime()
	{
		return _respawnTime;
	}

	public boolean isRoundEvent()
	{
		return _roundEvent;
	}

	public int getTotalRounds()
	{
		return _rounds;
	}

	public int getCurrentRound()
	{
		return _currentRound;
	}

	public boolean getBuffer()
	{
		return _buffer;
	}

	protected boolean isRootBetweenRounds()
	{
		return _rootBetweenRounds;
	}

	public boolean isLastRound()
	{
		return (!isRoundEvent()) || (getCurrentRound() == getTotalRounds());
	}

	protected List<FightClubTeam> getTeams()
	{
		return _teams;
	}

	public MultiValueSet<String> getSet()
	{
		return _set;
	}

	public void clearSet()
	{
		_set = null;
	}

	public FightClubEventManager.CLASSES[] getExcludedClasses()
	{
		return _excludedClasses;
	}

	public boolean isHidePersonality()
	{
		return !_showPersonality;
	}

	protected int getTeamTotalKills(FightClubTeam team)
	{
		if(!isTeamed())
			return 0;
		int totalKills = 0;
		for(FightClubPlayer iFPlayer : team.getPlayers()) {
			totalKills += iFPlayer.getKills(true);
		}
		return totalKills;
	}

	public int getPlayersCount(String[] groups)
	{
		return getPlayers(groups).size();
	}

	public List<FightClubPlayer> getPlayers(String[] groups)
	{
		if(groups.length == 1)
		{
			List<FightClubPlayer> fPlayers = getObjects(groups[0]);
			return fPlayers;
		}

		List<FightClubPlayer> newList = new ArrayList<FightClubPlayer>();
		for(String group : groups)
		{
			List<FightClubPlayer> fPlayers = getObjects(group);
			newList.addAll(fPlayers);
		}
		return newList;
	}

	public List<Player> getAllFightingPlayers()
	{
		List<FightClubPlayer> fPlayers = getPlayers(new String[] { "fighting_players" });
		List<Player> players = new ArrayList<Player>(fPlayers.size());
		for(FightClubPlayer fPlayer : fPlayers)
			players.add(fPlayer.getPlayer());
		return players;
	}

	public List<Player> getMyTeamFightingPlayers(Player player)
	{
		FightClubTeam fTeam = getFightClubPlayer(player).getTeam();
		List<FightClubPlayer> fPlayers = getPlayers(new String[] { "fighting_players" });
		List<Player> players = new ArrayList<Player>(fPlayers.size());

		if(!isTeamed())
		{
			player.sendPacket(new SayPacket2(0, ChatType.BATTLEFIELD, getName(), "(There are no teams, only you can see the message)")); // TODO: Вынести в ДП.
			players.add(player);
		}
		else
		{
			for(FightClubPlayer iFPlayer : fPlayers)
			{
				if(iFPlayer.getTeam().equals(fTeam))
					players.add(iFPlayer.getPlayer());
			}
		}
		return players;
	}

	public FightClubPlayer getFightClubPlayer(Creature creature)
	{
		return getFightClubPlayer(creature, new String[] { "fighting_players" });
	}

	public FightClubPlayer getFightClubPlayer(Creature creature, String[] groups)
	{
		if(!creature.isPlayable()) {
			return null;
		}
		int lookedPlayerId = creature.getPlayer().getObjectId();

		for(FightClubPlayer iFPlayer : getPlayers(groups))
			if(iFPlayer.getPlayer().getObjectId() == lookedPlayerId)
				return iFPlayer;
		return null;
	}

	private void spreadIntoTeamsAndPartys()
	{
		for(int i = 0; i < _room.getTeamsCount(); i++) {
			_teams.add(new FightClubTeam(i + 1));
		}
		int index = 0;
		for(Player player : _room.getAllPlayers())
		{
			FightClubTeam team = _teams.get(index % _room.getTeamsCount());

			FightClubPlayer fPlayer = getFightClubPlayer(player, new String[] { "registered_players" });
			if(fPlayer == null)
				continue;
			fPlayer.setTeam(team);
			team.addPlayer(fPlayer);

			index++;
		}

		for(FightClubTeam team : _teams)
		{
			List<List<Player>> partys = spreadTeamInPartys(team);
			for(List<Player> party : partys)
				createParty(party);
		}
	}

	private List<List<Player>> spreadTeamInPartys(FightClubTeam team)
	{
		Map<FightClubEventManager.CLASSES, List<Player>> classesMap = new FastMap<FightClubEventManager.CLASSES, List<Player>>();
		for(FightClubEventManager.CLASSES clazz : FightClubEventManager.CLASSES.values())
			classesMap.put(clazz, new ArrayList<Player>());

		for(FightClubPlayer iFPlayer : team.getPlayers())
		{
			Player player = iFPlayer.getPlayer();
			FightClubEventManager.CLASSES clazz = FightClubGameRoom.getPlayerClassGroup(player);
			classesMap.get(clazz).add(player);
		}

		int partyCount = (int)Math.ceil(team.getPlayers().size() / Party.MAX_SIZE);

		List<List<Player>> partys = new ArrayList<List<Player>>();
		for(int i = 0; i < partyCount; i++)
			partys.add(new ArrayList<Player>());

		if(partyCount == 0)
			return partys;

		int finishedOnIndex = 0;
		for(Map.Entry<FightClubEventManager.CLASSES, List<Player>> clazzEntry : classesMap.entrySet())
		{
			for(Player player : clazzEntry.getValue())
			{
				partys.get(finishedOnIndex).add(player);
				finishedOnIndex++;
				if(finishedOnIndex == partyCount)
					finishedOnIndex = 0;
			}
		}
		return partys;
	}

	private void createParty(List<Player> listOfPlayers)
	{
		if(listOfPlayers.size() <= 1)
			return;

		Party newParty = null;
		for(Player player : listOfPlayers)
		{
			if(player.getParty() != null)
				player.getParty().removePartyMember(player, true);

			if(newParty == null)
				player.setParty(newParty = new Party(player, 4));
			else
				player.joinParty(newParty);
		}
	}

	private synchronized void createReflection(IntObjectMap<DoorTemplate> doors, Map<String, ZoneTemplate> zones)
	{
		InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(400);

		_reflection = new Reflection();
		_reflection.init(iz);
		_reflection.init(doors, zones);

		for(Zone zone : _reflection.getZones())
			zone.addListener(_zoneListener);
	}

	private Location getSafeLocation(Location[] locations)
	{
		Location safeLoc = null;
		int checkedCount = 0;
		boolean isOk = false;

		while(!isOk)
		{
			safeLoc = Rnd.get(locations);

			isOk = nobodyIsClose(safeLoc);
			checkedCount++;

			if(checkedCount > locations.length * 2)
				isOk = true;
		}
		return safeLoc;
	}

	protected Location getTeamSpawn(FightClubPlayer fPlayer, boolean randomNotClosestToPt)
	{
		FightClubTeam team = fPlayer.getTeam();
		Location[] spawnLocs = getMap().getTeamSpawns().get(team.getIndex());

		if(randomNotClosestToPt || _state != EVENT_STATE.STARTED)
		{
			if(team.getSpawnLoc() == null)
				team.setSpawnLoc(Rnd.get(spawnLocs));
			return team.getSpawnLoc();
		}

		List<Player> playersToCheck = new ArrayList<Player>();
		if(fPlayer.getParty() != null)
			playersToCheck = fPlayer.getParty().getPartyMembers();
		else
		{
			for(FightClubPlayer iFPlayer : team.getPlayers())
				playersToCheck.add(iFPlayer.getPlayer());
		}

		Map<Location, Integer> spawnLocations = new FastMap<Location, Integer>();
		for(Location loc : spawnLocs)
			spawnLocations.put(loc, 0);

		for(Player player : playersToCheck)
		{
			if(player != null && player.isOnline() && !player.isDead())
			{
				Location winner = null;
				double winnerDist = -1.0D;
				for(Location loc : spawnLocs)
				{
					if(winnerDist > 0.0D && winnerDist >= player.getDistance(loc))
						continue;
					winner = loc;
					winnerDist = player.getDistance(loc);
				}

				if(winner != null)
					spawnLocations.put(winner, spawnLocations.get(winner) + 1);
			}
		}

		Location winner = null;
		double points = -1.0D;
		for(Map.Entry<Location, Integer> spawn : spawnLocations.entrySet())
		{
			if(points < spawn.getValue())
			{
				winner = spawn.getKey();
				points = spawn.getValue();
			}
		}

		if(points <= 0.0D)
			return Rnd.get(spawnLocs);

		return winner;
	}

	private void giveRewards(FightClubPlayer[] topKillers)
	{
		for(FightClubPlayer iFPlayer : getPlayers(new String[] { "fighting_players" }))
			if(iFPlayer != null)
				rewardPlayer(iFPlayer, Util.arrayContains(topKillers, iFPlayer));
	}

	private void showLastAFkMessage()
	{
		for(FightClubPlayer iFPlayer : getPlayers(new String[] { "fighting_players" }))
		{
			int minutesAFK = (int)Math.round(iFPlayer.getTotalAfkSeconds() / 60.0D);
			int badgesDecreased = -minutesAFK * -1;
			sendMessageToPlayer(iFPlayer, MESSAGE_TYPES.NORMAL_MESSAGE, new StringBuilder().append("Reward decreased by ").append(badgesDecreased).append(" FA for AFK time!").toString()); // TODO: Вынести в ДП.
		}
	}

	private Map<String, Integer> getBestScores()
	{
		if(!_scoredUpdated)
			return _bestScores;

		List<Integer> points = new ArrayList<Integer>(_scores.values());
		Collections.sort(points);
		Collections.reverse(points);

		int cap;
		if(points.size() <= 26)
			cap = points.size() - 1;
		else
			cap = 25;

		Map<String, Integer> finalResult = new LinkedHashMap<String, Integer>();
		for(Map.Entry<String, Integer> i : _scores.entrySet())
		{
			if(i.getValue() > cap)
				finalResult.put(i.getKey(), i.getValue());
		}

		if(finalResult.size() < 25)
		{
			for(Map.Entry<String, Integer> i : _scores.entrySet())
			{
				if(i.getValue() == cap)
				{
					finalResult.put(i.getKey(), i.getValue());
					if(finalResult.size() == 25)
						break;
				}
			}
		}

		_bestScores = finalResult;
		_scoredUpdated = false;

		return finalResult;
	}

	private void updateEveryScore()
	{
		for(FightClubPlayer iFPlayer : getPlayers(new String[] { "fighting_players" }))
		{
			_scores.put(getScorePlayerName(iFPlayer), iFPlayer.getKills(true));
			_scoredUpdated = true;
		}
	}

	private String getScreenScores(boolean showScoreNotKills, boolean teamPointsNotInvidual)
	{
		String msg = "";
		if(isTeamed() && teamPointsNotInvidual)
		{
			List<FightClubTeam> teams = getTeams();
			Collections.sort(teams, new BestTeamComparator(showScoreNotKills));
			for(FightClubTeam team : teams)
				msg = new StringBuilder().append(msg).append(team.getName()).append(" Team: ").append(showScoreNotKills ? team.getScore() : getTeamTotalKills(team)).append(" ").append(showScoreNotKills ? "Points" : "Kills").append("\n").toString(); // TODO: Вынести в ДП.
		}
		else
		{
			List<FightClubPlayer> fPlayers = getPlayers(new String[] { "fighting_players" });
			List<FightClubPlayer> changedFPlayers = new ArrayList<FightClubPlayer>(fPlayers.size());
			changedFPlayers.addAll(fPlayers);

			Collections.sort(changedFPlayers, new BestPlayerComparator(showScoreNotKills));
			int max = Math.min(10, changedFPlayers.size());

			for(int i = 0; i < max; i++)
				msg = new StringBuilder().append(msg).append(changedFPlayers.get(i).getPlayer().getName()).append(" ").append(showScoreNotKills ? "Score" : "Kills").append(": ").append(showScoreNotKills ? changedFPlayers.get(i).getScore() : changedFPlayers.get(i).getKills(true)).append("\n").toString(); // TODO: Вынести в ДП.
		}
		return msg;
	}

  protected int getRewardForWinningTeam(FightClubPlayer fPlayer, boolean atLeast1Kill)
  {
    if ((!_teamed) || ((_state != EVENT_STATE.OVER) && (_state != EVENT_STATE.NOT_ACTIVE))) {
      return 0;
    }
    if ((atLeast1Kill) && (fPlayer.getKills(true) <= 0) && (FightClubGameRoom.getPlayerClassGroup(fPlayer.getPlayer()) != FightClubEventManager.CLASSES.HEALERS)) {
      return 0;
    }
    FightClubTeam winner = null;
    int winnerPoints = -1;
    boolean sameAmount = false;
    for (FightClubTeam team : getTeams())
    {
      if (team.getScore() > winnerPoints)
      {
        winner = team;
        winnerPoints = team.getScore();
        sameAmount = false;
      }
      else if (team.getScore() == winnerPoints)
      {
        sameAmount = true;
      }
    }
    
    if ((!sameAmount) && (fPlayer.getTeam().equals(winner)))
    {
      return (int)_badgeWin;
    }
    

    return 0;
  }

	private boolean nobodyIsClose(Location loc)
	{
		for(FightClubPlayer iFPlayer : getPlayers(new String[] { "fighting_players" }))
		{
			Location playerLoc = iFPlayer.getPlayer().getLoc();
			if(Math.abs(playerLoc.getX() - loc.getX()) <= 80)
				return false;
			if(Math.abs(playerLoc.getY() - loc.getY()) <= 80)
				return false;
		}
		return true;
	}

	private void checkIfRegisteredMeetCriteria()
	{
		for(FightClubPlayer iFPlayer : getPlayers(new String[] { "registered_players" }))
			checkIfRegisteredPlayerMeetCriteria(iFPlayer);
	}

	private boolean checkIfRegisteredPlayerMeetCriteria(FightClubPlayer fPlayer)
	{
		return FightClubEventManager.getInstance().canPlayerParticipate(fPlayer.getPlayer(), true, false);
	}

	private void cancelNegativeEffects(Playable playable)
	{
		List<Effect> _buffList = new ArrayList<Effect>();

		for(Effect e : playable.getEffectList().getAllEffects())
		{
			if(e.isOffensive() && e.isCancelable())
				_buffList.add(e);
		}

		for(Effect e : _buffList)
			e.exit();
	}

	private FightClubEventManager.CLASSES[] parseExcludedClasses(String classes)
	{
		if(classes.isEmpty())
			return new FightClubEventManager.CLASSES[0];

		String[] classType = classes.split(";");
		FightClubEventManager.CLASSES[] realTypes = new FightClubEventManager.CLASSES[classType.length];

		for(int i = 0; i < classType.length; i++)
			realTypes[i] = FightClubEventManager.CLASSES.valueOf(classType[i]);

		return realTypes;
	}

	protected int[] parseExcludedSkills(String ids)
	{
		if(ids == null || ids.isEmpty())
			return null;

		StringTokenizer st = new StringTokenizer(ids, ";");
		int[] realIds = new int[st.countTokens()];
		int index = 0;
		while(st.hasMoreTokens())
		{
			realIds[index] = Integer.parseInt(st.nextToken());
			index++;
		}
		return realIds;
	}

	private int[][] parseAutoStartTimes(String times)
	{
		if(times == null || times.isEmpty())
			return (int[][])null;

		StringTokenizer st = new StringTokenizer(times, ",");
		int[][] realTimes = new int[st.countTokens()][2];
		int index = 0;
		while(st.hasMoreTokens())
		{
			String[] hourMin = st.nextToken().split(":");
			int[] realHourMin = { Integer.parseInt(hourMin[0]), Integer.parseInt(hourMin[1]) };
			realTimes[index] = realHourMin;
			index++;
		}
		return realTimes;
	}

	private int[][] parseBuffs(String buffs)
	{
		if(buffs == null || buffs.isEmpty())
			return (int[][])null;

		StringTokenizer st = new StringTokenizer(buffs, ";");
		int[][] realBuffs = new int[st.countTokens()][2];
		int index = 0;
		while(st.hasMoreTokens())
		{
			String[] skillLevel = st.nextToken().split(",");
			int[] realHourMin = { Integer.parseInt(skillLevel[0]), Integer.parseInt(skillLevel[1]) };
			realBuffs[index] = realHourMin;
			index++;
		}
		return realBuffs;
	}

	private int getTimeToWait(int totalLeftTimeInSeconds)
	{
		int toWait = 1;

		int[] stops = { 5, 15, 30, 60, 300, 600, 900 };

		for(int stop : stops)
		{
			if(totalLeftTimeInSeconds > stop)
				toWait = stop;
		}
		return toWait;
	}

	@Deprecated
	public static boolean teleportWholeRoomTimer(int eventObjId, int secondsLeft)
	{
		AbstractFightClub event = FightClubEventManager.getInstance().getEventByObjId(eventObjId);
		if(secondsLeft == 0)
		{
			event._dontLetAnyoneIn = true;
			event.startEvent();
		}
		else
		{
			event.checkIfRegisteredMeetCriteria();
			event.sendMessageToRegistered(MESSAGE_TYPES.SCREEN_BIG, new StringBuilder().append("You are going to be teleported in ").append(getFixedTime(secondsLeft)).append("!").toString()); // TODO: Вынести в ДП.
		}
		return true;
	}

	@Deprecated
	public static boolean startRoundTimer(int eventObjId, int secondsLeft)
	{
		AbstractFightClub event = FightClubEventManager.getInstance().getEventByObjId(eventObjId);

		if(secondsLeft > 0)
		{
			String firstWord;
			if(event.isRoundEvent())
				firstWord = new StringBuilder().append(event.getCurrentRound() + 1 == event.getTotalRounds() ? "Last" : ROUND_NUMBER_IN_STRING[(event.getCurrentRound() + 1)]).append(" Round").toString(); // TODO: Вынести в ДП.
			else
				firstWord = "Match";
			String message = new StringBuilder().append(firstWord).append(" is going to start in ").append(getFixedTime(secondsLeft)).append("!").toString(); // TODO: Вынести в ДП.
			event.sendMessageToFighting(MESSAGE_TYPES.SCREEN_BIG, message, true);
		}
		else
			event.startRound();

		return true;
	}

	@Deprecated
	public static boolean endRoundTimer(int eventObjId, int secondsLeft)
	{
		AbstractFightClub event = FightClubEventManager.getInstance().getEventByObjId(eventObjId);
		if(secondsLeft > 0)
			event.sendMessageToFighting(MESSAGE_TYPES.SCREEN_BIG, new StringBuilder().append(!event.isLastRound() ? "Round" : "Match").append(" is going to be Over in ").append(getFixedTime(secondsLeft)).append("!").toString(), false); // TODO: Вынести в ДП.
		else
			event.endRound();

		return true;
	}

	@Deprecated
	public static boolean shutDownTimer(int eventObjId, int secondsLeft)
	{
		AbstractFightClub event = FightClubEventManager.getInstance().getEventByObjId(eventObjId);

		if(!FightClubEventManager.getInstance().serverShuttingDown())
		{
			event._dontLetAnyoneIn = false;
			return false;
		}

		if(secondsLeft < 180)
		{
			if(!event._dontLetAnyoneIn)
			{
				event.sendMessageToRegistered(MESSAGE_TYPES.CRITICAL, "You are no longer registered because of Shutdown!"); // TODO: Вынести в ДП.
				for(FightClubPlayer player : event.getPlayers(new String[] { "registered_players" }))
					event.unregister(player.getPlayer());

				event.getObjects("registered_players").clear();
				event._dontLetAnyoneIn = true;
			}
		}

		if(secondsLeft < 60)
		{
			event._timer.cancel(false);
			event.sendMessageToFighting(MESSAGE_TYPES.CRITICAL, "Event ended because of Shutdown!", false); // TODO: Вынести в ДП.
			event.setState(EVENT_STATE.OVER);
			event.stopEvent(false);

			event._dontLetAnyoneIn = false;
			return false;
		}
		return true;
	}

	@Deprecated
	public static boolean teleportBackSinglePlayerTimer(int eventObjId, int secondsLeft, Player player)
	{
		AbstractFightClub event = FightClubEventManager.getInstance().getEventByObjId(eventObjId);

		if(player == null || !player.isOnline())
			return false;

		if(secondsLeft > 0)
			event.sendMessageToPlayer(player, MESSAGE_TYPES.SCREEN_BIG, new StringBuilder().append("You are going to be teleported back in ").append(getFixedTime(secondsLeft)).append("!").toString()); // TODO: Вынести в ДП.
		else
			event.teleportBackToTown(player);

		return true;
	}

	@Deprecated
	public static boolean ressurectionTimer(int eventObjId, int secondsLeft, FightClubPlayer fPlayer)
	{
		AbstractFightClub event = FightClubEventManager.getInstance().getEventByObjId(eventObjId);
		Player player = fPlayer.getPlayer();

		if(player == null || !player.isOnline() || !player.isDead())
			return false;

		if(secondsLeft > 0)
			player.sendMessage(new StringBuilder().append("Respawn in ").append(getFixedTime(secondsLeft)).append("!").toString()); // TODO: Вынести в ДП.
		else
		{
			event.hideScores(player);
			event.teleportSinglePlayer(fPlayer, false, true);
		}
		return true;
	}

	@Deprecated
	public static boolean setInvisible(int eventObjId, int secondsLeft, FightClubPlayer fPlayer, boolean sendMessages)
	{
		AbstractFightClub event = FightClubEventManager.getInstance().getEventByObjId(eventObjId);
		if(fPlayer.getPlayer() == null || !fPlayer.getPlayer().isOnline())
			return false;

		if(secondsLeft > 0)
		{
			if(sendMessages)
				event.sendMessageToPlayer(fPlayer, MESSAGE_TYPES.SCREEN_BIG, new StringBuilder().append("Visible in ").append(getFixedTime(secondsLeft)).append("!").toString()); // TODO: Вынести в ДП.
		}
		else
		{
			if(sendMessages && event.getState() == EVENT_STATE.STARTED)
				event.sendMessageToPlayer(fPlayer, MESSAGE_TYPES.SCREEN_BIG, "Fight!");
			event.stopInvisibility(fPlayer.getPlayer());
		}
		return true;
	}

	public void startNewTimer(boolean saveAsMainTimer, int firstWaitingTimeInMilis, String methodName, Object[] args)
	{
		ScheduledFuture<?> timer = ThreadPoolManager.getInstance().schedule(new SmartTimer(methodName, saveAsMainTimer, args), firstWaitingTimeInMilis);

		if(saveAsMainTimer)
			_timer = timer;
	}

	@Override
	public void reCalcNextTime(boolean onInit)
	{
		clearActions();

		registerActions();
	}

	@Override
	public EventType getType()
	{
		return EventType.FIGHT_CLUB_EVENT;
	}

	@Override
	protected long startTimeMillis()
	{
		return 0L;
	}

	@Override
	public void onAddEvent(GameObject o)
	{
		if(o.isPlayer())
			o.getPlayer().addListener(_exitListener);
	}

	@Override
	public void onRemoveEvent(GameObject o)
	{
		if(o.isPlayer())
			o.getPlayer().removeListener(_exitListener);
	}

	@Override
	protected void printInfo()
	{
		info(getName() + " inited");
	}

	public void printScheduledTime(long startTime)
	{
		info(getName() + " time - " + TimeUtils.toSimpleFormat(startTime));
	}

	@Override
	public boolean isInProgress()
	{
		return _state != EVENT_STATE.NOT_ACTIVE;
	}

	private class SmartTimer extends RunnableImpl
	{
		private final String _methodName;
		private final Object[] _args;
		private final boolean _saveAsMain;

		private SmartTimer(String methodName, boolean saveAsMainTimer, Object[] args)
		{
			_methodName = methodName;

			Object[] changedArgs = new Object[args.length + 1];
			changedArgs[0] = Integer.valueOf(getObjectId());
			for(int i = 0; i < args.length; i++)
				changedArgs[(i + 1)] = args[i];
			_args = changedArgs;
			_saveAsMain = saveAsMainTimer;
		}

		@Override
		public void runImpl() throws Exception
		{
			Class<?>[] parameterTypes = new Class<?>[_args.length];
			for(int i = 0; i < _args.length; i++)
				parameterTypes[i] = _args[i] != null ? _args[i].getClass() : null;

			int waitingTime = ((Integer)_args[1]).intValue();
			try
			{
				Object ret = MethodUtils.invokeMethod(AbstractFightClub.this, _methodName, _args, parameterTypes);

				if(!((Boolean)ret).booleanValue())
					return;
			}
			catch(java.lang.IllegalAccessException e)
			{
				e.printStackTrace();
			}

			if(waitingTime > 0)
			{
				int toWait = AbstractFightClub.this.getTimeToWait(waitingTime);

				waitingTime -= toWait;

				_args[1] = Integer.valueOf(waitingTime);

				ScheduledFuture<?> timer = ThreadPoolManager.getInstance().schedule(this, toWait * 1000);

				if(_saveAsMain)
					_timer = timer;
			}
			else
				return;
		}
	}

	private class BestPlayerComparator implements Comparator<FightClubPlayer>
	{
		private boolean _scoreNotKills;

		private BestPlayerComparator(boolean scoreNotKills)
		{
			_scoreNotKills = scoreNotKills;
		}

		public int compare(FightClubPlayer arg0, FightClubPlayer arg1)
		{
			if(_scoreNotKills)
				return Integer.compare(arg1.getScore(), arg0.getScore());
			return Integer.compare(arg1.getKills(true), arg0.getKills(true));
		}
	}

	private class BestTeamComparator implements Comparator<FightClubTeam>
	{
		private boolean _scoreNotKills;

		private BestTeamComparator(boolean scoreNotKills)
		{
			_scoreNotKills = scoreNotKills;
		}

		@Override
		public int compare(FightClubTeam arg0, FightClubTeam arg1)
		{
			if(_scoreNotKills)
				return Integer.compare(arg1.getScore(), arg0.getScore());
			return Integer.compare(getTeamTotalKills(arg1), getTeamTotalKills(arg0));
		}
	}

	private class CheckAfkThread extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			long currentTime = System.currentTimeMillis();
			for(FightClubPlayer iFPlayer : getPlayers(new String[] { "fighting_players" }))
			{
				Player player = iFPlayer.getPlayer();
				boolean isAfk = player.getLastNotAfkTime() + 30000L < currentTime;

				if(player.isDead() && !_ressAllowed && getRespawnTime() <= 0)
					isAfk = false;

				if(iFPlayer.isAfk())
				{
					if(!isAfk)
						AbstractFightClub.this.handleAfk(iFPlayer, false);
					else if(_state != AbstractFightClub.EVENT_STATE.OVER)
						sendMessageToPlayer(player, AbstractFightClub.MESSAGE_TYPES.CRITICAL, "You are in AFK mode!"); // TODO: Вынести в ДП.
				}
				else if(_state == AbstractFightClub.EVENT_STATE.NOT_ACTIVE)
					AbstractFightClub.this.handleAfk(iFPlayer, false);
				else if(isAfk)
					AbstractFightClub.this.handleAfk(iFPlayer, true);
			}

			if(getState() != AbstractFightClub.EVENT_STATE.NOT_ACTIVE)
				ThreadPoolManager.getInstance().schedule(this, 1000L);
			else
			{
				for(FightClubPlayer iFPlayer : getPlayers(new String[] { "fighting_players" }))
				{
					if(iFPlayer.isAfk())
						AbstractFightClub.this.handleAfk(iFPlayer, false);
				}
			}
		}
	}

	private class LeftZoneThread extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			List<FightClubPlayer> toDelete = new ArrayList<FightClubPlayer>();
			SayPacket2 packet = new SayPacket2(0, ChatType.COMMANDCHANNEL_ALL, "Error", "Go Back To Event Zone!"); // TODO: Вынести в ДП.

			for(Map.Entry<FightClubPlayer, Zone> entry : _leftZone.entrySet())
			{
				Player player = entry.getKey().getPlayer();
				if(player == null || !player.isOnline() || _state == AbstractFightClub.EVENT_STATE.NOT_ACTIVE || entry.getValue().checkIfInZone(player) || player.isDead() || player.isTeleporting())
				{
					toDelete.add(entry.getKey());
					continue;
				}

				int power = (int) Math.max(400.0D, entry.getValue().findDistanceToZone(player, true) - 4000.0D);

				player.sendPacket(new EarthQuakePacket(player.getLoc(), power, 5));
				player.sendPacket(packet);
				entry.getKey().increaseSecondsOutsideZone();

				if(entry.getKey().getSecondsOutsideZone() >= 10)
				{
					player.doDie(null);
					toDelete.add(entry.getKey());
					entry.getKey().clearSecondsOutsideZone();
				}
			}

			for(FightClubPlayer playerToDelete : toDelete)
			{
				if(playerToDelete != null)
				{
					_leftZone.remove(playerToDelete);
					playerToDelete.clearSecondsOutsideZone();
				}
			}

			if(_state != AbstractFightClub.EVENT_STATE.NOT_ACTIVE)
				ThreadPoolManager.getInstance().schedule(this, 1000L);
		}
	}

	private class TimeSpentOnEventThread extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			if(_state == AbstractFightClub.EVENT_STATE.STARTED)
			{
				for(FightClubPlayer iFPlayer : getPlayers(new String[] { "fighting_players" }))
				{
					if(iFPlayer.getPlayer() == null || !iFPlayer.getPlayer().isOnline() || iFPlayer.isAfk())
						continue;
					iFPlayer.incSecondsSpentOnEvent(10);
				}
			}

			if(_state != AbstractFightClub.EVENT_STATE.NOT_ACTIVE)
				ThreadPoolManager.getInstance().schedule(new TimeSpentOnEventThread(), 10000L);
		}
	}

	public static enum MESSAGE_TYPES
	{
		GM, 
		NORMAL_MESSAGE, 
		SCREEN_BIG, 
		SCREEN_SMALL, 
		CRITICAL;
	}

	public static enum EVENT_STATE
	{
		NOT_ACTIVE, 
		COUNT_DOWN, 
		PREPARATION, 
		STARTED, 
		OVER;
	}

	private class ZoneListener implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature actor)
		{
			if(actor.isPlayer())
			{
				FightClubPlayer fPlayer = getFightClubPlayer(actor);
				if(fPlayer != null)
				{
					actor.sendPacket(new EarthQuakePacket(actor.getLoc(), 0, 1));
					_leftZone.remove(getFightClubPlayer(actor));
				}
			}
		}

		@Override
		public void onZoneLeave(Zone zone, Creature actor)
		{
			if(actor.isPlayer() && _state != AbstractFightClub.EVENT_STATE.NOT_ACTIVE)
			{
				FightClubPlayer fPlayer = getFightClubPlayer(actor);
				if(fPlayer != null)
					_leftZone.put(getFightClubPlayer(actor), zone);
			}
		}
	}

	private class ExitListener implements OnPlayerExitListener
	{
		@Override
		public void onPlayerExit(Player player)
		{
			loggedOut(player);
		}
	}
	
  protected boolean isPlayerActive(Player player)
  {
    if (player == null)
      return false;
    if (player.isDead())
      return false;
    if (!player.getReflection().equals(getReflection()))
      return false;
    if (System.currentTimeMillis() - player.getLastNotAfkTime() > 120000L) {
      return false;
    }
    boolean insideZone = false;
    for (Zone zone : getReflection().getZones())
      if (zone.checkIfInZone(player.getX(), player.getY(), player.getZ(), player.getReflection()))
        insideZone = true;
    if (!insideZone) {
      return false;
    }
    return true;
  }	
  
  private FightClubPlayer[] getTopKillers()
  {
    if ((!_teamed) || (topKillerReward == 0)) {
      return null;
    }
    FightClubPlayer[] topKillers = new FightClubPlayer[_teams.size()];
    int[] topKillersKills = new int[_teams.size()];
    
    int teamIndex = 0;
    for (FightClubTeam team : _teams)
    {
      for (FightClubPlayer fPlayer : team.getPlayers())
      {
        if (fPlayer != null)
        {
          if (fPlayer.getKills(true) == topKillersKills[teamIndex])
          {
            topKillers[teamIndex] = null;
          }
          else if (fPlayer.getKills(true) > topKillersKills[teamIndex])
          {
            topKillers[teamIndex] = fPlayer;
            topKillersKills[teamIndex] = fPlayer.getKills(true);
          }
        }
      }
      teamIndex++;
    }
    return topKillers;
  } 
  
  protected boolean isAfkTimerStopped(Player player) {
    return (player.isDead()) && (!_ressAllowed) && (_respawnTime <= 0);
  } 
}
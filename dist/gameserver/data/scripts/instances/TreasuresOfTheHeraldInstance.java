package instances;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

import org.apache.commons.lang3.mutable.MutableInt;

import events.TreasuresOfTheHerald.TreasuresOfTheHerald;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.lang.reference.HardReference;
import l2s.commons.lang.reference.HardReferences;
import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.listener.actor.player.OnPlayerPartyLeaveListener;
import l2s.gameserver.listener.actor.player.OnTeleportListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.ExBlockUpSetList;
import l2s.gameserver.network.l2.s2c.ExBlockUpSetState;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.RevivePacket;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;

@SuppressWarnings("unused")
public class TreasuresOfTheHeraldInstance extends Reflection
{
	private final static int BOX_ID = 18822;
	private final static int BOSS_ID = 25655;

	private final static int SCORE_BOX = Config.EVENT_TREASURES_OF_THE_HERALD_SCORE_BOX;
	private final static int SCORE_BOSS = Config.EVENT_TREASURES_OF_THE_HERALD_SCORE_BOSS;
	private final static int SCORE_KILL = Config.EVENT_TREASURES_OF_THE_HERALD_SCORE_KILL;
	private final static int SCORE_DEATH = Config.EVENT_TREASURES_OF_THE_HERALD_SCORE_DEATH;

	private int eventTime = Config.EVENT_TREASURES_OF_THE_HERALD_TIME;
	private long bossSpawnTime = 10 * 60 * 1000L;

	private boolean active = false;

	private Party team1;
	private Party team2;
	private List<HardReference<Player>> bothTeams = new CopyOnWriteArrayList<HardReference<Player>>();

	private TIntObjectHashMap<MutableInt> score = new TIntObjectHashMap<MutableInt>();
	private int team1Score = 0;
	private int team2Score = 0;

	private long startTime;

	private ScheduledFuture<?> _bossSpawnTask;
	private ScheduledFuture<?> _countDownTask;
	private ScheduledFuture<?> _battleEndTask;

	private DeathListener _deathListener = new DeathListener();
	private TeleportListener _teleportListener = new TeleportListener();
	private PlayerPartyLeaveListener _playerPartyLeaveListener = new PlayerPartyLeaveListener();

	private Zone zonebattle;
	private Zone zonepvp;

	private Zone zonepeace1;
	private Zone peace1;

	private Zone zonepeace2;
	private Zone peace2;

	public void setTeam1(Party party1)
	{
		team1 = party1;
	}

	public void setTeam2(Party party2)
	{
		team2 = party2;
	}

	public TreasuresOfTheHeraldInstance()
	{
		super();
	}

	/**
	 * General instance initialization and assigning global variables
	 */
	public void start()
	{
		zonepvp = getZone("[gvg_battle_zone]");
		peace1 = getZone("[gvg_1_peace]");
		peace2 = getZone("[gvg_2_peace]");
		//Box spawns
		Location boxes[] = {new Location(142696, 139704, -15264, 0), new Location(142696, 145944, -15264, 0), new Location(145784, 142824, -15264, 0), new Location(145768, 139704, -15264, 0), new Location(145768, 145944, -15264, 0), new Location(141752, 142760, -15624, 0), new Location(145720, 142008, -15880, 0), new Location(145720, 143640, -15880, 0), new Location(139592, 142824, -15264, 0)};

		for(int i = 0; i < boxes.length; i++)
		{
			addSpawnWithoutRespawn(BOX_ID, boxes[i], 0);
		}

		addSpawnWithoutRespawn(35423, new Location(139640, 139736, -15264), 0); //Red team flag
		addSpawnWithoutRespawn(35426, new Location(139672, 145896, -15264), 0); //Blue team flag

		_bossSpawnTask = ThreadPoolManager.getInstance().schedule(new BossSpawn(), bossSpawnTime); //
		_countDownTask = ThreadPoolManager.getInstance().schedule(new CountingDown(), (eventTime - 1) * 1000L);
		_battleEndTask = ThreadPoolManager.getInstance().schedule(new BattleEnd(), (eventTime - 6) * 1000L); // -6 is about to prevent built-in BlockChecker countdown task

		//Assigning players to teams
		for(Player member : team1.getPartyMembers())
		{
			bothTeams.add(member.getRef());
			member.addListener(_deathListener);
			member.addListener(_teleportListener);
			member.addListener(_playerPartyLeaveListener);
		}

		for(Player member : team2.getPartyMembers())
		{
			bothTeams.add(member.getRef());
			member.addListener(_deathListener);
			member.addListener(_teleportListener);
			member.addListener(_playerPartyLeaveListener);
		}

		startTime = System.currentTimeMillis() + eventTime * 1000L; //Used in packet broadcasting

		//Forming packets to send everybody
		final ExBlockUpSetState.PointsInfo initialPoints = new ExBlockUpSetState.PointsInfo(eventTime, team1Score, team2Score);
		final ExBlockUpSetList.CloseUI cui = new ExBlockUpSetList.CloseUI();
		ExBlockUpSetState.ChangePoints clientSetUp;

		for(Player tm : HardReferences.unwrap(bothTeams))
		{
			score.put(tm.getObjectId(), new MutableInt());

			tm.setCurrentCp(tm.getMaxCp());
			tm.setCurrentHp(tm.getMaxHp(), false);
			tm.setCurrentMp(tm.getMaxMp());
			clientSetUp = new ExBlockUpSetState.ChangePoints(eventTime, team1Score, team2Score, isRedTeam(tm), tm, 0);
			tm.sendPacket(clientSetUp);
			tm.sendActionFailed(); //useless? copy&past from BlockChecker
			tm.sendPacket(initialPoints);
			tm.sendPacket(cui); //useless? copy&past from BlockChecker
			broadCastPacketToBothTeams(new ExBlockUpSetList.AddPlayer(tm, isRedTeam(tm)));
		}

		active = true;
	}

	/**
	 * @param packet Broadcasting packet to every member of instance
	 */
	private void broadCastPacketToBothTeams(L2GameServerPacket packet)
	{
		for(Player tm : HardReferences.unwrap(bothTeams))
		{
			tm.sendPacket(packet);
		}
	}

	/**
	 * @return Whether event is active. active starts with instance dungeon and ends with team victory
	 */
	private boolean isActive()
	{
		return active;
	}

	/**
	 * @param player
	 * @return Whether player belongs to Red Team (team2)
	 */
	private boolean isRedTeam(Player player)
	{
		if(team2.containsMember(player))
		{
			return true;
		}
		return false;
	}

	/**
	 * Handles the end of event
	 */
	private void end()
	{
		active = false;

		startCollapseTimer(60 * 1000L);

		paralyzePlayers();
		ThreadPoolManager.getInstance().schedule(new Finish(), 55 * 1000L);

		if(_bossSpawnTask != null)
		{
			_bossSpawnTask.cancel(false);
			_bossSpawnTask = null;
		}
		if(_countDownTask != null)
		{
			_countDownTask.cancel(false);
			_countDownTask = null;
		}
		if(_battleEndTask != null)
		{
			_battleEndTask.cancel(false);
			_battleEndTask = null;
		}

		boolean isRedWinner = false;

		isRedWinner = getRedScore() >= getBlueScore();

		final ExBlockUpSetState.GameEnd end = new ExBlockUpSetState.GameEnd(isRedWinner);
		broadCastPacketToBothTeams(end);

		reward(isRedWinner ? team2 : team1);
		TreasuresOfTheHerald.updateWinner(isRedWinner ? team2.getPartyLeader() : team1.getPartyLeader());

		//Удаление созданных зон из мира
		zonepvp.setActive(false);
		peace1.setActive(false);
		peace2.setActive(false);
	}

	private void reward(Party party)
	{
		for(Player member : party.getPartyMembers())
		{
			member.sendMessage("Ваша группа выиграла турнир Treasures of the Herald, лидер группы добавлен в рейтинг победителей.");
			member.setFame(member.getFame() + 500, "Treasures of the Herald"); // fame
			ItemFunctions.addItem(member, Config.EVENT_TREASURES_OF_THE_HERALD_ITEM_ID, Config.EVENT_TREASURES_OF_THE_HERALD_ITEM_COUNT, "Treasures Of The Herald instance reward");
		}
	}

	private class DeathListener implements OnDeathListener
	{
		@Override
		public void onDeath(Creature self, Creature killer)
		{
			if(!isActive())
			{
				return;
			}

			//Убийство произошло в инстанте
			if(self.getReflection() != killer.getReflection() || self.getReflection() != TreasuresOfTheHeraldInstance.this)
			{
				return;
			}

			if(self.isPlayer() && killer.isPlayable()) //if PvP kill
			{
				if(team1.containsMember(self.getPlayer()) && team2.containsMember(killer.getPlayer()))
				{
					addPlayerScore(killer.getPlayer());
					changeScore(1, SCORE_KILL, SCORE_DEATH, true, true, killer.getPlayer());
				}
				else if(team2.containsMember(self.getPlayer()) && team1.containsMember(killer.getPlayer()))
				{
					addPlayerScore(killer.getPlayer());
					changeScore(2, SCORE_KILL, SCORE_DEATH, true, true, killer.getPlayer());
				}
				resurrectAtBase(self.getPlayer());
			}
			else if(self.isPlayer() && !killer.isPlayable()) //if not-PvP kill
			{
				resurrectAtBase(self.getPlayer());
			}
			else if(self.isNpc() && killer.isPlayable())
			{
				if(self.getNpcId() == BOX_ID)
				{
					if(team1.containsMember(killer.getPlayer()))
					{
						changeScore(1, SCORE_BOX, 0, false, false, killer.getPlayer());
					}
					else if(team2.containsMember(killer.getPlayer()))
					{
						changeScore(2, SCORE_BOX, 0, false, false, killer.getPlayer());
					}
				}
				else if(self.getNpcId() == BOSS_ID)
				{
					if(team1.containsMember(killer.getPlayer()))
					{
						changeScore(1, SCORE_BOSS, 0, false, false, killer.getPlayer());
					}
					else if(team2.containsMember(killer.getPlayer()))
					{
						changeScore(2, SCORE_BOSS, 0, false, false, killer.getPlayer());
					}

					broadCastPacketToBothTeams(new ExShowScreenMessage("Охранник Сокровищ Геральда погиб от руки " + killer.getName(), 5000, ScreenMessageAlign.MIDDLE_CENTER, true));
					end();
				}
			}
		}
	}

	/**
	 * @param teamId
	 * @param toAdd             - how much points to add
	 * @param toSub             - how much points to remove
	 * @param subbing           - whether change is reducing points
	 * @param affectAnotherTeam - change can affect only teamId or both
	 * @param player            Any score change are handled here.
	 */
	private synchronized void changeScore(int teamId, int toAdd, int toSub, boolean subbing, boolean affectAnotherTeam, Player player)
	{
		int timeLeft = (int) ((startTime - System.currentTimeMillis()) / 1000);
		if(teamId == 1)
		{
			if(subbing)
			{
				team1Score -= toSub;
				if(team1Score < 0)
				{
					team1Score = 0;
				}
				if(affectAnotherTeam)
				{
					team2Score += toAdd;
					broadCastPacketToBothTeams(new ExBlockUpSetState.ChangePoints(timeLeft, team1Score, team2Score, true, player, getPlayerScore(player)));
				}
				broadCastPacketToBothTeams(new ExBlockUpSetState.ChangePoints(timeLeft, team1Score, team2Score, false, player, getPlayerScore(player)));
			}
			else
			{
				team1Score += toAdd;
				if(affectAnotherTeam)
				{
					team2Score -= toSub;
					if(team2Score < 0)
					{
						team2Score = 0;
					}
					broadCastPacketToBothTeams(new ExBlockUpSetState.ChangePoints(timeLeft, team1Score, team2Score, true, player, getPlayerScore(player)));
				}
				broadCastPacketToBothTeams(new ExBlockUpSetState.ChangePoints(timeLeft, team1Score, team2Score, false, player, getPlayerScore(player)));
			}
		}
		else if(teamId == 2)
		{
			if(subbing)
			{
				team2Score -= toSub;
				if(team2Score < 0)
				{
					team2Score = 0;
				}
				if(affectAnotherTeam)
				{
					team1Score += toAdd;
					broadCastPacketToBothTeams(new ExBlockUpSetState.ChangePoints(timeLeft, team1Score, team2Score, false, player, getPlayerScore(player)));
				}
				broadCastPacketToBothTeams(new ExBlockUpSetState.ChangePoints(timeLeft, team1Score, team2Score, true, player, getPlayerScore(player)));
			}
			else
			{
				team2Score += toAdd;
				if(affectAnotherTeam)
				{
					team1Score -= toSub;
					if(team1Score < 0)
					{
						team1Score = 0;
					}
					broadCastPacketToBothTeams(new ExBlockUpSetState.ChangePoints(timeLeft, team1Score, team2Score, false, player, getPlayerScore(player)));
				}
				broadCastPacketToBothTeams(new ExBlockUpSetState.ChangePoints(timeLeft, team1Score, team2Score, true, player, getPlayerScore(player)));
			}
		}
	}

	/**
	 * @param player Handles the increase of personal player points
	 */
	private void addPlayerScore(Player player)
	{
		MutableInt points = score.get(player.getObjectId());
		points.increment();
	}

	/**
	 * @param player
	 * @return Returns personal player score
	 */
	public int getPlayerScore(Player player)
	{
		MutableInt points = score.get(player.getObjectId());
		return points.intValue();
	}

	/**
	 * Paralyzes everybody in instance to prevent any actions while event is !isActive
	 */
	public void paralyzePlayers()
	{
		for(Player tm : HardReferences.unwrap(bothTeams))
		{
			if(tm.isDead())
			{
				tm.setCurrentHp(tm.getMaxHp(), true);
				tm.broadcastPacket(new RevivePacket(tm));
			}
			else
			{
				tm.setCurrentHp(tm.getMaxHp(), false);
			}

			tm.setCurrentMp(tm.getMaxMp());
			tm.setCurrentCp(tm.getMaxCp());

			tm.getEffectList().stopEffect(Skill.SKILL_MYSTIC_IMMUNITY);
			tm.block();
		}
	}

	/**
	 * Romoves paralization
	 */
	public void unParalyzePlayers()
	{
		for(Player tm : HardReferences.unwrap(bothTeams))
		{
			tm.unblock();
			removePlayer(tm, true);
		}
	}

	/**
	 * Cleans up every list and task
	 */
	private void cleanUp()
	{
		team1 = null;
		team2 = null;
		bothTeams.clear();
		team1Score = 0;
		team2Score = 0;
		score.clear();
	}

	/**
	 * @param player
	 * @param refId  Called by onDeath. Handles the resurrection at the proper base.
	 */
	public void resurrectAtBase(Player player)
	{
		if(player.isDead())
		{
			//player.setCurrentCp(player.getMaxCp());
			player.setCurrentHp(0.7 * player.getMaxHp(), true);
			//player.setCurrentMp(player.getMaxMp());
			player.broadcastPacket(new RevivePacket(player));
		}
		player.altOnMagicUseTimer(player, SkillHolder.getInstance().getSkill(5660, 2)); // Battlefield Death Syndrome

		Location pos;
		if(team1.containsMember(player))
		{
			pos = Location.findPointToStay(TreasuresOfTheHerald.TEAM1_LOC, 0, 150, getGeoIndex());
		}
		else
		{
			pos = Location.findPointToStay(TreasuresOfTheHerald.TEAM2_LOC, 0, 150, getGeoIndex());
		}

		player.teleToLocation(pos, this);
	}

	/**
	 * @param player
	 * @param legalQuit - whether quit was called by event or by player escape
	 *                  Removes player from every list or instance, teleports him and stops the event timer
	 */
	private void removePlayer(Player player, boolean legalQuit)
	{
		bothTeams.remove(player.getRef());

		broadCastPacketToBothTeams(new ExBlockUpSetList.RemovePlayer(player, isRedTeam(player)));
		player.removeListener(_deathListener);
		player.removeListener(_teleportListener);
		player.removeListener(_playerPartyLeaveListener);
		player.leaveParty();
		if(!legalQuit)
		{
			player.sendPacket(new ExBlockUpSetState.GameEnd(false));
		}
		player.teleToLocation(Location.findPointToStay(TreasuresOfTheHerald.RETURN_LOC, 0, 150, ReflectionManager.DEFAULT.getGeoIndex()), ReflectionManager.DEFAULT);
	}

	/**
	 * @param isRed Handles the team withdraw from the area of event. Can only be called when !isActive
	 */
	private void teamWithdraw(Party party)
	{
		if(party == team1)
		{
			for(Player player : team1.getPartyMembers())
			{
				removePlayer(player, false);
			}

			Player player = team2.getPartyLeader();
			changeScore(2, 200, 0, false, false, player); //adding 200 to the team score for enemy team withdrawal. player - leader of the team who's left in the instance
		}
		else
		{
			for(Player player : team2.getPartyMembers())
			{
				removePlayer(player, false);
			}

			Player player = team1.getPartyLeader();
			changeScore(1, 200, 0, false, false, player); //adding 200 to the team score for enemy team withdrawal. player - leader of the team who's left in the instance
		}

		broadCastPacketToBothTeams(new ExShowScreenMessage("Команда соперника покинула поле боя в полном составе. Конец сражения.", 4000, ScreenMessageAlign.MIDDLE_CENTER, true));
		end();
	}

	private int getBlueScore()
	{
		return team1Score;
	}

	private int getRedScore()
	{
		return team2Score;
	}

	public class BossSpawn extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			broadCastPacketToBothTeams(new ExShowScreenMessage("Появился Охранник Сокровищ Геральда", 5000, ScreenMessageAlign.MIDDLE_CENTER, true));
			addSpawnWithoutRespawn(BOSS_ID, new Location(147304, 142824, -15864, 32768), 0);
			openDoor(24220042);
		}
	}

	public class CountingDown extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			broadCastPacketToBothTeams(new ExShowScreenMessage("До конца сражения осталась 1 минута", 4000, ScreenMessageAlign.MIDDLE_CENTER, true));
		}
	}

	public class BattleEnd extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			broadCastPacketToBothTeams(new ExShowScreenMessage("Время битвы истекло. Телепортация через 1 минуту.", 4000, ScreenMessageAlign.BOTTOM_RIGHT, true));
			end();
		}
	}

	public class Finish extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			unParalyzePlayers();
			cleanUp();
		}
	}

	/**
	 * @param npcId
	 * @param loc
	 * @param randomOffset
	 * @param refId        Custom instanced spawn method
	 */
	@Override
	public NpcInstance addSpawnWithoutRespawn(int npcId, Location loc, int randomOffset)
	{
		NpcInstance npc = super.addSpawnWithoutRespawn(npcId, loc, randomOffset);
		npc.addListener(_deathListener);
		return npc;
	}

	/**
	 * Handles any Teleport action of any player inside
	 */
	private class TeleportListener implements OnTeleportListener
	{
		@Override
		public void onTeleport(Player player, int x, int y, int z, Reflection reflection)
		{
			if(zonepvp.checkIfInZone(x, y, z, reflection) || peace1.checkIfInZone(x, y, z, reflection) || peace2.checkIfInZone(x, y, z, reflection))
			{
				return;
			}

			removePlayer(player, false);
			player.sendMessage("Вы досрочно покинули зону битвы и были дисквалифицированы.");
		}
	}

	/**
	 * Handles quit from the group
	 */
	private class PlayerPartyLeaveListener implements OnPlayerPartyLeaveListener
	{
		@Override
		public void onPartyLeave(Player player)
		{
			if(!isActive())
			{
				return;
			}

			Party party = player.getParty();

			if(party.getMemberCount() >= 3) //when getMemberCount() >= 3 the party won't be dissolved.
			{
				removePlayer(player, false);
				return;
			}

			// else if getMemberCount() < 3 the party will be dissolved -> launching team withdrawal method
			teamWithdraw(party);
		}
	}
}
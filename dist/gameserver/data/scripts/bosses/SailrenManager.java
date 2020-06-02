package bosses;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bosses.EpicBossState.State;
import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.actor.listener.CharListenerList;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.SocialActionPacket;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.scripts.ScriptFile;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.ReflectionUtils;
import l2s.gameserver.utils.TimeUtils;

public class SailrenManager extends Functions implements ScriptFile, OnDeathListener
{
	private static final Logger _log = LoggerFactory.getLogger(SailrenManager.class);

	private static class ActivityTimeEnd extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			sleep();
		}
	}

	private static class CubeSpawn extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			_teleportCube = spawn(new Location(27734, -6838, -1982, 0), TeleportCubeId);
		}
	}

	private static class IntervalEnd extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			if(Config.ANNOUNCE_EPIC_BOSS_RESPAWN)
			{
				NpcTemplate template = NpcHolder.getInstance().getTemplate(Sailren);
				if(template != null)
					Announcements.getInstance().announceByCustomMessage("services.RaidBossHistory.epicboss.respawn", new String[]{ template.name });
			}

			_state.setState(EpicBossState.State.NOTSPAWN);
			_state.update();
		}
	}

	private static class Social extends RunnableImpl
	{
		private int _action;
		private NpcInstance _npc;

		public Social(NpcInstance npc, int actionId)
		{
			_npc = npc;
			_action = actionId;
		}

		@Override
		public void runImpl() throws Exception
		{
			_npc.broadcastPacket(new SocialActionPacket(_npc.getObjectId(), _action));
		}
	}

	private static class onAnnihilated extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			sleep();
		}
	}

	// Do spawn Valakas.
	private static class SailrenSpawn extends RunnableImpl
	{
		private int _npcId;
		private final Location _pos = new Location(27628, -6109, -1982, 44732);

		SailrenSpawn(int npcId)
		{
			_npcId = npcId;
		}

		@Override
		public void runImpl() throws Exception
		{
			if(_socialTask != null)
			{
				_socialTask.cancel(false);
				_socialTask = null;
			}

			switch(_npcId)
			{
				case Velociraptor:
					_velociraptor = spawn(new Location(27852, -5536, -1983, 44732), Velociraptor);
					((DefaultAI) _velociraptor.getAI()).addTaskMove(_pos, false, false);

					if(_socialTask != null)
					{
						_socialTask.cancel(false);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new Social(_velociraptor, 2), 6000);
					if(_activityTimeEndTask != null)
					{
						_activityTimeEndTask.cancel(false);
						_activityTimeEndTask = null;
					}
					_activityTimeEndTask = ThreadPoolManager.getInstance().schedule(new ActivityTimeEnd(), BossesConfig.SAILREN_ACTIVITY_MONSTERS_SPAWN_DELAY * 60000);
					break;
				case Pterosaur:
					_pterosaur = spawn(new Location(27852, -5536, -1983, 44732), Pterosaur);
					((DefaultAI) _pterosaur.getAI()).addTaskMove(_pos, false, false);
					if(_socialTask != null)
					{
						_socialTask.cancel(false);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new Social(_pterosaur, 2), 6000);
					if(_activityTimeEndTask != null)
					{
						_activityTimeEndTask.cancel(false);
						_activityTimeEndTask = null;
					}
					_activityTimeEndTask = ThreadPoolManager.getInstance().schedule(new ActivityTimeEnd(), BossesConfig.SAILREN_ACTIVITY_MONSTERS_SPAWN_DELAY * 60000);
					break;
				case Tyrannosaurus:
					_tyranno = spawn(new Location(27852, -5536, -1983, 44732), Tyrannosaurus);
					((DefaultAI) _tyranno.getAI()).addTaskMove(_pos, false, false);
					if(_socialTask != null)
					{
						_socialTask.cancel(false);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new Social(_tyranno, 2), 6000);
					if(_activityTimeEndTask != null)
					{
						_activityTimeEndTask.cancel(false);
						_activityTimeEndTask = null;
					}
					_activityTimeEndTask = ThreadPoolManager.getInstance().schedule(new ActivityTimeEnd(), BossesConfig.SAILREN_ACTIVITY_MONSTERS_SPAWN_DELAY * 60000);
					break;
				case Sailren:
					_sailren = spawn(new Location(27810, -5655, -1983, 44732), Sailren);

					_state.setNextRespawnDate(getRespawnTime() + BossesConfig.SAILREN_ACTIVITY_MONSTERS_SPAWN_DELAY * 60000);
					_state.setState(EpicBossState.State.ALIVE);
					_state.update();

					_sailren.setRunning();
					((DefaultAI) _sailren.getAI()).addTaskMove(_pos, false, false);
					if(_socialTask != null)
					{
						_socialTask.cancel(false);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().schedule(new Social(_sailren, 2), 6000);
					if(_activityTimeEndTask != null)
					{
						_activityTimeEndTask.cancel(false);
						_activityTimeEndTask = null;
					}
					_activityTimeEndTask = ThreadPoolManager.getInstance().schedule(new ActivityTimeEnd(), BossesConfig.SAILREN_ACTIVITY_MONSTERS_SPAWN_DELAY * 60000);
					break;
			}
		}
	}

	private static NpcInstance _velociraptor;
	private static NpcInstance _pterosaur;
	private static NpcInstance _tyranno;
	private static NpcInstance _sailren;
	private static NpcInstance _teleportCube;

	// Tasks.
	private static ScheduledFuture<?> _cubeSpawnTask = null;
	private static ScheduledFuture<?> _monsterSpawnTask = null;
	private static ScheduledFuture<?> _intervalEndTask = null;
	private static ScheduledFuture<?> _socialTask = null;
	private static ScheduledFuture<?> _activityTimeEndTask = null;
	private static ScheduledFuture<?> _onAnnihilatedTask = null;

	private static final int Sailren = 29065;
	private static final int Velociraptor = 22198;
	private static final int Pterosaur = 22199;
	private static final int Tyrannosaurus = 22217;
	private static final int TeleportCubeId = 31759;

	private static EpicBossState _state;
	private static Zone _zone;
	private static Location _enter = new Location(27734, -6938, -1982);

	private static boolean _isAlreadyEnteredOtherParty = false;

	private static boolean Dying = false;

	public static EpicBossState getState() {
		return _state;
	}

	private static void banishForeigners()
	{
		for(Player player : getPlayersInside())
			player.teleToClosestTown();
	}

	private synchronized static void checkAnnihilated()
	{
		if(_onAnnihilatedTask == null && isPlayersAnnihilated())
			_onAnnihilatedTask = ThreadPoolManager.getInstance().schedule(new onAnnihilated(), 5000);
	}

	private static List<Player> getPlayersInside()
	{
		return getZone().getInsidePlayers();
	}

	private static long getRespawnTime()
	{
		return BossesConfig.SAILREN_RESPAWN_TIME_PATTERN.next(System.currentTimeMillis());
	}

	public static Zone getZone()
	{
		return _zone;
	}

	private void init()
	{
		CharListenerList.addGlobal(this);

		_state = new EpicBossState(Sailren);
		_zone = ReflectionUtils.getZone("[sailren_epic]");

		_log.info("SailrenManager: State of Sailren is " + _state.getState() + ".");
		if(!_state.getState().equals(EpicBossState.State.NOTSPAWN))
			setIntervalEndTask();

		_log.info("SailrenManager: Next spawn date of Sailren is " + TimeUtils.toSimpleFormat(_state.getRespawnDate()) + ".");
	}

	private static boolean isPlayersAnnihilated()
	{
		for(Player pc : getPlayersInside())
			if(!pc.isDead())
				return false;
		return true;
	}

	@Override
	public void onDeath(Creature self, Creature killer)
	{
		if(self.isPlayer() && _state != null && _state.getState() == State.ALIVE && _zone != null && _zone.checkIfInZone(self.getX(), self.getY()))
			checkAnnihilated();
		else if(self == _velociraptor)
		{
			if(_monsterSpawnTask != null)
				_monsterSpawnTask.cancel(false);
			_monsterSpawnTask = ThreadPoolManager.getInstance().schedule(new SailrenSpawn(Pterosaur), BossesConfig.SAILREN_ACTIVITY_MONSTERS_RESPAWN_DELAY * 60000);
		}
		else if(self == _pterosaur)
		{
			if(_monsterSpawnTask != null)
				_monsterSpawnTask.cancel(false);
			_monsterSpawnTask = ThreadPoolManager.getInstance().schedule(new SailrenSpawn(Tyrannosaurus), BossesConfig.SAILREN_ACTIVITY_MONSTERS_RESPAWN_DELAY * 60000);
		}
		else if(self == _tyranno)
		{
			if(_monsterSpawnTask != null)
				_monsterSpawnTask.cancel(false);
			_monsterSpawnTask = ThreadPoolManager.getInstance().schedule(new SailrenSpawn(Sailren), BossesConfig.SAILREN_ACTIVITY_MONSTERS_RESPAWN_DELAY * 60000);
		}
		else if(self == _sailren)
			onSailrenDie(killer);
	}

	private static void onSailrenDie(Creature killer)
	{
		if(Dying)
			return;

		Dying = true;
		_state.setNextRespawnDate(getRespawnTime());
		_state.setState(EpicBossState.State.INTERVAL);
		_state.update();

		if(Config.ANNOUNCE_EPIC_BOSS_DIE)
			Announcements.getInstance().announceByCustomMessage("services.RaidBossHistory.epicboss.die", new String[]{ _sailren.getName() });

		Log.add("Sailren died", "bosses");

		_cubeSpawnTask = ThreadPoolManager.getInstance().schedule(new CubeSpawn(), 10000);
	}

	// Start interval.
	private static void setIntervalEndTask()
	{
		setUnspawn();

		if(_state.getState().equals(EpicBossState.State.ALIVE))
		{
			_state.setState(EpicBossState.State.NOTSPAWN);
			_state.update();
			return;
		}

		//init state of Sailren lair.
		if(!_state.getState().equals(EpicBossState.State.INTERVAL))
		{
			_state.setNextRespawnDate(getRespawnTime());
			_state.setState(EpicBossState.State.INTERVAL);
			_state.update();
		}

		_intervalEndTask = ThreadPoolManager.getInstance().schedule(new IntervalEnd(), _state.getInterval());
	}

	private static void setUnspawn()
	{
		banishForeigners();

		if(_velociraptor != null)
		{
			if(_velociraptor.getSpawn() != null)
				_velociraptor.getSpawn().stopRespawn();
			_velociraptor.deleteMe();
			_velociraptor = null;
		}
		if(_pterosaur != null)
		{
			if(_pterosaur.getSpawn() != null)
				_pterosaur.getSpawn().stopRespawn();
			_pterosaur.deleteMe();
			_pterosaur = null;
		}
		if(_tyranno != null)
		{
			if(_tyranno.getSpawn() != null)
				_tyranno.getSpawn().stopRespawn();
			_tyranno.deleteMe();
			_tyranno = null;
		}
		if(_sailren != null)
		{
			if(_sailren.getSpawn() != null)
				_sailren.getSpawn().stopRespawn();
			_sailren.deleteMe();
			_sailren = null;
		}
		if(_teleportCube != null)
		{
			if(_teleportCube.getSpawn() != null)
				_teleportCube.getSpawn().stopRespawn();
			_teleportCube.deleteMe();
			_teleportCube = null;
		}
		if(_cubeSpawnTask != null)
		{
			_cubeSpawnTask.cancel(false);
			_cubeSpawnTask = null;
		}
		if(_monsterSpawnTask != null)
		{
			_monsterSpawnTask.cancel(false);
			_monsterSpawnTask = null;
		}
		if(_intervalEndTask != null)
		{
			_intervalEndTask.cancel(false);
			_intervalEndTask = null;
		}
		if(_socialTask != null)
		{
			_socialTask.cancel(false);
			_socialTask = null;
		}
		if(_activityTimeEndTask != null)
		{
			_activityTimeEndTask.cancel(false);
			_activityTimeEndTask = null;
		}
		if(_onAnnihilatedTask != null)
		{
			_onAnnihilatedTask.cancel(false);
			_onAnnihilatedTask = null;
		}
	}

	private static void sleep()
	{
		setUnspawn();
		if(_state.getState().equals(EpicBossState.State.ALIVE))
		{
			_state.setState(EpicBossState.State.NOTSPAWN);
			_state.update();
		}
	}

	public synchronized static void setSailrenSpawnTask()
	{
		if(_monsterSpawnTask == null)
			_monsterSpawnTask = ThreadPoolManager.getInstance().schedule(new SailrenSpawn(Velociraptor), BossesConfig.SAILREN_ACTIVITY_MONSTERS_RESPAWN_DELAY * 60000);
	}

	public static boolean isEnableEnterToLair()
	{
		return _state.getState() == EpicBossState.State.NOTSPAWN;
	}

	public static int canIntoSailrenLair(Player pc)
	{
		if(!BossesConfig.SAILREN_SINGLE_ENTARANCE_AVAILABLE && pc.getParty() == null)
			return 4;
		else if(_isAlreadyEnteredOtherParty)
			return 2;
		else if(_state.getState().equals(EpicBossState.State.NOTSPAWN))
			return 0;
		else if(_state.getState().equals(EpicBossState.State.ALIVE) || _state.getState().equals(EpicBossState.State.DEAD))
			return 1;
		else if(_state.getState().equals(EpicBossState.State.INTERVAL))
			return 3;
		else
			return 0;
	}

	public static void entryToSailrenLair(Player pc)
	{
		if(pc.getParty() == null)
			pc.teleToLocation(Location.findPointToStay(_enter, 80, pc.getGeoIndex()));
		else
		{
			List<Player> members = new ArrayList<Player>();
			for(Player mem : pc.getParty().getPartyMembers())
				if(mem != null && !mem.isDead() && mem.isInRange(pc, 1000))
					members.add(mem);
			for(Player mem : members)
				mem.teleToLocation(Location.findPointToStay(_enter, 80, mem.getGeoIndex()));
		}
		_isAlreadyEnteredOtherParty = true;
	}

	@Override
	public void onLoad()
	{
		init();
	}

	@Override
	public void onReload()
	{
		sleep();
	}

	@Override
	public void onShutdown()
	{
	}
}
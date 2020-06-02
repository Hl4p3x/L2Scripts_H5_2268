package l2s.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.GameTimeController;
import l2s.gameserver.dao.SpawnsDAO;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.data.xml.holder.SpawnHolder;
import l2s.gameserver.listener.game.OnDayNightChangeListener;
import l2s.gameserver.listener.game.OnSSPeriodListener;
import l2s.gameserver.model.HardSpawner;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.entity.SevenSigns;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.templates.spawn.PeriodOfDay;
import l2s.gameserver.templates.spawn.SpawnTemplate;
import l2s.gameserver.utils.Location;

public class SpawnManager
{
	private class Listeners implements OnDayNightChangeListener, OnSSPeriodListener
	{
		@Override
		public void onDay()
		{
			despawn(PeriodOfDay.NIGHT.name());
			spawn(PeriodOfDay.DAY.name());
		}

		@Override
		public void onNight()
		{
			despawn(PeriodOfDay.DAY.name());
			spawn(PeriodOfDay.NIGHT.name());
		}

		@Override
		public void onPeriodChange(int mode)
		{
			switch(mode)
			{
				case 0: // all spawns
					despawn(DAWN_GROUP);
					despawn(DUSK_GROUP);
					spawn(DAWN_GROUP);
					spawn(DUSK_GROUP);
					break;
				case 1: // dusk spawns
					despawn(DAWN_GROUP);
					despawn(DUSK_GROUP);
					spawn(DUSK_GROUP);
					spawn(DUSK_VICTORY_GROUP);
					break;
				case 2: // dawn spawns
					despawn(DAWN_GROUP);
					despawn(DUSK_GROUP);
					spawn(DAWN_GROUP);
					spawn(DAWN_VICTORY_GROUP);
					break;
			}
		}
	}

	private static final Logger _log = LoggerFactory.getLogger(SpawnManager.class);

	private static SpawnManager _instance = new SpawnManager();

	private static final String DAWN_GROUP = "dawn_spawn";
	private static final String DUSK_GROUP = "dusk_spawn";
	private static final String DAWN_VICTORY_GROUP = "dawn_victory_spawn";
	private static final String DUSK_VICTORY_GROUP = "dusk_victory_spawn";

	private Map<String, List<Spawner>> _spawns = new ConcurrentHashMap<String, List<Spawner>>();
	private Listeners _listeners = new Listeners();

	private final Map<Integer, List<Location>> spawnLocationsByNpcId = new HashMap<Integer, List<Location>>();	
	
	public static SpawnManager getInstance()
	{
		return _instance;
	}

	private SpawnManager()
	{
		for(Map.Entry<String, List<SpawnTemplate>> entry : SpawnHolder.getInstance().getSpawns().entrySet())
			fillSpawn(entry.getKey(), entry.getValue());

		fillSpawn("NONE", SpawnsDAO.getInstance().restore());

		GameTimeController.getInstance().addListener(_listeners);
		SevenSigns.getInstance().addListener(_listeners);
	}

	private List<Spawner> fillSpawn(String group,  List<SpawnTemplate> templateList)
	{
		if(Config.DONTLOADSPAWN)
			return Collections.emptyList();

		List<Spawner> spawnerList = _spawns.get(group);
		if(spawnerList == null)
			_spawns.put(group, spawnerList = new ArrayList<Spawner>(templateList.size()));

		for(SpawnTemplate template : templateList)
		{
			HardSpawner spawner = new HardSpawner(template);
			spawnerList.add(spawner);

			NpcTemplate npcTemplate = NpcHolder.getInstance().getTemplate(spawner.getCurrentNpcId());

			int count = template.getCount();
			if(Config.RATE_MOB_SPAWN > 0 && npcTemplate.getInstanceClass() == MonsterInstance.class && npcTemplate.level >= Config.RATE_MOB_SPAWN_MIN_LEVEL && npcTemplate.level <= Config.RATE_MOB_SPAWN_MAX_LEVEL)
				count = (int) Math.max(1, count * Config.RATE_MOB_SPAWN);

			spawner.setAmount(count);
			spawner.setRespawnDelay(template.getRespawn(), template.getRespawnRandom());
			spawner.setRespawnPattern(template.getRespawnPattern());
			spawner.setReflection(ReflectionManager.DEFAULT);
			spawner.setRespawnTime(0);

			if(npcTemplate.isRaid && group.equals(PeriodOfDay.NONE.name()))
				RaidBossSpawnManager.getInstance().addNewSpawn(npcTemplate.getNpcId(), spawner);
		}

		return spawnerList;
	}

	public void spawnAll()
	{
		spawn(PeriodOfDay.NONE.name());
		if(Config.ALLOW_EVENT_GATEKEEPER)
			spawn("event_gatekeeper");
		if(!Config.ALLOW_CLASS_MASTERS_LIST.isEmpty())
			spawn("class_master");
	}

	public List<Spawner> spawn(String group, boolean logging)
	{
		List<Spawner> spawnerList = _spawns.get(group);
		if(spawnerList == null)
			return Collections.emptyList();

		int npcSpawnCount = 0;

		for(Spawner spawner : spawnerList)
		{
			npcSpawnCount += spawner.init();

			if(logging && npcSpawnCount % 1000 == 0 && npcSpawnCount != 0)
				_log.info("SpawnManager: spawned " + npcSpawnCount + " npc for group: " + group);
				
		}

		if(logging)
			_log.info("SpawnManager: spawned " + npcSpawnCount + " npc; spawns: " + spawnerList.size() + "; group: " + group);

		return spawnerList;
	}

	public List<Spawner> spawn(String group)
	{
		return spawn(group, true);
	}

	public void despawn(String group)
	{
		List<Spawner> spawnerList = _spawns.get(group);
		if(spawnerList == null)
			return;

		for(Spawner spawner : spawnerList)
			spawner.deleteAll();
	}

	public List<Spawner> getSpawners(String group)
	{
		List<Spawner> list = _spawns.get(group);
		return list == null ? Collections.<Spawner>emptyList() : list;
	}

	public List<NpcInstance> getAllSpawned(String group)
	{
		List<NpcInstance> result = new ArrayList<NpcInstance>();
		for(Spawner spawner : getSpawners(group))
			result.addAll(spawner.getAllSpawned());
		return result;
	}
	
	public List<Location> getRandomSpawnsByNpc(int npcId)
	{
		return spawnLocationsByNpcId.get(npcId);
	}	

	public void reloadAll()
	{
		RaidBossSpawnManager.getInstance().cleanUp();
		for(List<Spawner> spawnerList : _spawns.values())
			for(Spawner spawner : spawnerList)
				spawner.deleteAll();

		RaidBossSpawnManager.getInstance().reloadBosses();

		spawnAll();

		//FIXME [VISTALL] придумать другой способ
		int mode = 0;
		if(SevenSigns.getInstance().getCurrentPeriod() == SevenSigns.PERIOD_SEAL_VALIDATION)
			mode = SevenSigns.getInstance().getCabalHighestScore();

		_listeners.onPeriodChange(mode);

		if(GameTimeController.getInstance().isNowNight())
			_listeners.onNight();
		else
			_listeners.onDay();
	}
}
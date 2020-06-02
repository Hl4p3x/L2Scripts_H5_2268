package manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.util.Rnd;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.instancemanager.SpawnManager;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.instances.DoorInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.scripts.ScriptFile;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.NpcUtils;
import l2s.gameserver.utils.ReflectionUtils;
import npc.model.SepulcherMonsterInstance;
import npc.model.SepulcherRaidInstance;

public class FourSepulchersSpawn extends Functions implements ScriptFile
{
	public static class NpcLocation extends Location
	{
		public int npcId;

		public NpcLocation()
		{
			//
		}

		public NpcLocation(int x, int y, int z, int heading, int npcId)
		{
			super(x, y, z, heading);
			this.npcId = npcId;
		}
	}

	public static class GateKeeper extends Location
	{
		public final DoorInstance door;
		public final NpcTemplate template;

		public GateKeeper(int npcId, int x, int y, int z, int h, int doorId)
		{
			super(x, y, z, h);
			door = ReflectionUtils.getDoor(doorId);
			template = NpcHolder.getInstance().getTemplate(npcId);
			if(template == null)
				LOGGER.warn("FourGoblets::Sepulcher::RoomLock npc_template " + npcId + " undefined");
			if(door == null)
				LOGGER.warn("FourGoblets::Sepulcher::RoomLock door id " + doorId + " undefined");
		}
	}

	public static final Map<Integer, Location> START_HALL_SPAWNS = new HashMap<>();
	public static final Map<Integer, Boolean> HALL_IN_USE = new HashMap<>();
	public static final List<GateKeeper> GATE_KEEPERS = new ArrayList<>();
	public static final Map<Integer, Integer> VICTIM = new HashMap<>();
	public static final List<NpcInstance> ALL_MOBS = new ArrayList<>();
	public static final List<NpcInstance> MANAGERS = new ArrayList<>();

	private static final Logger LOGGER = LoggerFactory.getLogger(FourSepulchersSpawn.class);

	private static final Map<Integer, NpcLocation> SHADOW_SPAWNS = new HashMap<>();
	private static final Map<Integer, Integer> KEY_BOX_NPC = new HashMap<>();

	private static final Map<Integer, String> DUKE_MOB_GROUPS = new HashMap<>();
	private static final Map<Integer, String> VISCOUNT_MOB_GROUPS = new HashMap<>();

	private static final Set<String> SPAWNED_GROUPS = new HashSet<>();

	private static final Location[] START_HALL_SPAWN = {
			new Location(181632, -85587, -7218),
			new Location(179963, -88978, -7218),
			new Location(173217, -86132, -7218),
			new Location(175608, -82296, -7218)
	};

	private static final NpcLocation[][] SHADOW_SPAWN_LOC = {
		{
				// x, y, z, heading, npcId
				new NpcLocation(191231, -85574, -7216, 33380, 25339),
				new NpcLocation(189534, -88969, -7216, 32768, 25349),
				new NpcLocation(173195, -76560, -7215, 49277, 25346),
				new NpcLocation(175591, -72744, -7215, 49317, 25342)
		},
		{
				new NpcLocation(191231, -85574, -7216, 33380, 25342),
				new NpcLocation(189534, -88969, -7216, 32768, 25339),
				new NpcLocation(173195, -76560, -7215, 49277, 25349),
				new NpcLocation(175591, -72744, -7215, 49317, 25346)
		},
		{
				new NpcLocation(191231, -85574, -7216, 33380, 25346),
				new NpcLocation(189534, -88969, -7216, 32768, 25342),
				new NpcLocation(173195, -76560, -7215, 49277, 25339),
				new NpcLocation(175591, -72744, -7215, 49317, 25349)
		},
		{
				new NpcLocation(191231, -85574, -7216, 33380, 25349),
				new NpcLocation(189534, -88969, -7216, 32768, 25346),
				new NpcLocation(173195, -76560, -7215, 49277, 25342),
				new NpcLocation(175591, -72744, -7215, 49317, 25339)
		}
	};

	public static void init()
	{
		initFixedInfo();
		initLocationShadowSpawns();
		spawnManagers();
	}

	private static void initFixedInfo()
	{
		START_HALL_SPAWNS.put(31921, START_HALL_SPAWN[0]);
		START_HALL_SPAWNS.put(31922, START_HALL_SPAWN[1]);
		START_HALL_SPAWNS.put(31923, START_HALL_SPAWN[2]);
		START_HALL_SPAWNS.put(31924, START_HALL_SPAWN[3]);

		HALL_IN_USE.put(31921, false);
		HALL_IN_USE.put(31922, false);
		HALL_IN_USE.put(31923, false);
		HALL_IN_USE.put(31924, false);

		GATE_KEEPERS.add(new GateKeeper(31925, 182727, -85493, -7200, -32584, 25150012));
		GATE_KEEPERS.add(new GateKeeper(31926, 184547, -85479, -7200, -32584, 25150013));
		GATE_KEEPERS.add(new GateKeeper(31927, 186349, -85473, -7200, -32584, 25150014));
		GATE_KEEPERS.add(new GateKeeper(31928, 188154, -85463, -7200, -32584, 25150015));
		GATE_KEEPERS.add(new GateKeeper(31929, 189947, -85466, -7200, -32584, 25150016));

		GATE_KEEPERS.add(new GateKeeper(31930, 181030, -88868, -7200, -33272, 25150002));
		GATE_KEEPERS.add(new GateKeeper(31931, 182809, -88856, -7200, -33272, 25150003));
		GATE_KEEPERS.add(new GateKeeper(31932, 184626, -88859, -7200, -33272, 25150004));
		GATE_KEEPERS.add(new GateKeeper(31933, 186438, -88858, -7200, -33272, 25150005));
		GATE_KEEPERS.add(new GateKeeper(31934, 188236, -88854, -7200, -33272, 25150006));

		GATE_KEEPERS.add(new GateKeeper(31935, 173102, -85105, -7200, -16248, 25150032));
		GATE_KEEPERS.add(new GateKeeper(31936, 173101, -83280, -7200, -16248, 25150033));
		GATE_KEEPERS.add(new GateKeeper(31937, 173103, -81479, -7200, -16248, 25150034));
		GATE_KEEPERS.add(new GateKeeper(31938, 173086, -79698, -7200, -16248, 25150035));
		GATE_KEEPERS.add(new GateKeeper(31939, 173083, -77896, -7200, -16248, 25150036));

		GATE_KEEPERS.add(new GateKeeper(31940, 175497, -81265, -7200, -16248, 25150022));
		GATE_KEEPERS.add(new GateKeeper(31941, 175495, -79468, -7200, -16248, 25150023));
		GATE_KEEPERS.add(new GateKeeper(31942, 175488, -77652, -7200, -16248, 25150024));
		GATE_KEEPERS.add(new GateKeeper(31943, 175489, -75856, -7200, -16248, 25150025));
		GATE_KEEPERS.add(new GateKeeper(31944, 175478, -74049, -7200, -16248, 25150026));

		KEY_BOX_NPC.put(18120, 31455);
		KEY_BOX_NPC.put(18121, 31455);
		KEY_BOX_NPC.put(18122, 31455);
		KEY_BOX_NPC.put(18123, 31455);
		KEY_BOX_NPC.put(18124, 31456);
		KEY_BOX_NPC.put(18125, 31456);
		KEY_BOX_NPC.put(18126, 31456);
		KEY_BOX_NPC.put(18127, 31456);
		KEY_BOX_NPC.put(18128, 31457);
		KEY_BOX_NPC.put(18129, 31457);
		KEY_BOX_NPC.put(18130, 31457);
		KEY_BOX_NPC.put(18131, 31457);
		KEY_BOX_NPC.put(18149, 31458);
		KEY_BOX_NPC.put(18150, 31459);
		KEY_BOX_NPC.put(18151, 31459);
		KEY_BOX_NPC.put(18152, 31459);
		KEY_BOX_NPC.put(18153, 31459);
		KEY_BOX_NPC.put(18154, 31460);
		KEY_BOX_NPC.put(18155, 31460);
		KEY_BOX_NPC.put(18156, 31460);
		KEY_BOX_NPC.put(18157, 31460);
		KEY_BOX_NPC.put(18158, 31461);
		KEY_BOX_NPC.put(18159, 31461);
		KEY_BOX_NPC.put(18160, 31461);
		KEY_BOX_NPC.put(18161, 31461);
		KEY_BOX_NPC.put(18162, 31462);
		KEY_BOX_NPC.put(18163, 31462);
		KEY_BOX_NPC.put(18164, 31462);
		KEY_BOX_NPC.put(18165, 31462);
		KEY_BOX_NPC.put(18183, 31463);
		KEY_BOX_NPC.put(18184, 31464);
		KEY_BOX_NPC.put(18212, 31465);
		KEY_BOX_NPC.put(18213, 31465);
		KEY_BOX_NPC.put(18214, 31465);
		KEY_BOX_NPC.put(18215, 31465);
		KEY_BOX_NPC.put(18216, 31466);
		KEY_BOX_NPC.put(18217, 31466);
		KEY_BOX_NPC.put(18218, 31466);
		KEY_BOX_NPC.put(18219, 31466);

		VICTIM.put(18150, 18158);
		VICTIM.put(18151, 18159);
		VICTIM.put(18152, 18160);
		VICTIM.put(18153, 18161);
		VICTIM.put(18154, 18162);
		VICTIM.put(18155, 18163);
		VICTIM.put(18156, 18164);
		VICTIM.put(18157, 18165);
	}

	private static void initLocationShadowSpawns()
	{
		int locNo = Rnd.get(4);
		int[] gateKeeper = { 31929, 31934, 31939, 31944 };

		SHADOW_SPAWNS.clear();
		for(int i = 0; i <= 3; i++)
		{
			NpcLocation loc = new NpcLocation();
			loc.set(SHADOW_SPAWN_LOC[locNo][i]);
			loc.npcId = SHADOW_SPAWN_LOC[locNo][i].npcId;
			SHADOW_SPAWNS.put(gateKeeper[i], loc);
		}
	}

	private static void spawnManagers()
	{
		for(int i = 31921; i <= 31924; i++)
			try
			{
				Location loc = null;
				switch(i)
				{
					case 31921: // conquerors
						loc = new Location(181061, -85595, -7200, -32584);
						break;
					case 31922: // emperors
						loc = new Location(179292, -88981, -7200, -33272);
						break;
					case 31923: // sages
						loc = new Location(173202, -87004, -7200, -16248);
						break;
					case 31924: // judges
						loc = new Location(175606, -82853, -7200, -16248);
						break;
				}
				NpcInstance npc = NpcUtils.spawnSingle(i, loc);
				MANAGERS.add(npc);
				LOGGER.info("FourSepulchersManager: Spawned " + npc.getName());
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	}

	static void closeAllDoors()
	{
		for(GateKeeper gk : GATE_KEEPERS)
			try
			{
				gk.door.closeMe();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	}

	public static void deleteAllMobs()
	{
		for(String group : SPAWNED_GROUPS)
			SpawnManager.getInstance().despawn(group);

		SPAWNED_GROUPS.clear();

		for(NpcInstance mob : ALL_MOBS)
			mob.deleteMe();

		ALL_MOBS.clear();
	}

	public static void spawnShadow(int npcId)
	{
		if(!FourSepulchersManager.isAttackTime())
			return;

		NpcLocation loc = SHADOW_SPAWNS.get(npcId);
		if(loc == null)
			return;

		NpcInstance npc = NpcUtils.spawnSingle(loc.npcId, loc);
		if(npc instanceof SepulcherRaidInstance)
			((SepulcherRaidInstance) npc).mysteriousBoxId = npcId;
		ALL_MOBS.add(npc);
	}

	public static void locationShadowSpawns()
	{
		int locNo = Rnd.get(4);
		int[] gateKeeper = { 31929, 31934, 31939, 31944 };
		for(int i = 0; i <= 3; i++)
		{
			Location loc = SHADOW_SPAWNS.get(gateKeeper[i]);
			// Обновляем только координаты, не npcId.
			loc.x = SHADOW_SPAWN_LOC[locNo][i].x;
			loc.y = SHADOW_SPAWN_LOC[locNo][i].y;
			loc.z = SHADOW_SPAWN_LOC[locNo][i].z;
			loc.h = SHADOW_SPAWN_LOC[locNo][i].h;
		}
	}

	public static void spawnEmperorsGraveNpc(int npcId)
	{
		if(!FourSepulchersManager.isAttackTime())
			return;

		String group = String.format("4_sepul_emperor_npc_%d", npcId);
		if(SPAWNED_GROUPS.add(group))
			SpawnManager.getInstance().spawn(group, false);
	}

	public static void spawnArchonOfHalisha(int npcId)
	{
		if(!FourSepulchersManager.isAttackTime())
			return;

		String group = String.format("4_sepul_duke_final_monst_%d", npcId);
		if(!SPAWNED_GROUPS.add(group))
			return;

		List<Spawner> spawners = SpawnManager.getInstance().spawn(group, false);
		for(Spawner spawner : spawners) {
			for(NpcInstance npc : spawner.getAllSpawned()) {
				if(npc instanceof SepulcherMonsterInstance)
					((SepulcherMonsterInstance) npc).mysteriousBoxId = npcId;
			}
		}
	}

	public static void spawnExecutionerOfHalisha(NpcInstance npc)
	{
		if(!FourSepulchersManager.isAttackTime())
			return;

		ALL_MOBS.add(NpcUtils.spawnSingle(VICTIM.get(npc.getNpcId()), npc.getLoc()));
	}

	public static void spawnKeyBox(NpcInstance npc)
	{
		if(!FourSepulchersManager.isAttackTime())
			return;

		ALL_MOBS.add(NpcUtils.spawnSingle(KEY_BOX_NPC.get(npc.getNpcId()), npc.getLoc()));
	}

	public static void spawnMonster(int npcId)
	{
		if(!FourSepulchersManager.isAttackTime())
			return;

		String group;
		if(Rnd.get(2) == 0)
			group = String.format("4_sepul_phys_monst_%d", npcId);
		else
			group = String.format("4_sepul_magic_monst_%d", npcId);

		if(!SPAWNED_GROUPS.add(group))
			return;

		boolean keyBoxMonsterSpawned = false;

		List<Spawner> spawners = SpawnManager.getInstance().spawn(group, false);
		for(Spawner spawner : spawners) {
			for(NpcInstance npc : spawner.getAllSpawned()) {
				switch (npcId) {
					case 31469:
					case 31474:
					case 31479:
					case 31484:
						if(!keyBoxMonsterSpawned && Rnd.chance(2)) {
							NpcInstance keyBoxMonster = NpcUtils.spawnSingle(18149, npc.getSpawnedLoc(), npc.getReflection());
							npc.deleteMe();
							npc = keyBoxMonster;
							keyBoxMonsterSpawned = true;
						}
						break;
				}

				if(npc instanceof SepulcherMonsterInstance)
					((SepulcherMonsterInstance) npc).mysteriousBoxId = npcId;
			}
		}

		switch(npcId)
		{
			case 31469:
			case 31474:
			case 31479:
			case 31484:
				VISCOUNT_MOB_GROUPS.put(npcId, group);
				break;
			case 31472:
			case 31477:
			case 31482:
			case 31487:
				DUKE_MOB_GROUPS.put(npcId, group);
				break;
		}
	}

	public static void spawnMysteriousBox(int npcId)
	{
		if(!FourSepulchersManager.isAttackTime())
			return;

		String group = String.format("4_sepul_myst_box_%d", npcId);
		if(SPAWNED_GROUPS.add(group))
			SpawnManager.getInstance().spawn(group, false);
	}

	public static synchronized boolean isDukeMobsAnnihilated(int npcId)
	{
		String group = DUKE_MOB_GROUPS.get(npcId);
		if(group == null)
			return true;

		List<Spawner> spawners = SpawnManager.getInstance().getSpawners(group);
		for(Spawner spawner : spawners) {
			for(NpcInstance npc : spawner.getAllSpawned()) {
				if (!npc.isDead())
					return false;
			}
		}
		return true;
	}

	public static synchronized boolean isViscountMobsAnnihilated(int npcId)
	{
		String group = VISCOUNT_MOB_GROUPS.get(npcId);
		if(group == null)
			return true;

		List<Spawner> spawners = SpawnManager.getInstance().getSpawners(group);
		for(Spawner spawner : spawners) {
			for(NpcInstance npc : spawner.getAllSpawned()) {
				if (!npc.isDead())
					return false;
			}
		}
		return true;
	}

	public static boolean isShadowAlive(int id)
	{
		NpcLocation loc = SHADOW_SPAWNS.get(id);
		if(loc == null)
			return true;

		for(NpcInstance npc : ALL_MOBS) {
			if (npc.getNpcId() == loc.npcId && !npc.isDead())
				return true;
		}

		for(String group : SPAWNED_GROUPS) {
			List<Spawner> spawners = SpawnManager.getInstance().getSpawners(group);
			for(Spawner spawner : spawners) {
				for(NpcInstance npc : spawner.getAllSpawned()) {
					if (npc.getNpcId() == loc.npcId && !npc.isDead())
						return true;
				}
			}
		}
		return false;
	}

	@Override
	public void onLoad()
	{
		//
	}

	@Override
	public void onReload()
	{
		//
	}

	@Override
	public void onShutdown()
	{
		//
	}
}
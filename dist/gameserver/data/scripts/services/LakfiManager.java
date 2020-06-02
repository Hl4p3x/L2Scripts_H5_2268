package services;

import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.data.xml.holder.SpawnHolder;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.HardSpawner;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.scripts.ScriptFile;
import l2s.gameserver.templates.spawn.SpawnTemplate;

public class LakfiManager extends Functions implements ScriptFile
{		

	private List<SpawnTemplate> listLakfi1 = SpawnHolder.getInstance().getSpawn("lakkfi_1");
	private List<SpawnTemplate> listLakfi2 = SpawnHolder.getInstance().getSpawn("lakkfi_2");
	private List<SpawnTemplate> listLakfi3 = SpawnHolder.getInstance().getSpawn("lakkfi_3");
	private List<SpawnTemplate> listLakfi4 = SpawnHolder.getInstance().getSpawn("lakkfi_4");
	private List<SpawnTemplate> listLakfi5 = SpawnHolder.getInstance().getSpawn("lakkfi_5");
	private List<SpawnTemplate> listLakfi6 = SpawnHolder.getInstance().getSpawn("lakkfi_6");
	private List<SpawnTemplate> listLakfi7 = SpawnHolder.getInstance().getSpawn("lakkfi_7");
	private List<SpawnTemplate> listLakfi8 = SpawnHolder.getInstance().getSpawn("lakkfi_8");
	private List<SpawnTemplate> listLakfi9 = SpawnHolder.getInstance().getSpawn("lakkfi_9");
	
	private static int[] npc_ids = {18664, 18665, 18666}; 
	@Override
	public void onLoad()
	{
		initSpawns();
	}
	private void initSpawns()
	{
		SpawnTemplate lakfiSpawn1 = getRndSpawn(listLakfi1);
		SpawnTemplate lakfiSpawn2 = getRndSpawn(listLakfi2);
		SpawnTemplate lakfiSpawn3 = getRndSpawn(listLakfi3);
		SpawnTemplate lakfiSpawn4 = getRndSpawn(listLakfi4);
		SpawnTemplate lakfiSpawn5 = getRndSpawn(listLakfi5);
		SpawnTemplate lakfiSpawn6 = getRndSpawn(listLakfi6);
		SpawnTemplate lakfiSpawn7 = getRndSpawn(listLakfi7);
		SpawnTemplate lakfiSpawn8 = getRndSpawn(listLakfi8);
		SpawnTemplate lakfiSpawn9 = getRndSpawn(listLakfi9);
		
		initSpawnForGrp(lakfiSpawn1);
		initSpawnForGrp(lakfiSpawn2);
		initSpawnForGrp(lakfiSpawn3);
		initSpawnForGrp(lakfiSpawn4);
		initSpawnForGrp(lakfiSpawn5);
		initSpawnForGrp(lakfiSpawn6);
		initSpawnForGrp(lakfiSpawn7);
		initSpawnForGrp(lakfiSpawn8);
		initSpawnForGrp(lakfiSpawn9);
		
		executeTask("services.LakfiManager", "DespawnTask", new Object[0], 1200000);
	}
	
	public void DespawnTask()
	{
		for(NpcInstance npc : GameObjectsStorage.getAllByNpcId(npc_ids, true))
		{
			if(npc != null)
				npc.deleteMe();
		}
		initSpawns();
	}
	
	private void initSpawnForGrp(SpawnTemplate template)
	{
		HardSpawner spawner = new HardSpawner(template);

		spawner.setAmount(1);
		spawner.setRespawnDelay(0, 0);
		spawner.setReflection(ReflectionManager.DEFAULT);
		spawner.setRespawnTime(0);	
		spawner.doSpawn(true);		
	}
	
	private SpawnTemplate getRndSpawn(List<SpawnTemplate> list)
	{
		if(list == null || list.isEmpty())
			return null;
		int index = Rnd.get(0, list.size() - 1);	
		return list.get(index);
	}
	
	@Override
	public void onReload()
	{
	}

	@Override
	public void onShutdown()
	{
	}
}
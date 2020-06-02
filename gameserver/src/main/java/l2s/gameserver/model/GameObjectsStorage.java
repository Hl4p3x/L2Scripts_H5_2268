package l2s.gameserver.model;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.text.StrTable;
import l2s.gameserver.Config;
import l2s.gameserver.GameServer;
import l2s.gameserver.model.instances.FenceInstance;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.items.ItemInstance;

/**
 * Общее "супер" хранилище для всех объектов L2Object,
 * объекты делятся на типы - максимум 32 (0 - 31), для каждого типа свое хранилище,
 * в каждом хранилище может хранится до 67 108 864 объектов (0 - 67108863),
 * каждому объекту назначается уникальный 64-битный индентификатор типа long
 * который бинарно складывается из objId + тип + индекс в хранилище
 * 
 * @author Drin & Diamond
 * 
 */
//TODO [G1ta0] вынести эту бредятину к чертям
@SuppressWarnings("rawtypes")
public class GameObjectsStorage
{
	@SuppressWarnings("unused")
	private static final Logger _log = LoggerFactory.getLogger(GameObjectsStorage.class);

	private static final int STORAGE_PLAYERS = 0x00;
	private static final int STORAGE_SUMMONS = 0x01;
	private static final int STORAGE_NPCS = 0x02;
	private static final int STORAGE_FENCES = 0x03;
	/** ......................................... */
	private static final int STORAGE_OTHER = 0x1E;
	private static final int STORAGE_NONE = 0x1F;

	private static final GameObjectArray[] storages = new GameObjectArray[STORAGE_NONE];

	static
	{
		storages[STORAGE_PLAYERS] = new GameObjectArray<Player>("PLAYERS", GameServer.getInstance().getOnlineLimit() + Config.FAKE_PLAYERS_COUNT, 1);
		storages[STORAGE_SUMMONS] = new GameObjectArray<Playable>("SUMMONS", GameServer.getInstance().getOnlineLimit(), 1);
		storages[STORAGE_NPCS] = new GameObjectArray<NpcInstance>("NPCS", (int) (60000 * Config.RATE_MOB_SPAWN), 5000);
		storages[STORAGE_FENCES] = new GameObjectArray<NpcInstance>("FENCES", 1000, 500);
		storages[STORAGE_OTHER] = new GameObjectArray<GameObject>("OTHER", 2000, 1000);
	}

	@SuppressWarnings("unchecked")
	private static GameObjectArray<Player> getStoragePlayers()
	{
		return storages[STORAGE_PLAYERS];
	}

	@SuppressWarnings( { "unchecked", "unused" })
	private static GameObjectArray<Playable> getStorageSummons()
	{
		return storages[STORAGE_SUMMONS];
	}

	@SuppressWarnings("unchecked")
	private static GameObjectArray<NpcInstance> getStorageNpcs()
	{
		return storages[STORAGE_NPCS];
	}

	@SuppressWarnings("unchecked")
	private static GameObjectArray<FenceInstance> getStorageFences()
	{
		return storages[STORAGE_FENCES];
	}
	
	private static int selectStorageID(GameObject o)
	{
		if(o.isFence())
			return STORAGE_FENCES;

		if(o.isNpc())
			return STORAGE_NPCS;

		if(o.isPlayable())
			return o.isPlayer() ? STORAGE_PLAYERS : STORAGE_SUMMONS;

		return STORAGE_OTHER;
	}

	public static GameObject get(long storedId)
	{
		int STORAGE_ID;
		if(storedId == 0 || (STORAGE_ID = getStorageID(storedId)) == STORAGE_NONE)
			return null;
		GameObject result = storages[STORAGE_ID].get(getStoredIndex(storedId));
		return result != null && result.getObjectId() == getStoredObjectId(storedId) ? result : null;
	}

	public static GameObject get(Long storedId)
	{
		int STORAGE_ID;
		if(storedId == null || storedId == 0 || (STORAGE_ID = getStorageID(storedId)) == STORAGE_NONE)
			return null;
		GameObject result = storages[STORAGE_ID].get(getStoredIndex(storedId));
		return result != null && result.getObjectId() == getStoredObjectId(storedId) ? result : null;
	}

	public static boolean isStored(long storedId)
	{
		int STORAGE_ID;
		if(storedId == 0 || (STORAGE_ID = getStorageID(storedId)) == STORAGE_NONE)
			return false;
		GameObject o = storages[STORAGE_ID].get(getStoredIndex(storedId));
		return o != null && o.getObjectId() == getStoredObjectId(storedId);
	}

	public static NpcInstance getAsNpc(long storedId)
	{
		return (NpcInstance) get(storedId);
	}

	public static NpcInstance getAsNpc(Long storedId)
	{
		return (NpcInstance) get(storedId);
	}

	public static Player getAsPlayer(long storedId)
	{
		return (Player) get(storedId);
	}

	public static Playable getAsPlayable(long storedId)
	{
		return (Playable) get(storedId);
	}

	public static Creature getAsCharacter(long storedId)
	{
		return (Creature) get(storedId);
	}

	public static MonsterInstance getAsMonster(long storedId)
	{
		return (MonsterInstance) get(storedId);
	}

	public static PetInstance getAsPet(long storedId)
	{
		return (PetInstance) get(storedId);
	}

	public static ItemInstance getAsItem(long storedId)
	{
		return (ItemInstance) get(storedId);
	}

	public static boolean contains(long storedId)
	{
		return get(storedId) != null;
	}

	public static Player getPlayer(String name)
	{
		return getStoragePlayers().findByName(name);
	}

	public static Player getPlayer(int objId)
	{
		return getStoragePlayers().findByObjectId(objId);
	}

	/**
	 * копия списка игроков из хранилища, пригодна для последующих манипуляций над ней
	 * для перебора в основном лучше использовать getAllPlayersForIterate()
	 */
	public static List<Player> getAllPlayers()
	{
		return getStoragePlayers().getAll();
	}

	/**
	 * использовать только для перебора типа for(L2Player player : getAllPlayersForIterate()) ...
	 */
	public static Iterable<Player> getAllPlayersForIterate()
	{
		return getStoragePlayers();
	}

	/**
	 * Возвращает онлайн с офф трейдерами
	 */
	public static int getAllPlayersCount()
	{
		return getStoragePlayers().getRealSize();
	}

	public static int getAllObjectsCount()
	{
		int result = 0;
		for(GameObjectArray<?> storage : storages)
			if(storage != null)
				result += storage.getRealSize();
		return result;
	}
	
	public static int getAllInTradeCount()
	{
		int i = 0;
		for(Player player : getAllPlayersForIterate())
		{
			if(player == null || player.isInOfflineMode() || !player.isInStoreMode())
				continue;
			i++;	
		}
		return i;
	}
	
	public static int getWithoutSameIPCount()
	{
		int i = 0;
		List<String> ips = new ArrayList<String>();
		
		for(Player player : getAllPlayersForIterate())
		{
			if(player == null)
				continue;
			if(ips.contains(player.getIP()))
				continue;
			ips.add(player.getIP());
			i++;	
		}
		return i;
	}
	
	public static int getWithoutSameHWIDCount()
	{
		int i = 0;
		List<String> hwid = new ArrayList<String>();
		
		for(Player player : getAllPlayersForIterate())
		{
			if(player == null || player.getNetConnection() == null)
				continue;
			if(hwid.contains(player.getHWID()))
				continue;
			hwid.add(player.getHWID());
			i++;	
		}
		return i;
	}
	
	public static int getSameHWIDsWithOutTrade()
	{
		int i = 0;
		List<String> hwid = new ArrayList<String>();
		
		for(Player player : getAllPlayersForIterate())
		{
			if(player == null || player.isInOfflineMode() || !player.isInStoreMode() || player.getNetConnection() == null)
				continue;
			if(hwid.contains(player.getHWID()))
			{
				i++;
				continue;
			}	
			hwid.add(player.getHWID());	
		}
		return i;		
	}
	
	public static int getClearOnlineCount()
	{
		return getAllPlayersCount() - getAllInTradeCount() - getAllOfflineCount() - getSameHWIDsWithOutTrade();
	}
	
	@SuppressWarnings( { "unchecked" })
	public static List<GameObject> getAllObjects()
	{
		List<GameObject> result = new ArrayList<GameObject>(getAllObjectsCount());
		for(GameObjectArray storage : storages)
			if(storage != null)
				storage.getAll(result);
		return result;
	}

	public static GameObject findObject(int objId)
	{
		GameObject result = null;
		for(GameObjectArray<?> storage : storages)
			if(storage != null)
				if((result = storage.findByObjectId(objId)) != null)
					return result;
		return null;
	}

	private static long offline_refresh = 0;
	private static int offline_count = 0;

	public static int getAllOfflineCount()
	{
		if(!Config.SERVICES_OFFLINE_TRADE_ALLOW)
			return 0;

		long now = System.currentTimeMillis();
		if(now > offline_refresh)
		{
			offline_refresh = now + 10000;
			offline_count = 0;
			for(Player player : getStoragePlayers())
				if(player.isInOfflineMode())
					offline_count++;
		}

		return offline_count;
	}

	public static List<NpcInstance> getAllNpcs()
	{
		return getStorageNpcs().getAll();
	}

	/**
	 * использовать только для перебора типа for(L2Player player : getAllPlayersForIterate()) ...
	 */
	public static Iterable<NpcInstance> getAllNpcsForIterate()
	{
		return getStorageNpcs();
	}

	public static NpcInstance getByNpcId(int npc_id)
	{
		NpcInstance result = null;
		for(NpcInstance temp : getStorageNpcs())
			if(npc_id == temp.getNpcId())
			{
				if(!temp.isDead())
					return temp;
				result = temp;
			}
		return result;
	}

	
	public static List<NpcInstance> getAllByNpcId(int npc_id, boolean justAlive)
	{
		return getAllByNpcId(npc_id, justAlive, false);
	}
  
	public static List<NpcInstance> getAllByNpcId(int npc_id, boolean justAlive, boolean visible)
	{
		List<NpcInstance> result = new ArrayList<NpcInstance>();
		for(NpcInstance temp : getStorageNpcs())
		{
			if(temp.getTemplate() != null && npc_id == temp.getTemplate().getNpcId() && (!justAlive || !temp.isDead()) && (!visible || temp.isVisible()))
				result.add(temp);
		}		
		return result;
	}
  
	public static List<NpcInstance> getAllByNpcId(int[] npc_ids, boolean justAlive)
	{
		List<NpcInstance> result = new ArrayList<NpcInstance>();
		for(NpcInstance temp : getStorageNpcs())
		{
			if(!justAlive || !temp.isDead())
			{
				for(int npc_id : npc_ids)
				{
					if(npc_id == temp.getNpcId())
						result.add(temp);
				}		
			}			
		}				
		return result;
	}

	public static NpcInstance getNpc(String s)
	{
		List<NpcInstance> npcs = getStorageNpcs().findAllByName(s);
		if(npcs.size() == 0)
			return null;
		for(NpcInstance temp : npcs)
			if(!temp.isDead())
				return temp;
		if(npcs.size() > 0)
			return npcs.remove(npcs.size() - 1);

		return null;
	}

	public static NpcInstance getNpc(int objId)
	{
		return getStorageNpcs().findByObjectId(objId);
	}

	public static List<FenceInstance> getFences()
	{
		return getStorageFences().getAll();
	}

	public static FenceInstance getFence(int objectId)
	{
		for(FenceInstance fence : getFences())
		{
			if(fence.getObjectId() == objectId)
				return fence;
		}
		return null;
	}

	/**
	 * кладет объект в хранилище и возвращает уникальный индентификатор по которому его можно будет найти в хранилище
	 */
	@SuppressWarnings("unchecked")
	public static long put(GameObject o)
	{
		int STORAGE_ID = selectStorageID(o);
		return o.getObjectId() & 0xFFFFFFFFL | (STORAGE_ID & 0x1FL) << 32 | (storages[STORAGE_ID].add(o) & 0xFFFFFFFFL) << 37;
	}

	public static long putDummy(GameObject o)
	{
		return objIdNoStore(o.getObjectId());
	}

	/**
	 * генерирует уникальный индентификатор по которому будет ясно что объект вне хранилища но можно будет получить objectId
	 */
	public static long objIdNoStore(int objId)
	{
		return objId & 0xFFFFFFFFL | (STORAGE_NONE & 0x1FL) << 32;
	}

	/**
	 * пересчитывает StoredId, необходимо при изменении ObjectId
	 */
	public static long refreshId(Creature o)
	{
		return o.getObjectId() & 0xFFFFFFFFL | o.getStoredId() >> 32 << 32;
	}

	public static GameObject remove(long storedId)
	{
		int STORAGE_ID = getStorageID(storedId);
		return STORAGE_ID == STORAGE_NONE ? null : storages[STORAGE_ID].remove(getStoredIndex(storedId), getStoredObjectId(storedId));
	}

	private static int getStorageID(long storedId)
	{
		return (int) (storedId >> 32) & 0x1F;
	}

	private static int getStoredIndex(long storedId)
	{
		return (int) (storedId >> 37);
	}

	public static int getStoredObjectId(long storedId)
	{
		return (int) storedId;
	}

	public static StrTable getStats()
	{
		StrTable table = new StrTable("L2 Objects Storage Stats");

		GameObjectArray<?> storage;
		for(int i = 0; i < storages.length; i++)
		{
			if((storage = storages[i]) == null)
				continue;

			synchronized (storage)
			{
				table.set(i, "Name", storage.name);
				table.set(i, "Size / Real", storage.size() + " / " + storage.getRealSize());
				table.set(i, "Capacity / init", storage.capacity() + " / " + storage.initCapacity);
			}
		}

		return table;
	}
}
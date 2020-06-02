package l2s.gameserver.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.collections.LazyArrayList;
import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * Аггролист NPC.
 * 
 * @author G1ta0
 */
public class AggroList
{
	private abstract class DamageHate
	{
		public int hate;
		public int damage;
	}

	public class HateInfo extends DamageHate
	{
		public final Creature attacker;

		HateInfo(Creature attacker, AggroInfo ai)
		{
			this.attacker = attacker;
			this.hate = ai.hate;
			this.damage = ai.damage;
		}
	}

	public class AggroInfo extends DamageHate
	{
		public final int attackerId;

		AggroInfo(Creature attacker)
		{
			this.attackerId = attacker.getObjectId();
		}
	}

	public static class DamageComparator implements Comparator<DamageHate>
	{
		private static Comparator<DamageHate> instance = new DamageComparator();

		public static Comparator<DamageHate> getInstance()
		{
			return instance;
		}

		DamageComparator()
		{}

		@Override
		public int compare(DamageHate o1, DamageHate o2)
		{
			if(o1 == null || o2 == null)
				return 0;
			if(o1 == o2)
				return 0;
			return Integer.compare(o2.damage, o1.damage);
		}
	}

	public static class HateComparator implements Comparator<DamageHate>
	{
		private static Comparator<DamageHate> instance = new HateComparator();

		public static Comparator<DamageHate> getInstance()
		{
			return instance;
		}

		HateComparator()
		{}

		@Override
		public int compare(DamageHate o1, DamageHate o2)
		{
			if(o1 == null || o2 == null)
				return 0;
			if(o1 == o2)
				return 0;
			if(o1.hate == o2.hate)
				return Integer.compare(o2.damage, o1.damage);
			return Integer.compare(o2.hate, o1.hate);
		}
	}

	private final NpcInstance npc;
	private final TIntObjectHashMap<AggroInfo> hateList = new TIntObjectHashMap<AggroInfo>();
	/** Блокировка для чтения/записи объектов списка */
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();

	public AggroList(NpcInstance npc)
	{
		this.npc = npc;
	}

	public void addDamageHate(Creature attacker, int damage, int aggro)
	{
		damage = Math.max(damage, 0);

		if(damage == 0 && aggro == 0)
			return;

		writeLock.lock();
		try
		{
			AggroInfo ai;

			if((ai = hateList.get(attacker.getObjectId())) == null)
				hateList.put(attacker.getObjectId(), ai = new AggroInfo(attacker));

			ai.damage += damage;
			ai.hate += aggro;
			ai.damage = Math.max(ai.damage, 0);
			ai.hate = Math.max(ai.hate, 0);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public void reduceHate(Creature target, int hate)
	{
		writeLock.lock();
		try
		{
			AggroInfo ai = hateList.get(target.getObjectId());
			if(ai != null)
			{
				ai.hate -= hate;
				ai.hate = Math.max(ai.hate, 0);
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public int getHate(Creature target)
	{
		int hate = 0;

		writeLock.lock();
		try
		{
			AggroInfo ai = hateList.get(target.getObjectId());
			if(ai != null)
				hate = ai.hate;
		}
		finally
		{
			writeLock.unlock();
		}
		return hate;
	}

	public AggroInfo get(Creature attacker)
	{
		readLock.lock();
		try
		{
			return hateList.get(attacker.getObjectId());
		}
		finally
		{
			readLock.unlock();
		}
	}

	private void remove(int objectId, boolean onlyHate)
	{
		writeLock.lock();
		try
		{
			if(!onlyHate)
			{
				hateList.remove(objectId);
				return;
			}

			AggroInfo ai = hateList.get(objectId);
			if(ai != null)
			{
				if(ai.damage == 0)
					hateList.remove(objectId);
				else
					ai.hate = 0;
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public void remove(Creature attacker, boolean onlyHate)
	{
		remove(attacker.getObjectId(), onlyHate);
	}

	public void clear()
	{
		clear(false);
	}

	public void clear(boolean onlyHate)
	{
		writeLock.lock();
		try
		{
			if(hateList.isEmpty())
				return;

			if(!onlyHate)
			{
				hateList.clear();
				return;
			}

			AggroInfo ai;
			for(TIntObjectIterator<AggroInfo> itr = hateList.iterator(); itr.hasNext();)
			{
				itr.advance();
				ai = itr.value();
				ai.hate = 0;
				if(ai.damage == 0)
					itr.remove();
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public boolean isEmpty()
	{
		readLock.lock();
		try
		{
			return hateList.isEmpty();
		}
		finally
		{
			readLock.unlock();
		}
	}

	private Creature getOrRemoveHated(int objectId)
	{
		GameObject object = GameObjectsStorage.findObject(objectId);
		if(object == null || !object.isCreature())
		{
			remove(objectId, true);
			return null;
		}

		Creature cha = (Creature) object;
		if(cha.isPlayable() && ((Playable) cha).isInNonAggroTime())
		{
			remove(objectId, true);
			return null;
		}

		if(cha.isPlayer() && !((Player) cha).isOnline())
		{
			remove(objectId, true);
			return null;
		}
		return cha;
	}

	public List<Creature> getHateList(int radius)
	{
		AggroInfo[] hated;

		readLock.lock();
		try
		{
			if(hateList.isEmpty())
				return Collections.emptyList();

			hated = hateList.values(new AggroInfo[hateList.size()]);
		}
		finally
		{
			readLock.unlock();
		}

		try
		{
			Arrays.sort(hated, HateComparator.getInstance());
		}
		catch(Exception e)
		{
			// Заглушка против глюка явы: Comparison method violates its general contract!
		}

		if(hated[0].hate == 0)
			return Collections.emptyList();

		List<Creature> hateList = new LazyArrayList<Creature>();
		AggroInfo ai;
		for(int i = 0; i < hated.length; i++)
		{
			ai = hated[i];
			if(ai.hate == 0)
				continue;

			Creature cha = getOrRemoveHated(ai.attackerId);
			if(cha == null)
				continue;

			if(radius == -1 || cha.isInRangeZ(npc.getLoc(), radius))
			{
				hateList.add(cha);
				break;
			}
		}

		return hateList;
	}

	public Creature getMostHated(int radius)
	{
		AggroInfo[] hated;

		readLock.lock();
		try
		{
			if(hateList.isEmpty())
				return null;

			hated = hateList.values(new AggroInfo[hateList.size()]);
		}
		finally
		{
			readLock.unlock();
		}

		try
		{
			Arrays.sort(hated, HateComparator.getInstance());
		}
		catch(Exception e)
		{
			// Заглушка против глюка явы: Comparison method violates its general contract!
		}

		if(hated[0].hate == 0)
			return null;

		AggroInfo ai;
		for(int i = 0; i < hated.length; i++)
		{
			ai = hated[i];
			if(ai.hate == 0)
				continue;

			Creature cha = getOrRemoveHated(ai.attackerId);
			if(cha == null)
				continue;

			if(radius == -1 || cha.isInRangeZ(npc.getLoc(), radius))
			{
				if(cha.isDead())
					continue;
				return cha;
			}
		}

		return null;
	}

	public Creature getRandomHated(int radius)
	{
		AggroInfo[] hated;

		readLock.lock();
		try
		{
			if(hateList.isEmpty())
				return null;

			hated = hateList.values(new AggroInfo[hateList.size()]);
		}
		finally
		{
			readLock.unlock();
		}

		try
		{
			Arrays.sort(hated, HateComparator.getInstance());
		}
		catch(Exception e)
		{
			// Заглушка против глюка явы: Comparison method violates its general contract!
		}

		if(hated[0].hate == 0)
			return null;

		LazyArrayList<Creature> randomHated = LazyArrayList.newInstance();

		AggroInfo ai;
		Creature mostHated;
		for(int i = 0; i < hated.length; i++)
		{
			ai = hated[i];
			if(ai.hate == 0)
				continue;

			Creature cha = getOrRemoveHated(ai.attackerId);
			if(cha == null)
				continue;

			if(radius == -1 || cha.isInRangeZ(npc.getLoc(), radius))
			{
				if(cha.isDead())
					continue;
				randomHated.add(cha);
				break;
			}
		}

		if(randomHated.isEmpty())
			mostHated = null;
		else
			mostHated = randomHated.get(Rnd.get(randomHated.size()));

		LazyArrayList.recycle(randomHated);

		return mostHated;
	}

	public Creature getTopDamager(Creature defaultDamager)
	{
		AggroInfo[] hated;

		readLock.lock();
		try
		{
			if(hateList.isEmpty())
				return defaultDamager;

			hated = hateList.values(new AggroInfo[hateList.size()]);
		}
		finally
		{
			readLock.unlock();
		}

		try
		{
			Arrays.sort(hated, DamageComparator.getInstance());
		}
		catch(Exception e)
		{
			// Заглушка против глюка явы: Comparison method violates its general contract!
		}

		if(hated[0].damage == 0)
			return null;

		List<Creature> chars = World.getAroundCharacters(npc);
		AggroInfo ai;
		for(int i = 0; i < hated.length; i++)
		{
			ai = hated[i];
			if(ai.damage == 0)
				continue;
			for(Creature cha : chars)
			{
				if(cha.getObjectId() == ai.attackerId)
				{
					return cha;
				}
			}
		}
		return defaultDamager;
	}

	public Map<Creature, HateInfo> getCharMap()
	{
		if(isEmpty())
			return Collections.emptyMap();

		Map<Creature, HateInfo> aggroMap = new HashMap<Creature, HateInfo>();
		List<Creature> chars = World.getAroundCharacters(npc);
		readLock.lock();
		try
		{
			AggroInfo ai;
			for(TIntObjectIterator<AggroInfo> itr = hateList.iterator(); itr.hasNext();)
			{
				itr.advance();
				ai = itr.value();
				if(ai.damage == 0 && ai.hate == 0)
					continue;
				for(Creature attacker : chars)
				{
					if(attacker.getObjectId() == ai.attackerId)
					{
						aggroMap.put(attacker, new HateInfo(attacker, ai));
						break;
					}
				}
			}
		}
		finally
		{
			readLock.unlock();
		}

		return aggroMap;
	}

	public Map<Playable, HateInfo> getPlayableMap()
	{
		if(isEmpty())
			return Collections.emptyMap();

		Map<Playable, HateInfo> aggroMap = new HashMap<Playable, HateInfo>();
		List<Playable> chars = World.getAroundPlayables(npc);
		readLock.lock();
		try
		{
			AggroInfo ai;
			for(TIntObjectIterator<AggroInfo> itr = hateList.iterator(); itr.hasNext();)
			{
				itr.advance();
				ai = itr.value();
				if(ai.damage == 0 && ai.hate == 0)
					continue;
				for(Playable attacker : chars)
				{
					if(attacker.getObjectId() == ai.attackerId)
					{
						aggroMap.put(attacker, new HateInfo(attacker, ai));
						break;
					}
				}
			}
		}
		finally
		{
			readLock.unlock();
		}

		return aggroMap;
	}
}

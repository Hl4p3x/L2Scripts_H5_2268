package ai;

import l2s.gameserver.ai.Fighter;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.SimpleSpawner;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.Location;

/**
 * @author Byldas
 * @date 25.09.11  
 * @AI for Mobs Furnace is MOS
 */

public class Furnace extends Fighter
{
	private static final long NextAtack = 10 * 1000; //  10 seconds supposedly TODO
	private long _lastAttackTime = 0;

	private static int[] Magic_Power = { 22800, 22800, 22800, 22800, 22800,
										22800, 22800, 22800, 22800, 22800,
										22800, 22800, 22800, 22800, 22798, 
										22798, 22799, 22799};
	private static int[] Protection = { 22798, 22798, 22798, 22798, 22798,
										22798, 22798, 22798, 22798, 22798,
										22798, 22798, 22798, 22798, 22800,
										22800, 22799, 22799};
	private static int[] Fighting_Spirit = { 22799, 22799, 22799, 22799, 22799, 
											22799, 22799, 22799, 22799, 22799, 
											22799, 22799, 22799, 22799, 22800,
											22800, 22798, 22798 };
	private static int[] Balance = { 22800, 22800, 22800, 22800, 22800, 22800,
										22798, 22798, 22798, 22798, 22798, 22798,
										22799, 22799, 22799, 22799, 22799,22799, };
	
	public Furnace(NpcInstance actor)
	{
		super(actor);
		actor.startImmobilized();
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		if(_lastAttackTime + NextAtack < System.currentTimeMillis())
		{
			NpcInstance actor = getActor();
			if (actor.getTitle() == "Furnace of Magic Power")
			{
				changestate(actor, 1);
				unSpawnMob();
				spawnMob(Magic_Power);
				_lastAttackTime = System.currentTimeMillis();
			}
			else if (actor.getTitle() == "Furnace of Fighting Spirit")
			{
				changestate(actor, 1);
				unSpawnMob();
				spawnMob(Fighting_Spirit);
				_lastAttackTime = System.currentTimeMillis();
			}
			else if (actor.getTitle() == "Furnace of Protection")
			{
				changestate(actor, 1);
				unSpawnMob();
				spawnMob(Protection);
				_lastAttackTime = System.currentTimeMillis();
			}
			else if (actor.getTitle() == "Furnace of Balance")
			{
				changestate(actor, 1);
				unSpawnMob();
				spawnMob(Balance);
				_lastAttackTime = System.currentTimeMillis();
			}
			else
			{
			
			}
			
		}
		super.onEvtAttacked(attacker, damage);
	}

	void spawnMob(int[] Mob)
	{
		for (int npcId : Mob)
		{
			NpcInstance actor = getActor();
			SimpleSpawner spawn;
			try 
			{
				spawn = new SimpleSpawner(NpcHolder.getInstance().getTemplate(npcId));
				spawn.setLoc(Location.coordsRandomize(actor.getLoc(), 50, 200));
				spawn.doSpawn(true);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	void unSpawnMob()
	{
		NpcInstance actor = getActor();
		for(NpcInstance npc : World.getAroundNpc(actor, 500, 100))
		{
			if (npc.getNpcId() == 22799 ||  npc.getNpcId() == 22798 || npc.getNpcId() == 22800)
			{
				npc.decayMe();
			}
		}
	}
	
	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
			return true;

		if(_lastAttackTime != 0 && _lastAttackTime + NextAtack < System.currentTimeMillis())
		{
			
			if (actor.getTitle() == "Furnace of Magic Power")
			{
				changestate(actor, 2);
			}
			else if (actor.getTitle() == "Furnace of Fighting Spirit")
			{
				changestate(actor, 2);
			}
			else if (actor.getTitle() == "Furnace of Protection")
			{
				changestate(actor, 2);
			}
			else if (actor.getTitle() == "Furnace of Balance")
			{
				changestate(actor, 2);
			}
			else
			{
				return false;
			}
			_lastAttackTime = 0;
		}

		return super.thinkActive();
	}
	
	private void changestate(NpcInstance actor, int state)
	{
		actor.setNpcState(state);
	}
}
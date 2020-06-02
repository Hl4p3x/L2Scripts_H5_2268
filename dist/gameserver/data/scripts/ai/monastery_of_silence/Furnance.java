package ai.monastery_of_silence;

import java.util.List;

import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.instancemanager.SpawnManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.updatetype.NpcInfoType;
import l2s.gameserver.scripts.Functions;

/**
 * @author Bonux
**/
public class Furnance extends Fighter
{
	private static class SpawnBurners extends RunnableImpl
	{
		private final String _burnerSpawnGroup;

		public SpawnBurners(String burnerMaker)
		{
			_burnerSpawnGroup = burnerMaker;
		}

		public void runImpl()
		{
			List<NpcInstance> npcs = SpawnManager.getInstance().getAllSpawned(_burnerSpawnGroup);
			if(npcs.isEmpty())
				SpawnManager.getInstance().spawn(_burnerSpawnGroup);
		}
	}

	private static final int BURNER_NUMBER = 100000;
	private static final int CHECK_TIME = 100100;
	private static final int CHECK_TIME_ANNOUNCE = 100200;
	private static final int CHECK_TIME_ANNOUNCE2 = 100400;

	private final int _burnerNumber;
	private final String _burnerSpawnGroup;
	private final String _spawnGroup0;
	private final String _spawnGroup1;

	private boolean _activated = false;
	private boolean _changed = false;

	public Furnance(NpcInstance actor)
	{
		super(actor);

		actor.setIsInvul(true);

		_burnerNumber = actor.getParameter("burner_number", 0);

		_burnerSpawnGroup = actor.getParameter("burner_spawn_group", null);
		_spawnGroup0 = actor.getParameter("spawn_group_0", null);
		_spawnGroup1 = actor.getParameter("spawn_group_1", null);
	}

	@Override
	protected void onEvtSpawn()
	{
		NpcInstance actor = getActor();

		switch(_burnerNumber)
		{
			case 1:
				actor.setNameNpcString(NpcString.FURN1);
				break;
			case 2:
				actor.setNameNpcString(NpcString.FURN2);
				break;
			case 3:
				actor.setNameNpcString(NpcString.FURN3);
				break;
			case 4:
				actor.setNameNpcString(NpcString.FURN4);
				break;
		}

		actor.broadcastCharInfoImpl(NpcInfoType.NAME_NPCSTRINGID);

		if(_burnerNumber != 1)
		{
			SpawnManager.getInstance().despawn(_spawnGroup0);
			SpawnManager.getInstance().despawn(_spawnGroup1);
		}

		unblockTimer(CHECK_TIME_ANNOUNCE);
		addTimer(CHECK_TIME_ANNOUNCE, 3 * 60 * 1000L);

		if(_burnerNumber == 1)
		{
			_activated = true;
			actor.setNpcState(1);
		}
		else
		{
			_activated = false;
			actor.setNpcState(2);
		}
		super.onEvtSpawn();
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		//if(skill == null)
		//{
			NpcInstance actor = getActor();

			_activated = true;
			actor.setNpcState(1);
			broadCastScriptEvent("21140011", _burnerNumber, 600);
			if(!_changed)
			{
				blockTimer(CHECK_TIME_ANNOUNCE);
				_changed = true;
				addTimer(CHECK_TIME_ANNOUNCE2, 100L);
			}
		//}
	}

	@Override
	protected void onIntentionAttack(Creature target)
	{
		//
	}

	@Override
	protected void onEvtAggression(Creature attacker, int aggro)
	{
		//
	}

	@Override
	protected void onEvtScriptEvent(String event, Object arg1, Object arg2)
	{
		if(event.equalsIgnoreCase("21140011"))
		{
			int burnerNumber = ((Integer) arg1).intValue();
			if(_burnerNumber != burnerNumber)
			{
				NpcInstance actor = getActor();

				_activated = false;
				actor.setNpcState(2);
				if(!_changed)
				{
					blockTimer(CHECK_TIME_ANNOUNCE);
					_changed = true;
					addTimer(CHECK_TIME_ANNOUNCE2, 100L);
				}
			}
		}
		super.onEvtScriptEvent(event, arg1, arg2);
	}

	@Override
	protected void onEvtTimer(int timerId, Object arg1, Object arg2)
	{
		NpcInstance actor = getActor();
		if(timerId == CHECK_TIME_ANNOUNCE || timerId == CHECK_TIME_ANNOUNCE2)
		{
			Functions.npcSay(actor, NpcString.FURN1);
			addTimer(CHECK_TIME, 15000L);
		}
		else if(timerId == CHECK_TIME)
		{
			if(_activated)
			{
				List<NpcInstance> npcs = SpawnManager.getInstance().getAllSpawned(_spawnGroup0);
				if(npcs.isEmpty())
					SpawnManager.getInstance().spawn(_spawnGroup0);

				npcs = SpawnManager.getInstance().getAllSpawned(_spawnGroup1);
				if(npcs.isEmpty())
					SpawnManager.getInstance().spawn(_spawnGroup1);

				if(_burnerNumber != 1)
					ThreadPoolManager.getInstance().schedule(new SpawnBurners(_burnerSpawnGroup), 60 * 60 * 1000L + 1000);
			}
			else
			{
				SpawnManager.getInstance().despawn(_spawnGroup0);
				SpawnManager.getInstance().despawn(_spawnGroup1);
			}

			actor.doDie(null);
		}
		super.onEvtTimer(timerId, arg1, arg2);
	}
}
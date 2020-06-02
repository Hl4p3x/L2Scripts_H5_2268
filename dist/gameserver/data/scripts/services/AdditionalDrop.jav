package services;

import l2s.commons.util.Rnd;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.listener.CharListenerList;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.scripts.ScriptFile;

/**
 * @author Bonux
**/
public class AdditionalDrop implements ScriptFile
{
	private static class DeathListener implements OnDeathListener
	{
		@Override
		public void onDeath(Creature cha, Creature killer)
		{
			if(!cha.isMonster())
				return;

			MonsterInstance monster = (MonsterInstance) cha;

			Creature topDamager = monster.getAggroList().getTopDamager(killer);
			if(topDamager == null || !topDamager.isPlayable())
				return;

			if(!Functions.SimpleCheckDrop(monster, topDamager))
				return;

			// TODO: [Bonux] Пересмотреть формулу шанса.
			if(Rnd.chance(DROP_ITEM_CHANCE))
				monster.dropItem(topDamager.getPlayer(), DROP_ITEM_ID, DROP_ITEM_COUNT);
		}
	}

	private static final int DROP_ITEM_ID = 57; // ID предмета, который будет дропать со всех монстров.
	private static final long DROP_ITEM_COUNT = 1; // Количество предметов, которые будет дропать со всех монстров.
	private static final double DROP_ITEM_CHANCE = 10; // Шанс предметов, которые будут дропать со всех монстров.

	@Override
	public void onLoad()
	{
		CharListenerList.addGlobal(new DeathListener());
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
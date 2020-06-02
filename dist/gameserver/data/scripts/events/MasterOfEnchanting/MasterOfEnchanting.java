package events.MasterOfEnchanting;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Announcements;
import l2s.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.SimpleSpawner;
import l2s.gameserver.model.actor.listener.CharListenerList;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.scripts.ScriptFile;

/**
 * Autor: Bonux
 * Date: 30.08.09
 * Time: 17:49
 * http://www.lineage2.com/archive/2009/06/master_of_encha.html
 **/
public class MasterOfEnchanting extends Functions implements ScriptFile, OnPlayerEnterListener
{
	private static final Logger _log = LoggerFactory.getLogger(MasterOfEnchanting.class);
	private static final String EVENT_NAME = "MasterOfEnchanting";
	private static int EVENT_MANAGER_ID = 32599;
	private static List<SimpleSpawner> _spawns = new ArrayList<SimpleSpawner>();
	private static boolean _active = false;
	private static final int[][] _herbdrop = {
			{ 20000, 100 }, //Spicy Kimchee
			{ 20001, 100 }, //Spicy Kimchee
			{ 20002, 100 }, //Spicy Kimchee
			{ 20003, 100 } }; //Sweet-and-Sour White Kimchee
	private static final int[][] _energydrop = {
			{ 20004, 30 }, //Energy Ginseng
			{ 20005, 100 } }; //Energy Red Ginseng

	/**
	 * Спавнит эвент менеджеров
	 */
	private void spawnEventManagers()
	{
		final int EVENT_MANAGERS[][] = {
				{ -119494, 44882, 360, 24576 }, //Kamael Village
				{ -117239, 46842, 360, 49151 },
				{ -84023, 243051, -3728, 4096 }, //Talking Island Village
				{ -84411, 244813, -3728, 57343 },
				{ 45538, 48357, -3056, 18000 }, //Elven Village
				{ 46908, 50856, -2992, 8192 },
				{ 9929, 16324, -4568, 62999 }, //Dark Elven Village
				{ 11546, 17599, -4584, 46900 },
				{ 115096, -178370, -880, 0 }, //Dwarven Village
				{ 116199, -182694, -1488, 0 },
				{ -45372, -114104, -240, 16384 }, //Orc Village
				{ -45278, -112766, -240, 0 },
				{ -83156, 150994, -3120, 0 }, //Gludin
				{ -81031, 150038, -3040, 0 },
				{ -13727, 122117, -2984, 16384 }, //Gludio
				{ -14129, 123869, -3112, 40959 },
				{ 16111, 142850, -2696, 16000 }, //Dion
				{ 17275, 145000, -3032, 25000 },
				{ 111004, 218928, -3536, 16384 }, //Heine
				{ 108426, 221876, -3592, 49151 },
				{ 81755, 146487, -3528, 32768 }, //Giran
				{ 82145, 148609, -3464, 0 },
				{ 83037, 149324, -3464, 44000 },
				{ 81083, 56118, -1552, 32768 }, //Oren
				{ 81987, 53723, -1488, 0 },
				{ 117356, 76708, -2688, 49151 }, //Hunters Village
				{ 115887, 76382, -2712, 0 },
				{ 147200, 25614, -2008, 16384 }, //Aden
				{ 148557, 26806, -2200, 32768 },
				{ 43966, -47709, -792, 49999 }, //Rune
				{ 43165, -48461, -792, 17000 },
				{ 147421, -55435, -2728, 49151 }, //Goddart
				{ 148206, -55786, -2776, 904 },
				{ 85584, -142490, -1336, 0 }, //Schutgard
				{ 86865, -142915, -1336, 26000 } };

		SpawnNPCs(EVENT_MANAGER_ID, EVENT_MANAGERS, _spawns);
	}

	/**
	 * Удаляет спавн эвент менеджеров
	 */
	private void unSpawnEventManagers()
	{
		deSpawnNPCs(_spawns);
	}

	/**
	 * Читает статус эвента из базы.
	 * @return
	 */
	private static boolean isActive()
	{
		return IsActive(EVENT_NAME);
	}

	/**
	 * Запускает эвент
	 */
	public void startEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;

		if(SetActive(EVENT_NAME, true))
		{
			spawnEventManagers();
			System.out.println("Event: Master of Enchanting started.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.MasOfEnch.AnnounceEventStarted", null);
		}
		else
			player.sendMessage("Event 'Master of Enchanting' already started.");

		_active = true;
		show("admin/events/events.htm", player);
	}

	/**
	 * Останавливает эвент
	 */
	public void stopEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;
		if(SetActive(EVENT_NAME, false))
		{
			unSpawnEventManagers();
			System.out.println("Event: Master of Enchanting stopped.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.MasOfEnch.AnnounceEventStoped", null);
		}
		else
			player.sendMessage("Event 'Master of Enchanting' not started.");

		_active = false;
		show("admin/events/events.htm", player);
	}

	@Override
	public void onLoad()
	{
		CharListenerList.addGlobal(this);
		if(isActive())
		{
			_active = true;
			spawnEventManagers();
			_log.info("Loaded Event: Master of Enchanting [state: activated]");
		}
		else
			_log.info("Loaded Event: Master of Enchanting [state: deactivated]");
	}

	@Override
	public void onReload()
	{
		unSpawnEventManagers();
	}

	@Override
	public void onShutdown()
	{
		unSpawnEventManagers();
	}

	@Override
	public void onPlayerEnter(Player player)
	{
		if(_active)
			Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.MasOfEnch.AnnounceEventStarted", null);
	}

	//TODO: Надо реализовать ивентовые хербы и их дроп
	/**
	 * Обработчик смерти мобов, управляющий эвентовым дропом
	 */
	/** public static void onDeath(L2Character cha, L2Character killer)
	{
		if(_active && cha.isMonster && !cha.isRaid && killer != null && killer.getPlayer() != null && Math.abs(cha.getLevel() - killer.getLevel()) < 10)
		{
			for(int[] drop : _herbdrop)
				if(Rnd.get(1000) <= drop[1])
				{
					L2ItemInstance item = ItemTable.getInstance().createItem(drop[0], killer.getPlayer().getObjectId(), 0, "Master of Enchanting");
					((L2NpcInstance) cha).dropItem(killer.getPlayer(), item);

					break;
				}
			for(int[] drop : _energydrop)
				if(Rnd.get(1000) <= drop[1])
				{
					L2ItemInstance item = ItemTable.getInstance().createItem(drop[0], killer.getPlayer().getObjectId(), 0, "Master of Enchanting");
					((L2NpcInstance) cha).dropItem(killer.getPlayer(), item);

					break;
				}
		}
	}**/
}
package events.Portals;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.util.Rnd;
import l2s.gameserver.Announcements;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.SimpleSpawner;
import l2s.gameserver.model.actor.listener.CharListenerList;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.s2c.EarthQuakePacket;
import l2s.gameserver.network.l2.s2c.ExRedSkyPacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.SayPacket2;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.scripts.ScriptFile;
import l2s.gameserver.utils.NpcUtils;

//By Evil_dnk

public class portals extends Functions implements ScriptFile, OnDeathListener, OnPlayerEnterListener
{
	private static int EVENT_MANAGER_ID1 = 40005;
	private static int PORTAL = 40001;

	private static final Logger _log = LoggerFactory.getLogger(portals.class);

	private static List<SimpleSpawner> _spawns = new ArrayList<SimpleSpawner>();
	private static boolean _active = false;

	@Override
	public void onLoad()
	{
		CharListenerList.addGlobal(this);
		if(isActive())
		{
			_active = true;
			spawnEventManagers();
			_log.info("Loaded Event: Portals [state: activated]");
		}
		else
			_log.info("Loaded Event: Portals [state: deactivated]");
	}

	/**
	 * Читает статус эвента из базы.
	 *
	 * @return
	 */
	private static boolean isActive()
	{
		return IsActive("portals");
	}

	/**
	 * Запускает эвент
	 */
	public void startEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;

		if(SetActive("portals", true))
		{
			spawnEventManagers();
			System.out.println("Event 'L2 Protals Event' started.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.portals.AnnounceEventStarted", null);
		}
		else
			player.sendMessage("Event 'L2 Portals Event' already started.");

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
		if(SetActive("portals", false))
		{
			unSpawnEventManagers();
			System.out.println("Event 'L2 Portals Event' stopped.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.portals.AnnounceEventStoped", null);
		}
		else
			player.sendMessage("Event 'L2 Portals' not started.");

		_active = false;

		show("admin/events/events.htm", player);
	}

	@Override
	public void onPlayerEnter(Player player)
	{
		if(_active)
			Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.portals.AnnounceEventStarted", null);
	}

	/**
	 * Спавнит эвент менеджеров
	 */
	private void spawnEventManagers()
	{
		final int EVENT_MANAGERS1[][] = {
				{ 82168, 148842, -3464, 7806 }, // Giran
				{ 82204, 53259, -1488, 16384 }, // Oren
				{ 18924, 145782, -3088, 44034 }, // Dion
				{ 111794, 218967, -3536, 20780 }, // Heine
				{ -14539, 124066, -3112, 50874 }, // Gludio
				{ 147271, -55573, -2736, 60304 }, // Goddard
				{ 87801, -143150, -1296, 28800 }, // Shuttgard
				{ -80684, 149458, -3040, 16384 }, // Gludin
		};

		SpawnNPCs(EVENT_MANAGER_ID1, EVENT_MANAGERS1, _spawns);
	}

	/**
	 * Удаляет спавн эвент менеджеров
	 */
	private void unSpawnEventManagers()
	{
		deSpawnNPCs(_spawns);
	}

	@Override
	public void onReload()
	{
		unSpawnEventManagers();
	}

	@Override
	public void onShutdown()
	{}

	/**
	 * Обработчик смерти мобов, управляющий эвентовым дропом
	 */

	@Override
	public void onDeath(Creature cha, Creature killer)
	{
		if(_active && SimpleCheckDrop(cha, killer) && killer.isPlayer())
			if(cha.getNpcId() > 40000)
				return;
			else if(Rnd.get(10000000) <= 10000)
			{
				L2GameServerPacket redSky = new ExRedSkyPacket(10);
				L2GameServerPacket eq = new EarthQuakePacket(killer.getLoc(), 30, 12);
				killer.sendPacket(redSky, eq);
				killer.sendPacket(new SayPacket2(0, ChatType.ANNOUNCEMENT, "", "Открылся пространственный разлом"));
				NpcUtils.spawnSingle(PORTAL, cha.getX(), cha.getY(), cha.getZ());
			}
	}
}

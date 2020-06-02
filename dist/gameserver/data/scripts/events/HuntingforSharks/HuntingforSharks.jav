package events.HuntingforSharks;

import l2s.gameserver.Announcements;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.SimpleSpawner;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.scripts.ScriptFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class HuntingforSharks extends Functions implements ScriptFile
{
	private static final Logger _log = LoggerFactory.getLogger(HuntingforSharks.class);
	private static final int EVENT_THREE_ID = 36609;
	private static final int EVENT_SHARK_ID = 36610;
	private static final int EVENT_MANAGER_ID = 36611;
	private static List<SimpleSpawner> _spawns = new ArrayList<SimpleSpawner>();

	private static boolean _active = false;

	private void spawnEventManagers()
	{
		final int EVENT_MANAGERS[][] = {{82247, 148605, -3472, 0}, //Giran
			{81923, 148916, -3482, 14902}, //Giran
			{81921, 148298, -3482, 47930}, //Giran
		};

		SpawnNPCs(EVENT_MANAGER_ID, EVENT_MANAGERS, _spawns);
	}

	private void spawnThree()
	{
		final int EVENT_THREE[][] = {{82168, 148856, -3464, 0}, //Giran
			{81672, 148856, -3464, 0}, //Giran
			{81672, 148360, -3464, 0}, //Giran
			{82168, 148360, -3464, 0}, //Giran
		};

		SpawnNPCs(EVENT_THREE_ID, EVENT_THREE, _spawns);
	}

	private void spawnShark85()
	{
		final int EVENT_SHARK[][] = {{147704, 172312, -5040, 0}, {147672, 171752, -4950, 0}, {147152, 171816, -5043, 0}, {147174, 171192, -4879, 0}, {147664, 170840, -4878, 0}, {148712, 171304, -4947, 0}, {149192, 171576, -4912, 0}, {149192, 171576, -4912, 0}, {149928, 171608, -4828, 0}, {150344, 171192, -4865, 0}, {150312, 170520, -4600, 0}, {151000, 170360, -4469, 0}, {150968, 170920, -4589, 0}, {150520, 171816, -4860, 0}, {150664, 171272, -4732, 0}, {151128, 171512, -4634, 0}, {151112, 171848, -4857, 0}, {151464, 172216, -4852, 0}, {151288, 174840, -4835, 0}, {150984, 175112, -4817, 0}, {151080, 174688, -4729, 0}, {150312, 175336, -4827, 0}, {150120, 176104, -4708, 0}, {149672, 175816, -4858, 0}, {149080, 175384, -4971, 0}, {148824, 176088, -4824, 0}, {148648, 176008, -4927, 0}, {147752, 175720, -4953, 0}, {147784, 175256, -5040, 0}, {148024, 174904, -5040, 0}, {146968, 174712, -5013, 0}, {147048, 174264, -5041, 0},}; //Спавн акул 85го уровня TODO Создать АИ передвижения

		SpawnNPCs(EVENT_SHARK_ID, EVENT_SHARK, _spawns);
	}

	private void unSpawnEventManagers()
	{
		deSpawnNPCs(_spawns);
	}

	private static boolean isActive()
	{
		return IsActive("HuntingforSharks");
	}

	public void startEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
		{
			return;
		}

		if(SetActive("HuntingforSharks", true))
		{
			spawnEventManagers();
			spawnThree();
			spawnShark85();
			System.out.println("Начался ивент 'Охота на акул'.");
			_log.info("Начался ивент 'Охота на акул'.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.HuntingforSharks.AnnounceEventStarted", null);
		}
		else
		{
			player.sendMessage("Ивент 'Охота на акул' уже запущен.");
		}

		_active = true;

		show("admin/events/events.htm", player);
	}

	public void stopEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
		{
			return;
		}

		if(SetActive("HuntingforSharks", false))
		{
			unSpawnEventManagers();
			_log.info("Ивент 'Охота на акул' закончен.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.HuntingforSharks.AnnounceEventStoped", null);
		}
		else
		{
			player.sendMessage("Ивент 'Охота на акул' незапущен.");
		}

		_active = false;

		show("admin/events/events.htm", player);
	}

	public static void OnPlayerEnter(Player player)
	{
		if(_active)
		{
			Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.HuntingforSharks.AnnounceEventStarted", null);
		}
	}

	@Override
	public void onLoad()
	{
		if(isActive())
		{
			_active = true;
			spawnEventManagers();
			spawnThree();
			spawnShark85();
			System.out.println("Loaded Event: Hunting for Sharks [state: activated]");
			_log.info("Loaded Event: Hunting for Sharks [state: activated]");
		}
		else
		{
			_log.info("Loaded Event: Hunting for Sharks [state: deactivated]");
		}
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
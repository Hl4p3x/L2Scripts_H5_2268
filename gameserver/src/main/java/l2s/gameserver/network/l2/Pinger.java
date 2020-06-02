package l2s.gameserver.network.l2;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.NetPingPacket;

/**
 * @author: Kolobrodik
 * @date: 0:19/29.02.12
 * @description: Retail-like система пинга.
 */
public class Pinger extends Thread
{
	private static Pinger instance;
	private Map<Integer, Integer> pingTimes; // Перевести на FastMap

	private static final Logger _log = LoggerFactory.getLogger(Pinger.class);

	public static Pinger getInstance()
	{
		if (instance == null)
			instance = new Pinger();

		return instance;
	}

	private Pinger()
	{
		//run();
	}

	public int getPingTimes(int objId)
	{
		Integer times = pingTimes.get(objId);
		if (times == null)
			return 0;
		else
			return times;
	}

	public void answerPing(int objId)
	{
		//if (!Config.PING_ENABLED)
		//	return;

		synchronized (pingTimes)
		{
			pingTimes.remove(objId);
		}
	}

	@Override
	public void run()
	{
		//try
		//{
			//Thread.sleep(Config.PING_INTERVAL);
		//}
		//catch (final InterruptedException ignored)
		//{

		//}

		try
		{
			Map<Integer, Integer> newPingTimes = null;
			synchronized (pingTimes)
			{
				for (final Player player : GameObjectsStorage.getAllPlayersForIterate())
				{
					if (player == null || player.isInOfflineMode() || player.isInStoreMode())
						continue;
					int oid = player.getObjectId();
					final int times = getPingTimes(oid);
					//if (times > Config.PING_IGNORED_REQUEST_LIMIT - 1)
					//	player.getNetConnection().closeNow(false); // Сделать гуманнее?
					//else
					//{
					//	newPingTimes.put(oid, times + 1);
					//	_log.info("::::::::::::"+times);
					//}
					player.sendPacket(new NetPingPacket(player.getObjectId()));
				}
			}
			pingTimes = newPingTimes;
		}
		catch (Exception ignored)
		{

		}
	}
}

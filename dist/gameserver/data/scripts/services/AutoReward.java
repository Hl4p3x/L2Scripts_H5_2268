package services;

import java.util.concurrent.ScheduledFuture;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.scripts.ScriptFile;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author Bonux
**/
public class AutoReward implements ScriptFile, Runnable
{
	private static final int REWARD_DELAY = 3600000; // 1 час
	private static final int NOT_REWARD_AFK_DELAY = 600000; // 10 минут

	private static final int[][] REWARDS = new int[][]{
		//{ 4357, 1 },
	};

	private ScheduledFuture<?> _task = null;

	@Override
	public void run()
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(!player.isOnline())
				continue;

			if(player.isInOfflineMode())
				continue;

			if((System.currentTimeMillis() - player.getLastNotAfkTime()) > NOT_REWARD_AFK_DELAY)
				continue;

			for(int[] reward : REWARDS)
				ItemFunctions.addItem(player, reward[0], reward[1], true, "Auto reward by service");
		}
	}

	@Override
	public void onLoad()
	{
		if(_task == null)
			_task = ThreadPoolManager.getInstance().scheduleAtFixedDelay(this, REWARD_DELAY, REWARD_DELAY);
	}

	@Override
	public void onReload()
	{
		if(_task != null)
		{
			_task.cancel(true);
			_task = null;
		}
		_task = ThreadPoolManager.getInstance().scheduleAtFixedDelay(this, REWARD_DELAY, REWARD_DELAY);
	}

	@Override
	public void onShutdown()
	{
		//
	}
}
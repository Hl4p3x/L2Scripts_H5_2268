package l2s.gameserver.instancemanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.lang.reference.HardReference;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.SetupGaugePacket;
import l2s.gameserver.network.l2.s2c.SocialActionPacket;

/**
 * @author: Kolobrodik
 * @date: 17:36/25.05.2012
 * @description: Данный класс предоставляет собой контроллер сервиса "Away"
 */
public final class AwayManager
{
	protected static final Logger _log = LoggerFactory.getLogger(AwayManager.class);

	private static AwayManager _instance;

	public static final AwayManager getInstance()
	{
		if(_instance == null)
			_instance = new AwayManager();
		return _instance;
	}

	private TIntObjectMap<String> _awayTexts = new TIntObjectHashMap<String>();

	private AwayManager()
	{
		_log.info("Away Manager: Initializing...");
	}

	/**
	 * @param player
	 * @param text
	 */
	public void setAway(Player player, String text)
	{
		player.broadcastPacket(new SocialActionPacket(player.getObjectId(), 9));
		player.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.AwayManager.setAway", player, Config.AWAY_TIMER));
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.sendPacket(new SetupGaugePacket(player, SetupGaugePacket.BLUE, Config.AWAY_TIMER * 1000));
		player.block();

		ThreadPoolManager.getInstance().schedule(new PlayerAwayTask(player, text), Config.AWAY_TIMER * 1000);
	}

	/**
	 * @param player
	 */
	public void setBack(Player player)
	{
		player.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.AwayManager.setBack", player, Config.BACK_TIMER));
		player.sendPacket(new SetupGaugePacket(player, SetupGaugePacket.BLUE, Config.BACK_TIMER * 1000));

		ThreadPoolManager.getInstance().schedule(new PlayerBackTask(player), Config.BACK_TIMER * 1000);
	}

	public String getAwayText(Player player)
	{
		return _awayTexts.get(player.getObjectId());
	}

	private class PlayerAwayTask implements Runnable
	{
		private final HardReference<Player> _playerRef;
		private final String _awayText;

		public PlayerAwayTask(Player player, String awayText)
		{
			_playerRef = player.getRef();
			_awayText = awayText;
		}

		@Override
		public void run()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;

			if(player.isAttackingNow() || player.isCastingNow())
			{
				player.unblock();
				player.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.AwayManager.PlayerAwayTask.InCombat", player));
				return;
			}

			if(_awayText.length() <= 1)
				player.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.AwayManager.PlayerAwayTask.NoText", player));
			else
				player.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.AwayManager.PlayerAwayTask", player, _awayText));

			_awayTexts.put(player.getObjectId(), _awayText);

			player.setAwayingMode(true);
			player.abortAttack(true, false);
			player.abortCast(true, false);
			player.setTarget(null);
			player.sitDown(null);
			player.broadcastUserInfo(true);
		}
	}

	private class PlayerBackTask implements Runnable
	{
		private final HardReference<Player> _playerRef;

		public PlayerBackTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void run()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;

			player.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.AwayManager.PlayerBackTask", player));

			_awayTexts.remove(player.getObjectId());

			player.unblock();
			player.setAwayingMode(false);
			player.standUp();
			player.broadcastUserInfo(false);
		}
	}
}

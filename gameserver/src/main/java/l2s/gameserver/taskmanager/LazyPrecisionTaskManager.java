package l2s.gameserver.taskmanager;

import java.util.concurrent.Future;

import l2s.commons.threading.SteppingRunnableQueueManager;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.SystemMessage;

/**
 * Менеджер задач вспомогательных задач, шаг выполенния задач 1с.
 *
 * @author G1ta0
 */
public class LazyPrecisionTaskManager extends SteppingRunnableQueueManager
{
	private static final LazyPrecisionTaskManager _instance = new LazyPrecisionTaskManager();

	public static final LazyPrecisionTaskManager getInstance()
	{
		return _instance;
	}

	private LazyPrecisionTaskManager()
	{
		super(1000L);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 1000L, 1000L);
		//Очистка каждые 60 секунд
		ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> LazyPrecisionTaskManager.this.purge(), 60000L, 60000L);
	}

	public Future<?> addPCCafePointsTask(final Player player)
	{
		long delay = Config.ALT_PCBANG_POINTS_DELAY * 60000L;

		return scheduleAtFixedRate(() ->
		{
			if(player.isInOfflineMode() || player.getLevel() < Config.ALT_PCBANG_POINTS_MIN_LVL)
				return;

			if(Config.ALT_PCBANG_PA_ONLY && !player.hasPremiumAccount())
				return;

			if(player.getPcBangPoints() < Config.ALT_MAX_PC_BANG_POINTS)
				player.addPcBangPoints(Config.ALT_PCBANG_POINTS_BONUS, Config.ALT_PCBANG_POINTS_BONUS_DOUBLE_CHANCE > 0 && Rnd.chance(Config.ALT_PCBANG_POINTS_BONUS_DOUBLE_CHANCE), true);
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessage.THE_MAXIMUM_ACCUMULATION_ALLOWED_OF_PC_CAFE_POINTS_HAS_BEEN_EXCEEDED_YOU_CAN_NO_LONGER_ACQUIRE);
				player.sendPacket(sm);
			}
		}, delay, delay);
	}

	public Future<?> addVitalityRegenTask(final Player player)
	{
		long delay = 60000L;

		return scheduleAtFixedRate(() ->
		{
			if(player.isInOfflineMode() || !player.isInPeaceZone())
				return;

			player.setVitality(player.getVitality() + 1); // одно очко раз в минуту
		}, delay, delay);
	}

	public Future<?> startPremiumAccountExpirationTask(Player player, int expire)
	{
		long delay = expire * 1000L - System.currentTimeMillis();
		return schedule(() -> player.removePremiumAccount(), delay);
	}

	public Future<?> addNpcAnimationTask(final NpcInstance npc)
	{
		return scheduleAtFixedRate(() ->
		{
			if(npc.isVisible() && !npc.isActionsDisabled() && !npc.isMoving && !npc.isInCombat())
				npc.onRandomAnimation();
		}, 1000L, Rnd.get(Config.MIN_NPC_ANIMATION, Config.MAX_NPC_ANIMATION) * 1000L);
	}
}

package l2s.gameserver.taskmanager;

import l2s.commons.threading.RunnableImpl;
import l2s.commons.threading.SteppingRunnableQueueManager;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;

/**
 * Менеджер задач для работы с эффектами, шаг выполенния задач 250 мс.
 * 
 * @author G1ta0
 */
public class EffectTaskManager extends SteppingRunnableQueueManager
{
	private final static long TICK = 250L;

	private final static EffectTaskManager[] _instances = new EffectTaskManager[Config.EFFECT_TASK_MANAGER_COUNT];
	static
	{
		for(int i =0; i < _instances.length; i++)
			_instances[i] = new EffectTaskManager();
	}

	private static int randomizer = 0;

	public final static EffectTaskManager getInstance()
	{
		return _instances[randomizer++ & _instances.length - 1];
	}

	private EffectTaskManager()
	{
		super(TICK);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, Rnd.get(TICK), TICK);
		//Очистка каждые 30 секунд
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl()
		{
			@Override
			public void runImpl() throws Exception
			{
				EffectTaskManager.this.purge();
			}

		}, 30000L, 30000L);
	}

	public CharSequence getStats(int num)
	{
		return _instances[num].getStats();
	}
}
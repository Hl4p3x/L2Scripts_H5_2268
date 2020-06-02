package l2s.gameserver.taskmanager.actionrunner.tasks;

import l2s.gameserver.taskmanager.actionrunner.ActionRunner;
import l2s.gameserver.taskmanager.actionrunner.ActionWrapper;

/**
 * @author VISTALL
 * @date 20:00/24.06.2011
 */
public abstract class AutomaticTask extends ActionWrapper
{
	public static final String TASKS = "automatic_tasks";

	public AutomaticTask()
	{
		super(TASKS);
	}

	public abstract void doTask() throws Exception;

	public abstract long reCalcTime(boolean start);

	@Override
	public void runImpl0() throws Exception
	{
		try
		{
			doTask();
		}
		finally
		{
			ActionRunner.getInstance().register(reCalcTime(false), this);
		}
	}
}

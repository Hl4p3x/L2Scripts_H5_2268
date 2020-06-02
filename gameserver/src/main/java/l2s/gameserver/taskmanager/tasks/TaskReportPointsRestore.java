package l2s.gameserver.taskmanager.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.taskmanager.Task;
import l2s.gameserver.taskmanager.TaskManager;
import l2s.gameserver.taskmanager.TaskManager.ExecutedTask;
import l2s.gameserver.taskmanager.TaskTypes;

public class TaskReportPointsRestore extends Task
{
	private static final Logger _log = LoggerFactory.getLogger(TaskReportPointsRestore.class);
	private static final String NAME = "report_points_restore";

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement update = con.prepareStatement("UPDATE characters SET bot_report_points = 7");
			update.execute();
			update.close();
			_log.info("Sucessfully restored Bot Report Points for all accounts!");
		}
		catch(SQLException sqle)
		{
			sqle.printStackTrace();
		}
		finally
		{
			try
			{
				DbUtils.closeQuietly(con);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void initializate()
	{
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", "00:00:00", StringUtils.EMPTY);
	}
}

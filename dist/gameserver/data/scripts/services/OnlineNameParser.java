package services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbutils.DbUtils;
import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.scripts.ScriptFile;
import l2s.gameserver.tables.FakePlayersTable;
import l2s.gameserver.utils.SqlBatch;


public class OnlineNameParser implements ScriptFile
{
	
	private static final Logger _log = LoggerFactory.getLogger(OnlineNameParser.class);
	
	@Override
	public void onLoad()
	{
		//if(Config.ENABLE_ONLINE_NAME_PARS)
			//ThreadPoolManager.getInstance().scheduleAtFixedRate(new GenerateFreshOnline(), 1000, Config.ONLINE_NAME_PARS_DELAY * 60 * 1000); //default 30min
	}

	@Override
	public void onReload()
	{
	}

	@Override
	public void onShutdown()
	{
		delete();
	}
	
	public class GenerateFreshOnline extends RunnableImpl
	{
		public void runImpl() throws Exception
		{
			delete();
			insert();
		}
	}
	
	public static void delete()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM `online_names`");
			statement.execute();
		}
		catch(SQLException e)
		{
			_log.error("couldn't", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}		
	}
	
	public static void insert()
	{
		List<OnlinePlayerStatus> all_stats = new ArrayList<OnlinePlayerStatus>();
		
		for(Player online_player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(online_player != null)
			{
				OnlinePlayerStatus status = new OnlinePlayerStatus(online_player.getName(), online_player.isInStoreMode() ? (online_player.isInOfflineMode() ? 2 : 1) : 0);
				all_stats.add(status);
			}
		}
		List<String> fakes = FakePlayersTable.getActiveFakePlayers();
		if(fakes != null && !fakes.isEmpty())
		{
			for(String names : fakes)
			{
				OnlinePlayerStatus status = new OnlinePlayerStatus(names, 0);
				all_stats.add(status);
			}
		}	
		
		if(all_stats.isEmpty())
			return;
		
		Connection con = null;
		Statement statement = null;
		StringBuilder sb;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			SqlBatch b = new SqlBatch("INSERT INTO `online_names` (`name`, `status`) VALUES"); //status 0

			for(OnlinePlayerStatus status : all_stats)
			{
				sb = new StringBuilder("(");
				sb.append("'"); // name
				sb.append(status.getName()); // name
				sb.append("',"); // name
				sb.append(status.isTrade()).append(")"); // is in trade
				b.write(sb.toString());			
				//_log.info("zapros: "+sb.toString()+"");	
			}		
			if(!b.isEmpty())
			{
				//_log.info("zapros all: "+b.close()+"");	
				statement.executeUpdate(b.close());
				
			}	
		}
		catch(SQLException e)
		{
			_log.error("couldn't", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}			
	}
	
	public static class OnlinePlayerStatus
	{
		public String _name;
		public int _status;
		
		public OnlinePlayerStatus(String name, int status)
		{
			_name = name;
			_status = status;
		}
		
		public String getName()
		{
			return _name;
		}
		
		public int isTrade()
		{
			return _status;
		}
	}
	
}
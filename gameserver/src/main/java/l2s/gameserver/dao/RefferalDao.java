package l2s.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbutils.DbUtils;
import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.handler.voicecommands.impl.RefferalSystem;

/**
 * @author Iqman
 * @date 19:23/11.01.2012
 */
public class RefferalDao
{
	private static final Logger _log = LoggerFactory.getLogger(RefferalDao.class);
	private static final RefferalDao _instance = new RefferalDao();

	public static RefferalDao getInstance()
	{
		return _instance;
	}
	
	public void loadRefferals()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM refferal_system");
			rset = statement.executeQuery();
			while(rset.next())
			{
				int reffered_id = rset.getInt("reffered_id");
				String reffered_name = rset.getString("reffered_name");
				int refferer_id = rset.getInt("refferer_id");
				String refferer_name = rset.getString("refferer_name");				
				CharInfo char_info = new CharInfo(reffered_id, reffered_name, refferer_id, refferer_name);
				RefferalSystem.all_info.add(char_info);
			}	
				
		}
		catch(Exception e)
		{
			//?
		}
		finally
		{
			
			DbUtils.closeQuietly(con, statement, rset);
			if(Config.ALLOW_REFFERAL_SYSTEM)
				_log.info("Refferal System: Data Loaded");
		}
	
	}

	public static class CharInfo
	{
		public final int _reffered_id;
		public final String _reffered_name;
		public final int _refferer_id;
		public final String _refferer_name;

		public CharInfo(int reffered_id, String reffered_name, int refferer_id, String refferer_name)
		{
			_reffered_id = reffered_id;
			_reffered_name = reffered_name;
			_refferer_id = refferer_id;
			_refferer_name = refferer_name;
			
		}
	}	

		public List<String> getCharNamesByRef(int reffered_id)
		{
			if(reffered_id <= 0)
				return null;
				
			List<String> _l = new ArrayList<String>();
			for(CharInfo info : RefferalSystem.all_info)
			{
				if(info._reffered_id == reffered_id)
					_l.add(info._refferer_name);
			}
			return _l;
		}
		
		public int getRefferedIdByRefferer(int refferer_id)
		{
			int reffered_id = 0;
			
			for(CharInfo info : RefferalSystem.all_info)
			{
				if(info._refferer_id == refferer_id)
				{
					reffered_id = info._reffered_id;
					break;
				}
			}
			return reffered_id;	
		}
		
		public void removeBlock(int refferer_id)
		{
			for(CharInfo info : RefferalSystem.all_info)
			{
				if(info._refferer_id == refferer_id)
				{
					RefferalSystem.all_info.remove(info);
					break;
				}	
			}			
		}	
	
	public static int getReffererIdByName(String name)
	{
		int id = 0;
		for(CharInfo info : RefferalSystem.all_info)
		{
			if(info._refferer_name.equalsIgnoreCase(name))
			{
				id = info._refferer_id;
				break;
			}
		}
		return id;
	}
	
	public int countReffs(String name)
	{
		int counter = 0;

		for(CharInfo info : RefferalSystem.all_info)
		{
			if(info._reffered_name.equalsIgnoreCase(name))
				counter++;
		}
		return counter;	
	}
	
	public boolean isCharReffered(int refferer_id)
	{
		boolean exist = false;
			
		for(CharInfo info : RefferalSystem.all_info)
		{
			if(info._refferer_id == refferer_id)
			{
				exist = true;
				break;
			}	
		}
		return exist;	
	}	
	public void startSaveTask()
	{
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new SaveRefSystem(), Config.REF_SAVE_INTERVAL*60000, Config.REF_SAVE_INTERVAL*60000);
		//ThreadPoolManager.getInstance().scheduleAtFixedRate(new SaveRefClanSystem(), Config.REF_CLAN_SAVE_INTERVAL*60000, Config.REF_CLAN_SAVE_INTERVAL*60000);
	}		
	
	public class SaveRefSystem extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{	
			SaveRef();
		}
		
	}	
	
/*	public class SaveRefSystem extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{	
			SaveClan();
		}
		
	}	
*/

	private void delRecords()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM refferal_system");
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}	
		for(CharInfo charInfo : RefferalSystem.all_info)
		{
			savePlayer(charInfo);
		}		
	}

	public void SaveRef()
	{
		delRecords();
	}

	public void savePlayer(CharInfo info)
	{
		
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO refferal_system (reffered_id,reffered_name,refferer_id,refferer_name) VALUES(?,?,?,?)");
			statement.setInt(1, info._reffered_id);
			statement.setString(2, info._reffered_name);
			statement.setInt(3, info._refferer_id);
			statement.setString(4, info._refferer_name);			
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("could update player info for: " + info._reffered_name, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		
	}	
}

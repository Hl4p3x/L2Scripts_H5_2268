package l2s.gameserver.security.hwid;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.ChangeLogManager;
import l2s.gameserver.utils.BatchStatement;

public class HwidEngine
{
	private static final Logger _log = LoggerFactory.getLogger(HwidEngine.class);
  
	private final List<HwidGamer> _allHwids = new CopyOnWriteArrayList<HwidGamer>();
	private final List<HwidLogging.SimpleLog> _logsToSave = new CopyOnWriteArrayList<HwidLogging.SimpleLog>();
  
	public HwidGamer getGamerByHwid(String hwid)
	{
		for(HwidGamer gamer : _allHwids)
		{
			if(gamer.getHwid().equals(hwid))
				return gamer;
		}
		return null;
	}
	
	public HwidGamer newPlayer(Player player)
	{
		String hwid = player.getHWID();
		for(HwidGamer gamer : _allHwids)
		{
			if(gamer.getHwid().equals(hwid))
			{
				gamer.addPlayer(player);
				return gamer;
			}
		}
		
		HwidGamer newHwid = loadHwidFromDatabase(hwid);
		newHwid.addPlayer(player);
		_allHwids.add(newHwid);
		return newHwid;
	}
	
	public String canILog(String hwid)
	{
		for(HwidGamer gamer : _allHwids)
		{
			if(gamer.getHwid().equals(hwid))
			{
				if(gamer.getOnlineChars().size() == Config.MAX_CHARS_PER_PC)
				{
					return "Only " + Config.MAX_CHARS_PER_PC + " character" + (Config.MAX_CHARS_PER_PC > 1 ? "s" : "") + " may be online on the same PC!";
				}
			}
		}
		long banTime = getBanLeftTime(hwid);
		if(banTime == -100L)
			return "You are banned!";
		if(banTime > 0L)
			return "You are banned for " + (int)Math.ceil(banTime / 60000L) + " more minutes!";
		return null;
	}
  
	public void logFailedLogin(Player player)
	{
		String hwid = player.getHWID();
		for(HwidGamer gamer : _allHwids)
		{
			if(gamer.getHwid().equals(hwid))
			{
				//Log.LogToPlayerCommunity(gamer, player, "Failed to login. Too many online characters!");
				return;
			}
		}
	}
  
	public void banHwid(String hwidToBan)
	{
		HwidGamer gamer = getGamerByHwid(hwidToBan);
		if(gamer != null)
		{
			gamer.setHwidBanned(-100L);
			for(Player player : gamer.getOnlineChars())
			{
				//Log.LogToPlayerCommunity(gamer, player, "Kicked! HWID was banned by admin!");
				player.kick();
			}
			banHwidInDb(hwidToBan, gamer.getBannedToDate());
		}
	}
  
	private long getBanLeftTime(String hwid)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;	
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT banned FROM hwid WHERE HWID=?");
			statement.setString(1, hwid);
			rset = statement.executeQuery();
			if(rset.next())
			{
				long toDate = rset.getLong("banned");
				if(toDate == -100L)
					return -100L;
				return toDate - System.currentTimeMillis();
			}
		}
        catch(Exception e)
        {
			_log.error("Failed to load Hwid(" + hwid + ") from Database: ", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return 0L;
	}
  
	private HwidGamer loadHwidFromDatabase(String hwid)
	{
		HwidGamer foundGamer = null;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;			
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM hwid WHERE HWID=?");
			statement.setString(1, hwid);
			rset = statement.executeQuery();
			if(rset.next())
			{
				long firstTimePlayed = rset.getLong("first_time_played");
				long totalTimePlayed = rset.getLong("total_time_played") * 1000L;
				int pollAnswer = rset.getInt("poll_answer");
				int warnings = rset.getInt("warnings");
				int seenChangeLog = rset.getInt("seenChangeLog");
				HwidGamer.PLAYER_THREAT threat = HwidGamer.PLAYER_THREAT.valueOf(rset.getString("threat"));
				long banned = rset.getLong("banned");
				foundGamer = new HwidGamer(hwid, firstTimePlayed, totalTimePlayed, pollAnswer, warnings, seenChangeLog, threat, banned);
			}
		}
        catch(Exception e)
        {
			_log.error("Failed to load Hwid(" + hwid + ") from Database: ", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
    
		if(foundGamer == null)
			foundGamer = justJoinedServer(hwid);
		return foundGamer;
	}
  
	private HwidGamer justJoinedServer(String hwid)
	{
		HwidGamer newGamer = new HwidGamer(hwid, System.currentTimeMillis() / 1000L, 0L, -1, 0, ChangeLogManager.getInstance().getLatestChangeId(), HwidGamer.PLAYER_THREAT.NONE, 0L);
		saveNewGamer(newGamer);
		return newGamer;
	}
  
	public void updateGamerInDb(HwidGamer gamer)
	{
		Connection con = null;
		PreparedStatement statement = null;	
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE hwid SET first_time_played=?, total_time_played=?, poll_answer=?, warnings=?, seenChangeLog=?, threat=?, banned=? WHERE HWID=?");
			statement.setLong(1, gamer.getFirstTimePlayed());
			statement.setLong(2, gamer.getTotalTimePlayed() / 1000L);
			statement.setInt(3, gamer.getPollAnswer());
			statement.setInt(4, gamer.getWarnings());
			statement.setInt(5, gamer.getSeenChangeLog());
			statement.setString(6, gamer.getThreat().toString());
			statement.setLong(7, gamer.getBannedToDate());
			statement.setString(8, gamer.getHwid());
			statement.executeUpdate();
		}
        catch(Exception e)
        {
          _log.error("Failed to insert Hwid(" + gamer.getHwid() + ") to Database: ", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
  
	private void banHwidInDb(String hwid, long toDate)
	{
		Connection con = null;
		PreparedStatement statement = null;			
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE hwid SET banned=? WHERE HWID=?");
			statement.setLong(1, toDate);
			statement.setString(2, hwid);
			statement.executeUpdate();
        }
        catch(Exception e)
        {
			_log.error("Failed to ban Hwid(" + hwid + ") in Database: ", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
  
	private void saveNewGamer(HwidGamer gamer)
	{
		Connection con = null;
		PreparedStatement statement = null;			
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO hwid SET HWID=?, first_time_played=?, total_time_played=?, poll_answer=?, warnings=?, seenChangeLog=?, threat=?, banned=?");
			statement.setString(1, gamer.getHwid());
			statement.setLong(2, gamer.getFirstTimePlayed());
			statement.setLong(3, gamer.getTotalTimePlayed() / 1000L);
			statement.setInt(4, gamer.getPollAnswer());
			statement.setInt(5, gamer.getWarnings());
			statement.setInt(6, gamer.getSeenChangeLog());
			statement.setString(7, gamer.getThreat().toString());
			statement.setLong(8, 0L);
			statement.execute();
		}
        catch(Exception e)
        {
			_log.error("Failed to insert Hwid(" + gamer.getHwid() + ") to Database: ", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
  

	public void saveAllData()
	{
		if(!_logsToSave.isEmpty())
		{
			Connection con = null;
			PreparedStatement statement = null;		
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = BatchStatement.createPreparedStatement(con, "INSERT INTO character_logs (obj_Id, HWID, action, time) VALUES (?, ?, ?, ?)");
				for(HwidLogging.SimpleLog log : _logsToSave)
				{
					statement.setInt(1, log._charObjId);
					statement.setString(2, log._hwid);
					statement.setString(3, log._msg);
					statement.setLong(4, log._time);
					statement.addBatch();
				}
				statement.executeBatch();
			}
			catch(Exception e)
			{
				_log.error("Failed to save all hwid logs to db: ", e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}
    
		if(!_allHwids.isEmpty())
		{
			Connection con = null;
			PreparedStatement statement = null;			
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = BatchStatement.createPreparedStatement(con, "INSERT INTO `hwid` (HWID,first_time_played,total_time_played,poll_answer,warnings,threat) VALUES(?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE first_time_played=VALUES(first_time_played),total_time_played=VALUES(total_time_played),poll_answer=VALUES(poll_answer),warnings=VALUES(warnings),threat=VALUES(threat);");
				for(HwidGamer gamer : _allHwids)
				{
					statement.setString(1, gamer.getHwid());
					statement.setLong(2, gamer.getFirstTimePlayed());
					statement.setLong(3, gamer.getTotalTimePlayed() / 1000L);
					statement.setInt(4, gamer.getPollAnswer());
					statement.setInt(5, gamer.getWarnings());
					statement.setString(6, gamer.getThreat().toString());
					statement.addBatch();
				}
				statement.executeBatch();
			}
			catch(Exception e)
			{
				_log.error("Failed to save all hwid times to db: ", e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}
	}
  
	public static HwidEngine getInstance()
	{
		return SingletonHolder._instance;
	}
  
	private static class SingletonHolder
	{
		protected static final HwidEngine _instance = new HwidEngine();
	}
}

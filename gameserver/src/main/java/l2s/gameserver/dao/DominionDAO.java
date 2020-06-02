package l2s.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dao.JdbcEntityState;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.entity.residence.Dominion;

/**
 * @author VISTALL
 * @date 18:10/15.04.2011
 */
public class DominionDAO
{
	private static final Logger _log = LoggerFactory.getLogger(DominionDAO.class);
	private static final DominionDAO _instance = new DominionDAO();

	public static final String SELECT_SQL_QUERY = "SELECT lord_object_id, wards FROM dominion WHERE id=?";
	public static final String UPDATE_SQL_QUERY = "UPDATE dominion SET lord_object_id=?, wards=? WHERE id=?";
	public static final String SELECT_SQL_QUERY2 = "SELECT siege_date FROM dominion where id =?";
	public static final String UPDATE_SQL_QUERY2 = "UPDATE dominion set siege_date =? where id =?";

	public static DominionDAO getInstance()
	{
		return _instance;
	}

	public void select(Dominion dominion)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_SQL_QUERY);
			statement.setInt(1, dominion.getId());
			rset = statement.executeQuery();
			if(rset.next())
			{
				dominion.setLordObjectId(rset.getInt("lord_object_id"));

				String flags = rset.getString("wards");
				if(!flags.isEmpty())
				{
					String[] values = flags.split(";");
					for(int i = 0; i < values.length; i++)
						dominion.addFlag(Integer.parseInt(values[i]));
				}
			}
		}
		catch(Exception e)
		{
			_log.error("Dominion.loadData(): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void update(Dominion residence)
	{
		if(!residence.getJdbcState().isUpdatable())
			return;

		residence.setJdbcState(JdbcEntityState.STORED);
		update0(residence);
	}

	private void update0(Dominion dominion)
	{
		String wardsString = "";
		int[] flags = dominion.getFlags();
		if(flags.length > 0)
			for(int flag : flags)
				wardsString += flag + ";";

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(UPDATE_SQL_QUERY);
			statement.setInt(1, dominion.getLordObjectId());
			statement.setString(2, wardsString);
			statement.setInt(3, dominion.getId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("DominionDAO#update0(Dominion): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public long getDominionSiegeDate(int dominion)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		long s_date = 0L;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_SQL_QUERY2);
			statement.setInt(1, dominion);
			rset = statement.executeQuery();
			if(rset.next())
			{
				s_date = rset.getLong("siege_date");
			}
		}
		catch(Exception e)
		{
			_log.error("Dominion.selectData(): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}	
		return s_date;
	
	}
	
	public void update01(int id, long time)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(UPDATE_SQL_QUERY2);
			statement.setLong(1, time);
			statement.setInt(2, id);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("DominionDAO#update01(Dominion, time): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}	
}

package l2s.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.utils.Location;

/**
 * @author Bonux
**/
public class FakePlayerDAO
{
	private static final Logger _log = LoggerFactory.getLogger(FakePlayerDAO.class);

	private static final FakePlayerDAO _instance = new FakePlayerDAO();

	public static FakePlayerDAO getInstance()
	{
		return _instance;
	}

	public TIntObjectMap<Location> restore()
	{
		TIntObjectMap<Location> locations = new TIntObjectHashMap<Location>();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT id, x, y, z, path_id FROM fake_players");
			rset = statement.executeQuery();
			while(rset.next())
			{
				int id = rset.getInt("id");
				int x = rset.getInt("x");
				int y = rset.getInt("y");
				int z = rset.getInt("z");
				int path_id = rset.getInt("path_id");

				locations.put(id, new Location(x, y, z, path_id));
			}

			DbUtils.closeQuietly(statement, rset);

			statement = con.prepareStatement("TRUNCATE TABLE fake_players");
			statement.execute();
			DbUtils.close(statement);
		}
		catch(Exception e)
		{
			_log.error("FakePlayerDAO.restore(int): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
		return locations;
	}

	public void insert(int id, Location loc, int pathId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO fake_players (id,x,y,z,path_id) VALUES(?,?,?,?,?)");
			statement.setInt(1, id);
			statement.setInt(2, loc.getX());
			statement.setInt(3, loc.getY());
			statement.setInt(4, loc.getZ());
			statement.setInt(5, pathId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("FakePlayerDAO.insert(int,Location,int): Could not add fake player ID: " + id, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void delete(int id)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM fake_players WHERE id = ?");
			statement.setInt(1, id);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("FriendList: could not delete fake player ID: " + id, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
}

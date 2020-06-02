package l2s.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.base.FenceState;
import l2s.gameserver.model.instances.FenceInstance;
import l2s.gameserver.utils.Location;

/**
 * @author Bonux
 */
public class FencesDAO
{
	private static final Logger _log = LoggerFactory.getLogger(FencesDAO.class);
	private static final FencesDAO _instance = new FencesDAO();

	public static final String SELECT_SQL_QUERY = "SELECT object_id, name, x, y, z, width, length, height, state FROM fences";
	public static final String DELETE_SQL_QUERY = "DELETE FROM fences WHERE object_id=?";
	public static final String UPDATE_SQL_QUERY = "UPDATE fences SET name=?, x=?, y=?, z=?, width=?, length=?, height=?, state=? WHERE object_id=?";
	public static final String INSERT_SQL_QUERY = "REPLACE INTO fences (object_id, name, x, y, z, width, length, height, state) VALUES (?,?,?,?,?,?,?,?,?)";

	public static FencesDAO getInstance()
	{
		return _instance;
	}

	public void restore()
	{
		int restoredCount = 0;

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_SQL_QUERY);
			rset = statement.executeQuery();
			while(rset.next())
			{
				int object_id = rset.getInt("object_id");
				String name = rset.getString("name");
				int x = rset.getInt("x");
				int y = rset.getInt("y");
				int z = rset.getInt("z");
				int width = rset.getInt("width");
				int length = rset.getInt("length");
				int height = rset.getInt("height");
				FenceState state = FenceState.VALUES[rset.getInt("state")];

				FenceInstance instance = new FenceInstance(object_id, name, width, length, height, state);
				instance.spawnMe(new Location(x, y, z));

				restoredCount++;
			}
		}
		catch(Exception e)
		{
			_log.error("FencesDAO:restore()", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		_log.info("FencesDAO: restored " + restoredCount + " fence(s).");
	}

	public boolean insert(FenceInstance fence)
	{
		if(!fence.getReflection().isMain())
			return false;

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(INSERT_SQL_QUERY);
			statement.setInt(1, fence.getObjectId());
			statement.setString(2, fence.getName());
			statement.setInt(3, fence.getX());
			statement.setInt(4, fence.getY());
			statement.setInt(5, fence.getZ());
			statement.setInt(6, fence.getWidth());
			statement.setInt(7, fence.getLength());
			statement.setInt(8, fence.getHeight());
			statement.setInt(9, fence.getState().ordinal());
			statement.executeUpdate();
		}
		catch(final Exception e)
		{
			_log.error("FencesDAO:insert(fence)", e);
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}

	public boolean update(FenceInstance fence)
	{
		if(!fence.getReflection().isMain())
			return false;

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(UPDATE_SQL_QUERY);
			statement.setString(1, fence.getName());
			statement.setInt(2, fence.getX());
			statement.setInt(3, fence.getY());
			statement.setInt(4, fence.getZ());
			statement.setInt(5, fence.getWidth());
			statement.setInt(6, fence.getLength());
			statement.setInt(7, fence.getHeight());
			statement.setInt(8, fence.getState().ordinal());
			statement.setInt(9, fence.getObjectId());
			statement.executeUpdate();
		}
		catch(final Exception e)
		{
			_log.error("FencesDAO:update(fence)", e);
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}

	public boolean delete(FenceInstance fence)
	{
		if(!fence.getReflection().isMain())
			return false;

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_SQL_QUERY);
			statement.setInt(1, fence.getObjectId());
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.error("FencesDAO:delete(fence)", e);
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}
}

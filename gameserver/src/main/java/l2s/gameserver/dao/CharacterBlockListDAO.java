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
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Block;

/**
 * @author Bonux
 */
public class CharacterBlockListDAO
{
	private static final Logger _log = LoggerFactory.getLogger(CharacterBlockListDAO.class);

	private static final CharacterBlockListDAO _instance = new CharacterBlockListDAO();

	public static CharacterBlockListDAO getInstance()
	{
		return _instance;
	}

	public TIntObjectMap<Block> select(Player owner)
	{
		TIntObjectMap<Block> map = new TIntObjectHashMap<Block>();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT b.target_Id, c.char_name FROM character_blocklist b LEFT JOIN characters c ON b.target_Id = c.obj_Id WHERE b.obj_Id = ?");
			statement.setInt(1, owner.getObjectId());
			rset = statement.executeQuery();
			while(rset.next())
			{
				String name = rset.getString("c.char_name");
				if(name == null)
					continue;

				int objectId = rset.getInt("b.target_Id");

				map.put(objectId, new Block(objectId, name));
			}
		}
		catch(Exception e)
		{
			_log.error("CharacterBlockListDAO.select(L2Player): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return map;
	}

	public void insert(Player owner, int blockedObjectId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO character_blocklist (obj_Id,target_Id) VALUES(?,?)");
			statement.setInt(1, owner.getObjectId());
			statement.setInt(2, blockedObjectId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn(owner.getBlockList() + " could not add player to black list objectid: " + blockedObjectId, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void delete(Player owner, int blockedObjectId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_blocklist WHERE obj_Id=? AND target_Id=?");
			statement.setInt(1, owner.getObjectId());
			statement.setInt(2, blockedObjectId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn(owner.getBlockList() + " could not delete blocked objectId: " + blockedObjectId + " ownerId: " + owner.getObjectId(), e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
}

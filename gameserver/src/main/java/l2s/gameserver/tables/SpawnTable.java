package l2s.gameserver.tables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.SpawnHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.SimpleSpawner;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.spawn.PeriodOfDay;
import l2s.gameserver.templates.spawn.SpawnNpcInfo;
import l2s.gameserver.templates.spawn.SpawnTemplate;
import l2s.gameserver.utils.Location;

public class SpawnTable
{
	private static final Logger _log = LoggerFactory.getLogger(SpawnTable.class);
	private static SpawnTable _instance;

	public static SpawnTable getInstance()
	{
		if (_instance == null)
			new SpawnTable();
		return _instance;
	}

	private SpawnTable()
	{
		_instance = this;
		if (Config.LOAD_CUSTOM_SPAWN)
			fillCustomSpawnTable();
	}

	private void fillCustomSpawnTable()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM add_spawnlist ORDER by npc_templateid");
			rset = statement.executeQuery();

			while (rset.next())
			{
				int count = rset.getInt("count");
				int delay = rset.getInt("respawn_delay");
				int delay_rnd = rset.getInt("respawn_delay_rnd");
				int npcId = rset.getInt("npc_templateid");
				int x = rset.getInt("locx");
				int y = rset.getInt("locy");
				int z = rset.getInt("locz");
				int h = rset.getInt("heading");

				SpawnTemplate template = new SpawnTemplate("from_database", PeriodOfDay.NONE, count, delay, delay_rnd, null);
				template.addNpc(new SpawnNpcInfo(npcId, 1, StatsSet.EMPTY));
				template.addSpawnRange(new Location(x, y, z, h));
				SpawnHolder.getInstance().addSpawn(PeriodOfDay.NONE.name(), template);
			}
		}
		catch (Exception e1)
		{
			_log.warn("custom_spawnlist couldnt be initialized:" + e1);
			e1.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void addNewSpawn(SimpleSpawner spawn)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO `add_spawnlist` (location,count,npc_templateid,locx,locy,locz,heading,respawn_delay) values(?,?,?,?,?,?,?,?)");
			statement.setString(1, "");
			statement.setInt(2, spawn.getAmount());
			statement.setInt(3, spawn.getCurrentNpcId());
			statement.setInt(4, spawn.getLocx());
			statement.setInt(5, spawn.getLocy());
			statement.setInt(6, spawn.getLocz());
			statement.setInt(7, spawn.getHeading());
			statement.setInt(8, spawn.getRespawnDelay());
			statement.execute();
		}
		catch (Exception e1)
		{
			_log.warn("spawn couldnt be stored in db:" + e1);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void deleteSpawn(Location loc, int template)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM add_spawnlist WHERE locx=? AND locy=? AND locz=? AND npc_templateid=? AND heading=?");
			statement.setInt(1, loc.x);
			statement.setInt(2, loc.y);
			statement.setInt(3, loc.z);
			statement.setInt(4, template);
			statement.setInt(5, loc.h);
			statement.execute();
		}
		catch (Exception e1)
		{
			_log.warn("spawn couldnt be deleted in db:" + e1);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
}
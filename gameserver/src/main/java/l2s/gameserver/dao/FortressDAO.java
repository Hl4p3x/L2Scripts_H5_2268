package l2s.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dao.JdbcEntityState;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.entity.residence.Fortress;

/**
 * @author VISTALL
 * @date 18:10/15.04.2011
 */
public class FortressDAO
{
	private static final Logger _log = LoggerFactory.getLogger(FortressDAO.class);
	private static final FortressDAO _instance = new FortressDAO();

	public static final String SELECT_SQL_QUERY = "SELECT * FROM fortress WHERE id = ?";
	public static final String REPLACE_SQL_QUERY = "REPLACE INTO fortress (id, name, state, castle_id, last_siege_date, own_date, siege_date, supply_count, facility_0, facility_1, facility_2, facility_3, facility_4, cycle, reward_count, paid_cycle, supply_spawn) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	public static FortressDAO getInstance()
	{
		return _instance;
	}

	public void select(Fortress fortress)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_SQL_QUERY);
			statement.setInt(1, fortress.getId());
			rset = statement.executeQuery();
			if(rset.next())
			{
				fortress.setFortState(rset.getInt("state"), rset.getInt("castle_id"));
				fortress.setCycle(rset.getInt("cycle"));
				fortress.setRewardCount(rset.getInt("reward_count"));
				fortress.setPaidCycle(rset.getInt("paid_cycle"));
				fortress.setSupplyCount(rset.getInt("supply_count"));
				fortress.setSupplySpawn(rset.getLong("supply_spawn"));
				fortress.getSiegeDate().setTimeInMillis(rset.getLong("siege_date"));
				fortress.getLastSiegeDate().setTimeInMillis(rset.getLong("last_siege_date"));
				fortress.getOwnDate().setTimeInMillis(rset.getLong("own_date"));
				for(int i = 0; i < Fortress.FACILITY_MAX; i++)
					fortress.setFacilityLevel(i, rset.getInt("facility_" + i));
			}
		}
		catch(Exception e)
		{
			_log.error("FortressDAO.select(Fortress):" + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void update(Fortress fortress)
	{
		if(!fortress.getJdbcState().isUpdatable())
			return;

		fortress.setJdbcState(JdbcEntityState.STORED);
		update0(fortress);
	}

	private void update0(Fortress fortress)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(REPLACE_SQL_QUERY);

			int i = 0;
			statement.setInt(++i, fortress.getId());
			statement.setString(++i, fortress.getName());
			statement.setInt(++i, fortress.getContractState());
			statement.setInt(++i, fortress.getCastleId());
			statement.setLong(++i, fortress.getLastSiegeDate().getTimeInMillis());
			statement.setLong(++i, fortress.getOwnDate().getTimeInMillis());
			statement.setLong(++i, fortress.getSiegeDate().getTimeInMillis());
			statement.setInt(++i, fortress.getSupplyCount());
			statement.setInt(++i, fortress.getFacilityLevel(0));
			statement.setInt(++i, fortress.getFacilityLevel(1));
			statement.setInt(++i, fortress.getFacilityLevel(2));
			statement.setInt(++i, fortress.getFacilityLevel(3));
			statement.setInt(++i, fortress.getFacilityLevel(4));
			statement.setInt(++i, fortress.getCycle());
			statement.setInt(++i, fortress.getRewardCount());
			statement.setInt(++i, fortress.getPaidCycle());
			statement.setLong(++i, fortress.getSupplySpawn());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("FortressDAO#update0(Fortress): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
}

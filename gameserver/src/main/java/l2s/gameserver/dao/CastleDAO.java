package l2s.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dao.JdbcEntityState;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.entity.residence.Castle;

/**
 * @author VISTALL
 * @date 18:10/15.04.2011
 */
public class CastleDAO
{
	private static final Logger _log = LoggerFactory.getLogger(CastleDAO.class);
	private static final CastleDAO _instance = new CastleDAO();

	public static final String SELECT_SQL_QUERY = "SELECT tax_percent, treasury, siege_date, last_siege_date, own_date FROM castle WHERE id=? LIMIT 1";
	public static final String REPLACE_SQL_QUERY = "REPLACE INTO castle (id, name, tax_percent, treasury, last_siege_date, own_date, siege_date) VALUES (?,?,?,?,?,?,?)";

	public static CastleDAO getInstance()
	{
		return _instance;
	}

	public void select(Castle castle)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_SQL_QUERY);
			statement.setInt(1, castle.getId());
			rset = statement.executeQuery();
			if(rset.next())
			{
				castle.setTaxPercent(rset.getInt("tax_percent"));
				castle.setTreasury(rset.getLong("treasury"));
				castle.getSiegeDate().setTimeInMillis(rset.getLong("siege_date"));
				castle.getLastSiegeDate().setTimeInMillis(rset.getLong("last_siege_date"));
				castle.getOwnDate().setTimeInMillis(rset.getLong("own_date"));
			}
		}
		catch(Exception e)
		{
			_log.error("CastleDAO.select(Castle):" + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void update(Castle residence)
	{
		if(!residence.getJdbcState().isUpdatable())
			return;

		residence.setJdbcState(JdbcEntityState.STORED);
		update0(residence);
	}

	private void update0(Castle castle)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(REPLACE_SQL_QUERY);

			int i = 0;
			statement.setInt(++i, castle.getId());
			statement.setString(++i, castle.getName());
			statement.setInt(++i, castle.getTaxPercent0());
			statement.setLong(++i, castle.getTreasury());
			statement.setLong(++i, castle.getLastSiegeDate().getTimeInMillis());
			statement.setLong(++i, castle.getOwnDate().getTimeInMillis());
			statement.setLong(++i, castle.getSiegeDate().getTimeInMillis());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("CastleDAO#update0(Castle): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
}

package l2s.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dao.JdbcEntityState;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.entity.votereward.VoteRewardRecord;

public class VoteRewardRecordsDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(VoteRewardRecordsDAO.class);
	private static final VoteRewardRecordsDAO INSTANCE = new VoteRewardRecordsDAO();

	public static VoteRewardRecordsDAO getInstance() {
		return INSTANCE;
	}

	private static final String SELECT_SQL_QUERY = "SELECT * FROM votereward_records WHERE site=?";
	private static final String INSERT_SQL_QUERY = "INSERT INTO votereward_records (site, identifier, votes, lastvotedate) VALUES (?,?,?,?)";
	private static final String UPDATE_SQL_QUERY = "UPDATE votereward_records SET votes=?, lastvotedate=? WHERE site=? AND identifier=?";

	public void restore(Map<String, VoteRewardRecord> records, String site) {
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_SQL_QUERY);
			statement.setString(1, site);
			rset = statement.executeQuery();
			while(rset.next())
			{
				String identifier = rset.getString("identifier");
				int votes = rset.getInt("votes");
				int lastvotedate = rset.getInt("lastvotedate");
				records.put(identifier, new VoteRewardRecord(site, identifier, votes, lastvotedate));
			}
		}
		catch(Exception e)
		{
			LOGGER.error("VoteRewardRecordsDAO.select(String):" + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void save(VoteRewardRecord voteRewardRecord) {
		if(!voteRewardRecord.getJdbcState().isSavable())
			return;

		voteRewardRecord.setJdbcState(JdbcEntityState.STORED);
		save0(voteRewardRecord);
	}

	private void save0(VoteRewardRecord voteRewardRecord) {
		Connection con = null;
		PreparedStatement ps = null;
		try  {
			con = DatabaseFactory.getInstance().getConnection();
			ps = con.prepareStatement(INSERT_SQL_QUERY);
			ps.setString(1, voteRewardRecord.getSite());
			ps.setString(2, voteRewardRecord.getIdentifier());
			ps.setInt(3, voteRewardRecord.getVotes());
			ps.setInt(4, voteRewardRecord.getLastVoteTime());
			ps.execute();
		}
		catch (SQLException e) {
			LOGGER.error("VoteRewardRecordsDAO.save0(VoteRewardRecord):" + e, e);
		}
		finally {
			DbUtils.closeQuietly(con, ps);
		}
	}

	public void update(VoteRewardRecord voteRewardRecord) {
		if(!voteRewardRecord.getJdbcState().isUpdatable())
			return;

		voteRewardRecord.setJdbcState(JdbcEntityState.STORED);
		update0(voteRewardRecord);
	}

	private void update0(VoteRewardRecord voteRewardRecord) {
		Connection con = null;
		PreparedStatement ps = null;
		try  {
			con = DatabaseFactory.getInstance().getConnection();
			ps = con.prepareStatement(UPDATE_SQL_QUERY);
			ps.setInt(1, voteRewardRecord.getVotes());
			ps.setInt(2, voteRewardRecord.getLastVoteTime());
			ps.setString(3, voteRewardRecord.getSite());
			ps.setString(4, voteRewardRecord.getIdentifier());
			ps.execute();
		}
		catch (SQLException e) {
			LOGGER.error("VoteRewardRecordsDAO.update0(VoteRewardRecord):" + e, e);
		}
		finally {
			DbUtils.closeQuietly(con, ps);
		}
	}
}
package handler.bbs.custom.communitybuffer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;

public class CommunityBufferDAO {
	private static CommunityBufferDAO _instance = new CommunityBufferDAO();

	public static CommunityBufferDAO getInstance() {
		return _instance;
	}

	private static final String SELECT_SET_SQL_QUERY = "SELECT id, name FROM bbs_buff_sets WHERE owner_id=?";
	private static final String SELECT_SET_SKILLS_SQL_QUERY = "SELECT skill_id FROM bbs_buff_set_skills WHERE owner_id=? AND set_id=?";

	private static final String INSERT_SET_SQL_QUERY = "INSERT INTO bbs_buff_sets (owner_id, id, name) VALUES (?, ?, ?)";
	private static final String INSERT_SET_SKILLS_SQL_QUERY = "INSERT INTO bbs_buff_set_skills (owner_id, set_id, skill_id) VALUES (?, ?, ?)";

	private static final String DELETE_SET_SQL_QUERY = "DELETE FROM bbs_buff_sets WHERE owner_id=? AND id=?";
	private static final String DELETE_SET_SKILLS_SQL_QUERY = "DELETE FROM bbs_buff_set_skills WHERE owner_id=? AND set_id=?";
	private static final String DELETE_SET_SKILL_SQL_QUERY = "DELETE FROM bbs_buff_set_skills WHERE owner_id=? AND set_id=? AND skill_id=?";

	private static final String CLEANUP_SET_SQL_QUERY = "DELETE FROM bbs_buff_sets WHERE owner_id NOT IN(SELECT obj_id FROM characters)";
	private static final String CLEANUP_SET_SKILLS_SQL_QUERY = "DELETE FROM bbs_buff_set_skills WHERE NOT EXISTS (SELECT * from bbs_buff_sets AS sets where bbs_buff_set_skills.owner_id = sets.owner_id and bbs_buff_set_skills.set_id = sets.id)";

	private static final Logger LOGGER = LoggerFactory.getLogger(CommunityBufferDAO.class);

	private final TIntObjectMap<Map<Integer, BuffSet>> CACHED_BUFF_SETS = new TIntObjectHashMap<>();

	private CommunityBufferDAO() {
		cleanUP();
	}

	public Map<Integer, BuffSet> restore(int ownerId) {
		synchronized (CACHED_BUFF_SETS) {
			Map<Integer, BuffSet> result = CACHED_BUFF_SETS.get(ownerId);
			if (result != null)
				return result;

			result = new LinkedHashMap<>();

			Connection con = null;
			PreparedStatement statement = null;
			ResultSet rset = null;
			try {
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(SELECT_SET_SQL_QUERY);
				statement.setInt(1, ownerId);
				rset = statement.executeQuery();
				while (rset.next()) {
					int id = rset.getInt("id");
					String name = rset.getString("name");
					BuffSet buffSet = new BuffSet(ownerId, id, name);

					PreparedStatement statement2 = null;
					ResultSet rset2 = null;
					try {
						statement2 = con.prepareStatement(SELECT_SET_SKILLS_SQL_QUERY);
						statement2.setInt(1, ownerId);
						statement2.setInt(2, id);
						rset2 = statement2.executeQuery();
						while (rset2.next()) {
							buffSet.getSkills().add(rset2.getInt("skill_id"));
						}
					} finally {
						DbUtils.closeQuietly(statement2, rset2);
					}

					result.put(buffSet.getId(), buffSet);
				}
			} catch (Exception e) {
				LOGGER.error("CharacterSubclassDAO:restore(int)", e);
			} finally {
				DbUtils.closeQuietly(con, statement, rset);
			}
			CACHED_BUFF_SETS.put(ownerId, result);
			return result;
		}
	}

	public boolean insertSet(BuffSet buffSet) {
		synchronized (CACHED_BUFF_SETS) {
			Connection con = null;
			PreparedStatement statement = null;
			try {
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(INSERT_SET_SQL_QUERY);
				statement.setInt(1, buffSet.getOwnerId());
				statement.setInt(2, buffSet.getId());
				statement.setString(3, buffSet.getName());
				statement.executeUpdate();

				for (int skillId : buffSet.getSkills()) {
					DbUtils.closeQuietly(statement);

					statement = con.prepareStatement(INSERT_SET_SKILLS_SQL_QUERY);
					statement.setInt(1, buffSet.getOwnerId());
					statement.setInt(2, buffSet.getId());
					statement.setInt(3, skillId);
					statement.executeUpdate();
				}
			} catch (final Exception e) {
				LOGGER.error("CommunityBufferDAO:insertSet(BuffSet)", e);
				return false;
			} finally {
				DbUtils.closeQuietly(con, statement);
			}

			Map<Integer, BuffSet> buffSets = CACHED_BUFF_SETS.get(buffSet.getOwnerId());
			if(buffSets == null) {
				buffSets = new LinkedHashMap<>();
				CACHED_BUFF_SETS.put(buffSet.getOwnerId(), buffSets);
			}
			buffSets.put(buffSet.getId(), buffSet);
			return true;
		}
	}

	public boolean insertSkillToSet(BuffSet buffSet, int skillId) {
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(INSERT_SET_SKILLS_SQL_QUERY);
			statement.setInt(1, buffSet.getOwnerId());
			statement.setInt(2, buffSet.getId());
			statement.setInt(3, skillId);
			statement.executeUpdate();
		} catch (final Exception e) {
			LOGGER.error("insertSkillToSet:insertSkillToSet(BuffSet,int)", e);
			return false;
		} finally {
			DbUtils.closeQuietly(con, statement);
		}

		buffSet.getSkills().add(skillId);
		return true;
	}

	public void deleteSet(int ownerId, int setId) {
		synchronized (CACHED_BUFF_SETS) {
			Connection con = null;
			PreparedStatement statement = null;
			try {
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(DELETE_SET_SQL_QUERY);
				statement.setInt(1, ownerId);
				statement.setInt(2, setId);
				statement.execute();

				DbUtils.closeQuietly(statement);

				statement = con.prepareStatement(DELETE_SET_SKILLS_SQL_QUERY);
				statement.setInt(1, ownerId);
				statement.setInt(2, setId);
				statement.execute();
			} catch (Exception e) {
				LOGGER.error("CharacterPostFriendDAO.delete(int,int): " + e, e);
				return;
			} finally {
				DbUtils.closeQuietly(con, statement);
			}

			Map<Integer, BuffSet> buffSets = CACHED_BUFF_SETS.get(ownerId);
			if (buffSets != null)
				buffSets.remove(setId);
		}
	}

	public void deleteSkillFromSet(BuffSet buffSet, int skillId) {
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_SET_SKILL_SQL_QUERY);
			statement.setInt(1, buffSet.getOwnerId());
			statement.setInt(2, buffSet.getId());
			statement.setInt(3, skillId);
			statement.execute();
		} catch(Exception e) {
			LOGGER.error("CharacterPostFriendDAO.delete(BuffSet,int): " + e, e);
			return;
		} finally {
			DbUtils.closeQuietly(con, statement);
		}

		buffSet.getSkills().remove(skillId);
	}

	private void cleanUP() {
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(CLEANUP_SET_SQL_QUERY);
			statement.executeUpdate();

			DbUtils.closeQuietly(statement);

			statement = con.prepareStatement(CLEANUP_SET_SKILLS_SQL_QUERY);
			statement.executeUpdate();
		} catch(Exception e) {
			LOGGER.error("CharacterPostFriendDAO.cleanUP(): " + e, e);
			return;
		} finally {
			DbUtils.closeQuietly(con, statement);
		}
	}
}
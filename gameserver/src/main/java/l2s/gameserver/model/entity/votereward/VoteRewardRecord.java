package l2s.gameserver.model.entity.votereward;

import l2s.commons.dao.JdbcEntity;
import l2s.commons.dao.JdbcEntityState;
import l2s.gameserver.dao.VoteRewardRecordsDAO;

/**
 * @author Bonux (bonuxq@gmail.com)
 * 11.02.2019
 * Developed for L2-Scripts.com
 **/
public class VoteRewardRecord implements JdbcEntity {
	private final String site;
	private final String identifier;

	private int votes;
	private int lastVoteTime;

	private JdbcEntityState _jdbcEntityState = JdbcEntityState.CREATED;

	public VoteRewardRecord(String site, String identifier, int votes, int lastVoteTime) {
		this.site = site;
		this.identifier = identifier;
		this.votes = votes;
		this.lastVoteTime = lastVoteTime;
	}

	public String getSite() {
		return site;
	}

	public String getIdentifier() {
		return identifier;
	}

	public int getVotes() {
		return votes;
	}

	public int getLastVoteTime() {
		return lastVoteTime;
	}

	public void onReceiveReward(int votes, long voteTime) {
		this.votes += votes;
		lastVoteTime = (int) (voteTime / 1000);
		setJdbcState(JdbcEntityState.UPDATED);
		update();
	}

	@Override
	public void setJdbcState(JdbcEntityState state) {
		_jdbcEntityState = state;
	}

	@Override
	public JdbcEntityState getJdbcState() {
		return _jdbcEntityState;
	}

	@Override
	public void save() {
		VoteRewardRecordsDAO.getInstance().save(this);
	}

	@Override
	public void delete() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void update() {
		VoteRewardRecordsDAO.getInstance().update(this);
	}
}

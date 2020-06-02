package votereward;

import org.apache.commons.lang3.StringUtils;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.votereward.VoteApiService;
import l2s.gameserver.model.entity.votereward.VoteRewardRecord;
import l2s.gameserver.model.entity.votereward.VoteRewardSite;

/**
 * @author Bonux (bonuxq@gmail.com)
 * 11.02.2019
 * Developed for L2-Scripts.com
 * Vote site handler for: l2network.eu
 **/
public class L2NetworkSite extends VoteRewardSite {
	private final String apiKey;

	public L2NetworkSite(MultiValueSet<String> parameters) {
		super(parameters);
		apiKey = parameters.getString("api_key");
	}

	@Override
	public boolean isEnabled() {
		if(StringUtils.isEmpty(apiKey))
			return false;
		return super.isEnabled();
	}

	@Override
	public boolean tryGiveRewards(Player player) {
		String serverResponse = VoteApiService.getApiResponse(String.format("https://l2network.eu/index.php?a=in&u=%s&ipc=%s", apiKey, player.getIP()));
		if(serverResponse == null) {
			return false;
		}

		int voteCount;
		try {
			voteCount = Integer.parseInt(serverResponse.trim());
		}
		catch (Exception e) {
			return false;
		}

		getLock().lock();
		try {
			VoteRewardRecord record = getRecord(player.getIP());

			int availableVotes = voteCount - record.getVotes();
			if (availableVotes <= 0)
				return false;

			record.onReceiveReward(availableVotes, System.currentTimeMillis());
			giveRewards(player, availableVotes);
			return true;
		}
		finally {
			getLock().unlock();
		}
	}
}
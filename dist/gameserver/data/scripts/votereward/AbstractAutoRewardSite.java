package votereward;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.napile.primitive.pair.IntIntPair;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.votereward.VoteRewardRecord;
import l2s.gameserver.model.entity.votereward.VoteRewardSite;

/**
 * @author Bonux (bonuxq@gmail.com)
 * 11.02.2019
 * Developed for L2-Scripts.com
 * Vote site handler for: l2top.ru
 **/
public abstract class AbstractAutoRewardSite extends VoteRewardSite {
	public AbstractAutoRewardSite(MultiValueSet<String> parameters) {
		super(parameters);
	}

	@Override
	public final void run() {
		getLock().lock();
		try {
			Map<String, List<IntIntPair>> votesCache = new HashMap<>();
			parseVotes(votesCache);

			for(Player player : GameObjectsStorage.getAllPlayersForIterate()) {
				if(player.isInOfflineMode() || player.isFakePlayer())
					continue;

				List<IntIntPair> voteInfos = votesCache.get(getIdentifier(player).toLowerCase());
				if(voteInfos == null)
					continue;

				int availableVotes = 0;

				VoteRewardRecord record = getRecord(getIdentifier(player));
				for(IntIntPair info : voteInfos) {
					if(info.getKey() > record.getLastVoteTime())
						availableVotes += info.getValue();
				}

				if(availableVotes > 0) {
					record.onReceiveReward(availableVotes, System.currentTimeMillis());
					giveRewards(player, availableVotes);
				}
			}
		}
		finally {
			getLock().unlock();
		}
	}

	protected String getIdentifier(Player player) {
		return player.getName();
	}

	protected abstract void parseVotes(Map<String, List<IntIntPair>> votesCache);
}
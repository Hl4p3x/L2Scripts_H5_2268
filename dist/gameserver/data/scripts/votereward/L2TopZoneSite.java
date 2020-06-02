package votereward;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.votereward.VoteApiService;
import l2s.gameserver.model.entity.votereward.VoteRewardRecord;
import l2s.gameserver.model.entity.votereward.VoteRewardSite;

/**
 * @author Bonux (bonuxq@gmail.com)
 * 11.02.2019
 * Developed for L2-Scripts.com
 * Vote site handler for: l2topzone.com
 **/
public class L2TopZoneSite extends VoteRewardSite {
	private final String apiKey;
	private final int voteDelay;

	public L2TopZoneSite(MultiValueSet<String> parameters) {
		super(parameters);
		apiKey = parameters.getString("api_key");
		voteDelay = parameters.getInteger("vote_delay", (int) TimeUnit.HOURS.toMillis(12));
	}

	@Override
	public boolean isEnabled() {
		if(StringUtils.isEmpty(apiKey))
			return false;
		return super.isEnabled();
	}

	@Override
	public boolean tryGiveRewards(Player player) {
		String serverResponse = VoteApiService.getApiResponse(String.format("https://api.l2topzone.com/v1/vote?token=%s&ip=%s", apiKey, player.getIP()));
		if(serverResponse == null) {
			return false;
		}

		//{"ok": true,"error_code": 0,"description": "","result": {"isVoted": true,"voteTime": "1502460586","serverTime": 1502460630}}
		JsonElement jelement = new JsonParser().parse(serverResponse);
		JsonObject  topObject = jelement.getAsJsonObject();

		JsonPrimitive isOkPrimitive = topObject.getAsJsonPrimitive("ok");
		if(!isOkPrimitive.getAsBoolean()){
			return false;
		}

		JsonObject resultObject = topObject.getAsJsonObject("result");

		JsonPrimitive isVotedObject = resultObject.getAsJsonPrimitive("isVoted");
		if(!isVotedObject.getAsBoolean()) {
			return false;
		}

		JsonPrimitive voteTimePrimitive = resultObject.getAsJsonPrimitive("voteTime");
		long voteTime = voteTimePrimitive.getAsInt() * 1000L;

		getLock().lock();
		try {
			VoteRewardRecord record = getRecord(player.getIP());

			long lastVoteTime = (record.getLastVoteTime() * 1000L);
			if(lastVoteTime >= voteTime)
				return false;

			long nextVoteTime = lastVoteTime + voteDelay;
			if (System.currentTimeMillis() < nextVoteTime)
				return false;

			record.onReceiveReward(1, voteTime);
			giveRewards(player, 1);
			return true;
		}
		finally {
			getLock().unlock();
		}
	}
}
package votereward;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
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
 * Vote site handler for: hopzone.net
 **/
public class HopZoneSite extends VoteRewardSite {
	private static final DateFormat HOP_ZONE_SERVER_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static {
		HOP_ZONE_SERVER_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("Europe/Bucharest"));
	}

	private final String apiKey;
	private final int voteDelay;

	public HopZoneSite(MultiValueSet<String> parameters) {
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
		String serverResponse = VoteApiService.getApiResponse(String.format("https://api.hopzone.net/lineage2/vote?token=%s&ip_address=%s", apiKey, player.getIP()));
		if(serverResponse == null) {
			return false;
		}

		//{"apiver":"0.1c","voted":true,"voteTime":"2019-02-17 21:10:20","hopzoneServerTime":"2019-02-17 21:37:06","status_code":"200"}
		JsonElement jelement = new JsonParser().parse(serverResponse);
		JsonObject topObject = jelement.getAsJsonObject();

		JsonPrimitive statusCodePrimitive = topObject.getAsJsonPrimitive("status_code");
		if(statusCodePrimitive.getAsInt() != 200) {
			return false;
		}

		JsonPrimitive votedPrimitive = topObject.getAsJsonPrimitive("voted");
		if(!votedPrimitive.getAsBoolean()) {
			return false;
		}

		JsonPrimitive voteTimePrimitive = topObject.getAsJsonPrimitive("voteTime");
		long voteTime;
		try {
			Date voteDate = HOP_ZONE_SERVER_DATE_FORMAT.parse(voteTimePrimitive.getAsString());
			voteTime = voteDate.getTime();
		} catch (ParseException e) {
			error("Cannot parse voteDate from HopZone.net!", e);
			return false;
		}

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
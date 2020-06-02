package votereward;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.napile.primitive.pair.IntIntPair;
import org.napile.primitive.pair.impl.IntIntPairImpl;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.Config;
import l2s.gameserver.model.entity.votereward.VoteApiService;

/**
 * @author Bonux (bonuxq@gmail.com)
 * 11.02.2019
 * Developed for L2-Scripts.com
 * Vote site handler for: l2top.ru
 **/
public class L2TopSite extends AbstractAutoRewardSite {
	private static final DateFormat L2TOP_SERVER_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static {
		L2TOP_SERVER_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
	}

	private final String apiKey;
	private final int serverUId;
	private final String serverPrefix;

	private final Pattern webVotesPattern;
	private final Pattern smsVotesPattern;
	private final Pattern voterNamePattern;

	public L2TopSite(MultiValueSet<String> parameters) {
		super(parameters);
		apiKey = parameters.getString("api_key");
		serverUId = parameters.getInteger("server_uid", 0);
		serverPrefix = parameters.getString("server_prefix", "");
		if(StringUtils.isEmpty(serverPrefix)) {
			webVotesPattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2} +\\d{2}:\\d{2}:\\d{2})\t+((\\S+))\\s+", Pattern.DOTALL | Pattern.MULTILINE | Pattern.UNICODE_CASE);
			smsVotesPattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2} +\\d{2}:\\d{2}:\\d{2})\t+((\\S+))\t+x(\\d+)\\s+", Pattern.DOTALL | Pattern.MULTILINE | Pattern.UNICODE_CASE);
			voterNamePattern = Pattern.compile("^(" + Config.CNAME_TEMPLATE + ")$", Pattern.UNICODE_CASE);
		} else {
			webVotesPattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2} +\\d{2}:\\d{2}:\\d{2})\t+(" + serverPrefix + "-(\\S+))\\s+", Pattern.DOTALL | Pattern.MULTILINE | Pattern.UNICODE_CASE);
			smsVotesPattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2} +\\d{2}:\\d{2}:\\d{2})\t+(" + serverPrefix + "-(\\S+))\t+x(\\d+)\\s+", Pattern.DOTALL | Pattern.MULTILINE | Pattern.UNICODE_CASE);
			voterNamePattern = Pattern.compile("^" + serverPrefix + "-(" + Config.CNAME_TEMPLATE + ")$", Pattern.UNICODE_CASE);
		}
	}

	@Override
	public boolean isEnabled() {
		if(StringUtils.isEmpty(apiKey) || serverUId == 0)
			return false;
		return super.isEnabled();
	}

	@Override
	protected void parseVotes(Map<String, List<IntIntPair>> votesCache) {
		parseVotes(votesCache, false);
		parseVotes(votesCache, true);
	}

	private void parseVotes(Map<String, List<IntIntPair>> votesCache, boolean sms) {
		String serverResponse = VoteApiService.getApiResponse(String.format("http://l2top.ru/editServ/?adminAct=lastVotes&uid=%d_%s&key=%s", serverUId, sms ? "sms" : "web", apiKey));
		if (serverResponse != null) {
			Matcher m = sms ? smsVotesPattern.matcher(serverResponse) : webVotesPattern.matcher(serverResponse);
			while (m.find()) {
				Date voteDate;
				try {
					voteDate = L2TOP_SERVER_DATE_FORMAT.parse(m.group(1));
				}
				catch (Exception e) {
					error(String.format("Cannot parse voting date: %s", m.group(1)), e);
					continue;
				}

				Matcher voterNameMatcher = voterNamePattern.matcher(m.group(2));
				if(!voterNameMatcher.find())
					continue;

				String identifier = voterNameMatcher.group(1);
				List<IntIntPair> votes = votesCache.computeIfAbsent(identifier.toLowerCase(), list -> new ArrayList<>());
				votes.add(new IntIntPairImpl((int) (voteDate.getTime() / 1000), sms ? Integer.parseInt(m.group(4)) : 1));
			}
		}
	}
}
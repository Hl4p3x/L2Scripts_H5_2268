package votereward;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.napile.primitive.pair.IntIntPair;
import org.napile.primitive.pair.impl.IntIntPairImpl;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.votereward.VoteApiService;

/**
 * @author Bonux (bonuxq@gmail.com)
 * 11.02.2019
 * Developed for L2-Scripts.com
 * Vote site handler for: mmotop.ru
 **/
public class MmoVoteSite extends AbstractAutoRewardSite {
	private static final DateFormat MMOVOTE_SERVER_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");

	private final Pattern VOTES_PATTERN = Pattern.compile("(\\d+)\t+(\\d{4}-\\d{2}-\\d{2} +\\d{2}:\\d{2}:\\d{2} +([a-zA-Z]{3}(-\\s{1,2})?))\t+(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\t+(\\S+)\t+(\\d+)\\s+", Pattern.DOTALL | Pattern.MULTILINE | Pattern.UNICODE_CASE);

	private final String serverVotesLink;
	private final String identifierType;

	public MmoVoteSite(MultiValueSet<String> parameters) {
		super(parameters);
		serverVotesLink = parameters.getString("votes_link");
		identifierType = parameters.getString("identifier_type");
	}

	@Override
	public boolean isEnabled() {
		if(StringUtils.isEmpty(serverVotesLink))
			return false;
		return super.isEnabled();
	}

	@Override
	protected String getIdentifier(Player player) {
		if(identifierType.equalsIgnoreCase("ip")) {
			return player.getIP();
		}
		return player.getName();
	}

	@Override
	protected void parseVotes(Map<String, List<IntIntPair>> votesCache) {
		String serverResponse = VoteApiService.getApiResponse(String.format("http://stat.mmovote.ru/ru/stat/%s", serverVotesLink));
		if (serverResponse != null) {
			Matcher m = VOTES_PATTERN.matcher(serverResponse);
			while (m.find()) {
				Date voteDate;
				try {
					voteDate = MMOVOTE_SERVER_DATE_FORMAT.parse(m.group(2));
				}
				catch (Exception e) {
					error(String.format("Cannot parse voting date: %s", m.group(2)), e);
					continue;
				}

				String identifier;
				if(identifierType.equalsIgnoreCase("ip")) {
					identifier = m.group(5);
				}
				else {
					identifier = m.group(6);
				}

				List<IntIntPair> votes = votesCache.computeIfAbsent(identifier.toLowerCase(), list -> new ArrayList<>());
				votes.add(new IntIntPairImpl((int) (voteDate.getTime() / 1000), Integer.parseInt(m.group(7))));
			}
		}
	}
}
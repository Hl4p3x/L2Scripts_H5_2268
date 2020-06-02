package l2s.gameserver.config.xml.holder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.model.entity.votereward.VoteRewardSite;

/**
 * @author Bonux (bonuxq@gmail.com)
 * 13.02.2019
 * Developed for L2-Scripts.com
 **/
public final class VoteRewardConfigHolder extends AbstractHolder {
	private static final VoteRewardConfigHolder INSTANCE = new VoteRewardConfigHolder();

	public static VoteRewardConfigHolder getInstance() {
		return INSTANCE;
	}

	public static String[] REWARD_COMMANDS = new String[0];

	private final Map<String, VoteRewardSite> voteRewardSites = new HashMap<>();

	public void addVoteRewardSite(VoteRewardSite site) {
		if(voteRewardSites.containsKey(site.getName()))
			warn(String.format("Dublicate %s Vote Site registered!", site.getName()));
		voteRewardSites.put(site.getName(), site);
	}

	public Collection<VoteRewardSite> getVoteRewardSites() {
		return voteRewardSites.values();
	}

	public void callInit() {
		for(VoteRewardSite site : voteRewardSites.values())
			site.init();
	}

	@Override
	public void log()
	{
		info(String.format("loaded %d Vote Site(s) count.", voteRewardSites.size()));
	}

	@Override
	public int size()
	{
		return voteRewardSites.size();
	}

	@Override
	public void clear()
	{
		voteRewardSites.clear();
	}
}
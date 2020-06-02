package handler.bbs.custom.communitybuffer;

import java.util.LinkedHashMap;
import java.util.Map;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.model.Player;

/**
 * @author Bonux
**/
public final class CommunityBufferHolder extends AbstractHolder {
	private static final CommunityBufferHolder _instance = new CommunityBufferHolder();

	public static CommunityBufferHolder getInstance() {
		return _instance;
	}

	private static final BuffSet AVAILABLE_SKILLS_BUFF_SET = new BuffSet(0, -1, null);
	private static final BuffSet AVAILABLE_PREMIUM_SKILLS_BUFF_SET = new BuffSet(0, -2, null);

	public static BuffSet getAvailableBuffSet(Player player) {
		if(player.hasPremiumAccount())
			return AVAILABLE_PREMIUM_SKILLS_BUFF_SET;
		return AVAILABLE_SKILLS_BUFF_SET;
	}

	private final Map<Integer, BuffSkill> availableSkills = new LinkedHashMap<>();
	private final Map<Integer, BuffSkill> availablePremiumSkills = new LinkedHashMap<>();

	private final Map<Integer, BuffSet> buffSets = new LinkedHashMap<>();

	public void addAvailableSkill(BuffSkill skill) {
		if(skill == null)
			return;

		availableSkills.put(skill.getId(), skill);
		AVAILABLE_SKILLS_BUFF_SET.getSkills().add(skill.getId());

		if(availablePremiumSkills.putIfAbsent(skill.getId(), skill) == null)
			AVAILABLE_PREMIUM_SKILLS_BUFF_SET.getSkills().add(skill.getId());
	}

	public Map<Integer, BuffSkill> getAvailableSkills() {
		return availableSkills;
	}

	public void addAvailablePremiumSkill(BuffSkill skill) {
		if(skill == null)
			return;

		availablePremiumSkills.put(skill.getId(), skill);
		AVAILABLE_PREMIUM_SKILLS_BUFF_SET.getSkills().add(skill.getId());
	}

	public Map<Integer, BuffSkill> getAvailablePremiumSkills() {
		return availablePremiumSkills;
	}

	public Map<Integer, BuffSkill> getAvailableSkills(Player player) {
		if(player.hasPremiumAccount())
			return availablePremiumSkills;
		return availableSkills;
	}

	public void addBuffSet(BuffSet buffSet) {
		buffSets.put(buffSet.getId(), buffSet);
	}

	public Map<Integer, BuffSet> getBuffSets() {
		return buffSets;
	}

	public void log()
	{
		info(String.format("loaded %d %s(s) count.", availableSkills.size(), "available skill"));
		info(String.format("loaded %d %s(s) count.", availablePremiumSkills.size(), "available premium skill"));
		info(String.format("loaded %d %s(s) count.", buffSets.size(), "buff set"));
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public void clear() {
		AVAILABLE_SKILLS_BUFF_SET.getSkills().clear();
		AVAILABLE_PREMIUM_SKILLS_BUFF_SET.getSkills().clear();
		availableSkills.clear();
		availablePremiumSkills.clear();
		buffSets.clear();
	}
}
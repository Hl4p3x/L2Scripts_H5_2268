package handler.bbs.custom.communitybuffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import l2s.gameserver.utils.Language;

/**
 * @author Bonux (bonuxq@gmail.com)
 * 06.03.2019
 * Developed for L2-Scripts.com
 **/
public class BuffSet {
	private final int ownerId;
	private final int id;
	private final String name;
	private final Map<Language, String> names = new HashMap<>();
	private final Set<Integer> skills = new LinkedHashSet<>();

	public BuffSet(int ownerId, int id, String name) {
		this.ownerId = ownerId;
		this.id = id;
		this.name = name;
	}

	public int getOwnerId() {
		return ownerId;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void addName(Language lang, String name) {
		names.put(lang, name);
	}

	public String getName(Language lang) {
		String name = this.name;
		if(name == null)
			name = names.get(lang);
		if(name == null) {
			if(lang == Language.ENGLISH)
				name = names.get(Language.RUSSIAN);
			else
				name = names.get(Language.ENGLISH);
		}
		if(name == null)
			return "ID: " + getId();
		return name;
	}

	public Set<Integer> getSkills() {
		return skills;
	}

	public List<BuffSkill> getBuffSkills(Map<Integer, BuffSkill> availableSkills) {
		List<BuffSkill> buffSkills = new ArrayList<>();
		for(int skillId : getSkills()) {
			BuffSkill skill = availableSkills.get(skillId);
			if(skill != null)
				buffSkills.add(skill);
		}
		return buffSkills;
	}

	public static int findId(Map<Integer, BuffSet> buffSets) {
		int topKey = 1;
		for(int key : buffSets.keySet()) {
			if(key > topKey)
				topKey = key;
		}
		topKey++;
		return topKey;
	}
}

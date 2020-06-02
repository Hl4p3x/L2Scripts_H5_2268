package l2s.gameserver.model.instances;

import l2s.commons.util.Rnd;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.PlaySoundPacket;
import l2s.gameserver.templates.npc.NpcTemplate;
/**
 * @reworked SanyaDC
 */

public class TreasureChestInstance extends MonsterInstance {
	private static final int TREASURE_BOMB_SKILL_ID = 4143;
	private static final int UNLOCK_SKILL_ID = 27;
	private int COMMON_TREASURE_OPEN_CHANCE = 25;

	public TreasureChestInstance(int objectId, NpcTemplate template) {
		super(objectId, template);
	}

	public void tryOpen(Player player, Skill skill) {
		int chance = calcChance(player, skill);
		if (Rnd.chance(chance)) {
			getAggroList().addDamageHate(player, 10000, 0);
			doDie(player);
		} else {
			fakeOpen(player);
			player.sendPacket(PlaySoundPacket.BROKEN_KEY);
		}
	}

	public int calcChance(Player player, Skill skill) {
		int skillId = skill.getId();
		int skillLvl = skill.getLevel();

		int npcLvl = getLevel();
		int playerLvl = player.getLevel();

		int npcLvlDiff = npcLvl - playerLvl;
		int baseDiff = playerLvl <= 77 ? 6 : 5;
		//Если сундук старше персонажа более чем на baseDiff, тогда награды не даем (не открываемся).
		if (isCommonTreasureChest() && npcLvlDiff >= baseDiff)
			return 0;

			if (skillId == UNLOCK_SKILL_ID) {
			int baseChance = 25;

			if (npcLvl > 20 && npcLvl < 27 && skillLvl == 1)
				baseChance = 100;
			if (npcLvl > 20 && npcLvl < 31 && skillLvl == 2)
				baseChance = 100;
			if (npcLvl > 21 && npcLvl < 35 && skillLvl == 3)
				baseChance = 100;
			if (npcLvl > 25 && npcLvl < 39 && skillLvl == 4)
				baseChance = 100;
			if (npcLvl > 29 && npcLvl < 43 && skillLvl == 5)
				baseChance = 100;
			if (npcLvl > 33 && npcLvl < 47 && skillLvl == 6)
				baseChance = 100;
			if (npcLvl > 36 && npcLvl < 50 && skillLvl == 7)
				baseChance = 100;
			if (npcLvl > 39 && npcLvl < 53 && skillLvl == 8)
				baseChance = 100;
			if (npcLvl > 45 && npcLvl < 59 && skillLvl == 9)
				baseChance = 100;
			if (npcLvl > 48 && npcLvl < 62 && skillLvl == 10)
				baseChance = 100;
			if (npcLvl > 53 && npcLvl < 67 && skillLvl == 11)
				baseChance = 100;
			if (npcLvl > 57 && npcLvl < 71 && skillLvl == 12)
				baseChance = 100;
			if (npcLvl > 61 && npcLvl < 75 && skillLvl == 13)
				baseChance = 100;
			if (npcLvl > 65 && npcLvl < 79 && skillLvl == 14)
				baseChance = 100;

			if (isCommonTreasureChest()) {
				if (baseChance > COMMON_TREASURE_OPEN_CHANCE)
					COMMON_TREASURE_OPEN_CHANCE = baseChance;
			}

		int chance = baseChance;
		return chance;
		}

	return skill.getActivateRate();
	}

	private void fakeOpen(Creature opener) {
		Skill bomb = SkillHolder.getInstance().getSkill(TREASURE_BOMB_SKILL_ID, getBombLvl());
		if (bomb != null)
			doCast(bomb, opener, false);
		onDecay();
	}

	private int getBombLvl() {
		return getLevel() / 10;
	}

	private boolean isCommonTreasureChest() {
		int npcId = getNpcId();
		if (npcId >= 18265 && npcId <= 18286)
			return true;
		return false;
	}

	@Override
	public void onReduceCurrentHp(final double damage, final Creature attacker, Skill skill, final boolean awake, final boolean standUp, boolean directHp) {
		if (!isCommonTreasureChest())
			fakeOpen(attacker);
	}

	@Override
	public boolean canChampion() {
		return false;
	}
}
package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _646_SignsOfRevolt extends QuestScript
{
	// NPCs
	private static int TORRANT = 32016;
	// Mobs
	private static int Ragna_Orc = 22029; // First in Range
	private static int Ragna_Orc_Sorcerer = 22044; // Last in Range
	private static int Guardian_of_the_Ghost_Town = 22047;
	private static int Varangkas_Succubus = 22049;
	// Items
	private static int Steel = 1880;
	private static int Coarse_Bone_Powder = 1881;
	private static int Leather = 1882;
	// Quest Items
	private static int CURSED_DOLL = 8087;
	// Chances
	private static int CURSED_DOLL_Chance = 75;

	public _646_SignsOfRevolt()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(TORRANT);
		for(int Ragna_Orc_id = Ragna_Orc; Ragna_Orc_id <= Ragna_Orc_Sorcerer; Ragna_Orc_id++)
			addKillId(Ragna_Orc_id);
		addKillId(Guardian_of_the_Ghost_Town);
		addKillId(Varangkas_Succubus);
		addQuestItem(CURSED_DOLL);
	}

	private static String doReward(QuestState st, int reward_id, int _count)
	{
		if(st.getQuestItemsCount(CURSED_DOLL) < 180)
			return null;
		st.takeItems(CURSED_DOLL, -1);
		st.giveItems(reward_id, _count, false, false);
		st.finishQuest();
		return "torant_q0646_0202.htm";
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("torant_q0646_0103.htm"))
			st.setCond(1);
		else if(event.equalsIgnoreCase("reward_adena"))
			return doReward(st, ADENA_ID, 21600);
		else if(event.equalsIgnoreCase("reward_cbp"))
			return doReward(st, Coarse_Bone_Powder, 12);
		else if(event.equalsIgnoreCase("reward_steel"))
			return doReward(st, Steel, 9);
		else if(event.equalsIgnoreCase("reward_leather"))
			return doReward(st, Leather, 20);

		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		if(npc.getNpcId() != TORRANT)
			return htmltext;

		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() < 40)
				htmltext = "torant_q0646_0102.htm";
			else
				htmltext = "torant_q0646_0101.htm";
		}
		else
			htmltext = st.getQuestItemsCount(CURSED_DOLL) >= 180 ? "torant_q0646_0105.htm" : "torant_q0646_0106.htm";

		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		Player player = qs.getRandomPartyMember(1, Config.ALT_PARTY_DISTRIBUTION_RANGE);
		if(player == null)
			return null;

		QuestState st = player.getQuestState(qs.getQuest());
		if(st == null)
			return null;

		long CURSED_DOLL_COUNT = st.getQuestItemsCount(CURSED_DOLL);
		if(CURSED_DOLL_COUNT < 180 && Rnd.chance(CURSED_DOLL_Chance))
		{
			st.giveItems(CURSED_DOLL, 1, true, true);
			if(st.getQuestItemsCount(CURSED_DOLL) > 179)
				st.setCond(2);
			else
				st.playSound(SOUND_ITEMGET);
		}
		return null;
	}
}
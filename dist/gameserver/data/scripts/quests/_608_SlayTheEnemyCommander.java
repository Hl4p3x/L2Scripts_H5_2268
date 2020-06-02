package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _608_SlayTheEnemyCommander extends QuestScript
{
	// npc
	private static final int KADUN_ZU_KETRA = 31370;
	private static final int VARKAS_COMMANDER_MOS = 25312;

	//quest items
	private static final int HEAD_OF_MOS = 7236;
	private static final int TOTEM_OF_WISDOM = 7220;
	@SuppressWarnings("unused")
	private static final int MARK_OF_KETRA_ALLIANCE1 = 7211;
	@SuppressWarnings("unused")
	private static final int MARK_OF_KETRA_ALLIANCE2 = 7212;
	@SuppressWarnings("unused")
	private static final int MARK_OF_KETRA_ALLIANCE3 = 7213;
	private static final int MARK_OF_KETRA_ALLIANCE4 = 7214;
	private static final int MARK_OF_KETRA_ALLIANCE5 = 7215;

	public _608_SlayTheEnemyCommander()
	{
		super(PARTY_ONE, REPEATABLE);
		addStartNpc(KADUN_ZU_KETRA);
		addKillId(VARKAS_COMMANDER_MOS);
		addQuestItem(HEAD_OF_MOS);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("quest_accept"))
		{
			htmltext = "elder_kadun_zu_ketra_q0608_0104.htm";
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("608_3"))
			if(st.getQuestItemsCount(HEAD_OF_MOS) >= 1)
			{
				htmltext = "elder_kadun_zu_ketra_q0608_0201.htm";
				st.takeItems(HEAD_OF_MOS, -1);
				st.giveItems(TOTEM_OF_WISDOM, 1, false, false);
				st.addExpAndSp(0, 10000);
				st.unset("cond");
				st.finishQuest();
			}
			else
				htmltext = "elder_kadun_zu_ketra_q0608_0106.htm";
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 75)
			{
				if(st.getQuestItemsCount(MARK_OF_KETRA_ALLIANCE4) >= 1 || st.getQuestItemsCount(MARK_OF_KETRA_ALLIANCE5) >= 1)
					htmltext = "elder_kadun_zu_ketra_q0608_0101.htm";
				else
					htmltext = "elder_kadun_zu_ketra_q0608_0102.htm";
			}
			else
				htmltext = "elder_kadun_zu_ketra_q0608_0103.htm";
		}
		else if(cond == 1 && st.getQuestItemsCount(HEAD_OF_MOS) == 0)
			htmltext = "elder_kadun_zu_ketra_q0608_0106.htm";
		else if(cond == 2 && st.getQuestItemsCount(HEAD_OF_MOS) >= 1)
			htmltext = "elder_kadun_zu_ketra_q0608_0105.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1)
		{
			st.giveItems(HEAD_OF_MOS, 1, false, false);
			st.setCond(2);
			st.playSound(SOUND_ITEMGET);
		}
		return null;
	}
}
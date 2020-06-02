package quests;

import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _007_ATripBegins extends QuestScript
{
	int MIRABEL = 30146;
	int ARIEL = 30148;
	int ASTERIOS = 30154;

	int ARIELS_RECOMMENDATION = 7572;

	int SCROLL_OF_ESCAPE_GIRAN = 7126;
	int MARK_OF_TRAVELER = 7570;

	public _007_ATripBegins()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(MIRABEL);

		addTalkId(MIRABEL);
		addTalkId(ARIEL);
		addTalkId(ASTERIOS);

		addQuestItem(ARIELS_RECOMMENDATION);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("mint_q0007_0104.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("ariel_q0007_0201.htm"))
		{
			st.giveItems(ARIELS_RECOMMENDATION, 1, false, false);
			st.setCond(2);
		}
		else if(event.equalsIgnoreCase("ozzy_q0007_0301.htm"))
		{
			st.takeItems(ARIELS_RECOMMENDATION, -1);
			st.setCond(3);
		}
		else if(event.equalsIgnoreCase("mint_q0007_0401.htm"))
		{
			st.giveItems(SCROLL_OF_ESCAPE_GIRAN, 1, false, false);
			st.giveItems(MARK_OF_TRAVELER, 1, false, false);
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == MIRABEL)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() == Race.ELF && st.getPlayer().getLevel() >= 3)
					htmltext = "mint_q0007_0101.htm";
				else
					htmltext = "mint_q0007_0102.htm";
			}
			else if(cond == 1)
				htmltext = "mint_q0007_0105.htm";
			else if(cond == 3)
				htmltext = "mint_q0007_0301.htm";
		}
		else if(npcId == ARIEL)
		{
			if(cond == 1 && st.getQuestItemsCount(ARIELS_RECOMMENDATION) == 0)
				htmltext = "ariel_q0007_0101.htm";
			else if(cond == 2)
				htmltext = "ariel_q0007_0202.htm";
		}
		else if(npcId == ASTERIOS)
			if(cond == 2 && st.getQuestItemsCount(ARIELS_RECOMMENDATION) > 0)
				htmltext = "ozzy_q0007_0201.htm";
			else if(cond == 2 && st.getQuestItemsCount(ARIELS_RECOMMENDATION) == 0)
				htmltext = "ozzy_q0007_0302.htm";
			else if(cond == 3)
				htmltext = "ozzy_q0007_0303.htm";
		return htmltext;
	}
}
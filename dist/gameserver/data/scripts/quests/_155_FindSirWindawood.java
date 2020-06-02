package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _155_FindSirWindawood extends QuestScript
{
	int OFFICIAL_LETTER = 1019;
	int HASTE_POTION = 734;

	public _155_FindSirWindawood()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(30042);

		addTalkId(30042);
		addTalkId(30311);

		addQuestItem(OFFICIAL_LETTER);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("30042-04.htm"))
		{
			st.giveItems(OFFICIAL_LETTER, 1, false, false);
			st.setCond(1);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(npcId == 30042)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 3)
				{
					htmltext = "30042-03.htm";
					return htmltext;
				}
				htmltext = "30042-02.htm";
			}
			else if(cond == 1 && st.getQuestItemsCount(OFFICIAL_LETTER) >= 1)
				htmltext = "30042-05.htm";
		}
		else if(npcId == 30311 && cond == 1 && st.getQuestItemsCount(OFFICIAL_LETTER) >= 1)
		{
			htmltext = "30311-01.htm";
			st.takeItems(OFFICIAL_LETTER, -1);
			st.giveItems(HASTE_POTION, 1, false, false);
			st.finishQuest();
		}
		return htmltext;
	}
}
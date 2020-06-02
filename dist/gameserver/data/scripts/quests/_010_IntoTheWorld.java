package quests;

import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _010_IntoTheWorld extends QuestScript
{
	int VERY_EXPENSIVE_NECKLACE = 7574;
	int SCROLL_OF_ESCAPE_GIRAN = 7126;
	int MARK_OF_TRAVELER = 7570;

	int BALANKI = 30533;
	int REED = 30520;
	int GERALD = 30650;

	public _010_IntoTheWorld()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(BALANKI);

		addTalkId(BALANKI);
		addTalkId(REED);
		addTalkId(GERALD);

		addQuestItem(VERY_EXPENSIVE_NECKLACE);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("elder_balanki_q0010_0104.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("warehouse_chief_reed_q0010_0201.htm"))
		{
			st.giveItems(VERY_EXPENSIVE_NECKLACE, 1, false, false);
			st.setCond(2);
		}
		else if(event.equalsIgnoreCase("gerald_priest_of_earth_q0010_0301.htm"))
		{
			st.takeItems(VERY_EXPENSIVE_NECKLACE, -1);
			st.setCond(3);
		}
		else if(event.equalsIgnoreCase("warehouse_chief_reed_q0010_0401.htm"))
		{
			st.setCond(4);
		}
		else if(event.equalsIgnoreCase("elder_balanki_q0010_0501.htm"))
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
		if(npcId == BALANKI)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() == Race.DWARF && st.getPlayer().getLevel() >= 3)
					htmltext = "elder_balanki_q0010_0101.htm";
				else
					htmltext = "elder_balanki_q0010_0102.htm";
			}
			else if(cond == 1)
				htmltext = "elder_balanki_q0010_0105.htm";
			else if(cond == 4)
				htmltext = "elder_balanki_q0010_0401.htm";
		}
		else if(npcId == REED)
		{
			if(cond == 1)
				htmltext = "warehouse_chief_reed_q0010_0101.htm";
			else if(cond == 2)
				htmltext = "warehouse_chief_reed_q0010_0202.htm";
			else if(cond == 3)
				htmltext = "warehouse_chief_reed_q0010_0301.htm";
			else if(cond == 4)
				htmltext = "warehouse_chief_reed_q0010_0402.htm";
		}
		else if(npcId == GERALD)
			if(cond == 2 && st.getQuestItemsCount(VERY_EXPENSIVE_NECKLACE) > 0)
				htmltext = "gerald_priest_of_earth_q0010_0201.htm";
			else if(cond == 3)
				htmltext = "gerald_priest_of_earth_q0010_0302.htm";
			else
				htmltext = "gerald_priest_of_earth_q0010_0303.htm";
		return htmltext;
	}
}
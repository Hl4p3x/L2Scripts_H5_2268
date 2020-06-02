package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _018_MeetingwiththeGoldenRam extends QuestScript
{
	private static final int SUPPLY_BOX = 7245;

	public _018_MeetingwiththeGoldenRam()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(31314);

		addTalkId(31314);
		addTalkId(31315);
		addTalkId(31555);

		addQuestItem(SUPPLY_BOX);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("warehouse_chief_donal_q0018_0104.htm"))
		{
			st.setCond(1);
		}
		else if(event.equals("freighter_daisy_q0018_0201.htm"))
		{
			st.setCond(2);
			st.giveItems(SUPPLY_BOX, 1, false, false);
		}
		else if(event.equals("supplier_abercrombie_q0018_0301.htm"))
		{
			st.takeItems(SUPPLY_BOX, -1);
			st.addExpAndSp(126668, 11731);
			st.giveItems(ADENA_ID, 40000, true, true);
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
		if(npcId == 31314)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 66)
					htmltext = "warehouse_chief_donal_q0018_0101.htm";
				else
					htmltext = "warehouse_chief_donal_q0018_0103.htm";
			}
			else if(cond == 1)
				htmltext = "warehouse_chief_donal_q0018_0105.htm";
		}
		else if(npcId == 31315)
		{
			if(cond == 1)
				htmltext = "freighter_daisy_q0018_0101.htm";
			else if(cond == 2)
				htmltext = "freighter_daisy_q0018_0202.htm";
		}
		else if(npcId == 31555)
			if(cond == 2 && st.getQuestItemsCount(SUPPLY_BOX) >= 1)
				htmltext = "supplier_abercrombie_q0018_0201.htm";
		return htmltext;
	}
}
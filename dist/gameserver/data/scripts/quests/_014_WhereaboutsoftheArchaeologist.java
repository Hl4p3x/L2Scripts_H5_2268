package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _014_WhereaboutsoftheArchaeologist extends QuestScript
{
	private static final int LETTER_TO_ARCHAEOLOGIST = 7253;

	public _014_WhereaboutsoftheArchaeologist()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(31263);
		addTalkId(31538);

		addQuestItem(LETTER_TO_ARCHAEOLOGIST);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("trader_liesel_q0014_0104.htm"))
		{
			st.setCond(1);
			st.giveItems(LETTER_TO_ARCHAEOLOGIST, 1, false, false);
		}
		else if(event.equalsIgnoreCase("explorer_ghost_a_q0014_0201.htm"))
		{
			st.takeItems(LETTER_TO_ARCHAEOLOGIST, -1);
			st.addExpAndSp(325881, 32524);
			st.giveItems(ADENA_ID, 136928, true, true);
			st.finishQuest();
			return "explorer_ghost_a_q0014_0201.htm";
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 31263)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 74)
					htmltext = "trader_liesel_q0014_0101.htm";
				else
					htmltext = "trader_liesel_q0014_0103.htm";
			}
			else if(cond == 1)
				htmltext = "trader_liesel_q0014_0104.htm";
		}
		else if(npcId == 31538)
			if(cond == 1 && st.getQuestItemsCount(LETTER_TO_ARCHAEOLOGIST) >= 1)
				htmltext = "explorer_ghost_a_q0014_0101.htm";
		return htmltext;
	}
}
package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _303_CollectArrowheads extends QuestScript
{
	int ORCISH_ARROWHEAD = 963;

	public _303_CollectArrowheads()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(30029);

		addTalkId(30029);

		addKillId(20361);

		addQuestItem(ORCISH_ARROWHEAD);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("minx_q0303_04.htm"))
		{
			st.setCond(1);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();

		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 10)
				htmltext = "minx_q0303_03.htm";
			else
				htmltext = "minx_q0303_02.htm";
		}
		else if(st.getQuestItemsCount(ORCISH_ARROWHEAD) < 10)
			htmltext = "minx_q0303_05.htm";
		else
		{
			st.takeItems(ORCISH_ARROWHEAD, -1);
			st.giveItems(ADENA_ID, 1000, true, true);
			st.addExpAndSp(2000, 0);
			htmltext = "minx_q0303_06.htm";
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getQuestItemsCount(ORCISH_ARROWHEAD) < 10)
		{
			st.giveItems(ORCISH_ARROWHEAD, 1, true, true);
			if(st.getQuestItemsCount(ORCISH_ARROWHEAD) >= 10)
			{
				st.setCond(2);
			}
			else
				st.playSound(SOUND_ITEMGET);
		}
		return null;
	}
}
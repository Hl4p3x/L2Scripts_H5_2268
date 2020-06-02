package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _10277_MutatedKaneusDion extends QuestScript
{

	// NPCs
	private static final int Lucas = 30071;
	private static final int Mirien = 30461;

	// MOBs
	private static final int CrimsonHatuOtis = 18558;
	private static final int SeerFlouros = 18559;

	// Items
	private static final int Tissue1 = 13832;
	private static final int Tissue2 = 13833;

	public _10277_MutatedKaneusDion()
	{
		super(PARTY_ONE, ONETIME);
		addStartNpc(Lucas);
		addTalkId(Mirien);
		addKillId(CrimsonHatuOtis, SeerFlouros);
		addQuestItem(Tissue1, Tissue2);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("30071-03.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("30461-02.htm"))
		{
			st.giveItems(57, 20000, true, true);
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
		if(npcId == Lucas)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 28)
					htmltext = "30071-01.htm";
				else
					htmltext = "30071-00.htm";
			}
			else if(cond == 1)
				htmltext = "30071-04.htm";
			else if(cond == 2)
				htmltext = "30071-05.htm";
		}
		else if(npcId == Mirien)
		{
			if(cond == 1)
				htmltext = "30461-01a.htm";
			else if(cond == 2)
				htmltext = "30461-01.htm";
		}
		return htmltext;
	}

	@Override
	public String onCompleted(NpcInstance npc, QuestState st)
	{
		String htmltext = COMPLETED_DIALOG;
		int npcId = npc.getNpcId();
		if(npcId == Lucas)
			htmltext = "30071-0a.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1)
		{
			st.giveItems(Tissue1, 1, false, false);
			st.giveItems(Tissue2, 1, false, false);
			st.setCond(2);
		}
		return null;
	}
}
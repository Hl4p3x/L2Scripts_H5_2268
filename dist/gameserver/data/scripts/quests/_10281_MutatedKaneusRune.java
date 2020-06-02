package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _10281_MutatedKaneusRune extends QuestScript
{

	// NPCs
	private static final int Mathias = 31340;
	private static final int Kayan = 31335;

	// MOBs
	private static final int WhiteAllosce = 18577;

	// Items
	private static final int Tissue = 13840;

	public _10281_MutatedKaneusRune()
	{
		super(PARTY_ONE, ONETIME);
		addStartNpc(Mathias);
		addTalkId(Kayan);
		addKillId(WhiteAllosce);
		addQuestItem(Tissue);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("31340-03.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("31335-02.htm"))
		{
			st.giveItems(57, 360000, true, true);
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
		if(npcId == Mathias)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 68)
					htmltext = "31340-01.htm";
				else
					htmltext = "31340-00.htm";
			}
			else if(cond == 1)
				htmltext = "31340-04.htm";
			else if(cond == 2)
				htmltext = "31340-05.htm";
		}
		else if(npcId == Kayan)
		{
			if(cond == 1)
				htmltext = "31335-01a.htm";
			else if(cond == 2)
				htmltext = "31335-01.htm";
		}
		return htmltext;
	}

	@Override
	public String onCompleted(NpcInstance npc, QuestState st)
	{
		String htmltext = COMPLETED_DIALOG;
		int npcId = npc.getNpcId();
		if(npcId == Mathias)
			htmltext = "31340-0a.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1)
		{
			st.giveItems(Tissue, 1, false, false);
			st.setCond(2);
		}
		return null;
	}
}
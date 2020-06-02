package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _10280_MutatedKaneusSchuttgart extends QuestScript
{

	// NPCs
	private static final int Vishotsky = 31981;
	private static final int Atraxia = 31972;

	// MOBs
	private static final int VenomousStorace = 18571;
	private static final int KelBilette = 18573;

	// Items
	private static final int Tissue1 = 13838;
	private static final int Tissue2 = 13839;

	public _10280_MutatedKaneusSchuttgart()
	{
		super(PARTY_ONE, ONETIME);
		addStartNpc(Vishotsky);
		addTalkId(Atraxia);
		addKillId(VenomousStorace, KelBilette);
		addQuestItem(Tissue1, Tissue2);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("31981-03.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("31972-02.htm"))
		{
			st.giveItems(57, 300000, true, true);
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
		if(npcId == Vishotsky)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 58)
					htmltext = "31981-01.htm";
				else
					htmltext = "31981-00.htm";
			}
			else if(cond == 1)
				htmltext = "31981-04.htm";
			else if(cond == 2)
				htmltext = "31981-05.htm";
		}
		else if(npcId == Atraxia)
		{
			if(cond == 1)
				htmltext = "31972-01a.htm";
			else if(cond == 2)
				htmltext = "31972-01.htm";
		}
		return htmltext;
	}

	@Override
	public String onCompleted(NpcInstance npc, QuestState st)
	{
		String htmltext = COMPLETED_DIALOG;
		int npcId = npc.getNpcId();
		if(npcId == Vishotsky)
			htmltext = "31981-0a.htm";
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
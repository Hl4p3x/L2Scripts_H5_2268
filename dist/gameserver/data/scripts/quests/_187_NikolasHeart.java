package quests;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _187_NikolasHeart extends QuestScript
{
	private static final int Kusto = 30512;
	private static final int Lorain = 30673;
	private static final int Nikola = 30621;

	private static final int Certificate = 10362;
	private static final int Metal = 10368;

	public _187_NikolasHeart()
	{
		super(PARTY_NONE, ONETIME);

		addTalkId(Kusto, Nikola, Lorain);
		addFirstTalkId(Lorain);
		addQuestItem(Certificate, Metal);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("researcher_lorain_q0187_03.htm"))
		{
			st.setCond(1);
			st.takeItems(Certificate, -1);
			st.giveItems(Metal, 1, false, false);
		}
		else if(event.equalsIgnoreCase("maestro_nikola_q0187_03.htm"))
		{
			st.setCond(2);
		}
		else if(event.equalsIgnoreCase("head_blacksmith_kusto_q0187_03.htm"))
		{
			st.giveItems(ADENA_ID, 93383, true, true);
			st.addExpAndSp(285935, 18711);
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
		if(npcId == Lorain)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 41)
					htmltext = "researcher_lorain_q0187_02.htm";
				else
					htmltext = "researcher_lorain_q0187_01.htm";
			}
			else if(cond == 1)
				htmltext = "researcher_lorain_q0187_04.htm";
		}
		else if(npcId == Nikola)
		{
			if(cond == 1)
				htmltext = "maestro_nikola_q0187_01.htm";
			else if(cond == 2)
				htmltext = "maestro_nikola_q0187_04.htm";
		}
		else if(npcId == Kusto)
		{
			if(cond == 2)
				htmltext = "head_blacksmith_kusto_q0187_01.htm";
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(NpcInstance npc, Player player)
	{
		if(player.isQuestCompleted(185) && player.getQuestState(this) == null)
			newQuestState(player);
		return "";
	}
}
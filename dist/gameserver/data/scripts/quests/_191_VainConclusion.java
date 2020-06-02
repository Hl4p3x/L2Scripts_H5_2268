package quests;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _191_VainConclusion extends QuestScript
{
	private static final int Kusto = 30512;
	private static final int Lorain = 30673;
	private static final int Dorothy = 30970;
	private static final int Shegfield = 30068;

	private static final int Metal = 10371;

	public _191_VainConclusion()
	{
		super(PARTY_NONE, ONETIME);

		addTalkId(Kusto, Dorothy, Lorain, Shegfield);
		addFirstTalkId(Dorothy);
		addQuestItem(Metal);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("30970-03.htm"))
		{
			st.setCond(1);
			st.giveItems(Metal, 1, false, false);
		}
		else if(event.equalsIgnoreCase("30673-02.htm"))
		{
			st.setCond(2);
			st.takeItems(Metal, -1);
		}
		else if(event.equalsIgnoreCase("30068-03.htm"))
		{
			st.setCond(3);
		}
		else if(event.equalsIgnoreCase("30512-02.htm"))
		{
			st.giveItems(ADENA_ID, 117327, true, true);
			st.addExpAndSp(309467, 20614);
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
		if(npcId == Dorothy)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 42)
					htmltext = "30970-00.htm";
				else
					htmltext = "30970-01.htm";
			}
			else if(cond == 1)
				htmltext = "30970-04.htm";
		}
		else if(npcId == Lorain)
		{
			if(cond == 1)
				htmltext = "30673-01.htm";
			else if(cond == 2)
				htmltext = "30673-03.htm";
			else if(cond == 3)
			{
				htmltext = "30673-04.htm";
				st.setCond(4);
			}
			else if(cond == 4)
				htmltext = "30673-05.htm";
		}
		else if(npcId == Shegfield)
		{
			if(cond == 2)
				htmltext = "30068-01.htm";
			else if(cond == 3)
				htmltext = "30068-04.htm";
		}
		else if(npcId == Kusto)
		{
			if(cond == 4)
				htmltext = "30512-01.htm";
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(NpcInstance npc, Player player)
	{
		if(player.isQuestCompleted(188) && player.getQuestState(this) == null)
			newQuestState(player);
		return "";
	}
}
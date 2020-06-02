package quests;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _188_SealRemoval extends QuestScript
{
	private static final int Dorothy = 30970;
	private static final int Lorain = 30673;
	private static final int Nikola = 30621;

	private static final int BrokenMetal = 10369;

	public _188_SealRemoval()
	{
		super(PARTY_NONE, ONETIME);

		addTalkId(Dorothy, Nikola, Lorain);
		addFirstTalkId(Lorain);
		addQuestItem(BrokenMetal);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("researcher_lorain_q0188_03.htm"))
		{
			st.setCond(1);
			st.giveItems(BrokenMetal, 1, false, false);
		}
		else if(event.equalsIgnoreCase("maestro_nikola_q0188_03.htm"))
		{
			st.setCond(2);
		}
		else if(event.equalsIgnoreCase("dorothy_the_locksmith_q0188_03.htm"))
		{
			st.giveItems(ADENA_ID, 98583, true, true);
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
					htmltext = "researcher_lorain_q0188_02.htm";
				else
					htmltext = "researcher_lorain_q0188_01.htm";
			}
			else if(cond == 1)
				htmltext = "researcher_lorain_q0188_04.htm";
		}
		else if(npcId == Nikola)
		{
			if(cond == 1)
				htmltext = "maestro_nikola_q0188_01.htm";
			else if(cond == 2)
				htmltext = "maestro_nikola_q0188_05.htm";
		}
		else if(npcId == Dorothy)
		{
			if(cond == 2)
				htmltext = "dorothy_the_locksmith_q0188_01.htm";
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(NpcInstance npc, Player player)
	{
		if((player.isQuestCompleted(186) || player.isQuestCompleted(187)) && player.getQuestState(this) == null)
			newQuestState(player);
		return "";
	}
}
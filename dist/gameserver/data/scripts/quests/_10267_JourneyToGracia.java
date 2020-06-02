package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _10267_JourneyToGracia extends QuestScript
{
	private final static int Orven = 30857;
	private final static int Keucereus = 32548;
	private final static int Papiku = 32564;

	private final static int Letter = 13810;

	public _10267_JourneyToGracia()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(Orven);

		addTalkId(Keucereus);
		addTalkId(Papiku);

		addQuestItem(Letter);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("30857-06.htm"))
		{
			st.setCond(1);
			st.giveItems(Letter, 1, false, false);
		}
		else if(event.equalsIgnoreCase("32564-02.htm"))
		{
			st.setCond(2);
		}
		else if(event.equalsIgnoreCase("32548-02.htm"))
		{
			st.giveItems(ADENA_ID, 92500, true, true);
			st.takeItems(Letter, -1);
			st.addExpAndSp(75480, 7570);
			st.unset("cond");
			st.finishQuest();
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Orven)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 75)
					htmltext = "30857-00.htm";
				else
					htmltext = "30857-01.htm";
			}
			else
				htmltext = "30857-07.htm";
		}
		else if(npcId == Papiku)
		{
			if(cond == 1)
				htmltext = "32564-01.htm";
			else
				htmltext = "32564-03.htm";
		}
		else if(npcId == Keucereus)
		{
			if(cond == 2)
				htmltext = "32548-01.htm";
		}
		return htmltext;
	}

	@Override
	public String onCompleted(NpcInstance npc, QuestState st)
	{
		String htmltext = COMPLETED_DIALOG;
		int npcId = npc.getNpcId();
		if(npcId == Keucereus)
			htmltext = "32548-03.htm";
		else if(npcId == Orven)
			htmltext = "30857-0a.htm";
		return htmltext;
	}
}
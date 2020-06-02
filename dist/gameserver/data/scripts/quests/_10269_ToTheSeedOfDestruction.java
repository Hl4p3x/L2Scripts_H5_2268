package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _10269_ToTheSeedOfDestruction extends QuestScript
{
	private final static int Keucereus = 32548;
	private final static int Allenos = 32526;

	private final static int Introduction = 13812;

	public _10269_ToTheSeedOfDestruction()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(Keucereus);
		addTalkId(Allenos);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("32548-05.htm"))
		{
			st.setCond(1);
			st.giveItems(Introduction, 1, false, false);
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Keucereus)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 75)
					htmltext = "32548-00.htm";
				else
					htmltext = "32548-01.htm";
			}
			else if(cond == 1)
				htmltext = "32548-06.htm";
		}
		else if(npcId == Allenos)
		{
			if(cond == 1)
			{
				htmltext = "32526-01.htm";
				st.giveItems(ADENA_ID, 29174, true, true);
				st.addExpAndSp(176121, 7671);
				st.finishQuest();
			}
		}
		return htmltext;
	}

	@Override
	public String onCompleted(NpcInstance npc, QuestState st)
	{
		String htmltext = COMPLETED_DIALOG;
		int npcId = npc.getNpcId();
		if(npcId == Allenos)
			htmltext = "32526-02.htm";
		else if(npcId == Keucereus)
			htmltext = "32548-0a.htm";
		return htmltext;
	}
}
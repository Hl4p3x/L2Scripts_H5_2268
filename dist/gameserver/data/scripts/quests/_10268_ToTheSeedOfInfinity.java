package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _10268_ToTheSeedOfInfinity extends QuestScript
{
	private final static int Keucereus = 32548;
	private final static int Tepios = 32603;

	private final static int Introduction = 13811;

	public _10268_ToTheSeedOfInfinity()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(Keucereus);
		addTalkId(Tepios);
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
		else if(npcId == Tepios)
		{
			if(cond == 1)
			{
				htmltext = "32530-01.htm";
				st.giveItems(ADENA_ID, 16671, true, true);
				st.addExpAndSp(100640, 10098);
				st.finishQuest();
			}
		}
		return htmltext;
	}
}
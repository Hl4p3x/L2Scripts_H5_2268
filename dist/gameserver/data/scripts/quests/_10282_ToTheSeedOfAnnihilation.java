package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _10282_ToTheSeedOfAnnihilation extends QuestScript
{
	private final static int KBALDIR = 32733;
	private final static int KLEMIS = 32734;

	private final static int SOA_ORDERS = 15512;

	public _10282_ToTheSeedOfAnnihilation()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(KBALDIR);
		addTalkId(KBALDIR);
		addTalkId(KLEMIS);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("32733-07.htm"))
		{
			st.setCond(1);
			st.giveItems(SOA_ORDERS, 1, false, false);
		}
		else if(event.equalsIgnoreCase("32734-02.htm"))
		{
			st.unset("cond");
			st.addExpAndSp(1148480, 99110);
			st.giveItems(57, 212182, true, true);
			st.takeItems(SOA_ORDERS, -1);
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
		if(npcId == KBALDIR)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 84)
					htmltext = "32733-01.htm";
				else
					htmltext = "32733-00.htm";
			}
			else if(cond == 1)
				htmltext = "32733-08.htm";
		}
		else if(npcId == KLEMIS)
		{
			if(cond == 1)
				htmltext = "32734-01.htm";
		}
		return htmltext;
	}

	@Override
	public String onCompleted(NpcInstance npc, QuestState st)
	{
		String htmltext = COMPLETED_DIALOG;
		int npcId = npc.getNpcId();
		if(npcId == KBALDIR)
			htmltext = "32733-09.htm";
		else if(npcId == KLEMIS)
			htmltext = "32734-03.htm";
		return htmltext;
	}
}
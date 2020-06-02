package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _020_BringUpWithLove extends QuestScript
{
	private static final int TUNATUN = 31537;
	// Item
	private static final int BEAST_WHIP = 15473;
	private static final int CRYSTAL = 9553;
	private static final int JEWEL = 7185;

	public _020_BringUpWithLove()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(TUNATUN);
		addTalkId(TUNATUN);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(npc.getNpcId() == TUNATUN)
		{
			if(event.equalsIgnoreCase("31537-12.htm"))
			{
				st.setCond(1);
			}
			else if(event.equalsIgnoreCase("31537-03.htm"))
			{
				if(st.getQuestItemsCount(BEAST_WHIP) > 0)
					return "31537-03a.htm";
				else
					st.giveItems(BEAST_WHIP, 1, false, false);
			}
			else if(event.equalsIgnoreCase("31537-15.htm"))
			{
				st.unset("cond");
				st.takeItems(JEWEL, -1);
				st.giveItems(CRYSTAL, 1, false, false);
				st.finishQuest();
			}
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmtext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(npc.getNpcId() == TUNATUN)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 82)
					htmtext = "31537-01.htm";
				else
					htmtext = "31537-00.htm";
			}
			else if(st.getCond() == 1)
				htmtext = "31537-13.htm";
			else if(st.getCond() == 2)
				htmtext = "31537-14.htm";
		}
		return htmtext;
	}
}
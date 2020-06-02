package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _119_LastImperialPrince extends QuestScript
{

	// NPC
	private static final int SPIRIT = 31453; // Nameless Spirit
	private static final int DEVORIN = 32009; // Devorin

	// ITEM
	private static final int BROOCH = 7262; // Antique Brooch

	// REWARD
	private static final int AMOUNT = 150292; // Amount

	public _119_LastImperialPrince()
	{
		super(PARTY_NONE, ONETIME);
		addStartNpc(SPIRIT);
		addTalkId(DEVORIN);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("31453-4.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("32009-2.htm"))
		{
			if(st.getQuestItemsCount(BROOCH) < 1)
			{
				htmltext = NO_QUEST_DIALOG;
				st.abortQuest();
			}
		}
		else if(event.equalsIgnoreCase("32009-3.htm"))
		{
			st.setCond(2);
		}
		else if(event.equalsIgnoreCase("31453-7.htm"))
		{
			st.giveItems(ADENA_ID, AMOUNT, true, true);
			st.addExpAndSp(902439, 90067);
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

		if(st.getQuestItemsCount(BROOCH) < 1)
		{
			htmltext = NO_QUEST_DIALOG;
			st.abortQuest();
			return htmltext;
		}

		if(npcId == SPIRIT)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 74)
				{
					htmltext = "<html><body>Quest for characters level 74 and above.</body></html>";
					return htmltext;
				}
				return "31453-1.htm";
			}
			else if(cond == 2)
				return "31453-5.htm";
			else
				return NO_QUEST_DIALOG;
		}
		else if(npcId == DEVORIN && cond == 1)
			htmltext = "32009-1.htm";
		return htmltext;
	}
}
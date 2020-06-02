package quests;

import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _173_ToTheIsleOfSouls extends QuestScript
{

	private static final int GALLADUCCIS_ORDER_DOCUMENT_ID_1 = 7563;
	private static final int MAGIC_SWORD_HILT_ID = 7568;
	private static final int MARK_OF_TRAVELER_ID = 7570;
	private static final int SCROLL_OF_ESCAPE_KAMAEL_VILLAGE = 9647;

	public _173_ToTheIsleOfSouls()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(30097);
		addTalkId(30094);
		addTalkId(30090);
		addTalkId(30116);

		addQuestItem(new int[]{
				GALLADUCCIS_ORDER_DOCUMENT_ID_1,
				MAGIC_SWORD_HILT_ID
		});
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("1"))
		{
			st.setCond(1);
			st.giveItems(GALLADUCCIS_ORDER_DOCUMENT_ID_1, 1, false, false);
			htmltext = "30097-03.htm";
		}
		else if(event.equals("2"))
		{
			st.setCond(2);
			st.takeItems(GALLADUCCIS_ORDER_DOCUMENT_ID_1, 1);
			st.giveItems(MAGIC_SWORD_HILT_ID, 1, false, false);
			htmltext = "30094-02.htm";
		}
		else if(event.equals("3"))
		{
			st.unset("cond");
			st.takeItems(MAGIC_SWORD_HILT_ID, 1);
			st.giveItems(SCROLL_OF_ESCAPE_KAMAEL_VILLAGE, 1, false, false);
			htmltext = "30097-12.htm";
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
		if(npcId == 30097)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() == Race.KAMAEL && st.getQuestItemsCount(MARK_OF_TRAVELER_ID) > 0)
					htmltext = "30097-02.htm";
				else
					htmltext = "30097-01.htm";
			}
			else if(cond == 1)
				htmltext = "30097-04.htm";
			else if(cond == 2)
				htmltext = "30097-05.htm";
		}
		else if(npcId == 30094)
		{
			if(cond == 1)
				htmltext = "30094-01.htm";
			else if(cond == 2)
				htmltext = "30094-03.htm";
		}
		return htmltext;
	}
}
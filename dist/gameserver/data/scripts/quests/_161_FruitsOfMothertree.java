package quests;

import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _161_FruitsOfMothertree extends QuestScript
{
	private static final int ANDELLRIAS_LETTER_ID = 1036;
	private static final int MOTHERTREE_FRUIT_ID = 1037;

	public _161_FruitsOfMothertree()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(30362);
		addTalkId(30371);

		addQuestItem(new int[]{
				MOTHERTREE_FRUIT_ID,
				ANDELLRIAS_LETTER_ID
		});
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("1"))
		{
			st.set("id", "0");
			htmltext = "30362-04.htm";
			st.giveItems(ANDELLRIAS_LETTER_ID, 1, false, false);
			st.setCond(1);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		if(npcId == 30362 && st.getCond() == 0)
		{
			if(st.getPlayer().getRace() != Race.ELF)
				htmltext = "30362-00.htm";
			else if(st.getPlayer().getLevel() >= 3)
				htmltext = "30362-03.htm";
			else
				htmltext = "30362-02.htm";
		}
		else if(npcId == 30362 && st.getCond() > 0)
		{
			if(st.getQuestItemsCount(ANDELLRIAS_LETTER_ID) >= 1 && st.getQuestItemsCount(MOTHERTREE_FRUIT_ID) == 0)
				htmltext = "30362-05.htm";
			else if(st.getQuestItemsCount(MOTHERTREE_FRUIT_ID) >= 1)
			{
				htmltext = "30362-06.htm";
				st.giveItems(ADENA_ID, 1000, true, true);
				st.addExpAndSp(1000, 0);
				st.takeItems(MOTHERTREE_FRUIT_ID, 1);
				st.finishQuest();
			}
		}
		else if(npcId == 30371 && st.getCond() == 1)
			if(st.getQuestItemsCount(ANDELLRIAS_LETTER_ID) >= 1)
			{
				if(st.getInt("id") != 161)
				{
					st.set("id", "161");
					htmltext = "30371-01.htm";
					st.giveItems(MOTHERTREE_FRUIT_ID, 1, false, false);
					st.takeItems(ANDELLRIAS_LETTER_ID, 1);
				}
			}
			else if(st.getQuestItemsCount(MOTHERTREE_FRUIT_ID) >= 1)
				htmltext = "30371-02.htm";
		return htmltext;
	}
}
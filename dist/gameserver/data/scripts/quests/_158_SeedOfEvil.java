package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _158_SeedOfEvil extends QuestScript
{
	int CLAY_TABLET_ID = 1025;
	int ENCHANT_ARMOR_D = 956;

	public _158_SeedOfEvil()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(30031);

		addKillId(27016);

		addQuestItem(CLAY_TABLET_ID);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("1"))
		{
			st.set("id", "0");
			st.setCond(1);
			htmltext = "30031-04.htm";
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		if(npcId == 30031 && st.getCond() == 0)
		{
			if(st.getPlayer().getLevel() >= 21)
				htmltext = "30031-03.htm";
			else
				htmltext = "30031-02.htm";
		}
		else if(npcId == 30031 && st.getCond() == 0)
			htmltext = COMPLETED_DIALOG;
		else if(npcId == 30031 && st.getCond() != 0 && st.getQuestItemsCount(CLAY_TABLET_ID) == 0)
			htmltext = "30031-05.htm";
		else if(npcId == 30031 && st.getCond() != 0 && st.getQuestItemsCount(CLAY_TABLET_ID) != 0)
		{
			st.takeItems(CLAY_TABLET_ID, st.getQuestItemsCount(CLAY_TABLET_ID));
			st.giveItems(ADENA_ID, 1495, true, true);
			st.addExpAndSp(17818, 927);
			st.giveItems(ENCHANT_ARMOR_D, 1, false, false);
			htmltext = "30031-06.htm";
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getQuestItemsCount(CLAY_TABLET_ID) == 0)
		{
			st.giveItems(CLAY_TABLET_ID, 1, false, false);
			st.setCond(2);
		}
		return null;
	}
}
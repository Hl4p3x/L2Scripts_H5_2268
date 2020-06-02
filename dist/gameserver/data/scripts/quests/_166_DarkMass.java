package quests;

import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;

public class _166_DarkMass extends QuestScript
{
	int UNDRES_LETTER_ID = 1088;
	int CEREMONIAL_DAGGER_ID = 1089;
	int DREVIANT_WINE_ID = 1090;
	int GARMIELS_SCRIPTURE_ID = 1091;

	public _166_DarkMass()
	{
		super(PARTY_NONE, ONETIME);
		addStartNpc(30130);
		addTalkId(30135, 30139, 30143);
		addQuestItem(CEREMONIAL_DAGGER_ID, DREVIANT_WINE_ID, GARMIELS_SCRIPTURE_ID, UNDRES_LETTER_ID);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("1"))
		{
			htmltext = "30130-04.htm";
			st.giveItems(UNDRES_LETTER_ID, 1, false, false);
			st.setCond(1);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 30130)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() != Race.DARKELF && st.getPlayer().getRace() != Race.HUMAN)
					htmltext = "30130-00.htm";
				else if(st.getPlayer().getLevel() >= 2)
					htmltext = "30130-03.htm";
				else
					htmltext = "30130-02.htm";
			}
			else if(cond == 1)
				htmltext = "30130-05.htm";
			else if(cond == 2)
			{
				htmltext = "30130-06.htm";
				st.takeItems(UNDRES_LETTER_ID, -1);
				st.takeItems(CEREMONIAL_DAGGER_ID, -1);
				st.takeItems(DREVIANT_WINE_ID, -1);
				st.takeItems(GARMIELS_SCRIPTURE_ID, -1);
				st.giveItems(ADENA_ID, 2966, true, true);
				st.addExpAndSp(5672, 446);
				if(st.getPlayer().getClassId().isOfLevel(ClassLevel.NONE) && !st.getPlayer().getVarBoolean("ng1"))
					st.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.NONE, 5000, ScreenMessageAlign.TOP_CENTER, true, "Delivery duty complete.\nGo find the Newbie Guide."));
				st.finishQuest();
			}
		}
		else if(npcId == 30135)
		{
			if(cond == 1 && st.getQuestItemsCount(CEREMONIAL_DAGGER_ID) == 0)
			{
				giveItem(st, CEREMONIAL_DAGGER_ID);
				htmltext = "30135-01.htm";
			}
			else
				htmltext = "30135-02.htm";
		}
		else if(npcId == 30139)
		{
			if(cond == 1 && st.getQuestItemsCount(DREVIANT_WINE_ID) == 0)
			{
				giveItem(st, DREVIANT_WINE_ID);
				htmltext = "30139-01.htm";
			}
			else
				htmltext = "30139-02.htm";
		}
		else if(npcId == 30143)
			if(cond == 1 && st.getQuestItemsCount(GARMIELS_SCRIPTURE_ID) == 0)
			{
				giveItem(st, GARMIELS_SCRIPTURE_ID);
				htmltext = "30143-01.htm";
			}
			else
				htmltext = "30143-02.htm";
		return htmltext;
	}

	private void giveItem(QuestState st, int item)
	{
		st.giveItems(item, 1, false, false);
		if(st.getQuestItemsCount(CEREMONIAL_DAGGER_ID) >= 1 && st.getQuestItemsCount(DREVIANT_WINE_ID) >= 1 && st.getQuestItemsCount(GARMIELS_SCRIPTURE_ID) >= 1)
			st.setCond(2);
	}
}
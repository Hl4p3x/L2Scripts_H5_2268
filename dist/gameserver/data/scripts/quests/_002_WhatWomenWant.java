package quests;

import l2s.gameserver.Config;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;

/**
 * Рейты учтены
 */
public class _002_WhatWomenWant extends QuestScript
{
	int ARUJIEN = 30223;
	int MIRABEL = 30146;
	int HERBIEL = 30150;
	int GREENIS = 30157;

	int ARUJIENS_LETTER1 = 1092;
	int ARUJIENS_LETTER2 = 1093;
	int ARUJIENS_LETTER3 = 1094;
	int POETRY_BOOK = 689;
	int GREENIS_LETTER = 693;

	int MYSTICS_EARRING = 113;

	public _002_WhatWomenWant()
	{
		super(PARTY_NONE, ONETIME);
		addStartNpc(ARUJIEN);

		addTalkId(MIRABEL);
		addTalkId(HERBIEL);
		addTalkId(GREENIS);

		addQuestItem(GREENIS_LETTER, ARUJIENS_LETTER3, ARUJIENS_LETTER1, ARUJIENS_LETTER2, POETRY_BOOK);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("quest_accept"))
		{
			htmltext = "arujien_q0002_04.htm";
			st.giveItems(ARUJIENS_LETTER1, 1, false, false);
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("2_1"))
		{
			htmltext = "arujien_q0002_08.htm";
			st.takeItems(ARUJIENS_LETTER3, -1);
			st.giveItems(POETRY_BOOK, 1, false, false);
			st.setCond(4);
		}
		else if(event.equalsIgnoreCase("2_2"))
		{
			htmltext = "arujien_q0002_09.htm";
			st.takeItems(ARUJIENS_LETTER3, -1);
			st.giveItems(ADENA_ID, 2300, true, true);
			st.addExpAndSp(4254, 335);
			if(st.getPlayer().getClassId().isOfLevel(ClassLevel.NONE) && !st.getPlayer().getVarBoolean("ng1"))
				st.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.NONE, 5000, ScreenMessageAlign.TOP_CENTER, true, "Delivery duty complete.\nGo find the Newbie Guide."));
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
		if(npcId == ARUJIEN)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() != Race.ELF && st.getPlayer().getRace() != Race.HUMAN)
					htmltext = "arujien_q0002_00.htm";
				else if(st.getPlayer().getLevel() >= 2)
					htmltext = "arujien_q0002_02.htm";
				else
					htmltext = "arujien_q0002_01.htm";
			}
			else if(cond == 1 && st.getQuestItemsCount(ARUJIENS_LETTER1) > 0)
				htmltext = "arujien_q0002_05.htm";
			else if(cond == 2 && st.getQuestItemsCount(ARUJIENS_LETTER2) > 0)
				htmltext = "arujien_q0002_06.htm";
			else if(cond == 3 && st.getQuestItemsCount(ARUJIENS_LETTER3) > 0)
				htmltext = "arujien_q0002_07.htm";
			else if(cond == 4 && st.getQuestItemsCount(POETRY_BOOK) > 0)
				htmltext = "arujien_q0002_11.htm";
			else if(cond == 5 && st.getQuestItemsCount(GREENIS_LETTER) > 0)
			{
				htmltext = "arujien_q0002_09.htm";
				st.takeItems(GREENIS_LETTER, -1);
				st.giveItems(MYSTICS_EARRING, 1, false, false);
				st.giveItems(ADENA_ID, (int) ((Config.RATE_QUESTS_REWARD - 1) * 620 + 1850 * Config.RATE_QUESTS_REWARD), false, false); // T2
				st.addExpAndSp(4254, 335);
				if(st.getPlayer().getClassId().isOfLevel(ClassLevel.NONE) && !st.getPlayer().getVarBoolean("ng1"))
					st.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.NONE, 5000, ScreenMessageAlign.TOP_CENTER, true, "Delivery duty complete.\nGo find the Newbie Guide."));
				st.finishQuest();
			}
		}
		else if(npcId == MIRABEL)
		{
			if(cond == 1 && st.getQuestItemsCount(ARUJIENS_LETTER1) > 0)
			{
				htmltext = "mint_q0002_01.htm";
				st.takeItems(ARUJIENS_LETTER1, -1);
				st.giveItems(ARUJIENS_LETTER2, 1, false, false);
				st.setCond(2);
			}
			else if(cond == 2)
				htmltext = "mint_q0002_02.htm";
		}
		else if(npcId == HERBIEL)
		{
			if(cond == 2 && st.getQuestItemsCount(ARUJIENS_LETTER2) > 0)
			{
				htmltext = "green_q0002_01.htm";
				st.takeItems(ARUJIENS_LETTER2, -1);
				st.giveItems(ARUJIENS_LETTER3, 1, false, false);
				st.setCond(3);
			}
			else if(cond == 3)
				htmltext = "green_q0002_02.htm";
		}
		else if(npcId == GREENIS)
			if(cond == 4 && st.getQuestItemsCount(POETRY_BOOK) > 0)
			{
				htmltext = "grain_q0002_02.htm";
				st.takeItems(POETRY_BOOK, -1);
				st.giveItems(GREENIS_LETTER, 1, false, false);
				st.setCond(5);
			}
			else if(cond == 5 && st.getQuestItemsCount(GREENIS_LETTER) > 0)
				htmltext = "grain_q0002_03.htm";
			else
				htmltext = "grain_q0002_01.htm";
		return htmltext;
	}
}

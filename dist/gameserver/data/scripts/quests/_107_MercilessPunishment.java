package quests;

import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;

public class _107_MercilessPunishment extends QuestScript
{
	int HATOSS_ORDER1 = 1553;
	int HATOSS_ORDER2 = 1554;
	int HATOSS_ORDER3 = 1555;
	int LETTER_TO_HUMAN = 1557;
	int LETTER_TO_DARKELF = 1556;
	int LETTER_TO_ELF = 1558;
	int BUTCHER = 1510;

	public _107_MercilessPunishment()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(30568);

		addTalkId(30580);

		addKillId(27041);

		addQuestItem(LETTER_TO_DARKELF, LETTER_TO_HUMAN, LETTER_TO_ELF, HATOSS_ORDER1, HATOSS_ORDER2, HATOSS_ORDER3);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("urutu_chief_hatos_q0107_03.htm"))
		{
			st.giveItems(HATOSS_ORDER1, 1, false, false);
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("urutu_chief_hatos_q0107_06.htm"))
		{
			st.takeItems(HATOSS_ORDER2, 1);
			st.takeItems(LETTER_TO_DARKELF, 1);
			st.takeItems(LETTER_TO_HUMAN, 1);
			st.takeItems(LETTER_TO_ELF, 1);
			st.takeItems(HATOSS_ORDER1, 1);
			st.takeItems(HATOSS_ORDER2, 1);
			st.takeItems(HATOSS_ORDER3, 1);
			st.giveItems(ADENA_ID, 200, true, true);
			st.unset("cond");
			st.playSound(SOUND_GIVEUP);
		}
		else if(event.equalsIgnoreCase("urutu_chief_hatos_q0107_07.htm"))
		{
			st.takeItems(HATOSS_ORDER1, 1);
			if(st.getQuestItemsCount(HATOSS_ORDER2) == 0)
				st.giveItems(HATOSS_ORDER2, 1, false, false);
		}
		else if(event.equalsIgnoreCase("urutu_chief_hatos_q0107_09.htm"))
		{
			st.takeItems(HATOSS_ORDER2, 1);
			if(st.getQuestItemsCount(HATOSS_ORDER3) == 0)
				st.giveItems(HATOSS_ORDER3, 1, false, false);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 30568)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() != Race.ORC)
					htmltext = "urutu_chief_hatos_q0107_00.htm";
				else if(st.getPlayer().getLevel() >= 10)
					htmltext = "urutu_chief_hatos_q0107_02.htm";
				else
					htmltext = "urutu_chief_hatos_q0107_01.htm";
			}
			else if(cond == 1 && st.getQuestItemsCount(HATOSS_ORDER1) > 0)
				htmltext = "urutu_chief_hatos_q0107_04.htm";
			else if(cond == 2 && st.getQuestItemsCount(HATOSS_ORDER1) > 0 && st.getQuestItemsCount(LETTER_TO_HUMAN) == 0)
				htmltext = "urutu_chief_hatos_q0107_04.htm";
			else if(cond == 3 && st.getQuestItemsCount(HATOSS_ORDER1) > 0 && st.getQuestItemsCount(LETTER_TO_HUMAN) >= 1)
			{
				htmltext = "urutu_chief_hatos_q0107_05.htm";
				st.setCond(4);
			}
			else if(cond == 4 && st.getQuestItemsCount(HATOSS_ORDER2) > 0 && st.getQuestItemsCount(LETTER_TO_DARKELF) == 0)
				htmltext = "urutu_chief_hatos_q0107_05.htm";
			else if(cond == 5 && st.getQuestItemsCount(HATOSS_ORDER2) > 0 && st.getQuestItemsCount(LETTER_TO_DARKELF) >= 1)
			{
				htmltext = "urutu_chief_hatos_q0107_08.htm";
				st.setCond(6);
			}
			else if(cond == 6 && st.getQuestItemsCount(HATOSS_ORDER3) > 0 && st.getQuestItemsCount(LETTER_TO_ELF) == 0)
				htmltext = "urutu_chief_hatos_q0107_08.htm";
			else if(cond == 7 && st.getQuestItemsCount(HATOSS_ORDER3) > 0 && st.getQuestItemsCount(LETTER_TO_ELF) + st.getQuestItemsCount(LETTER_TO_HUMAN) + st.getQuestItemsCount(LETTER_TO_DARKELF) >= 3)
			{
				htmltext = "urutu_chief_hatos_q0107_10.htm";
				st.takeItems(LETTER_TO_DARKELF, -1);
				st.takeItems(LETTER_TO_HUMAN, -1);
				st.takeItems(LETTER_TO_ELF, -1);
				st.takeItems(HATOSS_ORDER3, -1);

				st.giveItems(BUTCHER, 1, false, false);
				st.addExpAndSp(34565, 2962);
				st.giveItems(ADENA_ID, 14666, true, true);

				if(st.getPlayer().getClassId().isOfLevel(ClassLevel.NONE) && !st.getPlayer().getVarBoolean("p1q3"))
				{
					st.getPlayer().setVar("p1q3", "1", -1); // flag for helper
					st.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.NONE, 5000, ScreenMessageAlign.TOP_CENTER, true, "Acquisition of race-specific weapon complete.\n           Go find the Newbie Guide."));
					st.giveItems(1060, 100, false, false); // healing potion
					for(int item = 4412; item <= 4417; item++)
						st.giveItems(item, 10, false, false); // echo cry
					st.playTutorialVoice("tutorial_voice_026");
					st.giveItems(5789, 7000, false, false); // newbie ss
				}

				st.finishQuest();
			}
		}
		else if(npcId == 30580 && cond >= 1 && (st.getQuestItemsCount(HATOSS_ORDER1) > 0 || st.getQuestItemsCount(HATOSS_ORDER2) > 0 || st.getQuestItemsCount(HATOSS_ORDER3) > 0))
		{
			if(cond == 1)
				st.setCond(2);
			htmltext = "centurion_parugon_q0107_01.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 27041)
			if(cond == 2 && st.getQuestItemsCount(HATOSS_ORDER1) > 0 && st.getQuestItemsCount(LETTER_TO_HUMAN) == 0)
			{
				st.giveItems(LETTER_TO_HUMAN, 1, false, false);
				st.setCond(3);
				st.playSound(SOUND_ITEMGET);
			}
			else if(cond == 4 && st.getQuestItemsCount(HATOSS_ORDER2) > 0 && st.getQuestItemsCount(LETTER_TO_DARKELF) == 0)
			{
				st.giveItems(LETTER_TO_DARKELF, 1, false, false);
				st.setCond(5);
				st.playSound(SOUND_ITEMGET);
			}
			else if(cond == 6 && st.getQuestItemsCount(HATOSS_ORDER3) > 0 && st.getQuestItemsCount(LETTER_TO_ELF) == 0)
			{
				st.giveItems(LETTER_TO_ELF, 1, false, false);
				st.setCond(7);
				st.playSound(SOUND_ITEMGET);
			}
		return null;
	}
}
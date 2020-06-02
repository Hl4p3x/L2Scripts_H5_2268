package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;

public class _101_SwordOfSolidarity extends QuestScript
{
	int ROIENS_LETTER = 796;
	int HOWTOGO_RUINS = 937;
	int BROKEN_SWORD_HANDLE = 739;
	int BROKEN_BLADE_BOTTOM = 740;
	int BROKEN_BLADE_TOP = 741;
	int ALLTRANS_NOTE = 742;
	int SWORD_OF_SOLIDARITY = 738;

	public _101_SwordOfSolidarity()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(30008);
		addTalkId(30283);

		addKillId(20361);
		addKillId(20362);

		addQuestItem(ALLTRANS_NOTE, HOWTOGO_RUINS, BROKEN_BLADE_TOP, BROKEN_BLADE_BOTTOM, ROIENS_LETTER, BROKEN_SWORD_HANDLE);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("roien_q0101_04.htm"))
		{
			st.setCond(1);
			st.giveItems(ROIENS_LETTER, 1, false, false);
		}
		else if(event.equalsIgnoreCase("blacksmith_alltran_q0101_02.htm"))
		{
			st.setCond(2);
			st.takeItems(ROIENS_LETTER, -1);
			st.giveItems(HOWTOGO_RUINS, 1, false, false);
		}
		else if(event.equalsIgnoreCase("blacksmith_alltran_q0101_07.htm"))
		{
			st.takeItems(BROKEN_SWORD_HANDLE, -1);

			st.giveItems(SWORD_OF_SOLIDARITY, 1, false, false);
			st.giveItems(ADENA_ID, 10981, true, true);
			st.addExpAndSp(25747, 2171);

			if(st.getPlayer().getClassId().isOfLevel(ClassLevel.NONE) && !st.getPlayer().getVarBoolean("p1q3"))
			{
				st.getPlayer().setVar("p1q3", "1", -1); // flag for helper
				st.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.NONE, 5000, ScreenMessageAlign.TOP_CENTER, true, "Now go find the Newbie Guide."));
				st.giveItems(1060, 100, false, false); // healing potion
				for(int item = 4412; item <= 4417; item++)
					st.giveItems(item, 10, false, false); // echo cry
				if(st.getPlayer().getClassId().isMage())
				{
					st.playTutorialVoice("tutorial_voice_027");
					st.giveItems(5790, 3000, true, true); // newbie sps
				}
				else
				{
					st.playTutorialVoice("tutorial_voice_026");
					st.giveItems(5789, 6000, true, true); // newbie ss
				}
			}

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
		if(npcId == 30008)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() != Race.HUMAN)
					htmltext = "roien_q0101_00.htm";
				else if(st.getPlayer().getLevel() >= 9)
					htmltext = "roien_q0101_02.htm";
				else
					htmltext = "roien_q0101_08.htm";
			}
			else if(cond == 1 && st.getQuestItemsCount(ROIENS_LETTER) >= 1)
				htmltext = "roien_q0101_05.htm";
			else if(cond >= 2 && st.getQuestItemsCount(ROIENS_LETTER) == 0 && st.getQuestItemsCount(ALLTRANS_NOTE) == 0)
			{
				if(st.getQuestItemsCount(BROKEN_BLADE_TOP) > 0 && st.getQuestItemsCount(BROKEN_BLADE_BOTTOM) > 0)
					htmltext = "roien_q0101_12.htm";
				if(st.getQuestItemsCount(BROKEN_BLADE_TOP) + st.getQuestItemsCount(BROKEN_BLADE_BOTTOM) <= 1)
					htmltext = "roien_q0101_11.htm";
				if(st.getQuestItemsCount(BROKEN_SWORD_HANDLE) > 0)
					htmltext = "roien_q0101_07.htm";
				if(st.getQuestItemsCount(HOWTOGO_RUINS) >= 1)
					htmltext = "roien_q0101_10.htm";
			}
			else if(cond == 4 && st.getQuestItemsCount(ALLTRANS_NOTE) > 0)
			{
				htmltext = "roien_q0101_06.htm";
				st.setCond(5);
				st.takeItems(ALLTRANS_NOTE, -1);
				st.giveItems(BROKEN_SWORD_HANDLE, 1, false, false);
			}
		}
		else if(npcId == 30283)
			if(cond == 1 && st.getQuestItemsCount(ROIENS_LETTER) > 0)
				htmltext = "blacksmith_alltran_q0101_01.htm";
			else if(cond >= 2 && st.getQuestItemsCount(HOWTOGO_RUINS) >= 1)
			{
				if(st.getQuestItemsCount(BROKEN_BLADE_TOP) + st.getQuestItemsCount(BROKEN_BLADE_BOTTOM) == 1)
					htmltext = "blacksmith_alltran_q0101_08.htm";
				else if(st.getQuestItemsCount(BROKEN_BLADE_TOP) + st.getQuestItemsCount(BROKEN_BLADE_BOTTOM) == 0)
					htmltext = "blacksmith_alltran_q0101_03.htm";
				else if(st.getQuestItemsCount(BROKEN_BLADE_TOP) > 0 && st.getQuestItemsCount(BROKEN_BLADE_BOTTOM) > 0)
				{
					htmltext = "blacksmith_alltran_q0101_04.htm";
					st.setCond(4);
					st.takeItems(HOWTOGO_RUINS, -1);
					st.takeItems(BROKEN_BLADE_TOP, -1);
					st.takeItems(BROKEN_BLADE_BOTTOM, -1);
					st.giveItems(ALLTRANS_NOTE, 1, false, false);
				}
				else if(cond == 4 && st.getQuestItemsCount(ALLTRANS_NOTE) > 0)
					htmltext = "blacksmith_alltran_q0101_05.htm";
			}
			else if(cond == 5 && st.getQuestItemsCount(BROKEN_SWORD_HANDLE) > 0)
				htmltext = "blacksmith_alltran_q0101_06.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if((npcId == 20361 || npcId == 20362) && st.getQuestItemsCount(HOWTOGO_RUINS) > 0)
		{
			if(st.getQuestItemsCount(BROKEN_BLADE_TOP) == 0 && Rnd.chance(60))
			{
				st.giveItems(BROKEN_BLADE_TOP, 1, false, false);
				st.playSound(SOUND_MIDDLE);
			}
			else if(st.getQuestItemsCount(BROKEN_BLADE_BOTTOM) == 0 && Rnd.chance(60))
			{
				st.giveItems(BROKEN_BLADE_BOTTOM, 1, false, false);
				st.playSound(SOUND_MIDDLE);
			}
			if(st.getQuestItemsCount(BROKEN_BLADE_TOP) > 0 && st.getQuestItemsCount(BROKEN_BLADE_BOTTOM) > 0)
				st.setCond(3);
		}
		return null;
	}
}
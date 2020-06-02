package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;

public class _103_SpiritOfCraftsman extends QuestScript
{
	public final int KAROYDS_LETTER_ID = 968;
	public final int CECKTINONS_VOUCHER1_ID = 969;
	public final int CECKTINONS_VOUCHER2_ID = 970;
	public final int BONE_FRAGMENT1_ID = 1107;
	public final int SOUL_CATCHER_ID = 971;
	public final int PRESERVE_OIL_ID = 972;
	public final int ZOMBIE_HEAD_ID = 973;
	public final int STEELBENDERS_HEAD_ID = 974;
	public final int BLOODSABER_ID = 975;

	public _103_SpiritOfCraftsman()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(30307);

		addTalkId(30132);
		addTalkId(30144);

		addKillId(20015);
		addKillId(20020);
		addKillId(20455);
		addKillId(20517);
		addKillId(20518);

		addQuestItem(KAROYDS_LETTER_ID, CECKTINONS_VOUCHER1_ID, CECKTINONS_VOUCHER2_ID, BONE_FRAGMENT1_ID, SOUL_CATCHER_ID, PRESERVE_OIL_ID, ZOMBIE_HEAD_ID, STEELBENDERS_HEAD_ID);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("blacksmith_karoyd_q0103_05.htm"))
		{
			st.giveItems(KAROYDS_LETTER_ID, 1, false, false);
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
		if(npcId == 30307)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() != Race.DARKELF)
					htmltext = "blacksmith_karoyd_q0103_00.htm";
				else if(st.getPlayer().getLevel() >= 10)
					htmltext = "blacksmith_karoyd_q0103_03.htm";
				else
					htmltext = "blacksmith_karoyd_q0103_02.htm";
			}
			else if(cond >= 1)
			{
				if(st.getQuestItemsCount(KAROYDS_LETTER_ID) >= 1 || st.getQuestItemsCount(CECKTINONS_VOUCHER1_ID) >= 1 || st.getQuestItemsCount(CECKTINONS_VOUCHER2_ID) >= 1)
					htmltext = "blacksmith_karoyd_q0103_06.htm";
				else if(cond == 8)
				{
					if(st.getQuestItemsCount(STEELBENDERS_HEAD_ID) >= 1)
					{
						htmltext = "blacksmith_karoyd_q0103_07.htm";
						st.takeItems(STEELBENDERS_HEAD_ID, 1);

						st.giveItems(BLOODSABER_ID, 1, false, false);
						st.giveItems(ADENA_ID, 19799, true, true);
						st.addExpAndSp(46663, 3999);

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
				}
			}
		}
		else if(npcId == 30132)
		{
			if(cond == 1)
			{
				if(st.getQuestItemsCount(KAROYDS_LETTER_ID) >= 1)
				{
					htmltext = "cecon_q0103_01.htm";
					st.setCond(2);
					st.takeItems(KAROYDS_LETTER_ID, 1);
					st.giveItems(CECKTINONS_VOUCHER1_ID, 1, false, false);
				}
			}
			else if(cond >= 2)
			{
				if(st.getQuestItemsCount(CECKTINONS_VOUCHER1_ID) >= 1 || st.getQuestItemsCount(CECKTINONS_VOUCHER2_ID) >= 1)
					htmltext = "cecon_q0103_02.htm";
				else if(cond == 5)
				{
					if(st.getQuestItemsCount(SOUL_CATCHER_ID) >= 1)
					{
						htmltext = "cecon_q0103_03.htm";
						st.setCond(6);
						st.takeItems(SOUL_CATCHER_ID, 1);
						st.giveItems(PRESERVE_OIL_ID, 1, false, false);
					}
				}
				else if(cond == 6)
				{
					if(st.getQuestItemsCount(PRESERVE_OIL_ID) >= 1 && st.getQuestItemsCount(ZOMBIE_HEAD_ID) == 0 && st.getQuestItemsCount(STEELBENDERS_HEAD_ID) == 0)
						htmltext = "cecon_q0103_04.htm";
				}
				else if(cond == 7)
				{
					if(st.getQuestItemsCount(ZOMBIE_HEAD_ID) >= 1)
					{
						htmltext = "cecon_q0103_05.htm";
						st.setCond(8);
						st.takeItems(ZOMBIE_HEAD_ID, 1);
						st.giveItems(STEELBENDERS_HEAD_ID, 1, false, false);
					}
				}
				else if(cond == 8)
				{
					if(st.getQuestItemsCount(STEELBENDERS_HEAD_ID) >= 1)
						htmltext = "cecon_q0103_06.htm";
				}
			}
		}
		else if(npcId == 30144)
		{
			if(cond == 2)
			{
				if(st.getQuestItemsCount(CECKTINONS_VOUCHER1_ID) >= 1)
				{
					htmltext = "harne_q0103_01.htm";
					st.setCond(3);
					st.takeItems(CECKTINONS_VOUCHER1_ID, 1);
					st.giveItems(CECKTINONS_VOUCHER2_ID, 1, false, false);
				}
			}
			else if(cond == 3)
			{
				if(st.getQuestItemsCount(CECKTINONS_VOUCHER2_ID) >= 1 && st.getQuestItemsCount(BONE_FRAGMENT1_ID) < 10)
					htmltext = "harne_q0103_02.htm";
			}
			else if(cond == 4)
			{
				if(st.getQuestItemsCount(CECKTINONS_VOUCHER2_ID) == 1 && st.getQuestItemsCount(BONE_FRAGMENT1_ID) >= 10)
				{
					htmltext = "harne_q0103_03.htm";
					st.setCond(5);
					st.takeItems(CECKTINONS_VOUCHER2_ID, 1);
					st.takeItems(BONE_FRAGMENT1_ID, 10);
					st.giveItems(SOUL_CATCHER_ID, 1, false, false);
				}
			}
			else if(cond == 5)
			{
				if(st.getQuestItemsCount(SOUL_CATCHER_ID) >= 1)
					htmltext = "harne_q0103_04.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if((npcId == 20517 || npcId == 20518 || npcId == 20455) && st.getCond() == 3)
		{
			if(st.getQuestItemsCount(CECKTINONS_VOUCHER2_ID) == 1 && st.getQuestItemsCount(BONE_FRAGMENT1_ID) < 10)
				if(Rnd.chance(33))
				{
					st.giveItems(BONE_FRAGMENT1_ID, 1, true, true);
					if(st.getQuestItemsCount(BONE_FRAGMENT1_ID) >= 10)
						st.setCond(4);
					else
						st.playSound(SOUND_ITEMGET);
				}
		}
		else if((npcId == 20015 || npcId == 20020) && st.getCond() == 6)
			if(st.getQuestItemsCount(PRESERVE_OIL_ID) >= 1)
				if(Rnd.chance(33))
				{
					st.giveItems(ZOMBIE_HEAD_ID, 1, false, false);
					st.takeItems(PRESERVE_OIL_ID, 1);
					st.setCond(7);
				}

		return null;
	}
}

package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _407_PathToElvenScout extends QuestScript
{

	public final int REISA = 30328;
	public final int MORETTI = 30337;
	public final int PIPPEN = 30426;

	public final int OL_MAHUM_SENTRY = 27031;
	public final int OL_MAHUM_PATROL = 20053;

	public final int REORIA_LETTER2_ID = 1207;
	public final int PRIGUNS_TEAR_LETTER1_ID = 1208;
	public final int PRIGUNS_TEAR_LETTER2_ID = 1209;
	public final int PRIGUNS_TEAR_LETTER3_ID = 1210;
	public final int PRIGUNS_TEAR_LETTER4_ID = 1211;
	public final int MORETTIS_HERB_ID = 1212;
	public final int MORETTIS_LETTER_ID = 1214;
	public final int PRIGUNS_LETTER_ID = 1215;
	public final int MONORARY_GUARD_ID = 1216;
	public final int REORIA_RECOMMENDATION_ID = 1217;
	public final int RUSTED_KEY_ID = 1293;
	public final int HONORARY_GUARD_ID = 1216;

	public _407_PathToElvenScout()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(REISA);

		addTalkId(MORETTI);
		addTalkId(PIPPEN);

		addKillId(OL_MAHUM_SENTRY);
		addKillId(OL_MAHUM_PATROL);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("1"))
		{
			if(st.getPlayer().getClassId().getId() == 0x12)
			{
				if(st.getPlayer().getLevel() >= 18)
				{
					if(st.getQuestItemsCount(REORIA_RECOMMENDATION_ID) > 0)
						htmltext = "master_reoria_q0407_04.htm";
					else
					{
						htmltext = "master_reoria_q0407_05.htm";
						st.giveItems(REORIA_LETTER2_ID, 1, false, false);
						st.setCond(1);
					}
				}
				else
					htmltext = "master_reoria_q0407_03.htm";
			}
			else if(st.getPlayer().getClassId().getId() == 0x16)
				htmltext = "master_reoria_q0407_02a.htm";
			else
				htmltext = "master_reoria_q0407_02.htm";
		}
		else if(event.equalsIgnoreCase("30337_1"))
		{
			st.takeItems(REORIA_LETTER2_ID, 1);
			st.setCond(2);
			htmltext = "guard_moretti_q0407_03.htm";
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(npcId == REISA)
		{
			if(cond == 0)
				htmltext = "master_reoria_q0407_01.htm";
			else if(cond == 1)
				htmltext = "master_reoria_q0407_06.htm";
			else if(cond > 1 && st.getQuestItemsCount(HONORARY_GUARD_ID) == 0)
				htmltext = "master_reoria_q0407_08.htm";
			else if(cond == 8 && st.getQuestItemsCount(HONORARY_GUARD_ID) >= 1)
			{
				htmltext = "master_reoria_q0407_07.htm";
				st.takeItems(HONORARY_GUARD_ID, 1);
				if(st.getPlayer().getClassId().isOfLevel(ClassLevel.NONE))
				{
					st.giveItems(REORIA_RECOMMENDATION_ID, 1, false, false);
					if(!st.getPlayer().getVarBoolean("prof1"))
					{
						st.getPlayer().setVar("prof1", "1", -1);
						st.addExpAndSp(228064, 16455);
						//FIXME [G1ta0] дать адены, только если первый чар на акке
						st.giveItems(ADENA_ID, 163800, true, true);
					}
				}
				st.finishQuest();
			}
		}
		else if(npcId == MORETTI)
		{
			if(cond == 1)
				htmltext = "guard_moretti_q0407_01.htm";
			else if(cond == 2)
				htmltext = "guard_moretti_q0407_04.htm";
			else if(cond == 3)
			{
				if(st.getQuestItemsCount(PRIGUNS_TEAR_LETTER1_ID) >= 1 && st.getQuestItemsCount(PRIGUNS_TEAR_LETTER2_ID) >= 1 && st.getQuestItemsCount(PRIGUNS_TEAR_LETTER3_ID) >= 1 && st.getQuestItemsCount(PRIGUNS_TEAR_LETTER4_ID) >= 1)
				{
					htmltext = "guard_moretti_q0407_06.htm";
					st.takeItems(PRIGUNS_TEAR_LETTER1_ID, 1);
					st.takeItems(PRIGUNS_TEAR_LETTER2_ID, 1);
					st.takeItems(PRIGUNS_TEAR_LETTER3_ID, 1);
					st.takeItems(PRIGUNS_TEAR_LETTER4_ID, 1);
					st.giveItems(MORETTIS_HERB_ID, 1, false, false);
					st.giveItems(MORETTIS_LETTER_ID, 1, false, false);
					st.setCond(4);
				}
				else
					htmltext = "guard_moretti_q0407_05.htm";
			}
			else if(cond == 7 && st.getQuestItemsCount(PRIGUNS_LETTER_ID) >= 1)
			{
				htmltext = "guard_moretti_q0407_07.htm";
				st.takeItems(PRIGUNS_LETTER_ID, 1);
				st.giveItems(HONORARY_GUARD_ID, 1, false, false);
				st.setCond(8);
			}
			else if(cond > 8)
				htmltext = "guard_moretti_q0407_08.htm";
		}
		else if(npcId == PIPPEN)
			if(cond == 4)
			{
				htmltext = "prigun_q0407_01.htm";
				st.setCond(5);
			}
			else if(cond == 5)
				htmltext = "prigun_q0407_01.htm";
			else if(cond == 6 && st.getQuestItemsCount(RUSTED_KEY_ID) >= 1 && st.getQuestItemsCount(MORETTIS_HERB_ID) >= 1 && st.getQuestItemsCount(MORETTIS_LETTER_ID) >= 1)
			{
				htmltext = "prigun_q0407_02.htm";
				st.takeItems(RUSTED_KEY_ID, 1);
				st.takeItems(MORETTIS_HERB_ID, 1);
				st.takeItems(MORETTIS_LETTER_ID, 1);
				st.giveItems(PRIGUNS_LETTER_ID, 1, false, false);
				st.setCond(7);
			}
			else if(cond == 7)
				htmltext = "prigun_q0407_04.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == OL_MAHUM_PATROL && cond == 2)
		{
			if(st.getQuestItemsCount(PRIGUNS_TEAR_LETTER1_ID) == 0)
			{
				st.giveItems(PRIGUNS_TEAR_LETTER1_ID, 1, false, false);
				st.playSound(SOUND_ITEMGET);
				return null;
			}
			if(st.getQuestItemsCount(PRIGUNS_TEAR_LETTER2_ID) == 0)
			{
				st.giveItems(PRIGUNS_TEAR_LETTER2_ID, 1, false, false);
				st.playSound(SOUND_ITEMGET);
				return null;
			}
			if(st.getQuestItemsCount(PRIGUNS_TEAR_LETTER3_ID) == 0)
			{
				st.giveItems(PRIGUNS_TEAR_LETTER3_ID, 1, false, false);
				st.playSound(SOUND_ITEMGET);
				return null;
			}
			if(st.getQuestItemsCount(PRIGUNS_TEAR_LETTER4_ID) == 0)
			{
				st.giveItems(PRIGUNS_TEAR_LETTER4_ID, 1, false, false);
				st.setCond(3);
				return null;
			}
		}
		else if(npcId == OL_MAHUM_SENTRY && cond == 5 && Rnd.chance(60))
		{
			st.giveItems(RUSTED_KEY_ID, 1, false, false);
			st.setCond(6);
		}
		return null;
	}
}

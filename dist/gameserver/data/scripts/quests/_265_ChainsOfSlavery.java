package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;

/**
 * Квест проверен и работает. Рейты применены путем увеличения награды за квестовые предметы.
 */
public class _265_ChainsOfSlavery extends QuestScript
{
	// NPC
	private static final int KRISTIN = 30357;

	// MOBS
	private static final int IMP = 20004;
	private static final int IMP_ELDER = 20005;

	// ITEMS
	private static final int IMP_SHACKLES = 1368;

	public _265_ChainsOfSlavery()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(KRISTIN);

		addKillId(IMP);
		addKillId(IMP_ELDER);

		addQuestItem(IMP_SHACKLES);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("sentry_krpion_q0265_03.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("sentry_krpion_q0265_06.htm"))
			st.finishQuest();
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		if(st.getCond() == 0)
		{
			if(st.getPlayer().getRace() != Race.DARKELF)
				htmltext = "sentry_krpion_q0265_00.htm";
			else if(st.getPlayer().getLevel() < 6)
				htmltext = "sentry_krpion_q0265_01.htm";
			else
				htmltext = "sentry_krpion_q0265_02.htm";
		}
		else
		{
			long count = st.getQuestItemsCount(IMP_SHACKLES);
			if(count > 0)
				if(count >= 10)
					st.giveItems(ADENA_ID, 13 * count + 500, true, true);
				else
					st.giveItems(ADENA_ID, 13 * count, true, true);
			st.takeItems(IMP_SHACKLES, -1);
			htmltext = "sentry_krpion_q0265_05.htm";

			if(st.getPlayer().getClassId().isOfLevel(ClassLevel.NONE) && !st.getPlayer().getVarBoolean("p1q2"))
			{
				st.getPlayer().setVar("p1q2", "1", -1);
				st.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.NONE, 5000, ScreenMessageAlign.TOP_CENTER, true, "Acquisition of Soulshot for beginners complete.\n                  Go find the Newbie Guide."));
				QuestState qs = st.getPlayer().getQuestState(255);
				if(qs != null && qs.getInt("Ex") != 10)
				{
					st.showQuestionMark(false, 26);
					qs.set("Ex", "10");
					if(st.getPlayer().getClassId().isMage())
					{
						st.playTutorialVoice("tutorial_voice_027");
						st.giveItems(5790, 3000, false, false);
					}
					else
					{
						st.playTutorialVoice("tutorial_voice_026");
						st.giveItems(5789, 6000, false, false);
					}
				}
			}
			else
				htmltext = "sentry_krpion_q0265_04.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(st.getCond() == 1 && Rnd.chance(5 + npcId - 20004))
		{
			st.giveItems(IMP_SHACKLES, 1, true, true);
			st.playSound(SOUND_ITEMGET);
		}
		return null;
	}
}
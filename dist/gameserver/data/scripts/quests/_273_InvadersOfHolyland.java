package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;

public class _273_InvadersOfHolyland extends QuestScript
{
	public final int BLACK_SOULSTONE = 1475;
	public final int RED_SOULSTONE = 1476;

	public _273_InvadersOfHolyland()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(30566);
		addKillId(new int[]{
				20311,
				20312,
				20313
		});
		addQuestItem(new int[]{
				BLACK_SOULSTONE,
				RED_SOULSTONE
		});
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("atuba_chief_varkees_q0273_03.htm"))
		{
			st.setCond(1);
		}
		else if(event.equals("atuba_chief_varkees_q0273_07.htm"))
		{
			st.finishQuest();
		}
		else if(event.equals("atuba_chief_varkees_q0273_08.htm"))
		{
			st.setCond(1);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getRace() != Race.ORC)
				htmltext = "atuba_chief_varkees_q0273_00.htm";
			else if(st.getPlayer().getLevel() < 6)
				htmltext = "atuba_chief_varkees_q0273_01.htm";
			else
				htmltext = "atuba_chief_varkees_q0273_02.htm";
		}
		else
		{
			if(st.getQuestItemsCount(BLACK_SOULSTONE) == 0 && st.getQuestItemsCount(RED_SOULSTONE) == 0)
				htmltext = "atuba_chief_varkees_q0273_04.htm";
			else
			{
				long adena = 0;
				if(st.getQuestItemsCount(BLACK_SOULSTONE) > 0)
				{
					htmltext = "atuba_chief_varkees_q0273_05.htm";
					adena += st.getQuestItemsCount(BLACK_SOULSTONE) * 5;
				}
				if(st.getQuestItemsCount(RED_SOULSTONE) > 0)
				{
					htmltext = "atuba_chief_varkees_q0273_06.htm";
					adena += st.getQuestItemsCount(RED_SOULSTONE) * 50;
				}
				st.takeAllItems(BLACK_SOULSTONE, RED_SOULSTONE);
				st.giveItems(ADENA_ID, adena, true, true);

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

				st.finishQuest();
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 20311)
		{
			if(cond == 1)
			{
				if(Rnd.chance(90))
					st.giveItems(BLACK_SOULSTONE, 1, true, true);
				else
					st.giveItems(RED_SOULSTONE, 1, true, true);
				st.playSound(SOUND_ITEMGET);
			}
		}
		else if(npcId == 20312)
		{
			if(cond == 1)
			{
				if(Rnd.chance(87))
					st.giveItems(BLACK_SOULSTONE, 1, true, true);
				else
					st.giveItems(RED_SOULSTONE, 1, true, true);
				st.playSound(SOUND_ITEMGET);
			}
		}
		else if(npcId == 20313)
			if(cond == 1)
			{
				if(Rnd.chance(77))
					st.giveItems(BLACK_SOULSTONE, 1, true, true);
				else
					st.giveItems(RED_SOULSTONE, 1, true, true);
				st.playSound(SOUND_ITEMGET);
			}
		return null;
	}
}
package quests;

import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;

public class _261_CollectorsDream extends QuestScript
{
	int GIANT_SPIDER_LEG = 1087;

	public _261_CollectorsDream()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(30222);

		addTalkId(30222);

		addKillId(20308);
		addKillId(20460);
		addKillId(20466);

		addQuestItem(GIANT_SPIDER_LEG);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.intern().equalsIgnoreCase("moneylender_alshupes_q0261_03.htm"))
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
			if(st.getPlayer().getLevel() >= 15)
			{
				htmltext = "moneylender_alshupes_q0261_02.htm";
				return htmltext;
			}
			htmltext = "moneylender_alshupes_q0261_01.htm";
		}
		else if(cond == 1 || st.getQuestItemsCount(GIANT_SPIDER_LEG) < 8)
			htmltext = "moneylender_alshupes_q0261_04.htm";
		else if(cond == 2 && st.getQuestItemsCount(GIANT_SPIDER_LEG) >= 8)
		{
			st.takeItems(GIANT_SPIDER_LEG, -1);

			st.giveItems(ADENA_ID, 1000, true, true);
			st.addExpAndSp(2000, 0);

			if(st.getPlayer().getClassId().isOfLevel(ClassLevel.NONE) && !st.getPlayer().getVarBoolean("p1q4"))
			{
				st.getPlayer().setVar("p1q4", "1", -1);
				st.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.NONE, 5000, ScreenMessageAlign.TOP_CENTER, true, "Now go find the Newbie Guide."));
			}

			htmltext = "moneylender_alshupes_q0261_05.htm";
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1 && st.getQuestItemsCount(GIANT_SPIDER_LEG) < 8)
		{
			st.giveItems(GIANT_SPIDER_LEG, 1, true, true);
			if(st.getQuestItemsCount(GIANT_SPIDER_LEG) >= 8)
			{
				st.setCond(2);
			}
			else
				st.playSound(SOUND_ITEMGET);
		}
		return null;
	}
}
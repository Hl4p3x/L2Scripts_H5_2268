package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;

public class _296_SilkOfTarantula extends QuestScript
{

	private static final int TARANTULA_SPIDER_SILK = 1493;
	private static final int TARANTULA_SPINNERETTE = 1494;
	private static final int RING_OF_RACCOON = 1508;
	private static final int RING_OF_FIREFLY = 1509;

	public _296_SilkOfTarantula()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(30519);
		addTalkId(30548);

		addKillId(20394);
		addKillId(20403);
		addKillId(20508);

		addQuestItem(TARANTULA_SPIDER_SILK);
		addQuestItem(TARANTULA_SPINNERETTE);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("trader_mion_q0296_03.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("quit"))
		{
			htmltext = "trader_mion_q0296_06.htm";
			st.takeItems(TARANTULA_SPINNERETTE, -1);
			st.finishQuest();
		}
		else if(event.equalsIgnoreCase("exchange"))
			if(st.getQuestItemsCount(TARANTULA_SPINNERETTE) >= 1)
			{
				htmltext = "defender_nathan_q0296_03.htm";
				st.giveItems(TARANTULA_SPIDER_SILK, 17, false, false);
				st.takeItems(TARANTULA_SPINNERETTE, -1);
			}
			else
				htmltext = "defender_nathan_q0296_02.htm";
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == 30519)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 15)
				{
					if(st.getQuestItemsCount(RING_OF_RACCOON) > 0 || st.getQuestItemsCount(RING_OF_FIREFLY) > 0)
						htmltext = "trader_mion_q0296_02.htm";
					else
						htmltext = "trader_mion_q0296_08.htm";
				}
				else
					htmltext = "trader_mion_q0296_01.htm";
			}
			else if(cond == 1)
				if(st.getQuestItemsCount(TARANTULA_SPIDER_SILK) < 1)
					htmltext = "trader_mion_q0296_04.htm";
				else if(st.getQuestItemsCount(TARANTULA_SPIDER_SILK) >= 1)
				{
					htmltext = "trader_mion_q0296_05.htm";
					st.giveItems(ADENA_ID, st.getQuestItemsCount(TARANTULA_SPIDER_SILK) * 23, true, true);
					st.takeItems(TARANTULA_SPIDER_SILK, -1);

					if(st.getPlayer().getClassId().isOfLevel(ClassLevel.NONE) && !st.getPlayer().getVarBoolean("p1q4"))
					{
						st.getPlayer().setVar("p1q4", "1", -1);
						st.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.NONE, 5000, ScreenMessageAlign.TOP_CENTER, true, "Now go find the Newbie Guide."));
					}
				}
		}
		else if(npcId == 30548 && cond == 1)
			htmltext = "defender_nathan_q0296_01.htm";

		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1)
			if(Rnd.chance(50))
				st.rollAndGive(TARANTULA_SPINNERETTE, 1, 45);
			else
				st.rollAndGive(TARANTULA_SPIDER_SILK, 1, 45);
		return null;
	}
}
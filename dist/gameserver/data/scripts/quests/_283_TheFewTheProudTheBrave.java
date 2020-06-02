package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;

public class _283_TheFewTheProudTheBrave extends QuestScript
{
	//NPCs
	private static int PERWAN = 32133;
	//Mobs
	private static int CRIMSON_SPIDER = 22244;
	//Quest Items
	private static int CRIMSON_SPIDER_CLAW = 9747;
	//Chances
	private static int CRIMSON_SPIDER_CLAW_CHANCE = 34;

	public _283_TheFewTheProudTheBrave()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(PERWAN);
		addKillId(CRIMSON_SPIDER);
		addQuestItem(CRIMSON_SPIDER_CLAW);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("subelder_perwan_q0283_0103.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("subelder_perwan_q0283_0203.htm"))
		{
			long count = st.getQuestItemsCount(CRIMSON_SPIDER_CLAW);
			if(count > 0)
			{
				st.takeItems(CRIMSON_SPIDER_CLAW, -1);
				st.giveItems(ADENA_ID, 45 * count, true, true);

				if(st.getPlayer().getClassId().isOfLevel(ClassLevel.NONE) && !st.getPlayer().getVarBoolean("p1q4"))
				{
					st.getPlayer().setVar("p1q4", "1", -1);
					st.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.NONE, 5000, ScreenMessageAlign.TOP_CENTER, true, "Now go find the Newbie Guide."));
				}

				st.playSound(SOUND_MIDDLE);
			}
		}
		else if(event.equalsIgnoreCase("subelder_perwan_q0283_0204.htm"))
		{
			st.takeItems(CRIMSON_SPIDER_CLAW, -1);
			st.finishQuest();
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == PERWAN)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 15)
					htmltext = "subelder_perwan_q0283_0101.htm";
				else
					htmltext = "subelder_perwan_q0283_0102.htm";
			}
			else
				htmltext = st.getQuestItemsCount(CRIMSON_SPIDER_CLAW) > 0 ? "subelder_perwan_q0283_0105.htm" : "subelder_perwan_q0283_0106.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(qs.isStarted())
		{
			if(Rnd.chance(CRIMSON_SPIDER_CLAW_CHANCE))
			{
				qs.giveItems(CRIMSON_SPIDER_CLAW, 1, true, true);
				qs.playSound(SOUND_ITEMGET);
			}
		}
		return null;
	}
}
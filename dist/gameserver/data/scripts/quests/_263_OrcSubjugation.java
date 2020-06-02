package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _263_OrcSubjugation extends QuestScript
{
	// NPC
	public final int KAYLEEN = 30346;

	// MOBS
	public final int BALOR_ORC_ARCHER = 20385;
	public final int BALOR_ORC_FIGHTER = 20386;
	public final int BALOR_ORC_FIGHTER_LEADER = 20387;
	public final int BALOR_ORC_LIEUTENANT = 20388;

	public final int ORC_AMULET = 1116;
	public final int ORC_NECKLACE = 1117;

	public _263_OrcSubjugation()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(KAYLEEN);
		addKillId(new int[]{
				BALOR_ORC_ARCHER,
				BALOR_ORC_FIGHTER,
				BALOR_ORC_FIGHTER_LEADER,
				BALOR_ORC_LIEUTENANT
		});
		addQuestItem(new int[]{
				ORC_AMULET,
				ORC_NECKLACE
		});
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("sentry_kayleen_q0263_03.htm"))
		{
			st.setCond(1);
		}
		else if(event.equals("sentry_kayleen_q0263_06.htm"))
		{
			st.finishQuest();
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
			if(st.getPlayer().getLevel() >= 8 && st.getPlayer().getRace() == Race.DARKELF)
				htmltext = "sentry_kayleen_q0263_02.htm";
			else if(st.getPlayer().getRace() != Race.DARKELF)
				htmltext = "sentry_kayleen_q0263_00.htm";
			else if(st.getPlayer().getLevel() < 8)
				htmltext = "sentry_kayleen_q0263_01.htm";
		}
		else if(cond == 1)
			if(st.getQuestItemsCount(ORC_AMULET) == 0 && st.getQuestItemsCount(ORC_NECKLACE) == 0)
				htmltext = "sentry_kayleen_q0263_04.htm";
			else if(st.getQuestItemsCount(ORC_AMULET) + st.getQuestItemsCount(ORC_NECKLACE) >= 10)
			{
				htmltext = "sentry_kayleen_q0263_05.htm";
				st.giveItems(ADENA_ID, st.getQuestItemsCount(ORC_AMULET) * 20 + st.getQuestItemsCount(ORC_NECKLACE) * 30 + 1100, true, true);
				st.takeItems(ORC_AMULET, -1);
				st.takeItems(ORC_NECKLACE, -1);
			}
			else
			{
				htmltext = "sentry_kayleen_q0263_05.htm";
				st.giveItems(ADENA_ID, st.getQuestItemsCount(ORC_AMULET) * 20 + st.getQuestItemsCount(ORC_NECKLACE) * 30, true, true);
				st.takeItems(ORC_AMULET, -1);
				st.takeItems(ORC_NECKLACE, -1);
			}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(st.getCond() == 1 && Rnd.chance(60))
		{
			if(npcId == BALOR_ORC_ARCHER)
				st.giveItems(ORC_AMULET, 1, true, true);
			else if(npcId == BALOR_ORC_FIGHTER || npcId == BALOR_ORC_FIGHTER_LEADER || npcId == BALOR_ORC_LIEUTENANT)
				st.giveItems(ORC_NECKLACE, 1, true, true);
			st.playSound(SOUND_ITEMGET);
		}
		return null;
	}
}
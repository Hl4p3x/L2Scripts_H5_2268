package quests;

import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _634_InSearchofDimensionalFragments extends QuestScript
{
	int DIMENSION_FRAGMENT_ID = 7079;

	public _634_InSearchofDimensionalFragments()
	{
		super(PARTY_ONE, REPEATABLE);

		for(int npcId = 31494; npcId < 31508; npcId++)
		{
			addTalkId(npcId);
			addStartNpc(npcId);
		}

		for(int mobs = 21208; mobs < 21256; mobs++)
			addKillId(mobs);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("quest_accept"))
		{
			htmltext = "dimension_keeper_1_q0634_03.htm";
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("634_2"))
		{
			htmltext = "dimension_keeper_1_q0634_06.htm";
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
			if(st.getPlayer().getLevel() > 20)
				htmltext = "dimension_keeper_1_q0634_01.htm";
			else
				htmltext = "dimension_keeper_1_q0634_02.htm";
		}
		else
			htmltext = "dimension_keeper_1_q0634_04.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		st.rollAndGive(DIMENSION_FRAGMENT_ID, 1, 60 * Experience.penaltyModifier(st.calculateLevelDiffForDrop(npc.getLevel(), st.getPlayer().getLevel()), 9) * npc.getTemplate().rateHp / 4);
		return null;
	}
}
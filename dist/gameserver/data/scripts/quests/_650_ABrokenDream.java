package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _650_ABrokenDream extends QuestScript
{
	// NPC
	private static final int RailroadEngineer = 32054;
	// mobs
	private static final int ForgottenCrewman = 22027;
	private static final int VagabondOfTheRuins = 22028;
	// QuestItem
	private static final int RemnantsOfOldDwarvesDreams = 8514;

	public _650_ABrokenDream()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(RailroadEngineer);

		addKillId(ForgottenCrewman);
		addKillId(VagabondOfTheRuins);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("quest_accept"))
		{
			htmltext = "ghost_of_railroadman_q0650_0103.htm";
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("650_4"))
		{
			htmltext = "ghost_of_railroadman_q0650_0205.htm";
			st.finishQuest();
			st.unset("cond");
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		String htmltext = NO_QUEST_DIALOG;
		if(cond == 0)
		{
			if(st.getPlayer().isQuestCompleted(117))
			{
				if(st.getPlayer().getLevel() < 39)
					htmltext = "ghost_of_railroadman_q0650_0102.htm";
				else
					htmltext = "ghost_of_railroadman_q0650_0101.htm";
			}
			else
				htmltext = "ghost_of_railroadman_q0650_0104.htm";
		}
		else if(cond == 1)
			htmltext = "ghost_of_railroadman_q0650_0202.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		st.rollAndGive(RemnantsOfOldDwarvesDreams, 1, 1, 68);
		return null;
	}

}
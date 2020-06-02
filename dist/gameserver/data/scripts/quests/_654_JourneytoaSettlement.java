package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _654_JourneytoaSettlement extends QuestScript
{
	// NPC
	private static final int NamelessSpirit = 31453;

	// Mobs
	private static final int CanyonAntelope = 21294;
	private static final int CanyonAntelopeSlave = 21295;

	// Items
	private static final int AntelopeSkin = 8072;

	// Rewards
	private static final int FrintezzasMagicForceFieldRemovalScroll = 8073;

	public _654_JourneytoaSettlement()
	{
		super(PARTY_ONE, REPEATABLE);

		addStartNpc(NamelessSpirit);
		addKillId(CanyonAntelope, CanyonAntelopeSlave);
		addQuestItem(AntelopeSkin);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("printessa_spirit_q0654_03.htm"))
		{
			st.setCond(1);
		}
		if(event.equalsIgnoreCase("printessa_spirit_q0654_04.htm"))
			st.setCond(2);
		if(event.equalsIgnoreCase("printessa_spirit_q0654_07.htm"))
		{
			st.giveItems(FrintezzasMagicForceFieldRemovalScroll, 1, false, false);
			st.finishQuest();
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(npc.getNpcId() == NamelessSpirit)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 74 || !st.getPlayer().isQuestCompleted(119))
					return "printessa_spirit_q0654_02.htm";
				return "printessa_spirit_q0654_01.htm";
			}
			if(cond == 1)
				return "printessa_spirit_q0654_03.htm";
			if(cond == 3)
				return "printessa_spirit_q0654_06.htm";
		}
		else
			htmltext = NO_QUEST_DIALOG;
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 2 && Rnd.chance(5))
		{
			st.setCond(3);
			st.giveItems(AntelopeSkin, 1, false, false);
		}
		return null;
	}
}
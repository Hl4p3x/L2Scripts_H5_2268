package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _645_GhostsOfBatur extends QuestScript
{
	//Npc
	private static final int Karuda = 32017;
	//Items
	private static final int CursedBurialItems = 14861;
	//Mobs
	private static final int[] MOBS = {
			22703,
			22704,
			22705,
			22706,
			22707
	};

	public _645_GhostsOfBatur()
	{
		super(PARTY_ONE, REPEATABLE);

		addStartNpc(Karuda);
		for(int i : MOBS)
			addKillId(i);
		addQuestItem(CursedBurialItems);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("karuda_q0645_0103.htm"))
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
			if(st.getPlayer().getLevel() < 80)
				htmltext = "karuda_q0645_0102.htm";
			else
				htmltext = "karuda_q0645_0101.htm";
		}
		else
		{
			if(cond == 2)
				st.setCond(1);

			if(st.getQuestItemsCount(CursedBurialItems) == 0)
				htmltext = "karuda_q0645_0106.htm";
			else
				htmltext = "karuda_q0645_0105.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() > 0)
			if(Rnd.chance(5))
				st.giveItems(CursedBurialItems, 1, true, true);
		return null;
	}
}
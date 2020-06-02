package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * @author pchayka
 */

public class _279_TargetOfOpportunity extends QuestScript
{
	private static final int Jerian = 32302;
	private static final int CosmicScout = 22373;
	private static final int CosmicWatcher = 22374;
	private static final int CosmicPriest = 22375;
	private static final int CosmicLord = 22376;

	private static final int SealComponentsPart1 = 15517;
	private static final int SealComponentsPart2 = 15518;
	private static final int SealComponentsPart3 = 15519;
	private static final int SealComponentsPart4 = 15520;

	public _279_TargetOfOpportunity()
	{
		super(PARTY_ALL, REPEATABLE);
		addStartNpc(Jerian);
		addKillId(CosmicScout, CosmicWatcher, CosmicPriest, CosmicLord);
		addQuestItem(SealComponentsPart1, SealComponentsPart2, SealComponentsPart3, SealComponentsPart4);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("jerian_q279_04.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("jerian_q279_07.htm"))
		{
			st.takeAllItems(SealComponentsPart1, SealComponentsPart2, SealComponentsPart3, SealComponentsPart4);
			st.giveItems(15515, 1, false, false);
			st.giveItems(15516, 1, false, false);
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Jerian)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 82)
					htmltext = "jerian_q279_01.htm";
				else
					htmltext = "jerian_q279_00.htm";
			}
			else if(cond == 1)
				htmltext = "jerian_q279_05.htm";
			else if(cond == 2)
				htmltext = "jerian_q279_06.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 1)
		{
			if(npcId == CosmicScout && st.getQuestItemsCount(SealComponentsPart1) < 1 && Rnd.chance(15))
				st.giveItems(SealComponentsPart1, 1, false, false);
			else if(npcId == CosmicWatcher && st.getQuestItemsCount(SealComponentsPart2) < 1 && Rnd.chance(15))
				st.giveItems(SealComponentsPart2, 1, false, false);
			else if(npcId == CosmicPriest && st.getQuestItemsCount(SealComponentsPart3) < 1 && Rnd.chance(15))
				st.giveItems(SealComponentsPart3, 1, false, false);
			else if(npcId == CosmicLord && st.getQuestItemsCount(SealComponentsPart4) < 1 && Rnd.chance(15))
				st.giveItems(SealComponentsPart4, 1, false, false);

			if(st.getQuestItemsCount(SealComponentsPart1) >= 1 && st.getQuestItemsCount(SealComponentsPart2) >= 1 && st.getQuestItemsCount(SealComponentsPart3) >= 1 && st.getQuestItemsCount(SealComponentsPart4) >= 1)
				st.setCond(2);
		}
		return null;
	}
}
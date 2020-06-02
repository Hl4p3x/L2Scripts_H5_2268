package quests;

import org.apache.commons.lang3.ArrayUtils;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * @author pchayka
 */
public class _252_GoodSmell extends QuestScript
{
	private static final int GuardStan = 30200;
	private static final int[] SelMahums = {
			22786,
			22787,
			22788
	};
	private static final int SelChef = 18908;
	private static final int SelMahumDiary = 15500;
	private static final int SelMahumCookbookPage = 15501;

	public _252_GoodSmell()
	{
		super(PARTY_NONE, ONETIME);
		addStartNpc(GuardStan);
		addKillId(SelMahums[0], SelMahums[1], SelMahums[2], SelChef);
		addQuestItem(SelMahumDiary, SelMahumCookbookPage);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("stan_q252_03.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("stan_q252_06.htm"))
		{
			st.takeAllItems(SelMahumDiary, SelMahumCookbookPage);
			st.giveItems(57, 147656, true, true);
			st.addExpAndSp(716238, 78324);
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(npc.getNpcId() == GuardStan)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 82)
					htmltext = "stan_q252_01.htm";
				else
					htmltext = "stan_q252_00.htm";
			}
			else if(cond == 1)
				htmltext = "stan_q252_04.htm";
			else if(cond == 2)
				htmltext = "stan_q252_05.htm";
		}

		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 1)
		{
			if(st.getQuestItemsCount(SelMahumDiary) < 10 && ArrayUtils.contains(SelMahums, npc.getNpcId()))
				st.rollAndGive(SelMahumDiary, 1, 15);
			if(st.getQuestItemsCount(SelMahumCookbookPage) < 5 && npc.getNpcId() == SelChef)
				st.rollAndGive(SelMahumCookbookPage, 1, 10);
			if(st.getQuestItemsCount(SelMahumDiary) >= 10 && st.getQuestItemsCount(SelMahumCookbookPage) >= 5)
				st.setCond(2);
		}
		return null;
	}
}
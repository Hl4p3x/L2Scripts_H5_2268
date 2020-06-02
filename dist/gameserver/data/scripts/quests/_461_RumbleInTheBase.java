package quests;

import org.apache.commons.lang3.ArrayUtils;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * @author pchayka
 */

public class _461_RumbleInTheBase extends QuestScript
{
	private static final int Stan = 30200;
	private static final int ShoesStringofSelMahum = 16382;
	private static final int ShinySalmon = 15503;
	private static final int[] SelMahums = {
			22786,
			22787,
			22788
	};
	private static final int SelChef = 18908;

	public _461_RumbleInTheBase()
	{
		super(PARTY_NONE, DAILY);
		addStartNpc(Stan);
		addQuestItem(ShoesStringofSelMahum, ShinySalmon);
		addKillId(SelMahums);
		addKillId(SelChef);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("stan_q461_03.htm"))
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
		if(npc.getNpcId() == Stan)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 82 && st.getPlayer().isQuestCompleted(252))
					htmltext = "stan_q461_01.htm";
				else
					htmltext = "stan_q461_00.htm";
			}
			else if(cond == 1)
				htmltext = "stan_q461_04.htm";
			else if(cond == 2)
			{
				htmltext = "stan_q461_05.htm";
				st.takeAllItems(ShoesStringofSelMahum);
				st.takeAllItems(ShinySalmon);
				st.addExpAndSp(224784, 342528);
				st.finishQuest();
			}
		}
		return htmltext;
	}

	@Override
	public String onCompleted(NpcInstance npc, QuestState st)
	{
		String htmltext = COMPLETED_DIALOG;
		int npcId = npc.getNpcId();
		if(npcId == Stan)
			htmltext = "stan_q461_00a.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		int npcId = npc.getNpcId();
		if(cond == 1)
		{
			if(st.getQuestItemsCount(ShoesStringofSelMahum) < 10 && st.getQuestItemsCount(ShinySalmon) < 5)
			{
				if(st.getQuestItemsCount(ShoesStringofSelMahum) < 10 && ArrayUtils.contains(SelMahums, npcId))
					st.rollAndGive(ShoesStringofSelMahum, 1, 20);
				if(st.getQuestItemsCount(ShinySalmon) < 5 && npcId == SelChef)
					st.rollAndGive(ShinySalmon, 1, 10);
			}
			else
				st.setCond(2);
		}
		return null;
	}
}
package quests;

import org.apache.commons.lang3.ArrayUtils;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _289_DeliciousFoodsAreMine extends QuestScript
{
	private static final int GuardStan = 30200;
	private static final int FoulFruit = 15507;
	private static final int FullBarrelofSoup = 15712;
	private static final int EmptySoupBarrel = 15713;
	private static final int[] SelMahums = {
			22786,
			22787,
			22788
	};
	private static final int SelChef = 18908;

	public _289_DeliciousFoodsAreMine()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(GuardStan);
		addQuestItem(FoulFruit, FullBarrelofSoup, EmptySoupBarrel);
		addKillId(SelMahums);
		addKillId(SelChef);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("stan_q289_03.htm"))
		{
			st.setCond(1);
			st.giveItems(FoulFruit, 500, false, false);
		}
		else if(event.equalsIgnoreCase("stan_q289_05.htm"))
		{
			st.giveItems(FoulFruit, 500, false, false);
		}
		else if(event.equalsIgnoreCase("continue"))
		{
			htmltext = "stan_q289_11.htm";
		}
		else if(event.equalsIgnoreCase("quit"))
		{
			htmltext = "stan_q289_12.htm";
			st.finishQuest();
		}
		else if(event.equalsIgnoreCase("icarus"))
		{
			if(st.getQuestItemsCount(FullBarrelofSoup) < 500)
				htmltext = "stan_q289_07.htm";
			else
			{
				st.takeItems(FullBarrelofSoup, 500);
				switch(Rnd.get(1, 5))
				{
					case 1:
						st.giveItems(10377, 1, false, false);
						break;
					case 2:
						st.giveItems(10401, 3, false, false);
						break;
					case 3:
						st.giveItems(10401, 4, false, false);
						break;
					case 4:
						st.giveItems(10401, 5, false, false);
						break;
					case 5:
						st.giveItems(10401, 6, false, false);
						break;
				}
				st.playSound(SOUND_MIDDLE);
				htmltext = "stan_q289_08.htm";
			}
		}
		else if(event.equalsIgnoreCase("moirai"))
		{
			if(st.getQuestItemsCount(FullBarrelofSoup) < 100)
				htmltext = "stan_q289_09.htm";
			else
			{
				st.takeItems(FullBarrelofSoup, 100);
				switch(Rnd.get(1, 18))
				{
					case 1:
						st.giveItems(15775, 1, false, false);
						break;
					case 2:
						st.giveItems(15778, 1, false, false);
						break;
					case 3:
						st.giveItems(15781, 1, false, false);
						break;
					case 4:
						st.giveItems(15784, 1, false, false);
						break;
					case 5:
						st.giveItems(15787, 1, false, false);
						break;
					case 6:
						st.giveItems(15791, 1, false, false);
						break;
					case 7:
						st.giveItems(15812, 1, false, false);
						break;
					case 8:
						st.giveItems(15813, 1, false, false);
						break;
					case 9:
						st.giveItems(15814, 1, false, false);
						break;
					case 10:
						st.giveItems(15645, 3, false, false);
						break;
					case 11:
						st.giveItems(15648, 3, false, false);
						break;
					case 12:
						st.giveItems(15651, 3, false, false);
						break;
					case 13:
						st.giveItems(15654, 3, false, false);
						break;
					case 14:
						st.giveItems(15657, 3, false, false);
						break;
					case 15:
						st.giveItems(15693, 3, false, false);
						break;
					case 16:
						st.giveItems(15772, 3, false, false);
						break;
					case 17:
						st.giveItems(15773, 3, false, false);
						break;
					case 18:
						st.giveItems(15774, 3, false, false);
						break;
				}
				st.playSound(SOUND_MIDDLE);
				htmltext = "stan_q289_10.htm";
			}
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
				if(st.getPlayer().getLevel() >= 82 && st.getPlayer().isQuestCompleted(252))
					htmltext = "stan_q289_01.htm";
				else
					htmltext = "stan_q289_00.htm";
			}
			else if(cond == 1 && (st.getQuestItemsCount(FullBarrelofSoup) + (st.getQuestItemsCount(EmptySoupBarrel) * 2)) < 100)
				htmltext = "stan_q289_04.htm";
			else if(cond == 1 && (st.getQuestItemsCount(FullBarrelofSoup) + (st.getQuestItemsCount(EmptySoupBarrel) * 2)) >= 100)
			{
				if(st.getQuestItemsCount(EmptySoupBarrel) >= 2)
				{
					st.giveItems(FullBarrelofSoup, st.getQuestItemsCount(EmptySoupBarrel) / 2, true, true);
					st.takeAllItems(EmptySoupBarrel);
				}
				htmltext = "stan_q289_06.htm";
			}
		}

		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 1)
		{
			if(ArrayUtils.contains(SelMahums, npc.getNpcId()) || npc.getNpcId() == SelChef)
				if(!st.rollAndGive(FullBarrelofSoup, 1, 15))
					st.rollAndGive(EmptySoupBarrel, 1, 100);
		}
		return null;
	}
}
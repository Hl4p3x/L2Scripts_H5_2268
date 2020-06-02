package quests;

import java.util.StringTokenizer;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * @author pchayka
 */

public class _10289_FadeToBlack extends QuestScript
{
	private static final int Greymore = 32757;
	private static final int Anays = 25701;
	private static final int MarkofSplendor = 15527;
	private static final int MarkofDarkness = 15528;

	public _10289_FadeToBlack()
	{
		super(PARTY_ALL, ONETIME);
		addStartNpc(Greymore);
		addKillId(Anays);
		addQuestItem(MarkofSplendor, MarkofDarkness);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("greymore_q10289_03.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("showmark"))
		{
			if(st.getCond() == 2 && st.getQuestItemsCount(MarkofDarkness) > 0)
			{
				st.takeItems(MarkofDarkness, -1);
				st.addExpAndSp(55983, 136500);
				htmltext = "greymore_q10289_06.htm";
			}	
			else if(st.getCond() == 3 && st.getQuestItemsCount(MarkofSplendor) > 0)
				htmltext = "greymore_q10289_07.htm";
			else
				htmltext = "greymore_q10289_08.htm";
		}
		else if(event.startsWith("exchange"))
		{
			StringTokenizer str = new StringTokenizer(event);
			str.nextToken();
			int id = Integer.parseInt(str.nextToken());
			switch(id)
			{
				case 1:
					st.giveItems(15775, 1, false, false);
					st.giveItems(ADENA_ID, 420920, true, true);
					break;
				case 2:
					st.giveItems(15776, 1, false, false);
					st.giveItems(ADENA_ID, 420920, true, true);
					break;
				case 3:
					st.giveItems(15777, 1, false, false);
					st.giveItems(ADENA_ID, 420920, true, true);
					break;
				case 4:
					st.giveItems(15778, 1, false, false);
					break;
				case 5:
					st.giveItems(15779, 1, false, false);
					st.giveItems(ADENA_ID, 168360, true, true);
					break;
				case 6:
					st.giveItems(15780, 1, false, false);
					st.giveItems(ADENA_ID, 168360, true, true);
					break;
				case 7:
					st.giveItems(15781, 1, false, false);
					st.giveItems(ADENA_ID, 252540, true, true);
					break;
				case 8:
					st.giveItems(15782, 1, false, false);
					st.giveItems(ADENA_ID, 357780, true, true);
					break;
				case 9:
					st.giveItems(15783, 1, false, false);
					st.giveItems(ADENA_ID, 357780, true, true);
					break;
				case 10:
					st.giveItems(15784, 1, false, false);
					st.giveItems(ADENA_ID, 505100, true, true);
					break;
				case 11:
					st.giveItems(15785, 1, false, false);
					st.giveItems(ADENA_ID, 505100, true, true);
					break;
				case 12:
					st.giveItems(15786, 1, false, false);
					st.giveItems(ADENA_ID, 505100, true, true);
					break;
				case 13:
					st.giveItems(15787, 1, false, false);
					st.giveItems(ADENA_ID, 505100, true, true);
					break;
				case 14:
					st.giveItems(15788, 1, false, false);
					st.giveItems(ADENA_ID, 505100, true, true);
					break;
				case 15:
					st.giveItems(15789, 1, false, false);
					st.giveItems(ADENA_ID, 505100, true, true);
					break;
				case 16:
					st.giveItems(15790, 1, false, false);
					st.giveItems(ADENA_ID, 496680, true, true);
					break;
				case 17:
					st.giveItems(15791, 1, false, false);
					st.giveItems(ADENA_ID, 496680, true, true);
					break;
				case 18:
					st.giveItems(15812, 1, false, false);
					st.giveItems(ADENA_ID, 563860, true, true);
					break;
				case 19:
					st.giveItems(15813, 1, false, false);
					st.giveItems(ADENA_ID, 509040, true, true);
					break;
				case 20:
					st.giveItems(15814, 1, false, false);
					st.giveItems(ADENA_ID, 454240, true, true);
					break;
			}
			htmltext = "greymore_q10289_09.htm";
			st.takeAllItems(MarkofSplendor, MarkofDarkness);
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(npc.getNpcId() == Greymore)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 82 && st.getPlayer().isQuestCompleted(10288))
					htmltext = "greymore_q10289_01.htm";
				else
					htmltext = "greymore_q10289_00.htm";
			}
			else if(cond == 1)
				htmltext = "greymore_q10289_04.htm";
			else if(cond == 2 || cond == 3)
				htmltext = "greymore_q10289_05.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 1)
		{
			if(npc.getNpcId() == Anays)
			{
				if(Rnd.chance(30))
				{
					st.giveItems(MarkofSplendor, 1, false, false);
					st.setCond(3);
				}
				else
				{
					st.giveItems(MarkofDarkness, 1, false, false);
					st.setCond(2);
				}
			}
		}
		return null;
	}
}
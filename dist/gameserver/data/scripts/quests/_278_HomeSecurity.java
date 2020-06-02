package quests;

import org.apache.commons.lang3.ArrayUtils;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * @author pchayka
 */

public class _278_HomeSecurity extends QuestScript
{
	private static final int Tunatun = 31537;
	private static final int[] FarmMonsters = {18905, 18906, 18907};
	private static final int SelMahumMane = 15531;

	public _278_HomeSecurity()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(Tunatun);
		addKillId(FarmMonsters);
		addQuestItem(SelMahumMane);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("tunatun_q278_03.htm"))
		{
			st.setCond(1);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Tunatun)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 82)
					htmltext = "tunatun_q278_01.htm";
				else
					htmltext = "tunatun_q278_00.htm";
			}
			else if(cond == 1)
				htmltext = "tunatun_q278_04.htm";
			else if(cond == 2)
			{
				if(st.getQuestItemsCount(SelMahumMane) >= 300)
				{
					htmltext = "tunatun_q278_05.htm";
					st.takeAllItems(SelMahumMane);
					switch(Rnd.get(1, 13))
					{
						case 1:
							st.giveItems(960, 1, false, false);
							break;
						case 2:
							st.giveItems(960, 2, false, false);
							break;
						case 3:
							st.giveItems(960, 3, false, false);
							break;
						case 4:
							st.giveItems(960, 4, false, false);
							break;
						case 5:
							st.giveItems(960, 5, false, false);
							break;
						case 6:
							st.giveItems(960, 6, false, false);
							break;
						case 7:
							st.giveItems(960, 7, false, false);
							break;
						case 8:
							st.giveItems(960, 8, false, false);
							break;
						case 9:
							st.giveItems(960, 9, false, false);
							break;
						case 10:
							st.giveItems(960, 10, false, false);
							break;
						case 11:
							st.giveItems(9553, 1, false, false);
							break;
						case 12:
							st.giveItems(9553, 2, false, false);
							break;
						case 13:
							st.giveItems(959, 1, false, false);
							break;
					}
					st.finishQuest();
				}
				else
					htmltext = "tunatun_q278_04.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 1)
			if(ArrayUtils.contains(FarmMonsters, npcId) && st.getQuestItemsCount(SelMahumMane) < 300)
			{
				st.giveItems(SelMahumMane, 1, true, true);
				if(st.getQuestItemsCount(SelMahumMane) >= 300)
					st.setCond(2);
			}
		return null;
	}
}
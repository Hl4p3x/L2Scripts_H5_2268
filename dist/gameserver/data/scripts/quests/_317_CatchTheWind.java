package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * Квест Catch The Wind
 *
 * @author Sergey Ibryaev aka Artful
 */

public class _317_CatchTheWind extends QuestScript
{
	//NPCs
	private static int Rizraell = 30361;
	//Quest Items
	private static int WindShard = 1078;
	//Mobs
	private static int Lirein = 20036;
	private static int LireinElder = 20044;
	//Drop Cond
	//# [COND, NEWCOND, ID, REQUIRED, ITEM, NEED_COUNT, CHANCE, DROP]
	public final int[][] DROPLIST_COND = {
			{
					1,
					0,
					Lirein,
					0,
					WindShard,
					0,
					60,
					1
			},
			{
					1,
					0,
					LireinElder,
					0,
					WindShard,
					0,
					60,
					1
			}
	};

	public _317_CatchTheWind()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(Rizraell);
		//Mob Drop
		for(int i = 0; i < DROPLIST_COND.length; i++)
			addKillId(DROPLIST_COND[i][2]);
		addQuestItem(WindShard);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("rizraell_q0317_04.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("rizraell_q0317_08.htm"))
		{
			st.finishQuest();
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(npcId == Rizraell)
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 18)
					htmltext = "rizraell_q0317_03.htm";
				else
					htmltext = "rizraell_q0317_02.htm";
			}
			else if(cond == 1)
			{
				long count = st.getQuestItemsCount(WindShard);
				if(count > 0)
				{
					st.takeItems(WindShard, -1);
					st.giveItems(ADENA_ID, 40 * count, true, true);
					htmltext = "rizraell_q0317_07.htm";
				}
				else
					htmltext = "rizraell_q0317_05.htm";
			}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		for(int i = 0; i < DROPLIST_COND.length; i++)
			if(cond == DROPLIST_COND[i][0] && npcId == DROPLIST_COND[i][2])
				if(DROPLIST_COND[i][3] == 0 || st.getQuestItemsCount(DROPLIST_COND[i][3]) > 0)
					if(DROPLIST_COND[i][5] == 0)
						st.rollAndGive(DROPLIST_COND[i][4], DROPLIST_COND[i][7], DROPLIST_COND[i][6]);
					else if(st.rollAndGive(DROPLIST_COND[i][4], DROPLIST_COND[i][7], DROPLIST_COND[i][7], DROPLIST_COND[i][5], DROPLIST_COND[i][6]))
						if(DROPLIST_COND[i][1] != cond && DROPLIST_COND[i][1] != 0)
						{
							st.setCond(Integer.valueOf(DROPLIST_COND[i][1]));
						}
		return null;
	}
}
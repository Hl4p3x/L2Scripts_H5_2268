package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _601_WatchingEyes extends QuestScript
{
	//NPC
	private static int EYE_OF_ARGOS = 31683;
	//ITEMS
	private static int PROOF_OF_AVENGER = 7188;
	//CHANCE
	private static int DROP_CHANCE = 50;
	//MOBS
	private static int[] MOBS = {
			21306,
			21308,
			21309,
			21310,
			21311
	};
	private static int[][] REWARDS = {
			{
					6699,
					90000,
					0,
					19
			},
			{
					6698,
					80000,
					20,
					39
			},
			{
					6700,
					40000,
					40,
					49
			},
			{
					0,
					230000,
					50,
					100
			}
	};

	public _601_WatchingEyes()
	{
		super(PARTY_ONE, REPEATABLE);

		addStartNpc(EYE_OF_ARGOS);

		addKillId(MOBS);

		addQuestItem(PROOF_OF_AVENGER);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("eye_of_argos_q0601_0104.htm"))
		{
			if(st.getPlayer().getLevel() < 71)
				htmltext = "eye_of_argos_q0601_0103.htm";
			else
				st.setCond(1);
		}
		else if(event.equalsIgnoreCase("eye_of_argos_q0601_0201.htm"))
		{
			int random = Rnd.get(101);
			int i = 0;
			int item = 0;
			int adena = 0;
			while(i < REWARDS.length)
			{
				item = REWARDS[i][0];
				adena = REWARDS[i][1];
				if(REWARDS[i][2] <= random && random <= REWARDS[i][3])
					break;
				i++;
			}
			st.giveItems(ADENA_ID, adena, true, true);
			if(item != 0)
			{
				st.giveItems(item, 5, true, true);
				st.addExpAndSp(120000, 10000);
			}
			st.takeItems(PROOF_OF_AVENGER, -1);
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(cond == 0)
			htmltext = "eye_of_argos_q0601_0101.htm";
		else if(cond == 1)
			htmltext = "eye_of_argos_q0601_0106.htm";
		else if(cond == 2 && st.getQuestItemsCount(PROOF_OF_AVENGER) >= 100)
			htmltext = "eye_of_argos_q0601_0105.htm";
		else
		{
			htmltext = "eye_of_argos_q0601_0202.htm";
			st.setCond(1);
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1)
		{
			long count = st.getQuestItemsCount(PROOF_OF_AVENGER);
			if(count < 100 && Rnd.chance(DROP_CHANCE))
			{
				st.giveItems(PROOF_OF_AVENGER, 1, true, true);
				if(st.getQuestItemsCount(PROOF_OF_AVENGER) > 99)
				{
					st.setCond(2);
				}
				else
					st.playSound(SOUND_ITEMGET);
			}
		}
		return null;
	}
}
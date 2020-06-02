package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * @author pchayka
 */

public class _458_PerfectForm extends QuestScript
{
	private static final int Kelleyia = 32768;

	private static final String KOOKABURA_VAR = "kookaburacount";
	private static final String COUGAR_VAR = "cougarcount";
	private static final String BUFFALO_VAR = "buffalocount";
	private static final String GRENDEL_VAR = "grendelcount";

	private static final int COUNT = 10;

	// client  4    "1018879|1018886|1018893|1018900"    4    "10|10|10|10"
	private static final int[] GrownKookabura = {
			18879,
			18878
	};
	private static final int[] GrownCougar = {
			18886,
			18885
	};
	private static final int[] GrownBuffalo = {
			18893,
			18892
	};
	private static final int[] GrownGrendel = {
			18900,
			18899
	};

	private static final int[][][] Rewards = {
			{
					{
							10373,
							1
					},
					{
							10374,
							1
					},
					{
							10375,
							1
					},
					{
							10376,
							1
					},
					{
							10377,
							1
					},
					{
							10378,
							1
					},
					{
							10379,
							1
					},
					{
							10380,
							1
					},
					{
							10381,
							1
					}
			},
			{
					{
							10397,
							5
					},
					{
							10398,
							5
					},
					{
							10399,
							5
					},
					{
							10400,
							5
					},
					{
							10401,
							5
					},
					{
							10402,
							5
					},
					{
							10403,
							5
					},
					{
							10404,
							5
					},
					{
							10405,
							5
					}
			},
			{
					{
							10397,
							2
					},
					{
							10398,
							2
					},
					{
							10399,
							2
					},
					{
							10400,
							2
					},
					{
							10401,
							2
					},
					{
							10402,
							2
					},
					{
							10403,
							2
					},
					{
							10404,
							2
					},
					{
							10405,
							2
					}
			}
	};

	public _458_PerfectForm()
	{
		super(PARTY_NONE, DAILY);

		addKillNpcWithLog(1, KOOKABURA_VAR, COUNT, GrownKookabura);
		addKillNpcWithLog(1, COUGAR_VAR, COUNT, GrownCougar);
		addKillNpcWithLog(1, BUFFALO_VAR, COUNT, GrownBuffalo);
		addKillNpcWithLog(1, GRENDEL_VAR, COUNT, GrownGrendel);

		addStartNpc(Kelleyia);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("kelleyia_q458_05.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("request_1stresults"))
		{
			switch(st.getInt("normaloverhits"))
			{
				case 1:
					htmltext = "kelleyia_q458_08a.htm";
					break;
				case 2:
					htmltext = "kelleyia_q458_08b.htm";
					break;
				case 3:
					htmltext = "kelleyia_q458_08c.htm";
					break;
			}
		}
		else if(event.equalsIgnoreCase("request_2ndresults"))
		{
			switch(st.getInt("critoverhits"))
			{
				case 1:
					htmltext = "kelleyia_q458_09a.htm";
					break;
				case 2:
					htmltext = "kelleyia_q458_09b.htm";
					break;
				case 3:
					htmltext = "kelleyia_q458_09c.htm";
					break;
			}
		}
		else if(event.equalsIgnoreCase("request_3rdresults"))
		{
			switch(st.getInt("contoverhits"))
			{
				case 1:
					htmltext = "kelleyia_q458_10a.htm";
					break;
				case 2:
					htmltext = "kelleyia_q458_10b.htm";
					break;
				case 3:
					htmltext = "kelleyia_q458_10c.htm";
					break;
			}
		}
		else if(event.equalsIgnoreCase("request_reward"))
		{
			int[] reward;
			switch(st.getInt("contoverhits"))
			{
				case 1:
					reward = Rewards[0][Rnd.get(Rewards[0].length)];
					st.giveItems(reward[0], reward[1], false, false);
					break;
				case 2:
					reward = Rewards[1][Rnd.get(Rewards[1].length)];
					st.giveItems(reward[0], reward[1], false, false);
					break;
				case 3:
					reward = Rewards[2][Rnd.get(Rewards[2].length)];
					st.giveItems(reward[0], reward[1], false, false);
					st.giveItems(15482, 10, false, false);
					st.giveItems(15483, 10, false, false);
					break;
			}
			htmltext = "kelleyia_q458_11.htm";
			st.unset(KOOKABURA_VAR);
			st.unset(COUGAR_VAR);
			st.unset(BUFFALO_VAR);
			st.unset(GRENDEL_VAR);
			st.unset("normaloverhits");
			st.unset("critoverhits");
			st.unset("contoverhits");
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(npc.getNpcId() == Kelleyia)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 82)
					htmltext = "kelleyia_q458_01.htm";
				else
					htmltext = "kelleyia_q458_00.htm";
			}
			else if(cond == 1)
				htmltext = "kelleyia_q458_06.htm";
			else if(cond == 2)
				htmltext = "kelleyia_q458_07.htm";
		}
		return htmltext;
	}

	@Override
	public String onCompleted(NpcInstance npc, QuestState st)
	{
		String htmltext = COMPLETED_DIALOG;
		int npcId = npc.getNpcId();
		if(npcId == Kelleyia)
			htmltext = "kelleyia_q458_00a.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		boolean doneCond = updateKill(npc, st);
		if(doneCond)
		{
			st.set("normaloverhits", Rnd.get(1, 3));
			st.set("critoverhits", Rnd.get(1, 3));
			st.set("contoverhits", Rnd.get(1, 3));
			st.setCond(2);
		}
		return null;
	}
}
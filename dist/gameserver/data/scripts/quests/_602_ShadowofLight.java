package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * Квест Shadowof Light
 *
 * @author Sergey Ibryaev aka Artful
 */

public class _602_ShadowofLight extends QuestScript
{
	//NPC
	private static final int ARGOS = 31683;
	//Quest Item
	private static final int EYE_OF_DARKNESS = 7189;
	//Bonus
	private static final int[][] REWARDS = {
			{
					6699,
					40000,
					120000,
					20000,
					1,
					19
			},
			{
					6698,
					60000,
					110000,
					15000,
					20,
					39
			},
			{
					6700,
					40000,
					150000,
					10000,
					40,
					49
			},
			{
					0,
					100000,
					140000,
					11250,
					50,
					100
			}
	};

	public _602_ShadowofLight()
	{
		super(PARTY_ONE, REPEATABLE);

		addStartNpc(ARGOS);

		addKillId(21299);
		addKillId(21304);

		addQuestItem(EYE_OF_DARKNESS);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("eye_of_argos_q0602_0104.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("eye_of_argos_q0602_0201.htm"))
		{
			st.takeItems(EYE_OF_DARKNESS, -1);
			int random = Rnd.get(100) + 1;
			for(int i = 0; i < REWARDS.length; i++)
				if(REWARDS[i][4] <= random && random <= REWARDS[i][5])
				{
					st.giveItems(ADENA_ID, REWARDS[i][1], true, true);
					st.addExpAndSp(REWARDS[i][2], REWARDS[i][3]);
					if(REWARDS[i][0] != 0)
						st.giveItems(REWARDS[i][0], 3, true, true);
				}
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(npcId == ARGOS)
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 68)
					htmltext = "eye_of_argos_q0602_0103.htm";
				else
					htmltext = "eye_of_argos_q0602_0101.htm";
			}
			else if(cond == 1)
				htmltext = "eye_of_argos_q0602_0106.htm";
			else if(cond == 2 && st.getQuestItemsCount(EYE_OF_DARKNESS) >= 100)
				htmltext = "eye_of_argos_q0602_0105.htm";
			else
			{
				htmltext = "eye_of_argos_q0602_0106.htm";
				st.setCond(1);
			}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1)
		{
			long count = st.getQuestItemsCount(EYE_OF_DARKNESS);
			if(count < 100 && Rnd.chance(npc.getNpcId() == 21299 ? 35 : 40))
			{
				st.giveItems(EYE_OF_DARKNESS, 1, true, true);
				if(st.getQuestItemsCount(EYE_OF_DARKNESS) > 99)
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

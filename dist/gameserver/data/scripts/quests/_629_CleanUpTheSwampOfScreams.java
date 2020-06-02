package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _629_CleanUpTheSwampOfScreams extends QuestScript
{
	//NPC
	private static int CAPTAIN = 31553;
	private static int CLAWS = 7250;
	private static int COIN = 7251;

	//CHANCES
	private static int[][] CHANCE = {
			{
					21508,
					50
			},
			{
					21509,
					43
			},
			{
					21510,
					52
			},
			{
					21511,
					57
			},
			{
					21512,
					74
			},
			{
					21513,
					53
			},
			{
					21514,
					53
			},
			{
					21515,
					54
			},
			{
					21516,
					55
			},
			{
					21517,
					56
			}
	};

	public _629_CleanUpTheSwampOfScreams()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(CAPTAIN);

		for(int npcId = 21508; npcId < 21518; npcId++)
			addKillId(npcId);

		addQuestItem(CLAWS);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("merc_cap_peace_q0629_0104.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("merc_cap_peace_q0629_0202.htm"))
		{
			if(st.getQuestItemsCount(CLAWS) >= 100)
			{
				st.takeItems(CLAWS, 100);
				st.giveItems(COIN, 20, false, false);
			}
			else
				htmltext = "merc_cap_peace_q0629_0203.htm";
		}
		else if(event.equalsIgnoreCase("merc_cap_peace_q0629_0204.htm"))
		{
			st.takeItems(CLAWS, -1);
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(st.getQuestItemsCount(7246) > 0 || st.getQuestItemsCount(7247) > 0)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 66)
					htmltext = "merc_cap_peace_q0629_0101.htm";
				else
					htmltext = "merc_cap_peace_q0629_0103.htm";
			}
			else
			{
				if(st.getQuestItemsCount(CLAWS) >= 100)
					htmltext = "merc_cap_peace_q0629_0105.htm";
				else
					htmltext = "merc_cap_peace_q0629_0106.htm";
			}
		}
		else
			htmltext = "merc_cap_peace_q0629_0205.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.isStarted())
			st.rollAndGive(CLAWS, 1, CHANCE[npc.getNpcId() - 21508][1]);
		return null;
	}
}
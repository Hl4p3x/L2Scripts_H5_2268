package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _627_HeartInSearchOfPower extends QuestScript
{
	//NPC
	private static final int M_NECROMANCER = 31518;
	private static final int ENFEUX = 31519;

	//ITEMS
	private static final int SEAL_OF_LIGHT = 7170;
	private static final int GEM_OF_SUBMISSION = 7171;
	private static final int GEM_OF_SAINTS = 7172;

	//REWARDS
	private static final int MOLD_HARDENER = 4041;
	private static final int ENRIA = 4042;
	private static final int ASOFE = 4043;
	private static final int THONS = 4044;

	public _627_HeartInSearchOfPower()
	{
		super(PARTY_ONE, REPEATABLE);

		addStartNpc(31518);

		addTalkId(31518);
		addTalkId(31519);

		for(int mobs = 21520; mobs <= 21541; mobs++)
			addKillId(mobs);

		addQuestItem(GEM_OF_SUBMISSION);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("dark_necromancer_q0627_0104.htm"))
		{
			st.setCond(1);
		}
		else if(event.equals("dark_necromancer_q0627_0201.htm"))
		{
			st.takeItems(GEM_OF_SUBMISSION, 300);
			st.giveItems(SEAL_OF_LIGHT, 1, false, false);
			st.setCond(3);
		}
		else if(event.equals("enfeux_q0627_0301.htm"))
		{
			st.takeItems(SEAL_OF_LIGHT, 1);
			st.giveItems(GEM_OF_SAINTS, 1, false, false);
			st.setCond(4);
		}
		else if(event.equals("dark_necromancer_q0627_0401.htm"))
			st.takeItems(GEM_OF_SAINTS, 1);
		else
		{
			if(event.equals("627_11"))
			{
				htmltext = "dark_necromancer_q0627_0402.htm";
				st.giveItems(ADENA_ID, 100000, true, true);
			}
			else if(event.equals("627_12"))
			{
				htmltext = "dark_necromancer_q0627_0402.htm";
				st.giveItems(ASOFE, 13, false, false);
				st.giveItems(ADENA_ID, 6400, true, true);
			}
			else if(event.equals("627_13"))
			{
				htmltext = "dark_necromancer_q0627_0402.htm";
				st.giveItems(THONS, 13, false, false);
				st.giveItems(ADENA_ID, 6400, true, true);
			}
			else if(event.equals("627_14"))
			{
				htmltext = "dark_necromancer_q0627_0402.htm";
				st.giveItems(ENRIA, 6, false, false);
				st.giveItems(ADENA_ID, 13600, true, true);
			}
			else if(event.equals("627_15"))
			{
				htmltext = "dark_necromancer_q0627_0402.htm";
				st.giveItems(MOLD_HARDENER, 3, false, false);
				st.giveItems(ADENA_ID, 17200, true, true);
			}
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == M_NECROMANCER)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 60)
					htmltext = "dark_necromancer_q0627_0101.htm";
				else
					htmltext = "dark_necromancer_q0627_0103.htm";
			}
			else if(cond == 1)
				htmltext = "dark_necromancer_q0627_0106.htm";
			else if(st.getQuestItemsCount(GEM_OF_SUBMISSION) >= 300)
				htmltext = "dark_necromancer_q0627_0105.htm";
			else if(st.getQuestItemsCount(GEM_OF_SAINTS) > 0)
				htmltext = "dark_necromancer_q0627_0301.htm";
		}
		else if(npcId == ENFEUX && st.getQuestItemsCount(SEAL_OF_LIGHT) > 0)
			htmltext = "enfeux_q0627_0201.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		long count = st.getQuestItemsCount(GEM_OF_SUBMISSION);
		if(st.getCond() == 1 && count < 300)
		{
			st.giveItems(GEM_OF_SUBMISSION, 1, true, true);
			if(st.getQuestItemsCount(GEM_OF_SUBMISSION) > 299)
			{
				st.setCond(2);
			}
			else
				st.playSound(SOUND_ITEMGET);
		}
		return null;
	}
}
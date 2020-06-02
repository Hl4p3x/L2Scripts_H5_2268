package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _379_FantasyWine extends QuestScript
{
	//NPC
	public final int HARLAN = 30074;
	//Mobs
	public final int Enku_Orc_Champion = 20291;
	public final int Enku_Orc_Shaman = 20292;
	//Quest Item
	public final int LEAF_OF_EUCALYPTUS = 5893;
	public final int STONE_OF_CHILL = 5894;
	//Item
	public final int[] REWARD = {
			5956,
			5957,
			5958
	};

	public _379_FantasyWine()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(HARLAN);

		addKillId(Enku_Orc_Champion);
		addKillId(Enku_Orc_Shaman);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("hitsran_q0379_06.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("reward"))
		{
			st.takeItems(LEAF_OF_EUCALYPTUS, -1);
			st.takeItems(STONE_OF_CHILL, -1);
			int rand = Rnd.get(100);
			if(rand < 25)
			{
				st.giveItems(REWARD[0], 1, false, false);
				htmltext = "hitsran_q0379_11.htm";
			}
			else if(rand < 50)
			{
				st.giveItems(REWARD[1], 1, false, false);
				htmltext = "hitsran_q0379_12.htm";
			}
			else
			{
				st.giveItems(REWARD[2], 1, false, false);
				htmltext = "hitsran_q0379_13.htm";
			}
			st.finishQuest();
		}
		else if(event.equalsIgnoreCase("hitsran_q0379_05.htm"))
			st.finishQuest();
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == HARLAN)
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 20)
					htmltext = "hitsran_q0379_01.htm";
				else
					htmltext = "hitsran_q0379_02.htm";
			}
			else if(cond == 1)
			{
				if(st.getQuestItemsCount(LEAF_OF_EUCALYPTUS) < 80 && st.getQuestItemsCount(STONE_OF_CHILL) < 100)
					htmltext = "hitsran_q0379_07.htm";
				else if(st.getQuestItemsCount(LEAF_OF_EUCALYPTUS) >= 80 && st.getQuestItemsCount(STONE_OF_CHILL) < 100)
					htmltext = "hitsran_q0379_08.htm";
				else if(st.getQuestItemsCount(LEAF_OF_EUCALYPTUS) < 80 && st.getQuestItemsCount(STONE_OF_CHILL) >= 100)
					htmltext = "hitsran_q0379_09.htm";
				else
					htmltext = "hitsran_q0379_02.htm";
			}
			else if(cond == 2)
				if(st.getQuestItemsCount(LEAF_OF_EUCALYPTUS) >= 80 && st.getQuestItemsCount(STONE_OF_CHILL) >= 100)
					htmltext = "hitsran_q0379_10.htm";
				else
				{
					st.setCond(1);
					if(st.getQuestItemsCount(LEAF_OF_EUCALYPTUS) < 80 && st.getQuestItemsCount(STONE_OF_CHILL) < 100)
						htmltext = "hitsran_q0379_07.htm";
					else if(st.getQuestItemsCount(LEAF_OF_EUCALYPTUS) >= 80 && st.getQuestItemsCount(STONE_OF_CHILL) < 100)
						htmltext = "hitsran_q0379_08.htm";
					else if(st.getQuestItemsCount(LEAF_OF_EUCALYPTUS) < 80 && st.getQuestItemsCount(STONE_OF_CHILL) >= 100)
						htmltext = "hitsran_q0379_09.htm";
				}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(st.getCond() == 1)
		{
			if(npcId == Enku_Orc_Champion && st.getQuestItemsCount(LEAF_OF_EUCALYPTUS) < 80)
				st.giveItems(LEAF_OF_EUCALYPTUS, 1, true, true);
			else if(npcId == Enku_Orc_Shaman && st.getQuestItemsCount(STONE_OF_CHILL) < 100)
				st.giveItems(STONE_OF_CHILL, 1, true, true);
			if(st.getQuestItemsCount(LEAF_OF_EUCALYPTUS) >= 80 && st.getQuestItemsCount(STONE_OF_CHILL) >= 100)
			{
				st.setCond(2);
			}
			else
				st.playSound(SOUND_ITEMGET);
		}
		return null;
	}
}
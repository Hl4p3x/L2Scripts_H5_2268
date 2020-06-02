package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _030_ChestCaughtWithABaitOfFire extends QuestScript
{
	int Linnaeus = 31577;
	int Rukal = 30629;

	int RedTreasureChest = 6511;
	int RukalsMusicalScore = 7628;
	int NecklaceOfProtection = 916;

	public _030_ChestCaughtWithABaitOfFire()
	{
		super(PARTY_NONE, ONETIME);
		addStartNpc(Linnaeus);
		addTalkId(Rukal);
		addQuestItem(RukalsMusicalScore);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("fisher_linneaus_q0030_0104.htm"))
		{
			st.setCond(1);
		}
		else if(event.equals("fisher_linneaus_q0030_0201.htm"))
		{
			if(st.getQuestItemsCount(RedTreasureChest) > 0)
			{
				st.takeItems(RedTreasureChest, 1);
				st.giveItems(RukalsMusicalScore, 1, false, false);
				st.setCond(2);
			}
			else
				htmltext = "fisher_linneaus_q0030_0202.htm";
		}
		else if(event.equals("bard_rukal_q0030_0301.htm"))
			if(st.getQuestItemsCount(RukalsMusicalScore) >= 1)
			{
				st.takeItems(RukalsMusicalScore, -1);
				st.giveItems(NecklaceOfProtection, 1, false, false);
				st.finishQuest();
			}
			else
			{
				htmltext = "bard_rukal_q0030_0302.htm";
				st.abortQuest();
			}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(npcId == Linnaeus)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 60)
					htmltext = "fisher_linneaus_q0030_0102.htm";
				else
				{
					QuestState LinnaeusSpecialBait = st.getPlayer().getQuestState(53);
					if(LinnaeusSpecialBait != null)
					{
						if(LinnaeusSpecialBait.isCompleted())
							htmltext = "fisher_linneaus_q0030_0101.htm";
						else
							htmltext = "fisher_linneaus_q0030_0102.htm";
					}
					else
						htmltext = "fisher_linneaus_q0030_0103.htm";
				}
			}
			else if(cond == 1)
			{
				htmltext = "fisher_linneaus_q0030_0105.htm";
				if(st.getQuestItemsCount(RedTreasureChest) == 0)
					htmltext = "fisher_linneaus_q0030_0106.htm";
			}
			else if(cond == 2)
				htmltext = "fisher_linneaus_q0030_0203.htm";
		}
		else if(npcId == Rukal)
			if(cond == 2)
				htmltext = "bard_rukal_q0030_0201.htm";
			else
				htmltext = "bard_rukal_q0030_0302.htm";
		return htmltext;
	}
}
package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _028_ChestCaughtWithABaitOfIcyAir extends QuestScript
{
	int OFulle = 31572;
	int Kiki = 31442;

	int BigYellowTreasureChest = 6503;
	int KikisLetter = 7626;
	int ElvenRing = 881;

	public _028_ChestCaughtWithABaitOfIcyAir()
	{
		super(PARTY_NONE, ONETIME);
		addStartNpc(OFulle);
		addTalkId(Kiki);
		addQuestItem(KikisLetter);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("fisher_ofulle_q0028_0104.htm"))
		{
			st.setCond(1);
		}
		else if(event.equals("fisher_ofulle_q0028_0201.htm"))
		{
			if(st.getQuestItemsCount(BigYellowTreasureChest) > 0)
			{
				st.setCond(2);
				st.takeItems(BigYellowTreasureChest, 1);
				st.giveItems(KikisLetter, 1, false, false);
			}
			else
				htmltext = "fisher_ofulle_q0028_0202.htm";
		}
		else if(event.equals("mineral_trader_kiki_q0028_0301.htm"))
			if(st.getQuestItemsCount(KikisLetter) >= 1)
			{
				htmltext = "mineral_trader_kiki_q0028_0301.htm";
				st.takeItems(KikisLetter, -1);
				st.giveItems(ElvenRing, 1, false, false);
				st.finishQuest();
			}
			else
			{
				htmltext = "mineral_trader_kiki_q0028_0302.htm";
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
		if(npcId == OFulle)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 36)
					htmltext = "fisher_ofulle_q0028_0101.htm";
				else
				{
					QuestState OFullesSpecialBait = st.getPlayer().getQuestState(51);
					if(OFullesSpecialBait != null)
					{
						if(OFullesSpecialBait.isCompleted())
							htmltext = "fisher_ofulle_q0028_0101.htm";
						else
							htmltext = "fisher_ofulle_q0028_0102.htm";
					}
					else
						htmltext = "fisher_ofulle_q0028_0103.htm";
				}
			}
			else if(cond == 1)
			{
				htmltext = "fisher_ofulle_q0028_0105.htm";
				if(st.getQuestItemsCount(BigYellowTreasureChest) == 0)
					htmltext = "fisher_ofulle_q0028_0106.htm";
			}
			else if(cond == 2)
				htmltext = "fisher_ofulle_q0028_0203.htm";
		}
		else if(npcId == Kiki)
			if(cond == 2)
				htmltext = "mineral_trader_kiki_q0028_0201.htm";
			else
				htmltext = "mineral_trader_kiki_q0028_0302.htm";
		return htmltext;
	}
}

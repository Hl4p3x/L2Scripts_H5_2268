package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * @author pchayka
 */

public class _288_HandleWithCare extends QuestScript
{
	private static final int Ankumi = 32741;
	private static final int MiddleGradeLizardScale = 15498;
	private static final int HighestGradeLizardScale = 15497;

	public _288_HandleWithCare()
	{
		super(PARTY_ONE, REPEATABLE);
		addStartNpc(Ankumi);
		addQuestItem(MiddleGradeLizardScale, HighestGradeLizardScale);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("ankumi_q288_03.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("request_reward"))
		{
			if(st.getCond() == 2 && st.getQuestItemsCount(MiddleGradeLizardScale) >= 1)
			{
				st.takeAllItems(MiddleGradeLizardScale);
				switch(Rnd.get(1, 6))
				{
					case 1:
						st.giveItems(959, 1, false, false);
						break;
					case 2:
						st.giveItems(960, 1, false, false);
						break;
					case 3:
						st.giveItems(960, 2, false, false);
						break;
					case 4:
						st.giveItems(960, 3, false, false);
						break;
					case 5:
						st.giveItems(9557, 1, false, false);
						break;
					case 6:
						st.giveItems(9557, 2, false, false);
						break;
				}
				htmltext = "ankumi_q288_06.htm";
				st.finishQuest();
			}
			else if(st.getCond() == 3 && st.getQuestItemsCount(HighestGradeLizardScale) >= 1)
			{
				st.takeAllItems(HighestGradeLizardScale);
				switch(Rnd.get(1, 4))
				{
					case 1:
						st.giveItems(959, 1, false, false);
						st.giveItems(9557, 1, false, false);
						break;
					case 2:
						st.giveItems(960, 1, false, false);
						st.giveItems(9557, 1, false, false);
						break;
					case 3:
						st.giveItems(960, 2, false, false);
						st.giveItems(9557, 1, false, false);
						break;
					case 4:
						st.giveItems(960, 3, false, false);
						st.giveItems(9557, 1, false, false);
						break;
				}
				htmltext = "ankumi_q288_06.htm";
				st.finishQuest();
			}
			else
			{
				htmltext = "ankumi_q288_07.htm";
				st.finishQuest();
			}
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Ankumi)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 82)
					htmltext = "ankumi_q288_01.htm";
				else
					htmltext = "ankumi_q288_00.htm";
			}
			else if(cond == 1)
				htmltext = "ankumi_q288_04.htm";
			else if(cond == 2 || cond == 3)
				htmltext = "ankumi_q288_05.htm";
		}
		return htmltext;
	}
}
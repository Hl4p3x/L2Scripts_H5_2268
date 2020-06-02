package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _027_ChestCaughtWithABaitOfWind extends QuestScript
{
	// NPC List
	private static final int Lanosco = 31570;
	private static final int Shaling = 31434;
	//Quest Items
	private static final int StrangeGolemBlueprint = 7625;
	//Items
	private static final int BigBlueTreasureChest = 6500;
	private static final int BlackPearlRing = 880;

	public _027_ChestCaughtWithABaitOfWind()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(Lanosco);
		addTalkId(Shaling);
		addQuestItem(StrangeGolemBlueprint);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("fisher_lanosco_q0027_0104.htm"))
		{
			st.setCond(1);
		}
		else if(event.equals("fisher_lanosco_q0027_0201.htm"))
		{
			if(st.getQuestItemsCount(BigBlueTreasureChest) > 0)
			{
				st.takeItems(BigBlueTreasureChest, 1);
				st.giveItems(StrangeGolemBlueprint, 1, false, false);
				st.setCond(2);
			}
			else
				htmltext = "fisher_lanosco_q0027_0202.htm";
		}
		else if(event.equals("blueprint_seller_shaling_q0027_0301.htm"))
			if(st.getQuestItemsCount(StrangeGolemBlueprint) >= 1)
			{
				st.takeItems(StrangeGolemBlueprint, -1);
				st.giveItems(BlackPearlRing, 1, false, false);
				st.finishQuest();
			}
			else
			{
				htmltext = "blueprint_seller_shaling_q0027_0302.htm";
				st.abortQuest();
			}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Lanosco)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 27)
					htmltext = "fisher_lanosco_q0027_0101.htm";
				else
				{
					QuestState LanoscosSpecialBait = st.getPlayer().getQuestState(50);
					if(LanoscosSpecialBait != null)
					{
						if(LanoscosSpecialBait.isCompleted())
							htmltext = "fisher_lanosco_q0027_0101.htm";
						else
							htmltext = "fisher_lanosco_q0027_0102.htm";
					}
					else
						htmltext = "fisher_lanosco_q0027_0103.htm";
				}
			}
			else if(cond == 1)
			{
				htmltext = "fisher_lanosco_q0027_0105.htm";
				if(st.getQuestItemsCount(BigBlueTreasureChest) == 0)
					htmltext = "fisher_lanosco_q0027_0106.htm";
			}
			else if(cond == 2)
				htmltext = "fisher_lanosco_q0027_0203.htm";
		}
		else if(npcId == Shaling)
			if(cond == 2)
				htmltext = "blueprint_seller_shaling_q0027_0201.htm";
			else
				htmltext = "blueprint_seller_shaling_q0027_0302.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		return null;
	}
}
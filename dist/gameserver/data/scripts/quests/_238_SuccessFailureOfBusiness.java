package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _238_SuccessFailureOfBusiness extends QuestScript
{

	private static final int Helvetica = 32641;

	private static final int BrazierOfPurity = 18806;
	private static final int EvilSpirit = 22658;
	private static final int GuardianSpirit = 22659;

	private static final int VicinityOfTheFieldOfSilenceResearchCenter = 14865;
	private static final int BrokenPieveOfMagicForce = 14867;
	private static final int GuardianSpiritFragment = 14868;

	public _238_SuccessFailureOfBusiness()
	{
		super(PARTY_NONE, ONETIME);
		addStartNpc(Helvetica);
		addKillId(BrazierOfPurity, EvilSpirit, GuardianSpirit);
		addQuestItem(BrokenPieveOfMagicForce, GuardianSpiritFragment);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("32641-03.htm"))
		{
			st.setCond(1);
		}
		if(event.equalsIgnoreCase("32641-06.htm"))
		{
			st.takeAllItems(BrokenPieveOfMagicForce);
			st.setCond(3);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Helvetica)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 82 || !st.getPlayer().isQuestCompleted(237))
					htmltext = "32641-00.htm";
				else if(st.getQuestItemsCount(VicinityOfTheFieldOfSilenceResearchCenter) == 0)
					htmltext = "32641-10.htm";
				else
					htmltext = "32641-01.htm";
			}
			else if(cond == 1)
				htmltext = "32641-04.htm";
			else if(cond == 2)
				htmltext = "32641-05.htm";
			else if(cond == 3)
				htmltext = "32641-07.htm";
			else if(cond == 4)
			{
				st.takeAllItems(VicinityOfTheFieldOfSilenceResearchCenter);
				st.takeAllItems(GuardianSpiritFragment);
				st.giveItems(ADENA_ID, 283346, true, true);
				st.addExpAndSp(1319736, 103553);
				st.finishQuest();
				htmltext = "32641-08.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onCompleted(NpcInstance npc, QuestState st)
	{
		String htmltext = COMPLETED_DIALOG;
		int npcId = npc.getNpcId();
		if(npcId == Helvetica)
			htmltext = "32641-09.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 1 && npc.getNpcId() == BrazierOfPurity)
		{
			st.giveItems(BrokenPieveOfMagicForce, 1, true, true);
			if(st.getQuestItemsCount(BrokenPieveOfMagicForce) >= 10)
				st.setCond(2);
		}
		else if(cond == 3 && (npc.getNpcId() == EvilSpirit || npc.getNpcId() == GuardianSpirit))
		{
			st.giveItems(GuardianSpiritFragment, 1, true, true);
			if(st.getQuestItemsCount(GuardianSpiritFragment) >= 20)
				st.setCond(4);
		}
		return null;
	}
}
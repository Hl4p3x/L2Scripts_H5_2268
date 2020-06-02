package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _308_ReedFieldMaintenance extends QuestScript
{

	private static final int Katensa = 32646;

	private static final int MucrokianHide = 14871;
	private static final int AwakenMucrokianHide = 14872;

	private static final int MucrokianFanatic = 22650;
	private static final int MucrokianAscetic = 22651;
	private static final int MucrokianSavior = 22652;
	private static final int MucrokianPreacher = 22653;
	private static final int ContaminatedMucrokian = 22654;
	private static final int ChangedMucrokian = 22655;
	private static final int[] MoiraiRecipes = {
			15777,
			15780,
			15783,
			15786,
			15789,
			15790,
			15812,
			15813,
			15814
	};
	private static final int[] Moiraimaterials = {
			15647,
			15650,
			15653,
			15656,
			15659,
			15692,
			15772,
			15773,
			15774
	};

	public _308_ReedFieldMaintenance()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(Katensa);
		addQuestItem(MucrokianHide, AwakenMucrokianHide);
		addKillId(MucrokianFanatic, MucrokianAscetic, MucrokianSavior, MucrokianPreacher, ContaminatedMucrokian, ChangedMucrokian);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("32646-04.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("32646-11.htm"))
			st.finishQuest();
		else if(event.equalsIgnoreCase("moirairec"))
		{
			if(st.getQuestItemsCount(MucrokianHide) >= 180)
			{
				st.takeItems(MucrokianHide, 180);
				st.giveItems(MoiraiRecipes[Rnd.get(MoiraiRecipes.length - 1)], 1, false, false);
				return null;
			}
			else
				htmltext = "32646-16.htm";
		}
		else if(event.equalsIgnoreCase("moiraimat"))
		{
			if(st.getQuestItemsCount(MucrokianHide) >= 100)
			{
				st.takeItems(MucrokianHide, 100);
				st.giveItems(Moiraimaterials[Rnd.get(Moiraimaterials.length - 1)], 1, false, false);
				return null;
			}
			else
				htmltext = "32646-16.htm";
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Katensa)
		{
			if(cond == 0)
			{
				QuestState qs1 = st.getPlayer().getQuestState(309);
				if(qs1 != null && qs1.isStarted())
					return "32646-15.htm"; // нельзя брать оба квеста сразу
				if(st.getPlayer().getLevel() < 82)
					return "32646-00.htm";
				return "32646-01.htm";
			}
			else if(cond == 1)
			{
				long awaken = st.takeAllItems(AwakenMucrokianHide);
				if(awaken > 0)
					st.giveItems(MucrokianHide, awaken * 2, false, false);

				if(st.getQuestItemsCount(MucrokianHide) == 0)
					return "32646-05.htm";
				else if(!st.getPlayer().isQuestCompleted(238))
					return "32646-a1.htm"; // обычные цены
				else
					return "32646-a2.htm"; // со скидкой
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		st.rollAndGive(npc.getNpcId() == ChangedMucrokian ? AwakenMucrokianHide : MucrokianHide, 1, 60);
		return null;
	}
}
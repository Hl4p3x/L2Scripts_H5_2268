package quests;

import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _247_PossessorOfaPreciousSoul4 extends QuestScript
{
	private static int CARADINE = 31740;
	private static int LADY_OF_LAKE = 31745;

	private static int CARADINE_LETTER_LAST = 7679;
	private static int NOBLESS_TIARA = 7694;

	public _247_PossessorOfaPreciousSoul4()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(CARADINE);

		addTalkId(LADY_OF_LAKE);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		int cond = st.getCond();
		if(cond == 0 && event.equals("caradine_q0247_03.htm"))
		{
			st.setCond(1);
		}
		else if(cond == 1)
		{
			if(event.equals("caradine_q0247_04.htm"))
				return htmltext;
			else if(event.equals("caradine_q0247_05.htm"))
			{
				st.setCond(2);
				st.takeItems(CARADINE_LETTER_LAST, 1);
				st.getPlayer().teleToLocation(143230, 44030, -3030);
				return htmltext;
			}
		}
		else if(cond == 2)
			if(event.equals("caradine_q0247_06.htm"))
				return htmltext;
			else if(event.equals("caradine_q0247_05.htm"))
			{
				st.getPlayer().teleToLocation(143230, 44030, -3030);
				return htmltext;
			}
			else if(event.equals("lady_of_the_lake_q0247_02.htm"))
				return htmltext;
			else if(event.equals("lady_of_the_lake_q0247_03.htm"))
				return htmltext;
			else if(event.equals("lady_of_the_lake_q0247_04.htm"))
				return htmltext;
			else if(event.equals("lady_of_the_lake_q0247_05.htm"))
				if(st.getPlayer().getLevel() >= 75)
				{
					st.giveItems(NOBLESS_TIARA, 1, false, false);
					st.addExpAndSp(93836, 0);
					st.finishQuest();
					Olympiad.addNoble(st.getPlayer());
					st.getPlayer().setNoble(true);
					st.getPlayer().updatePledgeClass();
					st.getPlayer().updateNobleSkills();
					st.getPlayer().sendSkillList();
					st.getPlayer().broadcastUserInfo(true);
				}
				else
					htmltext = "lady_of_the_lake_q0247_06.htm";
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		if(!st.getPlayer().isSubClassActive())
			return "Subclass only!";

		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == CARADINE)
		{
			if(cond == 0)
			{
				if(st.getPlayer().isQuestCompleted(246) && st.getPlayer().getLevel() >= 75)
					htmltext = "caradine_q0247_01.htm";
				else
					htmltext = "caradine_q0247_02.htm";
			}
			else if(cond == 1)
				htmltext = "caradine_q0247_03.htm";
			else if(cond == 2)
				htmltext = "caradine_q0247_06.htm";
		}
		else if(npcId == LADY_OF_LAKE && cond == 2)
			if(st.getPlayer().getLevel() >= 75)
				htmltext = "lady_of_the_lake_q0247_01.htm";
			else
				htmltext = "lady_of_the_lake_q0247_06.htm";
		return htmltext;
	}
}
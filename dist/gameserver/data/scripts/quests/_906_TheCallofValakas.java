package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * @author pchayka
 *         Daily quest
 */
public class _906_TheCallofValakas extends QuestScript
{
	private static final int Klein = 31540;
	private static final int LavasaurusAlphaFragment = 21993;
	private static final int ValakasMinion = 29029;

	public _906_TheCallofValakas()
	{
		super(PARTY_ALL, DAILY);
		addStartNpc(Klein);
		addKillId(ValakasMinion);
		addQuestItem(LavasaurusAlphaFragment);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("klein_q906_04.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("klein_q906_07.htm"))
		{
			st.takeAllItems(LavasaurusAlphaFragment);
			st.giveItems(21895, 1, false, false); // Scroll: Valakas Call
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(npc.getNpcId() == Klein)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 83)
				{
					if(st.getQuestItemsCount(7267) > 0)
						htmltext = "klein_q906_01.htm";
					else
						htmltext = "klein_q906_00b.htm";
				}
				else
					htmltext = "klein_q906_00.htm";
			}
			else if(cond == 1)
				htmltext = "klein_q906_05.htm";
			else if(cond == 2)
				htmltext = "klein_q906_06.htm";
		}

		return htmltext;
	}

	@Override
	public String onCompleted(NpcInstance npc, QuestState st)
	{
		String htmltext = COMPLETED_DIALOG;
		int npcId = npc.getNpcId();
		if(npcId == Klein)
			htmltext = "klein_q906_00a.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 1)
		{
			if(npc.getNpcId() == ValakasMinion && Rnd.chance(40))
			{
				st.giveItems(LavasaurusAlphaFragment, 1, false, false);
				st.setCond(2);
			}
		}
		return null;
	}
}
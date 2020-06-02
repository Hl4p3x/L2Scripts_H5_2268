package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _121_PavelTheGiants extends QuestScript
{
	//NPCs
	private static int NEWYEAR = 31961;
	private static int YUMI = 32041;

	public _121_PavelTheGiants()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(NEWYEAR);
		addTalkId(YUMI);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equals("collecter_yumi_q0121_0201.htm"))
		{
			st.addExpAndSp(346320, 26069);
			st.finishQuest();
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == NEWYEAR)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 70)
				{
					htmltext = "head_blacksmith_newyear_q0121_0101.htm";
					st.setCond(1);
				}
				else
					htmltext = "head_blacksmith_newyear_q0121_0103.htm";
			}
			else if(cond == 1)
				htmltext = "head_blacksmith_newyear_q0121_0105.htm";
		}
		else if(npcId == YUMI)
		{
			if(cond == 1)
				htmltext = "collecter_yumi_q0121_0101.htm";
		}
		return htmltext;
	}
}
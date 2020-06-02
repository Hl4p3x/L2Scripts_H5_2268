package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _015_SweetWhispers extends QuestScript
{

	public _015_SweetWhispers()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(31302);

		addTalkId(31517);
		addTalkId(31518);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("trader_vladimir_q0015_0104.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("dark_necromancer_q0015_0201.htm"))
			st.setCond(2);
		else if(event.equalsIgnoreCase("dark_presbyter_q0015_0301.htm"))
		{
			st.addExpAndSp(350531, 28204);
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 31302)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 60)
					htmltext = "trader_vladimir_q0015_0101.htm";
				else
					htmltext = "trader_vladimir_q0015_0103.htm";
			}
			else if(cond >= 1)
				htmltext = "trader_vladimir_q0015_0105.htm";
		}
		else if(npcId == 31518)
		{
			if(cond == 1)
				htmltext = "dark_necromancer_q0015_0101.htm";
			else if(cond == 2)
				htmltext = "dark_necromancer_q0015_0202.htm";
		}
		else if(npcId == 31517)
			if(cond == 2)
				htmltext = "dark_presbyter_q0015_0201.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		return null;
	}
}
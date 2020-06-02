package quests;

import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _009_IntoTheCityOfHumans extends QuestScript
{
	//NPC
	public final int PETUKAI = 30583;
	public final int TANAPI = 30571;
	public final int TAMIL = 30576;
	//Items
	public final int SCROLL_OF_ESCAPE_GIRAN = 7126;
	//Quest Item
	public final int MARK_OF_TRAVELER = 7570;

	public _009_IntoTheCityOfHumans()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(PETUKAI);

		addTalkId(PETUKAI);
		addTalkId(TANAPI);
		addTalkId(TAMIL);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("centurion_petukai_q0009_0104.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("seer_tanapi_q0009_0201.htm"))
		{
			st.setCond(2);
		}
		else if(event.equalsIgnoreCase("gatekeeper_tamil_q0009_0301.htm"))
		{
			st.giveItems(SCROLL_OF_ESCAPE_GIRAN, 1, false, false);
			st.giveItems(MARK_OF_TRAVELER, 1, false, false);
			st.unset("cond");
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
		if(npcId == PETUKAI)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() == Race.ORC && st.getPlayer().getLevel() >= 3)
					htmltext = "centurion_petukai_q0009_0101.htm";
				else
					htmltext = "centurion_petukai_q0009_0102.htm";
			}
			else if(cond == 1)
				htmltext = "centurion_petukai_q0009_0105.htm";
		}
		else if(npcId == TANAPI)
		{
			if(cond == 1)
				htmltext = "seer_tanapi_q0009_0101.htm";
			else if(cond == 2)
				htmltext = "seer_tanapi_q0009_0202.htm";
		}
		else if(npcId == TAMIL)
			if(cond == 2)
				htmltext = "gatekeeper_tamil_q0009_0201.htm";
		return htmltext;
	}
}
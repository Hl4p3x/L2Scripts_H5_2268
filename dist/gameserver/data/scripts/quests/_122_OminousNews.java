package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _122_OminousNews extends QuestScript
{
	int MOIRA = 31979;
	int KARUDA = 32017;

	public _122_OminousNews()
	{
		super(PARTY_NONE, ONETIME);
		addStartNpc(MOIRA);
		addTalkId(KARUDA);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		int cond = st.getCond();
		htmltext = event;
		if(htmltext.equalsIgnoreCase("seer_moirase_q0122_0104.htm") && cond == 0)
		{
			st.setCond(1);
		}
		else if(htmltext.equalsIgnoreCase("karuda_q0122_0201.htm"))
			if(cond == 1)
			{
				st.giveItems(ADENA_ID, 8923, true, true);
				st.addExpAndSp(45151, 2310); // награда соответствует Т2
				st.finishQuest();
			}
			else
				htmltext = NO_QUEST_DIALOG;
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(npcId == MOIRA)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 20)
					htmltext = "seer_moirase_q0122_0101.htm";
				else
					htmltext = "seer_moirase_q0122_0103.htm";
			}
			else
				htmltext = "seer_moirase_q0122_0104.htm";
		}
		else if(npcId == KARUDA && cond == 1)
			htmltext = "karuda_q0122_0101.htm";
		return htmltext;
	}
}
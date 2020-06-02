package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _112_WalkOfFate extends QuestScript
{
	//NPC
	private static final int Livina = 30572;
	private static final int Karuda = 32017;
	//Items
	private static final int EnchantD = 956;

	public _112_WalkOfFate()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(Livina);
		addTalkId(Karuda);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("karuda_q0112_0201.htm"))
		{
			st.addExpAndSp(112876, 5774);
			st.giveItems(ADENA_ID, 22308, true, true);
			st.giveItems(EnchantD, 1, false, false);
			st.finishQuest();
		}
		else if(event.equalsIgnoreCase("seer_livina_q0112_0104.htm"))
		{
			st.setCond(1);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(npcId == Livina)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 20)
					htmltext = "seer_livina_q0112_0101.htm";
				else
					htmltext = "seer_livina_q0112_0103.htm";
			}
			else if(cond == 1)
				htmltext = "seer_livina_q0112_0105.htm";
		}
		else if(npcId == Karuda)
			if(cond == 1)
				htmltext = "karuda_q0112_0101.htm";
		return htmltext;
	}
}

package quests;

import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _170_DangerousSeduction extends QuestScript
{
	//NPC
	private static final int Vellior = 30305;
	//Quest Items
	private static final int NightmareCrystal = 1046;
	//MOB
	private static final int Merkenis = 27022;

	public _170_DangerousSeduction()
	{
		super(PARTY_NONE, ONETIME);
		addStartNpc(Vellior);
		addTalkId(Vellior);
		addKillId(Merkenis);
		addQuestItem(NightmareCrystal);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("30305-04.htm"))
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
		if(npcId == Vellior)
			if(cond == 0)
			{
				if(st.getPlayer().getRace() != Race.DARKELF)
					htmltext = "30305-00.htm";
				else if(st.getPlayer().getLevel() < 21)
					htmltext = "30305-02.htm";
				else
					htmltext = "30305-03.htm";
			}
			else if(cond == 1)
				htmltext = "30305-05.htm";
			else if(cond == 2)
			{
				st.takeItems(NightmareCrystal, -1);
				st.giveItems(ADENA_ID, 102680, true, true);
				st.addExpAndSp(38607, 4018);
				htmltext = "30305-06.htm";
				st.finishQuest();
			}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 1 && npcId == Merkenis)
		{
			if(st.getQuestItemsCount(NightmareCrystal) == 0)
				st.giveItems(NightmareCrystal, 1, false, false);
			st.setCond(2);
		}
		return null;
	}
}
package quests;

import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _294_CovertBusiness extends QuestScript
{
	public static int BatFang = 1491;
	public static int RingOfRaccoon = 1508;

	public static int BarbedBat = 20370;
	public static int BladeBat = 20480;

	public static int Keef = 30534;

	public _294_CovertBusiness()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(Keef);

		addKillId(BarbedBat);
		addKillId(BladeBat);

		addQuestItem(BatFang);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("elder_keef_q0294_03.htm"))
		{
			st.setCond(1);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Keef)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() != Race.DWARF)
					htmltext = "elder_keef_q0294_00.htm";
				else if(st.getPlayer().getLevel() >= 10)
					htmltext = "elder_keef_q0294_02.htm";
				else
					htmltext = "elder_keef_q0294_01.htm";
			}
			else
			{
				if(st.getQuestItemsCount(BatFang) < 100)
					htmltext = "elder_keef_q0294_04.htm";
				else
				{
					if(st.getQuestItemsCount(RingOfRaccoon) < 1)
					{
						st.giveItems(RingOfRaccoon, 1, false, false);
						htmltext = "elder_keef_q0294_05.htm";
					}
					else
					{
						st.giveItems(ADENA_ID, 2400, true, true);
						htmltext = "elder_keef_q0294_06.htm";
					}
					st.addExpAndSp(0, 600);
					st.finishQuest();
				}
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1)
			st.rollAndGive(BatFang, 1, 2, 100, 100);
		return null;
	}
}
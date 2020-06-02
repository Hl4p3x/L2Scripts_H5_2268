package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _258_BringWolfPelts extends QuestScript
{
	int WOLF_PELT = 702;

	int Cotton_Shirt = 390;
	int Leather_Pants = 29;
	int Leather_Shirt = 22;
	int Short_Leather_Gloves = 1119;
	int Tunic = 426;

	public _258_BringWolfPelts()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(30001);
		addKillId(20120);
		addKillId(20442);

		addQuestItem(WOLF_PELT);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.intern().equalsIgnoreCase("lector_q0258_03.htm"))
		{
			st.setCond(1);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 3)
			{
				htmltext = "lector_q0258_02.htm";
				return htmltext;
			}
			htmltext = "lector_q0258_01.htm";
		}
		else if(cond == 1 && st.getQuestItemsCount(WOLF_PELT) >= 0 && st.getQuestItemsCount(WOLF_PELT) < 40)
			htmltext = "lector_q0258_05.htm";
		else if(cond == 2 && st.getQuestItemsCount(WOLF_PELT) >= 40)
		{
			st.takeItems(WOLF_PELT, 40);
			int n = Rnd.get(16);
			if(n == 0)
			{
				st.giveItems(Cotton_Shirt, 1, false, false);
				st.playSound(SOUND_JACKPOT);
			}
			else if(n < 6)
				st.giveItems(Leather_Pants, 1, false, false);
			else if(n < 9)
				st.giveItems(Leather_Shirt, 1, false, false);
			else if(n < 13)
				st.giveItems(Short_Leather_Gloves, 1, false, false);
			else
				st.giveItems(Tunic, 1, false, false);
			htmltext = "lector_q0258_06.htm";
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		long count = st.getQuestItemsCount(WOLF_PELT);
		if(count < 40 && st.getCond() == 1)
		{
			st.giveItems(WOLF_PELT, 1, true, true);
			if(count >= 39)
			{
				st.setCond(2);
			}
			else
				st.playSound(SOUND_ITEMGET);
		}
		return null;
	}
}
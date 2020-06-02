package quests;

import org.apache.commons.lang3.ArrayUtils;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * @author pchayka
 */

public class _270_TheOneWhoEndsSilence extends QuestScript
{
	private static final int Greymore = 32757;
	private static final int TatteredMonkClothes = 15526;
	private static final int[] LowMobs = {
			22791,
			22790,
			22793
	};
	private static final int[] HighMobs = {
			22794,
			22795,
			22797,
			22798,
			22799,
			22800
	};

	public _270_TheOneWhoEndsSilence()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(Greymore);
		addKillId(LowMobs);
		addKillId(HighMobs);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("greymore_q270_03.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("showrags"))
		{
			if(st.getQuestItemsCount(TatteredMonkClothes) < 1)
				htmltext = "greymore_q270_05.htm";
			else if(st.getQuestItemsCount(TatteredMonkClothes) < 100)
				htmltext = "greymore_q270_06.htm";
			else if(st.getQuestItemsCount(TatteredMonkClothes) >= 100)
				htmltext = "greymore_q270_07.htm";
		}
		else if(event.equalsIgnoreCase("rags100"))
		{
			if(st.getQuestItemsCount(TatteredMonkClothes) >= 100)
			{
				st.takeItems(TatteredMonkClothes, 100);
				switch(Rnd.get(1, 21))
				{
					//Recipes
					case 1:
						st.giveItems(10373, 1, false, false);
						break;
					case 2:
						st.giveItems(10374, 1, false, false);
						break;
					case 3:
						st.giveItems(10375, 1, false, false);
						break;
					case 4:
						st.giveItems(10376, 1, false, false);
						break;
					case 5:
						st.giveItems(10377, 1, false, false);
						break;
					case 6:
						st.giveItems(10378, 1, false, false);
						break;
					case 7:
						st.giveItems(10379, 1, false, false);
						break;
					case 8:
						st.giveItems(10380, 1, false, false);
						break;
					case 9:
						st.giveItems(10381, 1, false, false);
						break;
					//Material
					case 10:
						st.giveItems(10397, 1, false, false);
						break;
					case 11:
						st.giveItems(10398, 1, false, false);
						break;
					case 12:
						st.giveItems(10399, 1, false, false);
						break;
					case 13:
						st.giveItems(10400, 1, false, false);
						break;
					case 14:
						st.giveItems(10401, 1, false, false);
						break;
					case 15:
						st.giveItems(10402, 1, false, false);
						break;
					case 16:
						st.giveItems(10403, 1, false, false);
						break;
					case 17:
						st.giveItems(10405, 1, false, false);
						break;
					// SP Scrolls
					case 18:
						st.giveItems(5593, 1, false, false);
						break;
					case 19:
						st.giveItems(5594, 1, false, false);
						break;
					case 20:
						st.giveItems(5595, 1, false, false);
						break;
					case 21:
						st.giveItems(9898, 1, false, false);
						break;
				}
				htmltext = "greymore_q270_09.htm";
			}
			else
				htmltext = "greymore_q270_08.htm";
		}
		else if(event.equalsIgnoreCase("rags200"))
		{
			if(st.getQuestItemsCount(TatteredMonkClothes) >= 200)
			{
				st.takeItems(TatteredMonkClothes, 200);
				switch(Rnd.get(1, 17))
				{
					//Recipes
					case 1:
						st.giveItems(10373, 1, false, false);
						break;
					case 2:
						st.giveItems(10374, 1, false, false);
						break;
					case 3:
						st.giveItems(10375, 1, false, false);
						break;
					case 4:
						st.giveItems(10376, 1, false, false);
						break;
					case 5:
						st.giveItems(10377, 1, false, false);
						break;
					case 6:
						st.giveItems(10378, 1, false, false);
						break;
					case 7:
						st.giveItems(10379, 1, false, false);
						break;
					case 8:
						st.giveItems(10380, 1, false, false);
						break;
					case 9:
						st.giveItems(10381, 1, false, false);
						break;
					//Material
					case 10:
						st.giveItems(10397, 1, false, false);
						break;
					case 11:
						st.giveItems(10398, 1, false, false);
						break;
					case 12:
						st.giveItems(10399, 1, false, false);
						break;
					case 13:
						st.giveItems(10400, 1, false, false);
						break;
					case 14:
						st.giveItems(10401, 1, false, false);
						break;
					case 15:
						st.giveItems(10402, 1, false, false);
						break;
					case 16:
						st.giveItems(10403, 1, false, false);
						break;
					case 17:
						st.giveItems(10405, 1, false, false);
						break;
				}
				switch(Rnd.get(1, 4))
				{
					// SP Scrolls
					case 1:
						st.giveItems(5593, 1, false, false);
						break;
					case 2:
						st.giveItems(5594, 1, false, false);
						break;
					case 3:
						st.giveItems(5595, 1, false, false);
						break;
					case 4:
						st.giveItems(9898, 1, false, false);
						break;
				}
				htmltext = "greymore_q270_09.htm";
			}
			else
				htmltext = "greymore_q270_08.htm";
		}
		else if(event.equalsIgnoreCase("rags300"))
		{
			if(st.getQuestItemsCount(TatteredMonkClothes) >= 300)
			{
				st.takeItems(TatteredMonkClothes, 300);
				switch(Rnd.get(1, 9))
				{
					//Recipes
					case 1:
						st.giveItems(10373, 1, false, false);
						break;
					case 2:
						st.giveItems(10374, 1, false, false);
						break;
					case 3:
						st.giveItems(10375, 1, false, false);
						break;
					case 4:
						st.giveItems(10376, 1, false, false);
						break;
					case 5:
						st.giveItems(10377, 1, false, false);
						break;
					case 6:
						st.giveItems(10378, 1, false, false);
						break;
					case 7:
						st.giveItems(10379, 1, false, false);
						break;
					case 8:
						st.giveItems(10380, 1, false, false);
						break;
					case 9:
						st.giveItems(10381, 1, false, false);
						break;
				}
				switch(Rnd.get(10, 17))
				{
					//Material
					case 10:
						st.giveItems(10397, 1, false, false);
						break;
					case 11:
						st.giveItems(10398, 1, false, false);
						break;
					case 12:
						st.giveItems(10399, 1, false, false);
						break;
					case 13:
						st.giveItems(10400, 1, false, false);
						break;
					case 14:
						st.giveItems(10401, 1, false, false);
						break;
					case 15:
						st.giveItems(10402, 1, false, false);
						break;
					case 16:
						st.giveItems(10403, 1, false, false);
						break;
					case 17:
						st.giveItems(10405, 1, false, false);
						break;
				}
				switch(Rnd.get(1, 4))
				{
					// SP Scrolls
					case 1:
						st.giveItems(5593, 1, false, false);
						break;
					case 2:
						st.giveItems(5594, 1, false, false);
						break;
					case 3:
						st.giveItems(5595, 1, false, false);
						break;
					case 4:
						st.giveItems(9898, 1, false, false);
						break;
				}
				htmltext = "greymore_q270_09.htm";
			}
			else
				htmltext = "greymore_q270_08.htm";
		}
		else if(event.equalsIgnoreCase("rags400"))
		{
			if(st.getQuestItemsCount(TatteredMonkClothes) >= 400)
			{
				st.takeItems(TatteredMonkClothes, 400);
				switch(Rnd.get(1, 9))
				{
					//Recipes
					case 1:
						st.giveItems(10373, 1, false, false);
						break;
					case 2:
						st.giveItems(10374, 1, false, false);
						break;
					case 3:
						st.giveItems(10375, 1, false, false);
						break;
					case 4:
						st.giveItems(10376, 1, false, false);
						break;
					case 5:
						st.giveItems(10377, 1, false, false);
						break;
					case 6:
						st.giveItems(10378, 1, false, false);
						break;
					case 7:
						st.giveItems(10379, 1, false, false);
						break;
					case 8:
						st.giveItems(10380, 1, false, false);
						break;
					case 9:
						st.giveItems(10381, 1, false, false);
						break;
				}
				switch(Rnd.get(10, 17))
				{
					//Material
					case 10:
						st.giveItems(10397, 1, false, false);
						break;
					case 11:
						st.giveItems(10398, 1, false, false);
						break;
					case 12:
						st.giveItems(10399, 1, false, false);
						break;
					case 13:
						st.giveItems(10400, 1, false, false);
						break;
					case 14:
						st.giveItems(10401, 1, false, false);
						break;
					case 15:
						st.giveItems(10402, 1, false, false);
						break;
					case 16:
						st.giveItems(10403, 1, false, false);
						break;
					case 17:
						st.giveItems(10405, 1, false, false);
						break;
				}
				switch(Rnd.get(1, 4))
				{
					// SP Scrolls
					case 1:
						st.giveItems(5593, 2, false, false);
						break;
					case 2:
						st.giveItems(5594, 2, false, false);
						break;
					case 3:
						st.giveItems(5595, 2, false, false);
						break;
					case 4:
						st.giveItems(9898, 2, false, false);
						break;
				}
				htmltext = "greymore_q270_09.htm";
			}
			else
				htmltext = "greymore_q270_08.htm";
		}
		else if(event.equalsIgnoreCase("rags500"))
		{
			if(st.getQuestItemsCount(TatteredMonkClothes) >= 500)
			{
				st.takeItems(TatteredMonkClothes, 500);
				switch(Rnd.get(1, 9))
				{
					//Recipes
					case 1:
						st.giveItems(10373, 2, false, false);
						break;
					case 2:
						st.giveItems(10374, 2, false, false);
						break;
					case 3:
						st.giveItems(10375, 2, false, false);
						break;
					case 4:
						st.giveItems(10376, 2, false, false);
						break;
					case 5:
						st.giveItems(10377, 2, false, false);
						break;
					case 6:
						st.giveItems(10378, 2, false, false);
						break;
					case 7:
						st.giveItems(10379, 2, false, false);
						break;
					case 8:
						st.giveItems(10380, 2, false, false);
						break;
					case 9:
						st.giveItems(10381, 2, false, false);
						break;
				}
				switch(Rnd.get(10, 17))
				{
					//Material
					case 10:
						st.giveItems(10397, 2, false, false);
						break;
					case 11:
						st.giveItems(10398, 2, false, false);
						break;
					case 12:
						st.giveItems(10399, 2, false, false);
						break;
					case 13:
						st.giveItems(10400, 2, false, false);
						break;
					case 14:
						st.giveItems(10401, 2, false, false);
						break;
					case 15:
						st.giveItems(10402, 2, false, false);
						break;
					case 16:
						st.giveItems(10403, 2, false, false);
						break;
					case 17:
						st.giveItems(10405, 2, false, false);
						break;
				}
				switch(Rnd.get(1, 4))
				{
					// SP Scrolls
					case 1:
						st.giveItems(5593, 1, false, false);
						break;
					case 2:
						st.giveItems(5594, 1, false, false);
						break;
					case 3:
						st.giveItems(5595, 1, false, false);
						break;
					case 4:
						st.giveItems(9898, 1, false, false);
						break;
				}
				htmltext = "greymore_q270_09.htm";
			}
			else
				htmltext = "greymore_q270_08.htm";
		}
		else if(event.equalsIgnoreCase("quit"))
		{
			htmltext = "greymore_q270_10.htm";
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(npc.getNpcId() == Greymore)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 82 && st.getPlayer().isQuestCompleted(10288))
					htmltext = "greymore_q270_01.htm";
				else
					htmltext = "greymore_q270_00.htm";
			}
			else if(cond == 1)
				htmltext = "greymore_q270_04.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 1)
		{
			if(ArrayUtils.contains(LowMobs, npc.getNpcId()) && Rnd.chance(40))
				st.giveItems(TatteredMonkClothes, 1, true, true);
			else if(ArrayUtils.contains(HighMobs, npc.getNpcId()))
				st.giveItems(TatteredMonkClothes, 1, true, true);
		}
		return null;
	}
}
package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _688_DefeatTheElrokianRaiders extends QuestScript
{
	//Settings: drop chance in %
	private static int DROP_CHANCE = 50;

	private static int DINOSAUR_FANG_NECKLACE = 8785;

	public _688_DefeatTheElrokianRaiders()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(32105);
		addTalkId(32105);
		addKillId(22214);
		addQuestItem(DINOSAUR_FANG_NECKLACE);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		long count = st.getQuestItemsCount(DINOSAUR_FANG_NECKLACE);
		if(event.equalsIgnoreCase("32105-03.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("32105-08.htm"))
		{
			if(count > 0)
			{
				st.takeItems(DINOSAUR_FANG_NECKLACE, -1);
				st.giveItems(ADENA_ID, count * 3000, true, true);
			}
			st.finishQuest();
		}
		else if(event.equalsIgnoreCase("32105-06.htm"))
		{
			st.takeItems(DINOSAUR_FANG_NECKLACE, -1);
			st.giveItems(ADENA_ID, count * 3000, true, true);
		}
		else if(event.equalsIgnoreCase("32105-07.htm"))
		{
			if(count >= 100)
			{
				st.takeItems(DINOSAUR_FANG_NECKLACE, 100);
				st.giveItems(ADENA_ID, 450000, true, true);
			}
			else
				htmltext = "32105-04.htm";
		}
		else if(event.equalsIgnoreCase("None"))
			htmltext = null;
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		long count = st.getQuestItemsCount(DINOSAUR_FANG_NECKLACE);
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 75)
				htmltext = "32105-01.htm";
			else
				htmltext = "32105-00.htm";
		}
		else if(cond == 1)
			if(count == 0)
				htmltext = "32105-04.htm";
			else
				htmltext = "32105-05.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		long count = st.getQuestItemsCount(DINOSAUR_FANG_NECKLACE);
		if(st.getCond() == 1 && count < 100 && Rnd.chance(DROP_CHANCE))
		{
			long numItems = 1;
			if(count + numItems > 100)
				numItems = 100 - count;
			if(count + numItems >= 100)
				st.playSound(SOUND_MIDDLE);
			else
				st.playSound(SOUND_ITEMGET);
			st.giveItems(DINOSAUR_FANG_NECKLACE, numItems, true, true);
		}
		return null;
	}
}
package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _023_LidiasHeart extends QuestScript
{
	// ~~~~~~ npcId list: ~~~~~~
	int Innocentin = 31328;
	int BrokenBookshelf = 31526;
	int GhostofvonHellmann = 31524;
	int Tombstone = 31523;
	int Violet = 31386;
	int Box = 31530;

	// ~~~~~ itemId List ~~~~~
	int MapForestofDeadman = 7063;
	int SilverKey = 7149;
	int LidiaHairPin = 7148;
	int LidiaDiary = 7064;
	int SilverSpear = 7150;

	public _023_LidiasHeart()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(Innocentin);

		addTalkId(Innocentin);
		addTalkId(BrokenBookshelf);
		addTalkId(GhostofvonHellmann);
		addTalkId(Tombstone);
		addTalkId(Violet);
		addTalkId(Box);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("31328-02.htm"))
		{
			st.giveItems(MapForestofDeadman, 1, false, false);
			st.giveItems(SilverKey, 1, false, false);
			st.setCond(1);
		}
		else if(event.equals("31328-03.htm"))
			st.setCond(2);
		else if(event.equals("31526-01.htm"))
			st.setCond(3);
		else if(event.equals("31526-05.htm"))
		{
			st.giveItems(LidiaHairPin, 1, false, false);
			if(st.getQuestItemsCount(LidiaDiary) != 0)
				st.setCond(4);
		}
		else if(event.equals("31526-11.htm"))
		{
			st.giveItems(LidiaDiary, 1, false, false);
			if(st.getQuestItemsCount(LidiaHairPin) != 0)
				st.setCond(4);
		}
		else if(event.equals("31328-19.htm"))
			st.setCond(6);
		else if(event.equals("31524-04.htm"))
		{
			st.setCond(7);
			st.takeItems(LidiaDiary, -1);
		}
		else if(event.equals("31523-02.htm"))
			st.addSpawn(GhostofvonHellmann, 120000);
		else if(event.equals("31523-05.htm"))
			st.startQuestTimer("viwer_timer", 10000);
		else if(event.equals("viwer_timer"))
		{
			st.setCond(8);
			htmltext = "31523-06.htm";
		}
		else if(event.equals("31530-02.htm"))
		{
			st.setCond(10);
			st.takeItems(SilverKey, -1);
			st.giveItems(SilverSpear, 1, false, false);
		}
		else if(event.equals("i7064-02.htm"))
			htmltext = "i7064-02.htm";
		else if(event.equals("31526-13.htm"))
			st.startQuestTimer("read_book", 120000);
		else if(event.equals("read_book"))
			htmltext = "i7064.htm";
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Innocentin)
		{
			if(cond == 0)
			{
				if(st.getPlayer().isQuestCompleted(22))
					htmltext = "31328-01.htm";
				else
					htmltext = "31328-00.htm";
			}
			else if(cond == 1)
				htmltext = "31328-03.htm";
			else if(cond == 2)
				htmltext = "31328-07.htm";
			else if(cond == 4)
				htmltext = "31328-08.htm";
			else if(cond == 6)
				htmltext = "31328-19.htm";
		}
		else if(npcId == BrokenBookshelf)
		{
			if(cond == 2)
			{
				if(st.getQuestItemsCount(SilverKey) != 0)
					htmltext = "31526-00.htm";
			}
			else if(cond == 3)
			{
				if(st.getQuestItemsCount(LidiaHairPin) == 0 && st.getQuestItemsCount(LidiaDiary) != 0)
					htmltext = "31526-12.htm";
				else if(st.getQuestItemsCount(LidiaHairPin) != 0 && st.getQuestItemsCount(LidiaDiary) == 0)
					htmltext = "31526-06.htm";
				else if(st.getQuestItemsCount(LidiaHairPin) == 0 && st.getQuestItemsCount(LidiaDiary) == 0)
					htmltext = "31526-02.htm";
			}
			else if(cond == 4)
				htmltext = "31526-13.htm";
		}
		else if(npcId == GhostofvonHellmann)
		{
			if(cond == 6)
				htmltext = "31524-01.htm";
			else if(cond == 7)
				htmltext = "31524-05.htm";
		}
		else if(npcId == Tombstone)
		{
			if(cond == 6)
				if(st.isRunningQuestTimer("spawn_timer"))
					htmltext = "31523-03.htm";
				else
					htmltext = "31523-01.htm";
			if(cond == 7)
				htmltext = "31523-04.htm";
			else if(cond == 8)
				htmltext = "31523-06.htm";
		}
		else if(npcId == Violet)
		{
			if(cond == 8)
			{
				htmltext = "31386-01.htm";
				st.setCond(9);
			}
			else if(cond == 9)
				htmltext = "31386-02.htm";
			else if(cond == 10)
				if(st.getQuestItemsCount(SilverSpear) != 0)
				{
					htmltext = "31386-03.htm";
					st.takeItems(SilverSpear, -1);
					st.giveItems(ADENA_ID, 350000, true, true);
					st.addExpAndSp(456893, 42112);
					st.finishQuest();
				}
				else
					htmltext = "You have no Silver Spear...";
		}
		else if(npcId == Box)
			if(cond == 9)
				if(st.getQuestItemsCount(SilverKey) != 0)
					htmltext = "31530-01.htm";
				else
					htmltext = "You have no key...";
			else if(cond == 10)
				htmltext = "31386-03.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		return null;
	}
}
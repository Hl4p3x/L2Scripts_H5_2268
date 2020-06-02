package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _603_DaimontheWhiteEyedPart1 extends QuestScript
{
	//NPC
	private static final int EYE = 31683;
	private static final int TABLE1 = 31548;
	private static final int TABLE2 = 31549;
	private static final int TABLE3 = 31550;
	private static final int TABLE4 = 31551;
	private static final int TABLE5 = 31552;
	//MOBS
	private static final int BUFFALO = 21299;
	private static final int BANDERSNATCH = 21297;
	private static final int GRENDEL = 21304;
	//ITEMS
	private static final int EVIL_SPIRIT = 7190;
	private static final int BROKEN_CRYSTAL = 7191;
	private static final int U_SUMMON = 7192;

	public _603_DaimontheWhiteEyedPart1()
	{
		super(PARTY_ONE, REPEATABLE);

		addStartNpc(EYE);

		addTalkId(TABLE1);
		addTalkId(TABLE2);
		addTalkId(TABLE3);
		addTalkId(TABLE4);
		addTalkId(TABLE5);

		addKillId(BUFFALO);
		addKillId(BANDERSNATCH);
		addKillId(GRENDEL);

		addQuestItem(EVIL_SPIRIT);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("31683-02.htm"))
		{
			if(st.getPlayer().getLevel() < 73)
				htmltext = "31683-01a.htm";
			else
				st.setCond(1);
		}
		else if(event.equalsIgnoreCase("31548-02.htm"))
		{
			st.setCond(2);
			st.giveItems(BROKEN_CRYSTAL, 1, false, false);
		}
		else if(event.equalsIgnoreCase("31549-02.htm"))
		{
			st.setCond(3);
			st.giveItems(BROKEN_CRYSTAL, 1, false, false);
		}
		else if(event.equalsIgnoreCase("31550-02.htm"))
		{
			st.setCond(4);
			st.giveItems(BROKEN_CRYSTAL, 1, false, false);
		}
		else if(event.equalsIgnoreCase("31551-02.htm"))
		{
			st.setCond(5);
			st.giveItems(BROKEN_CRYSTAL, 1, false, false);
		}
		else if(event.equalsIgnoreCase("31552-02.htm"))
		{
			st.setCond(6);
			st.giveItems(BROKEN_CRYSTAL, 1, false, false);
		}
		else if(event.equalsIgnoreCase("31683-04.htm"))
		{
			if(st.getQuestItemsCount(BROKEN_CRYSTAL) < 5)
				htmltext = "31683-08.htm";
			else
			{
				st.setCond(7);
				st.takeItems(BROKEN_CRYSTAL, -1);
			}
		}
		else if(event.equalsIgnoreCase("31683-07.htm"))
			if(st.getQuestItemsCount(EVIL_SPIRIT) < 200)
				htmltext = "31683-09.htm";
			else
			{
				st.takeItems(EVIL_SPIRIT, -1);
				st.giveItems(U_SUMMON, 1, false, false);
				st.finishQuest();
			}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 0)
		{
			if(npcId == EYE)
				htmltext = "31683-01.htm";
		}
		else if(cond == 1)
		{
			if(npcId == EYE)
				htmltext = "31683-02a.htm";
			else if(npcId == TABLE1)
				htmltext = "31548-01.htm";
		}
		else if(cond == 2)
		{
			if(npcId == EYE)
				htmltext = "31683-02a.htm";
			else if(npcId == TABLE2)
				htmltext = "31549-01.htm";
			else
				htmltext = "table-no.htm";
		}
		else if(cond == 3)
		{
			if(npcId == EYE)
				htmltext = "31683-02a.htm";
			else if(npcId == TABLE3)
				htmltext = "31550-01.htm";
			else
				htmltext = "table-no.htm";
		}
		else if(cond == 4)
		{
			if(npcId == EYE)
				htmltext = "31683-02a.htm";
			else if(npcId == TABLE4)
				htmltext = "31551-01.htm";
			else
				htmltext = "table-no.htm";
		}
		else if(cond == 5)
		{
			if(npcId == EYE)
				htmltext = "31683-02a.htm";
			else if(npcId == TABLE5)
				htmltext = "31552-01.htm";
			else
				htmltext = "table-no.htm";
		}
		else if(cond == 6)
		{
			if(npcId == EYE)
				htmltext = "31683-03.htm";
			else
				htmltext = "table-no.htm";
		}
		else if(cond == 7)
		{
			if(npcId == EYE)
				htmltext = "31683-05.htm";
		}
		else if(cond == 8)
			if(npcId == EYE)
				htmltext = "31683-06.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		st.rollAndGive(EVIL_SPIRIT, 1, 1, 200, 100);
		if(st.getQuestItemsCount(EVIL_SPIRIT) >= 200)
		{
			st.setCond(8);
		}
		return null;
	}
}
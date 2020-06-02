package quests;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.s2c.ExStartScenePlayer;

public class _192_SevenSignSeriesOfDoubt extends QuestScript
{
	// NPC
	private static int CROOP = 30676;
	private static int HECTOR = 30197;
	private static int STAN = 30200;
	private static int CORPSE = 32568;
	private static int HOLLINT = 30191;

	// ITEMS
	private static int CROOP_INTRO = 13813;
	private static int JACOB_NECK = 13814;
	private static int CROOP_LETTER = 13815;

	public _192_SevenSignSeriesOfDoubt()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(CROOP);
		addTalkId(HECTOR, STAN, CORPSE, HOLLINT);
		addQuestItem(CROOP_INTRO, JACOB_NECK, CROOP_LETTER);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		Player player = st.getPlayer();
		String htmltext = event;
		if(event.equalsIgnoreCase("30676-03.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("8"))
		{
			st.setCond(2);
			player.showQuestMovie(ExStartScenePlayer.SCENE_SSQ_SUSPICIOUS_DEATH);
			return "";
		}
		else if(event.equalsIgnoreCase("30197-03.htm"))
		{
			st.setCond(4);
			st.takeItems(CROOP_INTRO, 1);
		}
		else if(event.equalsIgnoreCase("30200-04.htm"))
		{
			st.setCond(5);
		}
		else if(event.equalsIgnoreCase("32568-02.htm"))
		{
			st.setCond(6);
			st.giveItems(JACOB_NECK, 1, false, false);
		}
		else if(event.equalsIgnoreCase("30676-12.htm"))
		{
			st.setCond(7);
			st.takeItems(JACOB_NECK, 1);
			st.giveItems(CROOP_LETTER, 1, false, false);
		}
		else if(event.equalsIgnoreCase("30191-03.htm"))
			if(player.getLevel() < 79)
				htmltext = "<html><body>Only characters who are <font color=\"LEVEL\">level 79</font> or higher may complete this quest.</body></html>";
			else if(player.getBaseClassId() == player.getActiveClassId())
			{
				st.addExpAndSp(25000000, 2500000);
				st.finishQuest();
			}
			else
				return "subclass_forbidden.htm";
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == CROOP)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 79)
					htmltext = "30676-01.htm";
				else
					htmltext = "30676-00.htm";
			}
			else if(cond == 1)
				htmltext = "30676-04.htm";
			else if(cond == 2)
			{
				htmltext = "30676-05.htm";
				st.setCond(3);
				st.giveItems(CROOP_INTRO, 1, false, false);
			}
			else if(cond >= 3 && cond <= 5)
				htmltext = "30676-06.htm";
			else if(cond == 6)
				htmltext = "30676-07.htm";
		}
		else if(npcId == HECTOR)
		{
			if(cond == 3)
				htmltext = "30197-01.htm";
			if(cond >= 4 && cond <= 7)
				htmltext = "30197-04.htm";
		}
		else if(npcId == STAN)
		{
			if(cond == 4)
				htmltext = "30200-01.htm";
			if(cond >= 5 && cond <= 7)
				htmltext = "30200-05.htm";
		}
		else if(npcId == CORPSE)
		{
			if(cond == 5)
				htmltext = "32568-01.htm";
		}
		else if(npcId == HOLLINT)
		{
			if(cond == 7)
				htmltext = "30191-01.htm";
		}
		return htmltext;
	}

	@Override
	public String onCompleted(NpcInstance npc, QuestState st)
	{
		String htmltext = COMPLETED_DIALOG;
		int npcId = npc.getNpcId();
		if(npcId == CROOP)
			htmltext = "30676-13.htm";
		return htmltext;
	}
}
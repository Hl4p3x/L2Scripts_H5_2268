package quests;

import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _178_IconicTrinity extends QuestScript
{
	//NPC
	private static final int Kekropus = 32138;
	private static final int IconOfThePast = 32255;
	private static final int IconOfThePresent = 32256;
	private static final int IconOfTheFuture = 32257;
	//Items
	private static final int EnchantD = 956;

	public _178_IconicTrinity()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(Kekropus);
		addTalkId(IconOfThePast);
		addTalkId(IconOfThePresent);
		addTalkId(IconOfTheFuture);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("32138-02.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("32255-03.htm") || event.equalsIgnoreCase("32256-03.htm") || event.equalsIgnoreCase("32257-03.htm"))
			st.set("id", "");
		else if(event.equalsIgnoreCase("32255-09.htm"))
		{
			st.set("id", "");
			st.setCond(2);
		}
		else if(event.equalsIgnoreCase("32256-09.htm"))
		{
			st.set("id", "");
			st.setCond(3);
		}
		else if(event.equalsIgnoreCase("32257-06.htm"))
		{
			st.set("id", "");
			st.setCond(4);
		}
		else if(event.length() == 1)
		{
			int cond = st.getCond();
			int len = 0;
			if(!event.equals("0"))
			{
				if(st.get("id") == null)
					st.set("id", "");
				String id = st.get("id").toString();
				st.set("id", id + event);
				len = st.get("id").toString().length();
			}
			if(!event.equals("0") && len == 4 && (cond == 1 || cond == 2) || len == 5 && cond == 3)
			{
				if(cond == 1 && st.get("id") != null && st.get("id").toString().equalsIgnoreCase("CRTR"))
					htmltext = "32255-04.htm";
				else if(cond == 2 && st.get("id") != null && st.get("id").toString().equalsIgnoreCase("CNCL"))
					htmltext = "32256-04.htm";
				else if(cond == 3 && st.get("id") != null && st.get("id").toString().equalsIgnoreCase("CHAOS"))
					htmltext = "32257-04.htm";
				else
				{
					htmltext = "<html><body>Quest Failed</body></html>";
					st.abortQuest();
				}
			}
			else
			{
				if(cond == 1)
					htmltext = HtmCache.getInstance().getHtml("quests/_178_IconicTrinity/32255-03.htm", st.getPlayer());
				else if(cond == 2)
					htmltext = HtmCache.getInstance().getHtml("quests/_178_IconicTrinity/32256-03.htm", st.getPlayer());
				else
					htmltext = HtmCache.getInstance().getHtml("quests/_178_IconicTrinity/32257-03.htm", st.getPlayer());

				if(len == 0)
					htmltext = htmltext.replace("Password :  ", "").replace("#N", "first");
				else if(len == 1)
					htmltext = htmltext.replace("Password :  ", "*").replace("#N", "second");
				else if(len == 2)
					htmltext = htmltext.replace("Password :  ", "**").replace("#N", "third");
				else if(len == 3)
					htmltext = htmltext.replace("Password :  ", "***").replace("#N", "fourth");
				else if(len == 4)
					htmltext = htmltext.replace("Password :  ", "****").replace("#N", "fifth");
			}
		}
		else if(event.equalsIgnoreCase("32138-04.htm"))
		{
			st.giveItems(EnchantD, 1, false, false);
			st.addExpAndSp(20123, 976);
			st.finishQuest();
		}
		if(event.equalsIgnoreCase("32255-07.htm") || event.equalsIgnoreCase("32255-09.htm") || event.equalsIgnoreCase("32256-07.htm") || event.equalsIgnoreCase("32256-08.htm") || event.equalsIgnoreCase("32256-09.htm") || event.equalsIgnoreCase("32257-06.htm"))
		{
			htmltext = HtmCache.getInstance().getHtml("quests/_178_IconicTrinity/" + event, st.getPlayer());
			htmltext = htmltext.replace("%player_name%", st.getPlayer().getName());
		}

		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(npcId == Kekropus)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() != Race.KAMAEL)
					htmltext = "32138-05.htm";
				else if(st.getPlayer().getLevel() < 17)
					htmltext = "32138-00.htm";
				else
					htmltext = "32138-01.htm";
			}
			else if(cond == 4)
				htmltext = "32138-03.htm";
		}
		else if(npcId == IconOfThePast && cond == 1)
			htmltext = "32255-01.htm";
		else if(npcId == IconOfThePresent && cond == 2)
			htmltext = "32256-01.htm";
		else if(npcId == IconOfTheFuture && cond == 3)
			htmltext = "32257-01.htm";
		return htmltext;
	}
}
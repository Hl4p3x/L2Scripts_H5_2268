package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _115_TheOtherSideOfTruth extends QuestScript
{
	// NPCs
	private static int Rafforty = 32020;
	private static int Misa = 32018;
	private static int Kierre = 32022;
	private static int Ice_Sculpture1 = 32021;
	private static int Ice_Sculpture2 = 32077;
	private static int Ice_Sculpture3 = 32078;
	private static int Ice_Sculpture4 = 32079;
	//private static int Suspicious_Man = 32019;
	// Quest Items
	private static int Misas_Letter = 8079;
	private static int Raffortys_Letter = 8080;
	private static int Piece_of_Tablet = 8081;
	private static int Report_Piece = 8082;

	public _115_TheOtherSideOfTruth()
	{
		super(PARTY_NONE, ONETIME);
		addStartNpc(Rafforty);
		addTalkId(Misa);
		addTalkId(Kierre);
		addTalkId(Ice_Sculpture1);
		addTalkId(Ice_Sculpture2);
		addTalkId(Ice_Sculpture3);
		addTalkId(Ice_Sculpture4);
		addQuestItem(Misas_Letter);
		addQuestItem(Raffortys_Letter);
		addQuestItem(Piece_of_Tablet);
		addQuestItem(Report_Piece);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("32020-02.htm"))
			st.setCond(1);
		else if(event.equalsIgnoreCase("32020-06.htm") || event.equalsIgnoreCase("32020-08a.htm"))
			st.finishQuest();
		else if(event.equalsIgnoreCase("32020-05.htm"))
		{
			st.setCond(3);
			st.takeItems(Misas_Letter, 1);
		}
		else if(event.equalsIgnoreCase("32020-08.htm") || event.equalsIgnoreCase("32020-07a.htm"))
		{
			st.setCond(4);
		}
		else if(event.equalsIgnoreCase("32020-12.htm"))
		{
			st.setCond(5);
		}
		else if(event.equalsIgnoreCase("32018-04.htm"))
		{
			st.setCond(7);
			st.takeItems(Raffortys_Letter, 1);
		}
		else if(event.equalsIgnoreCase("Sculpture-04a.htm"))
		{
			st.setCond(8);
			if(st.getInt("32021") == 0 && st.getInt("32077") == 0)
				st.giveItems(Piece_of_Tablet, 1, false, false);

			//Functions.npcSay(st.addSpawn(Suspicious_Man, 117890, -126478, -2584, 0, 0, 300000), "This looks like the right place...");

			return "Sculpture-04.htm";
		}
		else if(event.equalsIgnoreCase("32022-02.htm"))
		{
			st.setCond(9);
			st.giveItems(Report_Piece, 1, false, false);

			//Functions.npcSay(st.addSpawn(Suspicious_Man, 104562, -107598, -3688, 0, 0, 300000), "We meet again.");
		}
		else if(event.equalsIgnoreCase("32020-16.htm"))
		{
			st.setCond(10);
			st.takeItems(Report_Piece, 1);
		}
		else if(event.equalsIgnoreCase("32020-18.htm"))
		{
			if(st.getQuestItemsCount(Piece_of_Tablet) > 0)
			{
				st.giveItems(ADENA_ID, 60044, true, true);
				st.finishQuest();
			}
			else
			{
				st.setCond(11);
				return "32020-19.htm";
			}
		}
		else if(event.equalsIgnoreCase("32020-19.htm"))
		{
			st.setCond(11);
		}
		else if(event.startsWith("32021") || event.startsWith("32077"))
		{
			if(event.contains("-pick"))
			{
				st.set("talk", "1");
				event = event.replace("-pick", "");
			}
			st.set(event, "1");
			return "Sculpture-05.htm";
		}

		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Rafforty)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 53)
					return "32020-01.htm";
				return "32020-00.htm";
			}
			else if(cond == 1)
				return "32020-03.htm";
			else if(cond == 2)
				return "32020-04.htm";
			else if(cond == 3)
				return "32020-05.htm";
			else if(cond == 4)
				return "32020-11.htm";
			else if(cond == 5)
			{
				st.setCond(6);
				st.giveItems(Raffortys_Letter, 1, true, true);
				return "32020-13.htm";
			}
			else if(cond == 6)
				return "32020-14.htm";
			else if(cond == 9)
				return "32020-15.htm";
			else if(cond == 10)
				return "32020-17.htm";
			else if(cond == 11)
				return "32020-20.htm";
			else if(cond == 12)
			{
				st.giveItems(ADENA_ID, 115673, true, true);
				st.addExpAndSp(493595, 40442);
				st.finishQuest();
				return "32020-18.htm";
			}
		}
		else if(npcId == Misa)
		{
			if(cond == 1)
			{
				st.setCond(2);
				st.giveItems(Misas_Letter, 1, false, false);
				return "32018-01.htm";
			}
			else if(cond == 2)
				return "32018-02.htm";
			else if(cond == 6)
				return "32018-03.htm";
			else if(cond == 7)
				return "32018-05.htm";
		}
		else if(npcId == Kierre)
		{
			if(cond == 8)
				return "32022-01.htm";
			else if(cond == 9)
				return "32022-03.htm";
		}
		else if((npcId == Ice_Sculpture1 || npcId == Ice_Sculpture2 || npcId == Ice_Sculpture3 || npcId == Ice_Sculpture4))
		{
			if(cond == 7)
			{
				String _npcId = String.valueOf(npcId);
				int npcId_flag = st.getInt(_npcId);
				if(npcId == Ice_Sculpture1 || npcId == Ice_Sculpture2)
				{
					int talk_flag = st.getInt("talk");
					return npcId_flag == 1 ? "Sculpture-02.htm" : talk_flag == 1 ? "Sculpture-06.htm" : "Sculpture-03-" + _npcId + ".htm";
				}
				else if(npcId_flag == 1)
					return "Sculpture-02.htm";
				else
				{
					st.set(_npcId, "1");
					return "Sculpture-01.htm";
				}
			}
			else if(cond == 8)
				return "Sculpture-04.htm";
			else if(cond == 11)
			{
				st.setCond(12);
				st.giveItems(Piece_of_Tablet, 1, false, false);
				return "Sculpture-07.htm";
			}
			else if(cond == 12)
				return "Sculpture-08.htm";
		}
		return NO_QUEST_DIALOG;
	}
}
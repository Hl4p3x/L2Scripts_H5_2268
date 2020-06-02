package quests;

import java.util.HashMap;
import java.util.Map;

import l2s.commons.text.PrintfFormat;
import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.scripts.Functions;

public class _060_GoodWorksReward extends QuestScript
{
	// NPCs
	private final static int Daeger = 31435;
	private final static int Helvetia = 30081;
	private final static int Black_Marketeer_of_Mammon = 31092;
	private final static int Mark = 32487;
	// Mobs
	private final static int Pursuer = 27340;
	// Items
	private final static int Bloody_Cloth_Fragment = 10867;
	private final static int Helvetias_Antidote = 10868;
	// Texts
	private final static PrintfFormat msgPursuer = new PrintfFormat("%s! I must kill you. Blame your own curiosity.");
	private final static String diePursuer = "You are strong. This was a mistake.";
	private final static String diePursuer_return = "You have good luck. I shall return.";
	private final static PrintfFormat Mammon_dialog = new PrintfFormat("<html><body>Black Marketeer of Mammon:<br>Forget about the money!<br>I will help you complete the class transfer, which is far more valuable! Which class would you like to be? Choose one.<br>%s</body></html>");

	private static long Last_Spawned_Pursuer = 0;

	public _060_GoodWorksReward()
	{
		super(PARTY_NONE, ONETIME);
		addStartNpc(Daeger);
		addTalkId(Helvetia);
		addTalkId(Black_Marketeer_of_Mammon);
		addTalkId(Mark);
		addKillId(Pursuer);
		addQuestItem(Bloody_Cloth_Fragment);
		addQuestItem(Helvetias_Antidote);
	}

	private boolean IsPursuerSpawned()
	{
		return GameObjectsStorage.contains(Last_Spawned_Pursuer);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int cond = st.getCond();

		if(event.equalsIgnoreCase("31435-03.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("32487-02.htm") && cond == 1 && !IsPursuerSpawned())
		{
			NpcInstance n = st.addSpawn(Pursuer, 72590, 148100, -3320, 1800000);
			Last_Spawned_Pursuer = n.getStoredId();
			n.setRunning();
			n.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, new Object[]{
					st.getPlayer(),
					100000
			});
			Functions.npcSay(n, msgPursuer.sprintf(st.getPlayer().getName()));
		}
		else if(event.equalsIgnoreCase("31435-05.htm") && cond == 3)
		{
			st.setCond(4);
		}
		else if(event.equalsIgnoreCase("30081-03.htm") && cond == 4)
		{
			if(st.getQuestItemsCount(Bloody_Cloth_Fragment) < 1)
				return "30081-03a.htm";
			st.takeItems(Bloody_Cloth_Fragment, -1);
			st.setCond(5);
		}
		else if(event.equalsIgnoreCase("30081-05.htm") && cond == 5)
		{
			st.setCond(6);
		}
		else if(event.equalsIgnoreCase("30081-08.htm") && (cond == 5 || cond == 6))
		{
			if(st.getQuestItemsCount(ADENA_ID) < 3000000)
			{
				st.setCond(6);
				return "30081-07.htm";
			}
			st.takeItems(ADENA_ID, 3000000);
			st.giveItems(Helvetias_Antidote, 1, false, false);
			st.setCond(7);
		}
		else if(event.equalsIgnoreCase("32487-06.htm") && cond == 7)
		{
			if(st.getQuestItemsCount(Helvetias_Antidote) < 1)
				return "32487-06a.htm";
			st.takeItems(Helvetias_Antidote, -1);
			st.setCond(8);
		}
		else if(event.equalsIgnoreCase("31435-08.htm") && cond == 8)
		{
			st.setCond(9);
		}
		else if(event.equalsIgnoreCase("31092-04.htm") && cond == 10)
		{
			if(profs.containsKey(st.getPlayer().getClassId().getId()))
				return Mammon_dialog.sprintf(profs.get(st.getPlayer().getClassId().getId()));
		}
		else if(event.equalsIgnoreCase("31092-05.htm") && cond == 10)
		{
			st.giveItems(ADENA_ID, 3000000, true, true);
			st.finishQuest();
		}
		else if(event.startsWith("marks-") && cond == 10)
		{
			String ocup = event.replaceAll("marks-", "");
			int[] _marks = marks.get(ocup);
			if(_marks == null)
				return "Error id: " + ocup;
			int return_adena = 0;
			for(int mark : _marks)
				if(st.getQuestItemsCount(mark) > 0)
					return_adena += 1000000;
				else
					st.giveItems(mark, 1, false, false);
			if(return_adena > 0)
				st.giveItems(ADENA_ID, return_adena, false, false);
			st.finishQuest();
			return "31092-05.htm";
		}

		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Daeger)
		{
			if(cond == 0)
			{
				if(npcId != Daeger)
					return NO_QUEST_DIALOG;
				if(st.getPlayer().getLevel() < 39 || st.getPlayer().getRace() == Race.KAMAEL || st.getPlayer().getClassLevel() != 1)
					return "31435-00.htm";
				return "31435-01.htm";
			}
			if(cond == 1 || cond == 2)
				return "31435-03.htm";
			if(cond == 3)
				return "31435-04.htm";
			if(cond >= 4 && cond <= 7)
				return "31435-06.htm";
			if(cond == 8)
				return "31435-07.htm";
			if(cond == 9)
			{
				st.setCond(10);
				return "31435-09.htm";
			}
			if(cond > 9)
				return "31435-10.htm";
		}
		else if(npcId == Helvetia)
		{
			if(cond == 4)
				return "30081-01.htm";
			if(cond == 5)
				return "30081-04.htm";
			if(cond == 6)
				return "30081-06.htm";
			if(cond > 6)
				return "30081-09.htm";
		}
		else if(npcId == Black_Marketeer_of_Mammon && cond == 10)
			return "31092-01.htm";
		else if(npcId == Mark)
		{
			if(cond == 1)
				return "32487-01.htm";
			if(cond == 2)
			{
				st.setCond(3);
				return "32487-03.htm";
			}
			if(cond == 3)
				return "32487-04.htm";
			if(cond == 7)
				return "32487-05.htm";
			if(cond > 7)
				return "32487-06.htm";
		}
		return NO_QUEST_DIALOG;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		Last_Spawned_Pursuer = 0;
		if(qs.getCond() == 1)
		{
			if(Rnd.chance(50))
			{
				Functions.npcSay(npc, diePursuer_return);
				return null;
			}
			Functions.npcSay(npc, diePursuer);
			qs.setCond(2);
			qs.giveItems(Bloody_Cloth_Fragment, 1, false, false);
		}
		return null;
	}

	private static final Map<Integer, String> profs = new HashMap<Integer, String>();
	private static final Map<String, int[]> marks = new HashMap<String, int[]>();

	static
	{
		profs.put(1, "<a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-WL\">Warlord.</a><br><a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-GL\">Gladiator.</a>");
		profs.put(4, "<a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-PA\">Paladin.</a><br><a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-DA\">Dark Avenger.</a>");
		profs.put(7, "<a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-TH\">Treasure Hunter.</a><br><a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-HK\">Hawkeye.</a>");
		profs.put(11, "<a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-SC\">Sorcerer.</a><br><a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-NM\">Necromancer.</a><br><a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-WA\">Warlock.</a>");
		profs.put(15, "<a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-BS\">Bishop.</a><br><a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-PP\">Prophet.</a>");
		profs.put(19, "<a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-TK\">Temple Knight.</a><br><a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-SS\">Swordsinger.</a>");
		profs.put(22, "<a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-PW\">Plainswalker.</a><br><a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-SR\">Silver Ranger.</a>");
		profs.put(26, "<a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-SP\">Spellsinger.</a><br><a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-ES\">Elemental Summoner.</a>");
		profs.put(29, "<a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-EE\">Elven Elder.</a>");
		profs.put(32, "<a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-SK\">Shillien Knight.</a><br><a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-BD\">Blade Dancer.</a>");
		profs.put(35, "<a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-AW\">Abyss Walker.</a><br><a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-PR\">Phantom Ranger.</a>");
		profs.put(39, "<a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-SH\">Spellhowler.</a><br><a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-PS\">Phantom Summoner.</a>");
		profs.put(42, "<a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-SE\">Shillien Elder.</a>");
		profs.put(45, "<a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-DT\">Destroyer.</a>");
		profs.put(47, "<a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-TR\">Tyrant.</a>");
		profs.put(50, "<a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-OL\">Overlord.</a><br><a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-WC\">Warcryer.</a>");
		profs.put(54, "<a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-BH\">Bounty Hunter.</a>");
		profs.put(56, "<a action=\"bypass -h npc_%objectId%_QuestEvent 60 marks-WS\">Warsmith.</a>");

		marks.put("AW", new int[]{
				2673,
				3172,
				2809
		});
		marks.put("BD", new int[]{
				2627,
				3172,
				2762
		});
		marks.put("BH", new int[]{
				2809,
				3119,
				3238
		});
		marks.put("BS", new int[]{
				2721,
				2734,
				2820
		});
		marks.put("DA", new int[]{
				2633,
				2734,
				3307
		});
		marks.put("DT", new int[]{
				2627,
				3203,
				3276
		});
		marks.put("EE", new int[]{
				2721,
				3140,
				2820
		});
		marks.put("ES", new int[]{
				2674,
				3140,
				3336
		});
		marks.put("GL", new int[]{
				2627,
				2734,
				2762
		});
		marks.put("HK", new int[]{
				2673,
				2734,
				3293
		});
		marks.put("NM", new int[]{
				2674,
				2734,
				3307
		});
		marks.put("OL", new int[]{
				2721,
				3203,
				3390
		});
		marks.put("PA", new int[]{
				2633,
				2734,
				2820
		});
		marks.put("PP", new int[]{
				2721,
				2734,
				2821
		});
		marks.put("PR", new int[]{
				2673,
				3172,
				3293
		});
		marks.put("PS", new int[]{
				2674,
				3172,
				3336
		});
		marks.put("PW", new int[]{
				2673,
				3140,
				2809
		});
		marks.put("SC", new int[]{
				2674,
				2734,
				2840
		});
		marks.put("SE", new int[]{
				2721,
				3172,
				2821
		});
		marks.put("SH", new int[]{
				2674,
				3172,
				2840
		});
		marks.put("SK", new int[]{
				2633,
				3172,
				3307
		});
		marks.put("SP", new int[]{
				2674,
				3140,
				2840
		});
		marks.put("SR", new int[]{
				2673,
				3140,
				3293
		});
		marks.put("SS", new int[]{
				2627,
				3140,
				2762
		});
		marks.put("TH", new int[]{
				2673,
				2734,
				2809
		});
		marks.put("TK", new int[]{
				2633,
				3140,
				2820
		});
		marks.put("TR", new int[]{
				2627,
				3203,
				2762
		});
		marks.put("WA", new int[]{
				2674,
				2734,
				3336
		});
		marks.put("WC", new int[]{
				2721,
				3203,
				2879
		});
		marks.put("WL", new int[]{
				2627,
				2734,
				3276
		});
		marks.put("WS", new int[]{
				2867,
				3119,
				3238
		});
	}
}
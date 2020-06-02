package quests;

import java.util.HashMap;
import java.util.Map;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _371_ShriekOfGhosts extends QuestScript
{
	// NPCs
	private static int REVA = 30867;
	private static int PATRIN = 30929;
	// Mobs
	private static int Hallates_Warrior = 20818;
	private static int Hallates_Knight = 20820;
	private static int Hallates_Commander = 20824;
	// Items
	private static int Ancient_Porcelain__Excellent = 6003;
	private static int Ancient_Porcelain__High_Quality = 6004;
	private static int Ancient_Porcelain__Low_Quality = 6005;
	private static int Ancient_Porcelain__Lowest_Quality = 6006;
	// Quest Items
	private static int Ancient_Ash_Urn = 5903;
	private static int Ancient_Porcelain = 6002;
	// Chances
	private static int Urn_Chance = 43;
	private static int Ancient_Porcelain__Excellent_Chance = 1; //       1%  80000a (1000%)
	private static int Ancient_Porcelain__High_Quality_Chance = 14; //   13% 16000a (400%)
	private static int Ancient_Porcelain__Low_Quality_Chance = 46; //    32% 4000a  (50%)
	private static int Ancient_Porcelain__Lowest_Quality_Chance = 84; // 38% 2640a  (33%)
	//                                                                   16% chance of nothing
	private Map<Integer, Integer> common_chances = new HashMap<Integer, Integer>();

	public _371_ShriekOfGhosts()
	{
		super(PARTY_ONE, REPEATABLE);

		addStartNpc(REVA);
		addTalkId(PATRIN);

		addKillId(Hallates_Warrior);
		addKillId(Hallates_Knight);
		addKillId(Hallates_Commander);

		addQuestItem(Ancient_Ash_Urn);

		common_chances.put(Hallates_Warrior, 71);
		common_chances.put(Hallates_Knight, 74);
		common_chances.put(Hallates_Commander, 82);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("30867-03.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("30867-10.htm"))
		{
			long Ancient_Ash_Urn_count = st.getQuestItemsCount(Ancient_Ash_Urn);
			if(Ancient_Ash_Urn_count > 0)
			{
				st.takeItems(Ancient_Ash_Urn, -1);
				st.giveItems(ADENA_ID, Ancient_Ash_Urn_count * 1000L, true, true);
			}
			st.finishQuest();
		}
		else if(event.equalsIgnoreCase("30867-TRADE"))
		{
			long Ancient_Ash_Urn_count = st.getQuestItemsCount(Ancient_Ash_Urn);
			if(Ancient_Ash_Urn_count > 0)
			{
				htmltext = Ancient_Ash_Urn_count > 100 ? "30867-08.htm" : "30867-07.htm";
				int bonus = Ancient_Ash_Urn_count > 100 ? 17000 : 3000;
				st.takeItems(Ancient_Ash_Urn, -1);
				st.giveItems(ADENA_ID, bonus + Ancient_Ash_Urn_count * 1000L, true, true);
			}
			else
				htmltext = "30867-06.htm";
		}
		else if(event.equalsIgnoreCase("30929-TRADE"))
		{
			long Ancient_Porcelain_count = st.getQuestItemsCount(Ancient_Porcelain);
			if(Ancient_Porcelain_count > 0)
			{
				st.takeItems(Ancient_Porcelain, 1);
				int chance = Rnd.get(100);
				if(chance < Ancient_Porcelain__Excellent_Chance)
				{
					st.giveItems(Ancient_Porcelain__Excellent, 1, false, false);
					htmltext = "30929-03.htm";
				}
				else if(chance < Ancient_Porcelain__High_Quality_Chance)
				{
					st.giveItems(Ancient_Porcelain__High_Quality, 1, false, false);
					htmltext = "30929-04.htm";
				}
				else if(chance < Ancient_Porcelain__Low_Quality_Chance)
				{
					st.giveItems(Ancient_Porcelain__Low_Quality, 1, false, false);
					htmltext = "30929-05.htm";
				}
				else if(chance < Ancient_Porcelain__Lowest_Quality_Chance)
				{
					st.giveItems(Ancient_Porcelain__Lowest_Quality, 1, false, false);
					htmltext = "30929-06.htm";
				}
				else
					htmltext = "30929-07.htm";
			}
			else
				htmltext = "30929-02.htm";
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 0)
		{
			if(npcId != REVA)
				return htmltext;

			if(st.getPlayer().getLevel() >= 59)
				htmltext = "30867-02.htm";
			else
				htmltext = "30867-01.htm";
		}
		else if(npcId == REVA)
			htmltext = st.getQuestItemsCount(Ancient_Porcelain) > 0 ? "30867-05.htm" : "30867-04.htm";
		else if(npcId == PATRIN)
			htmltext = "30929-01.htm";

		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		Player player = qs.getRandomPartyMember(1, Config.ALT_PARTY_DISTRIBUTION_RANGE);
		if(player == null)
			return null;

		QuestState st = player.getQuestState(qs.getQuest());
		if(st == null)
			return null;

		Integer _chance = common_chances.get(npc.getNpcId());
		if(_chance == null)
			return null;

		if(Rnd.chance(_chance))
		{
			st.giveItems(Rnd.chance(Urn_Chance) ? Ancient_Ash_Urn : Ancient_Porcelain, 1, true, true);
			st.playSound(SOUND_ITEMGET);
		}

		return null;
	}
}
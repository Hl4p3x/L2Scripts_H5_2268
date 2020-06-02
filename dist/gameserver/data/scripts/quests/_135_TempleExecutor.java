package quests;

import java.util.ArrayList;
import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;


public class _135_TempleExecutor extends QuestScript
{
	// NPCs
	private final static int Shegfield = 30068;
	private final static int Pano = 30078;
	private final static int Alex = 30291;
	private final static int Sonin = 31773;

	// Mobs
	private final static int[] mobs = {
			20781,
			21104,
			21105,
			21106,
			21107
	};

	// Quest Items
	private final static int Stolen_Cargo = 10328;
	private final static int Hate_Crystal = 10329;
	private final static int Old_Treasure_Map = 10330;
	private final static int Sonins_Credentials = 10331;
	private final static int Panos_Credentials = 10332;
	private final static int Alexs_Credentials = 10333;

	// Items
	private final static int Badge_Temple_Executor = 10334;

	public _135_TempleExecutor()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(Shegfield);
		addTalkId(Alex);
		addTalkId(Sonin);
		addTalkId(Pano);
		addKillId(mobs);
		addQuestItem(Stolen_Cargo);
		addQuestItem(Hate_Crystal);
		addQuestItem(Old_Treasure_Map);
		addQuestItem(Sonins_Credentials);
		addQuestItem(Panos_Credentials);
		addQuestItem(Alexs_Credentials);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("shegfield_q0135_03.htm"))
			st.setCond(1);
		else if(event.equalsIgnoreCase("shegfield_q0135_13.htm"))
		{
			st.unset("Report");
			st.giveItems(ADENA_ID, 16294, true, true);
			st.addExpAndSp(30000, 2000);
			st.giveItems(Badge_Temple_Executor, 1, false, false);
			st.finishQuest();
		}
		else if(event.equalsIgnoreCase("shegfield_q0135_04.htm"))
			st.setCond(2);
		else if(event.equalsIgnoreCase("alankell_q0135_07.htm"))
			st.setCond(3);
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Shegfield)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 35)
					return "shegfield_q0135_02.htm";
				return "shegfield_q0135_01.htm";
			}
			if(cond == 1)
				return "shegfield_q0135_03.htm";
			if(cond == 5)
			{
				if(st.getInt("Report") == 1)
					return "shegfield_q0135_09.htm";
				if(st.getQuestItemsCount(Sonins_Credentials) > 0 && st.getQuestItemsCount(Panos_Credentials) > 0 && st.getQuestItemsCount(Alexs_Credentials) > 0)
				{
					st.takeItems(Panos_Credentials, -1);
					st.takeItems(Sonins_Credentials, -1);
					st.takeItems(Alexs_Credentials, -1);
					st.set("Report", "1");
					return "shegfield_q0135_08.htm";
				}
				return NO_QUEST_DIALOG;
			}
			return "shegfield_q0135_06.htm";
		}

		if(npcId == Alex)
		{
			if(cond == 2)
				return "alankell_q0135_02.htm";
			if(cond == 3)
				return "alankell_q0135_08.htm";
			if(cond == 4)
			{
				if(st.getQuestItemsCount(Sonins_Credentials) > 0 && st.getQuestItemsCount(Panos_Credentials) > 0)
				{
					st.setCond(5);
					st.takeItems(Old_Treasure_Map, -1);
					st.giveItems(Alexs_Credentials, 1, false, false);
					return "alankell_q0135_10.htm";
				}
				return "alankell_q0135_09.htm";
			}
			if(cond == 5)
				return "alankell_q0135_11.htm";
		}

		if(npcId == Sonin)
		{
			if(st.getQuestItemsCount(Stolen_Cargo) < 10)
				return "warehouse_keeper_sonin_q0135_04.htm";
			st.takeItems(Stolen_Cargo, -1);
			st.giveItems(Sonins_Credentials, 1, false, false);
			st.playSound(SOUND_MIDDLE);
			return "warehouse_keeper_sonin_q0135_03.htm";
		}

		if(npcId == Pano && cond == 4)
		{
			if(st.getQuestItemsCount(Hate_Crystal) < 10)
				return "pano_q0135_04.htm";
			st.takeItems(Hate_Crystal, -1);
			st.giveItems(Panos_Credentials, 1, false, false);
			st.playSound(SOUND_MIDDLE);
			return "pano_q0135_03.htm";
		}

		return NO_QUEST_DIALOG;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(qs.getCond() == 3)
		{
			List<Integer> drops = new ArrayList<Integer>();
			if(qs.getQuestItemsCount(Stolen_Cargo) < 10)
				drops.add(Stolen_Cargo);
			if(qs.getQuestItemsCount(Hate_Crystal) < 10)
				drops.add(Hate_Crystal);
			if(qs.getQuestItemsCount(Old_Treasure_Map) < 10)
				drops.add(Old_Treasure_Map);
			if(drops.isEmpty())
				return null;
			int drop = drops.get(Rnd.get(drops.size()));
			qs.giveItems(drop, 1, true, true);
			if(drops.size() == 1 && qs.getQuestItemsCount(drop) >= 10)
			{
				qs.setCond(4);
				return null;
			}
			qs.playSound(SOUND_ITEMGET);
		}
		return null;
	}
}
package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _642_APowerfulPrimevalCreature extends QuestScript
{
	// NPCs
	private static int Dinn = 32105;
	// Mobs
	private static int Ancient_Egg = 18344;
	private static int[] Dino = {
			22196,
			22197,
			22198,
			22199,
			22200,
			22201,
			22202,
			22203,
			22204,
			22205,
			22218,
			22219,
			22220,
			22223,
			22224,
			22225,
			22226,
			22227,
			22742,
			22743,
			22744,
			22745
	};
	// Items
	private static int[] Rewards = {
			8690,
			8692,
			8694,
			8696,
			8698,
			8700,
			8702,
			8704,
			8706,
			8708,
			8710
	};
	// Quest Items
	private static int Dinosaur_Tissue = 8774;
	private static int Dinosaur_Egg = 8775;
	// Chances
	private static int Dinosaur_Tissue_Chance = 33;
	private static int Dinosaur_Egg_Chance = 1;

	public _642_APowerfulPrimevalCreature()
	{
		super(PARTY_ONE, REPEATABLE);
		addStartNpc(Dinn);
		addTalkId(Dinn);
		addKillId(Ancient_Egg);
		for(int dino_id : Dino)
			addKillId(dino_id);
		addQuestItem(Dinosaur_Tissue);
		addQuestItem(Dinosaur_Egg);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		long Dinosaur_Tissue_Count = st.getQuestItemsCount(Dinosaur_Tissue);
		if(event.equalsIgnoreCase("dindin_q0642_05.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("dindin_q0642_08a.htm"))
		{
			if(Dinosaur_Tissue_Count == 0)
				return "dindin_q0642_09.htm";
			st.takeItems(Dinosaur_Tissue, -1);
			st.giveItems(ADENA_ID, Dinosaur_Tissue_Count * 5000, false, false);
			st.playSound(SOUND_MIDDLE);
		}
		else if(event.contains("reward1"))
		{
			if(Dinosaur_Tissue_Count < 150)
				return "dindin_q0642_09.htm";
			if(!st.haveQuestItem(Dinosaur_Egg))
				return "dindin_q0642_12.htm";
			st.takeItems(Dinosaur_Tissue, 150);
			st.takeItems(Dinosaur_Egg, 1);
			st.playSound(SOUND_MIDDLE);
				if(event.contains("8690"))
					st.giveItems(8690, 1, true);
				else if(event.contains("8692"))
					st.giveItems(8692, 1, true);
				else if(event.contains("8694"))
					st.giveItems(8694, 1, true);
				else if(event.contains("8696"))
					st.giveItems(8696, 1, true);
				else if(event.contains("8698"))
					st.giveItems(8698, 1, true);
				else if(event.contains("8700"))
					st.giveItems(8700, 1, true);
				else if(event.contains("8702"))
					st.giveItems(8702, 1, true);
				else if(event.contains("8704"))
					st.giveItems(8704, 1, true);
				else if(event.contains("8706"))
					st.giveItems(8706, 1, true);
				else if(event.contains("8708"))
					st.giveItems(8708, 1, true);
				else if(event.contains("8710"))
					st.giveItems(8710, 1, true);
			return "dindin_q0642_11.htm";
		}
		else if(event.contains("reward2"))
		{
			if (Dinosaur_Tissue_Count < 450)
				return "dindin_q0642_10.htm";
			st.takeItems(Dinosaur_Tissue, 450);
			st.playSound(SOUND_MIDDLE);
				if(event.contains("9967"))
					st.giveItems(9967, 1, true);
				else if(event.contains("9968"))
					st.giveItems(9968, 1, true);
				else if(event.contains("9969"))
					st.giveItems(9969, 1, true);
				else if(event.contains("9970"))
					st.giveItems(9970, 1, true);
				else if(event.contains("9971"))
					st.giveItems(9971, 1, true);
				else if(event.contains("9972"))
					st.giveItems(9972, 1, true);
				else if(event.contains("9973"))
					st.giveItems(9973, 1, true);
				else if(event.contains("9974"))
					st.giveItems(9974, 1, true);
				else if(event.contains("9975"))
					st.giveItems(9975, 1, true);
			return "dindin_q0642_11.htm";
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();

		if(npc.getNpcId() == Dinn)
		{
			if (cond == 0)
			{
				if (st.getPlayer().getLevel() < 75)
					return "dindin_q0642_02.htm";
				return "dindin_q0642_01.htm";
			}
			if (cond == 1)
			{
				if (!st.haveQuestItem(Dinosaur_Tissue))
					return "dindin_q0642_09.htm";
				else
					return "dindin_q0642_08.htm";
			}
		}
		return htmltext;

	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() != 1)
			return null;
		if(npc.getNpcId() == Ancient_Egg && Rnd.chance(Dinosaur_Egg_Chance))
			st.giveItems(Dinosaur_Egg,1,true, true);
		else if(Rnd.chance(Dinosaur_Tissue_Chance))
			st.giveItems(Dinosaur_Tissue,1, true, true);
		return null;
	}

}
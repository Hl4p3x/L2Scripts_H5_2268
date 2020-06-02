package quests;

import org.apache.commons.lang3.ArrayUtils;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _643_RiseAndFallOfTheElrokiTribe extends QuestScript
{
	// NPCs
	private static final int SINGSING = 32106;
	private static final int KARAKAWEI = 32117;
	// Item
	private static final int BONES_OF_A_PLAINS_DINOSAUR = 8776;
	// Misc
	private static final int MIN_LEVEL = 75;
	private static final double CHANCE_MOBS1 = 11.6;
	private static final double CHANCE_MOBS2 = 36.0;
	private static final double CHANCE_DEINO = 55.8;

	// Rewards
	private static final int[] PIECE =
	{
		8712, // Sirra's Blade Edge
		8713, // Sword of Ipos Blade
		8714, // Barakiel's Axe Piece
		8715, // Behemoth's Tuning Fork Piece
		8716, // Naga Storm Piece
		8717, // Tiphon's Spear Edge
		8718, // Shyeed's Bow Shaft
		8719, // Sobekk's Hurricane Edge
		8720, // Themis' Tongue Piece
		8721, // Cabrio's Hand Head
		8722, // Daimon Crystal Fragment
	};
	// Mobs
	private static final int[] MOBS1 =
	{
		22200, // Ornithomimus
		22201, // Ornithomimus
		22202, // Ornithomimus
		22204, // Deinonychus
		22205, // Deinonychus
		22208, // Pachycephalosaurus
		22209, // Pachycephalosaurus
		22210, // Pachycephalosaurus
		22211, // Wild Strider
		22212, // Wild Strider
		22213, // Wild Strider
		22219, // Ornithomimus
		22220, // Deinonychus
		22221, // Pachycephalosaurus
		22222, // Wild Strider
		22224, // Ornithomimus
		22225, // Deinonychus
		22226, // Pachycephalosaurus
		22227, // Wild Strider
	};
	
	private static final int[] MOBS2 =
	{
		22742, // Ornithomimus
		22743, // Deinonychus
		22744, // Ornithomimus
		22745, // Deinonychus
	};
	
	private static final int DEINONYCHUS = 22203;

	public _643_RiseAndFallOfTheElrokiTribe()
	{
		super(PARTY_ONE, REPEATABLE);

		addStartNpc(SINGSING);
		addTalkId(KARAKAWEI);

		addKillId(MOBS1);
		addKillId(MOBS2);
		addKillId(DEINONYCHUS);

		addQuestItem(BONES_OF_A_PLAINS_DINOSAUR);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		long count = st.getQuestItemsCount(BONES_OF_A_PLAINS_DINOSAUR);
		if(event.equalsIgnoreCase("singsing_q0643_05.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("shaman_caracawe_q0643_06.htm"))
		{
			if(count >= 300)
			{
				st.takeItems(BONES_OF_A_PLAINS_DINOSAUR, 300);
				st.giveItems(PIECE[Rnd.get(PIECE.length)], 5, false, false);
			}
			else
				htmltext = "shaman_caracawe_q0643_05.htm";
		}
		else if(event.equalsIgnoreCase("None"))
			htmltext = null;
		else if(event.equalsIgnoreCase("Quit"))
		{
			htmltext = null;
			st.finishQuest();
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
			if(st.getPlayer().getLevel() >= MIN_LEVEL)
				htmltext = "singsing_q0643_01.htm";
			else
				htmltext = "singsing_q0643_04.htm";
		}
		else
		{
			if(npcId == SINGSING)
			{
				long count = st.getQuestItemsCount(BONES_OF_A_PLAINS_DINOSAUR);
				if(count == 0)
					htmltext = "singsing_q0643_08.htm";
				else
				{
					htmltext = "singsing_q0643_08.htm";
					st.takeItems(BONES_OF_A_PLAINS_DINOSAUR, -1);
					st.giveItems(ADENA_ID, count * 1374, false, false);
				}
			}
			else if(npcId == KARAKAWEI)
				htmltext = "shaman_caracawe_q0643_02.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1)
		{
			final int npcId = npc.getNpcId();
			if(ArrayUtils.contains(MOBS1, npcId))
			{
				if(Rnd.chance(CHANCE_MOBS1))
					st.rollAndGive(BONES_OF_A_PLAINS_DINOSAUR, 2, CHANCE_MOBS1);
				else
					st.rollAndGive(BONES_OF_A_PLAINS_DINOSAUR, 1, CHANCE_MOBS1);
			}
			else if(ArrayUtils.contains(MOBS2, npcId))
			{
				st.rollAndGive(BONES_OF_A_PLAINS_DINOSAUR, 1, CHANCE_MOBS2);
			}
			else if(npcId == DEINONYCHUS)
			{
				st.rollAndGive(BONES_OF_A_PLAINS_DINOSAUR, 1, CHANCE_DEINO);
			}
		}
		return null;
	}
}
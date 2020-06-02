package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * @author Bonux
 * http://l2wiki.info/Драгоценный_камень_дракона_земли
 */
public class _10504_JewelOfAntharas extends QuestScript
{
	//NPC's
	private static final int THEODRIC = 30755;
	private static final int ULTIMATE_ANTHARAS = 29068;
	//Item's
	private static final int CLEAR_CRYSTAL = 21905;
	private static final int FILLED_CRYSTAL_ANTHARAS = 21907;
	private static final int PORTAL_STONE = 3865;
	private static final int JEWEL_OF_ANTHARAS = 21898;

	public _10504_JewelOfAntharas()
	{
		super(PARTY_ALL, ONETIME);
		addStartNpc(THEODRIC);
		addQuestItem(CLEAR_CRYSTAL, FILLED_CRYSTAL_ANTHARAS);
		addKillId(ULTIMATE_ANTHARAS);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("antharas_watchman_theodric_q10504_04.htm"))
		{
			st.setCond(1);
			st.giveItems(CLEAR_CRYSTAL, 1, false, false);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == THEODRIC)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 84)
					htmltext = "antharas_watchman_theodric_q10504_00.htm";
				else if(st.getQuestItemsCount(PORTAL_STONE) < 1)
					htmltext = "antharas_watchman_theodric_q10504_00a.htm";
				else
					htmltext = "antharas_watchman_theodric_q10504_01.htm";
			}
			else if(cond == 1)
			{
				if(st.getQuestItemsCount(CLEAR_CRYSTAL) < 1)
				{
					htmltext = "antharas_watchman_theodric_q10504_08.htm";
					st.giveItems(CLEAR_CRYSTAL, 1, false, false);
				}
				else
					htmltext = "antharas_watchman_theodric_q10504_05.htm";
			}
			else if(cond == 2)
			{
				if(st.getQuestItemsCount(FILLED_CRYSTAL_ANTHARAS) >= 1)
				{
					htmltext = "antharas_watchman_theodric_q10504_07.htm";
					st.takeAllItems(FILLED_CRYSTAL_ANTHARAS);
					st.giveItems(JEWEL_OF_ANTHARAS, 1, false, false);
					st.finishQuest();
				}
				else
					htmltext = "antharas_watchman_theodric_q10504_06.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onCompleted(NpcInstance npc, QuestState st)
	{
		String htmltext = COMPLETED_DIALOG;
		int npcId = npc.getNpcId();
		if(npcId == THEODRIC)
			htmltext = "antharas_watchman_theodric_q10504_09.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(cond == 1 && npcId == ULTIMATE_ANTHARAS)
		{
			st.takeAllItems(CLEAR_CRYSTAL);
			st.giveItems(FILLED_CRYSTAL_ANTHARAS, 1, false, false);
			st.setCond(2);
		}
		return null;
	}
}
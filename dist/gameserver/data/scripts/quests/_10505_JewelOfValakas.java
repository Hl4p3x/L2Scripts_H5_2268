package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * @author Bonux
 * http://l2wiki.info/Драгоценный_камень_дракона_огня
 */
public class _10505_JewelOfValakas extends QuestScript
{
	//NPC's
	private static final int KLEIN = 31540;
	private static final int VALAKAS = 29028;
	//Item's
	private static final int EMPTY_CRYSTAL = 21906;
	private static final int FILLED_CRYSTAL_VALAKAS = 21908;
	private static final int VACUALITE_FLOATING_STONE = 7267;
	private static final int JEWEL_OF_VALAKAS = 21896;

	public _10505_JewelOfValakas()
	{
		super(PARTY_ALL, ONETIME);
		addStartNpc(KLEIN);
		addQuestItem(EMPTY_CRYSTAL, FILLED_CRYSTAL_VALAKAS);
		addKillId(VALAKAS);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("valakas_watchman_klein_q10505_04.htm"))
		{
			st.setCond(1);
			st.giveItems(EMPTY_CRYSTAL, 1, false, false);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == KLEIN)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 84)
					htmltext = "valakas_watchman_klein_q10505_00.htm";
				else if(st.getQuestItemsCount(VACUALITE_FLOATING_STONE) < 1)
					htmltext = "valakas_watchman_klein_q10505_00a.htm";
				else
					htmltext = "valakas_watchman_klein_q10505_01.htm";
			}
			else if(cond == 1)
			{
				if(st.getQuestItemsCount(EMPTY_CRYSTAL) < 1)
				{
					htmltext = "valakas_watchman_klein_q10505_08.htm";
					st.giveItems(EMPTY_CRYSTAL, 1, false, false);
				}
				else
					htmltext = "valakas_watchman_klein_q10505_05.htm";
			}
			else if(cond == 2)
			{
				if(st.getQuestItemsCount(FILLED_CRYSTAL_VALAKAS) >= 1)
				{
					htmltext = "valakas_watchman_klein_q10505_07.htm";
					st.takeAllItems(FILLED_CRYSTAL_VALAKAS);
					st.giveItems(JEWEL_OF_VALAKAS, 1, false, false);
					st.finishQuest();
				}
				else
					htmltext = "valakas_watchman_klein_q10505_06.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onCompleted(NpcInstance npc, QuestState st)
	{
		String htmltext = COMPLETED_DIALOG;
		int npcId = npc.getNpcId();
		if(npcId == KLEIN)
			htmltext = "valakas_watchman_klein_q10505_09.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 1 && npcId == VALAKAS)
		{
			st.takeAllItems(EMPTY_CRYSTAL);
			st.giveItems(FILLED_CRYSTAL_VALAKAS, 1, false, false);
			st.setCond(2);
		}
		return null;
	}
}
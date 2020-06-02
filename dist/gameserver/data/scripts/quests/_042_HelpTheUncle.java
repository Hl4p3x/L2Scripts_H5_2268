package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _042_HelpTheUncle extends QuestScript
{
	private static final int WATERS = 30828;
	private static final int SOPHYA = 30735;

	private static final int TRIDENT = 291;
	private static final int MAP_PIECE = 7548;
	private static final int MAP = 7549;
	private static final int PET_TICKET = 7583;

	private static final int MONSTER_EYE_DESTROYER = 20068;
	private static final int MONSTER_EYE_GAZER = 20266;

	private static final int MAX_COUNT = 30;

	public _042_HelpTheUncle()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(WATERS);
		addTalkId(SOPHYA);

		addKillId(MONSTER_EYE_DESTROYER);
		addKillId(MONSTER_EYE_GAZER);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("1"))
		{
			htmltext = "pet_manager_waters_q0042_0104.htm";
			st.setCond(1);
		}
		else if(event.equals("3") && st.getQuestItemsCount(TRIDENT) > 0)
		{
			htmltext = "pet_manager_waters_q0042_0201.htm";
			st.takeItems(TRIDENT, 1);
			st.setCond(2);
		}
		else if(event.equals("4") && st.getQuestItemsCount(MAP_PIECE) >= MAX_COUNT)
		{
			htmltext = "pet_manager_waters_q0042_0301.htm";
			st.takeItems(MAP_PIECE, MAX_COUNT);
			st.giveItems(MAP, 1, false, false);
			st.setCond(4);
		}
		else if(event.equals("5") && st.getQuestItemsCount(MAP) > 0)
		{
			htmltext = "sophia_q0042_0401.htm";
			st.takeItems(MAP, 1);
			st.setCond(5);
		}
		else if(event.equals("7"))
		{
			htmltext = "pet_manager_waters_q0042_0501.htm";
			st.giveItems(PET_TICKET, 1, false, false);
			st.unset("cond");
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
		if(npcId == WATERS)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 25)
					htmltext = "pet_manager_waters_q0042_0101.htm";
				else
					htmltext = "pet_manager_waters_q0042_0103.htm";
			}
			else if(cond == 1)
			{
				if(st.getQuestItemsCount(TRIDENT) == 0)
					htmltext = "pet_manager_waters_q0042_0106.htm";
				else
					htmltext = "pet_manager_waters_q0042_0105.htm";
			}
			else if(cond == 2)
				htmltext = "pet_manager_waters_q0042_0204.htm";
			else if(cond == 3)
				htmltext = "pet_manager_waters_q0042_0203.htm";
			else if(cond == 4)
				htmltext = "pet_manager_waters_q0042_0303.htm";
			else if(cond == 5)
				htmltext = "pet_manager_waters_q0042_0401.htm";
		}
		else if(npcId == SOPHYA)
		{
			if(cond == 4 && st.getQuestItemsCount(MAP) > 0)
				htmltext = "sophia_q0042_0301.htm";
			else if(cond == 5)
				htmltext = "sophia_q0042_0402.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 2)
		{
			if(st.getQuestItemsCount(MAP_PIECE) < MAX_COUNT)
			{
				st.giveItems(MAP_PIECE, 1, true, true);
				st.playSound(SOUND_ITEMGET);
			}
			if(st.getQuestItemsCount(MAP_PIECE) >= MAX_COUNT)
			{
				st.setCond(3);
			}
		}
		return null;
	}
}
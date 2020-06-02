package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * Last editor - LEXX
 */
public class _113_StatusOfTheBeaconTower extends QuestScript
{
	// NPC
	private static final int MOIRA = 31979;
	private static final int TORRANT = 32016;

	// QUEST ITEM
	private static final int BOX = 8086;

	public _113_StatusOfTheBeaconTower()
	{
		super(PARTY_NONE, ONETIME);
		addStartNpc(MOIRA);
		addTalkId(TORRANT);

		addQuestItem(BOX);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("seer_moirase_q0113_0104.htm"))
		{
			st.setCond(1);
			st.giveItems(BOX, 1, false, false);
		}
		else if(event.equalsIgnoreCase("torant_q0113_0201.htm"))
		{
			st.giveItems(ADENA_ID, 154800, true, true);
			st.addExpAndSp(619300, 44200);
			st.takeItems(BOX, 1);
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
		if(npcId == MOIRA)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 40)
					htmltext = "seer_moirase_q0113_0101.htm";
				else
					htmltext = "seer_moirase_q0113_0103.htm";
			}
			else if(cond == 1)
				htmltext = "seer_moirase_q0113_0105.htm";
		}
		else if(npcId == TORRANT && st.getQuestItemsCount(BOX) >= 1)
			htmltext = "torant_q0113_0101.htm";
		return htmltext;
	}
}
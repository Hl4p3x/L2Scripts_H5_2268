package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _109_InSearchOfTheNest extends QuestScript
{
	//NPC
	private static final int PIERCE = 31553;
	private static final int CORPSE = 32015;
	private static final int KAHMAN = 31554;

	//QUEST ITEMS
	private static final int MEMO = 8083;
	private static final int GOLDEN_BADGE_RECRUIT = 7246;
	private static final int GOLDEN_BADGE_SOLDIER = 7247;

	public _109_InSearchOfTheNest()
	{
		super(PARTY_NONE, ONETIME);
		addStartNpc(PIERCE);
		addTalkId(CORPSE);
		addTalkId(KAHMAN);

		addQuestItem(MEMO);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		int cond = st.getCond();
		if(event.equalsIgnoreCase("Memo") && cond == 1)
		{
			st.giveItems(MEMO, 1, false, false);
			st.setCond(2);
			st.playSound(SOUND_ITEMGET);
			htmltext = "You've find something...";
		}
		else if(event.equalsIgnoreCase("merc_cap_peace_q0109_0301.htm") && cond == 2)
		{
			st.takeItems(MEMO, -1);
			st.setCond(3);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == CORPSE)
		{
			if(cond == 1)
				htmltext = "corpse_of_scout_q0109_0101.htm";
			else if(cond == 2)
				htmltext = "corpse_of_scout_q0109_0203.htm";
		}
		else if(npcId == PIERCE)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 66 && npcId == PIERCE && (st.getQuestItemsCount(GOLDEN_BADGE_RECRUIT) > 0 || st.getQuestItemsCount(GOLDEN_BADGE_SOLDIER) > 0))
				{
					st.setCond(1);
					htmltext = "merc_cap_peace_q0109_0105.htm";
				}
				else
					htmltext = "merc_cap_peace_q0109_0103.htm";
			}
			if(cond == 1)
				htmltext = "merc_cap_peace_q0109_0304.htm";
			else if(cond == 2)
				htmltext = "merc_cap_peace_q0109_0201.htm";
			else if(cond == 3)
				htmltext = "merc_cap_peace_q0109_0303.htm";
		}
		else if(npcId == KAHMAN && cond == 3)
		{
			htmltext = "merc_kahmun_q0109_0401.htm";
			st.addExpAndSp(701500, 50000);
			st.giveItems(ADENA_ID, 161500, true, true);
			st.finishQuest();
		}
		return htmltext;
	}
}
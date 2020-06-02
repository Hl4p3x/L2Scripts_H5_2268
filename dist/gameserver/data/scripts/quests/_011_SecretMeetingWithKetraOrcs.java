package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _011_SecretMeetingWithKetraOrcs extends QuestScript
{
	int CADMON = 31296;
	int LEON = 31256;
	int WAHKAN = 31371;

	int MUNITIONS_BOX = 7231;

	public _011_SecretMeetingWithKetraOrcs()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(CADMON);

		addTalkId(LEON);
		addTalkId(WAHKAN);

		addQuestItem(MUNITIONS_BOX);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("guard_cadmon_q0011_0104.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("trader_leon_q0011_0201.htm"))
		{
			st.giveItems(MUNITIONS_BOX, 1, false, false);
			st.setCond(2);
		}
		else if(event.equalsIgnoreCase("herald_wakan_q0011_0301.htm"))
		{
			st.takeItems(MUNITIONS_BOX, 1);
			st.addExpAndSp(82045, 6047);
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
		if(npcId == CADMON)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 74)
					htmltext = "guard_cadmon_q0011_0101.htm";
				else
					htmltext = "guard_cadmon_q0011_0103.htm";
			}
			else if(cond == 1)
				htmltext = "guard_cadmon_q0011_0105.htm";
		}
		else if(npcId == LEON)
		{
			if(cond == 1)
				htmltext = "trader_leon_q0011_0101.htm";
			else if(cond == 2)
				htmltext = "trader_leon_q0011_0202.htm";
		}
		else if(npcId == WAHKAN)
			if(cond == 2 && st.getQuestItemsCount(MUNITIONS_BOX) > 0)
				htmltext = "herald_wakan_q0011_0201.htm";
		return htmltext;
	}
}

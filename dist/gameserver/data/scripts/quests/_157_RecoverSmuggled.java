package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _157_RecoverSmuggled extends QuestScript
{
	int ADAMANTITE_ORE_ID = 1024;
	int BUCKLER = 20;

	public _157_RecoverSmuggled()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(30005);

		addTalkId(30005);

		addKillId(20121);

		addQuestItem(ADAMANTITE_ORE_ID);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("1"))
		{
			st.set("id", "0");
			st.setCond(1);
			htmltext = "30005-05.htm";
		}
		else if(event.equals("157_1"))
		{
			htmltext = "30005-04.htm";
			return htmltext;
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = NO_QUEST_DIALOG;
		if(npcId == 30005 && st.getCond() == 0)
		{
			if(st.getPlayer().getLevel() >= 5)
				htmltext = "30005-03.htm";
			else
				htmltext = "30005-02.htm";
		}
		else if(npcId == 30005 && st.getCond() != 0 && st.getQuestItemsCount(ADAMANTITE_ORE_ID) < 20)
			htmltext = "30005-06.htm";
		else if(npcId == 30005 && st.getCond() != 0 && st.getQuestItemsCount(ADAMANTITE_ORE_ID) >= 20)
		{
			st.takeItems(ADAMANTITE_ORE_ID, st.getQuestItemsCount(ADAMANTITE_ORE_ID));
			st.giveItems(BUCKLER, 1, false, false);
			htmltext = "30005-07.htm";
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(npcId == 20121)
		{
			st.set("id", "0");
			if(st.getCond() != 0 && st.getQuestItemsCount(ADAMANTITE_ORE_ID) < 20 && Rnd.chance(14))
			{
				st.giveItems(ADAMANTITE_ORE_ID, 1, true, true);
				if(st.getQuestItemsCount(ADAMANTITE_ORE_ID) >= 20)
					st.playSound(SOUND_MIDDLE);
				else
					st.playSound(SOUND_ITEMGET);
			}
		}
		return null;
	}
}
package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _297_GateKeepersFavor extends QuestScript
{

	private static final int STARSTONE = 1573;
	private static final int GATEKEEPER_TOKEN = 1659;

	public _297_GateKeepersFavor()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(30540);
		addTalkId(30540);
		addKillId(20521);
		addQuestItem(STARSTONE);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("gatekeeper_wirphy_q0297_03.htm"))
		{
			st.setCond(1);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 30540)
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 15)
					htmltext = "gatekeeper_wirphy_q0297_02.htm";
				else
					htmltext = "gatekeeper_wirphy_q0297_01.htm";
			}
			else if(cond == 1 && st.getQuestItemsCount(STARSTONE) < 20)
				htmltext = "gatekeeper_wirphy_q0297_04.htm";
			else if(cond == 2 && st.getQuestItemsCount(STARSTONE) < 20)
				htmltext = "gatekeeper_wirphy_q0297_04.htm";
			else if(cond == 2 && st.getQuestItemsCount(STARSTONE) >= 20)
			{
				htmltext = "gatekeeper_wirphy_q0297_05.htm";
				st.takeItems(STARSTONE, -1);
				st.giveItems(GATEKEEPER_TOKEN, 2, false, false);
				st.finishQuest();
			}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		st.rollAndGive(STARSTONE, 1, 1, 20, 33);
		if(st.getQuestItemsCount(STARSTONE) >= 20)
			st.setCond(2);
		return null;
	}
}
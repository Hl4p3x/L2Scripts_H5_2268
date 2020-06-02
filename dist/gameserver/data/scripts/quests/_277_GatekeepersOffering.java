package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _277_GatekeepersOffering extends QuestScript
{

	private static final int STARSTONE1_ID = 1572;
	private static final int GATEKEEPER_CHARM_ID = 1658;

	public _277_GatekeepersOffering()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(30576);
		addKillId(20333);
		addQuestItem(STARSTONE1_ID);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("1"))
			if(st.getPlayer().getLevel() >= 15)
			{
				htmltext = "gatekeeper_tamil_q0277_03.htm";
				st.setCond(1);
			}
			else
				htmltext = "gatekeeper_tamil_q0277_01.htm";
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == 30576 && cond == 0)
			htmltext = "gatekeeper_tamil_q0277_02.htm";
		else if(npcId == 30576 && cond == 1 && st.getQuestItemsCount(STARSTONE1_ID) < 20)
			htmltext = "gatekeeper_tamil_q0277_04.htm";
		else if(npcId == 30576 && cond == 2 && st.getQuestItemsCount(STARSTONE1_ID) < 20)
			htmltext = "gatekeeper_tamil_q0277_04.htm";
		else if(npcId == 30576 && cond == 2 && st.getQuestItemsCount(STARSTONE1_ID) >= 20)
		{
			htmltext = "gatekeeper_tamil_q0277_05.htm";
			st.takeItems(STARSTONE1_ID, -1);
			st.giveItems(GATEKEEPER_CHARM_ID, 2, false, false);
			st.finishQuest();
		}

		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		st.rollAndGive(STARSTONE1_ID, 1, 1, 20, 33);
		if(st.getQuestItemsCount(STARSTONE1_ID) >= 20)
			st.setCond(2);
		return null;
	}
}
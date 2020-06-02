package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _651_RunawayYouth extends QuestScript
{
	//Npc
	private static int IVAN = 32014;
	private static int BATIDAE = 31989;
	protected NpcInstance _npc;

	//Items
	private static int SOE = 736;

	public _651_RunawayYouth()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(IVAN);
		addTalkId(BATIDAE);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("runaway_boy_ivan_q0651_03.htm"))
		{
			if(st.getQuestItemsCount(SOE) > 0)
			{
				st.setCond(1);
				st.takeItems(SOE, 1);
				htmltext = "runaway_boy_ivan_q0651_04.htm";
				//npc.broadcastPacket(MagicSkillUser(npc,npc,2013,1,20000,0));
				//Каст СОЕ и изчезновение НПЦ
				st.startQuestTimer("ivan_timer", 20000);
			}
		}
		else if(event.equalsIgnoreCase("runaway_boy_ivan_q0651_05.htm"))
		{
			st.finishQuest(SOUND_GIVEUP);
		}
		else if(event.equalsIgnoreCase("ivan_timer"))
		{
			_npc.deleteMe();
			htmltext = null;
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == IVAN && cond == 0)
		{
			if(st.getPlayer().getLevel() >= 26)
				htmltext = "runaway_boy_ivan_q0651_01.htm";
			else
				htmltext = "runaway_boy_ivan_q0651_01a.htm";
		}
		else if(npcId == BATIDAE && cond == 1)
		{
			htmltext = "fisher_batidae_q0651_01.htm";
			st.giveItems(ADENA_ID, 2883, true, true );
			st.finishQuest();
		}
		return htmltext;
	}
}

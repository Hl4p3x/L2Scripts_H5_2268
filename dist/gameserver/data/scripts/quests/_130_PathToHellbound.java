package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * User: Keiichi
 * Date: 05.10.2008
 * Time: 19:45:18
 * Info: Один из 2х квестов для прохода на остров Hellbound.
 * Info: Пройдя его ведьма Galate открывает ТП до локации (xyz = -11095, 236440, -3232)
 */
public class _130_PathToHellbound extends QuestScript
{
	// NPC's
	private static int CASIAN = 30612;
	private static int GALATE = 32292;
	// ITEMS
	private static int CASIAN_BLUE_CRY = 12823;

	public _130_PathToHellbound()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(CASIAN);
		addTalkId(GALATE);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int cond = st.getCond();
		String htmltext = event;

		if(event.equals("sage_kasian_q0130_05.htm") && cond == 0)
		{
			st.setCond(1);
		}

		if(event.equals("galate_q0130_03.htm") && cond == 1)
		{
			st.setCond(2);
		}

		if(event.equals("sage_kasian_q0130_08.htm") && cond == 2)
		{
			st.setCond(3);
			st.giveItems(CASIAN_BLUE_CRY, 1, false, false);
		}

		if(event.equals("galate_q0130_07.htm") && cond == 3)
		{
			st.takeItems(CASIAN_BLUE_CRY, -1);
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
		if(npcId == CASIAN)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 78)
					htmltext = "sage_kasian_q0130_01.htm";
				else
					htmltext = "sage_kasian_q0130_02.htm";
			}
			else if(cond == 2)
				htmltext = "sage_kasian_q0130_07.htm";
		}
		else if(npcId == GALATE)
		{
			if(cond == 1)
				htmltext = "galate_q0130_01.htm";
			else if(cond == 3)
				htmltext = "galate_q0130_05.htm";
		}
		return htmltext;
	}
}
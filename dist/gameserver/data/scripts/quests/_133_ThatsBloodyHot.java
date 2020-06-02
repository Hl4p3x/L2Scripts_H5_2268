package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * User: Keiichi
 * Date: 06.10.2008
 * Time: 11:31:36
 * Info: Один из 2х квестов для прохода на остров Hellbound.
 * Info: Пройдя его ведьма Galate открывает ТП до Beleth's stronghold on Hellbound Island
 */
public class _133_ThatsBloodyHot extends QuestScript
{
	// NPC's
	private static int KANIS = 32264;
	private static int GALATE = 32292;
	// ITEMS
	private static int CRYSTAL_SAMPLE = 9785;

	public _133_ThatsBloodyHot()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(KANIS);
		addTalkId(KANIS);
		addTalkId(GALATE);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int cond = st.getCond();
		String htmltext = event;

		if(event.equals("priest_kanis_q0133_04.htm") && cond == 0)
		{
			st.setCond(1);
		}

		if(event.equals("priest_kanis_q0133_12.htm") && cond == 1)
		{
			st.setCond(2);
			st.giveItems(CRYSTAL_SAMPLE, 1, false, false);
		}

		if(event.equals("Galate_q0133_06.htm") && cond == 2)
		{
			st.takeItems(CRYSTAL_SAMPLE, -1);
			st.giveItems(ADENA_ID, 254247, true, true);
			st.addExpAndSp(331457, 32524);
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
		if(npcId == KANIS)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 78 && st.getPlayer().isQuestCompleted(131))
					htmltext = "priest_kanis_q0133_01.htm";
				else
					htmltext = "priest_kanis_q0133_03.htm";
			}
		}
		else if(npcId == GALATE)
		{
			if(cond == 2)
				htmltext = "Galate_q0133_02.htm";
		}
		return htmltext;
	}
}

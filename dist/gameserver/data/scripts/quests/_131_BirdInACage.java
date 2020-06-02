package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * @author Diamond
 */
public class _131_BirdInACage extends QuestScript
{
	// NPC's
	private static int KANIS = 32264;
	private static int PARME = 32271;
	// ITEMS
	private static int KANIS_ECHO_CRY = 9783;
	private static int PARMES_LETTER = 9784;

	public _131_BirdInACage()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(KANIS);
		addTalkId(PARME);

		addQuestItem(KANIS_ECHO_CRY);
		addQuestItem(PARMES_LETTER);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int cond = st.getCond();
		String htmltext = event;

		if(event.equals("priest_kanis_q0131_04.htm") && cond == 0)
		{
			st.setCond(1);
		}
		else if(event.equals("priest_kanis_q0131_12.htm") && cond == 1)
		{
			st.setCond(2);
			st.giveItems(KANIS_ECHO_CRY, 1, false, false);
		}
		else if(event.equals("parme_131y_q0131_04.htm") && cond == 2)
		{
			st.setCond(3);
			st.giveItems(PARMES_LETTER, 1, false, false);
			st.getPlayer().teleToLocation(143472 + Rnd.get(-100, 100), 191040 + Rnd.get(-100, 100), -3696, ReflectionManager.DEFAULT);
		}
		else if(event.equals("priest_kanis_q0131_17.htm") && cond == 3)
		{
			st.playSound(SOUND_MIDDLE);
			st.takeItems(PARMES_LETTER, -1);
		}
		else if(event.equals("priest_kanis_q0131_19.htm") && cond == 3)
		{
			st.takeItems(KANIS_ECHO_CRY, -1);
			st.addExpAndSp(250677, 25019);
			st.finishQuest();
		}
		else if(event.equals("meet") && cond == 2)
			st.getPlayer().teleToLocation(153736, 142056, -9744);

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
				if(st.getPlayer().getLevel() >= 78)
					htmltext = "priest_kanis_q0131_01.htm";
				else
					htmltext = "priest_kanis_q0131_02.htm";
			}
			else if(cond == 1)
				htmltext = "priest_kanis_q0131_05.htm";
			else if(cond == 2)
				htmltext = "priest_kanis_q0131_13.htm";
			else if(cond == 3)
				if(st.getQuestItemsCount(PARMES_LETTER) > 0)
					htmltext = "priest_kanis_q0131_16.htm";
				else
					htmltext = "priest_kanis_q0131_17.htm";
		}
		else if(npcId == PARME && cond == 2)
			htmltext = "parme_131y_q0131_02.htm";

		return htmltext;
	}
}
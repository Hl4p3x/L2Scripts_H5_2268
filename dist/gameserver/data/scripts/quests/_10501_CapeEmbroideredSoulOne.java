package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _10501_CapeEmbroideredSoulOne extends QuestScript
{
	// NPC's
	private static final int OLF_ADAMS = 32612;
	// Mob's
	private static final int ZAKEN_HIGH = 29181;
	// Quest Item's
	private static final int SOUL_ZAKEN = 21722;
	// Item's
	private static final int CLOAK_OF_ZAKEN = 21719;

	public _10501_CapeEmbroideredSoulOne()
	{
		super(PARTY_ALL, ONETIME);
		addStartNpc(OLF_ADAMS);
		addTalkId(OLF_ADAMS);
		addKillId(ZAKEN_HIGH);
		addQuestItem(SOUL_ZAKEN);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("olf_adams_q10501_02.htm"))
		{
			st.setCond(1);
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 78)
				htmltext = "olf_adams_q10501_01.htm";
			else
				htmltext = "olf_adams_q10501_00.htm";
		}
		else if(cond == 1)
			htmltext = "olf_adams_q10501_03.htm";
		else if(cond == 2)
			if(st.getQuestItemsCount(SOUL_ZAKEN) < 20)
			{
				st.setCond(1);
				htmltext = "olf_adams_q10501_03.htm";
			}
			else
			{
				st.takeItems(SOUL_ZAKEN, -1);
				st.giveItems(CLOAK_OF_ZAKEN, 1, false, false);
				htmltext = "olf_adams_q10501_04.htm";
				st.finishQuest();
			}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 1 && npcId == ZAKEN_HIGH)
		{
			if(st.getQuestItemsCount(SOUL_ZAKEN) < 20)
				st.giveItems(SOUL_ZAKEN, Rnd.get(1,2), false, false);
			if(st.getQuestItemsCount(SOUL_ZAKEN) >= 20)
			{
				st.setCond(2);
			}
		}
		return null;
	}
}
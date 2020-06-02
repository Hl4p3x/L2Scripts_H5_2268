package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _10503_CapeEmbroideredSoulThree extends QuestScript
{
	// NPC's
	private static final int OLF_ADAMS = 32612;
	// Mob's
	private static final int FRINTEZZA = 29047;
	// Quest Item's
	private static final int SOUL_FRINTEZZA = 21724;
	// Item's
	private static final int CLOAK_FRINTEZZA = 21721;

	public _10503_CapeEmbroideredSoulThree()
	{
		super(PARTY_ALL, ONETIME);
		addStartNpc(OLF_ADAMS);
		addTalkId(OLF_ADAMS);
		addKillId(FRINTEZZA);
		addQuestItem(SOUL_FRINTEZZA);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("olf_adams_q10503_02.htm"))
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
			if(st.getPlayer().getLevel() >= 80)
				htmltext = "olf_adams_q10503_01.htm";
			else
				htmltext = "olf_adams_q10503_00.htm";
		}
		else if(cond == 1)
			htmltext = "olf_adams_q10503_03.htm";
		else if(cond == 2)
			if(st.getQuestItemsCount(SOUL_FRINTEZZA) < 20)
			{
				st.setCond(1);
				htmltext = "olf_adams_q10503_03.htm";
			}
			else
			{
				st.takeItems(SOUL_FRINTEZZA, -1);
				st.giveItems(CLOAK_FRINTEZZA, 1, false, false);
				htmltext = "olf_adams_q10503_04.htm";
				st.finishQuest();
			}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 1 && npcId == FRINTEZZA)
		{
			if(st.getQuestItemsCount(SOUL_FRINTEZZA) < 20)
				st.giveItems(SOUL_FRINTEZZA, Rnd.get(1, 2), false, false);
			if(st.getQuestItemsCount(SOUL_FRINTEZZA) >= 20)
			{
				st.setCond(2);
			}
		}
		return null;
	}
}
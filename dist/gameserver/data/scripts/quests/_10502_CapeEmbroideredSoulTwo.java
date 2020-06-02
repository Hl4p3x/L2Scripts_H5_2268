package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _10502_CapeEmbroideredSoulTwo extends QuestScript
{
	// NPC's
	private static final int OLF_ADAMS = 32612;
	// Mob's
	private static final int FREYA_NORMAL = 29179;
	private static final int FREYA_HARD = 29180;
	// Quest Item's
	private static final int SOUL_FREYA = 21723;
	// Item's
	private static final int CLOAK_FREYA = 21720;

	public _10502_CapeEmbroideredSoulTwo()
	{
		super(PARTY_ALL, ONETIME);
		addStartNpc(OLF_ADAMS);
		addTalkId(OLF_ADAMS);
		addKillId(FREYA_NORMAL, FREYA_HARD);
		addQuestItem(SOUL_FREYA);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("olf_adams_q10502_02.htm"))
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
			if(st.getPlayer().getLevel() >= 82)
				htmltext = "olf_adams_q10502_01.htm";
			else
				htmltext = "olf_adams_q10502_00.htm";
		}
		else if(cond == 1)
			htmltext = "olf_adams_q10502_03.htm";
		else if(cond == 2)
			if(st.getQuestItemsCount(SOUL_FREYA) < 20)
			{
				st.setCond(1);
				htmltext = "olf_adams_q10502_03.htm";
			}
			else
			{
				st.takeItems(SOUL_FREYA, -1);
				st.giveItems(CLOAK_FREYA, 1, false, false);
				htmltext = "olf_adams_q10502_04.htm";
				st.finishQuest();
			}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 1 && (npcId == FREYA_NORMAL || npcId == FREYA_HARD))
		{
			if(st.getQuestItemsCount(SOUL_FREYA) < 20)
				st.giveItems(SOUL_FREYA, Rnd.get(1, 2), false, false);
			if(st.getQuestItemsCount(SOUL_FREYA) >= 20)
			{
				st.setCond(2);
			}
		}
		return null;
	}
}
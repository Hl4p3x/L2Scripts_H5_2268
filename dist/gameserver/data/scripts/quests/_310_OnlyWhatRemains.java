package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * @author VAVAN
 * @corrected n0nam3
 */

public class _310_OnlyWhatRemains extends QuestScript
{

	// NPC's
	private static final int KINTAIJIN = 32640;
	// MOBS's
	private static final int[] MOBS = {
			22617,
			22624,
			22625,
			22626
	};
	// ITEMS's
	private static final int DIRTYBEAD = 14880;
	private static final int ACCELERATOR = 14832;
	private static final int JEWEL = 14835;

	public _310_OnlyWhatRemains()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(KINTAIJIN);
		addKillId(MOBS);
		addQuestItem(DIRTYBEAD);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("32640-3.htm"))
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
		if(npcId == KINTAIJIN)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 81 && st.getPlayer().isQuestCompleted(240))
					htmltext = "32640-1.htm";
				else
					htmltext = "32640-0.htm";
			}
			else if(cond == 1)
				htmltext = "32640-8.htm";
			else if(cond == 2)
			{
				st.takeItems(DIRTYBEAD, 500);
				st.giveItems(ACCELERATOR, 1, false, false);
				st.giveItems(JEWEL, 1, false, false);
				st.finishQuest();
				htmltext = "32640-9.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onCompleted(NpcInstance npc, QuestState st)
	{
		String htmltext = COMPLETED_DIALOG;
		int npcId = npc.getNpcId();
		if(npcId == KINTAIJIN)
			htmltext = "32640-10.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1)
		{
			st.giveItems(DIRTYBEAD, 1, true, true);
			if(st.getQuestItemsCount(DIRTYBEAD) >= 500)
			{
				st.setCond(2);
			}
			else
				st.playSound(SOUND_ITEMGET);
		}
		return null;
	}
}
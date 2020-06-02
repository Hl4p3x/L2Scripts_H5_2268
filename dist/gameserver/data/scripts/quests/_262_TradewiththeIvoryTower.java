package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _262_TradewiththeIvoryTower extends QuestScript
{
	//NPC
	public final int VOLODOS = 30137;

	//MOB
	public final int GREEN_FUNGUS = 20007;
	public final int BLOOD_FUNGUS = 20400;

	public final int FUNGUS_SAC = 707;

	public _262_TradewiththeIvoryTower()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(VOLODOS);
		addKillId(new int[]{
				BLOOD_FUNGUS,
				GREEN_FUNGUS
		});
		addQuestItem(new int[]{FUNGUS_SAC});
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("vollodos_q0262_03.htm"))
		{
			st.setCond(1);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 8)
			{
				htmltext = "vollodos_q0262_02.htm";
				return htmltext;
			}
			htmltext = "vollodos_q0262_01.htm";
		}
		else if(cond == 1 && st.getQuestItemsCount(FUNGUS_SAC) < 10)
			htmltext = "vollodos_q0262_04.htm";
		else if(cond == 2 && st.getQuestItemsCount(FUNGUS_SAC) >= 10)
		{
			st.giveItems(ADENA_ID, 3000, true, true);
			st.takeItems(FUNGUS_SAC, -1);
			st.finishQuest();
			htmltext = "vollodos_q0262_05.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int random = Rnd.get(10);
		if(st.getCond() == 1 && st.getQuestItemsCount(FUNGUS_SAC) < 10)
			if(npcId == GREEN_FUNGUS && random < 3 || npcId == BLOOD_FUNGUS && random < 4)
			{
				st.giveItems(FUNGUS_SAC, 1, true, true);
				if(st.getQuestItemsCount(FUNGUS_SAC) >= 10)
				{
					st.setCond(2);
				}
				else
					st.playSound(SOUND_ITEMGET);
			}
		return null;
	}
}
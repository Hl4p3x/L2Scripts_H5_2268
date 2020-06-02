package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _268_TracesOfEvil extends QuestScript
{
	//NPC
	public final int KUNAI = 30559;
	//MOBS
	public final int SPIDER = 20474;
	public final int FANG_SPIDER = 20476;
	public final int BLADE_SPIDER = 20478;
	//ITEMS
	public final int CONTAMINATED = 10869;

	public _268_TracesOfEvil()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(KUNAI);
		addKillId(SPIDER, FANG_SPIDER, BLADE_SPIDER);
		addQuestItem(CONTAMINATED);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("trader_kunai_q0268_03.htm"))
		{
			st.setCond(1);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		if(st.getCond() == 0)
		{
			if(st.getPlayer().getLevel() < 15)
				htmltext = "trader_kunai_q0268_02.htm";
			else
				htmltext = "trader_kunai_q0268_01.htm";
		}
		else if(st.getQuestItemsCount(CONTAMINATED) >= 30)
		{
			htmltext = "trader_kunai_q0268_06.htm";
			st.giveItems(ADENA_ID, 2474, true, true);
			st.addExpAndSp(8738, 409);
			st.finishQuest();
		}
		else
			htmltext = "trader_kunai_q0268_04.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		st.giveItems(CONTAMINATED, 1, true, true);
		if(st.getQuestItemsCount(CONTAMINATED) <= 29)
			st.playSound(SOUND_ITEMGET);
		else if(st.getQuestItemsCount(CONTAMINATED) >= 30)
		{
			st.setCond(2);
		}
		return null;
	}
}
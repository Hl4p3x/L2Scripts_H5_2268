package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _368_TrespassingIntoTheSacredArea extends QuestScript
{
	//NPCs
	private static int RESTINA = 30926;
	//Items
	private static int BLADE_STAKATO_FANG = 5881;
	//Chances
	private static int BLADE_STAKATO_FANG_BASECHANCE = 10;

	public _368_TrespassingIntoTheSacredArea()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(RESTINA);
		for(int Blade_Stakato_id = 20794; Blade_Stakato_id <= 20797; Blade_Stakato_id++)
			addKillId(Blade_Stakato_id);
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		if(npc.getNpcId() != RESTINA)
			return htmltext;

		if(st.isNotAccepted())
		{
			if(st.getPlayer().getLevel() < 36)
				htmltext = "30926-00.htm";
			else
				htmltext = "30926-01.htm";
		}
		else
		{
			long _count = st.getQuestItemsCount(BLADE_STAKATO_FANG);
			if(_count > 0)
			{
				htmltext = "30926-04.htm";
				st.takeItems(BLADE_STAKATO_FANG, -1);
				st.giveItems(ADENA_ID, _count * 2250, true, true);
				st.playSound(SOUND_MIDDLE);
			}
			else
				htmltext = "30926-03.htm";
		}
		return htmltext;
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("30926-02.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("30926-05.htm"))
		{
			st.finishQuest();
		}
		return event;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(!qs.isStarted())
			return null;

		if(Rnd.chance(npc.getNpcId() - 20794 + BLADE_STAKATO_FANG_BASECHANCE))
		{
			qs.giveItems(BLADE_STAKATO_FANG, 1, true, true);
			qs.playSound(SOUND_ITEMGET);
		}
		return null;
	}
}

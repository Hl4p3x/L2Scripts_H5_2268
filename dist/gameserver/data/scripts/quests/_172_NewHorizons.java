package quests;

import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _172_NewHorizons extends QuestScript
{
	//NPC
	private static final int Zenya = 32140;
	private static final int Ragara = 32163;
	//Items
	private static final int ScrollOfEscapeGiran = 7126;
	private static final int MarkOfTraveler = 7570;

	public _172_NewHorizons()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(Zenya);

		addTalkId(Zenya);
		addTalkId(Ragara);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("subelder_zenya_q0172_04.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("gatekeeper_ragara_q0172_02.htm"))
		{
			st.giveItems(ScrollOfEscapeGiran, 1, false, false);
			st.giveItems(MarkOfTraveler, 1, false, false);
			st.unset("cond");
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
		if(npcId == Zenya)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() != Race.KAMAEL)
					htmltext = "subelder_zenya_q0172_03.htm";
				else if(st.getPlayer().getLevel() >= 3)
					htmltext = "subelder_zenya_q0172_01.htm";
				else
					htmltext = "subelder_zenya_q0172_02.htm";
			}
		}
		else if(npcId == Ragara)
			if(cond == 1)
				htmltext = "gatekeeper_ragara_q0172_01.htm";
		return htmltext;
	}
}
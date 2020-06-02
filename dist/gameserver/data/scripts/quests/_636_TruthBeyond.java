package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _636_TruthBeyond extends QuestScript
{

	//Npc
	public final int ELIYAH = 31329;
	public final int FLAURON = 32010;

	//Items
	public final int MARK = 8067;
	public final int VISITORSMARK = 8064;

	public _636_TruthBeyond()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(ELIYAH);
		addTalkId(FLAURON);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equals("priest_eliyah_q0636_05.htm"))
		{
			st.setCond(1);
		}
		else if(event.equals("falsepriest_flauron_q0636_02.htm"))
		{
			st.giveItems(VISITORSMARK, 1, false, false);
			st.finishQuest();
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(npcId == ELIYAH && cond == 0)
		{
			if(st.getQuestItemsCount(VISITORSMARK) == 0 && st.getQuestItemsCount(MARK) == 0)
			{
				if(st.getPlayer().getLevel() > 72)
					htmltext = "priest_eliyah_q0636_01.htm";
				else
					htmltext = "priest_eliyah_q0636_03.htm";
			}
			else
				htmltext = "priest_eliyah_q0636_06.htm";
		}
		else if(npcId == FLAURON)
			if(cond == 1)
			{
				htmltext = "falsepriest_flauron_q0636_01.htm";
				st.setCond(2);
			}
			else
				htmltext = "falsepriest_flauron_q0636_03.htm";
		return htmltext;
	}
}
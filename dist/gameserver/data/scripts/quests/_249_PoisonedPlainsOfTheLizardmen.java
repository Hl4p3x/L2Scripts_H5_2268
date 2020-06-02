package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _249_PoisonedPlainsOfTheLizardmen extends QuestScript
{

	private static final int MOUEN = 30196;
	private static final int JOHNNY = 32744;

	public _249_PoisonedPlainsOfTheLizardmen()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(MOUEN);
		addTalkId(MOUEN);
		addTalkId(JOHNNY);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;

		if(npc.getNpcId() == MOUEN)
		{
			if(event.equalsIgnoreCase("30196-03.htm"))
			{
				st.setCond(1);
			}
		}
		else if(npc.getNpcId() == JOHNNY && event.equalsIgnoreCase("32744-03.htm"))
		{
			st.unset("cond");
			st.giveItems(57, 83056, true, true);
			st.addExpAndSp(477496, 58743);
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
		if(npcId == MOUEN)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 82)
					htmltext = "30196-01.htm";
				else
					htmltext = "30196-00.htm";
			}
			else if(cond == 1)
				htmltext = "30196-04.htm";
		}
		else if(npcId == JOHNNY)
		{
			if(cond == 1)
				htmltext = "32744-01.htm";
		}
		return htmltext;
	}

	@Override
	public String onCompleted(NpcInstance npc, QuestState st)
	{
		String htmltext = COMPLETED_DIALOG;
		int npcId = npc.getNpcId();
		if(npcId == MOUEN)
			htmltext = "30196-05.htm";
		else if(npcId == JOHNNY)
			htmltext = "32744-04.htm";
		return htmltext;
	}
}
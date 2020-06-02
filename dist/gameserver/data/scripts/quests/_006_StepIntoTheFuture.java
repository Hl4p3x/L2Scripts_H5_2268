package quests;

import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * Рейты не учитываются, награда специфичная
 */
public class _006_StepIntoTheFuture extends QuestScript
{
	//NPC
	private static final int Roxxy = 30006;
	private static final int Baulro = 30033;
	private static final int Windawood = 30311;
	//Quest Item
	private static final int BaulrosLetter = 7571;
	//Items
	private static final int ScrollOfEscapeGiran = 7126;
	private static final int MarkOfTraveler = 7570;

	public _006_StepIntoTheFuture()
	{
		super(PARTY_NONE, ONETIME);
		addStartNpc(Roxxy);

		addTalkId(Baulro);
		addTalkId(Windawood);

		addQuestItem(BaulrosLetter);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("rapunzel_q0006_0104.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("baul_q0006_0201.htm"))
		{
			st.giveItems(BaulrosLetter, 1, false, false);
			st.setCond(2);
		}
		else if(event.equalsIgnoreCase("sir_collin_windawood_q0006_0301.htm"))
		{
			st.takeItems(BaulrosLetter, -1);
			st.setCond(3);
		}
		else if(event.equalsIgnoreCase("rapunzel_q0006_0401.htm"))
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
		if(npcId == Roxxy)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() == Race.HUMAN && st.getPlayer().getLevel() >= 3)
					htmltext = "rapunzel_q0006_0101.htm";
				else
					htmltext = "rapunzel_q0006_0102.htm";
			}
			else if(cond == 1)
				htmltext = "rapunzel_q0006_0105.htm";
			else if(cond == 3)
				htmltext = "rapunzel_q0006_0301.htm";
		}
		else if(npcId == Baulro)
		{
			if(cond == 1)
				htmltext = "baul_q0006_0101.htm";
			else if(cond == 2 && st.getQuestItemsCount(BaulrosLetter) > 0)
				htmltext = "baul_q0006_0202.htm";
		}
		else if(npcId == Windawood)
			if(cond == 2 && st.getQuestItemsCount(BaulrosLetter) > 0)
				htmltext = "sir_collin_windawood_q0006_0201.htm";
			else if(cond == 2 && st.getQuestItemsCount(BaulrosLetter) == 0)
				htmltext = "sir_collin_windawood_q0006_0302.htm";
			else if(cond == 3)
				htmltext = "sir_collin_windawood_q0006_0303.htm";
		return htmltext;
	}
}

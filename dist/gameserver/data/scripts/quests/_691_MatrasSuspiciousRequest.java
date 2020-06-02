package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _691_MatrasSuspiciousRequest extends QuestScript
{
	// NPC
	private final static int MATRAS = 32245;
	private final static int LABYRINTH_CAPTAIN = 22368;

	// Items
	private final static int RED_STONE = 10372;
	private final static int RED_STONES_COUNT = 744;
	private final static int DYNASTIC_ESSENCE_II = 10413;

	public _691_MatrasSuspiciousRequest()
	{
		super(PARTY_ONE, REPEATABLE);

		addStartNpc(MATRAS);
		addQuestItem(RED_STONE);
		addKillId(new int[]{
				22363,
				22364,
				22365,
				22366,
				22367,
				LABYRINTH_CAPTAIN,
				22369,
				22370,
				22371,
				22372
		});
	}

	@Override
	public String onEvent(String event, QuestState qs, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("32245-03.htm"))
		{
			qs.setCond(1);
		}
		else if(event.equalsIgnoreCase("32245-05.htm"))
		{
			qs.takeItems(RED_STONE, RED_STONES_COUNT);
			qs.giveItems(DYNASTIC_ESSENCE_II, 1, false, false);
			qs.finishQuest();
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
			if(st.getPlayer().getLevel() >= 76)
				htmltext = "32245-01.htm";
			else
				htmltext = "32245-00.htm";
		}
		else if(st.getQuestItemsCount(RED_STONE) < RED_STONES_COUNT)
			htmltext = "32245-03.htm";
		else
			htmltext = "32245-04.htm";

		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		st.rollAndGive(RED_STONE, 1, 1, RED_STONES_COUNT, npc.getNpcId() == LABYRINTH_CAPTAIN ? 50 : 30);
		return null;
	}
}
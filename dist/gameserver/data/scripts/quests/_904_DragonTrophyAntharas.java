package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * @author pchayka
 *         Repeatable
 */
public class _904_DragonTrophyAntharas extends QuestScript
{
	private static final int Theodric = 30755;
	private static final int AntharasMin = 29066;
	private static final int AntharasAvar = 29067;
	private static final int AntharasMax = 29068;
	private static final int MedalofGlory = 21874;

	public _904_DragonTrophyAntharas()
	{
		super(PARTY_ALL, REPEATABLE);
		addStartNpc(Theodric);
		addKillId(AntharasMin);
		addKillId(AntharasAvar);
		addKillId(AntharasMax);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("theodric_q904_04.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("theodric_q904_07.htm"))
		{
			st.giveItems(MedalofGlory, 30, false, false);
			st.finishQuest();
		}

		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(npc.getNpcId() == Theodric)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 84)
				{
					if(st.getQuestItemsCount(3865) > 0)
						htmltext = "theodric_q904_01.htm";
					else
						htmltext = "theodric_q904_00b.htm";
				}
				else
					htmltext = "theodric_q904_00.htm";
			}
			else if(cond == 1)
				htmltext = "theodric_q904_05.htm";
			else if(cond == 2)
				htmltext = "theodric_q904_06.htm";
		}

		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 1)
		{
			if(npc.getNpcId() == AntharasMax || npc.getNpcId() == AntharasAvar || npc.getNpcId() == AntharasMin)
				st.setCond(2);
		}
		return null;
	}
}
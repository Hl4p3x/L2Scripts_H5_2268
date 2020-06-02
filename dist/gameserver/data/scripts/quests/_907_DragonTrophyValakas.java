package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * @author pchayka
 *         Repeatable
 */
public class _907_DragonTrophyValakas extends QuestScript
{
	private static final int Klein = 31540;
	private static final int Valakas = 29028;
	private static final int MedalofGlory = 21874;

	public _907_DragonTrophyValakas()
	{
		super(PARTY_ALL, REPEATABLE);
		addStartNpc(Klein);
		addKillId(Valakas);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("klein_q907_04.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("klein_q907_07.htm"))
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
		if(npc.getNpcId() == Klein)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 84)
				{
					if(st.getQuestItemsCount(7267) > 0)
						htmltext = "klein_q907_01.htm";
					else
						htmltext = "klein_q907_00b.htm";
				}
				else
					htmltext = "klein_q907_00.htm";
			}
			else if(cond == 1)
				htmltext = "klein_q907_05.htm";
			else if(cond == 2)
				htmltext = "klein_q907_06.htm";
		}

		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 1)
		{
			if(npc.getNpcId() == Valakas)
				st.setCond(2);
		}
		return null;
	}
}
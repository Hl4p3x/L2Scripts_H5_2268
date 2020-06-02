package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _637_ThroughOnceMore extends QuestScript
{

	//Drop rate
	public final int CHANCE = 40;

	//Npc
	public final int FLAURON = 32010;

	//Items
	public final int VISITORSMARK = 8064;
	public final int NECROHEART = 8066;
	public final int MARK = 8067;

	public _637_ThroughOnceMore()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(FLAURON);

		addKillId(21565, 21566, 21567, 21568);

		addQuestItem(NECROHEART);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equals("falsepriest_flauron_q0637_04.htm"))
		{
			st.setCond(1);
			st.takeItems(VISITORSMARK, 1);
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext;
		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() > 72 && st.getQuestItemsCount(VISITORSMARK) > 0 && st.getQuestItemsCount(MARK) == 0)
				htmltext = "falsepriest_flauron_q0637_02.htm";
			else
				htmltext = "falsepriest_flauron_q0637_01.htm";
		}
		else if(cond == 2 && st.getQuestItemsCount(NECROHEART) >= 10)
		{
			htmltext = "falsepriest_flauron_q0637_05.htm";
			st.takeItems(NECROHEART, 10);
			st.giveItems(MARK, 1, false, false);
			st.finishQuest();
		}
		else
			htmltext = "falsepriest_flauron_q0637_04.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		long count = st.getQuestItemsCount(NECROHEART);
		if(st.getCond() == 1 && Rnd.chance(CHANCE) && count < 10)
		{
			st.giveItems(NECROHEART, 1, true, true);
			if(st.getQuestItemsCount(NECROHEART) > 9)
			{
				st.setCond(2);
			}
			else
				st.playSound(SOUND_ITEMGET);
		}
		return null;
	}

	@Override
	public void onAbort(QuestState st)
	{
		if(st.getQuestItemsCount(VISITORSMARK) == 0)
			st.giveItems(VISITORSMARK, 1, false, false);
	}
}
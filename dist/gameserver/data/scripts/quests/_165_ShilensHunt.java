package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _165_ShilensHunt extends QuestScript
{
	private static final int DARK_BEZOAR = 1160;
	private static final int LESSER_HEALING_POTION = 1060;

	public _165_ShilensHunt()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(30348);

		addTalkId(30348);

		addKillId(20456);
		addKillId(20529);
		addKillId(20532);
		addKillId(20536);

		addQuestItem(DARK_BEZOAR);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("1"))
		{
			st.setCond(1);
			htmltext = "30348-03.htm";
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();

		if(cond == 0)
		{
			if(st.getPlayer().getRace() != Race.DARKELF)
				htmltext = "30348-00.htm";
			else if(st.getPlayer().getLevel() >= 3)
				htmltext = "30348-02.htm";
			else
				htmltext = "30348-01.htm";
		}
		else if(cond == 1 || st.getQuestItemsCount(DARK_BEZOAR) < 13)
			htmltext = "30348-04.htm";
		else if(cond == 2)
		{
			htmltext = "30348-05.htm";
			st.takeItems(DARK_BEZOAR, -1);
			st.giveItems(LESSER_HEALING_POTION, 5, false, false);
			st.addExpAndSp(1000, 0);
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 1 && st.getQuestItemsCount(DARK_BEZOAR) < 13 && Rnd.chance(90))
		{
			st.giveItems(DARK_BEZOAR, 1, true, true);
			if(st.getQuestItemsCount(DARK_BEZOAR) >= 13)
			{
				st.setCond(2);
			}
			else
				st.playSound(SOUND_ITEMGET);
		}
		return null;
	}
}
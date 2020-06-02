package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _295_DreamsOfTheSkies extends QuestScript
{
	public static int FLOATING_STONE = 1492;
	public static int RING_OF_FIREFLY = 1509;

	public static int Arin = 30536;
	public static int MagicalWeaver = 20153;

	public _295_DreamsOfTheSkies()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(Arin);
		addTalkId(Arin);
		addKillId(MagicalWeaver);

		addQuestItem(FLOATING_STONE);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("elder_arin_q0295_03.htm"))
		{
			st.setCond(1);
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
			if(st.getPlayer().getLevel() >= 11)
				htmltext = "elder_arin_q0295_02.htm";
			else
				htmltext = "elder_arin_q0295_01.htm";
		}
		else if(cond == 1 || st.getQuestItemsCount(FLOATING_STONE) < 50)
			htmltext = "elder_arin_q0295_04.htm";
		else if(cond == 2 && st.getQuestItemsCount(FLOATING_STONE) >= 50)
		{
			st.addExpAndSp(0, 500);
			st.finishQuest();
			if(st.getQuestItemsCount(RING_OF_FIREFLY) < 1)
			{
				htmltext = "elder_arin_q0295_05.htm";
				st.giveItems(RING_OF_FIREFLY, 1, false, false);
			}
			else
			{
				htmltext = "elder_arin_q0295_06.htm";
				st.giveItems(ADENA_ID, 2400, true, true);
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1 && st.getQuestItemsCount(FLOATING_STONE) < 50)
			if(Rnd.chance(25))
			{
				st.giveItems(FLOATING_STONE, 1, true, true);
				if(st.getQuestItemsCount(FLOATING_STONE) >= 50)
				{
					st.setCond(2);
				}
				else
					st.playSound(SOUND_ITEMGET);
			}
			else if(st.getQuestItemsCount(FLOATING_STONE) >= 48)
			{
				st.giveItems(FLOATING_STONE, 50 - st.getQuestItemsCount(FLOATING_STONE), true, true);
				st.setCond(2);
			}
			else
			{
				st.giveItems(FLOATING_STONE, 2, true, true);
				st.playSound(SOUND_ITEMGET);
			}
		return null;
	}
}
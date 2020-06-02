package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _329_CuriosityOfDwarf extends QuestScript
{
	private int GOLEM_HEARTSTONE = 1346;
	private int BROKEN_HEARTSTONE = 1365;

	public _329_CuriosityOfDwarf()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(30437);
		addKillId(20083);
		addKillId(20085);

		addQuestItem(BROKEN_HEARTSTONE);
		addQuestItem(GOLEM_HEARTSTONE);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("trader_rolento_q0329_03.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("trader_rolento_q0329_06.htm"))
		{
			st.finishQuest();
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext;
		if(st.getCond() == 0)
		{
			if(st.getPlayer().getLevel() >= 33)
				htmltext = "trader_rolento_q0329_02.htm";
			else
				htmltext = "trader_rolento_q0329_01.htm";
		}
		else
		{
			long heart = st.getQuestItemsCount(GOLEM_HEARTSTONE);
			long broken = st.getQuestItemsCount(BROKEN_HEARTSTONE);
			if(broken + heart > 0)
			{
				st.giveItems(ADENA_ID, (50 * broken) + (1000 * heart), true, true);
				st.takeItems(BROKEN_HEARTSTONE, -1);
				st.takeItems(GOLEM_HEARTSTONE, -1);
				htmltext = "trader_rolento_q0329_05.htm";
			}
			else
				htmltext = "trader_rolento_q0329_04.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int n = Rnd.get(1, 100);
		if(npcId == 20085)
		{
			if(n < 5)
			{
				st.giveItems(GOLEM_HEARTSTONE, 1, true, true);
				st.playSound(SOUND_ITEMGET);
			}
			else if(n < 58)
			{
				st.giveItems(BROKEN_HEARTSTONE, 1, true, true);
				st.playSound(SOUND_ITEMGET);
			}
		}
		else if(npcId == 20083)
			if(n < 6)
			{
				st.giveItems(GOLEM_HEARTSTONE, 1, true, true);
				st.playSound(SOUND_ITEMGET);
			}
			else if(n < 56)
			{
				st.giveItems(BROKEN_HEARTSTONE, 1, true, true);
				st.playSound(SOUND_ITEMGET);
			}
		return null;
	}
}
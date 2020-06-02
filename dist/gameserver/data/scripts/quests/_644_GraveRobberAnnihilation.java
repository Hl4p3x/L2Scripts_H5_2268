package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _644_GraveRobberAnnihilation extends QuestScript
{
	//NPC
	private static final int KARUDA = 32017;
	//QuestItem
	private static int ORC_GOODS = 8088;

	public _644_GraveRobberAnnihilation()
	{
		super(PARTY_ONE, REPEATABLE);
		addStartNpc(KARUDA);

		addKillId(22003);
		addKillId(22004);
		addKillId(22005);
		addKillId(22006);
		addKillId(22008);

		addQuestItem(ORC_GOODS);

	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("karuda_q0644_0103.htm"))
		{
			st.takeItems(ORC_GOODS, -1);
			if(st.getPlayer().getLevel() < 20)
				htmltext = "karuda_q0644_0102.htm";
			else
			{
				st.setCond(1);
			}
		}
		if(st.getCond() == 2 && st.getQuestItemsCount(ORC_GOODS) >= 120)
		{
			if(event.equalsIgnoreCase("varn"))
			{
				st.takeItems(ORC_GOODS, -1);
				st.giveItems(1865, 30, false, false);
				htmltext = null;
			}
			else if(event.equalsIgnoreCase("an_s"))
			{
				st.takeItems(ORC_GOODS, -1);
				st.giveItems(1867, 40, false, false);
				htmltext = null;
			}
			else if(event.equalsIgnoreCase("an_b"))
			{
				st.takeItems(ORC_GOODS, -1);
				st.giveItems(1872, 40, false, false);
				htmltext = null;
			}
			else if(event.equalsIgnoreCase("char"))
			{
				st.takeItems(ORC_GOODS, -1);
				st.giveItems(1871, 30, false, false);
				htmltext = null;
			}
			else if(event.equalsIgnoreCase("coal"))
			{
				st.takeItems(ORC_GOODS, -1);
				st.giveItems(1870, 30, false, false);
				htmltext = null;
			}
			else if(event.equalsIgnoreCase("i_o"))
			{
				st.takeItems(ORC_GOODS, -1);
				st.giveItems(1869, 30, false, false);
				htmltext = null;
			}
			if(htmltext == null)
			{
				st.finishQuest();
			}
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(cond == 0)
			htmltext = "karuda_q0644_0101.htm";
		else if(cond == 1)
			htmltext = "karuda_q0644_0106.htm";
		else if(cond == 2)
			if(st.getQuestItemsCount(ORC_GOODS) >= 120)
				htmltext = "karuda_q0644_0105.htm";
			else
			{
				st.setCond(1);
				htmltext = "karuda_q0644_0106.htm";
			}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1 && Rnd.chance(90))
		{
			st.giveItems(ORC_GOODS, 1, true, true);
			if(st.getQuestItemsCount(ORC_GOODS) >= 120)
			{
				st.setCond(2);
			}
		}
		return null;
	}
}
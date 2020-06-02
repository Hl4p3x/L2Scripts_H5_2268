package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _291_RevengeOfTheRedbonnet extends QuestScript
{
	//NPC
	int MaryseRedbonnet = 30553;
	//Quest Items
	int BlackWolfPelt = 1482;
	//Item
	int ScrollOfEscape = 736;
	int GrandmasPearl = 1502;
	int GrandmasMirror = 1503;
	int GrandmasNecklace = 1504;
	int GrandmasHairpin = 1505;
	//Mobs
	int BlackWolf = 20317;

	public _291_RevengeOfTheRedbonnet()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(MaryseRedbonnet);
		addTalkId(MaryseRedbonnet);

		addKillId(BlackWolf);

		addQuestItem(BlackWolfPelt);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("marife_redbonnet_q0291_03.htm"))
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
			if(st.getPlayer().getLevel() < 4)
				htmltext = "marife_redbonnet_q0291_01.htm";
			else
				htmltext = "marife_redbonnet_q0291_02.htm";
		}
		else if(cond == 1)
			htmltext = "marife_redbonnet_q0291_04.htm";
		else if(cond == 2 && st.getQuestItemsCount(BlackWolfPelt) < 40)
		{
			htmltext = "marife_redbonnet_q0291_04.htm";
			st.setCond(1);
		}
		else if(cond == 2 && st.getQuestItemsCount(BlackWolfPelt) >= 40)
		{
			int random = Rnd.get(100);
			st.takeItems(BlackWolfPelt, -1);
			if(random < 3)
				st.giveItems(GrandmasPearl, 1, false, false);
			else if(random < 21)
				st.giveItems(GrandmasMirror, 1, false, false);
			else if(random < 46)
				st.giveItems(GrandmasNecklace, 1, false, false);
			else
			{
				st.giveItems(ScrollOfEscape, 1, false, false);
				st.giveItems(GrandmasHairpin, 1, false, false);
			}
			htmltext = "marife_redbonnet_q0291_05.htm";
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1 && st.getQuestItemsCount(BlackWolfPelt) < 40)
		{
			st.giveItems(BlackWolfPelt, 1, true, true);
			if(st.getQuestItemsCount(BlackWolfPelt) < 40)
				st.playSound(SOUND_ITEMGET);
			else
			{
				st.setCond(2);
			}
		}
		return null;
	}
}

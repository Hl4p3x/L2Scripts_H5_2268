package quests;

import l2s.gameserver.Config;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;

/**
 * Рейты не учитываются, награда не стекуемая
 */
public class _004_LongLivethePaagrioLord extends QuestScript
{
	int HONEY_KHANDAR = 1541;
	int BEAR_FUR_CLOAK = 1542;
	int BLOODY_AXE = 1543;
	int ANCESTOR_SKULL = 1544;
	int SPIDER_DUST = 1545;
	int DEEP_SEA_ORB = 1546;

	int[][] NPC_GIFTS = {
			{
					30585,
					BEAR_FUR_CLOAK
			},
			{
					30566,
					HONEY_KHANDAR
			},
			{
					30562,
					BLOODY_AXE
			},
			{
					30560,
					ANCESTOR_SKULL
			},
			{
					30559,
					SPIDER_DUST
			},
			{
					30587,
					DEEP_SEA_ORB
			}
	};

	public _004_LongLivethePaagrioLord()
	{
		super(PARTY_NONE, ONETIME);
		addStartNpc(30578);

		addTalkId(30559, 30560, 30562, 30566, 30578, 30585, 30587);

		addQuestItem(SPIDER_DUST, ANCESTOR_SKULL, BLOODY_AXE, HONEY_KHANDAR, BEAR_FUR_CLOAK, DEEP_SEA_ORB);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("30578-03.htm"))
		{
			st.setCond(1);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 30578)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() != Race.ORC)
					htmltext = "30578-00.htm";
				else if(st.getPlayer().getLevel() >= 2)
					htmltext = "30578-02.htm";
				else
					htmltext = "30578-01.htm";
			}
			else if(cond == 1)
				htmltext = "30578-04.htm";
			else if(cond == 2)
			{
				htmltext = "30578-06.htm";
				for(int[] item : NPC_GIFTS)
					st.takeItems(item[1], -1);
				st.giveItems(4, 1, false, false);
				st.giveItems(ADENA_ID, (int) ((Config.RATE_QUESTS_REWARD - 1) * 590 + 1850 * Config.RATE_QUESTS_REWARD), false, false); // T2
				st.addExpAndSp(4254, 335);
				if(st.getPlayer().getClassId().isOfLevel(ClassLevel.NONE) && !st.getPlayer().getVarBoolean("ng1"))
					st.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.NONE, 5000, ScreenMessageAlign.TOP_CENTER, true, "Delivery duty complete.\nGo find the Newbie Guide."));
				st.finishQuest();
			}
		}
		else if(cond == 1)
			for(int Id[] : NPC_GIFTS)
				if(Id[0] == npcId)
				{
					int item = Id[1];
					if(st.getQuestItemsCount(item) > 0)
						htmltext = npc + "-02.htm";
					else
					{
						st.giveItems(item, 1, false, false);
						htmltext = npc + "-01.htm";
						int count = 0;
						for(int[] item1 : NPC_GIFTS)
							count += st.getQuestItemsCount(item1[1]);
						if(count == 6)
						{
							st.setCond(2);
						}
						else
							st.playSound(SOUND_ITEMGET);
					}
					return htmltext;
				}
		return htmltext;
	}
}
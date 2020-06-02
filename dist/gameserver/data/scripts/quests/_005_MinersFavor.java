package quests;

import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;

/**
 * Рейты не учитываются, награда не стекуемая
 */
public class _005_MinersFavor extends QuestScript
{
	//NPC
	public final int BOLTER = 30554;
	public final int SHARI = 30517;
	public final int GARITA = 30518;
	public final int REED = 30520;
	public final int BRUNON = 30526;
	//QuestItem
	public final int BOLTERS_LIST = 1547;
	public final int MINING_BOOTS = 1548;
	public final int MINERS_PICK = 1549;
	public final int BOOMBOOM_POWDER = 1550;
	public final int REDSTONE_BEER = 1551;
	public final int BOLTERS_SMELLY_SOCKS = 1552;
	//Item
	public final int NECKLACE = 906;

	public _005_MinersFavor()
	{
		super(PARTY_NONE, ONETIME);
		addStartNpc(BOLTER);
		addTalkId(SHARI);
		addTalkId(GARITA);
		addTalkId(REED);
		addTalkId(BRUNON);

		addQuestItem(BOLTERS_LIST, BOLTERS_SMELLY_SOCKS, MINING_BOOTS, MINERS_PICK, BOOMBOOM_POWDER, REDSTONE_BEER);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("miner_bolter_q0005_03.htm"))
		{
			st.setCond(1);
			st.giveItems(BOLTERS_LIST, 1, false, false);
			st.giveItems(BOLTERS_SMELLY_SOCKS, 1, false, false);
		}
		else if(event.equalsIgnoreCase("blacksmith_bronp_q0005_02.htm"))
		{
			st.takeItems(BOLTERS_SMELLY_SOCKS, -1);
			st.giveItems(MINERS_PICK, 1, false, false);
			if(st.getQuestItemsCount(BOLTERS_LIST) > 0 && st.getQuestItemsCount(MINING_BOOTS) + st.getQuestItemsCount(MINERS_PICK) + st.getQuestItemsCount(BOOMBOOM_POWDER) + st.getQuestItemsCount(REDSTONE_BEER) == 4)
			{
				st.setCond(2);
			}
			else
				st.playSound(SOUND_ITEMGET);

		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(npcId == BOLTER)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 2)
					htmltext = "miner_bolter_q0005_02.htm";
				else
					htmltext = "miner_bolter_q0005_01.htm";
			}
			else if(cond == 1)
				htmltext = "miner_bolter_q0005_04.htm";
			else if(cond == 2 && st.getQuestItemsCount(MINING_BOOTS) + st.getQuestItemsCount(MINERS_PICK) + st.getQuestItemsCount(BOOMBOOM_POWDER) + st.getQuestItemsCount(REDSTONE_BEER) >= 4)
			{
				htmltext = "miner_bolter_q0005_06.htm";
				st.takeItems(MINING_BOOTS, -1);
				st.takeItems(MINERS_PICK, -1);
				st.takeItems(BOOMBOOM_POWDER, -1);
				st.takeItems(REDSTONE_BEER, -1);
				st.takeItems(BOLTERS_LIST, -1);
				st.giveItems(NECKLACE, 1, false, false);
				st.addExpAndSp(5672, 446);
				if(st.getPlayer().getClassId().isOfLevel(ClassLevel.NONE) && !st.getPlayer().getVarBoolean("ng1"))
					st.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.NONE, 5000, ScreenMessageAlign.TOP_CENTER, true, "Delivery duty complete.\nGo find the Newbie Guide."));
				st.giveItems(ADENA_ID, 2466, true, true);
				st.unset("cond");
				st.finishQuest();
			}
		}
		else if(cond == 1 && st.getQuestItemsCount(BOLTERS_LIST) > 0)
		{
			if(npcId == SHARI)
			{
				if(st.getQuestItemsCount(BOOMBOOM_POWDER) == 0)
				{
					htmltext = "trader_chali_q0005_01.htm";
					st.giveItems(BOOMBOOM_POWDER, 1, false, false);
					st.playSound(SOUND_ITEMGET);
				}
				else
					htmltext = "trader_chali_q0005_02.htm";
			}
			else if(npcId == GARITA)
			{
				if(st.getQuestItemsCount(MINING_BOOTS) == 0)
				{
					htmltext = "trader_garita_q0005_01.htm";
					st.giveItems(MINING_BOOTS, 1, false, false);
					st.playSound(SOUND_ITEMGET);
				}
				else
					htmltext = "trader_garita_q0005_02.htm";
			}
			else if(npcId == REED)
			{
				if(st.getQuestItemsCount(REDSTONE_BEER) == 0)
				{
					htmltext = "warehouse_chief_reed_q0005_01.htm";
					st.giveItems(REDSTONE_BEER, 1, false, false);
					st.playSound(SOUND_ITEMGET);
				}
				else
					htmltext = "warehouse_chief_reed_q0005_02.htm";
			}
			else if(npcId == BRUNON && st.getQuestItemsCount(BOLTERS_SMELLY_SOCKS) > 0)
				if(st.getQuestItemsCount(MINERS_PICK) == 0)
					htmltext = "blacksmith_bronp_q0005_01.htm";
				else
					htmltext = "blacksmith_bronp_q0005_03.htm";
			if(st.getQuestItemsCount(BOLTERS_LIST) > 0 && st.getQuestItemsCount(MINING_BOOTS) + st.getQuestItemsCount(MINERS_PICK) + st.getQuestItemsCount(BOOMBOOM_POWDER) + st.getQuestItemsCount(REDSTONE_BEER) >= 4)
			{
				st.setCond(2);
			}
		}
		return htmltext;
	}
}

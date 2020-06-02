package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _299_GatherIngredientsforPie extends QuestScript
{
	// NPCs
	private static int Emily = 30620;
	private static int Lara = 30063;
	private static int Bright = 30466;
	// Mobs
	private static int Wasp_Worker = 20934;
	private static int Wasp_Leader = 20935;
	// Items
	private static int Varnish = 1865;
	// Quest Items
	private static int Fruit_Basket = 7136;
	private static int Avellan_Spice = 7137;
	private static int Honey_Pouch = 7138;
	// Chances
	private static int Wasp_Worker_Chance = 55;
	private static int Wasp_Leader_Chance = 70;
	private static int Reward_Varnish_Chance = 50;

	public _299_GatherIngredientsforPie()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(Emily);
		addTalkId(Lara);
		addTalkId(Bright);
		addKillId(Wasp_Worker);
		addKillId(Wasp_Leader);
		addQuestItem(Fruit_Basket);
		addQuestItem(Avellan_Spice);
		addQuestItem(Honey_Pouch);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int cond = st.getCond();
		if(event.equalsIgnoreCase("emilly_q0299_0104.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("emilly_q0299_0201.htm"))
		{
			if(st.getQuestItemsCount(Honey_Pouch) < 100)
				return "emilly_q0299_0202.htm";
			st.takeItems(Honey_Pouch, -1);
			st.setCond(3);
		}
		else if(event.equalsIgnoreCase("lars_q0299_0301.htm") && cond == 3)
		{
			st.giveItems(Avellan_Spice, 1, false, false);
			st.setCond(4);
		}
		else if(event.equalsIgnoreCase("emilly_q0299_0401.htm"))
		{
			if(st.getQuestItemsCount(Avellan_Spice) < 1)
				return "emilly_q0299_0402.htm";
			st.takeItems(Avellan_Spice, -1);
			st.setCond(5);
		}
		else if(event.equalsIgnoreCase("guard_bright_q0299_0501.htm") && cond == 5)
		{
			st.giveItems(Fruit_Basket, 1, false, false);
			st.setCond(6);
		}
		else if(event.equalsIgnoreCase("emilly_q0299_0601.htm"))
		{
			if(st.getQuestItemsCount(Fruit_Basket) < 1)
				return "emilly_q0299_0602.htm";
			st.takeItems(Fruit_Basket, -1);
			if(Rnd.chance(Reward_Varnish_Chance))
				st.giveItems(Varnish, 50, false, false);
			else
				st.giveItems(ADENA_ID, 25000, true, true);
			st.finishQuest();
		}

		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Emily)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 34)
					return "emilly_q0299_0101.htm";
				return "emilly_q0299_0102.htm";
			}
			if(cond == 1 && st.getQuestItemsCount(Honey_Pouch) <= 99)
				return "emilly_q0299_0106.htm";
			if(cond == 2 && st.getQuestItemsCount(Honey_Pouch) >= 100)
				return "emilly_q0299_0105.htm";
			if(cond == 3 && st.getQuestItemsCount(Avellan_Spice) == 0)
				return "emilly_q0299_0203.htm";
			if(cond == 4 && st.getQuestItemsCount(Avellan_Spice) == 1)
				return "emilly_q0299_0301.htm";
			if(cond == 5 && st.getQuestItemsCount(Fruit_Basket) == 0)
				return "emilly_q0299_0403.htm";
			if(cond == 6 && st.getQuestItemsCount(Fruit_Basket) == 1)
				return "emilly_q0299_0501.htm";
		}
		if(npcId == Lara && cond == 3)
			return "lars_q0299_0201.htm";
		if(npcId == Lara && cond == 4)
			return "lars_q0299_0302.htm";
		if(npcId == Bright && cond == 5)
			return "guard_bright_q0299_0401.htm";
		if(npcId == Bright && cond == 5)
			return "guard_bright_q0299_0502.htm";

		return NO_QUEST_DIALOG;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(qs.getCond() != 1 || qs.getQuestItemsCount(Honey_Pouch) >= 100)
			return null;

		int npcId = npc.getNpcId();
		if(npcId == Wasp_Worker && Rnd.chance(Wasp_Worker_Chance) || npcId == Wasp_Leader && Rnd.chance(Wasp_Leader_Chance))
		{
			qs.giveItems(Honey_Pouch, 1, true, true);
			if(qs.getQuestItemsCount(Honey_Pouch) < 100)
				qs.playSound(SOUND_ITEMGET);
			else
			{
				qs.setCond(2);
			}
		}

		return null;
	}
}
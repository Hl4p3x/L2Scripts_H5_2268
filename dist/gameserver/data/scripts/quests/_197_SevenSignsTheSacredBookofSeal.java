package quests;

import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.scripts.Functions;

public class _197_SevenSignsTheSacredBookofSeal extends QuestScript
{
	// NPCs
	private static int Wood = 32593;
	private static int Orven = 30857;
	private static int Leopard = 32594;
	private static int Lawrence = 32595;
	private static int ShilensEvilThoughts = 27396;
	private static int Sofia = 32596;

	// ITEMS
	private static int PieceofDoubt = 14354;
	private static int MysteriousHandwrittenText = 13829;

	public _197_SevenSignsTheSacredBookofSeal()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(Wood);
		addTalkId(Wood, Orven, Leopard, Lawrence, Sofia);
		addKillId(ShilensEvilThoughts);
		addQuestItem(PieceofDoubt, MysteriousHandwrittenText);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		Player player = st.getPlayer();
		String htmltext = event;
		if(event.equalsIgnoreCase("wood_q197_2.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("orven_q197_2.htm"))
		{
			st.setCond(2);
		}
		else if(event.equalsIgnoreCase("leopard_q197_2.htm"))
		{
			st.setCond(3);
		}
		else if(event.equalsIgnoreCase("lawrence_q197_2.htm"))
		{
			NpcInstance mob = st.addSpawn(ShilensEvilThoughts, 152520, -57502, -3408, 0, 0, 180000);
			Functions.npcSay(mob, "Shilen's power is endless!");
			mob.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, player, 100000);
			st.set("evilthought", 1);
		}
		else if(event.equalsIgnoreCase("lawrence_q197_4.htm"))
		{
			st.setCond(5);
		}
		else if(event.equalsIgnoreCase("sofia_q197_2.htm"))
		{
			st.setCond(6);
			st.giveItems(MysteriousHandwrittenText, 1, false, false);
		}
		else if(event.equalsIgnoreCase("wood_q197_4.htm"))
			if(player.getBaseClassId() == player.getActiveClassId())
			{
				st.takeItems(PieceofDoubt, -1);
				st.takeItems(MysteriousHandwrittenText, -1);
				st.addExpAndSp(25000000, 2500000);
				st.finishQuest();
			}
			else
				return "subclass_forbidden.htm";
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		Player player = st.getPlayer();
		String htmltext = NO_QUEST_DIALOG;
		if(npcId == Wood)
		{
			if(cond == 0)
			{
				if(player.getLevel() >= 79 && player.isQuestCompleted(196))
					htmltext = "wood_q197_1.htm";
				else
					htmltext = "wood_q197_0.htm";
			}
			else if(cond == 6)
				htmltext = "wood_q197_3.htm";
			else
				htmltext = "wood_q197_5.htm";
		}
		else if(npcId == Orven)
		{
			if(cond == 1)
				htmltext = "orven_q197_1.htm";
			else if(cond == 2)
				htmltext = "orven_q197_3.htm";
		}
		else if(npcId == Leopard)
		{
			if(cond == 2)
				htmltext = "leopard_q197_1.htm";
			else if(cond == 3)
				htmltext = "leopard_q197_3.htm";
		}
		else if(npcId == Lawrence)
		{
			if(cond == 3)
			{
				if(st.get("evilthought") != null && Integer.parseInt(st.get("evilthought")) == 1)
					htmltext = "lawrence_q197_0.htm";
				else
					htmltext = "lawrence_q197_1.htm";
			}
			else if(cond == 4)
				htmltext = "lawrence_q197_3.htm";
			else if(cond == 5)
				htmltext = "lawrence_q197_5.htm";
		}
		else if(npcId == Sofia)
			if(cond == 5)
				htmltext = "sofia_q197_1.htm";
			else if(cond == 6)
				htmltext = "sofia_q197_3.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		Player player = st.getPlayer();
		if(player == null)
			return null;

		if(npc.getNpcId() == ShilensEvilThoughts && cond == 3)
		{
			st.setCond(4);
			st.playSound(SOUND_ITEMGET);
			st.giveItems(PieceofDoubt, 1, false, false);
			st.set("evilthought", 2);
		}
		return null;
	}
}
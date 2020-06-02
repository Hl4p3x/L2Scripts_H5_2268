package quests;

import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.scripts.Functions;

public class _714_PathToBecomingALordSchuttgart extends QuestScript
{
	private static final int August = 35555;
	private static final int Newyear = 31961;
	private static final int Yasheni = 31958;
	private static final int GolemShard = 17162;

	private static final int ShuttgartCastle = 9;

	public _714_PathToBecomingALordSchuttgart()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(August);
		addTalkId(Newyear, Yasheni);
		for(int i = 22801; i < 22812; i++)
			addKillId(i);
		addQuestItem(GolemShard);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		Castle castle = ResidenceHolder.getInstance().getResidence(ShuttgartCastle);
		if(castle.getOwner() == null)
			return "Castle has no lord";
		Player castleOwner = castle.getOwner().getLeader().getPlayer();

		if(event.equals("august_q714_03.htm"))
		{
			st.setCond(1);
		}
		else if(event.equals("august_q714_05.htm"))
		{
			st.setCond(2);
		}
		else if(event.equals("newyear_q714_03.htm"))
		{
			st.setCond(3);
		}
		else if(event.equals("yasheni_q714_02.htm"))
		{
			st.setCond(5);
		}
		else if(event.equals("august_q714_08.htm"))
		{
			Functions.npcSay(npc, NpcString.S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_SCHUTTGART, st.getPlayer().getName());
			castle.getDominion().changeOwner(castleOwner.getClan());
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		Castle castle = ResidenceHolder.getInstance().getResidence(ShuttgartCastle);
		if(castle.getOwner() == null)
			return "Castle has no lord";

		if(npcId == August)
		{
			if(cond == 0)
			{
				Player castleOwner = castle.getOwner().getLeader().getPlayer();
				if(castleOwner == st.getPlayer())
				{
					if(castle.getDominion().getLordObjectId() != st.getPlayer().getObjectId())
						htmltext = "august_q714_01.htm";
					else
						htmltext = "august_q714_00.htm";
				}
				else
					htmltext = "august_q714_00a.htm";
			}
			else if(cond == 1)
				htmltext = "august_q714_04.htm";
			else if(cond == 2)
				htmltext = "august_q714_06.htm";
			else if(cond == 7)
				htmltext = "august_q714_07.htm";
		}
		else if(npcId == Newyear)
		{
			if(cond == 2)
			{
				htmltext = "newyear_q714_01.htm";
			}
			else if(cond == 3)
			{
				if(st.getPlayer().isQuestCompleted(121))
				{
					if(st.getPlayer().isQuestCompleted(114))
					{
						if(st.getPlayer().isQuestCompleted(120))
						{
							st.setCond(4);
							htmltext = "newyear_q714_04.htm";
						}
						else
							htmltext = "newyear_q714_04a.htm";
					}
					else
						htmltext = "newyear_q714_04b.htm";
				}
				else
					htmltext = "newyear_q714_04c.htm";
			}
		}
		else if(npcId == Yasheni)
		{
			if(cond == 4)
				htmltext = "yasheni_q714_01.htm";
			else if(cond == 5)
				htmltext = "yasheni_q714_03.htm";
			else if(cond == 6)
			{
				st.takeAllItems(GolemShard);
				st.setCond(7);
				htmltext = "yasheni_q714_04.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 5)
		{
			if(st.getQuestItemsCount(GolemShard) < 300)
				st.giveItems(GolemShard, 1, true, true);
			if(st.getQuestItemsCount(GolemShard) >= 300)
				st.setCond(6);
		}
		return null;
	}
}
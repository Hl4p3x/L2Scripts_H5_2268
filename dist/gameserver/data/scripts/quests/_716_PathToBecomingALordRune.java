package quests;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.scripts.Functions;

/**
 * @author pchayka
 *
 * TODO: удалять квест у доверенного лица
 */

public class _716_PathToBecomingALordRune extends QuestScript
{
	private static final int Frederick = 35509;
	private static final int Agripel = 31348;
	private static final int Innocentin = 31328;

	private static final int RuneCastle = 8;
	private static List<Integer> Pagans = new ArrayList<Integer>();

	static
	{
		for(int i = 22138; i <= 22176; i++)
			Pagans.add(i);
		for(int i = 22188; i <= 22195; i++)
			Pagans.add(i);
	}

	public _716_PathToBecomingALordRune()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(Frederick);
		addTalkId(Agripel, Innocentin);
		addKillId(Pagans);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		Castle castle = ResidenceHolder.getInstance().getResidence(RuneCastle);
		if(castle.getOwner() == null)
			return "Castle has no lord";
		Player castleOwner = castle.getOwner().getLeader().getPlayer();
		String htmltext = event;
		if(event.equals("frederick_q716_03.htm"))
		{
			st.setCond(1);
		}
		else if(event.equals("agripel_q716_03.htm"))
			st.setCond(3);
		else if(event.equals("frederick_q716_08.htm"))
		{
			castleOwner.getQuestState(this).set("confidant", String.valueOf(st.getPlayer().getObjectId()), true);
			castleOwner.getQuestState(this).setCond(5);
		}
		else if(event.equals("innocentin_q716_03.htm"))
		{
			if(castleOwner != null && castleOwner != st.getPlayer() && castleOwner.getQuestState(this) != null && castleOwner.getQuestState(this).getCond() == 5)
				castleOwner.getQuestState(this).setCond(6);
		}
		else if(event.equals("agripel_q716_08.htm"))
			st.setCond(8);
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		Castle castle = ResidenceHolder.getInstance().getResidence(RuneCastle);
		if(castle.getOwner() == null)
			return "Castle has no lord";
		Player castleOwner = castle.getOwner().getLeader().getPlayer();

		if(npcId == Frederick)
		{
			if(cond == 0)
			{
				if(castleOwner == st.getPlayer())
				{
					if(castle.getDominion().getLordObjectId() != st.getPlayer().getObjectId())
						htmltext = "frederick_q716_01.htm";
					else
						htmltext = "frederick_q716_00.htm";
				}
				// Лидер клана в игре, говорящий не лидер, у лидера взят квест и пройден до стадии назначения поверенного
				else if(castleOwner != null && castleOwner != st.getPlayer() && castleOwner.getQuestState(this) != null && castleOwner.getQuestState(this).getCond() == 4)
				{
					if(castleOwner.isInRangeZ(npc, 200))
						htmltext = "frederick_q716_07.htm";
					else
						htmltext = "frederick_q716_07a.htm";
				}
				else
					htmltext = "frederick_q716_00a.htm";
			}
			else if(cond == 1)
			{
				if(st.getPlayer().isQuestCompleted(21) && st.getPlayer().isQuestCompleted(25))
				{
					st.setCond(2);
					htmltext = "frederick_q716_04.htm";
				}
				else
					htmltext = "frederick_q716_03.htm";
			}
			else if(cond == 2)
				htmltext = "frederick_q716_04a.htm";
			else if(cond == 3)
			{
				st.setCond(4);
				htmltext = "frederick_q716_05.htm";
			}
			else if(cond == 4)
				htmltext = "frederick_q716_06.htm";
			else if(cond == 5)
				htmltext = "frederick_q716_09.htm";
			else if(cond == 6)
			{
				st.setCond(7);
				htmltext = "frederick_q716_10.htm";
			}
			else if(cond == 7)
				htmltext = "frederick_q716_11.htm";
			else if(cond == 8)
			{
				Functions.npcSay(npc, NpcString.S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_RUNE, st.getPlayer().getName());
				castle.getDominion().changeOwner(castleOwner.getClan());
				htmltext = "frederick_q716_12.htm";
				st.finishQuest();
			}
		}
		else if(npcId == Agripel)
		{
			if(cond == 2)
				htmltext = "agripel_q716_01.htm";
			else if(cond == 7)
			{
				if(st.get("paganCount") != null && Integer.parseInt(st.get("paganCount")) >= 100)
					htmltext = "agripel_q716_07.htm";
				else
					htmltext = "agripel_q716_04.htm";
			}
			else if(cond == 8)
				htmltext = "agripel_q716_09.htm";
		}
		else if(npcId == Innocentin)
		{
			if(cond == 0)
			{
				if(castleOwner != null && castleOwner != st.getPlayer() && castleOwner.getQuestState(this) != null && castleOwner.getQuestState(this).getCond() == 5)
				{
					if(Integer.parseInt(castleOwner.getQuestState(this).get("confidant")) == st.getPlayer().getObjectId())
						htmltext = "innocentin_q716_01.htm";
					else
						htmltext = "innocentin_q716_00.htm";
				}
				else
					htmltext = "innocentin_q716_00a.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		Castle castle = ResidenceHolder.getInstance().getResidence(RuneCastle);
		Player castleOwner = castle.getOwner().getLeader().getPlayer();
		if(st.getCond() == 0)
		{
			if(castleOwner != null && castleOwner != st.getPlayer() && castleOwner.getQuestState(this) != null && castleOwner.getQuestState(this).getCond() == 7)
			{
				if(castleOwner.getQuestState(this).get("paganCount") != null)
					castleOwner.getQuestState(this).set("paganCount", String.valueOf(Integer.parseInt(castleOwner.getQuestState(this).get("paganCount")) + 1), true);
				else
					castleOwner.getQuestState(this).set("paganCount", "1", true);
			}
		}
		return null;
	}
}
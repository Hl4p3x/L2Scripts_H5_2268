package quests;

import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.scripts.Functions;

public class _713_PathToBecomingALordAden extends QuestScript
{
	private static final int Logan = 35274;
	private static final int Orven = 30857;
	private static final int[] Orcs = {
			20669,
			20665
	};

	private static final int AdenCastle = 5;
	private int _mobs = 0;

	public _713_PathToBecomingALordAden()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(Logan);
		addTalkId(Orven);
		addKillId(Orcs);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		Castle castle = ResidenceHolder.getInstance().getResidence(AdenCastle);
		if(castle.getOwner() == null)
			return "Castle has no lord";
		Player castleOwner = castle.getOwner().getLeader().getPlayer();

		if(event.equals("logan_q713_02.htm"))
		{
			st.setCond(1);
		}
		else if(event.equals("orven_q713_03.htm"))
		{
			st.setCond(2);
		}
		else if(event.equals("logan_q713_05.htm"))
		{
			Functions.npcSay(npc, NpcString.S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_ADEN, st.getPlayer().getName());
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

		Castle castle = ResidenceHolder.getInstance().getResidence(AdenCastle);
		if(castle.getOwner() == null)
			return "Castle has no lord";

		if(npcId == Logan)
		{
			if(cond == 0)
			{
				Player castleOwner = castle.getOwner().getLeader().getPlayer();
				if(castleOwner == st.getPlayer())
				{
					if(castle.getDominion().getLordObjectId() != st.getPlayer().getObjectId())
						htmltext = "logan_q713_01.htm";
					else
						htmltext = "logan_q713_00.htm";
				}
				else
					htmltext = "logan_q713_00a.htm";
			}
			else if(cond == 1)
				htmltext = "logan_q713_03.htm";
			else if(cond == 7)
				htmltext = "logan_q713_04.htm";
		}
		else if(npcId == Orven)
		{
			if(cond == 1)
				htmltext = "orven_q713_01.htm";
			else if(cond == 2)
				htmltext = "orven_q713_04.htm";
			else if(cond == 4)
				htmltext = "orven_q713_05.htm";
			else if(cond == 5)
			{
				st.setCond(7);
				htmltext = "orven_q713_06.htm";
			}
			else if(cond == 7)
				htmltext = "orven_q713_06.htm";
		}

		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 4)
		{
			if(_mobs < 100)
				_mobs++;
			else
				st.setCond(5);
		}
		return null;
	}
}
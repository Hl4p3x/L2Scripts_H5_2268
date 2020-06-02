package quests;

import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.scripts.Functions;

public class _710_PathToBecomingALordGiran extends QuestScript
{
	private static final int Saul = 35184;
	private static final int Gesto = 30511;
	private static final int Felton = 30879;
	private static final int CargoBox = 32243;

	private static final int FreightChest = 13014;
	private static final int GestoBox = 13013;

	private static final int[] Mobs = { 20832, 20833, 20835, 21602, 21603, 21604, 21605, 21606, 21607, 21608, 21609 };

	private static final int GiranCastle = 3;

	public _710_PathToBecomingALordGiran()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(Saul);
		addTalkId(Gesto, Felton, CargoBox);
		addQuestItem(FreightChest, GestoBox);
		addKillId(Mobs);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		Castle castle = ResidenceHolder.getInstance().getResidence(GiranCastle);
		if(castle.getOwner() == null)
			return "Castle has no lord";
		Player castleOwner = castle.getOwner().getLeader().getPlayer();
		if(event.equals("saul_q710_03.htm"))
		{
			st.setCond(1);
		}
		else if(event.equals("gesto_q710_03.htm"))
		{
			st.setCond(3);
		}
		else if(event.equals("felton_q710_02.htm"))
		{
			st.setCond(4);
		}
		else if(event.equals("saul_q710_07.htm"))
		{
			Functions.npcSay(npc, NpcString.S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_GIRAN, st.getPlayer().getName());
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
		Castle castle = ResidenceHolder.getInstance().getResidence(GiranCastle);
		if(castle.getOwner() == null)
			return "Castle has no lord";
		Player castleOwner = castle.getOwner().getLeader().getPlayer();
		if(npcId == Saul)
		{
			if(cond == 0)
			{
				if(castleOwner == st.getPlayer())
				{
					if(castle.getDominion().getLordObjectId() != st.getPlayer().getObjectId())
						htmltext = "saul_q710_01.htm";
					else
						htmltext = "saul_q710_00.htm";
				}
				else
					htmltext = "saul_q710_00a.htm";
			}
			else if(cond == 1)
			{
				st.setCond(2);
				htmltext = "saul_q710_04.htm";
			}
			else if(cond == 2)
				htmltext = "saul_q710_05.htm";
			else if(cond == 9)
				htmltext = "saul_q710_06.htm";
		}
		else if(npcId == Gesto)
		{
			if(cond == 2)
				htmltext = "gesto_q710_01.htm";
			else if(cond == 3 || cond == 4)
				htmltext = "gesto_q710_04.htm";
			else if(cond == 5)
			{
				st.takeAllItems(FreightChest);
				st.setCond(7);
				htmltext = "gesto_q710_05.htm";
			}
			else if(cond == 7)
				htmltext = "gesto_q710_06.htm";
			else if(cond == 8)
			{
				st.takeAllItems(GestoBox);
				st.setCond(9);
				htmltext = "gesto_q710_07.htm";
			}
			else if(cond == 9)
				htmltext = "gesto_q710_07.htm";

		}
		else if(npcId == Felton)
		{
			if(cond == 3)
				htmltext = "felton_q710_01.htm";
			else if(cond == 4)
				htmltext = "felton_q710_03.htm";
		}
		else if(npcId == CargoBox)
		{
			if(cond == 4)
			{
				st.setCond(5);
				st.giveItems(FreightChest, 1, false, false);
				htmltext = "box_q710_01.htm";
			}
			else if(cond == 5)
				htmltext = "box_q710_02.htm";
		}

		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 7)
		{
			if(st.getQuestItemsCount(GestoBox) < 300)
				st.giveItems(GestoBox, 1, true, true);
			if(st.getQuestItemsCount(GestoBox) >= 300)
				st.setCond(8);
		}
		return null;
	}
}
package quests;

import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.scripts.Functions;

public class _712_PathToBecomingALordOren extends QuestScript
{
	private static final int Brasseur = 35226;
	private static final int Croop = 30676;
	private static final int Marty = 30169;
	private static final int Valleria = 30176;

	private static final int NebuliteOrb = 13851;

	private static final int[] OelMahims = { 20575, 20576 };

	private static final int OrenCastle = 4;

	public _712_PathToBecomingALordOren()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(Brasseur, Marty);
		addTalkId(Croop, Marty, Valleria);
		addQuestItem(NebuliteOrb);
		addKillId(OelMahims);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		Castle castle = ResidenceHolder.getInstance().getResidence(OrenCastle);
		if(castle.getOwner() == null)
			return "Castle has no lord";
		Player castleOwner = castle.getOwner().getLeader().getPlayer();
		if(event.equals("brasseur_q712_03.htm"))
		{
			st.setCond(1);
		}
		else if(event.equals("croop_q712_03.htm"))
		{
			st.setCond(3);
		}
		else if(event.equals("marty_q712_02.htm"))
		{
			if(isLordAvailable(3, st))
			{
				castleOwner.getQuestState(this).setCond(4);
			}
		}
		else if(event.equals("valleria_q712_02.htm"))
		{
			if(isLordAvailable(4, st))
			{
				castleOwner.getQuestState(this).setCond(5);
				st.finishQuest();
			}
		}
		else if(event.equals("croop_q712_05.htm"))
		{
			st.setCond(6);
		}
		else if(event.equals("croop_q712_07.htm"))
		{
			st.setCond(8);
		}
		else if(event.equals("brasseur_q712_06.htm"))
		{
			Functions.npcSay(npc, NpcString.S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_OREN, st.getPlayer().getName());
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
		Castle castle = ResidenceHolder.getInstance().getResidence(OrenCastle);
		if(castle.getOwner() == null)
			return "Castle has no lord";
		Player castleOwner = castle.getOwner().getLeader().getPlayer();

		if(npcId == Brasseur)
		{
			if(cond == 0)
			{
				if(castleOwner == st.getPlayer())
				{
					if(castle.getDominion().getLordObjectId() != st.getPlayer().getObjectId())
						htmltext = "brasseur_q712_01.htm";
					else
						htmltext = "brasseur_q712_00.htm";
				}
				else
					htmltext = "brasseur_q712_00a.htm";
			}
			else if(cond == 1)
			{
				st.setCond(2);
				htmltext = "brasseur_q712_04.htm";
			}
			else if(cond == 2)
				htmltext = "brasseur_q712_04.htm";
			else if(cond == 8)
				htmltext = "brasseur_q712_05.htm";
		}
		else if(npcId == Croop)
		{
			if(cond == 2)
				htmltext = "croop_q712_01.htm";
			else if(cond == 3 || cond == 4)
				htmltext = "croop_q712_03.htm";
			else if(cond == 5)
				htmltext = "croop_q712_04.htm";
			else if(cond == 6)
				htmltext = "croop_q712_05.htm";
			else if(cond == 7)
				htmltext = "croop_q712_06.htm";
			else if(cond == 8)
				htmltext = "croop_q712_08.htm";
		}
		else if(npcId == Marty)
		{
			if(cond == 0)
			{
				if(isLordAvailable(3, st))
					htmltext = "marty_q712_01.htm";
				else
					htmltext = "marty_q712_00.htm";
			}
		}
		else if(npcId == Valleria)
		{
			if(st.isStarted() && isLordAvailable(4, st))
				htmltext = "valleria_q712_01.htm";
		}

		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 6)
		{
			if(st.getQuestItemsCount(NebuliteOrb) < 300)
				st.giveItems(NebuliteOrb, 1, true, true);
			if(st.getQuestItemsCount(NebuliteOrb) >= 300)
				st.setCond(7);
		}
		return null;
	}

	private boolean isLordAvailable(int cond, QuestState st)
	{
		Castle castle = ResidenceHolder.getInstance().getResidence(OrenCastle);
		Clan owner = castle.getOwner();
		Player castleOwner = castle.getOwner().getLeader().getPlayer();
		if(owner != null)
			if(castleOwner != null && castleOwner != st.getPlayer() && owner == st.getPlayer().getClan() && castleOwner.getQuestState(this) != null && castleOwner.getQuestState(this).getCond() == cond)
				return true;
		return false;
	}
}

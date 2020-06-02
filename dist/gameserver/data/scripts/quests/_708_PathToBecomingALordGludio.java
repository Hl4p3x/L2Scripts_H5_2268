package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.scripts.Functions;

public class _708_PathToBecomingALordGludio extends QuestScript
{
	private static final int Sayres = 35100;
	private static final int Pinter = 30298;
	private static final int Bathis = 30332;

	private static final int HeadlessKnightsArmor = 13848;

	private static final int[] Mobs = { 20045, 20051, 20099 };

	private static final int GludioCastle = 1;

	public _708_PathToBecomingALordGludio()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(Sayres);
		addTalkId(Pinter, Bathis);
		addQuestItem(HeadlessKnightsArmor);
		addKillId(Mobs);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		Castle castle = ResidenceHolder.getInstance().getResidence(GludioCastle);
		if(castle.getOwner() == null)
			return "Castle has no lord";
		Player castleOwner = castle.getOwner().getLeader().getPlayer();
		if(event.equals("sayres_q708_03.htm"))
		{
			st.setCond(1);
		}
		else if(event.equals("sayres_q708_05.htm"))
		{
			st.setCond(2);
		}
		else if(event.equals("sayres_q708_08.htm"))
		{
			if(isLordAvailable(2, st))
			{
				castleOwner.getQuestState(this).set("confidant", String.valueOf(st.getPlayer().getObjectId()), true);
				castleOwner.getQuestState(this).setCond(3);
			}
			else
				htmltext = "sayres_q708_05a.htm";
		}
		else if(event.equals("pinter_q708_03.htm"))
		{
			if(isLordAvailable(3, st))
			{
				castleOwner.getQuestState(this).setCond(4);
			}
			else
				htmltext = "pinter_q708_03a.htm";
		}
		else if(event.equals("bathis_q708_02.htm"))
		{
			st.setCond(6);
		}
		else if(event.equals("bathis_q708_05.htm"))
		{
			st.setCond(8);
			Functions.npcSay(npc, NpcString.LISTEN_YOU_VILLAGERS_OUR_LIEGE_WHO_WILL_SOON_BECAME_A_LORD_HAS_DEFEATED_THE_HEADLESS_KNIGHT);
		}
		else if(event.equals("pinter_q708_05.htm"))
		{
			if(isLordAvailable(8, st))
			{
				st.takeItems(1867, 100);
				st.takeItems(1865, 100);
				st.takeItems(1869, 100);
				st.takeItems(1879, 50);
				castleOwner.getQuestState(this).setCond(9);
			}
			else
				htmltext = "pinter_q708_03a.htm";
		}
		else if(event.equals("sayres_q708_12.htm"))
		{
			Functions.npcSay(npc, NpcString.S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_GLUDIO, st.getPlayer().getName());
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
		Castle castle = ResidenceHolder.getInstance().getResidence(GludioCastle);
		if(castle.getOwner() == null)
			return "Castle has no lord";
		Player castleOwner = castle.getOwner().getLeader().getPlayer();
		if(npcId == Sayres)
		{
			if(cond == 0)
			{
				if(castleOwner == st.getPlayer())
				{
					if(castle.getDominion().getLordObjectId() != st.getPlayer().getObjectId())
						htmltext = "sayres_q708_01.htm";
					else
						htmltext = "sayres_q708_00.htm";
				}
				else if(isLordAvailable(2, st))
				{
					if(castleOwner.isInRangeZ(npc, 200))
						htmltext = "sayres_q708_07.htm";
					else
						htmltext = "sayres_q708_05a.htm";
				}
				else
					htmltext = "sayres_q708_00a.htm";
			}
			else if(cond == 1)
			{
				htmltext = "sayres_q708_04.htm";
			}
			else if(cond == 2)
				htmltext = "sayres_q708_06.htm";
			else if(cond == 4)
			{
				st.setCond(5);
				htmltext = "sayres_q708_09.htm";
			}
			else if(cond == 5)
				htmltext = "sayres_q708_10.htm";
			else if(cond > 5 && cond < 9)
				htmltext = "sayres_q708_08.htm";
			else if(cond == 9)
				htmltext = "sayres_q708_11.htm";

		}
		else if(npcId == Pinter)
		{
			if(cond == 0 && isLordAvailable(3, st))
			{
				if(Integer.parseInt(castleOwner.getQuestState(this).get("confidant")) == st.getPlayer().getObjectId())
					htmltext = "pinter_q708_01.htm";
			}
			else if(cond == 0 && isLordAvailable(8, st))
			{
				if(st.getQuestItemsCount(1867) >= 100 && st.getQuestItemsCount(1865) >= 100 && st.getQuestItemsCount(1869) >= 100 && st.getQuestItemsCount(1879) >= 50)
					htmltext = "pinter_q708_04.htm";
				else
					htmltext = "pinter_q708_04a.htm";
			}
			else if(cond == 0 && isLordAvailable(9, st))
			{
				htmltext = "pinter_q708_06.htm";
			}

		}
		else if(npcId == Bathis)
		{
			if(cond == 5)
				htmltext = "bathis_q708_01.htm";
			else if(cond == 6)
				htmltext = "bathis_q708_03.htm";
			else if(cond == 7)
				htmltext = "bathis_q708_04.htm";
			else if(cond == 8)
				htmltext = "sophia_q709_06.htm";

		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 6)
		{
			if(Rnd.chance(10))
			{
				st.giveItems(HeadlessKnightsArmor, 1, false, false);
				st.setCond(7);
			}
		}
		return null;
	}

	private boolean isLordAvailable(int cond, QuestState st)
	{
		Castle castle = ResidenceHolder.getInstance().getResidence(GludioCastle);
		Clan owner = castle.getOwner();
		Player castleOwner = castle.getOwner().getLeader().getPlayer();
		if(owner != null)
			if(castleOwner != null && castleOwner != st.getPlayer() && owner == st.getPlayer().getClan() && castleOwner.getQuestState(this) != null && castleOwner.getQuestState(this).getCond() == cond)
				return true;
		return false;
	}
}
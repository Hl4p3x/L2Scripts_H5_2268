package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;

public class _169_OffspringOfNightmares extends QuestScript
{
	//NPC
	private static final int Vlasty = 30145;
	//QuestItem
	private static final int CrackedSkull = 1030;
	private static final int PerfectSkull = 1031;
	//Item
	private static final int BoneGaiters = 31;
	//MOB
	private static final int DarkHorror = 20105;
	private static final int LesserDarkHorror = 20025;

	public _169_OffspringOfNightmares()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(Vlasty);

		addTalkId(Vlasty);

		addKillId(DarkHorror);
		addKillId(LesserDarkHorror);

		addQuestItem(new int[]{
				CrackedSkull,
				PerfectSkull
		});
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("30145-04.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("30145-08.htm"))
		{
			st.takeItems(CrackedSkull, -1);
			st.takeItems(PerfectSkull, -1);
			st.giveItems(BoneGaiters, 1, false, false);
			st.giveItems(ADENA_ID, 17050, true, true);
			st.addExpAndSp(17475, 818);

			if(st.getPlayer().getClassId().isOfLevel(ClassLevel.NONE) && !st.getPlayer().getVarBoolean("p1q4"))
			{
				st.getPlayer().setVar("p1q4", "1", -1);
				st.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.NONE, 5000, ScreenMessageAlign.TOP_CENTER, true, "Now go find the Newbie Guide."));
			}
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(npcId == Vlasty)
			if(cond == 0)
			{
				if(st.getPlayer().getRace() != Race.DARKELF)
					htmltext = "30145-00.htm";
				else if(st.getPlayer().getLevel() >= 15)
					htmltext = "30145-03.htm";
				else
					htmltext = "30145-02.htm";
			}
			else if(cond == 1)
			{
				if(st.getQuestItemsCount(CrackedSkull) == 0)
					htmltext = "30145-05.htm";
				else
					htmltext = "30145-06.htm";
			}
			else if(cond == 2)
				htmltext = "30145-07.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 1)
		{
			if(Rnd.chance(20) && st.getQuestItemsCount(PerfectSkull) == 0)
			{
				st.giveItems(PerfectSkull, 1, false, false);
				st.setCond(2);
			}
			if(Rnd.chance(70))
			{
				st.giveItems(CrackedSkull, 1, true, true);
				st.playSound(SOUND_ITEMGET);
			}
		}
		return null;
	}
}
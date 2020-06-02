package quests;

import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;

public class _061_LawEnforcement extends QuestScript
{
	/**
	 * The one who knows everything
	 * Visit Kekropus in Kamael Village to learn more about the Inspector and Judicator.
	 */
	private static final int COND1 = 1;
	/**
	 * Nostra's Successor
	 * It is said that Nostra's successor is in Kamael Village. He is the first Inspector and master of souls. Find him.
	 */
	private static final int COND2 = 2;

	private static final int Liane = 32222;
	private static final int Kekropus = 32138;
	private static final int Eindburgh = 32469;

	public _061_LawEnforcement()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(Liane);
		addTalkId(Kekropus, Eindburgh);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("ask"))
		{
			if(st.getPlayer().getRace() != Race.KAMAEL)
				htmltext = "grandmaste_piane_q0061_03.htm";
			else if(st.getPlayer().getClassId() != ClassId.INSPECTOR || st.getPlayer().getLevel() < 76)
				htmltext = "grandmaste_piane_q0061_02.htm";
			else
				htmltext = "grandmaste_piane_q0061_04.htm";
		}
		else if(event.equals("accept"))
		{
			st.setCond(COND1);
			htmltext = "grandmaste_piane_q0061_05.htm";
		}
		else if(event.equals("kekrops_q0061_09.htm"))
			st.setCond(COND2);
		else if(event.equals("subelder_aientburg_q0061_08.htm") || event.equals("subelder_aientburg_q0061_09.htm"))
		{
			st.giveItems(ADENA_ID, 26000, true, true);
			st.getPlayer().setClassId(ClassId.JUDICATOR.ordinal(), false, true);
			st.getPlayer().broadcastCharInfo();
			st.getPlayer().broadcastPacket(new MagicSkillUse(st.getPlayer(), 4339, 1, 6000, 1));
			st.getPlayer().broadcastPacket(new MagicSkillUse(npc, 4339, 1, 6000, 1));
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

		if(npcId == Liane)
		{
			if(cond == 0)
				htmltext = "grandmaste_piane_q0061_01.htm";
			else
				htmltext = "grandmaste_piane_q0061_06.htm";
		}
		else if(npcId == Kekropus)
		{
			if(cond == COND1)
				htmltext = "kekrops_q0061_01.htm";
			else if(cond == COND2)
				htmltext = "kekrops_q0061_10.htm";
		}
		else if(npcId == Eindburgh && cond == COND2)
			htmltext = "subelder_aientburg_q0061_01.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		return null;
	}
}
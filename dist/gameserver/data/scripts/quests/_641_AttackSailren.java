package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _641_AttackSailren extends QuestScript
{
	//NPC
	private static int STATUE = 32109;

	//MOBS
	private static int VEL1 = 22196;
	private static int VEL2 = 22197;
	private static int VEL3 = 22198;
	private static int VEL4 = 22218;
	private static int VEL5 = 22223;
	private static int PTE = 22199;
	//items
	private static int FRAGMENTS = 8782;
	private static int GAZKH = 8784;

	public _641_AttackSailren()
	{
		super(PARTY_ONE, REPEATABLE);

		addStartNpc(STATUE);

		addKillId(VEL1);
		addKillId(VEL2);
		addKillId(VEL3);
		addKillId(VEL4);
		addKillId(VEL5);
		addKillId(PTE);

		addQuestItem(FRAGMENTS);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("statue_of_shilen_q0641_05.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("statue_of_shilen_q0641_08.htm"))
		{
			st.takeItems(FRAGMENTS, -1);
			st.giveItems(GAZKH, 1, false, false);
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(cond == 0)
		{
			if(!st.getPlayer().isQuestCompleted(126) || st.getPlayer().getLevel() < 77)
				htmltext = "statue_of_shilen_q0641_02.htm";
			else
				htmltext = "statue_of_shilen_q0641_01.htm";
		}
		else if(cond == 1)
			htmltext = "statue_of_shilen_q0641_05.htm";
		else if(cond == 2)
			htmltext = "statue_of_shilen_q0641_07.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getQuestItemsCount(FRAGMENTS) < 30)
		{
			st.giveItems(FRAGMENTS, 1, true, true);
			if(st.getQuestItemsCount(FRAGMENTS) >= 30)
			{
				st.setCond(2);
			}
			else
				st.playSound(SOUND_ITEMGET);
		}
		return null;
	}
}
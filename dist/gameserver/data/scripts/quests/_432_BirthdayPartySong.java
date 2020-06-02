package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _432_BirthdayPartySong extends QuestScript
{
	//NPC
	private static int MELODY_MAESTRO_OCTAVIA = 31043;
	//MOB
	private static int ROUGH_HEWN_ROCK_GOLEMS = 21103;
	//Quest items
	private static int RED_CRYSTALS = 7541;
	private static int BIRTHDAY_ECHO_CRYSTAL = 7061;

	public _432_BirthdayPartySong()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(MELODY_MAESTRO_OCTAVIA);

		addKillId(ROUGH_HEWN_ROCK_GOLEMS);

		addQuestItem(RED_CRYSTALS);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("muzyko_q0432_0104.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("muzyko_q0432_0201.htm"))
			if(st.getQuestItemsCount(RED_CRYSTALS) >= 50)
			{
				st.takeItems(RED_CRYSTALS, -1);
				st.giveItems(BIRTHDAY_ECHO_CRYSTAL, 25, false, false);
				st.finishQuest();
			}
			else
				htmltext = "muzyko_q0432_0202.htm";
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int condition = st.getCond();
		int npcId = npc.getNpcId();
		if(npcId == MELODY_MAESTRO_OCTAVIA)
			if(condition == 0)
			{
				if(st.getPlayer().getLevel() >= 31)
					htmltext = "muzyko_q0432_0101.htm";
				else
					htmltext = "muzyko_q0432_0103.htm";
			}
			else if(condition == 1)
				htmltext = "muzyko_q0432_0106.htm";
			else if(condition == 2 && st.getQuestItemsCount(RED_CRYSTALS) >= 50)
				htmltext = "muzyko_q0432_0105.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(!st.isStarted())
			return null;
		int npcId = npc.getNpcId();

		if(npcId == ROUGH_HEWN_ROCK_GOLEMS)
			if(st.getCond() == 1 && st.getQuestItemsCount(RED_CRYSTALS) < 50)
			{
				st.giveItems(RED_CRYSTALS, 1, true, true);

				if(st.getQuestItemsCount(RED_CRYSTALS) >= 50)
				{
					st.setCond(2);
				}
				else
					st.playSound(SOUND_ITEMGET);
			}
		return null;
	}
}
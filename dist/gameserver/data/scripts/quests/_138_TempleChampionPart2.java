package quests;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _138_TempleChampionPart2 extends QuestScript
{
	// NPCs
	private static final int SYLVAIN = 30070;
	private static final int PUPINA = 30118;
	private static final int ANGUS = 30474;
	private static final int SLA = 30666;

	// ITEMs
	private static final int MANIFESTO = 10341;
	private static final int RELIC = 10342;
	private static final int ANGUS_REC = 10343;
	private static final int PUPINA_REC = 10344;

	// Monsters
	private final static int Wyrm = 20176;
	private final static int GuardianBasilisk = 20550;
	private final static int RoadScavenger = 20551;
	private final static int FetteredSoul = 20552;

	public _138_TempleChampionPart2()
	{
		super(PARTY_NONE, ONETIME);

		// Нет стартового NPC, чтобы квест не появлялся в списке раньше времени
		addFirstTalkId(SYLVAIN);
		addTalkId(SYLVAIN, PUPINA, ANGUS, SLA);
		addKillId(Wyrm, GuardianBasilisk, RoadScavenger, FetteredSoul);
		addQuestItem(MANIFESTO, RELIC, ANGUS_REC, PUPINA_REC);
	}

	@Override
	public String onFirstTalk(NpcInstance npc, Player player)
	{
		if(player.isQuestCompleted(137) && player.getQuestState(this) == null)
			newQuestState(player);
		return "";
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("sylvain_q0138_04.htm"))
		{
			st.setCond(1);
			st.giveItems(MANIFESTO, 1, false, false);
		}
		else if(event.equalsIgnoreCase("sylvain_q0138_09.htm"))
		{
			st.addExpAndSp(187062, 11307);
			st.giveItems(ADENA_ID, 84593, true, true);
			st.finishQuest();
		}
		else if(event.equalsIgnoreCase("sylvain_q0138_06.htm"))
		{
			st.setCond(2);
		}
		else if(event.equalsIgnoreCase("pupina_q0138_08.htm"))
		{
			st.setCond(3);
		}
		else if(event.equalsIgnoreCase("pupina_q0138_11.htm"))
		{
			st.setCond(6);
			st.set("talk", "0");
			st.giveItems(PUPINA_REC, 1, false, false);
		}
		else if(event.equalsIgnoreCase("grandmaster_angus_q0138_03.htm"))
		{
			st.setCond(4);
		}
		else if(event.equalsIgnoreCase("preacher_sla_q0138_03.htm"))
		{
			st.set("talk", "1");
			st.takeItems(PUPINA_REC, -1);
		}
		else if(event.equalsIgnoreCase("preacher_sla_q0138_05.htm"))
		{
			st.set("talk", "2");
			st.takeItems(MANIFESTO, -1);
		}
		else if(event.equalsIgnoreCase("preacher_sla_q0138_12.htm"))
		{
			st.setCond(7);
			st.unset("talk");
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == SYLVAIN)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 36)
					htmltext = "sylvain_q0138_01.htm";
				else
					htmltext = "sylvain_q0138_03.htm";
			}
			else if(cond == 1)
				htmltext = "sylvain_q0138_04.htm";
			else if(cond >= 2 && cond <= 6)
				htmltext = "sylvain_q0138_06.htm";
			else if(cond == 7)
				htmltext = "sylvain_q0138_08.htm";
		}
		else if(npcId == PUPINA)
		{
			if(cond == 2)
				htmltext = "pupina_q0138_02.htm";
			else if(cond == 3 || cond == 4)
				htmltext = "pupina_q0138_09.htm";
			else if(cond == 5)
			{
				htmltext = "pupina_q0138_10.htm";
				st.takeItems(ANGUS_REC, -1);
			}
			else if(cond == 6)
				htmltext = "pupina_q0138_13.htm";
		}
		else if(npcId == ANGUS)
		{
			if(cond == 3)
				htmltext = "grandmaster_angus_q0138_02.htm";
			else if(cond == 4)
			{
				if(st.getQuestItemsCount(RELIC) >= 10)
				{
					htmltext = "grandmaster_angus_q0138_05.htm";
					st.takeItems(RELIC, -1);
					st.giveItems(ANGUS_REC, 1, false, false);
					st.setCond(5);
				}
				else
					htmltext = "grandmaster_angus_q0138_04.htm";
			}
			else if(cond == 5)
				htmltext = "grandmaster_angus_q0138_06.htm";
		}
		else if(npcId == SLA)
		{
			if(cond == 6)
			{
				if(st.getInt("talk") == 0)
					htmltext = "preacher_sla_q0138_02.htm";
				else if(st.getInt("talk") == 1)
					htmltext = "preacher_sla_q0138_03.htm";
				else if(st.getInt("talk") == 2)
					htmltext = "preacher_sla_q0138_05.htm";
			}
			else if(cond == 7)
				htmltext = "preacher_sla_q0138_13.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 4)
		{
			if(st.getQuestItemsCount(RELIC) < 10)
			{
				st.giveItems(RELIC, 1, true, true);
				if(st.getQuestItemsCount(RELIC) >= 10)
					st.playSound(SOUND_MIDDLE);
				else
					st.playSound(SOUND_ITEMGET);
			}
		}
		return null;
	}
}
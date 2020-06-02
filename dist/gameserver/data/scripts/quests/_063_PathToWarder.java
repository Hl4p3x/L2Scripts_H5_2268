package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _063_PathToWarder extends QuestScript
{

	int Sione = 32195;
	int Gobie = 32198;
	int Patrol = 20053;
	int Novice = 20782;
	int Bathis = 30332;
	int Tobias = 30297;
	int Tak = 27337;
	int Maille = 20919;
	int Maille_scout = 20920;
	int Maille_guard = 20921;

	int OlMahumOrders = 9762;
	int OlMahumOrganizationChart = 9763;
	int GobiesOrders = 9764;
	int LettertotheHumans = 9765;
	int HumansReply = 9766;
	int LettertotheDarkElves = 9767;
	int DarkElvesReply = 9768;
	int ReporttoSione = 9769;
	int EmptySoulCrystal = 9770; //empty
	int TaksCapturedSoul = 9771;
	int SteelrazorEvaluation = 9772;

	public _063_PathToWarder()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(Sione);
		addTalkId(Sione);
		addTalkId(Gobie);
		addTalkId(Bathis);
		addTalkId(Tobias);
		addKillId(Patrol);
		addKillId(Novice);
		addKillId(Tak);
		addKillId(Maille);
		addKillId(Maille_scout);
		addKillId(Maille_guard);
		addQuestItem(new int[]{
				OlMahumOrganizationChart,
				OlMahumOrders,
				TaksCapturedSoul,
				EmptySoulCrystal
		});
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("master_sione_q0063_06.htm"))
		{
			st.setCond(1);
		}
		if(event.equals("master_sione_q0063_08.htm"))
			st.setCond(2);
		else if(event.equals("captain_bathia_q0063_04.htm"))
		{
			st.takeItems(LettertotheHumans, 1);
			st.giveItems(HumansReply, 1, false, false);
			st.setCond(6);
		}
		else if(event.equals("master_gobie_q0063_08.htm"))
		{
			st.takeItems(HumansReply, 1);
			st.giveItems(LettertotheDarkElves, 1, false, false);
			st.setCond(7);
		}
		else if(event.equals("master_tobias_q0063_05.htm"))
		{
			st.takeItems(LettertotheDarkElves, 1);
			st.giveItems(DarkElvesReply, 1, false, false);
			st.setCond(8);
		}
		else if(event.equals("master_gobie_q0063_11.htm"))
		{
			st.takeItems(DarkElvesReply, 1);
			st.giveItems(ReporttoSione, 1, false, false);
			st.setCond(9);
		}
		else if(event.equals("master_gobie_q0063_16.htm"))
		{
			st.takeItems(EmptySoulCrystal, 1);
			st.setCond(11);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Sione)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getClassId() != ClassId.KAMAEL_F_SOLDIER)
					htmltext = "master_sione_q0063_04.htm";
				else if(st.getPlayer().getLevel() < 18)
					htmltext = "master_sione_q0063_02.htm";
				else
					htmltext = "master_sione_q0063_05.htm";
			}
			else if(cond == 1)
				htmltext = "master_sione_q0063_06.htm";
			else if(cond == 3)
			{
				if(st.getQuestItemsCount(OlMahumOrders) < 10 && st.getQuestItemsCount(OlMahumOrganizationChart) < 5)
					htmltext = "master_sione_q0063_09.htm";
				else
				{
					htmltext = "master_sione_q0063_10.htm";
					st.setCond(4);
					st.takeItems(OlMahumOrders, -1);
					st.takeItems(OlMahumOrganizationChart, -1);
					st.giveItems(GobiesOrders, 1, false, false);
				}
			}
			else if(cond == 9)
			{
				st.takeItems(ReporttoSione, 1);
				st.setCond(10);
				htmltext = "master_sione_q0063_13.htm";
			}
		}
		if(npcId == Gobie)
		{
			if(cond == 4)
				if(st.getQuestItemsCount(GobiesOrders) < 1)
					htmltext = "master_gobie_q0063_01.htm";
				else
				{
					htmltext = "master_gobie_q0063_03.htm";
					st.takeItems(GobiesOrders, 1);
					st.giveItems(LettertotheHumans, 1, false, false);
					st.setCond(5);
				}
			if(cond == 6)
				htmltext = "master_gobie_q0063_05.htm";
			if(cond == 8)
				htmltext = "master_gobie_q0063_10.htm";
			if(cond == 10)
				htmltext = "master_gobie_q0063_14.htm";
			if(cond == 11)
				htmltext = "master_gobie_q0063_17.htm";
			if(cond == 12)
				if(st.getQuestItemsCount(TaksCapturedSoul) > 0)
				{
					st.takeItems(TaksCapturedSoul, 1);
					if(st.getPlayer().getClassId().isOfLevel(ClassLevel.NONE))
					{
						st.giveItems(SteelrazorEvaluation, 1, false, false);
						if(!st.getPlayer().getVarBoolean("prof1"))
						{
							st.getPlayer().setVar("prof1", "1", -1);
							st.addExpAndSp(160267, 11023);
							st.giveItems(ADENA_ID, 163800, true, true);
							//FIXME [G1ta0] дать адены, только если первый чар на акке	
						}
					}
					st.finishQuest();
					htmltext = "master_gobie_q0063_20.htm";
				}
				else
					htmltext = "master_gobie_q0063_19.htm";
		}
		if(npcId == Bathis && cond == 5)
			htmltext = "captain_bathia_q0063_01.htm";
		if(npcId == Tobias && cond == 7)
			htmltext = "master_tobias_q0063_01.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 2)
		{
			if(npcId == Patrol)
			{
				st.giveItems(OlMahumOrganizationChart, 1, true, true);
				st.playSound(SOUND_ITEMGET);
			}
			else if(npcId == Novice)
			{
				st.giveItems(OlMahumOrders, 1, true, true);
				st.playSound(SOUND_ITEMGET);
			}
			if(st.getQuestItemsCount(OlMahumOrders) > 9 && st.getQuestItemsCount(OlMahumOrganizationChart) > 4)
			{
				st.setCond(3);
			}
		}
		if(cond == 11)
			if((npcId == Maille || npcId == Maille_scout || npcId == Maille_guard) && Rnd.chance(20))
				st.addSpawn(Tak);
			else if(npcId == Tak)
			{
				st.takeItems(EmptySoulCrystal, 1);
				st.giveItems(TaksCapturedSoul, 1, false, false);
				st.setCond(12);
			}
		return null;
	}
}
package quests;

import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;

/**
 * Квест на вторую профессию The Way Of The Warrior
 *
 * @author Sergey Ibryaev aka Artful
 */

public class _175_TheWayOfTheWarrior extends QuestScript
{
	//NPC
	private static final int Kekropus = 32138;
	private static final int Perwan = 32133;
	//Quest Items
	private static final int WolfTail = 9807;
	private static final int MuertosClaw = 9808;
	//Items
	private static final int WarriorsSword = 9720;
	//MOBs
	private static final int MountainWerewolf = 22235;
	private static final int MountainWerewolfChief = 22235;
	private static final int MuertosArcher = 22236;
	private static final int MuertosGuard = 22239;
	private static final int MuertosScout = 22240;
	private static final int MuertosWarrior = 22242;
	private static final int MuertosCaptain = 22243;
	private static final int MuertosLieutenant = 22245;
	private static final int MuertosCommander = 22246;

	//Drop Cond
	//# [COND, NEWCOND, ID, REQUIRED, ITEM, NEED_COUNT, CHANCE, DROP]
	private static final int[][] DROPLIST_COND = {
			{
					2,
					3,
					MountainWerewolf,
					0,
					WolfTail,
					5,
					35,
					1
			},
			{
					2,
					3,
					MountainWerewolfChief,
					0,
					WolfTail,
					5,
					40,
					1
			},
			{
					7,
					8,
					MuertosArcher,
					0,
					MuertosClaw,
					10,
					32,
					1
			},
			{
					7,
					8,
					MuertosGuard,
					0,
					MuertosClaw,
					10,
					44,
					1
			},
			{
					7,
					8,
					MuertosScout,
					0,
					MuertosClaw,
					10,
					48,
					1
			},
			{
					7,
					8,
					MuertosWarrior,
					0,
					MuertosClaw,
					10,
					56,
					1
			},
			{
					7,
					8,
					MuertosCaptain,
					0,
					MuertosClaw,
					10,
					60,
					1
			},
			{
					7,
					8,
					MuertosLieutenant,
					0,
					MuertosClaw,
					10,
					68,
					1
			},
			{
					7,
					8,
					MuertosCommander,
					0,
					MuertosClaw,
					10,
					72,
					1
			}
	};

	public _175_TheWayOfTheWarrior()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(Kekropus);

		addTalkId(Perwan);

		for(int i = 0; i < DROPLIST_COND.length; i++)
			addKillId(DROPLIST_COND[i][2]);

		addQuestItem(WolfTail);
		addQuestItem(MuertosClaw);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("32138-04.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("32138-08.htm"))
		{
			st.takeItems(MuertosClaw, -1);

			st.giveItems(WarriorsSword, 1, false, false);
			st.giveItems(ADENA_ID, 8799, true, true);
			st.addExpAndSp(20739, 1777);

			if(st.getPlayer().getClassId().isOfLevel(ClassLevel.NONE) && !st.getPlayer().getVarBoolean("p1q3"))
			{
				st.getPlayer().setVar("p1q3", "1", -1); // flag for helper
				st.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.NONE, 5000, ScreenMessageAlign.TOP_CENTER, true, "Now go find the Newbie Guide."));
				st.giveItems(1060, 100, false, false); // healing potion
				for(int item = 4412; item <= 4417; item++)
					st.giveItems(item, 10, false, false); // echo cry
				st.playTutorialVoice("tutorial_voice_026");
				st.giveItems(5789, 7000, true, true); // newbie ss
			}
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
		if(npcId == Kekropus)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() != Race.KAMAEL)
					htmltext = "32138-00.htm";
				else if(st.getPlayer().getLevel() < 10)
					htmltext = "32138-01.htm";
				else
					htmltext = "32138-02.htm";
			}
			else if(cond == 1)
				htmltext = "32138-04.htm";
			else if(cond == 4)
			{
				st.setCond(5);
				htmltext = "32138-05.htm";
			}
			else if(cond == 6)
			{
				st.setCond(7);
				htmltext = "32138-06.htm";
			}
			else if(cond == 8)
				htmltext = "32138-07.htm";
		}
		else if(npcId == Perwan)
			if(cond == 1)
			{
				st.setCond(2);
				htmltext = "32133-01.htm";
			}
			else if(cond == 3)
			{
				st.takeItems(WolfTail, -1);
				st.setCond(4);
				htmltext = "32133-02.htm";
			}
			else if(cond == 5)
			{
				st.setCond(6);
				htmltext = "32133-03.htm";
			}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		for(int i = 0; i < DROPLIST_COND.length; i++)
			if(cond == DROPLIST_COND[i][0] && npcId == DROPLIST_COND[i][2])
				if(DROPLIST_COND[i][3] == 0 || st.getQuestItemsCount(DROPLIST_COND[i][3]) > 0)
					if(DROPLIST_COND[i][5] == 0)
						st.rollAndGive(DROPLIST_COND[i][4], DROPLIST_COND[i][7], DROPLIST_COND[i][6]);
					else if(st.rollAndGive(DROPLIST_COND[i][4], DROPLIST_COND[i][7], DROPLIST_COND[i][7], DROPLIST_COND[i][5], DROPLIST_COND[i][6]))
						if(DROPLIST_COND[i][1] != cond && DROPLIST_COND[i][1] != 0)
						{
							st.setCond(Integer.valueOf(DROPLIST_COND[i][1]));
						}
		return null;
	}
}
package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * Квест Keen Claws
 *
 * @author Sergey Ibryaev aka Artful
 */

public class _264_KeenClaws extends QuestScript
{
	//NPC
	private static final int Payne = 30136;
	//Quest Items
	private static final int WolfClaw = 1367;
	//Items
	private static final int LeatherSandals = 36;
	private static final int WoodenHelmet = 43;
	private static final int Stockings = 462;
	private static final int HealingPotion = 1061;
	private static final int ShortGloves = 48;
	private static final int ClothShoes = 35;
	//MOB
	private static final int Goblin = 20003;
	private static final int AshenWolf = 20456;
	//Drop Cond
	//# [COND, NEWCOND, ID, REQUIRED, ITEM, NEED_COUNT, CHANCE, DROP]
	private static final int[][] DROPLIST_COND = {
			{
					1,
					2,
					Goblin,
					0,
					WolfClaw,
					50,
					50,
					2
			},
			{
					1,
					2,
					AshenWolf,
					0,
					WolfClaw,
					50,
					50,
					2
			}
	};

	public _264_KeenClaws()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(Payne);

		addKillId(Goblin);
		addKillId(AshenWolf);

		addQuestItem(WolfClaw);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("paint_q0264_03.htm"))
		{
			st.setCond(1);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Payne)
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 3)
					htmltext = "paint_q0264_02.htm";
				else
					htmltext = "paint_q0264_01.htm";
			}
			else if(cond == 1)
				htmltext = "paint_q0264_04.htm";
			else if(cond == 2)
			{
				st.takeItems(WolfClaw, -1);
				int n = Rnd.get(17);
				if(n == 0)
				{
					st.giveItems(WoodenHelmet, 1, false, false);
					st.playSound(SOUND_JACKPOT);
				}
				else if(n < 2)
					st.giveItems(ADENA_ID, 1000, true, true);
				else if(n < 5)
					st.giveItems(LeatherSandals, 1, false, false);
				else if(n < 8)
				{
					st.giveItems(Stockings, 1, false, false);
					st.giveItems(ADENA_ID, 50, true, true);
				}
				else if(n < 11)
					st.giveItems(HealingPotion, 1, false, false);
				else if(n < 14)
					st.giveItems(ShortGloves, 1, false, false);
				else
					st.giveItems(ClothShoes, 1, false, false);
				htmltext = "paint_q0264_05.htm";
				st.finishQuest();
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
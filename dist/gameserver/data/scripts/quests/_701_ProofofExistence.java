package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * @author: pchayka
 * @date: 22.06.2010
 */
public class _701_ProofofExistence extends QuestScript
{
	// NPC's
	private static int Artius = 32559;

	// ITEMS
	private static int DeadmansRemains = 13875;
	private static int BansheeQueensEye = 13876;

	// MOB's
	private static int Enira = 25625;
	private static int FloatingSkull1 = 22606;
	private static int FloatingSkull2 = 22607;
	private static int FloatingZombie1 = 22608;
	private static int FloatingZombie2 = 22609;

	public _701_ProofofExistence()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(Artius);
		addTalkId(Artius);
		addKillId(Enira, FloatingSkull1, FloatingSkull2, FloatingZombie1, FloatingZombie2);
		addQuestItem(DeadmansRemains, BansheeQueensEye);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int cond = st.getCond();
		String htmltext = event;

		if(event.equals("artius_q701_2.htm") && cond == 0)
		{
			st.setCond(1);
		}
		else if(event.equals("ex_mons") && cond == 1)
		{
			if(st.getQuestItemsCount(DeadmansRemains) >= 1)
			{
				st.giveItems(ADENA_ID, st.getQuestItemsCount(DeadmansRemains) * 2500, true, true); // умножается на рейт квестов
				st.takeItems(DeadmansRemains, -1);
				htmltext = "artius_q701_4.htm";
			}
			else
				htmltext = "artius_q701_3a.htm";
		}
		else if(event.equals("ex_boss") && cond == 1)
			if(st.getQuestItemsCount(BansheeQueensEye) >= 1)
			{
				st.giveItems(ADENA_ID, st.getQuestItemsCount(BansheeQueensEye) * 1000000, true, true); // умножается на рейт квестов
				st.takeItems(BansheeQueensEye, -1);
				htmltext = "artius_q701_4.htm";
			}
			else
				htmltext = "artius_q701_3a.htm";
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Artius)
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 78 && st.getPlayer().isQuestCompleted(10273))
					htmltext = "artius_q701_1.htm";
				else
					htmltext = "artius_q701_0.htm";
			}
			else if(cond == 1)
				if(st.getQuestItemsCount(DeadmansRemains) >= 1 || st.getQuestItemsCount(BansheeQueensEye) >= 1)
					htmltext = "artius_q701_3.htm";
				else
					htmltext = "artius_q701_3a.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 1 && npcId != Enira)
		{
			st.giveItems(DeadmansRemains, 1, true, true);
			st.playSound(SOUND_ITEMGET);
		}
		else if(cond == 1 && npcId == Enira)
		{
			st.giveItems(BansheeQueensEye, 1, false, false);
			st.playSound(SOUND_ITEMGET);
		}
		return null;
	}
}
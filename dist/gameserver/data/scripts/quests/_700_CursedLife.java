package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * @author: pchayka
 * @date: 22.06.2010
 */
public class _700_CursedLife extends QuestScript
{
	// NPC's
	private static int Orbyu = 32560;

	// ITEMS
	private static int SwallowedSkull = 13872;
	private static int SwallowedSternum = 13873;
	private static int SwallowedBones = 13874;

	// MOB's
	private static int MutantBird1 = 22602;
	private static int MutantBird2 = 22603;
	private static int DraHawk1 = 22604;
	private static int DraHawk2 = 22605;
	private static int Rok = 25624;

	//Prices
	private static int _skullprice = 50000;
	private static int _sternumprice = 5000;
	private static int _bonesprice = 500;

	public _700_CursedLife()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(Orbyu);
		addTalkId(Orbyu);
		addKillId(MutantBird1, MutantBird2, DraHawk1, DraHawk2);
		addQuestItem(SwallowedSkull, SwallowedSternum, SwallowedBones);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int cond = st.getCond();
		String htmltext = event;

		if(event.equals("orbyu_q700_2.htm") && cond == 0)
		{
			st.setCond(1);
		}
		else if(event.equals("ex_bones") && cond == 1)
			if(st.getQuestItemsCount(SwallowedSkull) >= 1 || st.getQuestItemsCount(SwallowedSternum) >= 1 || st.getQuestItemsCount(SwallowedBones) >= 1)
			{
				long _adenatogive = st.getQuestItemsCount(SwallowedSkull) * _skullprice + st.getQuestItemsCount(SwallowedSternum) * _sternumprice + st.getQuestItemsCount(SwallowedBones) * _bonesprice;

				st.giveItems(ADENA_ID, _adenatogive, true, true);
				if(st.getQuestItemsCount(SwallowedSkull) >= 1)
					st.takeItems(SwallowedSkull, -1);
				if(st.getQuestItemsCount(SwallowedSternum) >= 1)
					st.takeItems(SwallowedSternum, -1);
				if(st.getQuestItemsCount(SwallowedBones) >= 1)
					st.takeItems(SwallowedBones, -1);
				htmltext = "orbyu_q700_4.htm";
			}
			else
				htmltext = "orbyu_q700_3a.htm";
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Orbyu)
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 75 && st.getPlayer().isQuestCompleted(10273))
					htmltext = "orbyu_q700_1.htm";
				else
					htmltext = "orbyu_q700_0.htm";
			}
			else if(cond == 1)
				if(st.getQuestItemsCount(SwallowedSkull) >= 1 || st.getQuestItemsCount(SwallowedSternum) >= 1 || st.getQuestItemsCount(SwallowedBones) >= 1)
					htmltext = "orbyu_q700_3.htm";
				else
					htmltext = "orbyu_q700_3a.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 1)
			if(npcId == MutantBird1 || npcId == MutantBird2 || npcId == DraHawk1 || npcId == DraHawk2)
			{
				st.giveItems(SwallowedBones, 1, true, true);
				st.playSound(SOUND_ITEMGET);
				if(Rnd.chance(20))
					st.giveItems(SwallowedSkull, 1, true, true);
				else if(Rnd.chance(20))
					st.giveItems(SwallowedSternum, 1, true, true);
			}
			else if(npcId == Rok)
			{
				st.giveItems(SwallowedSternum, 50, true, true);
				st.giveItems(SwallowedSkull, 30, true, true);
				st.giveItems(SwallowedBones, 100, true, true);
				st.playSound(SOUND_ITEMGET);
			}
		return null;
	}
}
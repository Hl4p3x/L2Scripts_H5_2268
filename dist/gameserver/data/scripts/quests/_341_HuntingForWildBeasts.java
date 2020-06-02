package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _341_HuntingForWildBeasts extends QuestScript
{
	//NPCs
	private static int PANO = 30078;
	//Mobs
	private static int Red_Bear = 20021;
	private static int Dion_Grizzly = 20203;
	private static int Brown_Bear = 20310;
	private static int Grizzly_Bear = 20335;
	//Quest Items
	private static int BEAR_SKIN = 4259;
	//Chances
	private static int BEAR_SKIN_CHANCE = 40;

	public _341_HuntingForWildBeasts()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(PANO);
		addKillId(Red_Bear);
		addKillId(Dion_Grizzly);
		addKillId(Brown_Bear);
		addKillId(Grizzly_Bear);
		addQuestItem(BEAR_SKIN);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("quest_accept"))
		{
			htmltext = "pano_q0341_04.htm";
			st.setCond(1);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		if(npc.getNpcId() != PANO)
			return htmltext;

		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 20)
				htmltext = "pano_q0341_01.htm";
			else
				htmltext = "pano_q0341_02.htm";
		}
		else
		{
			if(st.getQuestItemsCount(BEAR_SKIN) >= 20)
			{
				htmltext = "pano_q0341_05.htm";
				st.takeItems(BEAR_SKIN, -1);
				st.giveItems(ADENA_ID, 3710, true, true);
				st.finishQuest();
			}
			else
				htmltext = "pano_q0341_06.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(!qs.isStarted())
			return null;

		long BEAR_SKIN_COUNT = qs.getQuestItemsCount(BEAR_SKIN);
		if(BEAR_SKIN_COUNT < 20 && Rnd.chance(BEAR_SKIN_CHANCE))
		{
			qs.giveItems(BEAR_SKIN, 1, true, true);
			if(BEAR_SKIN_COUNT >= 20)
			{
				qs.setCond(2);
			}
			else
				qs.playSound(SOUND_ITEMGET);
		}
		return null;
	}
}
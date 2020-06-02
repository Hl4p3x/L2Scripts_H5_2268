package quests;

import org.apache.commons.lang3.ArrayUtils;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * @author pchayka
 */

public class _290_ThreatRemoval extends QuestScript
{
	private static final int GuardPinaps = 30201;
	private static final int[] SelMahumTrainers = {
			22775,
			22776,
			22777,
			22778
	};
	private static final int[] SelMahumRecruits = {
			22780,
			22781,
			22782,
			22783,
			22784,
			22785
	};
	private static final int SelMahumIDTag = 15714;

	public _290_ThreatRemoval()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(GuardPinaps);
		addKillId(SelMahumTrainers);
		addKillId(SelMahumRecruits);
		addQuestItem(SelMahumIDTag);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("pinaps_q290_02.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("pinaps_q290_05.htm"))
		{
			st.takeItems(SelMahumIDTag, 400);
			switch(Rnd.get(1, 6))
			{
				case 1:
					st.giveItems(959, 1, false, false);
					break;
				case 2:
					st.giveItems(960, 1, false, false);
					break;
				case 3:
					st.giveItems(960, 2, false, false);
					break;
				case 4:
					st.giveItems(960, 3, false, false);
					break;
				case 5:
					st.giveItems(9552, 1, false, false);
					break;
				case 6:
					st.giveItems(9552, 2, false, false);
					break;
			}
			st.playSound(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("continue"))
		{
			htmltext = "pinaps_q290_06.htm";
		}
		else if(event.equalsIgnoreCase("quit"))
		{
			htmltext = "pinaps_q290_07.htm";
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(npc.getNpcId() == GuardPinaps)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 82 && st.getPlayer().isQuestCompleted(251))
					htmltext = "pinaps_q290_01.htm";
				else
					htmltext = "pinaps_q290_00.htm";
			}
			else if(cond == 1 && st.getQuestItemsCount(SelMahumIDTag) < 400)
				htmltext = "pinaps_q290_03.htm";
			else if(cond == 1 && st.getQuestItemsCount(SelMahumIDTag) >= 400)
				htmltext = "pinaps_q290_04.htm";
		}

		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 1)
		{
			if(ArrayUtils.contains(SelMahumTrainers, npc.getNpcId()))
				st.rollAndGive(SelMahumIDTag, 1, 93.2);
			else if(ArrayUtils.contains(SelMahumRecruits, npc.getNpcId()))
				st.rollAndGive(SelMahumIDTag, 1, 36.3);
		}
		return null;
	}
}
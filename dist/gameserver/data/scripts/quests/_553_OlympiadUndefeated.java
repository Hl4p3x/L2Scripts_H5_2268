package quests;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.olympiad.OlympiadGame;
import l2s.gameserver.model.entity.olympiad.OlympiadTeam;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _553_OlympiadUndefeated extends QuestScript
{
	// NPCs
	private static final int OLYMPIAD_MANAGER = 31688;

	// Items
	private static final int MEDAL_OF_GLORY = 21874;
	private static final int OLYMPIAD_CHEST = 17169;
	private static final int WINS_CONFIRMATION1 = 17244;
	private static final int WINS_CONFIRMATION2 = 17245;
	private static final int WINS_CONFIRMATION3 = 17246;

	public _553_OlympiadUndefeated()
	{
		super(PARTY_NONE, DAILY);

		addStartNpc(OLYMPIAD_MANAGER);
		addTalkId(OLYMPIAD_MANAGER);
		addQuestItem(WINS_CONFIRMATION1, WINS_CONFIRMATION2, WINS_CONFIRMATION3);
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		switch(npcId)
		{
			case OLYMPIAD_MANAGER:
				Player player = st.getPlayer();
				if(!player.isNoble() || player.getLevel() < 75 || player.getClassLevel() < 3)
					return "olympiad_operator_q0553_08.htm";

				if(st.isNotAccepted())
					return "olympiad_operator_q0553_01.htm";
				else if(st.isStarted())
				{
					if(st.getQuestItemsCount(WINS_CONFIRMATION1, WINS_CONFIRMATION2, WINS_CONFIRMATION3) == 0)
						return "olympiad_operator_q0553_04.htm";

					if(st.getQuestItemsCount(WINS_CONFIRMATION3) > 0)
					{
						st.giveItems(OLYMPIAD_CHEST, 6, false, false);
						st.giveItems(MEDAL_OF_GLORY, 5, false, false);
						st.takeItems(WINS_CONFIRMATION1, -1);
						st.takeItems(WINS_CONFIRMATION2, -1);
						st.takeItems(WINS_CONFIRMATION3, -1);
						st.finishQuest();
						return "olympiad_operator_q0553_07.htm";
					}
					else
						return "olympiad_operator_q0553_05.htm";
				}
				break;
		}

		return null;
	}

	@Override
	public String onCompleted(NpcInstance npc, QuestState st)
	{
		String htmltext = COMPLETED_DIALOG;
		int npcId = npc.getNpcId();
		if(npcId == OLYMPIAD_MANAGER)
			htmltext = "olympiad_operator_q0553_06.htm";
		return htmltext;
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("olympiad_operator_q0553_03.htm"))
		{
			st.setCond(1);
		}
		if(event.equalsIgnoreCase("olympiad_operator_q0553_07.htm"))
		{
			if(st.getQuestItemsCount(WINS_CONFIRMATION3) > 0)
			{
				st.giveItems(OLYMPIAD_CHEST, 6, false, false);
				st.giveItems(MEDAL_OF_GLORY, 5, false, false);
				st.takeItems(WINS_CONFIRMATION1, -1);
				st.takeItems(WINS_CONFIRMATION2, -1);
				st.takeItems(WINS_CONFIRMATION3, -1);
				st.finishQuest();
			}
			else if(st.getQuestItemsCount(WINS_CONFIRMATION2) > 0)
			{
				st.giveItems(OLYMPIAD_CHEST, 3, false, false);
				st.giveItems(MEDAL_OF_GLORY, 3, false, false); // от балды
				st.takeItems(WINS_CONFIRMATION1, -1);
				st.takeItems(WINS_CONFIRMATION2, -1);
				st.takeItems(WINS_CONFIRMATION3, -1);
				st.finishQuest();
			}
			else if(st.getQuestItemsCount(WINS_CONFIRMATION1) > 0)
			{
				st.giveItems(OLYMPIAD_CHEST, 1, false, false);
				st.takeItems(WINS_CONFIRMATION1, -1);
				st.takeItems(WINS_CONFIRMATION2, -1);
				st.takeItems(WINS_CONFIRMATION3, -1);
				st.finishQuest();
			}
		}
		return event;
	}

	@Override
	public void onOlympiadEnd(OlympiadGame og, QuestState qs)
	{
		if(qs.getCond() == 1)
		{
			int count = qs.getInt("count");
			OlympiadTeam winner = og.getWinnerTeam();
			if(winner != null && winner.contains(qs.getPlayer().getObjectId()))
				count++;
			else
				count = 0;

			qs.set("count", count);
			if(count == 2 && qs.getQuestItemsCount(WINS_CONFIRMATION1) == 0)
			{
				qs.giveItems(WINS_CONFIRMATION1, 1, false, false);
				qs.playSound(SOUND_ITEMGET);
			}
			else if(count == 5 && qs.getQuestItemsCount(WINS_CONFIRMATION2) == 0)
			{
				qs.giveItems(WINS_CONFIRMATION2, 1, false, false);
				qs.playSound(SOUND_ITEMGET);
			}
			else if(count == 10 && qs.getQuestItemsCount(WINS_CONFIRMATION3) == 0)
			{
				qs.giveItems(WINS_CONFIRMATION3, 2, false, false);
				qs.setCond(2);
			}
			if(count < 10 && qs.getQuestItemsCount(WINS_CONFIRMATION3) > 0)
				qs.takeItems(WINS_CONFIRMATION3, -1);
			if(count < 5 && qs.getQuestItemsCount(WINS_CONFIRMATION2) > 0)
				qs.takeItems(WINS_CONFIRMATION2, -1);
			if(count < 2 && qs.getQuestItemsCount(WINS_CONFIRMATION1) > 0)
				qs.takeItems(WINS_CONFIRMATION1, -1);
		}
	}
}

package quests;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.olympiad.OlympiadGame;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _551_OlympiadStarter extends QuestScript
{
	// NPCs
	private static final int OLYMPIAD_MANAGER = 31688;

	// Items
	private static final int MEDAL_OF_GLORY = 21874;
	private static final int OLYMPIAD_CHEST = 17169;
	private static final int OLYMPIAD_CERT1 = 17238;
	private static final int OLYMPIAD_CERT2 = 17239;
	private static final int OLYMPIAD_CERT3 = 17240;

	public _551_OlympiadStarter()
	{
		super(PARTY_NONE, DAILY);

		addStartNpc(OLYMPIAD_MANAGER);
		addTalkId(OLYMPIAD_MANAGER);
		addQuestItem(OLYMPIAD_CERT1, OLYMPIAD_CERT2, OLYMPIAD_CERT3);
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
					return "olympiad_operator_q0551_08.htm";

				if(st.isNotAccepted())
					return "olympiad_operator_q0551_01.htm";

				if(st.getQuestItemsCount(OLYMPIAD_CERT1, OLYMPIAD_CERT2, OLYMPIAD_CERT3) == 0)
					return "olympiad_operator_q0551_04.htm";

				if(st.getQuestItemsCount(OLYMPIAD_CERT3) > 0)
				{
					st.giveItems(OLYMPIAD_CHEST, 4, false, false);
					st.giveItems(MEDAL_OF_GLORY, 5, false, false);
					st.takeItems(OLYMPIAD_CERT1, -1);
					st.takeItems(OLYMPIAD_CERT2, -1);
					st.takeItems(OLYMPIAD_CERT3, -1);
					st.finishQuest();
					return "olympiad_operator_q0551_07.htm";
				}
				return "olympiad_operator_q0551_05.htm";
		}

		return null;
	}

	@Override
	public String onCompleted(NpcInstance npc, QuestState st)
	{
		String htmltext = COMPLETED_DIALOG;
		int npcId = npc.getNpcId();
		if(npcId == OLYMPIAD_MANAGER)
			htmltext = "olympiad_operator_q0551_06.htm";
		return htmltext;
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("olympiad_operator_q0551_03.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("olympiad_operator_q0551_07.htm"))
		{
			if(st.getQuestItemsCount(OLYMPIAD_CERT3) > 0)
			{
				st.giveItems(OLYMPIAD_CHEST, 4, false, false);
				st.giveItems(MEDAL_OF_GLORY, 5, false, false);
				st.takeItems(OLYMPIAD_CERT1, -1);
				st.takeItems(OLYMPIAD_CERT2, -1);
				st.takeItems(OLYMPIAD_CERT3, -1);
				st.finishQuest();
			}
			else if(st.getQuestItemsCount(OLYMPIAD_CERT2) > 0)
			{
				st.giveItems(OLYMPIAD_CHEST, 2, false, false);
				st.giveItems(MEDAL_OF_GLORY, 3, false, false); // от балды
				st.takeItems(OLYMPIAD_CERT1, -1);
				st.takeItems(OLYMPIAD_CERT2, -1);
				st.takeItems(OLYMPIAD_CERT3, -1);
				st.finishQuest();
			}
			else if(st.getQuestItemsCount(OLYMPIAD_CERT1) > 0)
			{
				st.giveItems(OLYMPIAD_CHEST, 1, false, false);
				//st.giveItems(MEDAL_OF_GLORY, 5); ??
				st.takeItems(OLYMPIAD_CERT1, -1);
				st.takeItems(OLYMPIAD_CERT2, -1);
				st.takeItems(OLYMPIAD_CERT3, -1);
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
			int count = qs.getInt("count") + 1;
			qs.set("count", count);
			if(count == 3)
			{
				qs.giveItems(OLYMPIAD_CERT1, 1, false, false);
				qs.playSound(SOUND_ITEMGET);
			}
			else if(count == 5)
			{
				qs.giveItems(OLYMPIAD_CERT2, 1, false, false);
				qs.playSound(SOUND_ITEMGET);
			}
			else if(count == 10)
			{
				qs.giveItems(OLYMPIAD_CERT3, 1, false, false);
				qs.setCond(2);
			}
		}
	}
}
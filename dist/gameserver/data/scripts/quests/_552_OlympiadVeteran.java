package quests;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.olympiad.OlympiadGame;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _552_OlympiadVeteran extends QuestScript
{
	// NPCs
	private static final int OLYMPIAD_MANAGER = 31688;

	// Items
	private static final int MEDAL_OF_GLORY = 21874;
	private static final int OLYMPIAD_CHEST = 17169;
	private static final int TEAM_CERTIFICATE = 17241;
	private static final int CLASS_FREE_CERTIFICATE = 17242;
	private static final int CLASS_CERTIFICATE = 17243;

	public _552_OlympiadVeteran()
	{
		super(PARTY_NONE, DAILY);

		addStartNpc(OLYMPIAD_MANAGER);
		addTalkId(OLYMPIAD_MANAGER);
		addQuestItem(TEAM_CERTIFICATE, CLASS_FREE_CERTIFICATE, CLASS_CERTIFICATE);
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
					return "olympiad_operator_q0552_08.htm";

				if(st.isNotAccepted())
					return "olympiad_operator_q0552_01.htm";
				else if(st.isStarted())
				{
					if(st.getQuestItemsCount(TEAM_CERTIFICATE, CLASS_FREE_CERTIFICATE, CLASS_CERTIFICATE) == 0)
						return "olympiad_operator_q0552_04.htm";
					else if(st.getQuestItemsCount(TEAM_CERTIFICATE) > 0 && st.getQuestItemsCount(CLASS_FREE_CERTIFICATE) > 0 && st.getQuestItemsCount(CLASS_CERTIFICATE) > 0)
					{
						st.giveItems(OLYMPIAD_CHEST, 3, false, false);
						st.takeItems(TEAM_CERTIFICATE, -1);
						st.takeItems(CLASS_FREE_CERTIFICATE, -1);
						st.takeItems(CLASS_CERTIFICATE, -1);
						st.finishQuest();
						return "olympiad_operator_q0552_07.htm";
					}
					return "olympiad_operator_q0552_05.htm";
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
			htmltext = "olympiad_operator_q0552_06.htm";
		return htmltext;
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("olympiad_operator_q0552_03.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("olympiad_operator_q0552_07.htm"))
		{
			int count = 0;
			if(st.getQuestItemsCount(TEAM_CERTIFICATE) > 0)
				count++;
			if(st.getQuestItemsCount(CLASS_FREE_CERTIFICATE) > 0)
				count++;
			if(st.getQuestItemsCount(CLASS_CERTIFICATE) > 0)
				count++;
			if(count > 0)
			{
				st.giveItems(OLYMPIAD_CHEST, count, false, false);
				st.takeItems(TEAM_CERTIFICATE, -1);
				st.takeItems(CLASS_FREE_CERTIFICATE, -1);
				st.takeItems(CLASS_CERTIFICATE, -1);
				st.finishQuest();
			}
		}
		return event;
	}

	@Override
	public void onOlympiadEnd(OlympiadGame og, QuestState qs)
	{
		if(qs.getCond() != 1)
			return;

		switch(og.getType())
		{
			case TEAM:
				int count1 = qs.getInt("count1") + 1;
				qs.set("count1", count1);
				if(count1 == 5)
				{
					qs.giveItems(TEAM_CERTIFICATE, 1, false, false);
					if(qs.getInt("count2") >= 5 && qs.getInt("count3") >= 5)
					{
						qs.setCond(2);
					}
					else
						qs.playSound(SOUND_ITEMGET);
				}
				break;
			case CLASSED:
				int count2 = qs.getInt("count2") + 1;
				qs.set("count2", count2);
				if(count2 == 5)
				{
					qs.giveItems(CLASS_CERTIFICATE, 1, false, false);
					if(qs.getInt("count1") >= 5 && qs.getInt("count3") >= 5)
					{
						qs.setCond(2);
					}
					else
						qs.playSound(SOUND_ITEMGET);
				}
				break;
			case NON_CLASSED:
				int count3 = qs.getInt("count3") + 1;
				qs.set("count3", count3);
				if(count3 == 5)
				{
					qs.giveItems(CLASS_FREE_CERTIFICATE, 1, false, false);
					if(qs.getInt("count1") >= 5 && qs.getInt("count2") >= 5)
					{
						qs.setCond(2);
					}
					else
						qs.playSound(SOUND_ITEMGET);
				}
				break;
		}
	}
}

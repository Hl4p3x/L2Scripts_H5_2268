package quests;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class _510_AClansReputation extends QuestScript
{
	private static final int VALDIS = 31331;
	private static final int CLAW = 8767;
	private static final int CLAN_POINTS_REWARD = 30;

	public _510_AClansReputation()
	{
		super(PARTY_ALL, REPEATABLE);

		addStartNpc(VALDIS);

		for(int npc = 22215; npc <= 22217; npc++)
			addKillId(npc);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int cond = st.getCond();
		String htmltext = event;
		if(event.equals("31331-3.htm"))
		{
			if(cond == 0)
			{
				st.setCond(1);
			}
		}
		else if(event.equals("31331-6.htm"))
		{
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>";
		Player player = st.getPlayer();
		Clan clan = player.getClan();
		if(player.getClan() == null || !player.isClanLeader())
		{
			st.abortQuest();
			htmltext = "31331-0.htm";
		}
		else if(player.getClan().getLevel() < 5)
		{
			st.abortQuest();
			htmltext = "31331-0.htm";
		}
		else
		{
			int cond = st.getCond();
			if(cond == 0)
				htmltext = "31331-1.htm";
			else if(cond == 1)
			{
				long count = st.getQuestItemsCount(CLAW);
				if(count == 0)
					htmltext = "31331-4.htm";
				else if(count >= 1)
				{
					htmltext = "31331-7.htm";// custom html
					st.takeItems(CLAW, -1);
					int pointsCount = CLAN_POINTS_REWARD * (int) count;
					if(count > 10)
						pointsCount += count % 10 * 118;
					int increasedPoints = clan.incReputation(pointsCount, true, "_510_AClansReputation");
					player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_SUCCESSFULLY_COMPLETED_A_CLAN_QUEST_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLAN_REPUTATION_SCORE).addNumber(increasedPoints));
				}
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.isStarted())
		{
			if(!st.getPlayer().isClanLeader())
				st.abortQuest();
			else
			{
				int npcId = npc.getNpcId();
				if(npcId >= 22215 && npcId <= 22218)
				{
					st.giveItems(CLAW, 1, true, true);
					st.playSound(SOUND_ITEMGET);
				}
			}
		}
		return null;
	}
}

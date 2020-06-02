package quests;

import java.util.concurrent.ScheduledFuture;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _457_LostAndFound extends QuestScript
{
	private static final int RESET_HOUR = 6;
	private static final int RESET_MIN = 30;

	private ScheduledFuture<?> FollowTask;

	public _457_LostAndFound()
	{
		super(PARTY_ONE, DAILY);
		addStartNpc(32759);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		Player player = st.getPlayer();
		String htmltext = event;
		if(event.equalsIgnoreCase("lost_villager_q0457_06.htm"))
		{
			st.setCond(1);
			DefaultAI.namechar = player.getName();
			if(DefaultAI.namechar != null)
			{
				if(FollowTask != null)
					FollowTask.cancel(false);
				FollowTask = null;
				FollowTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Follow(npc, st), 10, 1000);
				npc.setRunning();		
				npc.setFollowTarget(st.getPlayer());
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, st.getPlayer(), Config.FOLLOW_RANGE);				
			}
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		Player player = st.getPlayer();
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 32759)
		{
			if(cond == 0)
			{
				if(DefaultAI.namechar != null && !DefaultAI.namechar.equals(player.getName()))
					return "lost_villager_q0457_01a.htm";
				if(st.getPlayer().getLevel() >= 82)
					return "lost_villager_q0457_01.htm";
				return "lost_villager_q0457_03.htm";
			}
			else
			{
				if(DefaultAI.namechar != null && !DefaultAI.namechar.equals(player.getName()))
					return "lost_villager_q0457_01a.htm";
				if(cond == 2)
				{
					DefaultAI.namechar = null;
					npc.deleteMe();

					st.giveItems(15716, 1, false, false);
					st.finishQuest();
					return "lost_villager_q0457_09.htm";
				}
				if(cond == 1)
					return "lost_villager_q0457_08.htm";
			}
		}
		return NO_QUEST_DIALOG;
	}

	@Override
	public String onCompleted(NpcInstance npc, QuestState st)
	{
		String htmltext = COMPLETED_DIALOG;
		int npcId = npc.getNpcId();
		if(npcId == 32759)
			htmltext = "lost_villager_q0457_02.htm";
		return htmltext;
	}

	private void checkInRadius(int id, QuestState st, NpcInstance npc)
	{
		for(NpcInstance quest0457 : npc.getAroundNpc(300, 300))
		{
			if(quest0457.getNpcId() == id)
			{
				st.setCond(2);
				if(FollowTask != null)
					FollowTask.cancel(false);
				FollowTask = null;
				npc.stopMove();			
			}
		}
	}

	private class Follow implements Runnable
	{
		private NpcInstance _npc;
		private QuestState st;

		private Follow(NpcInstance npc, QuestState _st)
		{
			_npc = npc;
			st = _st;
		}

		@Override
		public void run()
		{
			checkInRadius(32764, st, _npc);
		}
	}
}
package quests;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.instancemanager.SpawnManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.templates.spawn.PeriodOfDay;


public class _653_WildMaiden extends QuestScript
{
	// Npc
	public final int SUKI = 32013;
	public final int GALIBREDO = 30181;

	// Items
	public final int SOE = 736;

	public _653_WildMaiden()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(SUKI);

		addTalkId(SUKI);
		addTalkId(GALIBREDO);
	}

	private NpcInstance findNpc(int npcId, Player player)
	{
		NpcInstance instance = null;
		List<NpcInstance> npclist = new ArrayList<NpcInstance>();
		for(Spawner spawn : SpawnManager.getInstance().getSpawners(PeriodOfDay.NONE.name()))
			if(spawn.getCurrentNpcId() == npcId)
			{
				instance = spawn.getLastSpawn();
				npclist.add(instance);
			}

		for(NpcInstance npc : npclist)
			if(player.isInRange(npc, 1600))
				return npc;

		return instance;
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		Player player = st.getPlayer();
		if(event.equalsIgnoreCase("spring_girl_sooki_q0653_03.htm"))
		{
			if(st.getQuestItemsCount(SOE) > 0)
			{
				st.setCond(1);
				st.takeItems(SOE, 1);
				htmltext = "spring_girl_sooki_q0653_04a.htm";
				NpcInstance n = findNpc(SUKI, player);
				n.broadcastPacket(new MagicSkillUse(n, n, 2013, 1, 20000, 0));
				st.startQuestTimer("suki_timer", 20000);
			}
		}
		else if(event.equalsIgnoreCase("spring_girl_sooki_q0653_03.htm"))
		{
			st.finishQuest();
			st.playSound(SOUND_GIVEUP);
		}
		else if(event.equalsIgnoreCase("suki_timer"))
		{
			NpcInstance n = findNpc(SUKI, player);
			n.deleteMe();
			htmltext = null;
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;

		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == SUKI)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 36)
					htmltext = "spring_girl_sooki_q0653_01.htm";
				else
					htmltext = "spring_girl_sooki_q0653_01a.htm";
			}
		}
		else if(npcId == GALIBREDO && cond == 1)
		{
			htmltext = "galicbredo_q0653_01.htm";
			st.giveItems(ADENA_ID, 2553, true, true);
			st.finishQuest();
		}
		return htmltext;
	}
}
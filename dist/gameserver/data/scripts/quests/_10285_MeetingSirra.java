package quests;

import gnu.trove.map.hash.TIntIntHashMap;
import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.s2c.ExStartScenePlayer;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.ReflectionUtils;

/**
 * @author pchayka
 */

public class _10285_MeetingSirra extends QuestScript
{
	private static final int Rafforty = 32020;
	private static final int Jinia = 32760;
	private static final int Jinia2 = 32781;
	private static final int Kegor = 32761;
	private static final int Sirra = 32762;
	private static TIntIntHashMap _instances = new TIntIntHashMap();

	public _10285_MeetingSirra()
	{
		super(PARTY_NONE, ONETIME);
		addStartNpc(Rafforty);
		addTalkId(Jinia, Jinia2, Kegor, Sirra);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("rafforty_q10285_03.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("enterinstance"))
		{
			if(st.getCond() == 1)
				st.setCond(2);
			enterInstance(st.getPlayer(), 141);
			return null;
		}
		else if(event.equalsIgnoreCase("jinia_q10285_02.htm"))
			st.setCond(3);
		else if(event.equalsIgnoreCase("kegor_q10285_02.htm"))
			st.setCond(4);
		else if(event.equalsIgnoreCase("sirraspawn"))
		{
			st.setCond(5);
			st.getPlayer().getReflection().addSpawnWithoutRespawn(Sirra, new Location(-23848, -8744, -5413, 49152), 0);
			for(NpcInstance sirra : st.getPlayer().getAroundNpc(1000, 100))
				if(sirra.getNpcId() == Sirra)
					Functions.npcSay(sirra, "Вас послушать, получается, что Вы знаете обо всем на свете. Но я больше не могу слушать Ваши мудрствования");
			return null;
		}
		else if(event.equalsIgnoreCase("sirra_q10285_07.htm"))
		{
			st.setCond(6);
			for(NpcInstance sirra : st.getPlayer().getAroundNpc(1000, 100))
				if(sirra.getNpcId() == 32762)
					sirra.deleteMe();
		}
		else if(event.equalsIgnoreCase("jinia_q10285_10.htm"))
		{
			if(!st.getPlayer().getReflection().isDefault())
			{
				st.getPlayer().getReflection().startCollapseTimer(60 * 1000L);
				st.getPlayer().sendPacket(new SystemMessage(SystemMessage.THIS_DUNGEON_WILL_EXPIRE_IN_S1_MINUTES).addNumber(1));
			}
			st.setCond(7);
		}
		else if(event.equalsIgnoreCase("exitinstance"))
		{
			st.getPlayer().getReflection().collapse();
			return null;
		}
		else if(event.equalsIgnoreCase("enterfreya"))
		{
			st.setCond(9);
			enterInstance(st.getPlayer(), 137);
			return null;
		}

		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Rafforty)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 82 && st.getPlayer().isQuestCompleted(10284))
					htmltext = "rafforty_q10285_01.htm";
				else
					htmltext = "rafforty_q10285_00.htm";
			}
			else if(cond >= 1 && cond < 7)
				htmltext = "rafforty_q10285_03.htm";
			else if(cond == 10)
			{
				htmltext = "rafforty_q10285_04.htm";
				st.giveItems(ADENA_ID, 283425, true, true);
				st.addExpAndSp(939075, 83855);
				st.finishQuest();
			}
		}
		else if(npcId == Jinia)
		{
			if(cond == 2)
				htmltext = "jinia_q10285_01.htm";
			else if(cond == 4)
				htmltext = "jinia_q10285_03.htm";
			else if(cond == 6)
				htmltext = "jinia_q10285_05.htm";
			else if(cond == 7)
				htmltext = "jinia_q10285_10.htm";
		}
		else if(npcId == Kegor)
		{
			if(cond == 3)
				htmltext = "kegor_q10285_01.htm";
		}
		else if(npcId == Sirra)
		{
			if(cond == 5)
				htmltext = "sirra_q10285_01.htm";
		}
		else if(npcId == Jinia2)
		{
			if(cond == 7 || cond == 8)
			{
				st.setCond(8);
				htmltext = "jinia2_q10285_01.htm";
			}
			else if(cond == 9)
				htmltext = "jinia2_q10285_02.htm";
		}
		return htmltext;
	}

	private void enterInstance(Player player, int izId)
	{
		Reflection r = player.getActiveReflection();
		if(r != null)
		{
			if(player.canReenterInstance(izId))
				player.teleToLocation(r.getTeleportLoc(), r);
		}
		else if(player.canEnterInstance(izId))
		{
			Reflection newInstance = ReflectionUtils.enterReflection(player, izId);
			if(izId == 137)
				ThreadPoolManager.getInstance().schedule(new FreyaSpawn(newInstance, player), 2 * 60 * 1000L);
		}
	}

	private class FreyaSpawn extends RunnableImpl
	{
		private Player _player;
		private Reflection _r;

		public FreyaSpawn(Reflection r, Player player)
		{
			_r = r;
			_player = player;
		}

		@Override
		public void runImpl() throws Exception
		{
			if(_r != null)
			{
				NpcInstance freya = _r.addSpawnWithoutRespawn(18847, new Location(114720, -117085, -11088, 15956), 0);
				ThreadPoolManager.getInstance().schedule(new FreyaMovie(_player, _r, freya), 2 * 60 * 1000L);
			}
		}
	}

	private class FreyaMovie extends RunnableImpl
	{
		Player _player;
		Reflection _r;
		NpcInstance _npc;

		public FreyaMovie(Player player, Reflection r, NpcInstance npc)
		{
			_player = player;
			_r = r;
			_npc = npc;
		}

		@Override
		public void runImpl() throws Exception
		{
			for(Spawner sp : _r.getSpawns())
				sp.deleteAll();
			if(_npc != null && !_npc.isDead())
				_npc.deleteMe();
			_player.showQuestMovie(ExStartScenePlayer.SCENE_BOSS_FREYA_FORCED_DEFEAT);
			ThreadPoolManager.getInstance().schedule(new ResetInstance(_player, _r), 23000L);
		}
	}

	private class ResetInstance extends RunnableImpl
	{
		Player _player;
		Reflection _r;

		public ResetInstance(Player player, Reflection r)
		{
			_player = player;
			_r = r;
		}

		@Override
		public void runImpl() throws Exception
		{
			QuestState st = _player.getQuestState(10285);
			if(st != null)
				st.setCond(10);
			_r.collapse();
		}
	}
}
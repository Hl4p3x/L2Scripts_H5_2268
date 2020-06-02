package l2s.gameserver.model.instances;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import l2s.commons.threading.RunnableImpl;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.instancemanager.RaidBossSpawnManager;
import l2s.gameserver.model.AggroList.HateInfo;
import l2s.gameserver.model.CommandChannel;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObjectTasks;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.entity.HeroDiary;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;

public class RaidBossInstance extends MonsterInstance
{
	private ScheduledFuture<?> minionMaintainTask;

	private static final int MINION_UNSPAWN_INTERVAL = 5000; //time to unspawn minions when boss is dead, msec

	public RaidBossInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean isRaid()
	{
		return true;
	}

	protected int getMinionUnspawnInterval()
	{
		return MINION_UNSPAWN_INTERVAL;
	}

	protected int getKilledInterval(MinionInstance minion)
	{
		return 120000; //2 minutes to respawn
	}

	@Override
	public void notifyMinionDied(MinionInstance minion)
	{
		minionMaintainTask = ThreadPoolManager.getInstance().schedule(new MaintainKilledMinion(minion), getKilledInterval(minion));
		super.notifyMinionDied(minion);
	}

	private class MaintainKilledMinion extends RunnableImpl
	{
		private final MinionInstance minion;

		public MaintainKilledMinion(MinionInstance minion)
		{
			this.minion = minion;
		}

		@Override
		public void runImpl() throws Exception
		{
			if(!isDead())
			{
				minion.refreshID();
				spawnMinion(minion);
			}
		}
	}

	@Override
	public double getRewardRate(Player player)
	{
		return Config.RATE_DROP_RAIDBOSS; // ПА не увеличивает дроп с рейдов
	}

	@Override
	protected void onDeath(Creature killer)
	{
		if(minionMaintainTask != null)
		{
			minionMaintainTask.cancel(false);
			minionMaintainTask = null;
		}
		

		final int points = getTemplate().rewardRp;
		if(points > 0)
			calcRaidPointsReward(points);

		if(this instanceof ReflectionBossInstance)
		{
			super.onDeath(killer);
			return;
		}

		if(killer != null && killer.isPlayable())
		{
			Player player = killer.getPlayer();
			if(player.isInParty())
			{
				for(Player member : player.getParty().getPartyMembers())
					if(member.isNoble())
						Hero.getInstance().addHeroDiary(member.getObjectId(), HeroDiary.ACTION_RAID_KILLED, getNpcId());
				player.getParty().broadCast(Msg.CONGRATULATIONS_YOUR_RAID_WAS_SUCCESSFUL);
			}
			else
			{
				if(player.isNoble())
					Hero.getInstance().addHeroDiary(player.getObjectId(), HeroDiary.ACTION_RAID_KILLED, getNpcId());
				player.sendPacket(Msg.CONGRATULATIONS_YOUR_RAID_WAS_SUCCESSFUL);
			}

			if(player.getClan() != null && player.getClan().getLeader().isOnline())
			{
				QuestState st = player.getClan().getLeader().getPlayer().getQuestState(508);
				if(st != null)
					st.getQuest().onKill(this, st);
			}
		}

		if(getMinionList().hasAliveMinions())
			ThreadPoolManager.getInstance().schedule(new RunnableImpl()
			{
				@Override
				public void runImpl() throws Exception
				{
					if(isDead())
						getMinionList().unspawnMinions();
				}
			}, getMinionUnspawnInterval());

		if(killer != null && killer == this) //make sure after restart such shit as mob is dead won't happen
		{
			super.onDeath(killer);
			return;
		}	
			
		int boxId = 0;
		switch(getNpcId())
		{
			case 25035: // Shilens Messenger Cabrio
				boxId = 31027;
				break;
			case 25054: // Demon Kernon
				boxId = 31028;
				break;
			case 25126: // Golkonda, the Longhorn General
				boxId = 31029;
				break;
			case 25220: // Death Lord Hallate
				boxId = 31030;
				break;
		}

		if(boxId != 0)
		{
			NpcTemplate boxTemplate = NpcHolder.getInstance().getTemplate(boxId);
			if(boxTemplate != null)
			{
				final NpcInstance box = new NpcInstance(IdFactory.getInstance().getNextId(), boxTemplate);
				box.spawnMe(getLoc());
				box.setSpawnedLoc(getLoc());

				ThreadPoolManager.getInstance().schedule(new GameObjectTasks.DeleteTask(box), 60000);
			}
		}
		
		if(killer.getPlayer() != null && Config.RAID_DROP_GLOBAL_ITEMS)
		{
			if(Config.MIN_RAID_LEVEL_TO_DROP > 0 && getLevel() < Config.MIN_RAID_LEVEL_TO_DROP)
			{
				super.onDeath(killer);
				return;
			}
			for(Config.RaidGlobalDrop drop_inf : Config.RAID_GLOBAL_DROP)
			{
				int id = drop_inf.getId();
				long count = drop_inf.getCount();
				double chance = drop_inf.getChance();
				if(Rnd.chance(chance))
					ItemFunctions.addItem(killer.getPlayer(), id, count, true, "Raid Boss global drop");
			}
		}
		
		super.onDeath(killer);
	}

	private class GroupInfo
	{
		public HashSet<Player> players;
		public long reward;

		public GroupInfo()
		{
			this.players = new HashSet<Player>();
			this.reward = 0;
		}
	}

	//FIXME [G1ta0] разобрать этот хлам
	private void calcRaidPointsReward(int totalPoints)
	{
		// Object groupkey (L2Party/L2CommandChannel/L2Player) | [List<L2Player> group, Long GroupDdamage]
		Map<Object, GroupInfo> groupsInfo = new HashMap<Object, GroupInfo>();
		double totalHp = getMaxHp();

		// Разбиваем игроков по группам. По возможности используем наибольшую из доступных групп: Command Channel → Party → StandAlone (сам плюс пет :)
		for(HateInfo ai : getAggroList().getPlayableMap().values())
		{
			Player player = ai.attacker.getPlayer();
			Object key = player.getParty() != null ? player.getParty().getCommandChannel() != null ? player.getParty().getCommandChannel() : player.getParty() : player.getPlayer();
			GroupInfo info = groupsInfo.get(key);
			if(info == null)
			{
				info = new GroupInfo();
				groupsInfo.put(key, info);
			}

			// если это пати или командный канал то берем оттуда весь список участвующих, даже тех кто не в аггролисте
			// дубликаты не страшны - это хашсет
			if(key instanceof CommandChannel)
			{
				for(Player p : ((CommandChannel) key))
				{
					if(p.isInRangeZ(this, Config.ALT_PARTY_DISTRIBUTION_RANGE))
						info.players.add(p);
				}
			}
			else if(key instanceof Party)
			{
				for(Player p : ((Party) key).getPartyMembers())
				{
					if(p.isInRangeZ(this, Config.ALT_PARTY_DISTRIBUTION_RANGE))
						info.players.add(p);
				}
			}
			else
				info.players.add(player);

			info.reward += ai.damage;
		}

		for(GroupInfo groupInfo : groupsInfo.values())
		{
			HashSet<Player> players = groupInfo.players;
			// это та часть, которую игрок заслужил дамагом группы, но на нее может быть наложен штраф от уровня игрока
			int perPlayer = (int) Math.round(totalPoints * groupInfo.reward / (totalHp * players.size()));
			for(Player player : players)
			{
				int playerReward = perPlayer;
				// применяем штраф если нужен
				playerReward = (int) Math.round(playerReward * Experience.penaltyModifier(calculateLevelDiffForDrop(player.getLevel()), 9));
				if(playerReward == 0)
					continue;
				player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EARNED_S1_RAID_POINTS).addNumber(playerReward));
				RaidBossSpawnManager.getInstance().addPoints(player.getObjectId(), getNpcId(), playerReward);
			}
		}

		RaidBossSpawnManager.getInstance().updatePointsDb();
		RaidBossSpawnManager.getInstance().calculateRanking();
	}

	@Override
	protected void onDecay()
	{
		super.onDecay();
		RaidBossSpawnManager.getInstance().onBossDespawned(this, true);
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();
		addSkill(SkillHolder.getInstance().getSkill(4045, 1)); // Resist Full Magic Attack
		RaidBossSpawnManager.getInstance().onBossSpawned(this);
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}

	@Override
	public boolean isLethalImmune()
	{
		return true;
	}

	@Override
	public boolean hasRandomWalk()
	{
		return false;
	}

	@Override
	public boolean canChampion()
	{
		return false;
	}
}
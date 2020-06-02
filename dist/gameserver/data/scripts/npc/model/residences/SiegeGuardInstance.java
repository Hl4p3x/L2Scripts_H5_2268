package npc.model.residences;

import java.util.List;

import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.reward.RewardItem;
import l2s.gameserver.model.reward.RewardList;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.npc.NpcTemplate;

public class SiegeGuardInstance extends NpcInstance
{
	private static final long serialVersionUID = 1L;

	public SiegeGuardInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setHasChatWindow(false);
	}

	@Override
	public boolean isSiegeGuard()
	{
		return true;
	}

	@Override
	public int getAggroRange()
	{
		return 1200;
	}

	@Override
	public double getRewardRate(Player player)
	{
		return player.hasPremiumAccount() ? Config.RATE_DROP_SIEGE_GUARD_PA : Config.RATE_DROP_SIEGE_GUARD; // ПА не действует на эполеты
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		Player player = attacker.getPlayer();
		if(player == null)
			return false;
		SiegeEvent<?, ?> siegeEvent = getEvent(SiegeEvent.class);
		SiegeEvent<?, ?> siegeEvent2 = attacker.getEvent(SiegeEvent.class);
		Clan clan = player.getClan();
		if(siegeEvent == null)
			return false;
		if(clan != null && siegeEvent == siegeEvent2 && siegeEvent.getSiegeClan(SiegeEvent.DEFENDERS, clan) != null)
			return false;
		return true;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	@Override
	public boolean isPeaceNpc()
	{
		return false;
	}

	@Override
	protected void onDeath(Creature killer)
	{
		SiegeEvent<?, ?> siegeEvent = getEvent(SiegeEvent.class);
		if(killer != null)
		{
			Player player = killer.getPlayer();
			if(siegeEvent != null && player != null)
			{
				Clan clan = player.getClan();
				SiegeEvent<?, ?> siegeEvent2 = killer.getEvent(SiegeEvent.class);
				if(clan != null && siegeEvent == siegeEvent2 && siegeEvent.getSiegeClan(SiegeEvent.DEFENDERS, clan) == null)
				{
					Creature topdam = getAggroList().getTopDamager(killer);
					for(RewardList list : getTemplate().getRewards())
						rollRewards(list, killer, topdam);
				}
			}
		}
		super.onDeath(killer);
	}

	public void rollRewards(RewardList list, final Creature lastAttacker, Creature topDamager)
	{
		final Player activePlayer = topDamager.getPlayer();
		if(activePlayer == null)
			return;

		final double penaltyMod = Experience.penaltyModifier(calculateLevelDiffForDrop(topDamager.getLevel()), 9);
		final double rateMod = calcStat(Stats.REWARD_MULTIPLIER, 1., topDamager, null);
		
		List<RewardItem> rewardItems = list.roll(activePlayer, penaltyMod, rateMod, this);

		for(RewardItem drop : rewardItems)
			dropItem(activePlayer, drop.itemId, drop.count);
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
	public Clan getClan()
	{
		return null;
	}
}
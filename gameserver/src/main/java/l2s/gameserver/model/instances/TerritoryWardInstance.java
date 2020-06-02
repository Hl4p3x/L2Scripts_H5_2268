package l2s.gameserver.model.instances;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2s.gameserver.model.entity.events.objects.TerritoryWardObject;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * @author VISTALL
 * @date 16:38/11.04.2011
 */
public class TerritoryWardInstance extends NpcInstance
{
	private final TerritoryWardObject _territoryWard;

	public TerritoryWardInstance(int objectId, NpcTemplate template, TerritoryWardObject territoryWardObject)
	{
		super(objectId, template);
		setHasChatWindow(false);
		_territoryWard = territoryWardObject;
	}

	@Override
	public void onDeath(Creature killer)
	{
		super.onDeath(killer);
		Player player = killer.getPlayer();
		if(player == null)
			return;

		if(_territoryWard.canPickUp(player))
		{
			_territoryWard.pickUp(player);
			decayMe();
		}
	}

	@Override
	protected void onDecay()
	{
		decayMe();

		_spawnAnimation = 2;
	}

	@Override
	public boolean isAttackable(Creature attacker)
	{
		return isAutoAttackable(attacker);
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		DominionSiegeEvent siegeEvent = getEvent(DominionSiegeEvent.class);
		if(siegeEvent == null)
			return false;
		DominionSiegeEvent siegeEvent2 = attacker.getEvent(DominionSiegeEvent.class);
		if(siegeEvent2 == null)
			return false;
		if(siegeEvent == siegeEvent2)
			return false;
		if(siegeEvent2.getResidence().getOwner() != attacker.getClan())
			return false;
		if(!attacker.isInRange(this, 150))
			return false;
		return true;
	}

	@Override
	protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp)
	{
		if(attacker.getTarget() != this)
			return;
		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);	
	}

	@Override
	public boolean isPeaceNpc()
	{
		return false;
	}

	@Override
	public Clan getClan()
	{
		return null;
	}
}

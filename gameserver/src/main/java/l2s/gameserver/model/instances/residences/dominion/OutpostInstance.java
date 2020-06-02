package l2s.gameserver.model.instances.residences.dominion;

import org.apache.commons.lang3.StringUtils;

import l2s.commons.geometry.Circle;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Territory;
import l2s.gameserver.model.World;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2s.gameserver.model.instances.residences.SiegeFlagInstance;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.funcs.FuncMul;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.ZoneTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * @author VISTALL
 * @date 14:59/09.06.2011
 * FIXME [VISTALL] возможна корекция статов
 */
public class OutpostInstance extends SiegeFlagInstance
{
	private class OnZoneEnterLeaveListenerImpl implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature actor)
		{
			DominionSiegeEvent siegeEvent = OutpostInstance.this.getEvent(DominionSiegeEvent.class);
			if(siegeEvent == null)
				return;

			if(actor.getEvent(DominionSiegeEvent.class) != siegeEvent)
				return;

			actor.addStatFunc(new FuncMul(Stats.REGENERATE_HP_RATE, 0x40, OutpostInstance.this, 2.));
			actor.addStatFunc(new FuncMul(Stats.REGENERATE_MP_RATE, 0x40, OutpostInstance.this, 2.));
			actor.addStatFunc(new FuncMul(Stats.REGENERATE_CP_RATE, 0x40, OutpostInstance.this, 2.));
		}

		@Override
		public void onZoneLeave(Zone zone, Creature actor)
		{
			actor.removeStatsOwner(OutpostInstance.this);
		}
	}

	private Zone _zone = null;

	public OutpostInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();

		Circle c = new Circle(getLoc(), 250);
		c.setZmax(World.MAP_MAX_Z);
		c.setZmin(World.MAP_MIN_Z);

		StatsSet set = new StatsSet();
		set.set("name", StringUtils.EMPTY);
		set.set("type", Zone.ZoneType.dummy);
		set.set("territory", new Territory().add(c));

		_zone = new Zone(new ZoneTemplate(set));
		_zone.setReflection(ReflectionManager.DEFAULT);
		_zone.addListener(new OnZoneEnterLeaveListenerImpl());
		_zone.setActive(true);
	}

	@Override
	public void onDelete()
	{
		super.onDelete();

		_zone.setActive(false);
		_zone = null;
	}

	@Override
	public boolean isPeaceNpc()
	{
		return true;
	}
}

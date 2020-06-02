package zones;

import java.util.List;

import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.entity.events.impl.DuelEvent;
import l2s.gameserver.model.entity.events.impl.PlayerVsPlayerDuelEvent;
import l2s.gameserver.scripts.ScriptFile;
import l2s.gameserver.utils.ReflectionUtils;

/**
 * @author PaInKiLlEr
 *         - При входе в мирную зону, дуэль заканчивается
 */
public class DuelZone implements ScriptFile
{
	private static ZoneListener _zoneListener;

	@Override
	public void onLoad()
	{
		_zoneListener = new ZoneListener();
		List<Zone> zones = ReflectionUtils.getZonesByType(ZoneType.peace_zone);
		for(Zone zone : zones)
			zone.addListener(_zoneListener);
	}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public class ZoneListener implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature cha)
		{
			if( !cha.isPlayer())
				return;

			Player player = (Player) cha;
			if( !player.isInDuel())
				return;

			DuelEvent duelEvent = player.getEvent(DuelEvent.class);
			if(duelEvent != null && duelEvent instanceof PlayerVsPlayerDuelEvent)
				duelEvent.stopEvent(false);
		}

		@Override
		public void onZoneLeave(Zone zone, Creature cha)
		{}
	}
}

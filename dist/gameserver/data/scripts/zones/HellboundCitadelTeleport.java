package zones;

import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.instances.DoorInstance;
import l2s.gameserver.scripts.ScriptFile;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.ReflectionUtils;

/**
 * @author Bonux
**/
public class HellboundCitadelTeleport implements ScriptFile
{
	private static final String TELEPORT_ZONE_NAME_1 = "[hellbound_citadel_enter_tel]";
	private static final String TELEPORT_ZONE_NAME_2 = "[hellbound_citadel_exit_tel]";

	private static final Location TELEPORT_LOC_1 = new Location(18008, 244280, 11616);
	private static final Location TELEPORT_LOC_2 = new Location(18216, 244264, 11616);

	private static final int DOOR_ID_1 = 20250006;
	private static final int DOOR_ID_2 = 20250007;

	@Override
	public void onLoad()
	{
		Zone zone = ReflectionUtils.getZone(TELEPORT_ZONE_NAME_1);
		zone.addListener(new ZoneListener(TELEPORT_LOC_1));

		zone = ReflectionUtils.getZone(TELEPORT_ZONE_NAME_2);
		zone.addListener(new ZoneListener(TELEPORT_LOC_2));
	}

	@Override
	public void onReload()
	{
		//
	}

	@Override
	public void onShutdown()
	{
		//
	}

	private static class ZoneListener implements OnZoneEnterLeaveListener
	{
		private final Location _teleportLoc;

		public ZoneListener(Location teleportLoc)
		{
			_teleportLoc = teleportLoc;
		}

		@Override
		public void onZoneEnter(Zone zone, Creature cha)
		{
			if(zone == null)
				return;

			if(cha == null)
				return;

			if(cha.isPlayer())
			{
				Player player = cha.getPlayer();
				if(player == null)
					return;

				DoorInstance door = player.getReflection().getDoor(DOOR_ID_1);
				if(door == null || !door.isOpen())
					door = player.getReflection().getDoor(DOOR_ID_2);

				if(door == null || !door.isOpen())
					return;

				player.teleToLocation(_teleportLoc);
			}
		}

		@Override
		public void onZoneLeave(Zone zone, Creature cha)
		{
			//
		}
	}
}
package npc.model;

import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.EventTriggerPacket;
import l2s.gameserver.network.l2.s2c.ExStartScenePlayer;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ReflectionUtils;

/**
 * @author pchayka
 */

public final class OddGlobeInstance extends NpcInstance
{
	private static final int instancedZoneId = 151;

	public OddGlobeInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equalsIgnoreCase("monastery_enter"))
		{
			Reflection r = player.getActiveReflection();
			if(r != null)
			{
				if(player.canReenterInstance(instancedZoneId))
					player.teleToLocation(r.getTeleportLoc(), r);
			}
			else if(player.canEnterInstance(instancedZoneId))
			{
				Reflection newfew = ReflectionUtils.enterReflection(player, instancedZoneId);
				ZoneListener zoneL = new ZoneListener();
				newfew.getZone("[ssq_holy_burial_ground]").addListener(zoneL);
				ZoneListener2 zoneL2 = new ZoneListener2();
				newfew.getZone("[ssq_holy_seal]").addListener(zoneL2);
			}
		}
		else
			super.onBypassFeedback(player, command);
	}

	public class ZoneListener implements OnZoneEnterLeaveListener
	{
		private boolean done = false;

		@Override
		public void onZoneEnter(Zone zone, Creature cha)
		{
			Player player = cha.getPlayer();
			if(player == null || !cha.isPlayer() || done)
				return;
			done = true;
			player.showQuestMovie(ExStartScenePlayer.SCENE_SSQ2_HOLY_BURIAL_GROUND_OPENING);
		}

		@Override
		public void onZoneLeave(Zone zone, Creature cha)
		{
		}
	}

	public class ZoneListener2 implements OnZoneEnterLeaveListener
	{
		private boolean done = false;

		@Override
		public void onZoneEnter(Zone zone, Creature cha)
		{
			Player player = cha.getPlayer();
			if(player == null || !cha.isPlayer())
				return;
			player.broadcastPacket(new EventTriggerPacket(21100100, true));
			if(!done)
			{
				done = true;
				player.showQuestMovie(ExStartScenePlayer.SCENE_SSQ2_SOLINA_TOMB_OPENING);
			}
		}

		@Override
		public void onZoneLeave(Zone zone, Creature cha)
		{
		}
	}


}
package npc.model;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.ReflectionBossInstance;
import l2s.gameserver.templates.InstantZone;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;

public class LostCaptainInstance extends ReflectionBossInstance
{
	private static final int TELE_DEVICE_ID = 4314;

	public LostCaptainInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	protected void onDeath(Creature killer)
	{
		Reflection r = getReflection();
		r.setReenterTime(System.currentTimeMillis());

		super.onDeath(killer);

		InstantZone iz = r.getInstancedZone();
		if(iz != null)
		{
			String tele_device_loc = iz.getAddParams().getString("tele_device_loc", null);
			if(tele_device_loc != null)
			{
				KamalokaGuardInstance npc = new KamalokaGuardInstance(IdFactory.getInstance().getNextId(), NpcHolder.getInstance().getTemplate(TELE_DEVICE_ID));
				npc.setSpawnedLoc(Location.parseLoc(tele_device_loc));
				npc.setReflection(r);
				npc.spawnMe(npc.getSpawnedLoc());
			}
		}
		for(Player player : r.getPlayers())
		{
			if(!Config.OFFLINE_ONLY_IF_PREMIUM || player.hasPremiumAccount())
			{
				if(r.getInstancedZone().getId()== 73)
					ItemFunctions.addItem(player, 13002, 5, true, "Reward by LostCaptainInstance die");
				else if(r.getInstancedZone().getId() == 74)
					ItemFunctions.addItem(player, 13002, 7, true, "Reward by LostCaptainInstance die");
				else if(r.getInstancedZone().getId() == 75)
					ItemFunctions.addItem(player, 13002, 8, true, "Reward by LostCaptainInstance die");
				else if(r.getInstancedZone().getId() == 76)
					ItemFunctions.addItem(player, 13002, 12, true, "Reward by LostCaptainInstance die");
				else if(r.getInstancedZone().getId() == 77)
					ItemFunctions.addItem(player, 13002, 15, true, "Reward by LostCaptainInstance die");
				else if(r.getInstancedZone().getId() == 78)
					ItemFunctions.addItem(player, 13002, 18, true, "Reward by LostCaptainInstance die");
				else if(r.getInstancedZone().getId() == 79)
					ItemFunctions.addItem(player, 13002, 18, true, "Reward by LostCaptainInstance die");
				else if(r.getInstancedZone().getId() == 134)
					ItemFunctions.addItem(player, 13002, 19, true, "Reward by LostCaptainInstance die");
			}
		}
	}
}

package services;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.listener.actor.npc.OnSpawnListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.listener.CharListenerList;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.RaidBossInstance;
import l2s.gameserver.scripts.ScriptFile;
import l2s.gameserver.tables.GmListTable;

/**
 * @author Bonux
**/
public class RaidBossHistory implements ScriptFile
{
	private static class DeathListener implements OnDeathListener, OnSpawnListener
	{
		@Override
		public void onDeath(Creature cha, Creature killer)
		{
			if(!cha.isRaid())
				return;

			if(!cha.isBoss() && Config.ANNOUNCE_RAID_BOSS_DIE)
				Announcements.getInstance().announceByCustomMessage("services.RaidBossHistory.raidboss.die", new String[]{ cha.getName() });

			if(killer == null)
				return;

			RaidBossInstance raid = (RaidBossInstance) cha;

			Connection con = null;
			PreparedStatement statement = null;			
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("INSERT INTO raidboss_history (killer_object_id, killer_name, raid_id, raid_name, kill_time) VALUES(?,?,?,?,?)");
				int i = 0;
				statement.setInt(++i, killer.getObjectId());
				statement.setString(++i, killer.getName());
				statement.setInt(++i, raid.getNpcId());
				statement.setString(++i, raid.getName());
				statement.setInt(++i, (int) (System.currentTimeMillis() / 1000));
				statement.execute();
			}
			catch(Exception e)
			{
				_log.error("RaidBossHistory: cannot store raid boss history: " + e, e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}

		@Override
		public void onSpawn(NpcInstance actor)
		{
			if(!actor.isRaid())
				return;

			if(!actor.getReflection().isMain())
				return;

			GmListTable.broadcastMessageToGMs("Spawning Raid Boss - " + actor.getName());

			if(!actor.isBoss() && Config.ANNOUNCE_RAID_BOSS_RESPAWN/* && (bossId <= 26000)*/)
				Announcements.getInstance().announceByCustomMessage("services.RaidBossHistory.raidboss.respawn", new String[]{ actor.getName() });
		}
	}

	private static final Logger _log = LoggerFactory.getLogger(RaidBossHistory.class);

	@Override
	public void onLoad()
	{
		CharListenerList.addGlobal(new DeathListener());
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
}
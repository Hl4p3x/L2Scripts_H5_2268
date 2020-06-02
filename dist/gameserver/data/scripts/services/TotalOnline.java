package services;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbutils.DbUtils;
import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.scripts.ScriptFile;
import l2s.gameserver.tables.FakePlayersTable;

/**
 * Online -> real + fake
 * by l2scripts
 */
public class TotalOnline implements ScriptFile
{
	private static final Logger _log = LoggerFactory.getLogger(TotalOnline.class);

	@Override
	public void onLoad()
	{
		_log.info("Loaded Service: Parse Online [" + (Config.ALLOW_ONLINE_PARSE ? "enabled]" : "disabled]"));
		if(Config.ALLOW_ONLINE_PARSE)
			ThreadPoolManager.getInstance().scheduleAtFixedRate(new UpdateOnline(), Config.FIRST_UPDATE * 60000, Config.DELAY_UPDATE * 60000);
	}

	private class UpdateOnline extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			int members = getOnlineMembers();
			int offMembers = getOfflineMembers();
			Connection con = null;
			PreparedStatement statement = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("UPDATE online SET totalOnline=?, totalOffline=? WHERE `index`=0");
				statement.setInt(1, members);
				statement.setInt(2, offMembers);
				statement.execute();
				DbUtils.closeQuietly(statement);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}
	}

	//for future possibility of parsing names of players method is taking also name to array for init
	private int getOnlineMembers()
	{
		int i = 0;
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			i++;

		i = i + FakePlayersTable.getActiveFakePlayersCount();
		return i;	
	}

	private int getOfflineMembers()
	{
		int i = 0;
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(player.isInOfflineMode())
				i++;
		}
		return i;	
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
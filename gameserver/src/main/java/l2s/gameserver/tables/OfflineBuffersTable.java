package l2s.gameserver.tables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.instancemanager.OfflineBufferManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;

public class OfflineBuffersTable
{
	private static final Logger _log = LoggerFactory.getLogger(OfflineBuffersTable.class);
	
	public void restoreOfflineBuffers()
	{
		_log.info(getClass().getSimpleName() + ": Loading offline buffers...");

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("SELECT * FROM character_offline_buffers WHERE charId > 0");
			rset = statement.executeQuery();
			int nBuffers = 0;
            
			while (rset.next())
            {
				Player player = null;
				try
				{
					player = Player.restore(rset.getInt("charId"), false);
					if (player != null)
					{
						player.setOfflineMode(true);
						player.setIsOnline(true);
						player.updateOnlineStatus();  
						player.spawnMe();
						if(player.getClan() != null && player.getClan().getAnyMember(player.getObjectId()) != null)
							player.getClan().getAnyMember(player.getObjectId()).setPlayerInstance(player, false);
                  
						OfflineBufferManager.BufferData buffer = new OfflineBufferManager.BufferData(player, rset.getString("title"), rset.getInt("price"), null);
						
						Connection con1 = null;
						PreparedStatement statement1 = null;
						ResultSet rset1 = null;                  

						try
						{
							con1 = DatabaseFactory.getInstance().getConnection();	
							statement1 = con1.prepareStatement("SELECT * FROM character_offline_buffer_buffs WHERE charId = ?");					
							statement1.setInt(1, player.getObjectId());
							rset1 = statement1.executeQuery();
							try
							{
								if(rset1.next())
								{
									String[] skillIds = rset1.getString("skillIds").split(",");
									for(String skillId : skillIds)
									{
										Skill skill = player.getKnownSkill(Integer.parseInt(skillId));
										if(skill != null)
											buffer.getBuffs().put(skill.getId(), skill);
									}
								}
							}
							catch(Exception e)
							{	
								e.printStackTrace();
							}
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
						finally
						{
							DbUtils.closeQuietly(con1, statement1, rset1);
						}						
						OfflineBufferManager.getInstance().getBuffStores().put(player.getObjectId(), buffer);
						player.sitDown(null);
						player.setTitleColor(Config.BUFF_STORE_TITLE_COLOR, false);
						player.setTitle(buffer.getSaleTitle());
						player.setNameColor(Config.BUFF_STORE_OFFLINE_NAME_COLOR, false);   
						player.setPrivateStoreType(Player.STORE_PRIVATE_BUFF);
						player.broadcastUserInfo(true);
						nBuffers++;
					}
				}
				catch (Exception e)
				{
					_log.warn(getClass().getSimpleName() + ": Error loading buffer: " + player, e);
					if(player != null)
					{
						player.deleteMe();
					}
				}
			}
			_log.info(getClass().getSimpleName() + ": Loaded: " + nBuffers + " offline buffer(s)");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	
	public synchronized void onLogin(Player trader)
	{
		Connection con = null;
		PreparedStatement statement = null;	
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			OfflineBufferManager.getInstance().getBuffStores().remove(trader.getObjectId());
			statement = con.prepareStatement("DELETE FROM character_offline_buffers WHERE charId=?");
			statement.setInt(1, trader.getObjectId());
			statement.executeUpdate();
			
			statement = con.prepareStatement("DELETE FROM character_offline_buffer_buffs WHERE charId=?");
			statement.setInt(1, trader.getObjectId());
			statement.executeUpdate();			
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
	
	public synchronized void onLogout(Player trader)
	{
		OfflineBufferManager.BufferData buffer = OfflineBufferManager.getInstance().getBuffStores().get(trader.getObjectId());
		if(buffer == null)
			return;
			
		Connection con = null;
		PreparedStatement statement = null;			
		try 
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO character_offline_buffers VALUES (?,?,?)");

			statement.setInt(1, trader.getObjectId());
			statement.setInt(2, buffer.getBuffPrice());
			statement.setString(3, buffer.getSaleTitle());
			statement.executeUpdate();
        
			statement = con.prepareStatement("REPLACE INTO character_offline_buffer_buffs VALUES (?,?)");
			statement.setInt(1, trader.getObjectId());
			statement.setString(2, joinAllSkillsToString(buffer.getBuffs().values()));
			statement.executeUpdate();
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

	private final String joinAllSkillsToString(Collection<Skill> skills)
	{
		if(skills.isEmpty()) 
			return "";
		String result = "";
		for(Skill val : skills)
		{
			result = result + val.getId() + ",";
		}
    
		return result.substring(0, result.length() - 1);
	}
  
	public static OfflineBuffersTable getInstance()
	{
		return SingletonHolder._instance;
	}
  
	private static class SingletonHolder
	{
		protected static final OfflineBuffersTable _instance = new OfflineBuffersTable();
	}
}

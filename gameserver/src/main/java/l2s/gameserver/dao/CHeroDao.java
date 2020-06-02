package l2s.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.entity.Hero;
/**
 * @author Iqman
 * @date 19:23/11.01.2012
 */
public class CHeroDao
{
	private static final Logger _log = LoggerFactory.getLogger(CHeroDao.class);
	
	private static ArrayList<Integer> _l = new ArrayList<Integer>();
	
	public static void LoadAllCustomHeroes()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			int c_hero = 0;
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM custom_heroes");
			rset = statement.executeQuery();
			while(rset.next())
			{
				c_hero = rset.getInt("hero_id");
				_l.add(c_hero);
			}	
		}	
		catch(Exception e)
		{
			_log.info("not working?");
		}
		finally
		{
			
			DbUtils.closeQuietly(con, statement, rset);
			_log.info("custom heroes loaded size: "+_l.size()+"");
		}		
	}
	
	public static void addCustomHero(int hero, long time)
	{
		if(_l.contains(hero) || Hero.getInstance().isHero(hero))
			return;
		Connection con = null;
		PreparedStatement statement = null;			
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO custom_heroes (hero_id, time) VALUES(?,?)");
			statement.setInt(1, hero);	
			statement.setLong(2, time);		
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("Custom Hero:" + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
			_l.add(hero);
		}
	}
	
	public static boolean isExpiredFor(int objId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		long time = 0;
		try
		{
			
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT time FROM custom_heroes WHERE hero_id = ?");
			statement.setInt(1, objId);	
			rset = statement.executeQuery();
			if(rset.next())
			{
				time = rset.getLong("time");
			}	
		}	
		catch(Exception e)
		{
			_log.info("not working?");
		}
		finally
		{
			
			DbUtils.closeQuietly(con, statement, rset);
		}	
		if(time == 0)
			return true;
		if(time == -1)
			return false;
		if(System.currentTimeMillis() > time)
			return true;
		return false;		
	}
	
	public static void removeCustomHero(int hero)
	{
		if(_l.contains(hero) || Hero.getInstance().isHero(hero))
			return;
		Connection con = null;
		PreparedStatement statement = null;			
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM custom_heroes WHERE hero_id =?");
			statement.setInt(1, hero);		
			statement.execute();
		}
		catch(Exception e)
		{
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
			_l.remove(hero);
		}
	}
	
	public static ArrayList<Integer> getAllCustomHeroes() 
	{
		return _l;
	}
	public static boolean isCustomHero(int id)
	{
		if(_l.contains(id))
			return true;
		return false;	
	}
}

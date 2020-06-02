package l2s.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.instancemanager.LfcManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.templates.item.ItemTemplate;

/**
 * @author Iqman
 * @date 00:42/06.06.2012
 */
public class LfcDAO
{
	private static final Logger _log = LoggerFactory.getLogger(LfcDAO.class);

	public static CopyOnWriteArrayList<Arenas> all_info = new CopyOnWriteArrayList<Arenas>();
	
	public static void LoadArenas()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM lfc_arena");
			rset = statement.executeQuery();
			while(rset.next())
			{
				int arena_id = rset.getInt("arena_id");
				String name_ru = rset.getString("name_ru");
				String name_en = rset.getString("name_en");
				int category = rset.getInt("category");
				String cat_name = rset.getString("category_name");
				int key_id = rset.getInt("key_id");
				long key_value = rset.getLong("key_value");
				long key_count = rset.getLong("key_count");
				boolean is_real_money = rset.getInt("is_real_money") == 1 ? true : false;
				int coupon_id = rset.getInt("coupon_id");
				long coupon_count = rset.getLong("coupon_count");
				long coupon_value = rset.getLong("coupon_value");
				String hero_type = rset.getString("hero_type");
				int hero_lengh = rset.getInt("hero_lengh");
				int min_level = rset.getInt("min_level");
				int max_level = rset.getInt("max_level");
				Arenas arenas_info = new Arenas(arena_id, name_ru, name_en, category, cat_name, key_id, key_value, key_count, is_real_money, coupon_id, coupon_count, coupon_value, hero_type, hero_lengh, min_level, max_level, null, null, true, null);
				all_info.add(arenas_info);
			}	
				
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			
			DbUtils.closeQuietly(con, statement, rset);
			_log.info("LFC: Loaded with "+all_info.size()+" arenas!");
		}
	
	}
	
	public static int getAllOpenArenasSize()
	{
		List<Arenas> search = new ArrayList<Arenas>();

		for(Arenas arena : all_info)
		{
			if(arena.isArenaOpen())
					search.add(arena);
		}
		return search.size();	
	}
	
	public static List<Arenas> getAllOpenedArenas()
	{
		List<Arenas> searchPrayor = new ArrayList<Arenas>();
		List<Arenas> search = new ArrayList<Arenas>();

		for(Arenas arena : all_info)
		{
			if(arena.isArenaOpen())
			{
				if(arena.getPlayerOne() != null || arena.getPlayerTwo() != null)
					searchPrayor.add(arena);
				else
					search.add(arena);
			}		
		}	
		
		for(Arenas arenaMinor : search)
		{
			if(searchPrayor.size() >= 15)
				break;
			searchPrayor.add(arenaMinor);	
		}
			//organized by arenas that have one player registred!!!
		return searchPrayor;
	}
	
	public static Arenas getArenaByArenaId(int arenaId)
	{
		for(Arenas arena : all_info)
		{
			if(arena.getArenaId() == arenaId)
				return arena;
		}
		return null;
	}
	public static List<Player> getPlayersInArena(int arenaId)
	{
		List<Player> search = new ArrayList<Player>();
		for(Arenas arena : all_info)
		{
			if(arena.getArenaId() == arenaId)
			{
				search.add(arena.getPlayerOne());
				search.add(arena.getPlayerTwo());
			}
		}
		return search;	
	}
	
	public static List<Arenas> getArenasByCategory(int cat_number)
	{
		List<Arenas> search = new ArrayList<Arenas>();
		for(Arenas arena : all_info)
		{
			if(arena.getCategory() == cat_number)
			{
				search.add(arena);
			}
		}
		return search;	
	}	
	
	public static int getCategoryIdByName(String name)
	{
		int cat_id = 0;
		for(Arenas arena : all_info)
		{
			if(arena.getCategoryName().equalsIgnoreCase(name))
			{
				cat_id = arena.getCategory();
				break; //no need to search all the categories
			}	
		}	
		return cat_id;	
	}
	
	public static List<String> getAllCategoryNames()
	{
		List<String> search = new ArrayList<String>();
		for(Arenas arena : all_info)
		{
			if(search.contains(arena.getCategoryName()))
				continue;
			search.add(arena.getCategoryName());	
		}		
		return search;
	}
	
	public static int getCategoryCount()
	{
		int i = 0;
		for(Arenas arena : all_info)
		{
			if(arena.getCategory() > i)
			{
				i = arena.getCategory();
			}
		}
		return i;	
	}	
	public static CopyOnWriteArrayList<Arenas> getAllArenas()
	{
		return all_info;
	}
	
	public static int AllArenasCount()
	{
		return all_info.size();
	}
	
	public static void notifyArenaClosed(Arenas arena)
	{
		arena.setOpenClose(false);
		LfcManager _game = new LfcManager(arena, arena.getPlayerOne(), arena.getPlayerTwo());
		arena.setArenaGame(_game);
		_game.initFight(arena);
	}

	public static long getCouponValueByCouponId(int id)
	{
		for(Arenas arena : all_info)
			if(arena.getCouponId() == id)
				return arena.getCouponValue();
		return -1;		
	}	
	
	public static class Arenas
	{
		public final int _arena_id;
		public final String _name_ru;
		public final String _name_en;
		public final int _category;
		public final String _cat_name;
		public final int _key_id;
		public final long _key_value;
		public final long _key_count;
		public final boolean _is_real_money;
		public final int _coupon_id;
		public final long _coupon_count;
		public final long _coupon_value;
		public final String _hero_type;
		public final int _hero_lengh;
		public final int _min_level;
		public final int _max_level;
		public Player _player1;
		public Player _player2;
		public boolean _isOpen;	
		public LfcManager _arena_game;	
		
		public Arenas(int arena_id, String name_ru, String name_en, int category, String cat_name, int key_id, long key_value, long key_count, boolean is_real_money, int coupon_id, long coupon_count, long coupon_value, String hero_type, int hero_lengh, int min_level, int max_level, Player player1, Player player2, boolean isOpen, LfcManager arena_game)
		{
			_arena_id = arena_id;
			_name_ru = name_ru;
			_name_en = name_en;
			_category = category;
			_cat_name = cat_name;
			_key_id = key_id;
			_key_value = key_value;
			_key_count = key_count; 
			_is_real_money = is_real_money;
			_coupon_id = coupon_id;
			_coupon_count = coupon_count;
			_coupon_value = coupon_value;
			_hero_type = hero_type;
			_hero_lengh = hero_lengh;
			_min_level = min_level;
			_max_level = max_level;
			_player1 = player1;
			_player2 = player2;
			_isOpen = isOpen;
			_arena_game = arena_game;
		}
		
		public int getArenaId()
		{
			return _arena_id;
		}
		public String getArenaNameRu()
		{
			return _name_ru;
		}		
		public String getArenaNameEn()
		{
			return _name_en;
		}	
		public int getCategory()
		{
			return _category;
		}		
		public String getCategoryName()
		{
			return _cat_name;
		}	
		public int getKeyToArena()
		{
			return _key_id;
		}		
		public long getKeyValue()
		{
			return _key_value;
		}	
		public long getKeyCount()
		{
			return _key_count;
		}		
		public boolean isMoneyFight()
		{
			return _is_real_money;
		}	
		public int getCouponId()
		{
			return _coupon_id;
		}	
		public long getCouponCount()
		{
			return _coupon_count;
		}	
		public long getCouponValue()
		{
			return _coupon_value;
		}		
		public boolean haveWinnerEffect()
		{
			return !_hero_type.equals("none");
		}
		public String getHeroType()
		{
			return _hero_type;
		}	
		public int getWinnerEffectTime()
		{
			return _hero_lengh;
		}	
		public int getArenMinLevel()
		{
			return _min_level;
		}	
		public int getArenMaxLevel()
		{
			return _max_level;
		}		
		public Player getPlayerOne()
		{
			return _player1;
		}	
		public Player getPlayerTwo()
		{
			return _player2;
		}	
		public boolean isArenaOpen()
		{
			return _isOpen;
		}			
		public void setOpenClose(boolean status)
		{
			_isOpen = status;
		}
		public void setPlayerOne(Player player)
		{
			_player1 = player;
			if(_player1 != null && _player2 != null)
			{
				notifyArenaClosed(this);
			}
		}
		public void setPlayerTwo(Player player)
		{
			_player2 = player;
			if(_player1 != null && _player2 != null)
			{
				notifyArenaClosed(this);
			}			
		}		
		
		public int getArenaStatus(Player player)
		{
			if(!_isOpen)
				return 2;		
			if(_player1 == player || _player2 == player)
			{
				if(_isOpen)
					return 3;
				else
					return 2; //won't happen but anyway
			}
			if(_player1 == null && _player2 == null)
				return 0; //empty
			if(_player1 != null || _player2 != null)
				return 1; //half empty
				
			return -1; //unknown request never happen	
		}
		public String getNameByCoupon()
		{
			ItemTemplate template = ItemHolder.getInstance().getTemplate(_coupon_id);
			return template.getName();
		}
		public void clear()
		{
			_arena_game = null;
			_isOpen = true;
			_player1 = null;
			_player2 = null;	
		}
		public void setArenaGame(LfcManager game)
		{
			game = _arena_game;
		}
	}	
	
}

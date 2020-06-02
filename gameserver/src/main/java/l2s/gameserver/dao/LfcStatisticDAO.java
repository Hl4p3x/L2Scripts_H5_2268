package l2s.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbutils.DbUtils;
import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.templates.item.ItemTemplate;

/**
 * @author Iqman
 * @date 00:42/06.06.2012
 */
public class LfcStatisticDAO
{
	private static final Logger _log = LoggerFactory.getLogger(LfcStatisticDAO.class);
	
	public static CopyOnWriteArrayList<LocalStatistic> all_info_fights = new CopyOnWriteArrayList<LocalStatistic>();
	public static CopyOnWriteArrayList<GlobalStatistic> all_info_global = new CopyOnWriteArrayList<GlobalStatistic>();
	public static int _count_battles = 0;
	public static int _count_money = 0;


	public static void LoadGlobalStatistics()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM lfc_stats_global");
			rset = statement.executeQuery();
			while(rset.next())
			{
				String char_name = rset.getString("char_name");
				int win_count = rset.getInt("win_count");
				int loose_count = rset.getInt("loose_count");
				int pay_battle_count = rset.getInt("pay_battle_count");
				int money_win = rset.getInt("money_win");
				GlobalStatistic global_info = new GlobalStatistic(char_name, win_count, loose_count, pay_battle_count, money_win);
				all_info_global.add(global_info);
			}	
				
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			LoadGlobalValues();
			DbUtils.closeQuietly(con, statement, rset);
			ThreadPoolManager.getInstance().scheduleAtFixedRate(new SaveGlobalFights(), 600000, 600000);
			ThreadPoolManager.getInstance().scheduleAtFixedRate(new UpdateGlobalValues(), 600000, 600000);
			_log.info("LFC Global Statistics: Loaded with "+all_info_global.size()+" characters!");
		}
	
	}	
	
	public static void LoadGlobalValues()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM lfc_global_values");
			rset = statement.executeQuery();
			while(rset.next())
			{
				_count_battles = rset.getInt("all_battles");
				_count_money = rset.getInt("all_money");
			}	
				
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}		
	}
	
	public static class UpdateGlobalValues extends RunnableImpl
	{

		public UpdateGlobalValues()
		{
		}

		@Override
		public void runImpl() throws Exception
		{
			updateGlobals();
		}		
	}
	private static void updateGlobals()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE lfc_global_values SET all_battles=?, all_money = ? WHERE dummy_slot=?");
			statement.setInt(1, getAllBattles());
			statement.setInt(2, getAllMoney());
			statement.setInt(3, 1);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}		
	}	
	public static int getAllBattles()
	{
		return _count_battles;
	}
	
	public static int getAllMoney()
	{
		return _count_money;
	}
	
	public static void increaseBattles(int count)
	{
		_count_battles += count;
	}
	
	public static void increaseMoney(long money)
	{
		_count_money += money;
	}
	
	public static class SaveGlobalFights extends RunnableImpl
	{

		public SaveGlobalFights()
		{
		}

		@Override
		public void runImpl() throws Exception
		{
			deleteGlobalTable();
		}		
	}

	private static void deleteGlobalTable()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM lfc_stats_global");
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}	
		for(GlobalStatistic stat : all_info_global)
			InsertGlobalData(stat);
	}	
	
	public static void InsertGlobalData(GlobalStatistic info)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO lfc_stats_global (char_name, win_count, loose_count, pay_battle_count, money_win) VALUES(?,?,?,?,?)");
			statement.setString(1, info._char_name);
			statement.setInt(2, info._win_count);
			statement.setInt(3, info._loose_count);
			statement.setInt(4, info._pay_battle_count);
			statement.setLong(5, info._money_win);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("could update player info for: " + info._char_name, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		
	}	
//////	
	public static void LoadLocalStatistics()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM lfc_stats_battle ORDER by place_id ASC");
			rset = statement.executeQuery();
			while(rset.next())
			{
				int arena_id = rset.getInt("arena_id");
				String name_ru = rset.getString("arena_name_ru");
				String name_en = rset.getString("arena_name_en");
				String winner = rset.getString("winner");
				String looser = rset.getString("looser");
				int item_won = rset.getInt("won_item");
				long item_count = rset.getLong("item_count");
				LocalStatistic local_info = new LocalStatistic(arena_id, name_ru, name_en, winner, looser, item_won, item_count);
				all_info_fights.add(local_info);
			}	
				
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			
			DbUtils.closeQuietly(con, statement, rset);
			ThreadPoolManager.getInstance().scheduleAtFixedRate(new SaveLocalFights(), 600000, 600000);
			_log.info("LFC Local Statistics: Loaded with "+all_info_fights.size()+" fights!");
		}
	
	}
	
	public static class SaveLocalFights extends RunnableImpl
	{

		public SaveLocalFights()
		{
		}

		@Override
		public void runImpl() throws Exception
		{
			deleteLocalTable();
		}		
	}

	private static void deleteLocalTable()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM lfc_stats_battle");
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}	
		for(LocalStatistic stat : all_info_fights)
			InsertLocalData(stat);
	}	
	
	public static void InsertLocalData(LocalStatistic info)
	{
		
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO lfc_stats_battle (arena_id, arena_name_en, arena_name_ru, winner, looser, won_item, item_count) VALUES(?,?,?,?,?,?,?)");
			statement.setInt(1, info._arena_id);
			statement.setString(2, info._name_ru);
			statement.setString(3, info._name_en);
			statement.setString(4, info._winner);
			statement.setString(5, info._looser);
			statement.setInt(6, info._won_item);
			statement.setLong(7, info._item_count);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("could update current fights ", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		
	}			
	
	public static CopyOnWriteArrayList<LocalStatistic> getArrangeLocalFights()
	{
		CopyOnWriteArrayList<LocalStatistic> copy = new CopyOnWriteArrayList<LocalStatistic>();
		CopyOnWriteArrayList<LocalStatistic> returnlist = new CopyOnWriteArrayList<LocalStatistic>();
		copy.addAll(all_info_fights);
		Collections.reverse(copy);
		for(LocalStatistic stat : copy)
		{
			if(returnlist.size() > 19)
				break;
			returnlist.add(stat);	
		}
		return returnlist;
	}
	
	public static class LocalStatistic
	{
		public final int _arena_id;
		public final String _name_ru;
		public final String _name_en;
		public final String _winner;
		public final String _looser;
		public final int _won_item;
		public final long _item_count;
		
		public LocalStatistic(int arena_id, String name_ru, String name_en, String winner, String looser, int won_item, long item_count)
		{
			_arena_id = arena_id;
			_name_ru = name_ru;
			_name_en = name_en;
			_winner = winner;
			_looser = looser;
			_won_item = won_item;
			_item_count = item_count;
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
		public String getWinner()
		{
			return _winner;
		}
		public String getLooser()
		{
			return _looser;
		}
		public String getWonItemName()
		{
			ItemTemplate template = ItemHolder.getInstance().getTemplate(_won_item);
			return template.getName();			
		}
		public long getWonItemCount()
		{
			return _item_count;
		}
	}	
	
//////
	public static class GlobalStatistic
	{
		public final String _char_name;
		public int _win_count;
		public int _loose_count;
		public int _pay_battle_count;
		public long _money_win;
		
		public GlobalStatistic(String char_name, int win_count, int loose_count, int pay_battle_count, long money_win)
		{
			_char_name = char_name;
			_win_count = win_count;
			_loose_count = loose_count;
			_pay_battle_count = pay_battle_count;
			_money_win = money_win;
		}
		
		public String getCharName()
		{
			return _char_name;
		}
		public int getWinCount()
		{
			return _win_count;
		}
		public int getLooseCount()
		{
			return _loose_count;
		}
		public int getPayBattleCount()
		{
			return _pay_battle_count;
		}
		public long getMoneyWin()
		{
			return _money_win;
		}
		public void IncreaseWinCount()
		{
			_win_count++;
		}
		public void IncreaseLooseCount()
		{
			_loose_count++;
		}
		public void IncreasePayMatchPlayed()
		{
			_pay_battle_count++;
		}
		public void IncreaseMoneyEarned(long add)
		{
			_money_win += add;
		}
	}	
	public static GlobalStatistic getPlayerRecord(Player player)
	{
		for(GlobalStatistic stat : all_info_global)
		{
			if(stat.getCharName().equalsIgnoreCase(player.getName()))
				return stat;
		}
		return null;
	}

	public static GlobalStatistic[] getSortedGlobalStatsArray()
	{
		GlobalStatistic[] result = all_info_global.toArray(new GlobalStatistic[all_info_global.size()]);
		Arrays.sort(result, new Comparator<GlobalStatistic>()
		{
			@Override
			public int compare(GlobalStatistic o1, GlobalStatistic o2)
			{
				if(o1.getWinCount() > o2.getWinCount())
					return -1;
				if(o1.getWinCount() < o2.getWinCount())
					return 1;
				return 0;			
			}
		});
		return result;
	}	
	public static void addLocalFight(LocalStatistic stat)
	{
		all_info_fights.add(stat);
	}
	public static void addGlobalStat(GlobalStatistic stat)
	{
		all_info_global.add(stat);
	}
}

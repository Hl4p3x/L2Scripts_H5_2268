package l2s.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.utils.BotPunish;

/**
 * @author: Kolobrodik
 * @date: 12:04/02.06.2012
 * @description: менеджер управления ботами (кнопка "Сообщить о боте")
 */
public class BotManager
{
	private static final Logger _log = LoggerFactory.getLogger(BotManager.class);

	private static HashMap<Integer, String[]> _unread;
	// Number of reportes made over each player
	private static HashMap<Integer, List<Player>> _reportedCount = new HashMap<Integer, List<Player>>();
	// Reporters blocked by time
	private static HashMap<Integer, Long> _lockedReporters = new HashMap<Integer, Long>();
	// Blocked ips
	private static Set<String> _lockedIps = new HashSet<String>();
	// Blocked accounts
	private static Set<String> _lockedAccounts = new HashSet<String>();

	private BotManager()
	{
		loadUnread();
	}

	public static BotManager getInstance()
	{
		return SingletonHolder._instance;
	}

	/**
	 * Check if the reported player is online
	 *
	 * @param reportedId
	 * @return true if L2World contains that player, else returns false
	 */
	private static boolean reportedIsOnline(Player player)
	{
		return GameObjectsStorage.getPlayer(player.getObjectId()) != null;
	}

	/**
	 * Will save the report in database
	 *
	 * @param reported (the Player who was reported)
	 * @param reporter (the Player who reported the bot)
	 */
	public synchronized void reportBot(Player reported, Player reporter)
	{
		if(!reportedIsOnline(reported))
		{
			reporter.sendMessage("Игрок на которого вы подаете жалобу сейчас оффлайн."); // TODO: CustomMessage
			return;
		}

		_lockedReporters.put(reporter.getObjectId(), System.currentTimeMillis());
		_lockedIps.add(reporter.getIP());
		_lockedAccounts.add(reporter.getAccountName());

		long date = Calendar.getInstance().getTimeInMillis();
		Connection con = null;

		try
		{
			if(!_reportedCount.containsKey(reported))
			{
				List<Player> p = new ArrayList<Player>();
				p.add(reported);
				_reportedCount.put(reporter.getObjectId(), p);
			}
			else
			{
				if(_reportedCount.get(reporter).contains(reported.getObjectId()))
				{
					reporter.sendMessage("Вы не можете подать жалобу больше одного раза на одного игрока.");
					return;
				}
				_reportedCount.get(reporter).add(reported);
			}

			con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO `bot_report`(`reported_name`, `reported_objectId`, `reporter_name`, `reporter_objectId`, `date`) VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, reported.getName());
			statement.setInt(2, reported.getObjectId());
			statement.setString(3, reporter.getName());
			statement.setInt(4, reporter.getObjectId());
			statement.setLong(5, date);
			statement.executeUpdate();

			ResultSet rs = statement.getGeneratedKeys();
			rs.next();
			int maxId = rs.getInt(1);

			statement.close();
			_unread.put(maxId, new String[]{reported.getName(), reporter.getName(), String.valueOf(date)});
		}
		catch(Exception e)
		{
			_log.error("Could not save reported bot " + reported.getName() + " by " + reporter.getName() + " at " + date + ".");
		}
		finally
		{
			try
			{
				con.close();
			}
			catch(Exception ignored)
			{
			}
		}
		SystemMessage sm = new SystemMessage(SystemMsg.C1_REPORTED_AS_BOT);
		sm.addName(reported);
		reporter.sendPacket(sm);
	}

	/**
	 * Will load the data from all unreaded reports (used to load reports
	 * in a window for admins/GMs)
	 *
	 * @return a FastMap<Integer, String[]> (Integer - report id, String[] - reported name, report name, date)
	 */
	private void loadUnread()
	{
		_unread = new HashMap<Integer, String[]>();
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT `report_id`, `reported_name`, `reporter_name`, `date` FROM `bot_report` WHERE `read` = ?");
			statement.setString(1, "false");

			ResultSet rset = statement.executeQuery();
			while(rset.next())
			{
				//Not loading objectIds to increase performance
				//L2World.getInstance().getPlayer(name).getObjectId();
				String[] data = new String[3];
				data[0] = rset.getString("reported_name");
				data[1] = rset.getString("reporter_name");
				data[2] = rset.getString("date");

				_unread.put(rset.getInt("report_id"), data);
			}
			rset.close();
			statement.close();
		}
		catch(Exception e)
		{
			_log.info("Could not load data from bot_report.");
		}
		finally
		{
			try
			{
				con.close();
			}
			catch(Exception ignored)
			{
			}
		}
	}

	/**
	 * Return a HashMap holding all the reports data
	 * to be viewed by any GM
	 *
	 * @return _unread
	 */
	public HashMap<Integer, String[]> getUnread()
	{
		return _unread;
	}

	/**
	 * Marks a reported bot as readed (from admin menu)
	 *
	 * @param id (the report id)
	 */
	public void markAsRead(int id)
	{
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE `bot_report` SET `read` = ? WHERE `report_id` = ?");
			statement.setString(1, "true");
			statement.setInt(2, id);
			statement.execute();

			statement.close();
			_unread.remove(id);
			_log.info("Reported bot marked as read, id was: " + id);
		}
		catch(Exception e)
		{
			_log.error("Could not mark as read the reported bot: " + id + ":\n" + e.getMessage());
		}
		finally
		{
			try
			{
				con.close();
			}
			catch(Exception ignored)
			{
			}
		}
	}

	/**
	 * Returns the number of times the player has been reported
	 *
	 * @param reported
	 * @return int
	 */
	public int getPlayerReportsCount(Player reported)
	{
		int count = 0;
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT COUNT(*) FROM `bot_report` WHERE `reported_objectId` = ?");
			statement.setInt(1, reported.getObjectId());

			ResultSet rset = statement.executeQuery();
			if(rset.next())
				count = rset.getInt(1);
			rset.close();
			statement.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				con.close();
			}
			catch(Exception ignored)
			{
			}
		}
		return count;
	}

	/**
	 * Will save the punish being suffered to player in database
	 * (at player logs out), to be restored next time players enter
	 * in server
	 *
	 * @param punished
	 */
	public void savePlayerPunish(Player punished)
	{
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE `bot_reported_punish` SET `time_left` = ? WHERE `charId` = ?");
			statement.setLong(1, punished.getPlayerPunish().getPunishTimeLeft());
			statement.setInt(2, punished.getObjectId());
			statement.execute();
			statement.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				con.close();
			}
			catch(Exception ignored)
			{
			}
		}
	}

	/**
	 * Retail report restrictions (Validates the player - reporter relationship)
	 *
	 * @param reported (the reported bot)
	 * @return
	 */
	public boolean validateBot(Player reported, Player reporter)
	{
		if(reported == null || reporter == null)
			return false;

		// Cannot report while reported is inside peace zone, war zone or olympiad
		if(reported.isInZone(ZoneType.peace_zone) || reported.isInZone(ZoneType.battle_zone) || reported.isInOlympiadMode())
		{
			reporter.sendPacket(SystemMsg.CANNOT_REPORT_IN_WARZONE_PEACEZONE_CLANWAR_OLYMPIAD);
			return false;
		}
		// Cannot report if reported and reporter are in war
		if(reported.getClan() != null && reporter.getClan() != null)
		{
			if(reported.getClan().isAtWarWith(reporter.getClanId()))
			{
				reporter.sendPacket(SystemMsg.CANNOT_REPORT_TARGET_IN_CLAN_WAR);
				return false;
			}
		}
		// Cannot report if the reported didnt earn exp since he logged in
		if(reported.getVarBoolean("NoExp"))
		{
			reporter.sendPacket(SystemMsg.CANNOT_REPORT_CHARACTER_WITHOUT_GAINEXP);
			return false;
		}
		// Cannot report twice or more a player
		if(_reportedCount.containsKey(reporter))
		{
			for(Player p : _reportedCount.get(reporter))
			{
				if(reported == p)
				{
					reporter.sendPacket(SystemMsg.C1_REPORTED_AS_BOT);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Retail report restrictions (Validates the reporter state)
	 *
	 * @param reporter
	 * @return
	 */
	public synchronized boolean validateReport(Player reporter)
	{
		if(reporter == null)
			return false;

		// The player has a 30 mins lock before be able to report anyone again
		if(reporter.getNetConnection().getReportsPoints() == 0)
		{
			SystemMessagePacket sm = new SystemMessagePacket(SystemMsg.YOU_CAN_REPORT_IN_S1_MINUTES_S2_REPORT_POINTS_REMAIN_IN_ACCOUNT);
			sm.addInteger(0);
			sm.addInteger(0);
			reporter.sendPacket(sm);
			return false;
		}
		// 30 mins must pass before report again 
		else if(_lockedReporters.containsKey(reporter.getObjectId()))
		{
			long delay = (System.currentTimeMillis() - _lockedReporters.get(reporter.getObjectId()));
			if(delay <= 1800000)
			{
				int left = (int) (1800000 - delay) / 60000;
				SystemMessagePacket sm = new SystemMessagePacket(SystemMsg.YOU_CAN_REPORT_IN_S1_MINUTES_S2_REPORT_POINTS_REMAIN_IN_ACCOUNT);
				sm.addInteger(left);
				sm.addInteger(reporter.getNetConnection().getReportsPoints());
				reporter.sendPacket(sm);
				return false;
			}
			else
				ThreadPoolManager.getInstance().execute(new ReportClear(reporter));
		}
		// In those 30 mins, the ip which made the first report cannot report again
		else if(_lockedIps.contains(reporter.getIP()))
		{
			reporter.sendPacket(SystemMsg.CANNOT_REPORT_ALREDY_REPORTED_FROM_YOUR_CLAN_OR_IP);
			return false;
		}
		// In those 30 mins, the account which made report cannot report again
		else if(_lockedAccounts.contains(reporter.getAccountName()))
		{
			reporter.sendPacket(SystemMsg.CANNOT_REPORT_ALAREDY_REPORTED_FROM_SAME_ACCOUNT);
			return false;
		}
		// If any clan/ally mate has reported any bot, you cannot report till he releases his lock
		else if(reporter.getClan() != null)
		{
			for(int i : _lockedReporters.keySet())
			{
				// Same clan
				Player p = GameObjectsStorage.getPlayer(i);
				if(p == null)
					continue;

				if(p.getClanId() == reporter.getClanId())
				{
					reporter.sendPacket(SystemMsg.CANNOT_REPORT_ALREDY_REPORTED_FROM_YOUR_CLAN_OR_IP);
					return false;
				}
				// Same ally
				else if(reporter.getClan().getAllyId() != 0)
				{
					if(p.getClan() != null && reporter.getClan() != null && p.getClan().getAllyId() == reporter.getClan().getAllyId())
					{
						reporter.sendPacket(SystemMsg.CANNOT_REPORT_ALREDY_REPORTED_FROM_YOUR_CLAN_OR_IP);
						return false;
					}
				}
			}
		}
		reporter.getNetConnection().reducePoints();
		return true;
	}

	/**
	 * Will manage needed actions on enter
	 *
	 * @param activeChar
	 */
	public void onEnter(Player activeChar)
	{
		restorePlayerBotPunishment(activeChar);
	}

	/**
	 * Will retore the player punish on enter
	 *
	 * @param activeChar
	 */
	private void restorePlayerBotPunishment(Player activeChar)
	{
		String punish = StringUtils.EMPTY;
		long delay = 0;
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT `punish_type`, `time_left` FROM `bot_reported_punish` WHERE `charId` = ?");
			statement.setInt(1, activeChar.getObjectId());

			ResultSet rset = statement.executeQuery();
			while(rset.next())
			{
				punish = rset.getString("punish_type");
				delay = rset.getLong("time_left");
			}
			rset.close();
			statement.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				DbUtils.closeQuietly(con);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		if(!punish.isEmpty() && BotPunish.Punish.valueOf(punish) != null)
		{
			if(delay < 0)
			{
				BotPunish.Punish p = BotPunish.Punish.valueOf(punish);
				long left = (-delay / 1000) / 60;
				activeChar.setPunishDueBotting(p, (int) left);
			}
			else
				activeChar.endPunishment();
		}
	}

	private static class SingletonHolder
	{
		private static BotManager _instance = new BotManager();
	}

	/**
	 * Manages the reporter restriction data clean up
	 * to be able to report again
	 */
	private class ReportClear implements Runnable
	{
		private Player _reporter;

		private ReportClear(Player reporter)
		{
			_reporter = reporter;
		}

		@Override
		public void run()
		{
			_lockedReporters.remove(_reporter.getObjectId());
			_lockedIps.remove(_reporter.getIP());
			_lockedAccounts.remove(_reporter.getAccountName());
		}
	}
}
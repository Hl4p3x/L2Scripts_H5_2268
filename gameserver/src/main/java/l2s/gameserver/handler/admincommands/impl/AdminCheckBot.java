package l2s.gameserver.handler.admincommands.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;

import l2s.gameserver.Config;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.instancemanager.BotManager;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;
import l2s.gameserver.utils.BotPunish;


public class AdminCheckBot implements IAdminCommandHandler
{
	
	private static enum Commands
	{
		admin_checkbots,
		admin_readbot,
		admin_markbotReaded,
		admin_punish_bot
	}
	
	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}
	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!Config.ALT_ENABLE_BOTREPORT)
		{
			activeChar.sendMessage("Bot reporting is not enabled!");
			return false;
		}
		
	
		switch(command)
		{
			case admin_checkbots:
				sendBotPage(activeChar);
				break;
			case admin_readbot:
				if(wordList[1] != null)
					sendBotInfoPage(activeChar, Integer.valueOf(wordList[1]));
				break;
			case admin_markbotReaded:
				try
				{
					if(wordList[1] != null)
					{
						BotManager.getInstance().markAsRead(Integer.valueOf(wordList[1]));
					sendBotPage(activeChar);
				}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			case admin_punish_bot:
				if(wordList[1] != null)
				{
					Player target = GameObjectsStorage.getPlayer(wordList[1]);
					if (target != null)
					{
						synchronized (target)
						{
							int punishLevel = 0;
							try
							{
								punishLevel = BotManager.getInstance().getPlayerReportsCount(target);
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
								
							// By System Message guess:
							// Reported 1 time = 10 mins chat ban
							// Reported 2 times = 60 mins w/o join pt
							// Reported 3 times = 120 mins w/o join pt
							// Reported 4 times = 180 mins w/o join pt
							// Reported 5 times = 120 mins w/o move
							// Reported 6 times = 180 mins w/o move
							// Reported 7 times = 120 mins w/o any action
							
							// Must be handled by GM or automatically ?
							// Since never will be retail info, ill put manually
							switch (punishLevel)
							{
							case 1:
									target.setPunishDueBotting(BotPunish.Punish.CHATBAN, 10);
									target.sendPacket(SystemMsg.REPORTED_10_MINS_WITHOUT_CHAT);
								break;
							case 2:
									target.setPunishDueBotting(BotPunish.Punish.PARTYBAN, 60);
									target.sendPacket(SystemMsg.REPORTED_60_MINS_WITHOUT_JOIN_PARTY);
								break;
							case 3:
									target.setPunishDueBotting(BotPunish.Punish.PARTYBAN, 120);
									target.sendPacket(SystemMsg.REPORTED_120_MINS_WITHOUT_JOIN_PARTY);
								break;
							case 4:
									target.setPunishDueBotting(BotPunish.Punish.PARTYBAN, 180);
									target.sendPacket(SystemMsg.REPORTED_180_MINS_WITHOUT_JOIN_PARTY);
								break;
							case 5:
									target.setPunishDueBotting(BotPunish.Punish.MOVEBAN, 120);
									target.sendPacket(SystemMsg.REPORTED_120_MINS_WITHOUT_MOVE);
								break;
							case 6:
									target.setPunishDueBotting(BotPunish.Punish.ACTIONBAN, 120);
									target.sendPacket(SystemMsg.REPORTED_120_MINS_WITHOUT_ACTIONS);
								break;
							case 7:
									target.setPunishDueBotting(BotPunish.Punish.ACTIONBAN, 180);
									target.sendPacket(SystemMsg.REPORTED_180_MINS_WITHOUT_ACTIONS);
								break;
							default:
								activeChar.sendMessage("Your target wasnt reported as a bot!");
							}
							// Inserts first time player punish in database, avoiding
							// problems to update punish state in future on log out
							if (punishLevel != 0)
							{
								introduceNewPunishedBotAndClear(target);
								activeChar.sendMessage(target.getName() + " has been punished.");
							}
						}
					}
					else
						activeChar.sendMessage("Your target doesnt exist!");
				}
				else
					activeChar.sendMessage("Usage: //punish_bot <charName>");
				break;
		}
		return true;
	}
	
	private static void sendBotPage(Player activeChar)
	{
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>Unread Bot List</title><body>");
		tb.append("Here's a list of the current <font color=LEVEL>unread</font> bots!<br>");

		for(int i : BotManager.getInstance().getUnread().keySet())
		{
			tb.append("<a action=\"bypass -h admin_readbot " + i + "\">Ticket #" + i + "</a><br1>");

		}
		tb.append("</body></html>");
		
		NpcHtmlMessagePacket nhm = new NpcHtmlMessagePacket(5);
		nhm.setHtml(tb.toString());
		activeChar.sendPacket(nhm);
	}
	
	private static void sendBotInfoPage(Player activeChar, int botId)
	{
		String[] report = BotManager.getInstance().getUnread().get(botId);
		StringBuilder tb = new StringBuilder();

		tb.append("<html><title>Bot #" + botId + "</title><body><br>");
		tb.append("- Bot report ticket Id: <font color=FF0000>" + botId + "</font><br>");
		tb.append("- Player reported: <font color=FF0000>" + report[0] + "</font><br>");
		tb.append("- Reported by: <font color=FF0000>" + report[1] + "</font><br>");
		tb.append("- Date: <font color=FF0000>" + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(Long.parseLong(report[2])) + "</font><br>");
		tb.append("<a action=\"bypass -h admin_markbotreaded " + botId + "\">Mark Report as Read</a><br>");
		tb.append("<a action=\"bypass -h admin_punish_bot " + report[0] + "\">Punish " + report[0] + "</a><br>");
		tb.append("<a action=\"bypass -h admin_checkbots\">Go Back to bot list</a>");
		tb.append("</center></body></html>");
		
		NpcHtmlMessagePacket nhm = new NpcHtmlMessagePacket(5);
		nhm.setHtml(tb.toString());
		activeChar.sendPacket(nhm);
	}
	
	/**
	 * Will introduce the first time a new punished bot in database,
	 * to avoid problems on his punish time left update, as will remove
	 * his reports from database
	 *
	 * @param Player
	 */
	private static void introduceNewPunishedBotAndClear(Player target)
	{
		Connection con = null;
		try
		{
			
			con = DatabaseFactory.getInstance().getConnection();
			// Introduce new Punished Bot in database
			PreparedStatement statement = con.prepareStatement("INSERT INTO bot_reported_punish VALUES ( ?, ?, ? )");
			statement.setInt(1, target.getObjectId());
			statement.setString(2, target.getPlayerPunish().getBotPunishType().name());
			statement.setLong(3, target.getPlayerPunish().getPunishTimeLeft());
			statement.execute();
			statement.close();
			
			// Delete all his reports from database
			PreparedStatement delStatement = con.prepareStatement("DELETE FROM bot_report WHERE reported_objectId = ?");
			delStatement.setInt(1, target.getObjectId());
			delStatement.execute();
			delStatement.close();
		}
		catch(SQLException sqle)
		{
			sqle.printStackTrace();
		}
		finally
		{
			try
			{
				con.close();
	}
	
			catch(SQLException e)
	{
				// empty
			}
		}
	}
}
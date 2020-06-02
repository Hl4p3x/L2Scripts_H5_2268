package l2s.gameserver.model.entity.CCPHelpers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;


public class CCPSecondaryPassword
{
	private static final Logger _log = LoggerFactory.getLogger(CCPSecondaryPassword.class);
  
	public static void startSecondaryPasswordSetup(Player player, String text)
	{
		StringTokenizer st = new StringTokenizer(text, "|");
		String[] args = new String[st.countTokens()];
		for(int i = 0; i < args.length; i++)
			args[i] = st.nextToken().trim();
		String pageIndex = args[0].substring(args[0].length() - 1);
    
		if(pageIndex.equals("F"))
		{
			if(hasPassword(player))
				sendHtml(player, HtmCache.getInstance().getHtml("command/cfgSPSecondaryChange.htm", player));
			else
				sendHtml(player, HtmCache.getInstance().getHtml("command/cfgSPSecondarySet.htm", player));
			return;
		}
		if(args.length < 2)
		{
			player.sendMessage("Incorrect values!");
			return;
		} 
		switch(pageIndex)
		{
			case "C": 
				String currentPass = args[1];
				String newPass = args.length > 2 ? args[2] : "";
				if(getSecondaryPass(player).equals(currentPass))
					setSecondaryPassword(player, player.getAccountName(), newPass);
				else
					player.kick();
					break;
			case "S": 
				setSecondaryPassword(player, player.getAccountName(), args[1]);
				break;
		}
    
	}
  

	public static void setSecondaryPassword(Player changer, String accountName, String password)
	{
		if(!CCPPasswordRecover.checkInvalidChars(password, false))
		{
			changer.sendMessage("Invalid characters in Password!");
			return;
		}
		Connection con = null;
		PreparedStatement statement = null;		
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE accounts SET secondaryPassword=? WHERE login=?");
			statement.setString(1, password);
			statement.setString(2, accountName);
			statement.execute();
        }
        catch(Exception e)
        {
          _log.info("Error setSecondaryPassword ", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		changer.sendMessage("Password Changed!");
	}
  
	public static boolean tryPass(Player player, String pass)
	{
		String correctPass = getSecondaryPass(player);
		if(pass.equalsIgnoreCase(correctPass))
			return true;
		return false;
	}
  
	public static boolean hasPassword(Player player)
	{
		String pass = getSecondaryPass(player);
		if(pass != null && pass.length() > 0)
			return true;
		return false;
	}
  
	private static void sendHtml(Player player, String html)
	{
		html = html.replace("%online%", CCPSmallCommands.showOnlineCount());
		NpcHtmlMessagePacket msg = new NpcHtmlMessagePacket(0);
		msg.setHtml(html);
		player.sendPacket(msg);
	}
  
	private static String getSecondaryPass(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;	
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT secondaryPassword FROM accounts WHERE login='" + player.getAccountName() + "'");
			rs = statement.executeQuery();
			if(rs.next())
				return rs.getString("secondaryPassword");
		}
		catch(Exception e)
		{
			_log.error("Error in getSecondaryPass ", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
		return null;
	}
}

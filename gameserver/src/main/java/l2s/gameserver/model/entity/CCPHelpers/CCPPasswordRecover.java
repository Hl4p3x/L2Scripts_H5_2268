package l2s.gameserver.model.entity.CCPHelpers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.mail.smtp.SMTPTransport;
import com.sun.net.ssl.internal.ssl.Provider;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;
import l2s.gameserver.utils.Util;

public class CCPPasswordRecover
{
	private static final Logger _log = LoggerFactory.getLogger(CCPPasswordRecover.class);
	private static final int MINUTES_FOR_CODE_ANSWER = 15;
	private static final char[] VALID_CHARS = { 'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm', '@', '.', ' ', '!', '?', '(', ')', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' };
  
	private static Map<Integer, Long[]> _generatedCodes = new HashMap<Integer, Long[]>();
	
	public static void setupFirstStage(Player player, String[] args)
	{
		if(args.length != 4)
		{
			player.sendMessage("Fill every needed information!");
			sendHtml(player, HtmCache.getInstance().getHtml("command/cfgSPRecover.htm", player));
			return;
		}
		if(!checkInvalidChars(args[1], true))
		{
			player.sendMessage("Question is Invalid!");
			sendHtml(player, HtmCache.getInstance().getHtml("command/cfgSPRecover.htm", player));
			return;
		}
		if(!checkInvalidChars(args[2], true))
		{
			player.sendMessage("Answer is Invalid!");
			sendHtml(player, HtmCache.getInstance().getHtml("command/cfgSPRecover.htm", player));
			return;
		}
    
		if(!checkInvalidChars(args[3], true) || !args[3].contains("@") || !args[3].contains(".") || args[3].endsWith("."))
		{
			player.sendMessage("Email is Invalid!");
			sendHtml(player, HtmCache.getInstance().getHtml("command/cfgSPRecover.htm", player));
			return;
		}
		sendEmail(args[3], "Hello!<br>To complete your Password Recovery setup in server, use the code inside TextBox in game!<br>Code: " + generateCode(player) + "<br>Thank You!");
		String html = HtmCache.getInstance().getHtml("command/cfgSPRecoverEmail.htm", player);
		html = html.replace("%question%", args[1]);
		html = html.replace("%answer%", args[2]);
		html = html.replace("%email%", args[3]);
		sendHtml(player, html);
	}

	public static void setupSecondStage(Player player, String[] args)
	{
		if(args.length != 5)
		{
			player.sendMessage("Invalid Code!");
			_generatedCodes.remove(Integer.valueOf(player.getObjectId()));
			return;
		} 
		String code = args[4];
		Long[] correctCode = _generatedCodes.get(Integer.valueOf(player.getObjectId()));
    
		if(correctCode == null || correctCode[0].longValue() + 900000L < System.currentTimeMillis())
		{
			player.sendMessage("Too late! Code is already gone!");
			_generatedCodes.remove(Integer.valueOf(player.getObjectId()));
			return;
		}
    
		long realCode = -1L;
		try
		{
			realCode = Long.parseLong(code);
		}
		catch(NumberFormatException e)
		{
			realCode = -1L;
		}
    
		if(realCode != correctCode[1].longValue())
		{
			player.sendMessage("Invalid Code!");
			_generatedCodes.remove(Integer.valueOf(player.getObjectId()));
			return;
		}
    
		saveRecovery(player, args[1], args[2], args[3]);
		player.sendMessage("Setup finished successfully!");
	}
  
	public static void startPasswordRecover(Player player)
	{
		if(!isRecoveryCompleted(player.getAccountName()))
			sendHtml(player, HtmCache.getInstance().getHtml("command/cfgSPRecover.htm", player));
		else
			reset(player, "cfgSPRecoverConfirmQ | 0");
	}
  
	public static void setup(Player player, String text)
	{
		StringTokenizer st = new StringTokenizer(text, "|");
		String[] args = new String[st.countTokens()];
		for (int i = 0; i < args.length; i++)
			args[i] = st.nextToken().trim();
		String index = args[0].substring(args[0].length() - 1);
		switch (index)
		{
			case "1": 
				setupFirstStage(player, args);
				break;
			case "2": 
				setupSecondStage(player, args);
				break;
		}
    
	}
  
	public static void reset(Player player, String text)
	{
		StringTokenizer st = new StringTokenizer(text, "|");
		String[] args = new String[st.countTokens()];
		for(int i = 0; i < args.length; i++)
			args[i] = st.nextToken().trim();
		String pageIndex = args[0].substring(args[0].length() - 1);
		String nextPage = args[0] + ".htm";
    
		switch (pageIndex)
		{
			case "A": 
				sendHtml(player, HtmCache.getInstance().getHtml("command/" + nextPage, player));
				break;
			case "Q": 
				if(args.length < 2)
				{
					player.sendMessage("Incorrect Values!");
					return;
				}
				resetFirstPage(player, args[1], nextPage);
				break;
			case "E": 
				if(args.length < 3)
				{
					player.sendMessage("Incorrect Values!");
					return;
				}
				resetSecondPage(player, args[1], args[2], nextPage);
				break;
			case "P": 
				if(args.length < 3)
				{
					player.sendMessage("Incorrect Values!");
					return;
				}
				resetThirdPage(player, args[1], args[2], nextPage);
				break;
			case "F": 
				if(args.length < 4)
				{
					player.sendMessage("Incorrect Values!");
					return;
				}
				chooseNewPasses(player, args[1], new String[] { args[2], args[3] });
				break;
		}
	}
  
	public static void resetFirstPage(Player player, String account, String page)
	{
		if(page.startsWith("cfgSPPassword"))
		{
			if(account == null || account.isEmpty() || !account.matches("[A-Za-z0-9]{4,14}"))
			{
				player.sendMessage("Incorrect Account!");
				return;
			}
			if(!isRecoveryCompleted(account))
				player.sendMessage("That account didn't Setup Password Recovery System!");
		}
		else
		{
			account = player.getAccountName();
		}
		String html = HtmCache.getInstance().getHtml("command/" + page, player);
		html = html.replace("%question%", getSecurityQuestion(account));
		html = html.replace("%account%", account);
		sendHtml(player, html);
	}
  
	public static void resetSecondPage(Player player, String account, String answer, String page)
	{
		if(account == null || account.length() < 3)
			account = player.getAccountName();
		String[] emailAns = getSecurityEmailAnswer(account);
		if(answer == null || answer.isEmpty() || !answer.equalsIgnoreCase(emailAns[1]))
		{
			player.kick();
			return;
		}
		String actionDesc = page.startsWith("cfgSPPassword") ? "To choose new Passwords" : "To setup Password Recovery again";
    
		sendEmail(emailAns[0], "Hello!<br>" + actionDesc + " in server, use the code inside TextBox in game!<br>Code: " + generateCode(player) + "<br>Thank You!");
    
		String html = HtmCache.getInstance().getHtml("command/" + page, player);
		html = html.replace("%account%", account);
		sendHtml(player, html);
	}
  
	public static void resetThirdPage(Player player, String account, String code, String page)
	{
		if(account == null || account.length() < 3)
			account = player.getAccountName();
		Long[] correctTimeCode = _generatedCodes.get(player.getObjectId());
		if(code == null || code.isEmpty() || Long.parseLong(code) != correctTimeCode[1].longValue() || correctTimeCode[0].longValue() + 900000L < System.currentTimeMillis())
		{
			player.sendMessage("Invalid Code!");
			return;
		}
    
		if(page.startsWith("cfgSPPasswordChangeP"))
		{
			String html = HtmCache.getInstance().getHtml("command/cfgSPPasswordChangeP.htm", player);
			html = html.replace("%account%", account);
			sendHtml(player, html);
		}
		else if(page.startsWith("cfgSPRecover"))
		{
			saveRecovery(player, "", "", "");
			sendHtml(player, HtmCache.getInstance().getHtml("command/cfgSPRecover.htm", player));
		}
	}
  
	public static void chooseNewPasses(Player player, String account, String[] passes)
	{
		boolean invalid = false;
		if(passes.length != 2)
		{
			invalid = true;
			return;
		} 
		for(int i = 0; i < passes.length; i++)
		{
			if(passes[i].length() < 5 || passes[i].length() > 20)
			{
				player.sendMessage("Incorrect size of the new password!");
				invalid = true;
				break;
			}
			if (!Util.isMatchingRegexp(passes[i], Config.APASSWD_TEMPLATE))
			{
				player.sendMessage("Incorrect value in new password!");
				invalid = true;
				break;
			}
		}
		if(invalid)
		{
			String html = HtmCache.getInstance().getHtml("command/cfgSPPasswordChangeP.htm", player);
			html = html.replace("%account%", account);
			sendHtml(player, html);
		}
		else
		{
			changeLoginPassword(account, passes[0]);
			CCPSecondaryPassword.setSecondaryPassword(player, account, passes[1]);
		}
	}
  
	private static long generateCode(Player player)
	{
		StringBuilder codeBuilder = new StringBuilder();
		for (int i = 0; i < 5; i++)
			codeBuilder.append(l2s.commons.util.Rnd.get(9));
		long toLong = Long.parseLong(codeBuilder.toString());
    
		Long[] array = { Long.valueOf(System.currentTimeMillis()), Long.valueOf(toLong) };
		_generatedCodes.put(Integer.valueOf(player.getObjectId()), array);
    
		return toLong;
	}
  
	private static void sendEmail(final String email, final String message)
	{
		ThreadPoolManager.getInstance().execute(new Runnable()
		{
			public void run()
			{
				try
				{
					CCPPasswordRecover.finalSendMail("l2jserver", "TAleS2283631", email, "", "Server Code!", message);
				}
				catch (MessagingException e)
				{
					CCPPasswordRecover._log.error("Error while sending Email, email:" + email + " message:" + message + " ", e);
				}
			}
		});
	}
  
	protected static boolean checkInvalidChars(String s, boolean sizeCheck)
	{
		if(sizeCheck && (s.length() < 3 || s.length() > 45))
			return false;
    
		char[] chars = s.toLowerCase().toCharArray();
		for(int i = 0; i < chars.length; i++)
		{
			boolean contains = false;
			for(char c : VALID_CHARS)
			{
				if(chars[i] == c)
				{
					contains = true;
					break;
				}
			}
			if(!contains)
				return false;
		}
		return true;
	}
  
	private static void sendHtml(Player player, String html)
	{
		html = html.replace("%online%", CCPSmallCommands.showOnlineCount());
		NpcHtmlMessagePacket msg = new NpcHtmlMessagePacket(0);
		msg.setHtml(html);
		player.sendPacket(msg);
	}
  
	private static String getSecurityQuestion(String accountName)
	{
			
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;	
		String quest = null;	
		try 
		{ 
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT securityQuestion FROM accounts WHERE login='" + accountName + "'");
			rset = statement.executeQuery();
			while(rset.next())
			{
				quest = rset.getString("securityQuestion");
				if(quest != null && quest.length() > 0)
					break;
			}
		}
		catch(Exception e)
		{
			_log.error("Error in isRecoveryCompleted ", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return quest;
	}
  
	private static String[] getSecurityEmailAnswer(String accountName)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;				
		try 
		{ 
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT securityAnswer, email FROM accounts WHERE login='" + accountName + "'");
			rset = statement.executeQuery();
            if(rset.next())
			{
				String answer = rset.getString("securityAnswer");
				String email = rset.getString("email");
				return new String[] { email, answer };
            }
		}
		catch(Exception e)
		{
			_log.error("Error in isRecoveryCompleted ", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}	
		return new String[] { "", "" };
	}
  
	public static boolean isRecoveryCompleted(String accountName)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;		
		boolean contains = false;
		try
		{ 
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT email FROM accounts WHERE login='" + accountName + "'");
			rset = statement.executeQuery();
			while (rset.next())
			{
				String email = rset.getString("email");
				if(email != null && email.length() > 0)
                contains = true;
			}
		}
		catch(Exception e)
		{
			_log.error("Error in isRecoveryCompleted ", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		} 
		return contains;
	}
  
	private static void saveRecovery(Player player, String question, String answer, String email)
	{
		Connection con = null;
		PreparedStatement statement = null;		
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE accounts SET securityQuestion=?,securityAnswer=?,email=? WHERE login=?");Throwable localThrowable4 = null;
			statement.setString(1, question);
			statement.setString(2, answer);
			statement.setString(3, email);
			statement.setString(4, player.getAccountName());
          
			statement.execute();
		}
        catch(Exception e)
        {
           _log.info("Error while saving Recovery, player:" + player.getName() + " question:" + question + " answer:" + answer + " email:" + email, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
  
	private static void changeLoginPassword(String accountName, String newPass)
	{
		Connection con = null;
		PreparedStatement statement = null;			
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE accounts SET password = ? WHERE login = ?");
			statement.setString(1, newPass);
			statement.setString(2, accountName);
			statement.executeUpdate();
		}
		catch(Exception e1)
		{
			e1.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
  
	private static void finalSendMail(String username, String password, String recipientEmail, String ccEmail, String title, String message) throws javax.mail.internet.AddressException, MessagingException
	{
		java.security.Security.addProvider(new Provider());
		String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
    

		Properties props = System.getProperties();
		props.setProperty("mail.smtps.host", "smtp.gmail.com");
		props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.setProperty("mail.smtp.socketFactory.fallback", "false");
		props.setProperty("mail.smtp.port", "465");
		props.setProperty("mail.smtp.socketFactory.port", "465");
		props.setProperty("mail.smtps.auth", "true");
		props.put("mail.smtps.quitwait", "false");
    
		Session session = Session.getInstance(props, null);
		MimeMessage msg = new MimeMessage(session);
    
		msg.setFrom(new InternetAddress(username + "@gmail.com"));
		msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail, false));
		if(ccEmail.length() > 0)
			msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccEmail, false));
    
		msg.setSubject(title);
		msg.setText(message, java.nio.charset.StandardCharsets.UTF_8.displayName(), "html");
		msg.setSentDate(new Date());
    
		SMTPTransport t = (SMTPTransport)session.getTransport("smtps");
    
		t.connect("smtp.gmail.com", username, password);
		t.sendMessage(msg, msg.getAllRecipients());
		t.close();
	}
}

package l2s.gameserver.handler.voicecommands.impl;

import l2s.commons.net.utils.NetList;
import l2s.gameserver.Config;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.authcomm.AuthServerCommunication;
import l2s.gameserver.network.authcomm.gs2as.LockAccountIP;
import l2s.gameserver.scripts.Functions;

/**
 * @Author: SYS
 * @Date: 10/4/2008
 */
public class Lock extends Functions implements IVoicedCommandHandler
{
	private final String[] _commandList = new String[] { "lock" };

	private static String defaultPage = "command/lock.htm";

	private void showDefaultPage(Player activeChar)
	{
		String html = HtmCache.getInstance().getHtml(defaultPage, activeChar);
		html = html.replaceFirst("%IP%", activeChar.getIP());
		show(html, activeChar);
	}

	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if(!Config.SERVICES_LOCK_ACCOUNT_IP)
			return false;
			
		if(command.equals("lock") && (target == null || target.equals("")))
		{
			showDefaultPage(activeChar);
			return true;
		}

		String[] param = target.split(" ");

		if(param.length > 0)
		{
			int time = 60 * 60 * 24 * 7;
			String ip = activeChar.getIP();
			if(param.length > 1)
				for(int i = 1; i < param.length; i++)
					if(param[i].contains("."))
						ip = param[i];
					else
						try
						{
							time = Integer.parseInt(param[1]) * 60 * 60 * 24;
						}
						catch(NumberFormatException e)
						{}

			boolean invalid = false;
			try
			{
				NetList allowedList = new NetList();
				if(!allowedList.matches(activeChar.getIP()))
				{
					activeChar.sendMessage("wtf");
					invalid = true;
				}	
			}
			catch(Exception e)
			{
				e.printStackTrace();
				invalid = true;
			}
			if(invalid)
			{
				activeChar.sendMessage("Invalid IP mask: you 'll be unable to login from your current address!");
				return false;
			}

			time = Math.min(time, 60 * 60 * 24 * 14);
			if(param[0].equalsIgnoreCase("on"))
			{
				AuthServerCommunication.getInstance().sendPacket(new LockAccountIP(activeChar.getAccountName(), ip, time));
				if(activeChar.isLangRus())
					activeChar.sendMessage("Акаунт закрыт для доступа по указанному ИП адресу!");
				else
					activeChar.sendMessage("This account is locked to login only for entered IP address");
					
				return true;
			}

			if(param[0].equalsIgnoreCase("off"))
			{
				AuthServerCommunication.getInstance().sendPacket(new LockAccountIP(activeChar.getAccountName(), "*", -1));
				if(activeChar.isLangRus())
					activeChar.sendMessage("Акаунт открыт для доступа с любого ИП адреса!");
				else
					activeChar.sendMessage("This account is unlocked to login on any IP address");
				return true;
			}
		}

		showDefaultPage(activeChar);
		return true;
	}

	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}
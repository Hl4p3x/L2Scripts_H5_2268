package l2s.gameserver.handler.voicecommands.impl;

import l2s.gameserver.Config;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.authcomm.AuthServerCommunication;
import l2s.gameserver.network.authcomm.gs2as.ChangePassword;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.utils.Util;

/**
 * @Author: Death
 * @Date: 16/6/2007
 * @Time: 11:27:35
 */
public class Password extends Functions implements IVoicedCommandHandler
{
	private String[] _commandList = new String[] { "password" };
	
	public void check(String[] var)
	{
		Player self = getSelf();
		if(var.length != 3)
		{
			show(new CustomMessage("scripts.commands.user.password.IncorrectValues", self), self);
			return;
		}
		useVoicedCommand("password", self, var[0] + " " + var[1] + " " + var[2]);
	}

	public boolean useVoicedCommand(String command, Player player, String args)
	{
		if(command.equals("password") && (args == null || args.equals("")))
		{
			String dialog = "";
			if(Config.SERVICES_CHANGE_PASSWORD)
				dialog = HtmCache.getInstance().getHtml("command/password.htm", player);
			else
				dialog = HtmCache.getInstance().getHtml("command/nopassword.htm", player);
			show(dialog, player);	
			return true;
		}
		
		if(Config.PASSWORD_PAY_ID > 0)
		{
			if(player.getInventory().getCountOf(Config.PASSWORD_PAY_ID) < Config.PASSWORD_PAY_COUNT)
			{
				if(player.isLangRus())
					player.sendMessage("Для того что-бы сменить пароль вам нужно заплатить "+Config.PASSWORD_PAY_COUNT+" "+Config.PASSWORD_PAY_ID+"");
				else
					player.sendMessage("In order to change password you must pay "+Config.PASSWORD_PAY_COUNT+" "+Config.PASSWORD_PAY_ID+"");
				return false;	
			}	
		}
		
		String[] parts = args.split(" ");

		if(parts.length != 3)
		{
			show(new CustomMessage("scripts.commands.user.password.IncorrectValues", player), player);
			return false;
		}

		if(!parts[1].equals(parts[2]))
		{
			show(new CustomMessage("scripts.commands.user.password.IncorrectConfirmation", player), player);
			return false;
		}

		if(parts[1].equals(parts[0]))
		{
			show(new CustomMessage("scripts.commands.user.password.NewPassIsOldPass", player), player);
			return false;
		}

		if(parts[1].length() < 5 || parts[1].length() > 20)
		{
			show(new CustomMessage("scripts.commands.user.password.IncorrectSize", player), player);
			return false;
		}

		if(!Util.isMatchingRegexp(parts[1], Config.APASSWD_TEMPLATE))
		{
			show(new CustomMessage("scripts.commands.user.password.IncorrectInput", player), player);
			return false;
		}

		AuthServerCommunication.getInstance().sendPacket(new ChangePassword(player.getAccountName(), parts[0], parts[1],"0"));
		return true;
	}

	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}

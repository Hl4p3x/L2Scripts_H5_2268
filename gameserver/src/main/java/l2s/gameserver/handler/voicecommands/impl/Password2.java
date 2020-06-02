package l2s.gameserver.handler.voicecommands.impl;

import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.CCPHelpers.CCPPassword;
import l2s.gameserver.scripts.Functions;

public class Password2 extends Functions implements IVoicedCommandHandler
{
	private static final String[] COMMANDS = { "password" };
	
	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		if (args.length() > 0)
		{
			CCPPassword.setNewPassword(player, args.split(" "));
		}
		else
			player.sendMessage("Use it like that: .password oldPassword newPassword newPassword");
		return true;
	}
  
	@Override
	public String[] getVoicedCommandList()
	{
		return COMMANDS;
	}
}

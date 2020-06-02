package l2s.gameserver.handler.voicecommands.impl;

import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.CCPHelpers.CCPSmallCommands;
import l2s.gameserver.scripts.Functions;


public class Ping2 extends Functions implements IVoicedCommandHandler
{
	private static final String[] COMMANDS = new String[0];
  
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		CCPSmallCommands.getPing(activeChar);
		return false;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return COMMANDS;
	}
}

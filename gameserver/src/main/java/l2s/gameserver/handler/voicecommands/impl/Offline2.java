package l2s.gameserver.handler.voicecommands.impl;

import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.CCPHelpers.CCPOffline;
import l2s.gameserver.scripts.Functions;

public class Offline2 extends Functions implements IVoicedCommandHandler
{
	private static final String[] COMMANDS = { "offline" };
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		CCPOffline.setOfflineStore(activeChar);
		return true;
	}
  
	@Override
	public String[] getVoicedCommandList()
	{
		return COMMANDS;
	}
}

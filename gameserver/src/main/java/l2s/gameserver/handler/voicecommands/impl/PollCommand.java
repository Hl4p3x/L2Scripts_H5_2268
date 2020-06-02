package l2s.gameserver.handler.voicecommands.impl;

import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.CCPHelpers.CCPPoll;

public class PollCommand implements IVoicedCommandHandler
{
	private static final String[] COMMANDS = { "poll" };
  
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		CCPPoll.bypass(activeChar, target.split(" "));
		return true;
	}
  
	@Override
	public String[] getVoicedCommandList()
	{
		return COMMANDS;
	}
}

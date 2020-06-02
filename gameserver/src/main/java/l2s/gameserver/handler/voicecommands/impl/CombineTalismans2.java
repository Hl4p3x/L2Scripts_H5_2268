package l2s.gameserver.handler.voicecommands.impl;

import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.CCPHelpers.CCPSmallCommands;
import l2s.gameserver.scripts.Functions;

public class CombineTalismans2 extends Functions implements IVoicedCommandHandler
{
	private static final String[] COMMANDS = { "combinetalismans" };
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		CCPSmallCommands.combineTalismans(activeChar);
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return COMMANDS;
	}
}

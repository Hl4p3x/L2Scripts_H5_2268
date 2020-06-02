package l2s.gameserver.handler.voicecommands.impl;

import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.CCPHelpers.CCPRepair;
import l2s.gameserver.scripts.Functions;

public class Repair2 extends Functions implements IVoicedCommandHandler
{
	private static final String[] COMMANDS = { "repair" };
  
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		CCPRepair.repairChar(activeChar, target);
		return false;
	}
  
	@Override
	public String[] getVoicedCommandList()
	{
		return COMMANDS;
	}
}

package l2s.gameserver.handler.voicecommands.impl;

import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.CCPHelpers.CCPCWHPrivilages;
import l2s.gameserver.scripts.Functions;

public class CWHPrivileges2 extends Functions implements IVoicedCommandHandler
{
	private static final String[] _commandList = { "clan" };

	@Override	
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		CCPCWHPrivilages.clanMain(activeChar, args);
		return false;
	}
  
	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}

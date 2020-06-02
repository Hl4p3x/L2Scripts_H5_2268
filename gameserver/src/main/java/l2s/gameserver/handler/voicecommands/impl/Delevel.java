package l2s.gameserver.handler.voicecommands.impl;

import l2s.gameserver.Config;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.scripts.Functions;

public class Delevel extends Functions implements IVoicedCommandHandler
{
	private final String[] _commandList = new String[] { "delevel" };

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if(!Config.ALLOW_VOICED_COMMANDS)
			return false;
		if(command.equals("delevel"))
		{
			int _old_level = activeChar.getLevel();
			if(_old_level == 1)
				return false;
			Long exp_add = Experience.LEVEL[_old_level-1] - activeChar.getExp();
			activeChar.addExpAndSp(exp_add, 0);
		}
		return false;
	}
}

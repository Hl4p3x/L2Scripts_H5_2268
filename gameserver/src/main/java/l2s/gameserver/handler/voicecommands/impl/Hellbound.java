package l2s.gameserver.handler.voicecommands.impl;

import l2s.gameserver.Config;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.instancemanager.HellboundManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.scripts.Functions;

public class Hellbound extends Functions implements IVoicedCommandHandler
{
	private final String[] _commandList = new String[] { "hellbound" };

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
		if(command.equals("hellbound"))
		{
			activeChar.sendMessage(new CustomMessage("common.Admin.Hellbound.HBLevel", activeChar).addNumber(HellboundManager.getHellboundLevel()));
			activeChar.sendMessage(new CustomMessage("common.Admin.Hellbound.HBPoints", activeChar).addNumber(HellboundManager.getConfidence()));	
		}
		return false;
	}
}

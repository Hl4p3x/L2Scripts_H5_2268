package l2s.gameserver.handler.voicecommands.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import l2s.gameserver.Config;
import l2s.gameserver.GameServer;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.scripts.Functions;


public class ServerInfo extends Functions implements IVoicedCommandHandler
{
	private final String[] _commandList = new String[] { "rev", "ver", "date", "time" };

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

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
		if(command.equals("rev") || command.equals("ver"))
		{
			if(!Config.ALLOW_REVISION_COMMANDS)
				return false;

			activeChar.sendMessage("Project Revision: " + GameServer.PROJECT_REVISION);
			activeChar.sendMessage("Build Revision: " + GameServer.getInstance().getVersion().getRevisionNumber());
			activeChar.sendMessage("Update: " + GameServer.UPDATE_NAME);
			activeChar.sendMessage("Build date: " + GameServer.getInstance().getVersion().getBuildDate());
			return true;
		}
		else if(command.equals("date") || command.equals("time"))
		{
			activeChar.sendMessage(DATE_FORMAT.format(new Date(System.currentTimeMillis())));
			return true;
		}

		return false;
	}
}

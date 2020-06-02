package l2s.gameserver.handler.voicecommands.impl;

import l2s.gameserver.Config;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.tables.FakePlayersTable;

public class Online extends Functions implements IVoicedCommandHandler
{
	private final String[] _commandList = new String[] { "online" };

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if(!Config.ALLOW_VOICED_COMMANDS || !Config.ALLOW_TOTAL_ONLINE)
			return false;
		if(command.equals("online"))
		{
			int i = 0;
			int j = 0;
			for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			{
				i++;
				if(player.isInOfflineMode())
					j++;
			}
			i = i + FakePlayersTable.getActiveFakePlayersCount();	
			if(activeChar.isLangRus())
			{
				activeChar.sendMessage("На сервере играют "+i+" игроков.");
				activeChar.sendMessage("Из них "+j+" находятся в оффлайн торге.");
			}	
			else
			{
				activeChar.sendMessage("Right now there are "+i+" players online.");
				activeChar.sendMessage("From them "+j+" are in offline trade mode.");			
			}
			return true;
		}
		return false;
	}
}

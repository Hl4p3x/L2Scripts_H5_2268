package handler.voicecommands;

import l2s.gameserver.Config;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.scripts.ScriptFile;
import l2s.gameserver.utils.TimeUtils;

public class PremiumState implements IVoicedCommandHandler, ScriptFile
{
	private String[] _commandList = new String[] { "pa" };

	@Override
	public void onLoad()
	{
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
	}

	@Override
	public void onReload()
	{
		//
	}

	@Override
	public void onShutdown()
	{
		//
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		if(!Config.ALLOW_VOICED_COMMANDS)
			return false;
		if(command.equalsIgnoreCase("pa"))
		{
			if(!activeChar.hasPremiumAccount())
			{
				if(activeChar.isLangRus())
					activeChar.sendMessage("Вы еще не купили премиальный аккаунт!");
				else
					activeChar.sendMessage("You do not have a premium account!");
				return true;	
			}		
			else
			{
				String date = TimeUtils.toSimpleFormat(activeChar.getNetConnection().getPremiumAccountExpire() * 1000L);
				if(activeChar.isLangRus())
					activeChar.sendMessage("Ваш премиальный аккаунт истекает: : " + date);
				else
					activeChar.sendMessage("Your premium account expires at : " + date);
				return true;
			}	
		}
		return false;
	}
}
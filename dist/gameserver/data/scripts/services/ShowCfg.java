package services;

import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.scripts.Functions;

public class ShowCfg extends Functions
{

	public void showConfiguration()
	{
		Player player = getSelf();
		IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler("cfg");
		if(vch != null)
		{
			vch.useVoicedCommand("cfg", player, null);
		}	
	}
}
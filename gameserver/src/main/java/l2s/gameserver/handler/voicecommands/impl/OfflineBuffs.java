package l2s.gameserver.handler.voicecommands.impl;

import l2s.gameserver.Config;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;
import l2s.gameserver.scripts.Functions;

public class OfflineBuffs extends Functions implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS = { "buffstore" };
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String params)
	{
		try
		{
			if (!Config.BUFF_STORE_ALLOWED_CLASS_LIST.contains(Integer.valueOf(activeChar.getClassId().getId())))
			{
				activeChar.sendMessage("Your profession is not allowed to set an Buff Store");
				return false;
			}
      

			NpcHtmlMessagePacket html = new NpcHtmlMessagePacket(0);
			html.setFile("command/buffstore/buff_store.htm");
			if (activeChar.getPrivateStoreType() == 20)
			{
				html.replace("%link%", activeChar.isLangRus() ? "Остановить продажу" : "Stop Store");
				html.replace("%bypass%", "bypass -h BuffStore stopstore");
			}
			else
			{
				html.replace("%link%", activeChar.isLangRus() ? "Создать продажу" : "Create Store");
				html.replace("%bypass%", "bypass -h player_help command/buffstore/buff_store_create.htm");
			}
			activeChar.sendPacket(html);
      
			return true;
		}
		catch (Exception e)
		{
			activeChar.sendMessage("Use: .buffstore");
		}
    
		return false;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}

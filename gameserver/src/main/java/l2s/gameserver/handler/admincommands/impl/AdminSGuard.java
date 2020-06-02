package l2s.gameserver.handler.admincommands.impl;

import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.DamageTextPacket;

public class AdminSGuard implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_wdmg,
		admin_wdmgs
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().Menu)
			return false;

		switch(command)
		{
			case admin_wdmg:
				if(wordList.length < 3)
				{
					activeChar.sendMessage("USAGE: //wdmg fontid color");
					return false;
				}

				int fontid = 0;
				int color = 0;
				try
				{
					fontid = Integer.parseInt(wordList[1]);
					color = Integer.parseInt(wordList[2]);
				}
				catch(NumberFormatException e)
				{
					activeChar.sendMessage("You must specify fontid and color rgba");
					return false;
				}
				activeChar.sendPacket(new DamageTextPacket(activeChar.getObjectId(), 1504, false, false, false, false, fontid, color, "", "", 0,0,0,0));
				break;
			case admin_wdmgs:
				if(wordList.length < 3)
				{
					activeChar.sendMessage("USAGE: //wdmg fontid color custom_word");
					return false;
				}

				int fontid1 = 0;
				String word = "";
				try
				{
					fontid1 = Integer.parseInt(wordList[1]);
					word = String.valueOf(wordList[2]);
				}
				catch(NumberFormatException e)
				{
					activeChar.sendMessage("You must specify fontid and color rgba");
					return false;
				}
				activeChar.sendPacket(new DamageTextPacket(activeChar.getObjectId(), 0, false, false, false, false, fontid1, 255, word, "", 0,0,0,0));
				break;
		}
		return true;
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}
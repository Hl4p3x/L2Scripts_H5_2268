package l2s.gameserver.handler.voicecommands.impl;

import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.network.l2.s2c.CastleSiegeInfoPacket;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;

public class Siege implements IVoicedCommandHandler
{
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if(!target.isEmpty())
		{
			int castleId = Integer.parseInt(target);
			Castle castle = (Castle)ResidenceHolder.getInstance().getResidence(castleId);
			activeChar.sendPacket(new CastleSiegeInfoPacket(castle, activeChar));
		}
		showMainPage(activeChar);
		return true;
	}
	
	private static void showMainPage(Player activeChar)
	{
		activeChar.sendPacket(new NpcHtmlMessagePacket(0).setFile("command/siege.htm"));
	}
  
	@Override
	public String[] getVoicedCommandList()
	{
		return new String[] { "siege" };
	}
}

package l2s.gameserver.handler.voicecommands.impl;

import l2s.gameserver.Config;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.scripts.Functions;

/**
 * @Author: Abaddon
 */
public class Help extends Functions implements IVoicedCommandHandler
{
	private String[] _commandList = new String[] { "help", "exp", "whereis" };

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		if(!Config.ALLOW_VOICED_COMMANDS)
			return false;
		command = command.intern();
		if(command.equalsIgnoreCase("help"))
			return help(command, activeChar, args);
		if(command.equalsIgnoreCase("whereis"))
			return whereis(command, activeChar, args);
		if(command.equalsIgnoreCase("exp"))
			return exp(command, activeChar, args);

		return false;
	}

	private boolean exp(String command, Player activeChar, String args)
	{
		if(activeChar.getLevel() >= (activeChar.isSubClassActive() ? Experience.getMaxSubLevel() : Experience.getMaxLevel()))
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Help.MaxLevel", activeChar));
		else
		{
			long exp = Experience.LEVEL[activeChar.getLevel() + 1] - activeChar.getExp();
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Help.ExpLeft", activeChar).addNumber(exp));
		}
		return true;
	}

	private boolean whereis(String command, Player activeChar, String args)
	{
		Player friend = World.getPlayer(args);
		if(friend == null)
			return false;

		if(friend.getParty() == activeChar.getParty() || friend.getClan() == activeChar.getClan())
		{
			activeChar.addRadar(friend.getLoc());
			return true;
		}

		return false;
	}

	private boolean help(String command, Player activeChar, String args)
	{
		String dialog = HtmCache.getInstance().getHtml("command/help.htm", activeChar);
		show(dialog, activeChar);
		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}
package l2s.gameserver.handler.admincommands.impl;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.Event;

public class AdminGlobalEvent implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_list_events
	}
	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands c = (Commands)comm;
		switch(c)
		{
			case admin_list_events:
				GameObject object = activeChar.getTarget();
				if(object == null)
					activeChar.sendPacket(Msg.INVALID_TARGET);
				else
				{
					for(Event e : object.getEvents())
						activeChar.sendMessage("- " + e.toString());
				}
				break;
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}

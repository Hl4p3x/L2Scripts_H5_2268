package l2s.gameserver.handler.admincommands.impl;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.TeamType;

public class AdminTeam implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_setteam
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		TeamType team = TeamType.NONE;
		if(wordList.length >= 2)
		{
			for(TeamType t : TeamType.values())
			{
				if(wordList[1].equalsIgnoreCase(t.name()))
					team = t;
			}
		}

		GameObject object = activeChar.getTarget();
		if(object == null || !object.isCreature())
		{
			activeChar.sendPacket(Msg.INVALID_TARGET);
			return false;
		}

		((Creature)object).setTeam(team);
		return true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}

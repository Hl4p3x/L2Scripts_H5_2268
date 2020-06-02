package l2s.gameserver.handler.usercommands.impl;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.handler.usercommands.IUserCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.SystemMessage;


/**
 * Support for /attacklist /underattacklist /warlist commands
 */
public class ClanWarsList implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS = { 88, 89, 90 };

	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if(id != COMMAND_IDS[0] && id != COMMAND_IDS[1] && id != COMMAND_IDS[2])
			return false;

		Clan clan = activeChar.getClan();
		if(clan == null)
		{
			activeChar.sendPacket(Msg.NOT_JOINED_IN_ANY_CLAN);
			return false;
		}

		SystemMessage sm;
		List<Clan> data = new ArrayList<Clan>();
		if(id == 88)
		{
			// attack list
			activeChar.sendPacket(Msg._ATTACK_LIST_);
			data = clan.getEnemyClans();
		}
		else if(id == 89)
		{
			// under attack list
			activeChar.sendPacket(Msg._UNDER_ATTACK_LIST_);
			data = clan.getAttackerClans();
		}
		else
			// id = 90
		{
			// war list
			activeChar.sendPacket(Msg._WAR_LIST_);
			for(Clan c : clan.getEnemyClans())
				if(clan.getAttackerClans().contains(c))
					data.add(c);
		}

		for(Clan c : data)
		{
			String clanName = c.getName();
			Alliance alliance = c.getAlliance();
			if(alliance != null)
				sm = new SystemMessage(SystemMessage.S1_S2_ALLIANCE).addString(clanName).addString(alliance.getAllyName());
			else
				sm = new SystemMessage(SystemMessage.S1_NO_ALLIANCE_EXISTS).addString(clanName);
			activeChar.sendPacket(sm);
		}

		activeChar.sendPacket(Msg.__EQUALS__);
		return true;
	}

	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
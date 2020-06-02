package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.utils.Log;

/**
 * format: c
 */
public class RequestWithdrawAlly extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		Clan clan = activeChar.getClan();
		if(clan == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(!activeChar.isClanLeader())
		{
			activeChar.sendPacket(Msg.ONLY_THE_CLAN_LEADER_MAY_APPLY_FOR_WITHDRAWAL_FROM_THE_ALLIANCE);
			return;
		}

		if(clan.getAlliance() == null)
		{
			activeChar.sendPacket(Msg.YOU_ARE_NOT_CURRENTLY_ALLIED_WITH_ANY_CLANS);
			return;
		}

		if(clan.equals(clan.getAlliance().getLeader()))
		{
			activeChar.sendPacket(Msg.ALLIANCE_LEADERS_CANNOT_WITHDRAW);
			return;
		}

		clan.broadcastToOnlineMembers(Msg.YOU_HAVE_WITHDRAWN_FROM_THE_ALLIANCE, Msg.A_CLAN_THAT_HAS_WITHDRAWN_OR_BEEN_EXPELLED_CANNOT_ENTER_INTO_AN_ALLIANCE_WITHIN_ONE_DAY_OF_WITHDRAWAL_OR_EXPULSION);
		Alliance alliance = clan.getAlliance();
		clan.setAllyId(0);
		clan.setLeavedAlly();
		alliance.broadcastAllyStatus();
		alliance.removeAllyMember(clan.getClanId());
		Log.LogEvent(activeChar.getName(), "Alliance", "WithdrawFromAlliance", "");
	}
}
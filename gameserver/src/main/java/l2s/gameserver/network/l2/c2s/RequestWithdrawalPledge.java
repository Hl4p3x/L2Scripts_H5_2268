package l2s.gameserver.network.l2.c2s;

import org.apache.commons.lang3.StringUtils;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.UnitMember;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.PledgeShowMemberListDeleteAllPacket;
import l2s.gameserver.network.l2.s2c.PledgeShowMemberListDeletePacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.utils.Log;

public class RequestWithdrawalPledge extends L2GameClientPacket
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

		//is the guy in a clan  ?
		if(activeChar.getClanId() == 0)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInCombat())
		{
			activeChar.sendPacket(Msg.ONE_CANNOT_LEAVE_ONES_CLAN_DURING_COMBAT);
			return;
		}

		Clan clan = activeChar.getClan();
		if(clan == null)
			return;

		UnitMember member = clan.getAnyMember(activeChar.getObjectId());
		if(member == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(member.isClanLeader())
		{
			activeChar.sendPacket(new SystemMessagePacket(SystemMsg.A_CLAN_LEADER_CANNOT_WITHDRAW_FROM_THEIR_OWN_CLAN));
			return;
		}

		DominionSiegeEvent siegeEvent = activeChar.getEvent(DominionSiegeEvent.class);
		if(siegeEvent != null && siegeEvent.isInProgress())
		{
			activeChar.sendPacket(SystemMsg.THIS_CLAN_MEMBER_CANNOT_WITHDRAW_OR_BE_EXPELLED_WHILE_PARTICIPATING_IN_A_TERRITORY_WAR);
			return;
		}

		int subUnitType = activeChar.getPledgeType();

		clan.removeClanMember(subUnitType, activeChar.getObjectId());

		clan.broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.S1_HAS_WITHDRAWN_FROM_THE_CLAN).addString(activeChar.getName()), new PledgeShowMemberListDeletePacket(activeChar.getName()));

		if(subUnitType == Clan.SUBUNIT_ACADEMY)
			activeChar.setLvlJoinedAcademy(0);

		activeChar.setClan(null);
		if(!activeChar.isNoble())
			activeChar.setTitle(StringUtils.EMPTY);

		activeChar.setLeaveClanCurTime();
		activeChar.broadcastCharInfo();

		activeChar.sendPacket(SystemMsg.YOU_HAVE_RECENTLY_BEEN_DISMISSED_FROM_A_CLAN, PledgeShowMemberListDeleteAllPacket.STATIC);
		Log.LogEvent(activeChar.getName(), "Clan", "LeftClan", "");
	}
}
package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.UnitMember;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.PledgeShowMemberListDeleteAllPacket;
import l2s.gameserver.network.l2.s2c.PledgeShowMemberListDeletePacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestOustPledgeMember extends L2GameClientPacket
{
	private String _target;

	@Override
	protected void readImpl()
	{
		_target = readS(16);
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();

		if(activeChar == null || !((activeChar.getClanPrivileges() & Clan.CP_CL_DISMISS) == Clan.CP_CL_DISMISS))
			return;

		Clan clan = activeChar.getClan();
		UnitMember member = clan.getAnyMember(_target);
		if(member == null)
		{
			activeChar.sendPacket(SystemMsg.THE_TARGET_MUST_BE_A_CLAN_MEMBER);
			return;
		}

		Player memberPlayer = member.getPlayer();

		if(member.isOnline() && member.getPlayer().isInCombat())
		{
			activeChar.sendPacket(SystemMsg.A_CLAN_MEMBER_MAY_NOT_BE_DISMISSED_DURING_COMBAT);
			return;
		}

		DominionSiegeEvent siegeEvent = memberPlayer == null ? null : memberPlayer.getEvent(DominionSiegeEvent.class);
		if(siegeEvent != null && siegeEvent.isInProgress())
		{
			activeChar.sendPacket(SystemMsg.A_CLAN_MEMBER_MAY_NOT_BE_DISMISSED_DURING_COMBAT);
			return;
		}

		if(member.isClanLeader())
		{
			activeChar.sendPacket(SystemMsg.THIS_CLAN_MEMBER_CANNOT_WITHDRAW_OR_BE_EXPELLED_WHILE_PARTICIPATING_IN_A_TERRITORY_WAR);
			return;
		}


		int subUnitType = member.getPledgeType();
		clan.removeClanMember(subUnitType, member.getObjectId());
		clan.broadcastToOnlineMembers(new SystemMessage(SystemMessage.CLAN_MEMBER_S1_HAS_BEEN_EXPELLED).addString(_target), new PledgeShowMemberListDeletePacket(_target));
		if(subUnitType != Clan.SUBUNIT_ACADEMY)
			clan.setExpelledMember();

		if(memberPlayer == null)
			return;

		if(subUnitType == Clan.SUBUNIT_ACADEMY)
			memberPlayer.setLvlJoinedAcademy(0);
		memberPlayer.setClan(null);

		if(!memberPlayer.isNoble())
			memberPlayer.setTitle("");

		memberPlayer.setLeaveClanCurTime();

		memberPlayer.broadcastCharInfo();
		//memberPlayer.broadcastRelationChanged();
		memberPlayer.store(true);

		memberPlayer.sendPacket(Msg.YOU_HAVE_RECENTLY_BEEN_DISMISSED_FROM_A_CLAN_YOU_ARE_NOT_ALLOWED_TO_JOIN_ANOTHER_CLAN_FOR_24_HOURS, PledgeShowMemberListDeleteAllPacket.STATIC);
	}
}
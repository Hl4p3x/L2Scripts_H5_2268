package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Request;
import l2s.gameserver.model.Request.L2RequestType;
import l2s.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.SubUnit;
import l2s.gameserver.model.pledge.UnitMember;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.JoinPledgePacket;
import l2s.gameserver.network.l2.s2c.PledgeShowInfoUpdatePacket;
import l2s.gameserver.network.l2.s2c.PledgeShowMemberListAddPacket;
import l2s.gameserver.network.l2.s2c.PledgeSkillListPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.utils.Log;

public class RequestAnswerJoinPledge extends L2GameClientPacket
{
	private int _response;

	@Override
	protected void readImpl()
	{
		_response = _buf.hasRemaining() ? readD() : 0;
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		answerJoinPledge(player, _response != 0);
	}

	public static void answerJoinPledge(Player player, boolean confirm)
	{
		Request request = player.getRequest();
		if(request == null || !request.isTypeOf(L2RequestType.CLAN))
			return;

		if(!request.isInProgress())
		{
			request.cancel();
			player.sendActionFailed();
			return;
		}

		if(player.isOutOfControl())
		{
			request.cancel();
			player.sendActionFailed();
			return;
		}

		Player requestor = request.getRequestor();
		if(requestor == null)
		{
			request.cancel();
			player.sendPacket(SystemMsg.THAT_PLAYER_IS_NOT_ONLINE);
			player.sendActionFailed();
			return;
		}

		if(requestor.getRequest() != request)
		{
			request.cancel();
			player.sendActionFailed();
			return;
		}

		Clan clan = requestor.getClan();
		if(clan == null)
		{
			request.cancel();
			player.sendActionFailed();
			return;
		}

		if(!confirm)
		{
			request.cancel();
			requestor.sendPacket(new SystemMessagePacket(SystemMsg.S1_DECLINED_YOUR_CLAN_INVITATION).addName(player));
			return;
		}

		if(!player.canJoinClan())
		{
			request.cancel();
			player.sendPacket(SystemMsg.AFTER_LEAVING_OR_HAVING_BEEN_DISMISSED_FROM_A_CLAN_YOU_MUST_WAIT_AT_LEAST_A_DAY_BEFORE_JOINING_ANOTHER_CLAN);
			return;
		}

		try
		{
			player.sendPacket(new JoinPledgePacket(requestor.getClanId()));

			int pledgeType = request.getInteger("pledgeType");
			SubUnit subUnit = clan.getSubUnit(pledgeType);
			if(subUnit == null)
				return;

			UnitMember member = new UnitMember(clan, player.getName(), player.getTitle(), player.getLevel(), player.getClassId().getId(), player.getObjectId(), pledgeType, player.getPowerGrade(), player.getApprentice(), player.getSex(), Clan.SUBUNIT_NONE);
			subUnit.addUnitMember(member);

			player.setPledgeType(pledgeType);
			player.setClan(clan);

			member.setPlayerInstance(player, false);

			if(pledgeType == Clan.SUBUNIT_ACADEMY)
				player.setLvlJoinedAcademy(player.getLevel());

			member.setPowerGrade(clan.getAffiliationRank(player.getPledgeType()));

			clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAddPacket(member), player);
			clan.broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.S1_HAS_JOINED_THE_CLAN).addString(player.getName()), new PledgeShowInfoUpdatePacket(clan));

			// this activates the clan tab on the new member
			player.sendPacket(SystemMsg.ENTERED_THE_CLAN);
			player.sendPacket(player.getClan().listAll());
			player.setLeaveClanTime(0);
			player.updatePledgeClass();

			// добавляем скилы игроку, ток тихо
			clan.addSkillsQuietly(player);
			// отображем
			player.sendPacket(new PledgeSkillListPacket(clan));
			player.sendSkillList();

			EventHolder.getInstance().findEvent(player);
			if(clan.getWarDominion() > 0) // баг оффа, после вступа в клан нужен релог для квестов
			{
				DominionSiegeEvent siegeEvent = player.getEvent(DominionSiegeEvent.class);

				siegeEvent.updatePlayer(player, true);
			}
			else
				player.broadcastCharInfo();

			player.store(false);
		}
		finally
		{
			request.done();
		}
		Log.LogEvent(player.getName(), "Clan", "JoinClan", "char: "+player.getName()+" join to clan");
	}
}
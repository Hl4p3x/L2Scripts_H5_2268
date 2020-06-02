package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.CrestCache;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.SystemMsg;

public class RequestSetPledgeCrestLarge extends L2GameClientPacket
{
	private int _length;
	private byte[] _data;

	/**
	 * format: chd(b)
	 */
	@Override
	protected void readImpl()
	{
		_length = readD();
		if(_length == CrestCache.HF_LARGE_CREST_SIZE && _length == _buf.remaining())
		{
			_data = new byte[_length];
			readB(_data);
		}
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		Clan clan = activeChar.getClan();
		if(clan == null)
			return;

		if((activeChar.getClanPrivileges() & Clan.CP_CL_EDIT_CREST) == Clan.CP_CL_EDIT_CREST)
		{
			if(clan.isPlacedForDisband())
			{
				activeChar.sendPacket(SystemMsg.AS_YOU_ARE_CURRENTLY_SCHEDULE_FOR_CLAN_DISSOLUTION_YOU_CANNOT_REGISTER_OR_DELETE_A_CLAN_CREST);
				return;
			}

			int crestId = 0;

			if(_data != null)
			{
				crestId = CrestCache.getInstance().savePledgeCrestLargeHF(clan.getClanId(), _data);
				activeChar.sendPacket(Msg.THE_CLANS_EMBLEM_WAS_SUCCESSFULLY_REGISTERED__ONLY_A_CLAN_THAT_OWNS_A_CLAN_HALL_OR_A_CASTLE_CAN_GET_THEIR_EMBLEM_DISPLAYED_ON_CLAN_RELATED_ITEMS);
			}
			else if(clan.hasCrestLargeHF())
				CrestCache.getInstance().removePledgeCrestLargeHF(clan.getClanId());

			clan.setCrestLargeId(crestId);
			clan.broadcastClanStatus(false, true, false);
		}
	}
}
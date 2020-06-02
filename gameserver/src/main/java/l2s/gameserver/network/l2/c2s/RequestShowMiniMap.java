package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.network.l2.s2c.ShowMinimapPacket;
import l2s.gameserver.utils.ItemFunctions;

public class RequestShowMiniMap extends L2GameClientPacket
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

		// Map of Hellbound
		if(activeChar.isActionBlocked(Zone.BLOCKED_ACTION_MINIMAP) ||
				(activeChar.isInZone("[Hellbound_territory]") && ItemFunctions.getItemCount(activeChar, 9994) == 0))
		{
			activeChar.sendPacket(Msg.THIS_IS_AN_AREA_WHERE_YOU_CANNOT_USE_THE_MINI_MAP_THE_MINI_MAP_WILL_NOT_BE_OPENED);
			return;
		}

		sendPacket(new ShowMinimapPacket(activeChar, 0));
	}
}
package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExGetBookMarkInfoPacket;
import l2s.gameserver.network.l2.s2c.ShortCutInitPacket;

public class RequestBookMarkSlotInfo extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		//just trigger
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		activeChar.sendPacket(new ExGetBookMarkInfoPacket(activeChar));
		activeChar.sendPacket(new ShortCutInitPacket(activeChar));
	}
}
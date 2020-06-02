package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExGetBookMarkInfoPacket;
import l2s.gameserver.network.l2.s2c.ShortCutInitPacket;

public class RequestDeleteBookMarkSlot extends L2GameClientPacket
{
	private int slot;

	@Override
	protected void readImpl()
	{
		slot = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar != null)
		{
			//TODO Msg.THE_SAVED_TELEPORT_LOCATION_WILL_BE_DELETED_DO_YOU_WISH_TO_CONTINUE
			activeChar.bookmarks.remove(slot);
			activeChar.sendPacket(new ExGetBookMarkInfoPacket(activeChar));
			activeChar.sendPacket(new ShortCutInitPacket(activeChar));
		}
	}
}
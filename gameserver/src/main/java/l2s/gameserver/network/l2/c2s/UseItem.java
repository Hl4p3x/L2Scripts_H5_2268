package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.utils.ItemFunctions;

public class UseItem extends L2GameClientPacket
{
	private int _objectId;
	private boolean _ctrlPressed;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_ctrlPressed = readD() == 1;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		activeChar.setActive();

		activeChar.getInventory().writeLock();
		try
		{
			ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
			if(item == null)
			{
				activeChar.sendActionFailed();
				return;
			}
			ItemFunctions.useItem(activeChar, item, _ctrlPressed, true);
		}
		finally
		{
			activeChar.getInventory().writeUnlock();
		}
	}
}
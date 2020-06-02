package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.handler.items.IItemHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.ExAutoSoulShot;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.utils.ItemFunctions;

/**
 * format:		chdd
 * @param decrypt
 */
public class RequestAutoSoulShot extends L2GameClientPacket
{
	private int _itemId;
	private boolean _type; // 1 = on : 0 = off;

	@Override
	protected void readImpl()
	{
		_itemId = readD();
		_type = readD() == 1;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		if(activeChar.getPrivateStoreType() != Player.STORE_PRIVATE_NONE || activeChar.isDead())
			return;

		activeChar.getInventory().writeLock();
		try
		{
			ItemInstance item = activeChar.getInventory().getItemByItemId(_itemId);
			if(item == null)
				return;

			IItemHandler handler = item.getTemplate().getHandler();
			if(handler == null || !handler.isAutoUse())
				return;

			if(_type)
			{
				activeChar.addAutoSoulShot(_itemId);
				activeChar.sendPacket(new ExAutoSoulShot(_itemId, true));
				activeChar.sendPacket(new SystemMessage(SystemMessage.THE_USE_OF_S1_WILL_NOW_BE_AUTOMATED).addItemName(item.getItemId()));
				ItemFunctions.useItem(activeChar, item, false, false);
				return;
			}

			activeChar.removeAutoSoulShot(_itemId);
			activeChar.sendPacket(new ExAutoSoulShot(_itemId, false));
			activeChar.sendPacket(new SystemMessage(SystemMessage.THE_AUTOMATIC_USE_OF_S1_WILL_NOW_BE_CANCELLED).addItemName(item.getItemId()));
		}
		finally
		{
			activeChar.getInventory().writeUnlock();
		}
	}
}
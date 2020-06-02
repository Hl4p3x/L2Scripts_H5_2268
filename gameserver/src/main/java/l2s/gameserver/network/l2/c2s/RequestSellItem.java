package l2s.gameserver.network.l2.c2s;

import org.apache.commons.lang3.ArrayUtils;

import l2s.commons.math.SafeMath;
import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.ExBuySellListPacket;
import l2s.gameserver.utils.Log;

/**
 * packet type id 0x37
 * format:		cddb, b - array if (ddd)
 */
public class RequestSellItem extends L2GameClientPacket
{
	private int _listId;
	private int _count;
	private int[] _items; // object id
	private long[] _itemQ; // count

	@Override
	protected void readImpl()
	{
		_listId = readD();
		_count = readD();
		if(_count * 16 > _buf.remaining() || _count > Short.MAX_VALUE || _count < 1)
		{
			_count = 0;
			return;
		}
		_items = new int[_count];
		_itemQ = new long[_count];

		for(int i = 0; i < _count; i++)
		{
			_items[i] = readD(); // object id
			readD(); //item id
			_itemQ[i] = readQ(); // count
			if(_itemQ[i] < 1 || ArrayUtils.indexOf(_items, _items[i]) < i)
			{
				_count = 0;
				break;
			}
		}
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _count == 0)
			return;

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		if(activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}

		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && activeChar.getKarma() > 0 && !activeChar.isGM())
		{
			activeChar.sendActionFailed();
			return;
		}

		NpcInstance merchant = activeChar.getLastNpc();
		boolean isValidMerchant = merchant != null && merchant.isMerchantNpc();
		if(!Config.BBS_SELL_ITEMS_ENABLED && (!activeChar.isGM() && (merchant == null || !isValidMerchant || !activeChar.checkInteractionDistance(merchant))))
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.getInventory().writeLock();
		activeChar.getRefund().writeLock();
		try
		{
			for(int i = 0; i < _count; i++)
			{
				int objectId = _items[i];
				long count = _itemQ[i];
				if(count <= 0)
					continue;
					
				ItemInstance item = activeChar.getInventory().getItemByObjectId(objectId);
				if(item == null || item.getCount() < count || !item.canBeSold(activeChar))
					continue;

				long price = SafeMath.mulAndCheck(item.getReferencePrice(), count) / Config.ALT_SELL_PRICE_DIV;

				ItemInstance refund = activeChar.getInventory().removeItemByObjectId(objectId, count);
				
				Log.LogEvent(activeChar.getName(), activeChar.getIP(), "RefundItem", "RefundItem Item:", ""+refund.getName()+" count: "+refund.getCount()+" objId: "+refund.getObjectId()+" from NPC " + (merchant == null ? "BBS" : merchant.getNpcId()));				
				Log.LogItem(activeChar, Log.RefundSell, refund);
				activeChar.addAdena(price);
				activeChar.getRefund().addItem(refund);
			}
		}
		catch(ArithmeticException ae)
		{
			sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		finally
		{
			activeChar.getInventory().writeUnlock();
			activeChar.getRefund().writeUnlock();
		}

		double taxRate = 0;
		Castle castle;
		if(merchant != null)
		{
			castle = merchant.getCastle(activeChar);
			if(castle != null)
			{
				taxRate = castle.getTaxRate();
			}
		}

		activeChar.sendPacket(new ExBuySellListPacket.SellRefundList(activeChar, true));
		activeChar.sendChanges();
	}
}
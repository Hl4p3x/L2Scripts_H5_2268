package l2s.gameserver.network.l2.c2s;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import l2s.commons.math.SafeMath;
import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExBuySellListPacket;
import l2s.gameserver.utils.Log;

public class RequestExRefundItem extends L2GameClientPacket
{
	private int _listId;
	private int _count;
	private int[] _items;

	@Override
	protected void readImpl()
	{
		_listId = readD();
		_count = readD();
		if(_count * 4 > _buf.remaining() || _count > Short.MAX_VALUE || _count < 1)
		{
			_count = 0;
			return;
		}
		_items = new int[_count];
		for(int i = 0; i < _count; i++)
		{
			_items[i] = readD();
			if(ArrayUtils.indexOf(_items, _items[i]) < i)
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

		NpcInstance npc = activeChar.getLastNpc();

		boolean isValidMerchant = npc != null && npc.isMerchantNpc();
		if(!activeChar.isGM() && (npc == null || !isValidMerchant || !activeChar.checkInteractionDistance(npc)))
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.getInventory().writeLock();
		activeChar.getRefund().writeLock();
		try
		{
			int slots = 0;
			long weight = 0;
			long totalPrice = 0;

			List<ItemInstance> refundList = new ArrayList<ItemInstance>();
			for(int objId : _items)
			{
				ItemInstance item = activeChar.getRefund().getItemByObjectId(objId);
				if(item == null)
					continue;

				totalPrice = SafeMath.addAndCheck(totalPrice, SafeMath.mulAndCheck(item.getCount(), item.getReferencePrice()) / Config.ALT_SELL_PRICE_DIV);
				weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(item.getCount(), item.getTemplate().getWeight()));

				if(!item.isStackable() || activeChar.getInventory().getItemByItemId(item.getItemId()) == null)
					slots++;

				refundList.add(item);
			}

			if(refundList.isEmpty())
			{
				activeChar.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
				activeChar.sendActionFailed();
				return;
			}

			if(!activeChar.getInventory().validateWeight(weight))
			{
				sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
				activeChar.sendActionFailed();
				return;
			}

			if(!activeChar.getInventory().validateCapacity(slots))
			{
				sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
				activeChar.sendActionFailed();
				return;
			}

			if(!activeChar.reduceAdena(totalPrice))
			{
				activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				activeChar.sendActionFailed();
				return;
			}

			for(ItemInstance item : refundList)
			{
				ItemInstance refund = activeChar.getRefund().removeItem(item);
				Log.LogEvent(activeChar.getName(), activeChar.getIP(), "Mail", "Mail returned with item:", "item name: "+refund.getName()+" count: "+refund.getCount()+", objId("+refund.getObjectId()+")");
				Log.LogItem(activeChar, Log.RefundReturn, refund);
				activeChar.getInventory().addItem(refund);
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
		if(npc != null)
		{
			castle = npc.getCastle(activeChar);
			if(castle != null)
			{
				taxRate = castle.getTaxRate();
			}
		}

		activeChar.sendPacket(new ExBuySellListPacket.SellRefundList(activeChar, true));
		activeChar.sendChanges();
	}
}
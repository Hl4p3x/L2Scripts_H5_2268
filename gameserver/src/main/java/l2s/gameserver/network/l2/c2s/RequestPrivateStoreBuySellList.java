package l2s.gameserver.network.l2.c2s;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.math.SafeMath;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.TradeItem;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.TradeHelper;

/**
 * Список продаваемого в приватный магазин покупки
 * 
 */
public class RequestPrivateStoreBuySellList extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(RequestPrivateStoreBuySellList.class);

	private int _buyerId, _count;
	private int[] _items; // object id
	private long[] _itemQ; // count
	private long[] _itemP; // price

	@Override
	protected void readImpl()
	{
		_buyerId = readD();
		_count = readD();

		if(_count * 28 > _buf.remaining() || _count > Short.MAX_VALUE || _count < 1)
		{
			_count = 0;
			return;
		}

		_items = new int[_count];
		_itemQ = new long[_count];
		_itemP = new long[_count];

		for(int i = 0; i < _count; i++)
		{
			_items[i] = readD();
			readD(); //itemId
			readH();
			readH();
			_itemQ[i] = readQ();
			_itemP[i] = readQ();

			if(_itemQ[i] < 1 || _itemP[i] < 1 || ArrayUtils.indexOf(_items, _items[i]) < i)
			{
				_count = 0;
				break;
			}
		}
	}

	@Override
	protected void runImpl()
	{
		Player seller = getClient().getActiveChar();
		if(seller == null || _count == 0)
			return;

		if(seller.isActionsDisabled())
		{
			seller.sendActionFailed();
			return;
		}

		if(seller.isInStoreMode())
		{
			seller.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		if(seller.isInTrade())
		{
			seller.sendActionFailed();
			return;
		}

		if(seller.isFishing())
		{
			seller.sendPacket(Msg.YOU_CANNOT_DO_ANYTHING_ELSE_WHILE_FISHING);
			return;
		}

		if(!seller.getPlayerAccess().UseTrade)
		{
			seller.sendPacket(Msg.THIS_ACCOUNT_CANOT_USE_PRIVATE_STORES);
			return;
		}

		Player buyer = (Player) seller.getVisibleObject(_buyerId);
		if(buyer == null || buyer.getPrivateStoreType() != Player.STORE_PRIVATE_BUY || !seller.checkInteractionDistance(buyer))
		{
			seller.sendPacket(Msg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
			seller.sendActionFailed();
			return;
		}

		List<TradeItem> buyList = buyer.getBuyList();
		if(buyList.isEmpty())
		{
			seller.sendPacket(Msg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
			seller.sendActionFailed();
			return;
		}

		List<TradeItem> sellList = new ArrayList<TradeItem>();

		long totalCost = 0;
		int slots = 0;
		long weight = 0;

		buyer.getInventory().writeLock();
		seller.getInventory().writeLock();
		try
		{
			loop: for(int i = 0; i < _count; i++)
			{
				int objectId = _items[i];
				long count = _itemQ[i];
				long price = _itemP[i];

				ItemInstance item = seller.getInventory().getItemByObjectId(objectId);
				if(item == null || item.getCount() < count || !item.canBeTraded(seller))
					break loop;

				TradeItem si = null;

				for(TradeItem bi : buyList)
					if(bi.getItemId() == item.getItemId())
						if(bi.getOwnersPrice() == price)
						{
							if(count > bi.getCount())
								break loop;

							totalCost = SafeMath.addAndCheck(totalCost, SafeMath.mulAndCheck(count, price));
							weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(count, item.getTemplate().getWeight()));
							if(!item.isStackable() || buyer.getInventory().getItemByItemId(item.getItemId()) == null)
								slots++;
							
							si = new TradeItem();
							si.setObjectId(objectId);
							si.setItemId(item.getItemId());
							si.setCount(count);
							si.setOwnersPrice(price);
	
							sellList.add(si);
							break;
						}
			}
		}
		catch(ArithmeticException ae)
		{
			//TODO audit
			sellList.clear();
			sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		finally
		{
			try
			{
				if(sellList.size() != _count)
				{
					seller.sendPacket(Msg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
					seller.sendActionFailed();
					return;
				}

				if(!buyer.getInventory().validateWeight(weight))
				{
					buyer.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
					seller.sendPacket(Msg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
					seller.sendActionFailed();
					return;
				}

				if(!buyer.getInventory().validateCapacity(slots))
				{
					buyer.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
					seller.sendPacket(Msg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
					seller.sendActionFailed();
					return;
				}

				if(!buyer.reduceAdena(totalCost))
				{
					buyer.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
					seller.sendPacket(Msg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
					seller.sendActionFailed();
					return;
				}

				ItemInstance item;
				for(TradeItem si : sellList)
				{
					item = seller.getInventory().removeItemByObjectId(si.getObjectId(), si.getCount());
					for(TradeItem bi : buyList)
						if(bi.getItemId() == si.getItemId())
							if(bi.getOwnersPrice() == si.getOwnersPrice())
							{
								bi.setCount(bi.getCount() - si.getCount());
								if(bi.getCount() < 1L)
									buyList.remove(bi);
								break;
							}
					Log.LogItem(seller, Log.PrivateStoreSell, item);
					Log.LogItem(buyer, Log.PrivateStoreBuy, item);
					Log.LogEvent(seller.getName(), seller.getIP(), "PrivateStore", "Sold Item:", ""+item.getName()+" count: "+item.getCount()+" objId: "+item.getObjectId()+" to "+buyer.getName()+"");
					Log.LogEvent(buyer.getName(), buyer.getIP(), "PrivateStore", "Bought Item:", ""+item.getName()+" count: "+item.getCount()+" objId: "+item.getObjectId()+" from "+buyer.getName()+"");
					buyer.getInventory().addItem(item);
					TradeHelper.purchaseItem(buyer, seller, si);
				}

				long tax = TradeHelper.getTax(seller, totalCost);
				if(tax > 0)
				{
					totalCost -= tax;
					seller.sendMessage(new CustomMessage("trade.HavePaidTax", seller).addNumber(tax));
				}

				seller.addAdena(totalCost);
				buyer.saveTradeList();
			}
			finally
			{
				seller.getInventory().writeUnlock();
				buyer.getInventory().writeUnlock();
			}
		}

		if(buyList.isEmpty())
			TradeHelper.cancelStore(buyer);

		seller.sendChanges();
		buyer.sendChanges();

		seller.sendActionFailed();
	}
}
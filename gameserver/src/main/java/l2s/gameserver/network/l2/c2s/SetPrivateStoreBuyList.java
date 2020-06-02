package l2s.gameserver.network.l2.c2s;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import l2s.commons.math.SafeMath;
import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.TradeItem;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.PrivateStoreBuyManageList;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.TradeHelper;

public class SetPrivateStoreBuyList extends L2GameClientPacket
{
	private int _count;
	private int[] _items; // item id
	private long[] _itemQ; // count
	private long[] _itemP; // price

	@Override
	protected void readImpl()
	{
		_count = readD();
		if(_count * 40 > _buf.remaining() || _count > Short.MAX_VALUE || _count < 1)
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

			readH();
			readH();

			_itemQ[i] = readQ();
			_itemP[i] = readQ();

			if(_itemQ[i] < 1 || _itemP[i] < 1)
			{
				_count = 0;
				break;
			}

			// TODO Gracia Final
			readC(); // FE
			readD(); // FF 00 00 00
			readD(); // 00 00 00 00

			readC(); // Unknown 7 bytes
			readC();
			readC();
			readC();
			readC();
			readC();
			readC();
		}
	}

	@Override
	protected void runImpl()
	{
		Player buyer = getClient().getActiveChar();
		if(buyer == null || _count == 0)
			return;

		if(!TradeHelper.checksIfCanOpenStore(buyer, Player.STORE_PRIVATE_BUY))
		{
			buyer.sendActionFailed();
			return;
		}

		if(buyer.getTransformation() != 0)
		{
			buyer.sendActionFailed();
			return;
		}

		List<TradeItem> buyList = new CopyOnWriteArrayList<TradeItem>();
		long totalCost = 0;
		try
		{
			loop: for(int i = 0; i < _count; i++)
			{
				int itemId = _items[i];
				long count = _itemQ[i];
				long price = _itemP[i];

				ItemTemplate item = ItemHolder.getInstance().getTemplate(itemId);

				if(item == null || itemId == ItemTemplate.ITEM_ID_ADENA)
					continue;

				if(item.getReferencePrice() / Config.ALT_SELL_PRICE_DIV > price)
				{
					buyer.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.SetPrivateStoreBuyList.TooLowPrice", buyer).addItemName(item).addNumber(item.getReferencePrice() / Config.ALT_SELL_PRICE_DIV));
					continue;
				}

				if(item.isStackable())
					for(TradeItem bi : buyList)
						if(bi.getItemId() == itemId)
						{
							bi.setOwnersPrice(price);
							bi.setCount(bi.getCount() + count);
							totalCost = SafeMath.addAndCheck(totalCost, SafeMath.mulAndCheck(count, price));
							continue loop;
						}

				TradeItem bi = new TradeItem();
				bi.setItemId(itemId);
				bi.setCount(count);
				bi.setOwnersPrice(price);
				totalCost = SafeMath.addAndCheck(totalCost, SafeMath.mulAndCheck(count, price));
				buyList.add(bi);
			}
		}
		catch(ArithmeticException ae)
		{
			//TODO audit
			buyer.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}

		if(buyList.size() > buyer.getTradeLimit())
		{
			buyer.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			buyer.sendPacket(new PrivateStoreBuyManageList(1, buyer));
			buyer.sendPacket(new PrivateStoreBuyManageList(2, buyer));
			return;
		}

		if(totalCost > buyer.getAdena())
		{
			buyer.sendPacket(Msg.THE_PURCHASE_PRICE_IS_HIGHER_THAN_THE_AMOUNT_OF_MONEY_THAT_YOU_HAVE_AND_SO_YOU_CANNOT_OPEN_A_PERSONAL_STORE);
			buyer.sendPacket(new PrivateStoreBuyManageList(1, buyer));
			buyer.sendPacket(new PrivateStoreBuyManageList(2, buyer));
			return;
		}

		if(!buyList.isEmpty())
		{
			buyer.setBuyList(buyList);
			buyer.saveTradeList();
			buyer.setPrivateStoreType(Player.STORE_PRIVATE_BUY);
			buyer.broadcastPrivateStoreInfo();
			buyer.sitDown(null);
			buyer.broadcastCharInfo();
		}

		buyer.sendActionFailed();
	}
}
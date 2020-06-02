package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.ProductHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.ProductItem;
import l2s.gameserver.model.ProductItemComponent;
import l2s.gameserver.network.l2.s2c.ExBR_BuyProductPacket;
import l2s.gameserver.network.l2.s2c.ExBR_GamePointPacket;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.Log;

public class RequestExBR_BuyProduct extends L2GameClientPacket
{
	private int _productId;
	private int _count;

	@Override
	protected void readImpl()
	{
		_productId = readD();
		_count = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		if(_count > 99 || _count <= 0)
			return;

		ProductItem product = ProductHolder.getInstance().getProduct(_productId);
		if(product == null)
		{
			activeChar.sendPacket(new ExBR_BuyProductPacket(ExBR_BuyProductPacket.RESULT_WRONG_PRODUCT));
			return;
		}

		if(!product.isOnSale() || (System.currentTimeMillis() < product.getStartTimeSale()) || (System.currentTimeMillis() > product.getEndTimeSale()))
		{
			activeChar.sendPacket(new ExBR_BuyProductPacket(ExBR_BuyProductPacket.RESULT_BUY_BEFORE_SALE_DATE));
			return;
		}

		final int pointsRequired = product.getPoints() * _count;
		if(pointsRequired <= 0)
		{
			activeChar.sendPacket(new ExBR_BuyProductPacket(ExBR_BuyProductPacket.RESULT_WRONG_PRODUCT));
			return;
		}

		final long pointsCount = activeChar.getPremiumPoints();
		if(pointsRequired > pointsCount)
		{
			activeChar.sendPacket(new ExBR_BuyProductPacket(ExBR_BuyProductPacket.RESULT_NOT_ENOUGH_POINTS));
			return;
		}

		int totalWeight = 0;
		for(ProductItemComponent com : product.getComponents())
			totalWeight += com.getWeight();

		totalWeight *= _count; //увеличиваем вес согласно количеству

		int totalCount = 0;

		for(ProductItemComponent com : product.getComponents())
		{
			ItemTemplate item = ItemHolder.getInstance().getTemplate(com.getItemId());
			if(item == null)
			{
				activeChar.sendPacket(new ExBR_BuyProductPacket(ExBR_BuyProductPacket.RESULT_WRONG_PRODUCT));
				return; //what
			}
			totalCount += item.isStackable() ? 1 : com.getCount() * _count;
		}

		if(!activeChar.getInventory().validateCapacity(totalCount) || !activeChar.getInventory().validateWeight(totalWeight))
		{
			activeChar.sendPacket(new ExBR_BuyProductPacket(ExBR_BuyProductPacket.RESULT_INVENTORY_FULL));
			return;
		}

		if(!activeChar.reducePremiumPoints(pointsRequired))
		{
			activeChar.sendPacket(new ExBR_BuyProductPacket(ExBR_BuyProductPacket.RESULT_NOT_ENOUGH_POINTS));
			return;
		}

		for(ProductItemComponent $comp : product.getComponents())
		{
			activeChar.getInventory().addItem($comp.getItemId(), $comp.getCount() * _count);
			Log.LogEvent(activeChar.getName(), "ItemMall", "buyItems", "bought: " + ($comp.getCount() * _count) + " of " + $comp.getItemId());
			Log.LogItem(activeChar, Log.ItemMallBuy, $comp.getItemId(), $comp.getCount() * _count);
		}

		activeChar.updateRecentProductList(_productId);

		activeChar.sendPacket(new ExBR_GamePointPacket(activeChar));
		activeChar.sendPacket(new ExBR_BuyProductPacket(ExBR_BuyProductPacket.RESULT_OK));
		activeChar.sendChanges();
	}
}
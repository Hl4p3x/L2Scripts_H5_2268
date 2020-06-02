package l2s.gameserver.network.l2.s2c;

import java.util.Collection;

import l2s.gameserver.data.xml.holder.ProductHolder;
import l2s.gameserver.model.ProductItem;

public class ExBR_ProductListPacket extends L2GameServerPacket
{

	@Override
	protected void writeImpl()
	{
		Collection<ProductItem> items = ProductHolder.getInstance().getAllItems();
		writeD(items.size());

		for(ProductItem template : items)
		{
			if(System.currentTimeMillis() < template.getStartTimeSale())
			{
				continue;
			}

			if(System.currentTimeMillis() > template.getEndTimeSale())
				continue;
			
			writeD(template.getProductId());	//product id
			writeH(template.getCategory());	//category 1 - enchant 2 - supplies  3 - decoration 4 - package 5 - other
			writeD(template.getPoints());	//points
			writeD(template.getTabId());	// show tab 2-th group - 1 показывает окошко про итем
			writeD((int) (template.getStartTimeSale() / 1000));	// start sale unix date in seconds
			writeD((int) (template.getEndTimeSale() / 1000));	// end sale unix date in seconds
			writeC(127);	// day week (127 = not daily goods)
			writeC(template.getStartHour());	// start hour
			writeC(template.getStartMin());	// start min
			writeC(template.getEndHour());	// end hour
			writeC(template.getEndMin());	// end min
			writeD(template.getStock()); // stock
			writeD(template.getMaxStock()); // max stock
		}
	}	
}
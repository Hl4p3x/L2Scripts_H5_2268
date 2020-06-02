package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.data.xml.holder.ProductHolder;
import l2s.gameserver.model.ProductItem;
import l2s.gameserver.model.ProductItemComponent;

public class ExBR_ProductInfoPacket extends L2GameServerPacket
{
	private ProductItem _productId;

	public ExBR_ProductInfoPacket(int id)
	{
		_productId = ProductHolder.getInstance().getProduct(id);
	}  	

	@Override
	protected void writeImpl()
	{
		if(_productId == null)
			return;

		writeD(_productId.getProductId());  //product id
		writeD(_productId.getPoints());	  // points
		writeD(_productId.getComponents().size());	   //size

		for(ProductItemComponent com : _productId.getComponents())
		{
			writeD(com.getItemId());   //item id
			writeD(com.getCount());  //quality
			writeD(com.getWeight()); //weight
			writeD(com.isDropable() ? 1 : 0); //0 - dont drop/trade
		}
	}
}
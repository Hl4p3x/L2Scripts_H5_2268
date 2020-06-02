package handler.pccoupon;

import l2s.commons.string.StringArrayUtils;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author Bonux
**/
public class AddItems extends ScriptPCCouponHandler
{
	@Override
	public int getType()
	{
		return 2;
	}

	@Override
	public boolean useCoupon(Player player, String value)
	{
		boolean success = false;
		long[][] items = StringArrayUtils.stringToLong2X(value, ";", "-");
		for(long[] item : items)
		{
			if(item.length == 0)
				continue;

			int itemId = (int) item[0];
			long count = item.length > 1 ? item[1] : 1L;
			if(!ItemFunctions.addItem(player, itemId, count, false, "Add items by coupon code.").isEmpty())
			{
				success = true;
				player.sendPacket(new SystemMessagePacket(SystemMsg.CONGRATULATIONS_YOU_HAVE_RECEIVED_S1).addItemName(itemId));
			}
		}
		return success;
	}
}

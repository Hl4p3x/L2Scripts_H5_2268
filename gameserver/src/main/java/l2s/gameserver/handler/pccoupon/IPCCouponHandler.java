package l2s.gameserver.handler.pccoupon;

import l2s.gameserver.model.Player;

/**
 * @author Bonux
**/
public interface IPCCouponHandler
{
	public int getType();

	public boolean useCoupon(Player player, String value);
}

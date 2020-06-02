package handler.pccoupon;

import l2s.gameserver.model.Player;

/**
 * @author Bonux
**/
public class AddPCBangPoints extends ScriptPCCouponHandler
{
	@Override
	public int getType()
	{
		return 1;
	}

	@Override
	public boolean useCoupon(Player player, String value)
	{
		try
		{
			player.addPcBangPoints(Integer.parseInt(value), false, true);
		}
		catch(Exception e)
		{
			return false;
		}
		return true;
	}
}

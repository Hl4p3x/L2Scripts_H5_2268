package handler.pccoupon;

import l2s.gameserver.handler.pccoupon.IPCCouponHandler;
import l2s.gameserver.handler.pccoupon.PCCouponHandler;
import l2s.gameserver.scripts.ScriptFile;

/**
 * @author Bonux
**/
public abstract class ScriptPCCouponHandler implements ScriptFile, IPCCouponHandler
{
	@Override
	public void onLoad()
	{
		PCCouponHandler.getInstance().registerHandler(this);
	}

	@Override
	public void onReload()
	{
		//
	}

	@Override
	public void onShutdown()
	{
		//
	}
}

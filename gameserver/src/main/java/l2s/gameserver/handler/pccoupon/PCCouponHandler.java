package l2s.gameserver.handler.pccoupon;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bonux
**/
public class PCCouponHandler
{
	private static final Logger _log = LoggerFactory.getLogger(PCCouponHandler.class);
	private static final PCCouponHandler _instance = new PCCouponHandler();

	private final IntObjectMap<IPCCouponHandler> _handlers = new HashIntObjectMap<IPCCouponHandler>();

	public static PCCouponHandler getInstance()
	{
		return _instance;
	}

	private PCCouponHandler()
	{
		//
	}

	public void registerHandler(IPCCouponHandler handler)
	{
		if(_handlers.containsKey(handler.getType()))
		{
			_log.warn(getClass().getSimpleName() + ": dublicate bypass registered! First handler: " + _handlers.get(handler.getType()).getClass().getSimpleName() + " second: " + handler.getClass().getSimpleName());
			return;
		}
		_handlers.put(handler.getType(), handler);
	}

	public void removeHandler(IPCCouponHandler handler)
	{
		if(_handlers.remove(handler.getType()) != null)
			_log.info(getClass().getSimpleName() + ": " + handler.getClass().getSimpleName() + " unloaded.");
	}

	public IPCCouponHandler getHandler(int type)
	{
		return _handlers.get(type);
	}
}

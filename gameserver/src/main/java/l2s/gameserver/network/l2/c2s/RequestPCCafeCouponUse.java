package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.instancemanager.PCCafeCouponManager;
import l2s.gameserver.model.Player;

/**
 * format: chS
 */
public class RequestPCCafeCouponUse extends L2GameClientPacket
{
	// format: (ch)S
	private String _couponCode;

	@Override
	protected void readImpl()
	{
		_couponCode = readS();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		PCCafeCouponManager.getInstance().requestEnterCode(player, _couponCode);
	}
}
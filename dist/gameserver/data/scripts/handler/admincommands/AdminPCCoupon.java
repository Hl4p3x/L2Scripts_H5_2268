package handler.admincommands;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.instancemanager.PCCafeCouponManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.scripts.Functions;

/**
 * @author Bonux
**/
public class AdminPCCoupon extends ScriptAdminCommand
{
	private static final Logger _log = LoggerFactory.getLogger(AdminPCCoupon.class);

	public enum Commands
	{
		admin_generate_pc_coupons
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player player)
	{
		if(!player.getPlayerAccess().UseGMShop)
			return false;

		Commands command = (Commands) comm;
		switch(command)
		{
			case admin_generate_pc_coupons:
				try
				{
					int count = Integer.parseInt(wordList[1]);
					int type = Integer.parseInt(wordList[2]);
					String value = fullString.substring(fullString.indexOf(wordList[3]));
					List<String> codes = PCCafeCouponManager.getInstance().generateCodes(count, type, value);
					player.sendMessage("Success generated " + codes.size() + " coupon codes (TYPE[" + type + "] VALUE[" + value + "]).");
				}
				catch(Exception e)
				{
					Functions.sendDebugMessage(player, "USAGE: //generate_pc_coupons [COUNT] [TYPE] [VALUE]");
				}
				break;
		}
		return true;
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}

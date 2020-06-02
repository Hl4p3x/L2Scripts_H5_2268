package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.templates.Henna;
import l2s.gameserver.utils.Log;

public class RequestHennaUnequip extends L2GameClientPacket
{
	private int _symbolId;

	/**
	 * format: d
	 */
	@Override
	protected void readImpl()
	{
		_symbolId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		for(int i = 1; i <= 3; i ++)
		{
			Henna henna = player.getHenna(i);
			if(henna == null)
				continue;

			if(henna.getSymbolId() == _symbolId)
			{
				long price = henna.getPrice() / 5;
				if(player.getAdena() < price)
				{
					player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
					break;
				}

				player.reduceAdena(price);

				player.removeHenna(i);
				Log.LogEvent(player.getName(), "Henna", "RemoveHena", "Removed Hena ID "+_symbolId+"");

				player.sendPacket(SystemMsg.THE_SYMBOL_HAS_BEEN_DELETED);
				break;
			}
		}
	}
}
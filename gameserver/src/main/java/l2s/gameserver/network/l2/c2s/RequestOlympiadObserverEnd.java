package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.utils.Log;

public class RequestOlympiadObserverEnd extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.getObserverMode() == Player.OBSERVER_STARTED)
			if(activeChar.getOlympiadObserveGame() != null)
				activeChar.leaveOlympiadObserverMode(true);
		Log.LogEvent(activeChar.getName(), "Olympiad", "LeftObserverMode", "");			
	}
}
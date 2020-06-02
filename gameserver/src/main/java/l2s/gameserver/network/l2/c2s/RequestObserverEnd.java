package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.utils.Log;

public class RequestObserverEnd extends L2GameClientPacket
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
			if(activeChar.getOlympiadGame() != null)
				activeChar.leaveOlympiadObserverMode(true);
			else
				activeChar.leaveObserverMode();
		Log.LogEvent(activeChar.getName(), "Olympiad", "LeftObserverMode", "");		
	}
}
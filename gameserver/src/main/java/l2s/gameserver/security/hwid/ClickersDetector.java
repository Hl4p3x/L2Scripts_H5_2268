package l2s.gameserver.security.hwid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.c2s.EnterWorld;

public class ClickersDetector
{
	private static final Logger _log = LoggerFactory.getLogger(EnterWorld.class);
	public ClickersDetector(Player player) {}
	
	public static void botPunish(Player player, int reason)
	{
		if (player.getHwidGamer() == null)
			return;
		if(reason >= 0 && reason <= 10)
		{}
	}
  
	private static class Kick extends RunnableImpl
	{
		private String HWID;
    
		private Kick(String hwid)
		{
			HWID = hwid;
		}
    
		public void runImpl() throws Exception
		{
			for(Player player : HwidEngine.getInstance().getGamerByHwid(HWID).getOnlineChars())
			{
				if(player.getNetConnection() != null)
					player.getNetConnection().closeNow(false);
			}
		}
	}
}
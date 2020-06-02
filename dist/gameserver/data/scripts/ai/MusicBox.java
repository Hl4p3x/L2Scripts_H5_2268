package ai;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.NpcAI;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.PlaySoundPacket;

/**
 * @author PaInKiLlEr
 *         - AI для Music Box (32437).
 *         - Проигровает музыку.
 *         - AI проверен и работает.
 */
public class MusicBox extends NpcAI
{
	public MusicBox(NpcInstance actor)
	{
		super(actor);
		ThreadPoolManager.getInstance().schedule(new ScheduleMusic(), 1000);
	}

	private class ScheduleMusic implements Runnable
	{
		@Override
		public void run()
		{
			NpcInstance actor = getActor();
			for(Player player : World.getAroundPlayers(actor, 5000, 5000))
				player.broadcastPacket(new PlaySoundPacket("TP04_F"));
		}
	}
}
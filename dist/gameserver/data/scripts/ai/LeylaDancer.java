package ai;

import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.SocialActionPacket;
import l2s.gameserver.utils.Location;

/**
 * @author PaInKiLlEr
 *         - AI для Танцоров (32424, 32425, 32426, 32427, 32428, 32432).
 *         - Показывает социалки, кричат в чат.
 *         - AI проверен и работает.
 */
public class LeylaDancer extends DefaultAI
{
	private static int count = 0;

	public LeylaDancer(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn()
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return;

		ThreadPoolManager.getInstance().schedule(new ScheduleStart(), 5000);
		ThreadPoolManager.getInstance().schedule(new ScheduleMoveFinish(), 220000);
		super.onEvtSpawn();
	}

	private class ScheduleStart implements Runnable
	{
		@Override
		public void run()
		{
			NpcInstance actor = getActor();
			if(actor != null)
			{
				if(count < 50)
				{
					count++;
					actor.broadcastPacket(new SocialActionPacket(actor.getObjectId(), Rnd.get(1, 2)));
					ThreadPoolManager.getInstance().schedule(new ScheduleStart(), 3600);
				}
				else
				{
					count = 0;
				}
			}
		}
	}

	private class ScheduleMoveFinish implements Runnable
	{
		@Override
		public void run()
		{
			NpcInstance actor = getActor();
			if(actor != null)
			{
				// actor.say(NPC_STRING.WE_LOVE_YOU, CHAT_TYPES.ALL);
				addTaskMove(new Location( -56594, -56064, -1988), true, false);
				doTask();
			}
		}
	}
}
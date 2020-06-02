package l2s.gameserver.model.actor.recorder;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.NpcInfoAbnormalVisualEffect;

/**
 * @author G1ta0
 */
public class NpcStatsChangeRecorder extends CharStatsChangeRecorder<NpcInstance>
{
	public NpcStatsChangeRecorder(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onSendChanges()
	{
		super.onSendChanges();

		if ((_changes & BROADCAST_CHAR_INFO) == BROADCAST_CHAR_INFO)
		{
			_activeChar.broadcastCharInfo();
			//TODOGOD
			_activeChar.broadcastPacket(new NpcInfoAbnormalVisualEffect(_activeChar));
		}
	}
}

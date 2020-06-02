package ai.hellbound;

import l2s.gameserver.ai.Fighter;
import l2s.gameserver.instancemanager.naia.NaiaCoreManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @author pchayka
 */
public class Epidos extends Fighter
{

	public Epidos(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		NaiaCoreManager.removeSporesAndSpawnCube();
		NaiaSpore.resetEpidosStats();
		super.onEvtDead(killer);
	}
}
package ai.Event.Portals;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.NpcUtils;

public class Invader extends Fighter
{
	private int CHEST = 40004;

	public Invader(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcUtils.spawnSingle(CHEST, getActor().getX() + Rnd.get(200), getActor().getY() + Rnd.get(200), getActor().getZ(), 300000);
		NpcUtils.spawnSingle(CHEST, getActor().getX() + Rnd.get(200), getActor().getY() + Rnd.get(200), getActor().getZ(), 300000);
		NpcUtils.spawnSingle(CHEST, getActor().getX() + Rnd.get(200), getActor().getY() + Rnd.get(200), getActor().getZ(), 300000);
		NpcUtils.spawnSingle(CHEST, getActor().getX() + Rnd.get(200), getActor().getY() + Rnd.get(200), getActor().getZ(), 300000);

		super.onEvtDead(killer);
	}
}
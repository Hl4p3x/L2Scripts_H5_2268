package ai;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.Ranger;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.scripts.Functions;

/**
 * AI для Karul Bugbear ID: 20600
 *
 * @author Diamond
 */
public class KarulBugbear extends Ranger
{
	private boolean _firstTimeAttacked = true;

	public KarulBugbear(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn()
	{
		_firstTimeAttacked = true;
		super.onEvtSpawn();
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(_firstTimeAttacked)
		{
			_firstTimeAttacked = false;
			if(Rnd.chance(25))
				Functions.npcSay(actor, NpcString.YOUR_REAR_IS_PRACTICALLY_UNGUARDED);
		}
		else if(Rnd.chance(10))
			Functions.npcSay(actor, NpcString.S1_WATCH_YOUR_BACK, attacker.getPlayer().getName());
		super.onEvtAttacked(attacker, damage);
	}
}
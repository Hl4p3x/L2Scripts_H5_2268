package ai.hellbound;

import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.instances.NpcInstance;

public class OutpostGuards extends Fighter
{
	public OutpostGuards(NpcInstance actor)
	{
		super(actor);
		actor.startImmobilized();
	}
}
package ai.dragonvalley;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.Mystic;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.NpcUtils;

/**
 * @author Aristo После каждой атаки имеет шанс призвать одного из двух мобов.
 */
public class Necromancer extends Mystic
{
	public Necromancer(NpcInstance actor)
	{
        super(actor);
    }

    @Override
	protected void onEvtDead(Creature killer)
	{
		super.onEvtDead(killer);
		if(Rnd.chance(30))
		{
            NpcInstance n = NpcUtils.spawnSingle(Rnd.chance(50) ? 22818 : 22819, getActor().getLoc());
            n.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, 2);
        }
    }
}
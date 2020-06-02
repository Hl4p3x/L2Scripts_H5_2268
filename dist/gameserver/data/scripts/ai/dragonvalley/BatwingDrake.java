package ai.dragonvalley;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ai.Mystic;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @author Aristo
 * Batwing Drake(22827)
 * Спавнит пиявок(22828), когда атакован.
 */
public class BatwingDrake extends Mystic {

    public BatwingDrake(NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onEvtAttacked(Creature attacker, int damage)
	{
        if (Rnd.chance(Config.BDRAKE_MS_CHANCE)) 
		{
            NpcInstance actor = getActor();
           // NpcInstance n = NpcUtils.spawnSingle(22828, (actor.getX() + Rnd.get(-100, 100)), (actor.getY() + Rnd.get(-100, 100)), actor.getZ());
            //n.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 2);
        }
        super.onEvtAttacked(attacker, damage);
    }
}

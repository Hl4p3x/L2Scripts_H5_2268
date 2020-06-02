package ai.dragonvalley;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @author Aristo
 * Drakos Hunter(22862)
 * Спавнит пачки из четырех Drakos Assassin(22823) когда атакован.
 */
public class DrakosHunter extends Fighter {

    public DrakosHunter(NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onEvtAttacked(Creature attacker, int damage) {
        if (Rnd.chance(Config.DHUNTER_MS_CHANCE))
		{
            NpcInstance actor = getActor();
           /* for (int i = 0; i < 4; i++) 
			{
                NpcInstance n = NpcUtils.spawnSingle(22823, (actor.getX() + Rnd.get(-100, 100)), (actor.getY() + Rnd.get(-100, 100)), actor.getZ());
                n.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 2);
            }
			*/
        }
        super.onEvtAttacked(attacker, damage);
    }
}

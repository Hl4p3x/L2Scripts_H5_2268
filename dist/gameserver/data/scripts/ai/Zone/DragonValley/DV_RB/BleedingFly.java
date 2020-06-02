/*
 * Copyright (C) 2013 Aristo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ai.Zone.DragonValley.DV_RB;

import java.util.ArrayList;
import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.Mystic;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.NpcUtils;

/**
 *
 * @author Aristo
 *
 * PTS  http://www.youtube.com/watch?v=PiuIeRkqdJc
 *
 */
public class BleedingFly extends Mystic {

    private List<Creature> leech_list = new ArrayList<Creature>();
    private boolean spawn_50 = true;
    private boolean spawn_33 = true;
    private long last_leech_spawn_5 = 0;
    
    private int parasitic_leech = 25734;
    
    private long last_attack_time = 0;

    public BleedingFly(NpcInstance actor) {
        super(actor);
    }

    @Override
    protected boolean thinkActive() {
        super.thinkActive();
        if (last_attack_time != 0 && last_attack_time + 30 * 60 * 1000L < System.currentTimeMillis()) {
            getActor().deleteMe();
        }
        return true;
    }
    
    @Override
    protected void onEvtSpawn() {
        super.onEvtSpawn();
        last_attack_time = System.currentTimeMillis();
    }

    @Override
    protected void onEvtAttacked(Creature attacker, int damage) {
        NpcInstance actor = getActor();
        if (actor.getCurrentHpPercents() <= 50 && spawn_50 && leech_list.size() < 6) {
            spawn_leechs(actor, attacker);
            spawn_50 = false;
        } else if (actor.getCurrentHpPercents() <= 33 && spawn_33 && leech_list.size() < 12) {
            spawn_leechs(actor, attacker);
            spawn_33 = false;
        } else if (actor.getCurrentHpPercents() <= 7 && System.currentTimeMillis() > last_leech_spawn_5 && leech_list.size() < 20) {
            spawn_leechs(actor, attacker);
            last_leech_spawn_5 = System.currentTimeMillis() + (Rnd.get(10,18) * 1000);
        }
        super.onEvtAttacked(attacker, damage);
    }

    private void spawn_leechs(NpcInstance actor, Creature attacker) {
        for (int i = 0; i < 3; i++) {
            NpcInstance n = NpcUtils.spawnSingle(parasitic_leech, (actor.getX() + Rnd.get(-100, 100)), (actor.getY() + Rnd.get(-100, 100)), actor.getZ());
            n.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 2);
            leech_list.add(n);
        }
    }

}

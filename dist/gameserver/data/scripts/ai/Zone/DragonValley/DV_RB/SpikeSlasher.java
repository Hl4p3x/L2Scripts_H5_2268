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

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.NpcUtils;

/**
 *
 * @author Aristo
 *
 * PTS  http://www.youtube.com/watch?v=nq54AesWUVg
 *
 */
public class SpikeSlasher extends Fighter{

    private boolean spawn_50 = true;
    private boolean spawn_33 = true;
    private boolean spawn_5 = true;
    
    private int gem_dragon = 25733;
    
    private long last_attack_time = 0;
    private long last_cast_anchor = 0;
    
    private Skill paralysis = SkillHolder.getInstance().getSkill(6878, 1);
    
    

    public SpikeSlasher(NpcInstance actor) {
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
        if (actor.getCurrentHpPercents() <= 50 && spawn_50) {
            spawn_gemdragons(actor, attacker);
            spawn_50 = false;
        } else if (actor.getCurrentHpPercents() <= 33 && spawn_33) {
            spawn_gemdragons(actor, attacker);
            spawn_33 = false;
        } else if (actor.getCurrentHpPercents() <= 5 && spawn_5) {
            spawn_gemdragons(actor, attacker);
            spawn_5 = false;
        }
        if (last_cast_anchor < System.currentTimeMillis()) {
            actor.doCast(paralysis, attacker, true);
            last_cast_anchor = System.currentTimeMillis() + Rnd.get(20,90) * 1000;
        }
        super.onEvtAttacked(attacker, damage);
    }

    private void spawn_gemdragons(NpcInstance actor, Creature attacker) {
        for (int i = 0; i < 3; i++) {
            NpcInstance n = NpcUtils.spawnSingle(gem_dragon, (actor.getX() + Rnd.get(-100, 100)), (actor.getY() + Rnd.get(-100, 100)), actor.getZ());
            n.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 2);
        }
    }

}
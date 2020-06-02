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
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;

/**
 *
 * @author Aristo
 */
public class BleedingFlyMinion extends Fighter {
    
    private Skill self_destruction = SkillHolder.getInstance().getSkill(6872, 1);
    
    private long last_cast_sd = 0;

    public BleedingFlyMinion(NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onEvtAttacked(Creature attacker, int damage) {
        NpcInstance actor = getActor();
        if (last_cast_sd < System.currentTimeMillis()) {
            actor.doCast(self_destruction, attacker, true);
            last_cast_sd = System.currentTimeMillis() + Rnd.get(15, 30) * 1000;
        }
        super.onEvtAttacked(attacker, damage);
    }
}

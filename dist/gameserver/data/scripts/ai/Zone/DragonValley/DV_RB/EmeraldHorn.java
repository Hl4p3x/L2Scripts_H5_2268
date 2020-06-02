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
import l2s.gameserver.ai.Mystic;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.instances.NpcInstance;

/**
 *
 * @author Aristo
 *
 * PTS  http://www.youtube.com/watch?v=CHPqJNDiq8E
 *
 */
public class EmeraldHorn extends Mystic {
 
   
    private long last_attack_time = 0;

    public EmeraldHorn(NpcInstance actor) {
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
        getActor().altOnMagicUseTimer(getActor(), SkillHolder.getInstance().getSkill(86, Rnd.get(1,3)));
        last_attack_time = System.currentTimeMillis();
    }

}
package services;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.scripts.Functions;

/**
 * После смерти  Антараса или Валакаса на 3 часа появляется Вестник Невитта
 * С помощью «Вестника Невитта» можно получить баф «Сокрушение Дракона», на 3 часа увеличивающий время действия «Благословения Невитта».
 */

public class InvokerNevitHerald extends Functions 
{
    public void getCrushingDragon()
    {
        Player player = getSelf();
        NpcInstance npc = getNpc();
        npc.doCast(SkillHolder.getInstance().getSkill(23312, 1), player, true);
    }
}

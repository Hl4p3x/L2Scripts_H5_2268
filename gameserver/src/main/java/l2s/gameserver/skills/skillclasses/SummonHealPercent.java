package l2s.gameserver.skills.skillclasses;

import java.util.List;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.StatsSet;

public class SummonHealPercent
  extends Skill
{
  private final boolean _ignoreHpEff;
  
  public SummonHealPercent(StatsSet set)
  {
    super(set);
    _ignoreHpEff = set.getBool("ignoreHpEff", true);
  }
  
  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    for (Creature target : targets) {
      if (target != null)
      {
		useInstantEffects(activeChar, target, false);
        getEffects(activeChar, target, getActivateRate() > 0, false);
        
        double hp = _power * target.getMaxHp() / 100.0D;
        double newHp = hp * (!_ignoreHpEff ? target.calcStat(Stats.HEAL_EFFECTIVNESS, 100.0D, activeChar, this) : 100.0D) / 100.0D;
        double addToHp = Math.max(0.0D, Math.min(newHp, target.calcStat(Stats.HP_LIMIT, null, null) * target.getMaxHp() / 100.0D - target.getCurrentHp()));
        if (addToHp > 0.0D) {
          target.setCurrentHp(addToHp + target.getCurrentHp(), false);
        }
        if (target.isPlayer()) {
          if (activeChar != target) {
            target.sendPacket((new SystemMessagePacket(SystemMsg.S2_HP_HAS_BEEN_RESTORED_BY_C1).addString(activeChar.getName())).addInteger(Math.round(addToHp)));
          } else {
            activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_HP_HAS_BEEN_RESTORED).addInteger(Math.round(addToHp)));
          }
        }
      }
    }
    if (isSSPossible()) {
      activeChar.unChargeShots(isMagic());
    }
  }
}

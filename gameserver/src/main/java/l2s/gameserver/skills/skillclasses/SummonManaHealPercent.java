package l2s.gameserver.skills.skillclasses;

import java.util.List;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.StatsSet;

public class SummonManaHealPercent
  extends Skill
{
  private final boolean _ignoreMpEff;
  
  public SummonManaHealPercent(StatsSet set)
  {
    super(set);
    _ignoreMpEff = set.getBool("ignoreMpEff", true);
  }
  
  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    for (Creature target : targets) {
      if (target != null)
      {
		useInstantEffects(activeChar, target, false);
        getEffects(activeChar, target, getActivateRate() > 0, false);
        
        double mp = _power * target.getMaxMp() / 100.0D;
        double newMp = mp * (!_ignoreMpEff ? target.calcStat(Stats.MANAHEAL_EFFECTIVNESS, 100.0D, activeChar, this) : 100.0D) / 100.0D;
        double addToMp = Math.max(0.0D, Math.min(newMp, target.calcStat(Stats.MP_LIMIT, null, null) * target.getMaxMp() / 100.0D - target.getCurrentMp()));
        if (addToMp > 0.0D) {
          target.setCurrentMp(target.getCurrentMp() + addToMp);
        }
        if (target.isPlayer()) {
          if (activeChar != target) {
            target.sendPacket((new SystemMessagePacket(SystemMsg.S2_MP_HAS_BEEN_RESTORED_BY_C1).addString(activeChar.getName())).addInteger(Math.round(addToMp)));
          } else {
            activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1_MP_HAS_BEEN_RESTORED).addInteger(Math.round(addToMp)));
          }
        }
      }
    }
    if (isSSPossible()) {
      activeChar.unChargeShots(isMagic());
    }
  }
}

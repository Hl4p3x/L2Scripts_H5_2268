package l2s.gameserver.skills.skillclasses;

import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.StatsSet;


public class ManaDam extends Skill
{
	public ManaDam(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		int sps = 0;
		if(isSSPossible())
			sps = activeChar.getChargedSpiritShot();

		for(Creature target : targets)
			if(target != null)
			{
				if(target.isDead())
					continue;

				int magicLevel = getMagicLevel() == 0 ? activeChar.getLevel() : getMagicLevel();
				int landRate = Rnd.get(30, 100);
				landRate *= target.getLevel();
				landRate /= magicLevel;

				if(Rnd.chance(landRate))
				{
					double mAtk = activeChar.getMAtk(target, this);
					if(sps == 2)
						mAtk *= 4;
					else if(sps == 1)
						mAtk *= 2;

					double mDef = target.getMDef(activeChar, this);
					if(mDef < 1.)
						mDef = 1.;

					double damage = Math.sqrt(mAtk) * this.getPower() * (target.getMaxMp() / 97) / mDef;

					boolean crit = Formulas.calcMCrit(activeChar.getMagicCriticalRate(target, this));
					if(crit)
					{
						activeChar.sendPacket(Msg.MAGIC_CRITICAL_HIT);
						damage *= activeChar.calcStat(Stats.MCRITICAL_DAMAGE, activeChar.isPlayable() && target.isPlayable() ? 2.5 : 3., target, this);
					}
					target.reduceCurrentMp(damage, activeChar);
					target.sendMessage("Your MP has decreased by "+Math.round(damage)+"");
					activeChar.sendMessage("You have burned "+Math.round(damage)+" MP from "+target.getName()+"");
				}
				else
				{
					SystemMessage msg = new SystemMessage(SystemMessage.C1_RESISTED_C2S_MAGIC).addName(target).addName(activeChar);
					activeChar.sendPacket(msg);
					target.sendPacket(msg);
					activeChar.sendPacket(new ExMagicAttackInfo(activeChar.getObjectId(), target.getObjectId(), ExMagicAttackInfo.RESISTED));
					target.sendPacket(new ExMagicAttackInfo(activeChar.getObjectId(), target.getObjectId(), ExMagicAttackInfo.RESISTED));
					target.reduceCurrentHp(1., activeChar, this, true, true, false, true, false, false, true);
				}

				useInstantEffects(activeChar, target, false);
				getEffects(activeChar, target, getActivateRate() > 0, false);
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}
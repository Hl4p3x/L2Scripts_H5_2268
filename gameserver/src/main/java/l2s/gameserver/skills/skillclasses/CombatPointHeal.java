package l2s.gameserver.skills.skillclasses;

import java.util.List;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.StatsSet;


public class CombatPointHeal extends Skill
{
	private final boolean _ignoreCpEff;

	public CombatPointHeal(StatsSet set)
	{
		super(set);
		_ignoreCpEff = set.getBool("ignoreCpEff", false);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
			if(target != null)
			{
				if(target.isDead() || target.isHealBlocked())
					continue;
				double maxNewCp = _power * (!_ignoreCpEff ? target.calcStat(Stats.CPHEAL_EFFECTIVNESS, 100., activeChar, this) : 100.) / 100.;
				double addToCp = Math.max(0, Math.min(maxNewCp, target.calcStat(Stats.CP_LIMIT, null, null) * target.getMaxCp() / 100. - target.getCurrentCp()));
				if(addToCp > 0)
					target.setCurrentCp(addToCp + target.getCurrentCp());
				target.sendPacket(new SystemMessage(SystemMessage.S1_CPS_WILL_BE_RESTORED).addNumber((long) addToCp));
				useInstantEffects(activeChar, target, false);
				getEffects(activeChar, target, getActivateRate() > 0, false);
			}
		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}

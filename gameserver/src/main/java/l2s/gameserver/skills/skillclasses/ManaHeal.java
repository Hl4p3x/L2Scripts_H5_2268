package l2s.gameserver.skills.skillclasses;

import java.util.List;

import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.StatsSet;

public class ManaHeal extends Skill
{
	private final boolean _ignoreMpEff;

	public ManaHeal(StatsSet set)
	{
		super(set);
		_ignoreMpEff = set.getBool("ignoreMpEff", false);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		double mp = _power;

		int sps = isSSPossible() ? activeChar.getChargedSpiritShot() : 0;
		if(sps > 0 && Config.MANAHEAL_SPS_BONUS)
			mp *= sps == 2 ? 1.5 : 1.3;

		for(Creature target : targets)
		{
			if(target.isHealBlocked())
				continue;

			double newMp = activeChar == target ? mp : Math.min(mp * 1.7, mp * (!_ignoreMpEff ? target.calcStat(Stats.MANAHEAL_EFFECTIVNESS, 100., activeChar, this) : 100.) / 100 + target.calcStat(Stats.MANAHEAL_BONUS, 0., activeChar, this));


			// Обработка разницы в левелах при речардже. Учитывыется разница уровня скилла и уровня цели.
			// 1013 = id скилла recharge. Для сервиторов не проверено убавление маны, пока оставлено так как есть.
			if(getMagicLevel() > 0 && activeChar != target)
			{
				int diff = target.getLevel() - getMagicLevel();
				if(diff > 5)
					if(diff < 20)
						newMp = newMp / 100 * (100 - diff * 5);
					else
						newMp = 0;
			}

			if(newMp == 0)
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.S1_HAS_FAILED).addSkillName(_id, getDisplayLevel()));
				getEffects(activeChar, target, getActivateRate() > 0, false);
				continue;
			}

			double addToMp = Math.max(0, Math.min(newMp, target.calcStat(Stats.MP_LIMIT, null, null) * target.getMaxMp() / 100. - target.getCurrentMp()));

			if(addToMp > 0)
				target.setCurrentMp(addToMp + target.getCurrentMp());
			if(target.isPlayer())
				if(activeChar != target)
					target.sendPacket(new SystemMessage(SystemMessage.XS2S_MP_HAS_BEEN_RESTORED_BY_S1).addString(activeChar.getName()).addNumber(Math.round(addToMp)));
				else
					activeChar.sendPacket(new SystemMessage(SystemMessage.S1_MPS_HAVE_BEEN_RESTORED).addNumber(Math.round(addToMp)));

			useInstantEffects(activeChar, target, false);
			getEffects(activeChar, target, getActivateRate() > 0, false);
		}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}
package l2s.gameserver.skills.skillclasses;

import java.util.List;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.residences.SiegeFlagInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.StatsSet;


public class Heal extends Skill
{
	private final boolean _ignoreHpEff;
	private final boolean _staticPower;

	public Heal(StatsSet set)
	{
		super(set);
		_ignoreHpEff = set.getBool("ignoreHpEff", false);
		_staticPower = set.getBool("staticPower", isHandler());
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(target == null || target.isDoor() || target instanceof SiegeFlagInstance)
			return false;

		if(activeChar.isPlayable() && activeChar.getPlayer().isInFightClub() && !activeChar.getPlayer().getFightClubEvent().canUsePositiveMagic(activeChar, target))
			return false;

		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		// Надо уточнить формулу.
		double hp = _power;
		if(!_staticPower)
			hp += 0.1 * _power * Math.sqrt(activeChar.getMAtk(null, this) / 333);

		int sps = isSSPossible() && getHpConsume() == 0 ? activeChar.getChargedSpiritShot() : 0;

		if(sps == 2)
			hp *= 1.5;
		else if(sps == 1)
			hp *= 1.3;

		if(activeChar.getSkillMastery(getId()) == 3 && !_staticPower)
		{
			activeChar.removeSkillMastery(getId());
			hp *= 3.;
		}

		for(Creature target : targets)
			if(target != null)
			{
				if(target.isHealBlocked())
					continue;

				// Player holding a cursed weapon can't be healed and can't heal
				if(target != activeChar)
					if(target.isPlayer() && target.isCursedWeaponEquipped())
						continue;
					else if(activeChar.isPlayer() && activeChar.isCursedWeaponEquipped())
						continue;
					else if(activeChar.isPlayable() && activeChar.getPlayer().isInFightClub() && !activeChar.getPlayer().getFightClubEvent().canUsePositiveMagic(activeChar, target))
						continue;

				double addToHp = 0;
				if(_staticPower)
					addToHp = _power;
				else
				{
					addToHp = hp * (!_ignoreHpEff ? target.calcStat(Stats.HEAL_EFFECTIVNESS, 100., activeChar, this) : 100.) / 100.;
					addToHp = activeChar.calcStat(Stats.HEAL_POWER, addToHp, target, this);
				}

				addToHp = Math.max(0, Math.min(addToHp, target.calcStat(Stats.HP_LIMIT, null, null) * target.getMaxHp() / 100. - target.getCurrentHp()));

				if(addToHp > 0)
					target.setCurrentHp(addToHp + target.getCurrentHp(), false);
				if(getId() == 4051)
					target.sendPacket(Msg.REJUVENATING_HP);
				else if(target.isPlayer())
					if(activeChar == target)
						activeChar.sendPacket(new SystemMessage(SystemMessage.S1_HPS_HAVE_BEEN_RESTORED).addNumber(Math.round(addToHp)));
					else
						// FIXME показывать ли лекарю, сколько он восстановил HP цели?
						target.sendPacket(new SystemMessage(SystemMessage.XS2S_HP_HAS_BEEN_RESTORED_BY_S1).addString(activeChar.getName()).addNumber(Math.round(addToHp)));
				else if(target.isSummon() || target.isPet())
				{
					Player owner = target.getPlayer();
					if(owner != null)
						if(activeChar == target) // Пет лечит сам себя
							owner.sendMessage(new CustomMessage("YOU_HAVE_RESTORED_S1_HP_OF_YOUR_PET", owner).addNumber(Math.round(addToHp)));
						else if(owner == activeChar) // Хозяин лечит пета
							owner.sendMessage(new CustomMessage("YOU_HAVE_RESTORED_S1_HP_OF_YOUR_PET", owner).addNumber(Math.round(addToHp)));
						else
							// Пета лечит кто-то другой
							owner.sendMessage(new CustomMessage("S1_HAS_BEEN_RESTORED_S2_HP_OF_YOUR_PET", owner).addString(activeChar.getName()).addNumber(Math.round(addToHp)));
				}
				useInstantEffects(activeChar, target, false);
				getEffects(activeChar, target, getActivateRate() > 0, false);
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}
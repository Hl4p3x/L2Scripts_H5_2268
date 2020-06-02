package l2s.gameserver.skills.skillclasses;

import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.StatsSet;


public class Continuous extends Skill
{
	private final int _lethal1;
	private final int _lethal2;

	public Continuous(StatsSet set)
	{
		super(set);
		_lethal1 = set.getInteger("lethal1", 0);
		_lethal2 = set.getInteger("lethal2", 0);
	}

	@Override
  public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
  {
		if(getSkillType() == Skill.SkillType.BUFF && target != null)
		{
			if(!target.equals(activeChar) && activeChar.isPlayable() && activeChar.getPlayer().isInFightClub() && !activeChar.getPlayer().getFightClubEvent().canUsePositiveMagic(activeChar, target))
				return false;
		}
		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		Creature realTarget;
		boolean reflected;

		for(Creature target : targets)
			if(target != null)
			{
				// Player holding a cursed weapon can't be buffed and can't buff
				if(getSkillType() == Skill.SkillType.BUFF && target != activeChar)
					if(target.isCursedWeaponEquipped() || activeChar.isCursedWeaponEquipped())
						continue;

				reflected = target.checkReflectSkill(activeChar, this);
				realTarget = reflected ? activeChar : target;

				double mult = 0.01 * realTarget.calcStat(Stats.DEATH_VULNERABILITY, activeChar, this);
				double lethal1 = _lethal1 * mult;
				double lethal2 = _lethal2 * mult;

				if(lethal1 > 0 && Rnd.chance(lethal1))
				{
					if(realTarget.isPlayer())
					{
						realTarget.reduceCurrentHp(realTarget.getCurrentCp(), activeChar, this, true, true, false, true, false, false, true);
						realTarget.sendPacket(SystemMsg.LETHAL_STRIKE);
						activeChar.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
					}
					else if(realTarget.isNpc() && !realTarget.isLethalImmune())
					{
						realTarget.reduceCurrentHp(realTarget.getCurrentHp() / 2, activeChar, this, true, true, false, true, false, false, true);
						activeChar.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
					}
				}
				else if(lethal2 > 0 && Rnd.chance(lethal2))
					if(realTarget.isPlayer())
					{
						realTarget.reduceCurrentHp(realTarget.getCurrentHp() + realTarget.getCurrentCp() - 1, activeChar, this, true, true, false, true, false, false, true);
						realTarget.sendPacket(SystemMsg.LETHAL_STRIKE);
						activeChar.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
					}
					else if(realTarget.isNpc() && !realTarget.isLethalImmune())
					{
						realTarget.reduceCurrentHp(realTarget.getCurrentHp() - 1, activeChar, this, true, true, false, true, false, false, true);
						activeChar.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
					}

				useInstantEffects(activeChar, target, reflected);
				getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
			}

		if(isSSPossible())
			if(!(Config.SAVING_SPS && _skillType == SkillType.BUFF))
				activeChar.unChargeShots(isMagic());
	}
}
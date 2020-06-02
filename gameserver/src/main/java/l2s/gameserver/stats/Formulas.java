package l2s.gameserver.stats;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Effect;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Skill.SkillType;
import l2s.gameserver.model.base.BaseStats;
import l2s.gameserver.model.base.Element;
import l2s.gameserver.model.base.SkillTrait;
import l2s.gameserver.model.instances.ReflectionBossInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.DamageTextPacket;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.skills.effects.EffectTemplate;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.utils.PositionUtils;

public class Formulas
{
	private static final boolean[] DEBUG_DISABLED = { false, false, false};

	public static double calcHpRegen(Creature cha)
	{
		double init;
		if(cha.isPlayer())
			init = cha.getPlayer().getTemplate().getBaseHpReg(cha.getLevel());
		else
			init = cha.getTemplate().getBaseHpReg();

		if(cha.isPlayable())
		{
			init *= BaseStats.CON.calcBonus(cha);
			if(cha.isSummon())
				init *= 2;
		}

		double regen = cha.calcStat(Stats.REGENERATE_HP_RATE, init);
		// TODO: Вынести значения в датапак?
		if(cha.isPlayer() && cha.getPlayer().isSitting())
			regen *= 1.5;
		else if(!cha.isMoving)
			regen *= 1.1;
		else if(cha.isRunning())
			regen *= 0.7;
		return regen;
	}

	public static double calcMpRegen(Creature cha)
	{
		double init;
		if(cha.isPlayer())
			init = cha.getPlayer().getTemplate().getBaseMpReg(cha.getLevel());
		else
			init = cha.getTemplate().getBaseMpReg();

		if(cha.isPlayable())
		{
			init *= BaseStats.MEN.calcBonus(cha);
			if(cha.isSummon())
				init *= 2;
		}

		double regen = cha.calcStat(Stats.REGENERATE_MP_RATE, init);
		// TODO: Вынести значения в датапак?
		if(cha.isPlayer() && cha.getPlayer().isSitting())
			regen *= 1.5;
		else if(!cha.isMoving)
			regen *= 1.1;
		else if(cha.isRunning())
			regen *= 0.7;
		return regen;
	}

	public static double calcCpRegen(Creature cha)
	{
		double init = 0.0D;
		if(cha.isPlayer())
			init = cha.getPlayer().getTemplate().getBaseCpReg(cha.getLevel()) * BaseStats.CON.calcBonus(cha);

		double regen = cha.calcStat(Stats.REGENERATE_CP_RATE, init);
		// TODO: Вынести значения в датапак?
		if(cha.isPlayer() && cha.getPlayer().isSitting())
			regen *= 1.5;
		else if(!cha.isMoving)
			regen *= 1.1;
		else if(cha.isRunning())
			regen *= 0.7;
		return regen;
	}

	public static class AttackInfo
	{
		public double damage = 0;
		public double defence = 0;
		public double crit_static = 0;
		public double death_rcpt = 0;
		public double lethal1 = 0;
		public double lethal2 = 0;
		public double lethal_dmg = 0;
		public boolean crit = false;
		public boolean shld = false;
		public boolean lethal = false;
		public boolean miss = false;
	}

	/**
	 * Для простых ударов
	 * patk = patk
	 * При крите простым ударом:
	 * patk = patk * (1 + crit_damage_rcpt) * crit_damage_mod + crit_damage_static
	 * Для blow скиллов
	 * TODO
	 * Для скилловых критов, повреждения просто удваиваются, бафы не влияют (кроме blow, для них выше)
	 * patk = (1 + crit_damage_rcpt) * (patk + skill_power)
	 * Для обычных атак
	 * damage = patk * ss_bonus * 70 / pdef
	 */

	public static AttackInfo calcPhysDam(Creature attacker, Creature target, Skill skill, boolean dual, boolean blow, boolean ss, boolean onCrit, boolean bow)
	{
		AttackInfo info = new AttackInfo();

		info.damage = attacker.getPAtk(target);
		info.defence = target.getPDef(attacker);
		info.crit_static = attacker.calcStat(Stats.CRITICAL_DAMAGE_STATIC, target, skill);
		info.death_rcpt = 0.01 * target.calcStat(Stats.DEATH_VULNERABILITY, attacker, skill);
		info.lethal1 = skill == null ? 0 : skill.getLethal1() * info.death_rcpt;
		info.lethal2 = skill == null ? 0 : skill.getLethal2() * info.death_rcpt;
		info.crit = Rnd.chance(calcCrit(attacker, target, skill, blow));
		info.shld = (skill == null || !skill.getShieldIgnore()) && Formulas.calcShldUse(attacker, target);
		info.lethal = false;
		info.miss = false;
		boolean isPvP = attacker.isPlayable() && target.isPlayable();

		if(info.shld)
			info.defence += target.getShldDef();

		info.defence = Math.max(info.defence, 1);

		if(skill != null)
		{
			final SkillTrait trait = skill.getTraitType();
			if (trait != null)
			{
				final Env env = new Env(attacker, target, skill);
				double traitMul = 1. + (trait.calcProf(env) - trait.calcVuln(env)) / 100.;
				if (traitMul == Double.NEGATIVE_INFINITY) // invul
				{
					info.damage = 0;
					return info;
				}
				/*else if (traitMul > 2.) // DS: нужны тесты
					traitMul = 2.;
				else if (traitMul < 0.05)
					traitMul = 0.05;

				power *= traitMul;*/
			}

			if(!blow && !target.isLethalImmune() && target.getLevel() - skill.getMagicLevel() <= 5) // считаем леталы для не blow скиллов
				if(info.lethal1 > 0 && Rnd.chance(info.lethal1))
				{
					if(target.isPlayer())
					{
						info.lethal = true;
						info.lethal_dmg = target.getCurrentCp();
						target.sendPacket(SystemMsg.YOUR_CP_WAS_DRAINED_BECAUSE_YOU_WERE_HIT_WITH_A_CP_SIPHON_SKILL);
					}
					else
						info.lethal_dmg = target.getCurrentHp() / 2;
					attacker.sendPacket(SystemMsg.CP_SIPHON);
				}
				else if(info.lethal2 > 0 && Rnd.chance(info.lethal2))
				{
					if(target.isPlayer())
					{
						info.lethal = true;
						info.lethal_dmg = target.getCurrentHp() + target.getCurrentCp() - 1;
						target.sendPacket(SystemMsg.LETHAL_STRIKE);
					}
					else
						info.lethal_dmg = target.getCurrentHp() - 1;
					attacker.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
				}

			// если скилл не имеет своей силы дальше идти бесполезно, можно сразу вернуть дамаг от летала
			if(skill.getPower(target) == 0)
			{
				info.damage = 0; // обычного дамага в этом случае не наносится
				return info;
			}

			if(blow && !skill.isBehind() && ss) // Для обычных blow не влияет на power
				info.damage *= 2.04;

			// Для зарядок влияет на суммарный бонус
			if(skill.isChargeBoost())
				info.damage = attacker.calcStat(Stats.SKILL_POWER, info.damage + skill.getPower(target), null, null);
			else
				info.damage += attacker.calcStat(Stats.SKILL_POWER, skill.getPower(target), null, null);

			if(blow && skill.isBehind() && ss) // Для backstab влияет на power, но меньше множитель
				info.damage *= 1.5;

			//Заряжаемые скилы имеют постоянный урон
			if(!skill.isChargeBoost())
				info.damage *= 1 + (Rnd.get() * attacker.getRandomDamage() * 2 - attacker.getRandomDamage()) / 100;

			if(blow)
			{
				// DS: Focus Death и Focus Power умножаются на 0.5 в датапаке
				info.damage *= 0.01 * attacker.calcStat(Stats.CRITICAL_DAMAGE, target, skill);
				info.damage = target.calcStat(Stats.CRIT_DAMAGE_RECEPTIVE, info.damage, attacker, skill);
				info.damage += 6.1 * info.crit_static;
			}

			if(skill.isChargeBoost())
				info.damage *= 0.8 + 0.2 * (attacker.getIncreasedForce() + Math.max(skill.getNumCharges(), 0));
			else if(skill.isSoulBoost())
				info.damage *= 1.0 + 0.06 * Math.min(attacker.getConsumedSouls(), 5);

			// Gracia Physical Skill Damage Bonus
			info.damage *= 1.10113;

			if(info.crit)
				info.damage *= 2.;
		}
		else
		{
			info.damage *= 1 + (Rnd.get() * attacker.getRandomDamage() * 2 - attacker.getRandomDamage()) / 100;

			if(dual)
				info.damage /= 2.;

			if(info.crit)
			{
				info.damage *= 0.01 * attacker.calcStat(Stats.CRITICAL_DAMAGE, target, skill);
				info.damage = 2 * target.calcStat(Stats.CRIT_DAMAGE_RECEPTIVE, info.damage, attacker, skill);
				info.damage += info.crit_static;
			}
		}

		if(info.crit)
		{
			// шанс абсорбации души (без анимации) при крите, если Soul Mastery 4го уровня или более
			int chance = attacker.getSkillLevel(Skill.SKILL_SOUL_MASTERY);
			if(chance > 0)
			{
				if(chance >= 21)
					chance = 30;
				else if(chance >= 15)
					chance = 25;
				else if(chance >= 9)
					chance = 20;
				else if(chance >= 4)
					chance = 15;
				if(Rnd.chance(chance))
					attacker.setConsumedSouls(attacker.getConsumedSouls() + 1, null);
			}
		}

		// у зарядок нет бонусов от положения цели
		if (skill == null || !skill.isChargeBoost())
			switch(PositionUtils.getDirectionTo(target, attacker))
			{
				case BEHIND:
					info.damage *= 1.2;
					break;
				case SIDE:
					info.damage *= 1.1;
					break;
			}

		if(ss && !blow)
			info.damage *= 2.0;

		info.damage *= 70. / info.defence;
		info.damage = attacker.calcStat(Stats.PHYSICAL_DAMAGE, info.damage, target, skill);

		if(info.shld)
		{
			if(Rnd.chance(5))
			{
				target.sendPacket(SystemMsg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
				info.damage = 1;
			}	
		}

		if(isPvP)
		{
			if(bow)
			{
				if(skill == null)
				{
					info.damage *= attacker.calcStat(Stats.PVP_BOW_DMG_BONUS, 1, null, null);
					info.damage /= target.calcStat(Stats.PVP_BOW_DEFENCE_BONUS, 1, null, null);
				}
				else
				{
					info.damage *= attacker.calcStat(Stats.PVP_BOW_SKILL_DMG_BONUS, 1, null, null);
					info.damage /= target.calcStat(Stats.PVP_BOW_SKILL_DEFENCE_BONUS, 1, null, null);
				}	
			}
			else
			{
				if(skill == null)
				{
					info.damage *= attacker.calcStat(Stats.PVP_PHYS_DMG_BONUS, 1, null, null);
					info.damage /= target.calcStat(Stats.PVP_PHYS_DEFENCE_BONUS, 1, null, null);
				}
				else
				{
					info.damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG_BONUS, 1, null, null);
					info.damage /= target.calcStat(Stats.PVP_PHYS_SKILL_DEFENCE_BONUS, 1, null, null);
				}
			}
		}
		else
		{
			if(bow)
			{
				if(skill == null)
				{
					info.damage *= attacker.calcStat(Stats.PVE_BOW_DMG_BONUS, 1, null, null);
					info.damage /= target.calcStat(Stats.PVE_BOW_DEFENCE_BONUS, 1, null, null);
				}
				else
				{
					info.damage *= attacker.calcStat(Stats.PVE_BOW_SKILL_DMG_BONUS, 1, null, null);
					info.damage /= target.calcStat(Stats.PVE_BOW_SKILL_DEFENCE_BONUS, 1, null, null);
				}
			}
			else
			{
				if(skill == null)
				{
						info.damage *= attacker.calcStat(Stats.PVE_PHYS_DMG_BONUS, 1, null, null);
						info.damage /= target.calcStat(Stats.PVE_PHYS_DEFENCE_BONUS, 1, null, null);
				}
				else
				{
						info.damage *= attacker.calcStat(Stats.PVE_PHYS_SKILL_DMG_BONUS, 1, null, null);
						info.damage /= target.calcStat(Stats.PVE_PHYS_SKILL_DEFENCE_BONUS, 1, null, null);
				}
			}
		}
		// Тут проверяем только если skill != null, т.к. L2Character.onHitTimer не обсчитывает дамаг.
		if(skill != null)
		{
			if(info.shld)
			{
				if(info.damage == 1)
				{
					target.sendPacket(SystemMsg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
				}
				else
				{
					target.sendPacket(SystemMsg.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED);
				}	
			}	

			// Уворот от физ скилов уводит атаку в 0
			if(info.damage > 1 && !skill.hasEffects() && Rnd.chance(target.calcStat(Stats.PSKILL_EVASION, 0, attacker, skill)))
			{
				attacker.sendPacket(new SystemMessage(SystemMsg.C1_DODGES_THE_ATTACK).addName(target));
				target.sendPacket(new SystemMessage(SystemMsg.YOU_HAVE_AVOIDED_C1S_ATTACK).addName(attacker));
				info.damage = 0;
				if(Config.ENABLE_DAM_ON_SCREEN)
				{
					attacker.sendPacket(new DamageTextPacket(target.getObjectId(), 0, false, true, false, false, Config.DAM_ON_SCREEN_FONT, Config.DAM_ON_SCREEN_FONT_COLOR_ATTACKER, "", "", 0,0,0,0)); //3 14500915
					target.sendPacket(new DamageTextPacket(target.getObjectId(), 0, false, true, false, false, Config.DAM_ON_SCREEN_FONT, Config.DAM_ON_SCREEN_FONT_COLOR_TARGET, "", "", 0,0,0,0)); //16711680
				}
			}

			if(info.damage > 1 && skill.isDeathlink())
				info.damage *= 1.8 * (1.0 - attacker.getCurrentHpRatio());

			if(onCrit && !calcBlow(attacker, target, skill))
			{
				info.miss = true;
				info.damage = 0;
				//attacker.sendPacket(new SystemMessage(SystemMsg.C1S_ATTACK_WENT_ASTRAY).addName(attacker));
				if(Config.ENABLE_DAM_ON_SCREEN)
				{
					attacker.sendPacket(new DamageTextPacket(target.getObjectId(), 0, false, true, false, false, Config.DAM_ON_SCREEN_FONT, Config.DAM_ON_SCREEN_FONT_COLOR_ATTACKER, "", "", 0,0,0,0));
					target.sendPacket(new DamageTextPacket(target.getObjectId(), 0, false, true, false, false, Config.DAM_ON_SCREEN_FONT, Config.DAM_ON_SCREEN_FONT_COLOR_TARGET, "", "", 0,0,0,0));	
				}
			}

			if(blow && target.getLevel() - skill.getMagicLevel() <= 5)
			{
				if(info.lethal1 > 0 && Rnd.chance(info.lethal1))
				{
					if(target.isPlayer())
					{
						info.lethal = true;
						info.lethal_dmg = target.getCurrentCp();
						target.sendPacket(SystemMsg.YOUR_CP_WAS_DRAINED_BECAUSE_YOU_WERE_HIT_WITH_A_CP_SIPHON_SKILL);
					}
					else if(target.isLethalImmune())
						info.damage *= 2;
					else
						info.lethal_dmg = target.getCurrentHp() / 2;
					attacker.sendPacket(SystemMsg.CP_SIPHON);
				}
				else if(info.lethal2 > 0 && Rnd.chance(info.lethal2))
				{
					if(target.isPlayer())
					{
						info.lethal = true;
						info.lethal_dmg = target.getCurrentHp() + target.getCurrentCp() - 1;
						target.sendPacket(SystemMsg.LETHAL_STRIKE);
					}
					else if(target.isLethalImmune())
						info.damage *= 3;
					else
						info.lethal_dmg = target.getCurrentHp() - 1;
					attacker.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
				}
			}
			
			if(target.isInSpecialPvPZone() && attacker.getPlayer() != null && target.getPlayer() != null) //only pvp
			{
				info.damage = (int)(info.damage * target.getEnchantedDefense());
			}
				
			if(info.damage > 0)
				attacker.displayGiveDamageMessage(target, (int) info.damage, info.crit || blow, false, false, false);

			calcStunBreak(target, info.crit);

			if(calcCastBreak(target, info.crit))
				target.abortCast(false, true);
		}
		else if(target.isInSpecialPvPZone() && attacker.getPlayer() != null && target.getPlayer() != null) //only pvp and non skill
		{
			info.damage = (int)(info.damage * target.getEnchantedDefense());
		}		

		return info;
	}

	public static double calcMagicDam(Creature attacker, Creature target, Skill skill, int sps, boolean toMp)
	{
		boolean isPvP = attacker.isPlayable() && target.isPlayable();
		// Параметр ShieldIgnore для магических скиллов инвертирован
		boolean shield = skill.getShieldIgnore() && calcShldUse(attacker, target);

		double mAtk = attacker.getMAtk(target, skill);

		if(sps == 2)
			mAtk *= 4;
		else if(sps == 1)
			mAtk *= 2;

		double mdef = target.getMDef(null, skill);

		if(shield)
			mdef += target.getShldDef();
		if(mdef == 0)
			mdef = 1;

		double power = skill.getPower(target);

		boolean gradePenalty = attacker.isPlayer() && ((Player)attacker).getWeaponsExpertisePenalty() > 0;

		final SkillTrait trait = skill.getTraitType();
		if (trait != null)
		{
			final Env env = new Env(attacker, target, skill);
			double traitMul = 1. + (trait.calcProf(env) - trait.calcVuln(env)) / 100.;
			if (traitMul == Double.NEGATIVE_INFINITY) // invul
				return 0;
			else if (traitMul > 2.)
				traitMul = 2.;
			else if (traitMul < 0.05)
				traitMul = 0.05;

			power *= traitMul;
		}

		double lethalDamage = 0;

		if (target.getLevel() - skill.getMagicLevel() <= 5 && !gradePenalty)
		{
			if(skill.getLethal1() > 0 && Rnd.chance(skill.getLethal1()))
			{
				if(target.isPlayer())
				{
					lethalDamage = target.getCurrentCp();
					target.sendPacket(SystemMsg.YOUR_CP_WAS_DRAINED_BECAUSE_YOU_WERE_HIT_WITH_A_CP_SIPHON_SKILL);
				}
				else if(!target.isLethalImmune())
					lethalDamage = target.getCurrentHp() / 2;
				else
					power *= 2;
				attacker.sendPacket(SystemMsg.CP_SIPHON);
			}
			else if(skill.getLethal2() > 0 && Rnd.chance(skill.getLethal2()))
			{
				if(target.isPlayer())
				{
					lethalDamage = target.getCurrentHp() + target.getCurrentCp() - 1;
					target.sendPacket(SystemMsg.LETHAL_STRIKE);
				}
				else if(!target.isLethalImmune())
					lethalDamage = target.getCurrentHp() - 1;
				else
					power *= 3;
				attacker.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
			}
		}

		if(power == 0)
		{
			if(target.isInSpecialPvPZone() && attacker.getPlayer() != null && target.getPlayer() != null) //only pvp
			{
				lethalDamage = (int)(lethalDamage * target.getEnchantedDefense());
			}		
			if(lethalDamage > 0)
				attacker.displayGiveDamageMessage(target, (int) lethalDamage, false, false, false, false);
			return lethalDamage;
		}

		if(skill.isSoulBoost())
			power *= 1.0 + 0.0225 * Math.min(attacker.getConsumedSouls(), 5);

		boolean crit = false;
		double damage = power * Math.sqrt(mAtk) / mdef;
		if (toMp || skill.getSkillType() == SkillType.MANADAM)
		{
			if (isPvP)
				damage *= target.getMaxMp() / 97.;
			else
			{
				damage *= 91.;
				damage = Math.max(1, damage / 2.);
			}
		}
		else
			damage *= 91.;

		if (skill.getMatak() == 0) // у кубиков нет рандомдамага и критов
		{
			damage *= 1 + (Rnd.get() * attacker.getRandomDamage() * 2 - attacker.getRandomDamage()) / 100;
			crit = calcMCrit(attacker.getMagicCriticalRate(target, skill));
		}

		if(crit)
			damage *= attacker.calcStat(Stats.MCRITICAL_DAMAGE, attacker.isPlayable() && target.isPlayable() ? 2.5 : 3., target, skill);

		damage = attacker.calcStat(Stats.MAGIC_DAMAGE, damage, target, skill);

		if(shield)
		{
			if(Rnd.chance(5))
			{
				damage = 0;
				target.sendPacket(SystemMsg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
				attacker.sendPacket(new SystemMessage(SystemMessage.C1_RESISTED_C2S_MAGIC).addName(target).addName(attacker));
				target.sendPacket(new ExMagicAttackInfo(attacker.getObjectId(), target.getObjectId(), ExMagicAttackInfo.RESISTED));
				attacker.sendPacket(new ExMagicAttackInfo(attacker.getObjectId(), target.getObjectId(), ExMagicAttackInfo.RESISTED));
				if(Config.ENABLE_DAM_ON_SCREEN)
				{
					attacker.sendPacket(new DamageTextPacket(target.getObjectId(), 0, false, false, true, true, Config.DAM_ON_SCREEN_FONT, Config.DAM_ON_SCREEN_FONT_COLOR_ATTACKER, "", "", 0,0,0,0)); //block + magic
					target.sendPacket(new DamageTextPacket(target.getObjectId(), 0, false, false, true, true, Config.DAM_ON_SCREEN_FONT, Config.DAM_ON_SCREEN_FONT_COLOR_TARGET, "", "", 0,0,0,0));		
				}
			}
			else
			{
				target.sendPacket(SystemMsg.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED);
				attacker.sendPacket(new SystemMessage(SystemMessage.YOUR_OPPONENT_HAS_RESISTANCE_TO_MAGIC_THE_DAMAGE_WAS_DECREASED));
			}
		}

		int levelDiff = target.getLevel() - attacker.getLevel(); // C Gracia Epilogue уровень маг. атак считается только по уроню атакующего

		final int subLvlDiff;
		if(target.isRaid())
			subLvlDiff = Math.min(levelDiff, Config.MDAM_TO_RAID_SUB_LVL_DIFF);
		else if(target.isMonster())
			subLvlDiff = Math.min(levelDiff, Config.MDAM_TO_MONSTER_SUB_LVL_DIFF);
		else
			subLvlDiff = 0;

		levelDiff -= subLvlDiff;

		if (damage > 1)
		{
			if (skill.isDeathlink())
				damage *= 1.8 * (1.0 - attacker.getCurrentHpRatio());

			if(skill.isBasedOnTargetDebuff())
			{
				int effectCount = 0;
				for (Effect e : target.getEffectList().getAllFirstEffects())
					if (!e.getSkill().isToggle())
						effectCount++;

				damage *= 0.3 + 0.0875 * effectCount;
			}
		}

		damage += lethalDamage;

		if(isPvP && damage > 1)
		{
			damage *= attacker.calcStat(Stats.PVP_MAGIC_SKILL_DMG_BONUS, 1, null, null);
			damage /= target.calcStat(Stats.PVP_MAGIC_SKILL_DEFENCE_BONUS, 1, null, null);
		}
		else if(damage > 1)
		{
			damage *= attacker.calcStat(Stats.PVE_MAGIC_SKILL_DMG_BONUS, 1, null, null);
			damage /= target.calcStat(Stats.PVE_MAGIC_SKILL_DEFENCE_BONUS, 1, null, null);
		}

		double magic_rcpt = target.calcStat(Stats.MAGIC_RESIST, attacker, skill) - attacker.calcStat(Stats.MAGIC_POWER, target, skill);
		double lvlMod =  4. * Math.max(1., target.getLevel() >= 80 ? (levelDiff - 4) * 1.6 : (levelDiff - 14) * 2);
		double failChance = gradePenalty ? 95. : Math.min(lvlMod * (1. + magic_rcpt / 100.), 95.);
		double resistChance = gradePenalty ? 95. : 5 * Math.max(levelDiff - 10, 1);

		if (attacker.isPlayer() && ((Player)attacker).isDebug())
			attacker.sendMessage("Fail chance " + (int)failChance + "/" + (int)resistChance);

		if(Rnd.chance(failChance))
		{
			if(Rnd.chance(resistChance))
			{
				damage = 0;
				SystemMessage msg = new SystemMessage(SystemMessage.C1_RESISTED_C2S_MAGIC).addName(target).addName(attacker);
				attacker.sendPacket(msg);
				target.sendPacket(msg);
				attacker.sendPacket(new ExMagicAttackInfo(attacker.getObjectId(), target.getObjectId(), ExMagicAttackInfo.RESISTED));
				target.sendPacket(new ExMagicAttackInfo(attacker.getObjectId(), target.getObjectId(), ExMagicAttackInfo.RESISTED));
				if(Config.ENABLE_DAM_ON_SCREEN)
				{
					attacker.sendPacket(new DamageTextPacket(target.getObjectId(), 0, false, false, true, true, Config.DAM_ON_SCREEN_FONT, Config.DAM_ON_SCREEN_FONT_COLOR_ATTACKER, "", "", 0,0,0,0)); //block + magic
					target.sendPacket(new DamageTextPacket(target.getObjectId(), 0, false, false, true, true, Config.DAM_ON_SCREEN_FONT, Config.DAM_ON_SCREEN_FONT_COLOR_TARGET, "", "", 0,0,0,0));	
				}
			}
			else
			{
				damage /= 2;
				SystemMessage msg = new SystemMessage(SystemMessage.DAMAGE_IS_DECREASED_BECAUSE_C1_RESISTED_AGAINST_C2S_MAGIC).addName(target).addName(attacker);
				attacker.sendPacket(msg);
				target.sendPacket(msg);
			}
		}
		if(target.isInSpecialPvPZone() && attacker.getPlayer() != null && target.getPlayer() != null) //only pvp
			damage = (int)(damage * target.getEnchantedDefense());

		attacker.displayGiveDamageMessage(target, (int) damage, crit, false, false, true);

		if(calcCastBreak(target, crit))
			target.abortCast(false, true);

		return damage;
	}

	public static void calcStunBreak(Creature target, boolean crit)
	{
		if(target.isStunned() && Rnd.chance(crit ? 75 : 10))
		{
			for(Effect e : target.getEffectList().getAllEffects())
			{
				if(e.getEffectType() == EffectType.Stun)
				{
					e.exit();
					if(target.isPlayer())
						target.sendPacket(new SystemMessagePacket(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(e.getSkill()));
				}
			}
		}

		for(Effect e : target.getEffectList().getAllEffects())
		{
			double chance = crit ? e.getSkill().getOnCritCancelChance() : e.getSkill().getOnAttackCancelChance();
			if(chance > 0 && Rnd.chance(chance))
			{
				e.exit();
				if(target.isPlayer())
					target.sendPacket(new SystemMessagePacket(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(e.getSkill()));
			}
		}
	}

	/** Returns true in case of fatal blow success */
	public static boolean calcBlow(Creature activeChar, Creature target, Skill skill)
	{
		WeaponTemplate weapon = activeChar.getActiveWeaponTemplate();

		double base_weapon_crit = weapon == null ? 4. : weapon.getCritical();
		double crit_height_bonus = 0.008 * Math.min(25, Math.max(-25, target.getZ() - activeChar.getZ())) + 1.1;
		double buffs_mult = activeChar.calcStat(Stats.FATALBLOW_RATE, target, skill);
		double skill_mod = skill.isBehind() ? 6 : 5; // CT 2.3 blowrate increase

		double chance = base_weapon_crit * buffs_mult * crit_height_bonus * skill_mod;

		if(!target.isInCombat())
			chance *= 1.1;

		switch(PositionUtils.getDirectionTo(target, activeChar))
		{
			case BEHIND:
				chance *= 1.3;
				break;
			case SIDE:
				chance *= 1.1;
				break;
			case FRONT:
				if(skill.isBehind())
					chance = 3.0;
				break;
		}
		chance = Math.min(skill.isBehind() ? 100 : 80, chance);
		return Rnd.chance(chance);
	}

	/** Возвращает шанс крита в процентах */
	public static double calcCrit(Creature attacker, Creature target, Skill skill, boolean blow)
	{
		if(attacker.isPlayer() && attacker.getActiveWeaponTemplate() == null)
			return 0;
		if(skill != null)
			return skill.getCriticalRate() * (blow ? BaseStats.DEX.calcBonus(attacker) : BaseStats.STR.calcBonus(attacker)) * 0.01 * attacker.calcStat(Stats.SKILL_CRIT_CHANCE_MOD, target, skill);

		double rate = attacker.getCriticalHit(target, null) * 0.01 * target.calcStat(Stats.CRIT_CHANCE_RECEPTIVE, attacker, skill);

		switch(PositionUtils.getDirectionTo(target, attacker))
		{
			case BEHIND:
				rate *= 1.4;
				break;
			case SIDE:
				rate *= 1.2;
				break;
		}

		return rate / 10;
	}

	public static boolean calcMCrit(double mRate)
	{
		// floating point random gives more accuracy calculation, because argument also floating point
		return Rnd.get() * 100 <= Math.min(Config.LIM_MCRIT, mRate);
	}

	public static boolean calcCastBreak(Creature target, boolean crit)
	{
		if(target == null || target.isDamageBlocked() || target.isRaid() || !target.isCastingNow())
			return false;
		Skill skill = target.getCastingSkill();
		if(skill == null)
			return false;
		if(skill.getMagicType() == Skill.SkillMagicType.PHYSIC || skill.getMagicType() == Skill.SkillMagicType.MUSIC)//(skill.getSkillType() == SkillType.TAKECASTLE || skill.getSkillType() == SkillType.TAKEFORTRESS || skill.getSkillType() == SkillType.TAKEFLAG))
			return false;
		return Rnd.chance(target.calcStat(Stats.CAST_INTERRUPT, crit ? 75 : 10, null, skill));
	}

	/** Calculate delay (in milliseconds) before next ATTACK */
	public static int calcPAtkSpd(double rate)
	{
		return (int) (500000 / rate); // в миллисекундах поэтому 500*1000
	}

	/** Calculate delay (in milliseconds) for skills cast */
	public static int calcMAtkSpd(Creature attacker, Skill skill, double skillTime)
	{
		if(skill.isMagic())
			return (int) (skillTime * 333 / Math.max(attacker.getMAtkSpd(), 1));
		return (int) (skillTime * 333 / Math.max(attacker.getPAtkSpd(), 1));
	}

	/** Calculate reuse delay (in milliseconds) for skills */
	public static long calcSkillReuseDelay(Creature actor, Skill skill)
	{
		long reuseDelay = skill.getReuseDelay();
		if(actor.isMonster())
			reuseDelay = skill.getReuseForMonsters();
		if(skill.isReuseDelayPermanent() || skill.isHandler() || skill.isItemSkill())
			return reuseDelay;
		if(actor.getSkillMastery(skill.getId()) == 1)
		{
			actor.removeSkillMastery(skill.getId());
			return 0;
		}
		if(skill.isMusic())
			return (long) actor.calcStat(Stats.MUSIC_REUSE_RATE, reuseDelay, null, skill);
		if(skill.isMagic())
			return (long) actor.calcStat(Stats.MAGIC_REUSE_RATE, reuseDelay, null, skill);
		return (long) actor.calcStat(Stats.PHYSIC_REUSE_RATE, reuseDelay, null, skill);
	}

	/** Returns true if hit missed (target evaded) */
	public static boolean calcHitMiss(Creature attacker, Creature target)
	{
		int chanceToHit = 88 + 2 * (attacker.getAccuracy() - target.getEvasionRate(attacker));

		chanceToHit = Math.max(chanceToHit, 28);
		chanceToHit = Math.min(chanceToHit, 98);

		PositionUtils.TargetDirection direction = PositionUtils.getDirectionTo(attacker, target);
		switch(direction)
		{
			case BEHIND:
				chanceToHit *= 1.2;
				break;
			case SIDE:
				chanceToHit *= 1.1;
				break;
		}
		return !Rnd.chance(chanceToHit);
	}

	/** Returns true if shield defence successfull */
	public static boolean calcShldUse(Creature attacker, Creature target)
	{
		WeaponTemplate template = target.getSecondaryWeaponTemplate();
		if(template == null || template.getItemType() != WeaponTemplate.WeaponType.NONE) //that's shield
			return false;
		int angle = (int) target.calcStat(Stats.SHIELD_ANGLE, attacker, null);
		if(!PositionUtils.isFacing(target, attacker, angle))
			return false;
		return Rnd.chance((int)target.calcStat(Stats.SHIELD_RATE, attacker, null));
	}

	public static boolean[] isDebugEnabled(Creature caster, Creature target)
	{
		if (Config.ALT_DEBUG_ENABLED)
		{
			// Включена ли отладка на кастере
			final boolean debugCaster = caster.getPlayer() != null && caster.getPlayer().isDebug();
			// Включена ли отладка на таргете
			final boolean debugTarget = target.getPlayer() != null && target.getPlayer().isDebug();
			// Разрешена ли отладка в PvP
			if (Config.ALT_DEBUG_PVP_ENABLED && (debugCaster && debugTarget) && (!Config.ALT_DEBUG_PVP_DUEL_ONLY || (caster.getPlayer().isInDuel() && target.getPlayer().isInDuel())))
				return new boolean[]{true, debugCaster, debugTarget};
			// Включаем отладку в PvE если разрешено
			if (Config.ALT_DEBUG_PVE_ENABLED && ((debugCaster && target.isMonster()) || (debugTarget && caster.isMonster())))
				return new boolean[]{true, debugCaster, debugTarget};
		}

		return DEBUG_DISABLED;
	}

	public static boolean calcSkillSuccess(Env env, EffectTemplate et, int spiritshot)
	{
		if(env.value == -1)
			return true;

		env.value = Math.max(Math.min(env.value, 150), 1); // На всякий случай
		final double base = env.value; // Запоминаем базовый шанс (нужен позже)

		final Skill skill = env.skill;
		if(!skill.isOffensive())
			return Rnd.chance(env.value);

		final Creature caster = env.character;
		final Creature target = env.target;

		final boolean[] debug = isDebugEnabled(caster, target);
		final boolean debugGlobal = debug[0];
		final boolean debugCaster = debug[1];
		final boolean debugTarget = debug[2];

		SkillTrait trait = skill.getTraitType();
		if(trait != null)
			env.value *= trait.getChanceMod();

		double statMod = 1.;
		if(skill.getSaveVs() != null)
		{
			statMod = skill.getSaveVs().calcChanceMod(target);
			env.value *= statMod; // Бонус от MEN/CON/etc
		}

		env.value = Math.max(env.value, 1);

		double mAtkMod = 1.;
		int ssMod = 0;
		if(skill.isMagic() && (et == null || et.getChance() < 0)) // Этот блок только для магических скиллов, эффекты с отдельным шансом тоже пропускаются
		{
			int mdef = Math.max(1, target.getMDef(target, skill)); // Вычисляем mDef цели
			double matk = caster.getMAtk(target, skill);

			if(skill.isSSPossible())
			{
				switch (spiritshot)
				{
				case ItemInstance.CHARGED_BLESSED_SPIRITSHOT:
					ssMod = 4;
					break;
				case ItemInstance.CHARGED_SPIRITSHOT:
					ssMod = 2;
					break;
				default:
					ssMod = 1;
				}
				matk *= ssMod;
			}

			mAtkMod = Config.SKILLS_CHANCE_MOD * Math.pow(matk, Config.SKILLS_CHANCE_POW) / mdef;

			/*
			if (mAtkMod < 0.7)
				mAtkMod = 0.7;
			else if (mAtkMod > 1.4)
				mAtkMod = 1.4;
			*/
			if(caster.isMonster())
				env.value *= Config.SKILLS_MOB_CHANCE;
			else

			env.value *= mAtkMod;
			env.value = Math.max(env.value, 1);
		}

		double lvlDependMod = skill.getLevelModifier();
		int mLevel = skill.getMagicLevel() == 0 ? caster.getLevel() : skill.getMagicLevel(); // The difference in the levels of
		if(mLevel > 0)
		{
			int tlevel = target.getLevel(); //35
			int delta = mLevel - tlevel;  // 35 - 85 = -50
			int deltamod = (delta / 5) * 5; // - 50

			if(delta != deltamod)
				if(delta < 0)
					deltamod -= 5;
				else if(delta > 0)
					deltamod += 5;

			env.value += deltamod;  // -50 of total chance.
		}

		double vulnMod = 0;
		double profMod = 0;
		double resMod = 1.;
		double debuffMod = 1.;
		if(!skill.isIgnoreResists())
		{
			if(et == null || et.getChance() < 0) // Эффекты с индивидуальным шансом - не дебафы (сброс цели). TODO: отдельный флаг
			{
				debuffMod = 1. - (target.calcStat(Stats.DEBUFF_RESIST, 100., caster, skill) - 100.) / 120.;

				if(debuffMod != 1) // Внимание, знак был изменен на противоположный !
				{
					if (debuffMod == Double.NEGATIVE_INFINITY)
					{
						if (debugGlobal)
						{
							if (debugCaster)
								caster.getPlayer().sendMessage("Full debuff immunity");
							if (debugTarget)
								target.getPlayer().sendMessage("Full debuff immunity");
						}
						return false;
					}
					if (debuffMod == Double.POSITIVE_INFINITY)
					{
						if (debugGlobal)
						{
							if (debugCaster)
								caster.getPlayer().sendMessage("Full debuff vulnerability");
							if (debugTarget)
								target.getPlayer().sendMessage("Full debuff vulnerability");
						}
						return true;
					}

					debuffMod = Math.max(debuffMod, 0);
	
					if(caster.isMonster())
						debuffMod *= Config.SKILLS_DEBUFF_MOB_CHANCE;

					env.value *= debuffMod;
				}
			}

			if(trait != null)
			{
				vulnMod = trait.calcVuln(env);
				profMod = trait.calcProf(env);

				final double maxResist = 90 + profMod * 0.85;
				resMod = (maxResist - vulnMod) / 60.;
			}

			if(resMod != 1) // Внимание, знак был изменен на противоположный !
			{
				if (resMod == Double.NEGATIVE_INFINITY)
				{
					if (debugGlobal)
					{
						if (debugCaster)
							caster.getPlayer().sendMessage("Full immunity");
						if (debugTarget)
							target.getPlayer().sendMessage("Full immunity");
					}
					return false;
				}
				if (resMod == Double.POSITIVE_INFINITY)
				{
					if (debugGlobal)
					{
						if (debugCaster)
							caster.getPlayer().sendMessage("Full vulnerability");
						if (debugTarget)
							target.getPlayer().sendMessage("Full vulnerability");
					}
					return true;
				}

				resMod = Math.max(resMod, 0);
				env.value *= resMod;
			}
		}

		double elementMod = 0;
		final Element element = skill.getElement();
		if (element != Element.NONE)
		{
			elementMod = skill.getElementPower();
			Element attackElement = getAttackElement(caster, target);
			if (attackElement == element)
				elementMod += caster.calcStat(element.getAttack(), 0, target, null);

			elementMod = getElementMod(elementMod, target.calcStat(element.getDefence(), 0, caster, null));
			env.value *= elementMod;
		}

		//if(skill.isSoulBoost()) // Бонус от душ камаелей
		//	env.value *= 0.85 + 0.06 * Math.min(character.getConsumedSouls(), 5);
		env.value = Math.max(env.value, Math.min(base, Config.SKILLS_CHANCE_MIN)); // If the base chance of a Config.SKILLS_CHANCE_MIN, then the small difference in levels, making drops below.
		env.value = Math.max(Math.min(env.value, Config.SKILLS_CHANCE_CAP), 1); // rimenyaem cap
		if(caster.isMonster())
			env.value *= Config.SKILLS_MOB_CHANCE;

		final boolean result = Rnd.chance((int)env.value);

		if (debugGlobal)
		{
			StringBuilder stat = new StringBuilder(100);
			stat.append(skill.getId());
			stat.append("/");				
			stat.append(skill.getDisplayLevel());
			stat.append(" ");				
			if (et == null)
				stat.append(skill.getName());
			else
				stat.append(et._effectType.name());
			stat.append(" AR:");
			stat.append((int)base);
			stat.append(" ");				
			if (skill.getSaveVs() != null)
			{
				stat.append(skill.getSaveVs().name());
				stat.append(":");				
				stat.append(String.format("%1.1f", statMod));
			}
			if (skill.isMagic())
			{
				stat.append(" ");				
				stat.append(" mAtk:");
				stat.append(String.format("%1.1f", mAtkMod));
				stat.append(" SS:");
				stat.append(ssMod);
			}
			if (skill.getTraitType() != null)
			{
				stat.append(" ");				
				stat.append(skill.getTraitType().name());
			}
			stat.append(" ");
			stat.append(String.format("%1.1f", resMod));
			stat.append("(");
			stat.append(String.format("%1.1f", profMod));
			stat.append("/");
			stat.append(String.format("%1.1f", vulnMod));
			if (debuffMod != 0)
			{
				stat.append("+");
				stat.append(String.format("%1.1f", debuffMod));
			}
			stat.append(") lvl:");
			stat.append(String.format("%1.1f", lvlDependMod));
			stat.append(" elem:");
			stat.append(String.format("%1.1f", elementMod));
			stat.append(" Chance:");
			stat.append(String.format("%1.1f", env.value));
			if (!result)
				stat.append(" failed");

			// отсылаем отладочные сообщения
			if (debugCaster)
				caster.getPlayer().sendMessage(stat.toString());
			if (debugTarget)
				target.getPlayer().sendMessage(stat.toString());
		}
		if(Config.DISABLE_EFFECT_ON_LEVEL_DIFFERENCE > -1)
		{
			if(target.getLevel() - caster.getLevel() > Config.DISABLE_EFFECT_ON_LEVEL_DIFFERENCE) //difference between levels from caster side only.
				return false;
		}
		return result;
	}

	public static boolean calcSkillSuccess(Creature player, Creature target, Skill skill, int activateRate)
	{
		Env env = new Env();
		env.character = player;
			
		//env.target = target;
		env.skill = skill;
		
		if(skill != null && skill.getId() == 1557)
			env.target = (Creature)player.getPlayer().getServitor();
		else	
			env.target = target;	
			
		env.value = activateRate;
		return calcSkillSuccess(env, null, player.getChargedSpiritShot());
	}

	public static void calcSkillMastery(Skill skill, Creature activeChar)
	{
		if(skill.isHandler())
			return;

		//Skill id 330 for fighters, 331 for mages
		//Actually only GM can have 2 skill masteries, so let's make them more lucky ^^
		if(activeChar.getSkillLevel(331) > 0 && activeChar.calcStat(Stats.SKILL_MASTERY, activeChar.getINT(), null, skill) >= Rnd.get(3000) || activeChar.getSkillLevel(330) > 0 && activeChar.calcStat(Stats.SKILL_MASTERY, activeChar.getSTR(), null, skill) >= Rnd.get(3000)) //TODO find the real random.
		{
			//byte mastery level, 0 = no skill mastery, 1 = no reuseTime, 2 = buff duration*2, 3 = power*3
			int masteryLevel;
			Skill.SkillType type = skill.getSkillType();
			if(skill.isMusic() || type == Skill.SkillType.BUFF || type == Skill.SkillType.HOT || type == Skill.SkillType.HEAL_PERCENT) //Hope i didn't forget skills to multiply their time
				masteryLevel = 2;
			else if(type == Skill.SkillType.HEAL)
				masteryLevel = 3;
			else
				masteryLevel = 1;
			if(masteryLevel > 0)
				activeChar.setSkillMastery(skill.getId(), masteryLevel);
		}
	}

	public static double calcDamageResists(Skill skill, Creature attacker, Creature defender, double value)
	{
		if(attacker == defender) // это дамаг от местности вроде ожога в лаве, наносится от своего имени
			return value; // TODO: по хорошему надо учитывать защиту, но поскольку эти скиллы немагические то надо делать отдельный механизм

		if(attacker.isBoss())
			value *= Config.RATE_EPIC_ATTACK;
		else if(attacker.isRaid() || attacker instanceof ReflectionBossInstance)
			value *= Config.RATE_RAID_ATTACK;

		if(defender.isBoss())
			value /= Config.RATE_EPIC_DEFENSE;
		else if(defender.isRaid() || defender instanceof ReflectionBossInstance)
			value /= Config.RATE_RAID_DEFENSE;

		Player pAttacker = attacker.getPlayer();

		// если уровень игрока ниже чем на 2 и более уровней моба 78+, то его урон по мобу снижается
		int diff = defender.getLevel() - (pAttacker != null ? pAttacker.getLevel() : attacker.getLevel());

		final int subLvlDiff;
		if(skill != null && skill.isMagic())
		{
			if(defender.isRaid())
				subLvlDiff = Math.min(diff, Config.MDAM_TO_RAID_SUB_LVL_DIFF);
			else if(defender.isMonster())
				subLvlDiff = Math.min(diff, Config.MDAM_TO_MONSTER_SUB_LVL_DIFF);
			else
				subLvlDiff = 0;
		}
		else
		{
			if(defender.isRaid())
				subLvlDiff = Math.min(diff, Config.PDAM_TO_RAID_SUB_LVL_DIFF);
			else if(defender.isMonster())
				subLvlDiff = Math.min(diff, Config.PDAM_TO_MONSTER_SUB_LVL_DIFF);
			else
				subLvlDiff = 0;
		}

		diff -= subLvlDiff;
		if(attacker.isPlayable() && defender.isMonster() && (defender.getLevel() - subLvlDiff) >= 78 && diff > 2)
			value *= .7 / Math.pow(diff - 2, .25);

		Element element = Element.NONE;
		double power = 0.;

		// использует элемент умения
		if(skill != null)
		{
			element = skill.getElement();
			power = skill.getElementPower();
		}
		// используем максимально эффективный элемент
		else
			element = getAttackElement(attacker, defender);

		if(element == Element.NONE)
			return value;

		if(pAttacker != null && pAttacker.isGM() && Config.ALT_DEBUG_ENABLED)
		{
			pAttacker.sendMessage("Element: " + element.name());
			pAttacker.sendMessage("Attack: " + attacker.calcStat(element.getAttack(), power));
			pAttacker.sendMessage("Defence: " + defender.calcStat(element.getDefence(), 0.));
			pAttacker.sendMessage("Modifier: " + getElementMod(defender.calcStat(element.getDefence(), 0.), attacker.calcStat(element.getAttack(), power)));
		}

		return value * getElementMod(defender.calcStat(element.getDefence(), 0.), attacker.calcStat(element.getAttack(), power));
	}

	/**
	 * Возвращает множитель для атаки из значений атакующего и защитного элемента.
	 * <br /><br />
	 * Диапазон от 1.0 до 1.7 (Freya)
	 * <br /><br />
	 * @param defense значение защиты
	 * @param attack значение атаки
	 * @return множитель
	 */
	private static double getElementMod(double defense, double attack)
	{
		double diff = attack - defense;
		if(diff <= 0)
			return 1.0;
		else if(diff < 50)
			return 1.0 + diff * 0.003948;
		else if(diff < 150)
			return 1.2;
		else if(diff < 300)
			return 1.4;
		else
			return 1.7;
	}

	/**
	 * Возвращает максимально эффективный атрибут, при атаке цели
	 * @param attacker
	 * @param target
	 * @return
	 */
	public static Element getAttackElement(Creature attacker, Creature target)
	{
		double val, max = Double.MIN_VALUE;
		Element result = Element.NONE;
		for(Element e : Element.VALUES)
		{
			val = attacker.calcStat(e.getAttack(), 0., null, null);
			if(val <= 0.)
				continue;

			if(target != null)
				val -= target.calcStat(e.getDefence(), 0., null, null);

			if(val > max)
			{
				result = e;
				max = val;
			}
		}

		return result;
	}
}
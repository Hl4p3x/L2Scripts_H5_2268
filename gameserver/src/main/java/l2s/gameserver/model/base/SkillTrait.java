package l2s.gameserver.model.base;

import l2s.gameserver.Config;
import l2s.gameserver.stats.Env;
import l2s.gameserver.stats.Stats;

public enum SkillTrait
{
	NONE,
	BLEED
	{
		@Override
		public final double calcVuln(Env env)
		{
			return env.target.calcStat(Stats.BLEED_RESIST, env.character, env.skill);
		}

		@Override
		public final double calcProf(Env env)
		{
			return env.character.calcStat(Stats.BLEED_POWER, env.target, env.skill);
		}
	},
	BOSS,
	DEATH,
	DERANGEMENT
	{
		@Override
		public final double calcVuln(Env env)
		{
			return env.target.calcStat(Stats.MENTAL_RESIST, env.character, env.skill);
		}

		@Override
		public final double calcProf(Env env)
		{
			return Math.min(40., env.character.calcStat(Stats.MENTAL_POWER, env.target, env.skill) + calcEnchantMod(env));
		}
	},
	ETC,
	GUST,
	HOLD
	{
		@Override
		public final double calcVuln(Env env)
		{
			return env.target.calcStat(Stats.ROOT_RESIST, env.character, env.skill);
		}

		@Override
		public final double calcProf(Env env)
		{
			return env.character.calcStat(Stats.ROOT_POWER, env.target, env.skill);
		}
	},
	PARALYZE
	{
		@Override
		public final double calcVuln(Env env)
		{
			return env.target.calcStat(Stats.PARALYZE_RESIST, env.character, env.skill);
		}

		@Override
		public final double calcProf(Env env)
		{
			return env.character.calcStat(Stats.PARALYZE_POWER, env.target, env.skill);
		}
	},
	PHYSICAL_BLOCKADE,
	POISON
	{
		@Override
		public final double calcVuln(Env env)
		{
			return env.target.calcStat(Stats.POISON_RESIST, env.character, env.skill);
		}

		@Override
		public final double calcProf(Env env)
		{
			return env.character.calcStat(Stats.POISON_POWER, env.target, env.skill);
		}
	},
	SHOCK
	{
		@Override
		public final double calcVuln(Env env)
		{
			return env.target.calcStat(Stats.STUN_RESIST, env.character, env.skill);
		}

		@Override
		public final double calcProf(Env env)
		{
			return Math.min(40., env.character.calcStat(Stats.STUN_POWER, env.target, env.skill) + calcEnchantMod(env));
		}
	},
	SLEEP
	{
		@Override
		public final double calcVuln(Env env)
		{
			return env.target.calcStat(Stats.SLEEP_RESIST, env.character, env.skill);
		}

		@Override
		public final double calcProf(Env env)
		{
			return env.character.calcStat(Stats.SLEEP_POWER, env.target, env.skill);
		}
	},
	VALAKAS;

	public double calcVuln(Env env)
	{
		return 0;
	}

	public double calcProf(Env env)
	{
		return 0;
	}

	public double getChanceMod()
	{
		switch(this)
		{
			case BLEED:
				return Config.BLEED_DEBUFF_CHANCE_MOD;
			case BOSS:
				return Config.BOSS_DEBUFF_CHANCE_MOD;
			case DEATH:
				return Config.DEATH_DEBUFF_CHANCE_MOD;
			case DERANGEMENT:
				return Config.DERANGEMENT_DEBUFF_CHANCE_MOD;
			case ETC:
				return Config.ETC_DEBUFF_CHANCE_MOD;
			case GUST:
				return Config.GUST_DEBUFF_CHANCE_MOD;
			case HOLD:
				return Config.HOLD_DEBUFF_CHANCE_MOD;
			case PARALYZE:
				return Config.PARALYZE_DEBUFF_CHANCE_MOD;
			case PHYSICAL_BLOCKADE:
				return Config.PHYSICAL_BLOCKADE_DEBUFF_CHANCE_MOD;
			case POISON:
				return Config.POISON_DEBUFF_CHANCE_MOD;
			case SHOCK:
				return Config.SHOCK_DEBUFF_CHANCE_MOD;
			case SLEEP:
				return Config.SLEEP_DEBUFF_CHANCE_MOD;
			case VALAKAS:
				return Config.VALAKAS_DEBUFF_CHANCE_MOD;
		}
		return 1.;
	}

/*
	public double calcResistMod(Env env)
	{
		final double vulnMod = calcVuln(env);
		final double profMod = calcProf(env);
		final double maxResist = 90. + Math.max(calcEnchantMod(env), profMod * 0.85);
		return (maxResist - vulnMod) / 60.;
	}
*/
	public static double calcEnchantMod(Env env)
	{
		int enchantLevel = env.skill.getDisplayLevel();
		if (enchantLevel <= 100)
			return 0;
		enchantLevel = enchantLevel % 100;
		return env.skill.getEnchantLevelCount() == 15 ? enchantLevel * 2 : enchantLevel;
	}
}
package l2s.gameserver.skills;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import l2s.gameserver.model.Effect;
import l2s.gameserver.skills.effects.EffectAddSkills;
import l2s.gameserver.skills.effects.EffectAgathionRes;
import l2s.gameserver.skills.effects.EffectAggression;
import l2s.gameserver.skills.effects.EffectBetray;
import l2s.gameserver.skills.effects.EffectBlessNoblesse;
import l2s.gameserver.skills.effects.EffectBlockStat;
import l2s.gameserver.skills.effects.EffectBluff;
import l2s.gameserver.skills.effects.EffectBuff;
import l2s.gameserver.skills.effects.EffectCPDamPercent;
import l2s.gameserver.skills.effects.EffectCallSkills;
import l2s.gameserver.skills.effects.EffectCharge;
import l2s.gameserver.skills.effects.EffectCharmOfCourage;
import l2s.gameserver.skills.effects.EffectCombatPointHealOverTime;
import l2s.gameserver.skills.effects.EffectConsumeSoulsOverTime;
import l2s.gameserver.skills.effects.EffectCubic;
import l2s.gameserver.skills.effects.EffectCurseOfLifeFlow;
import l2s.gameserver.skills.effects.EffectDamOverTime;
import l2s.gameserver.skills.effects.EffectDamOverTimeLethal;
import l2s.gameserver.skills.effects.EffectDebuffImmunity;
import l2s.gameserver.skills.effects.EffectDeffenseSpecial;
import l2s.gameserver.skills.effects.EffectDestroySummon;
import l2s.gameserver.skills.effects.EffectDisarm;
import l2s.gameserver.skills.effects.EffectDiscord;
import l2s.gameserver.skills.effects.EffectDispelEffects;
import l2s.gameserver.skills.effects.EffectDummy;
import l2s.gameserver.skills.effects.EffectDummy2;
import l2s.gameserver.skills.effects.EffectDummy3;
import l2s.gameserver.skills.effects.EffectEnervation;
import l2s.gameserver.skills.effects.EffectFakeDeath;
import l2s.gameserver.skills.effects.EffectFear;
import l2s.gameserver.skills.effects.EffectGrow;
import l2s.gameserver.skills.effects.EffectHPDamPercent;
import l2s.gameserver.skills.effects.EffectHate;
import l2s.gameserver.skills.effects.EffectHeal;
import l2s.gameserver.skills.effects.EffectHealBlock;
import l2s.gameserver.skills.effects.EffectHealCPPercent;
import l2s.gameserver.skills.effects.EffectHealOverTime;
import l2s.gameserver.skills.effects.EffectHealPercent;
import l2s.gameserver.skills.effects.EffectHourglass;
import l2s.gameserver.skills.effects.EffectImmobilize;
import l2s.gameserver.skills.effects.EffectInterrupt;
import l2s.gameserver.skills.effects.EffectInvisible;
import l2s.gameserver.skills.effects.EffectInvulnerable;
import l2s.gameserver.skills.effects.EffectLDManaDamOverTime;
import l2s.gameserver.skills.effects.EffectLockInventory;
import l2s.gameserver.skills.effects.EffectMPDamPercent;
import l2s.gameserver.skills.effects.EffectManaDamOverTime;
import l2s.gameserver.skills.effects.EffectManaHeal;
import l2s.gameserver.skills.effects.EffectManaHealOverTime;
import l2s.gameserver.skills.effects.EffectManaHealPercent;
import l2s.gameserver.skills.effects.EffectMeditation;
import l2s.gameserver.skills.effects.EffectMute;
import l2s.gameserver.skills.effects.EffectMuteAll;
import l2s.gameserver.skills.effects.EffectMuteAttack;
import l2s.gameserver.skills.effects.EffectMutePhisycal;
import l2s.gameserver.skills.effects.EffectNegateEffects;
import l2s.gameserver.skills.effects.EffectNegateMusic;
import l2s.gameserver.skills.effects.EffectParalyze;
import l2s.gameserver.skills.effects.EffectPetrification;
import l2s.gameserver.skills.effects.EffectRandomHate;
import l2s.gameserver.skills.effects.EffectRelax;
import l2s.gameserver.skills.effects.EffectRemoveTarget;
import l2s.gameserver.skills.effects.EffectRoot;
import l2s.gameserver.skills.effects.EffectSalvation;
import l2s.gameserver.skills.effects.EffectServitorShare;
import l2s.gameserver.skills.effects.EffectSilentMove;
import l2s.gameserver.skills.effects.EffectSleep;
import l2s.gameserver.skills.effects.EffectStun;
import l2s.gameserver.skills.effects.EffectSymbol;
import l2s.gameserver.skills.effects.EffectTemplate;
import l2s.gameserver.skills.effects.EffectTransformation;
import l2s.gameserver.skills.effects.EffectUnAggro;
import l2s.gameserver.skills.effects.EffectVitalityDamOverTime;
import l2s.gameserver.skills.effects.EffectZombiesVsHumans1;
import l2s.gameserver.skills.effects.EffectZombiesVsHumans2;
import l2s.gameserver.skills.effects.EffectZombiesVsHumans3;
import l2s.gameserver.stats.Env;
import l2s.gameserver.stats.Stats;

public enum EffectType
{
	// Основные эффекты
	AddSkills(EffectAddSkills.class, null, false),
	AgathionResurrect(EffectAgathionRes.class, null, true),
	Aggression(EffectAggression.class, null, true),
	Betray(EffectBetray.class, null, Stats.MENTAL_RESIST, Stats.MENTAL_POWER, true),
	BlessNoblesse(EffectBlessNoblesse.class, null, true),
	BlockStat(EffectBlockStat.class, null, true),
	Buff(EffectBuff.class, null, false),
	Bluff(EffectBluff.class, AbnormalEffect.NONE, Stats.MENTAL_RESIST, Stats.MENTAL_POWER, true),
	DebuffImmunity(EffectDebuffImmunity.class, null, true),
	DispelEffects(EffectDispelEffects.class, null, Stats.CANCEL_RESIST, Stats.CANCEL_POWER, true),
	CallSkills(EffectCallSkills.class, null, false),
	CombatPointHealOverTime(EffectCombatPointHealOverTime.class, null, true),
	ConsumeSoulsOverTime(EffectConsumeSoulsOverTime.class, null, true),
	Charge(EffectCharge.class, null, false),
	CharmOfCourage(EffectCharmOfCourage.class, null, true),
	CPDamPercent(EffectCPDamPercent.class, null, true),
	Cubic(EffectCubic.class, null, true),
	DamOverTime(EffectDamOverTime.class, null, false),
	DamOverTimeLethal(EffectDamOverTimeLethal.class, null, false),
	DestroySummon(EffectDestroySummon.class, null, Stats.MENTAL_RESIST, Stats.MENTAL_POWER, true),
	Disarm(EffectDisarm.class, null, true),
	Discord(EffectDiscord.class, AbnormalEffect.DOT_SOIL, Stats.MENTAL_RESIST, Stats.MENTAL_POWER, true),
	Enervation(EffectEnervation.class, null, Stats.MENTAL_RESIST, Stats.MENTAL_POWER, false),
	FakeDeath(EffectFakeDeath.class, null, true),
	Fear(EffectFear.class, AbnormalEffect.DOT_WIND, Stats.MENTAL_RESIST, Stats.MENTAL_POWER, true),
	Grow(EffectGrow.class, AbnormalEffect.BIG_BODY, false),
	Hate(EffectHate.class, null, false),
	Heal(EffectHeal.class, null, false),
	HealBlock(EffectHealBlock.class, null, true),
	HealCPPercent(EffectHealCPPercent.class, null, true),
	HealOverTime(EffectHealOverTime.class, null, false),
	HealPercent(EffectHealPercent.class, null, false),
	HPDamPercent(EffectHPDamPercent.class, null, true),
	IgnoreSkill(EffectBuff.class, null, false),
	Immobilize(EffectImmobilize.class, null, true),
	Interrupt(EffectInterrupt.class, null, true),
	Invulnerable(EffectInvulnerable.class, null, false),
	Invisible(EffectInvisible.class, null, false),
	LockInventory(EffectLockInventory.class, null, false),
	CurseOfLifeFlow(EffectCurseOfLifeFlow.class, null, true),
	LDManaDamOverTime(EffectLDManaDamOverTime.class, null, true),
	ManaDamOverTime(EffectManaDamOverTime.class, null, true),
	ManaHeal(EffectManaHeal.class, null, false),
	ManaHealOverTime(EffectManaHealOverTime.class, null, false),
	ManaHealPercent(EffectManaHealPercent.class, null, false),
	Meditation(EffectMeditation.class, null, false),
	MPDamPercent(EffectMPDamPercent.class, null, true),
	Mute(EffectMute.class, AbnormalEffect.SILENCE, Stats.MENTAL_RESIST, Stats.MENTAL_POWER, true),
	MuteAll(EffectMuteAll.class, AbnormalEffect.SILENCE, Stats.MENTAL_RESIST, Stats.MENTAL_POWER, true),
	MuteAttack(EffectMuteAttack.class, AbnormalEffect.SILENCE, Stats.MENTAL_RESIST, Stats.MENTAL_POWER, true),
	MutePhisycal(EffectMutePhisycal.class, AbnormalEffect.SILENCE, Stats.MENTAL_RESIST, Stats.MENTAL_POWER, true),
	NegateEffects(EffectNegateEffects.class, null, false),
	NegateMusic(EffectNegateMusic.class, null, false),
	Paralyze(EffectParalyze.class, AbnormalEffect.PARALYZE, Stats.PARALYZE_RESIST, Stats.PARALYZE_POWER, true),
	Petrification(EffectPetrification.class, AbnormalEffect.FLESH_STONE, Stats.PARALYZE_RESIST, Stats.PARALYZE_POWER, true),
	RandomHate(EffectRandomHate.class, null, true),
	Relax(EffectRelax.class, null, true),
	RemoveTarget(EffectRemoveTarget.class, null, true),
	Root(EffectRoot.class, AbnormalEffect.ROOT, Stats.ROOT_RESIST, Stats.ROOT_POWER, true),
	Hourglass(EffectHourglass.class, null, true),
	Salvation(EffectSalvation.class, null, true),
	ServitorShare(EffectServitorShare.class, null, true),
	SilentMove(EffectSilentMove.class, AbnormalEffect.STEALTH, true),
	Sleep(EffectSleep.class, AbnormalEffect.SLEEP, Stats.SLEEP_RESIST, Stats.SLEEP_POWER, true),
	Stun(EffectStun.class, AbnormalEffect.STUN, Stats.STUN_RESIST, Stats.STUN_POWER, true),
	Symbol(EffectSymbol.class, null, false),
	Transformation(EffectTransformation.class, null, true),
	UnAggro(EffectUnAggro.class, null, true),
	Vitality(EffectBuff.class, AbnormalEffect.VP_UP, true),
	VitalityMaintenance(EffectBuff.class, AbnormalEffect.VP_UP, true),
	VitalityDamOverTime(EffectVitalityDamOverTime.class, AbnormalEffect.VP_UP, true),
	DeffenseSpecial(EffectDeffenseSpecial.class, null, false),
	Dummy(EffectDummy.class, null, false),
	Dummy2(EffectDummy2.class, null, false),
	Dummy3(EffectDummy3.class, null, false),
	// Производные от основных эффектов
	Poison(EffectDamOverTime.class, null, Stats.POISON_RESIST, Stats.POISON_POWER, false),
	PoisonLethal(EffectDamOverTimeLethal.class, null, Stats.POISON_RESIST, Stats.POISON_POWER, false),
	Bleed(EffectDamOverTime.class, null, Stats.BLEED_RESIST, Stats.BLEED_POWER, false),
	Debuff(EffectBuff.class, null, false),
	WatcherGaze(EffectBuff.class, null, false),
	ZombiesVsHumans1(EffectZombiesVsHumans1.class, null, false),
	ZombiesVsHumans2(EffectZombiesVsHumans2.class, null, false),
	ZombiesVsHumans3(EffectZombiesVsHumans3.class, null, false),	

	AbsorbDamageToEffector(EffectBuff.class, null, false), // absorbs part of the effects of damage to effektora
	AbsorbDamageToMp(EffectBuff.class, AbnormalEffect.MP_SHIELD, false), // absorbs part of the damage in MP
	AbsorbDamageToSummon(EffectLDManaDamOverTime.class, null, true); // absorbs part of the damage to Soumont

	private final Constructor<? extends Effect> _constructor;
	private final AbnormalEffect _abnormal;
	private final Stats _resistType;
	private final Stats _attributeType;
	private final boolean _isRaidImmune;

	private EffectType(Class<? extends Effect> clazz, AbnormalEffect abnormal, boolean isRaidImmune)
	{
		this(clazz, abnormal, null, null, isRaidImmune);
	}

	private EffectType(Class<? extends Effect> clazz, AbnormalEffect abnormal, Stats resistType, Stats attributeType, boolean isRaidImmune)
	{
		try
		{
			_constructor = clazz.getConstructor(Env.class, EffectTemplate.class);
		}
		catch(NoSuchMethodException e)
		{
			throw new Error(e);
		}
		_abnormal = abnormal;
		_resistType = resistType;
		_attributeType = attributeType;
		_isRaidImmune = isRaidImmune;
	}

	public AbnormalEffect getAbnormal()
	{
		return _abnormal;
	}

	public Stats getResistType()
	{
		return _resistType;
	}

	public Stats getAttributeType()
	{
		return _attributeType;
	}

	public boolean isRaidImmune()
	{
		return _isRaidImmune;
	}

	public Effect makeEffect(Env env, EffectTemplate template) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException
	{
		return _constructor.newInstance(env, template);
	}
}
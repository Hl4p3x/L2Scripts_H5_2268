package l2s.gameserver.model;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.collections.LazyArrayList;
import l2s.commons.geometry.Polygon;
import l2s.commons.lang.ArrayUtils;
import l2s.commons.threading.RunnableImpl;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.string.SkillNameHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.instancemanager.games.HandysBlockCheckerManager;
import l2s.gameserver.instancemanager.games.HandysBlockCheckerManager.ArenaParticipantsHolder;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.base.BaseStats;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.Element;
import l2s.gameserver.model.base.SkillTrait;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.instances.FeedableBeastInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.TreasureChestInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo;
import l2s.gameserver.network.l2.s2c.FlyToLocationPacket.FlyType;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.skills.effects.EffectTemplate;
import l2s.gameserver.skills.skillclasses.AIeffects;
import l2s.gameserver.skills.skillclasses.Aggression;
import l2s.gameserver.skills.skillclasses.Balance;
import l2s.gameserver.skills.skillclasses.BeastFeed;
import l2s.gameserver.skills.skillclasses.BuffCharger;
import l2s.gameserver.skills.skillclasses.CPDam;
import l2s.gameserver.skills.skillclasses.Call;
import l2s.gameserver.skills.skillclasses.ChainHeal;
import l2s.gameserver.skills.skillclasses.Charge;
import l2s.gameserver.skills.skillclasses.ChargeSoul;
import l2s.gameserver.skills.skillclasses.ClanGate;
import l2s.gameserver.skills.skillclasses.CombatPointHeal;
import l2s.gameserver.skills.skillclasses.Continuous;
import l2s.gameserver.skills.skillclasses.Craft;
import l2s.gameserver.skills.skillclasses.CurseDivinity;
import l2s.gameserver.skills.skillclasses.Decoy;
import l2s.gameserver.skills.skillclasses.Default;
import l2s.gameserver.skills.skillclasses.DefuseTrap;
import l2s.gameserver.skills.skillclasses.DeleteHate;
import l2s.gameserver.skills.skillclasses.DeleteHateOfMe;
import l2s.gameserver.skills.skillclasses.DestroySummon;
import l2s.gameserver.skills.skillclasses.DetectTrap;
import l2s.gameserver.skills.skillclasses.Disablers;
import l2s.gameserver.skills.skillclasses.Drain;
import l2s.gameserver.skills.skillclasses.DrainSoul;
import l2s.gameserver.skills.skillclasses.EffectsFromSkills;
import l2s.gameserver.skills.skillclasses.EnergyReplenish;
import l2s.gameserver.skills.skillclasses.ExtractStone;
import l2s.gameserver.skills.skillclasses.FishingSkill;
import l2s.gameserver.skills.skillclasses.Harvesting;
import l2s.gameserver.skills.skillclasses.Heal;
import l2s.gameserver.skills.skillclasses.HealPercent;
import l2s.gameserver.skills.skillclasses.InstantJump;
import l2s.gameserver.skills.skillclasses.KamaelWeaponExchange;
import l2s.gameserver.skills.skillclasses.LethalShot;
import l2s.gameserver.skills.skillclasses.MDam;
import l2s.gameserver.skills.skillclasses.ManaDam;
import l2s.gameserver.skills.skillclasses.ManaHeal;
import l2s.gameserver.skills.skillclasses.ManaHealPercent;
import l2s.gameserver.skills.skillclasses.NegateEffects;
import l2s.gameserver.skills.skillclasses.NegateStats;
import l2s.gameserver.skills.skillclasses.PDam;
import l2s.gameserver.skills.skillclasses.PcBangPointsAdd;
import l2s.gameserver.skills.skillclasses.PetSummon;
import l2s.gameserver.skills.skillclasses.Recall;
import l2s.gameserver.skills.skillclasses.RecoverAll;
import l2s.gameserver.skills.skillclasses.ReelingPumping;
import l2s.gameserver.skills.skillclasses.Refill;
import l2s.gameserver.skills.skillclasses.Resurrect;
import l2s.gameserver.skills.skillclasses.Ride;
import l2s.gameserver.skills.skillclasses.SPHeal;
import l2s.gameserver.skills.skillclasses.SelfSacrifice;
import l2s.gameserver.skills.skillclasses.ShiftAggression;
import l2s.gameserver.skills.skillclasses.Sowing;
import l2s.gameserver.skills.skillclasses.Spoil;
import l2s.gameserver.skills.skillclasses.StealBuff;
import l2s.gameserver.skills.skillclasses.SummonItem;
import l2s.gameserver.skills.skillclasses.SummonSiegeFlag;
import l2s.gameserver.skills.skillclasses.Sweep;
import l2s.gameserver.skills.skillclasses.TakeCastle;
import l2s.gameserver.skills.skillclasses.TakeFlag;
import l2s.gameserver.skills.skillclasses.TakeFortress;
import l2s.gameserver.skills.skillclasses.TameControl;
import l2s.gameserver.skills.skillclasses.Toggle;
import l2s.gameserver.skills.skillclasses.Transformation;
import l2s.gameserver.skills.skillclasses.Unlock;
import l2s.gameserver.skills.skillclasses.VitalityHeal;
import l2s.gameserver.stats.Env;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.StatTemplate;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.conditions.Condition;
import l2s.gameserver.stats.funcs.Func;
import l2s.gameserver.stats.funcs.FuncTemplate;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.PositionUtils;
import l2s.gameserver.utils.SkillUtils;

public abstract class Skill extends StatTemplate implements Cloneable
{
	public static class AddedSkill
	{
		public static final AddedSkill[] EMPTY_ARRAY = new AddedSkill[0];

		public int id;
		public int level;
		private Skill _skill;

		public AddedSkill(int id, int level)
		{
			this.id = id;
			this.level = level;
		}

		public Skill getSkill()
		{
			if(_skill == null)
				_skill = SkillHolder.getInstance().getSkill(id, level);
			return _skill;
		}
	}

	public static enum NextAction
	{
		ATTACK,
		CAST,
		DEFAULT,
		MOVE,
		NONE
	}

	public static enum SkillOpType
	{
		OP_ACTIVE,
		OP_PASSIVE,
		OP_TOGGLE
	}

	public static enum Ternary
	{
		TRUE,
		FALSE,
		DEFAULT
	}

	public static enum SkillMagicType
	{
		PHYSIC,
		MAGIC,
		SPECIAL,
		MUSIC
	}

	public static enum SkillTargetType
	{
		TARGET_ALLY,
		TARGET_FRIEND,
		TARGET_AREA,
		TARGET_AREA_AIM_CORPSE,
		TARGET_AURA,
		TARGET_SERVITOR_AURA,
		TARGET_CHEST,
		TARGET_FEEDABLE_BEAST,
		TARGET_CLAN,
		TARGET_CLAN_ONLY,
		TARGET_CORPSE,
		TARGET_CORPSE_PLAYER,
		TARGET_ENEMY_PET,
		TARGET_ENEMY_SUMMON,
		TARGET_ENEMY_SERVITOR,
		TARGET_EVENT,
		TARGET_FLAGPOLE,
		TARGET_COMMCHANNEL,
		TARGET_HOLY,
		TARGET_ITEM,
		TARGET_MULTIFACE,
		TARGET_MULTIFACE_AURA,
		TARGET_TUNNEL,
		TARGET_NONE,
		TARGET_ONE,
		TARGET_OWNER,
		TARGET_PARTY,
		TARGET_PARTY_ONE,
		TARGET_SERVITOR,
		TARGET_SUMMON,
		TARGET_PET,
		TARGET_SELF,
		TARGET_SIEGE,
		TARGET_UNLOCKABLE
	}

	public static enum SkillType
	{
		AGGRESSION(Aggression.class),
		AIEFFECTS(AIeffects.class),
		BALANCE(Balance.class),
		BEAST_FEED(BeastFeed.class),
		BLEED(Continuous.class),
		BUFF(Continuous.class),
		BUFF_CHARGER(BuffCharger.class),
		CALL(Call.class),
		CHAIN_HEAL(ChainHeal.class),
		CHARGE(Charge.class),
		CHARGE_SOUL(ChargeSoul.class),
		CLAN_GATE(ClanGate.class),
		COMBATPOINTHEAL(CombatPointHeal.class),
		CONT(Toggle.class),
		CPDAM(CPDam.class),
		CPHOT(Continuous.class),
		CRAFT(Craft.class),
		DEATH_PENALTY(l2s.gameserver.skills.skillclasses.DeathPenalty.class),
		DECOY(Decoy.class),
		DEBUFF(Continuous.class),
		DELETE_HATE(DeleteHate.class),
		DELETE_HATE_OF_ME(DeleteHateOfMe.class),
		DESTROY_SUMMON(DestroySummon.class),
		DEFUSE_TRAP(DefuseTrap.class),
		DETECT_TRAP(DetectTrap.class),
		DISCORD(Continuous.class),
		DOT(Continuous.class),
		DRAIN(Drain.class),
		DRAIN_SOUL(DrainSoul.class),
		EFFECT(l2s.gameserver.skills.skillclasses.Effect.class),
		EFFECTS_FROM_SKILLS(EffectsFromSkills.class),
		ENERGY_REPLENISH(EnergyReplenish.class),
		ENCHANT_ARMOR,
		ENCHANT_WEAPON,
		EXTRACT_STONE(ExtractStone.class),
		FEED_PET,
		FISHING(FishingSkill.class),
		HARDCODED(l2s.gameserver.skills.skillclasses.Effect.class),
		HARVESTING(Harvesting.class),
		HEAL(Heal.class),
		HEAL_PERCENT(HealPercent.class),
		HOT(Continuous.class),
		INSTANT_JUMP(InstantJump.class),
		KAMAEL_WEAPON_EXCHANGE(KamaelWeaponExchange.class),
		LETHAL_SHOT(LethalShot.class),
		LUCK,
		MANADAM(ManaDam.class),
		MANAHEAL(ManaHeal.class),
		MANAHEAL_PERCENT(ManaHealPercent.class),
		MDAM(MDam.class),
		MDOT(Continuous.class),
		MPHOT(Continuous.class),
		MUTE(Disablers.class),
		NEGATE_EFFECTS(NegateEffects.class),
		NEGATE_STATS(NegateStats.class),
		ADD_PC_BANG(PcBangPointsAdd.class),
		NOTDONE,
		NOTUSED,
		PARALYZE(Disablers.class),
		PASSIVE,
		PDAM(PDam.class),
		PET_SUMMON(PetSummon.class),
		POISON(Continuous.class),
		PUMPING(ReelingPumping.class),
		RECALL(Recall.class),
		REELING(ReelingPumping.class),
		REFILL(Refill.class),
		RESURRECT(Resurrect.class),
		RIDE(Ride.class),
		ROOT(Disablers.class),
		//self sacrifice - targets specifications
		SELF_SACRIFICE(SelfSacrifice.class),
		SHIFT_AGGRESSION(ShiftAggression.class),
		SLEEP(Disablers.class),
		SOULSHOT,
		SOWING(Sowing.class),
		SPHEAL(SPHeal.class),
		SPIRITSHOT,
		SPOIL(Spoil.class),
		STEAL_BUFF(StealBuff.class),
		CURSE_DIVINITY(CurseDivinity.class),
		STUN(Disablers.class),
		SUMMON(l2s.gameserver.skills.skillclasses.Summon.class),
		SUMMON_FLAG(SummonSiegeFlag.class),
		SUMMON_ITEM(SummonItem.class),
		SWEEP(Sweep.class),
		TAKECASTLE(TakeCastle.class),
		TAKEFORTRESS(TakeFortress.class),
		TAMECONTROL(TameControl.class),
		TAKEFLAG(TakeFlag.class),
		TRANSFORMATION(Transformation.class),
		UNLOCK(Unlock.class),
		WATCHER_GAZE(Continuous.class),
		VITALITY_HEAL(VitalityHeal.class),
		RECOVER_ALL(RecoverAll.class);

		private final Class<? extends Skill> clazz;

		private SkillType()
		{
			clazz = Default.class;
		}

		private SkillType(Class<? extends Skill> clazz)
		{
			this.clazz = clazz;
		}

		public Skill makeSkill(StatsSet set)
		{
			try
			{
				Constructor<? extends Skill> c = clazz.getConstructor(StatsSet.class);
				return c.newInstance(set);
			}
			catch(Exception e)
			{
				_log.error("", e);
				throw new RuntimeException(e);
			}
		}

		/**
		 * Ð Ð°Ð±Ð¾Ñ‚Ð°ÑŽÑ‚ Ñ‚Ð¾Ð»ÑŒÐºÐ¾ Ð¿Ñ€Ð¾Ñ‚Ð¸Ð² npc
		 */
		public final boolean isPvM()
		{
			switch(this)
			{
				case DISCORD:
					return true;
				default:
					return false;
			}
		}

		/**
		 * Ð¢Ð°ÐºÐ¸Ðµ Ñ�ÐºÐ¸Ð»Ð»Ñ‹ Ð½Ðµ Ð°Ð³Ð³Ñ€Ñ�Ñ‚ Ñ†ÐµÐ»ÑŒ, Ð¸ Ð½Ðµ Ñ„Ð»Ð°Ð³Ð°ÑŽÑ‚ Ñ‡Ð°Ñ€Ð°, Ð½Ð¾ Ñ�Ð²Ð»Ñ�ÑŽÑ‚Ñ�Ñ� "Ð¿Ð»Ð¾Ñ…Ð¸Ð¼Ð¸"
		 */
		public boolean isAI()
		{
			switch(this)
			{
				case AGGRESSION:
				case AIEFFECTS:
				case SOWING:
				case DELETE_HATE:
				case DELETE_HATE_OF_ME:
					return true;
				default:
					return false;
			}
		}

		public final boolean isPvpSkill()
		{
			switch(this)
			{
				case BLEED:
				case AGGRESSION:
				case DEBUFF:
				case DOT:
				case MDOT:
				case MUTE:
				case PARALYZE:
				case POISON:
				case ROOT:
				case SLEEP:
				case MANADAM:
				case DESTROY_SUMMON:
				case NEGATE_STATS:
				case NEGATE_EFFECTS:
				case STEAL_BUFF:
				case CURSE_DIVINITY:
				case DELETE_HATE:
				case DELETE_HATE_OF_ME:
					return true;
				default:
					return false;
			}
		}
		public boolean isBuff()
		{
			switch(this)
			{
				case BUFF:
					return true;
				default:
					return false;
			}
		}

		public boolean isOffensive()
		{
			switch(this)
			{
				case AGGRESSION:
				case AIEFFECTS:
				case BLEED:
				case DEBUFF:
				case DOT:
				case DRAIN:
				case DRAIN_SOUL:
				case LETHAL_SHOT:
				case MANADAM:
				case MDAM:
				case MDOT:
				case MUTE:
				case PARALYZE:
				case PDAM:
				case CPDAM:
				case POISON:
				case ROOT:
				case SLEEP:
				case SOULSHOT:
				case SPIRITSHOT:
				case SPOIL:
				case STUN:
				case SWEEP:
				case HARVESTING:
				case SOWING:
				case DELETE_HATE:
				case DELETE_HATE_OF_ME:
				case DESTROY_SUMMON:
				case STEAL_BUFF:
				case CURSE_DIVINITY:
				case DISCORD:
				case INSTANT_JUMP:
					return true;
				default:
					return false;
			}
		}
	}

	public boolean isPenalty()
	{
		return _skillType == SkillType.DEATH_PENALTY || _id == 4267 || _id == 4270;
	}

	private static final Logger _log = LoggerFactory.getLogger(Skill.class);

	public static final Skill[] EMPTY_ARRAY = new Skill[0];

	protected EffectTemplate[] _effectTemplates = EffectTemplate.EMPTY_ARRAY;
	protected EffectTemplate[] _iEffectTemplates = EffectTemplate.EMPTY_ARRAY;

	protected List<Integer> _teachers; // which NPC teaches
	protected List<ClassId> _canLearn; // which classes can learn

	protected AddedSkill[] _addedSkills = AddedSkill.EMPTY_ARRAY;

	protected final int[] _itemConsume;
	protected final int[] _itemConsumeId;
	protected final int _referenceItemId; // Ð´Ð»Ñ� Ñ‚Ð°Ð»Ð¸Ñ�Ð¼Ð°Ð½Ð¾Ð²
	protected final int _referenceItemMpConsume; // ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ Ð¿Ð¾Ñ‚Ñ€ÐµÐ±Ð»Ñ�ÐµÐ¼Ð¾Ð¹ Ð¼Ð¿ Ñ‚Ð°Ð»Ð¸Ñ�Ð¼Ð°Ð½Ð°

	//public static final int SKILL_CUBIC_MASTERY = 143;
	public static final int SKILL_CRAFTING = 172;
	public static final int SKILL_POLEARM_MASTERY = 216;
	public static final int SKILL_CRYSTALLIZE = 248;
	public static final int SKILL_WEAPON_MAGIC_MASTERY1 = 249;
	public static final int SKILL_WEAPON_MAGIC_MASTERY2 = 250;
	public static final int SKILL_BLINDING_BLOW = 321;
	public static final int SKILL_STRIDER_ASSAULT = 325;
	public static final int SKILL_WYVERN_AEGIS = 327;
	public static final int SKILL_BLUFF = 358;
	public static final int SKILL_HEROIC_MIRACLE = 395;
	public static final int SKILL_HEROIC_BERSERKER = 396;
	public static final int SKILL_SOUL_MASTERY = 467;
	public static final int SKILL_TRANSFORM_DISPEL = 619;
	public static final int SKILL_FINAL_FLYING_FORM = 840;
	public static final int SKILL_AURA_BIRD_FALCON = 841;
	public static final int SKILL_AURA_BIRD_OWL = 842;
	public static final int SKILL_DETECTION = 933;
	public static final int SKILL_RECHARGE = 1013;
	public static final int SKILL_TRANSFER_PAIN = 1262;
	public static final int SKILL_FISHING_MASTERY = 1315;
	public static final int SKILL_NOBLESSE_BLESSING = 1323;
	public static final int SKILL_SUMMON_CP_POTION = 1324;
	public static final int SKILL_FORTUNE_OF_NOBLESSE = 1325;
	public static final int SKILL_HARMONY_OF_NOBLESSE = 1326;
	public static final int SKILL_SYMPHONY_OF_NOBLESSE = 1327;
	public static final int SKILL_HEROIC_VALOR = 1374;
	public static final int SKILL_HEROIC_GRANDEUR = 1375;
	public static final int SKILL_HEROIC_DREAD = 1376;
	public static final int SKILL_MYSTIC_IMMUNITY = 1411;
	public static final int SKILL_RAID_BLESSING = 2168;
	public static final int SKILL_HINDER_STRIDER = 4258;
	public static final int SKILL_WYVERN_BREATH = 4289;
	public static final int SKILL_RAID_CURSE = 4515;
	public static final int SKILL_CHARM_OF_COURAGE = 5041;
	public static final int SKILL_EVENT_TIMER = 5239;
	public static final int SKILL_BATTLEFIELD_DEATH_SYNDROME = 5660;
	public static final int SKILL_SERVITOR_SHARE = 1557;

	protected boolean _isAltUse;
	protected boolean _isBehind;
	protected boolean _isCancelable;
	protected boolean _isCorpse;
	protected boolean _isCommon;
	protected boolean _isItemHandler;
	protected boolean _isOffensive;
	protected boolean _isBuff;
	protected boolean _isPvpSkill;
	protected boolean _isNotUsedByAI;
	protected boolean _isFishingSkill;
	protected boolean _isPvm;
	protected boolean _isForceUse;
	protected boolean _isNewbie;
	protected boolean _isPreservedOnDeath;
	protected boolean _isHeroic;
	protected boolean _isSaveable;
	protected boolean _isSkillTimePermanent;
	protected boolean _isReuseDelayPermanent;
	protected boolean _isReflectable;
	protected boolean _isSuicideAttack;
	protected boolean _isShieldignore;
	protected boolean _isUndeadOnly;
	protected Ternary _isUseSS;
	protected boolean _isOverhit;
	protected boolean _isSoulBoost;
	protected boolean _isChargeBoost;
	protected boolean _isUsingWhileCasting;
	protected boolean _isIgnoreResists;
	protected boolean _isIgnoreInvul;
	protected boolean _isTrigger;
	protected boolean _isNotAffectedByMute;
	protected boolean _basedOnTargetDebuff;
	protected boolean _deathlink;
	protected boolean _hideStartMessage;
	protected boolean _hideUseMessage;
	protected boolean _skillInterrupt;
	protected boolean _flyingTransformUsage;
	protected boolean _canUseTeleport;
	protected boolean _isProvoke;
	protected boolean _isCubicSkill = false;
	protected boolean _isSelfDispellable;
	protected boolean _isAbnormalInstant;

	protected SkillType _skillType;
	protected SkillOpType _operateType;
	protected SkillTargetType _targetType;
	protected SkillMagicType _magicType;
	protected SkillTrait _traitType;
	protected BaseStats _saveVs;
	protected NextAction _nextAction;
	protected Element _element;
	protected FlyType _flyType;
	protected boolean _flyToBack;
	protected Condition[] _preCondition = Condition.EMPTY_ARRAY;

	protected int _id;
	protected int _level;
	protected int _baseLevel;
	protected int _displayId;
	protected int _displayLevel;

	protected int _activateRate;
	protected int _castRange;
	protected int _cancelTarget;
	protected int _condCharges;
	protected int _coolTime;
	protected int _delayedEffect;
	protected int _effectPoint;
	protected int _energyConsume;
	protected int _elementPower;
	protected int _flyRadius;
	protected int _hitTime;
	protected int _hpConsume;
	protected int _vitConsume;
	protected int _levelModifier;
	protected int _magicLevel;
	protected int _matak;
	protected int _minPledgeClass;
	protected int _minRank;
	protected int _negatePower;
	protected int _negateSkill;
	protected int _npcId;
	protected int _numCharges;
	protected int _skillInterruptTime;
	protected int _skillRadius;
	protected int _soulsConsume;
	protected int _symbolId;
	protected int _weaponsAllowed;
	protected int _castCount;
	protected int _enchantLevelCount;
	protected int _criticalRate;
	protected int _numberOfCounterAttacks;
	private final boolean _UDSafe;
	private final boolean _stopActor;

	protected long _reuseDelay;

	protected double _power;
	protected double _powerPvP;
	protected double _powerPvE;
	protected double _mpConsume1;
	protected double _mpConsume2;
	protected double _lethal1;
	protected double _lethal2;
	protected double _absorbPart;

	protected String _name;
	protected String _baseValues;
	protected String _icon;

	public boolean _isStandart = false;

	private final int hashCode;

	private final double _onAttackCancelChance;
	private final double _onCritCancelChance;

	private final int _reuseSkillId;
	private final int _reuseHash;

	/**
	 * Ð’Ð½Ð¸Ð¼Ð°Ð½Ð¸Ðµ!!! Ð£ Ð½Ð°Ñ�Ð»ÐµÐ´Ð½Ð¸ÐºÐ¾Ð² Ð²Ñ€ÑƒÑ‡Ð½ÑƒÑŽ Ð½Ð°Ð´Ð¾ Ð¿Ð¾Ð¼ÐµÐ½Ñ�Ñ‚ÑŒ Ñ‚Ð¸Ð¿ Ð½Ð° public
	 * @param set Ð¿Ð°Ñ€Ð°Ð¼ÐµÑ€Ñ‚Ñ‹ Ñ�ÐºÐ¸Ð»Ð»Ð°
	 */
	protected Skill(StatsSet set)
	{
		//_set = set;
		_id = set.getInteger("skill_id");
		_level = set.getInteger("level");
		_displayId = set.getInteger("displayId", _id);
		_displayLevel = set.getInteger("displayLevel", _level);
		_baseLevel = set.getInteger("base_level");
		_name = set.getString("name");
		_operateType = set.getEnum("operateType", SkillOpType.class);
		_isNewbie = set.getBool("isNewbie", false);
		_isSelfDispellable = set.getBool("isSelfDispellable", true);
		_isPreservedOnDeath = set.getBool("isPreservedOnDeath", false);
		_isHeroic = set.getBool("isHeroic", false);
		_isAltUse = set.getBool("altUse", false);
		_mpConsume1 = set.getInteger("mpConsume1", 0);
		_mpConsume2 = set.getInteger("mpConsume2", 0);
		_energyConsume = set.getInteger("energyConsume", 0);
		_vitConsume = set.getInteger("vitConsume", 0); 
		_hpConsume = set.getInteger("hpConsume", 0);
		_soulsConsume = set.getInteger("soulsConsume", 0);
		_isSoulBoost = set.getBool("soulBoost", false);
		_isChargeBoost = set.getBool("chargeBoost", false);
		_isProvoke = set.getBool("provoke", false);
		_isUsingWhileCasting = set.getBool("isUsingWhileCasting", false);
		_matak = set.getInteger("mAtk", 0);
		_isUseSS = Ternary.valueOf(set.getString("useSS", Ternary.DEFAULT.toString()).toUpperCase());
		_magicLevel = set.getInteger("magicLevel", 0);
		_castCount = set.getInteger("castCount", 0);
		_castRange = set.getInteger("castRange", 40);
		_baseValues = set.getString("baseValues", null);

		String s1 = set.getString("itemConsumeCount", "");
		String s2 = set.getString("itemConsumeId", "");

		if(s1.length() == 0)
			_itemConsume = new int[] { 0 };
		else
		{
			String[] s = s1.split(" ");
			_itemConsume = new int[s.length];
			for(int i = 0; i < s.length; i++)
				_itemConsume[i] = Integer.parseInt(s[i]);
		}

		if(s2.length() == 0)
			_itemConsumeId = new int[] { 0 };
		else
		{
			String[] s = s2.split(" ");
			_itemConsumeId = new int[s.length];
			for(int i = 0; i < s.length; i++)
				_itemConsumeId[i] = Integer.parseInt(s[i]);
		}

		_referenceItemId = set.getInteger("referenceItemId", 0);
		_referenceItemMpConsume = set.getInteger("referenceItemMpConsume", 0);

		_isItemHandler = set.getBool("isHandler", false);
		_isCommon = set.getBool("isCommon", false);
		_isSaveable = set.getBool("isSaveable", true);
		_coolTime = set.getInteger("coolTime", 0);
		_skillInterruptTime = set.getInteger("hitCancelTime", 0);
		_reuseDelay = set.getLong("reuseDelay", 0);
		_hitTime = set.getInteger("hitTime", 0);
		_skillRadius = set.getInteger("skillRadius", 80);
		_targetType = set.getEnum("target", SkillTargetType.class, SkillTargetType.TARGET_SELF);
		_magicType = set.getEnum("magicType", SkillMagicType.class, SkillMagicType.PHYSIC);
		_traitType = set.getEnum("trait", SkillTrait.class, null);
		_saveVs = set.getEnum("saveVs", BaseStats.class, null);
		_hideStartMessage = set.getBool("isHideStartMessage", false);
		_hideUseMessage = set.getBool("isHideUseMessage", false);
		_isUndeadOnly = set.getBool("undeadOnly", false);
		_isCorpse = set.getBool("corpse", false);
		_power = set.getDouble("power", 0.);
		_powerPvP = set.getDouble("powerPvP", 0.);
		_powerPvE = set.getDouble("powerPvE", 0.);
		_effectPoint = set.getInteger("effectPoint", 0);
		_nextAction = NextAction.valueOf(set.getString("nextAction", "DEFAULT").toUpperCase());
		_skillType = set.getEnum("skillType", SkillType.class, SkillType.NOTDONE);
		_isSuicideAttack = set.getBool("isSuicideAttack", false);
		_isSkillTimePermanent = set.getBool("isSkillTimePermanent", false);
		_isReuseDelayPermanent = set.getBool("isReuseDelayPermanent", false);
		_deathlink = set.getBool("deathlink", false);
		_basedOnTargetDebuff = set.getBool("basedOnTargetDebuff", false);
		_isNotUsedByAI = set.getBool("isNotUsedByAI", false);
		_isIgnoreResists = set.getBool("isIgnoreResists", false);
		_isIgnoreInvul = set.getBool("isIgnoreInvul", false);
		_isTrigger = set.getBool("isTrigger", false);
		_isNotAffectedByMute = set.getBool("isNotAffectedByMute", false);
		_flyingTransformUsage = set.getBool("flyingTransformUsage", false);
		_canUseTeleport = set.getBool("canUseTeleport", true);

		if(NumberUtils.isCreatable(set.getString("element", "NONE")))
			_element = Element.getElementById(set.getInteger("element", -1));
		else
			_element = Element.getElementByName(set.getString("element", "none").toUpperCase());

		_elementPower = set.getInteger("elementPower", 0);

		_activateRate = set.getInteger("activateRate", -1);
		_levelModifier = set.getInteger("levelModifier", 1);
		_isCancelable = set.getBool("cancelable", true);
		_isReflectable = set.getBool("reflectable", true);
		_isShieldignore = set.getBool("shieldignore", false);
		_criticalRate = set.getInteger("criticalRate", 0);
		_isOverhit = set.getBool("overHit", false);
		_weaponsAllowed = set.getInteger("weaponsAllowed", 0);
		_minPledgeClass = set.getInteger("minPledgeClass", 0);
		_minRank = set.getInteger("minRank", 0);
		_isOffensive = set.getBool("isOffensive", _skillType.isOffensive());
		_isBuff = set.getBool("isOffensive", _skillType.isBuff());
		_isPvpSkill = set.getBool("isPvpSkill", _skillType.isPvpSkill());
		_isFishingSkill = set.getBool("isFishingSkill", false);
		_isPvm = set.getBool("isPvm", _skillType.isPvM());
		_isForceUse = set.getBool("isForceUse", false);
		_isBehind = set.getBool("behind", false);
		_symbolId = set.getInteger("symbolId", 0);
		_npcId = set.getInteger("npcId", 0);
		_flyType = FlyType.valueOf(set.getString("flyType", "NONE").toUpperCase());
		_flyToBack = set.getBool("flyToBack", false);
		_flyRadius = set.getInteger("flyRadius", 200);
		_negateSkill = set.getInteger("negateSkill", 0);
		_negatePower = set.getInteger("negatePower", Integer.MAX_VALUE);
		_numCharges = set.getInteger("num_charges", 0);
		_condCharges = set.getInteger("cond_charges", 0);
		_delayedEffect = set.getInteger("delayedEffect", 0);
		_cancelTarget = set.getInteger("cancelTarget", 0);
		_skillInterrupt = set.getBool("skillInterrupt", false);
		_lethal1 = set.getDouble("lethal1", 0.);
		_lethal2 = set.getDouble("lethal2", 0.);
		_absorbPart = set.getDouble("absorbPart", 0.);
		_icon = set.getString("icon", "");
		_numberOfCounterAttacks = set.getInteger("numberOfCounterAttacks", -1);
		_UDSafe = set.getBool("UDSafe", !isOffensive());
		_stopActor = set.getBool("stopActor", true);
		_isAbnormalInstant = set.getBool("abnormal_instant", false);
		
		StringTokenizer st = new StringTokenizer(set.getString("addSkills", ""), ";");
		while(st.hasMoreTokens())
		{
			int id = Integer.parseInt(st.nextToken());
			int level = Integer.parseInt(st.nextToken());
			if(level == -1)
				level = _level;
			_addedSkills = ArrayUtils.add(_addedSkills, new AddedSkill(id, level));
		}

		if(_nextAction == NextAction.DEFAULT)
			switch(_skillType)
			{
				case PDAM:
				case CPDAM:
				case LETHAL_SHOT:
				case SPOIL:
				case SOWING:
				case STUN:
				case DRAIN_SOUL:
					_nextAction = NextAction.ATTACK;
					break;
				default:
					_nextAction = NextAction.NONE;
			}

		_reuseSkillId = set.getInteger("reuse_skill_id", _id);
		_reuseHash = SkillUtils.generateSkillHashCode(_reuseSkillId, _level);

		String canLearn = set.getString("canLearn", null);
		if(canLearn == null)
			_canLearn = null;
		else
		{
			_canLearn = new ArrayList<ClassId>();
			st = new StringTokenizer(canLearn, " \r\n\t,;");
			while(st.hasMoreTokens())
			{
				String cls = st.nextToken();
				_canLearn.add(ClassId.valueOf(cls));
			}
		}

		String teachers = set.getString("teachers", null);
		if(teachers == null)
			_teachers = null;
		else
		{
			_teachers = new ArrayList<Integer>();
			st = new StringTokenizer(teachers, " \r\n\t,;");
			while(st.hasMoreTokens())
			{
				String npcid = st.nextToken();
				_teachers.add(Integer.parseInt(npcid));
			}
		}

		hashCode = SkillUtils.generateSkillHashCode(_id, _level);

		_onAttackCancelChance = set.getDouble("on_attack_cancel_chance", 0.);
		_onCritCancelChance = set.getDouble("on_crit_cancel_chance", 0.);
	}

	public final boolean getWeaponDependancy(Creature activeChar)
	{
		if(_weaponsAllowed == 0)
			return true;

		if((activeChar.getBaseAttackType().mask() & _weaponsAllowed) != 0)
			return true;

		if(activeChar.getSecondaryWeaponInstance() != null && activeChar.getSecondaryWeaponTemplate() != null)
			if((activeChar.getSecondaryWeaponTemplate().getItemType().mask() & _weaponsAllowed) != 0)
				return true;

		activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));

		return false;
	}

	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		return checkCondition(activeChar, target, forceUse, dontMove, first, false);
	}

	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean trigger)
	{
		Player player = activeChar.getPlayer();

		if(activeChar.isDead())
			return false;

		if(target != null && activeChar.getReflection() != target.getReflection())
		{
			if(!trigger)
				activeChar.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
			return false;
		}

		if(!getWeaponDependancy(activeChar))
			return false;

		if(activeChar.isUnActiveSkill(_id))
			return false;

		if(first && activeChar.isSkillDisabled(this))
		{
			if(!trigger)
				activeChar.sendReuseMessage(this);
			if(activeChar.getTarget() != null && !activeChar.isAttackingNow())
			{
				if(activeChar.isPlayer())
				{
					activeChar.getPlayer().attackOnReuse(this, activeChar.getTarget());
				}	
			}	
			return false;
		}

		// DS: Clarity Ð½Ðµ Ð²Ð»Ð¸Ñ�ÐµÑ‚ Ð½Ð° mpConsume1 
		if(first && activeChar.getCurrentMp() < (isMagic() ? _mpConsume1 + activeChar.calcStat(Stats.MP_MAGIC_SKILL_CONSUME, _mpConsume2, target, this) : _mpConsume1 + activeChar.calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, _mpConsume2, target, this)))
		{
			if(!trigger)
				activeChar.sendPacket(Msg.NOT_ENOUGH_MP);
			return false;
		}

		if(activeChar.getCurrentHp() < _hpConsume + 1)
		{
			if(!trigger)
				activeChar.sendPacket(Msg.NOT_ENOUGH_HP);
			return false;
		}
		
		if(activeChar.isPlayer() && _vitConsume > 0)
		{
			Player p = (Player) activeChar;
			if(p.getVitality() < _vitConsume + 1)
			{
				if(!trigger)
					p.sendPacket(Msg.NOT_ENOUGH_MATERIALS);
				return false;
			}
		}
		else if(!activeChar.isPlayer() && _vitConsume > 0)
			return false;
		
		if(!(_isItemHandler || _isAltUse) && activeChar.isMuted(this))
			return false;

		if(_soulsConsume > activeChar.getConsumedSouls())
		{
			if(!trigger)
				activeChar.sendPacket(Msg.THERE_IS_NOT_ENOUGHT_SOUL);
			return false;
		}

		// TODO Ð¿ÐµÑ€ÐµÐ½ÐµÑ�Ñ‚Ð¸ Ð¿Ð¾Ñ‚Ñ€ÐµÐ±Ð»ÐµÐ½Ð¸Ðµ Ð¸Ð· Ñ„Ð¾Ñ€Ð¼ÑƒÐ» Ñ�ÑŽÐ´Ð°
		if(activeChar.getIncreasedForce() < _condCharges || activeChar.getIncreasedForce() < _numCharges)
		{
			if(!trigger)
				activeChar.sendPacket(Msg.YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY_);
			return false;
		}

		if(player != null)
		{
			if(player.isInFlyingTransform() && _isItemHandler && !flyingTransformUsage())
			{
				if(!trigger)
					player.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(getItemConsumeId()[0]));
				return false;
			}

			if(player.isInBoat())
			{
				// Ð�Ð° Ð²Ð¾Ð·Ð´ÑƒÑˆÐ½Ñ‹Ñ… ÐºÐ¾Ñ€Ð°Ð±Ð»Ñ�Ñ… Ð¼Ð¾Ð¶Ð½Ð¾ Ð¸Ñ�Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÑŒ Ñ�ÐºÐ¸Ð»Ñ‹-Ñ…Ñ�Ð½Ð´Ð»ÐµÑ€Ñ‹
				if(player.getBoat().isAirShip() && !_isItemHandler)
					return false;

				// Ð¡ Ð¼Ð¾Ñ€Ñ�ÐºÐ¸Ñ… ÐºÐ¾Ñ€Ð°Ð±Ð»ÐµÐ¹ Ð¼Ð¾Ð¶Ð½Ð¾ Ð»Ð¾Ð²Ð¸Ñ‚ÑŒ Ñ€Ñ‹Ð±Ñƒ
				if(player.getBoat().isVehicle() && !(this instanceof FishingSkill || this instanceof ReelingPumping))
					return false;
			}

			if(player.isInObserverMode())
			{
				if(!trigger)
					activeChar.sendPacket(Msg.OBSERVERS_CANNOT_PARTICIPATE);
				return false;
			}

			if(first && _itemConsume[0] > 0 && activeChar.canConsumeSkillItem(this))
			{
				for(int i = 0; i < _itemConsume.length; i++)
				{
					if(!activeChar.haveItemForConsume(_itemConsumeId[i], _itemConsume[i]))
					{
						if(activeChar == player && !trigger)
							player.sendPacket(isHandler() ? SystemMsg.INCORRECT_ITEM_COUNT : SystemMsg.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
						return false;
					}
				}
			}

			if(player.isFishing() && !isFishingSkill() && !altUse() && !activeChar.isServitor())
			{
				if(activeChar == player && !trigger)
					player.sendPacket(Msg.ONLY_FISHING_SKILLS_ARE_AVAILABLE);
				return false;
			}
		}

		// Warp (628) && Shadow Step (821) can be used while rooted
		if(getFlyType() != FlyType.NONE && getId() != 628 && getId() != 821 && (activeChar.isImmobilized() || activeChar.isRooted()))
		{
			if(activeChar.isPlayer() && !trigger)
				activeChar.sendPacket(Msg.YOUR_TARGET_IS_OUT_OF_RANGE);
			return false;
		}

		if(first && target != null && getFlyType() == FlyType.CHARGE)
		{
			if(activeChar.isInRange(target.getLoc(), Math.min(150, getFlyRadius())))
			{
				if(!trigger)
					activeChar.sendPacket(SystemMsg.THERE_IS_NOT_ENOUGH_SPACE_TO_MOVE_THE_SKILL_CANNOT_BE_USED);
				return false;
			}

			Location flyLoc = activeChar.getFlyLocation(target, this);
			if(flyLoc == null)
			{
				if(!trigger)
					activeChar.sendPacket(SystemMsg.THE_TARGET_IS_LOCATED_WHERE_YOU_CANNOT_CHARGE);
				return false;
			}
		}

		SystemMsg msg = checkTarget(activeChar, target, target, forceUse, first);
		if(msg != null && activeChar.getPlayer() != null)
		{
			if(!trigger)
				activeChar.sendPacket(msg);
			return false;
		}

		if(_preCondition.length == 0)
			return true;

		Env env = new Env();
		env.character = activeChar;
		env.skill = this;
		if(this != null && getId() == 1557)
			env.target = (Creature)activeChar.getServitor();
		else	
			env.target = target;

		if(first)
			for(Condition cond : _preCondition)
				if(!cond.test(env))
				{
					if(!trigger)
					{
						SystemMsg cond_msg = cond.getSystemMsg();
						if(cond_msg != null)
						{
							if(cond_msg.size() > 0)
								activeChar.sendPacket(new SystemMessagePacket(cond_msg).addSkillName(this));
							else
								activeChar.sendPacket(cond_msg);
						}
					}
					return false;
				}

		return true;
	}

	public SystemMsg checkTarget(Creature activeChar, Creature target, Creature aimingTarget, boolean forceUse, boolean first)
	{
		if(getId() == 1557)
		{
			if(activeChar.getServitor() == null)
				return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
			else if(!activeChar.getServitor().isSummon())
				return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
			else
				return null;
		}
		
		if(target == activeChar && isNotTargetAoE() || target == activeChar.getServitor() && _targetType == SkillTargetType.TARGET_SERVITOR_AURA)
			return null;
		if(target == null || isOffensive() && target == activeChar)
			return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
		if(isPvpSkill() && target.isPeaceNpc()) // TODO: [Bonux] Ð—Ð°Ð¿Ñ€ÐµÑ‚Ð¸Ñ‚ÑŒ ÑŽÐ·Ð°Ñ‚ÑŒ Ñ‚Ð¾Ð»ÑŒÐºÐ¾ Ð´ÐµÐ±Ð°Ñ„Ñ„ Ñ�ÐºÐ¸Ð»Ð»Ñ‹ (Ð¾Ñ„Ñ„Ð»Ð°Ð¹Ðº).
			return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
		if(activeChar.getReflection() != target.getReflection())
			return SystemMsg.CANNOT_SEE_TARGET;
		// ÐŸÐ¾Ð¿Ð°Ð´Ð°ÐµÑ‚ Ð»Ð¸ Ñ†ÐµÐ»ÑŒ Ð² Ñ€Ð°Ð´Ð¸ÑƒÑ� Ð´ÐµÐ¹Ñ�Ñ‚Ð²Ð¸Ñ� Ð² ÐºÐ¾Ð½Ñ†Ðµ ÐºÐ°Ñ�Ñ‚Ð°
		if(!first && target != activeChar && target == aimingTarget && getCastRange() > 0 && getCastRange() != 32767 && !activeChar.isInRange(target.getLoc(), getCastRange() + (getCastRange() < 200 ? 400 : 500)))
			return SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE;
		// Ð”Ð»Ñ� Ñ�Ñ‚Ð¸Ñ… Ñ�ÐºÐ¸Ð»Ð»Ð¾Ð² Ð´Ð°Ð»ÑŒÐ½ÐµÐ¹ÑˆÐ¸Ðµ Ð¿Ñ€Ð¾Ð²ÐµÑ€ÐºÐ¸ Ð½Ðµ Ð½ÑƒÐ¶Ð½Ñ‹
		if(_skillType == SkillType.TAKECASTLE || _skillType == SkillType.TAKEFORTRESS || _skillType == SkillType.TAKEFLAG)
			return null;
		// ÐšÐ¾Ð½ÑƒÑ�Ð¾Ð¾Ð±Ñ€Ð°Ð·Ð½Ñ‹Ðµ Ñ�ÐºÐ¸Ð»Ð»Ñ‹
		if(!first && target != activeChar && (_targetType == SkillTargetType.TARGET_MULTIFACE || _targetType == SkillTargetType.TARGET_MULTIFACE_AURA || _targetType == SkillTargetType.TARGET_TUNNEL) && (_isBehind ? PositionUtils.isFacing(activeChar, target, 120) : !PositionUtils.isFacing(activeChar,  target, 60)))
			return SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE;
		// ÐŸÑ€Ð¾Ð²ÐµÑ€ÐºÐ° Ð½Ð° ÐºÐ°Ñ�Ñ‚ Ð¿Ð¾ Ñ‚Ñ€ÑƒÐ¿Ñƒ
		if(target.isDead() != _isCorpse && _targetType != SkillTargetType.TARGET_AREA_AIM_CORPSE || _isUndeadOnly && !target.isUndead())
			return SystemMsg.INVALID_TARGET;
		// Ð”Ð»Ñ� Ñ€Ð°Ð·Ð»Ð¸Ñ‡Ð½Ñ‹Ñ… Ð±ÑƒÑ‚Ñ‹Ð»Ð¾Ðº, Ð¸ Ð´Ð»Ñ� Ñ�ÐºÐ¸Ð»Ð»Ð° ÐºÐ¾Ñ€Ð¼Ð»ÐµÐ½Ð¸Ñ�, Ð´Ð°Ð»ÑŒÐ½ÐµÐ¹ÑˆÐ¸Ðµ Ð¿Ñ€Ð¾Ð²ÐµÑ€ÐºÐ¸ Ð½Ðµ Ð½ÑƒÐ¶Ð½Ñ‹
		if(_isAltUse || _targetType == SkillTargetType.TARGET_FEEDABLE_BEAST || _targetType == SkillTargetType.TARGET_UNLOCKABLE || _targetType == SkillTargetType.TARGET_CHEST)
			return null;
		Player player = activeChar.getPlayer();
		if(player != null)
		{
			// Ð—Ð°Ð¿Ñ€ÐµÑ‚ Ð½Ð° Ð°Ñ‚Ð°ÐºÑƒ Ð¼Ð¸Ñ€Ð½Ñ‹Ñ… NPC Ð² Ð¾Ñ�Ð°Ð´Ð½Ð¾Ð¹ Ð·Ð¾Ð½Ðµ Ð½Ð° TW. Ð˜Ð½Ð°Ñ‡Ðµ Ñ‚Ð°ÐºÐ¸Ð¼ Ñ�Ð¿Ð¾Ñ�Ð¾Ð±Ð¾Ð¼ Ð½Ð°Ð±Ð¸Ð²Ð°ÑŽÑ‚ Ð¾Ñ‡ÐºÐ¸.
			//if(player.getTerritorySiege() > -1 && target.isNpc() && !(target instanceof L2TerritoryFlagInstance) && !(target.getAI() instanceof DefaultAI) && player.isInZone(ZoneType.Siege))
			//	return Msg.INVALID_TARGET;

			Player pcTarget = target.getPlayer();
			if(pcTarget != null)
			{
				if(isPvM())
					return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;

				if(player.isInZone(ZoneType.epic) != pcTarget.isInZone(ZoneType.epic))
					return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;

				if(pcTarget.isInOlympiadMode() && (!player.isInOlympiadMode() || player.getOlympiadGame() != pcTarget.getOlympiadGame())) // Ð�Ð° Ð²Ñ�Ñ�ÐºÐ¸Ð¹ Ñ�Ð»ÑƒÑ‡Ð°Ð¹
					return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;

				//TODO [VISTALL] Ñ‡Ñ‚Ð¾ Ð·Ð°?
				if(player.getTeam() != TeamType.NONE && pcTarget.getTeam() == TeamType.NONE) // Ð—Ð°Ð¿Ñ€ÐµÑ‚ Ð½Ð° Ð°Ñ‚Ð°ÐºÑƒ/Ð±Ð°Ñ„ ÑƒÑ‡Ð°Ñ�Ñ‚Ð½Ð¸ÐºÐ¾Ð¼ Ñ�Ð²ÐµÐ½Ñ‚Ð° Ð½ÐµÐ·Ð°Ñ€ÐµÐ³Ð¸Ñ�Ñ‚Ñ€Ð¸Ñ€Ð¾Ð²Ð°Ð½Ð½Ð¾Ð³Ð¾ Ð¸Ð³Ñ€Ð¾ÐºÐ°
					return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
				if(pcTarget.getTeam() != TeamType.NONE && player.getTeam() == TeamType.NONE) // Ð—Ð°Ð¿Ñ€ÐµÑ‚ Ð½Ð° Ð°Ñ‚Ð°ÐºÑƒ/Ð±Ð°Ñ„ ÑƒÑ‡Ð°Ñ�Ñ‚Ð½Ð¸ÐºÐ° Ñ�Ð²ÐµÐ½Ñ‚Ð° Ð½ÐµÐ·Ð°Ñ€ÐµÐ³Ð¸Ñ�Ñ‚Ñ€Ð¸Ñ€Ð¾Ð²Ð°Ð½Ð½Ñ‹Ð¼ Ð¸Ð³Ñ€Ð¾ÐºÐ¾Ð¼
					return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
				if(player.getBlockCheckerArena() > -1 && pcTarget.getBlockCheckerArena() > -1 && _targetType == SkillTargetType.TARGET_EVENT)
					return null;

				if(isOffensive())
				{
					if(player.isInOlympiadMode() && !player.isOlympiadCompStart()) // Ð‘Ð¾Ð¹ ÐµÑ‰Ðµ Ð½Ðµ Ð½Ð°Ñ‡Ð°Ð»Ñ�Ñ�
						return SystemMsg.INVALID_TARGET;
					if(player.isInOlympiadMode() && player.isOlympiadCompStart() && player.getOlympiadSide() == pcTarget.getOlympiadSide() && !forceUse) // Ð¡Ð²Ð¾ÑŽ ÐºÐ¾Ð¼Ð°Ð½Ð´Ñƒ Ð°Ñ‚Ð°ÐºÐ¾Ð²Ð°Ñ‚ÑŒ Ð½ÐµÐ»ÑŒÐ·Ñ�
						return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
					//TODO [VISTALL] Ñ‡Ñ‚Ð¾ Ð·Ð°?
					if(player.getTeam() != TeamType.NONE && pcTarget.getTeam() != TeamType.NONE && player.getTeam() == pcTarget.getTeam() && player != pcTarget && !player.isInLastHero() && !player.isInEventModelEvent()) // Ð¡Ð²Ð¾ÑŽ ÐºÐ¾Ð¼Ð°Ð½Ð´Ñƒ Ð°Ñ‚Ð°ÐºÐ¾Ð²Ð°Ñ‚ÑŒ Ð½ÐµÐ»ÑŒÐ·Ñ�
						return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
					if(pcTarget.isInNonPvpTime())
						return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
					if(player.isInHunterClub() && pcTarget.isInHunterClub())
						if(player.getEventTargetA() != pcTarget && player.getEventTargetB() != pcTarget)
							return SystemMsg.CANNOT_SEE_TARGET;					
					if(isAoE() && getCastRange() < Integer.MAX_VALUE && !GeoEngine.canSeeTarget(activeChar, target))
						return SystemMsg.CANNOT_SEE_TARGET;
					if(activeChar.isInZoneBattle() != target.isInZoneBattle() && !player.getPlayerAccess().PeaceAttack)
						return SystemMsg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE;
					if((activeChar.isInZonePeace() || target.isInZonePeace()) && !player.getPlayerAccess().PeaceAttack)
						return SystemMsg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE;

					if(activeChar.isInZoneBattle())
					{
						if(!forceUse && !isForceUse() && player.getParty() != null && player.getParty() == pcTarget.getParty())
							return SystemMsg.INVALID_TARGET;
						if(Config.ENABLE_NEW_PVP_SYSTEM && player.isInSpecialPvPZone() && pcTarget.getPlayer() != null)
						{
							int minLevel = player.getSpecialZoneMinLevel();
							int maxLevel = player.getSpecialZoneMaxLevel();
							if(player.getLevel() < minLevel || player.getLevel() > maxLevel || pcTarget.getLevel() < minLevel || pcTarget.getLevel() > maxLevel)
							{
								return SystemMsg.INVALID_TARGET;
							}
						}							
						return null; // ÐžÑ�Ñ‚Ð°Ð»ÑŒÐ½Ñ‹Ðµ ÑƒÑ�Ð»Ð¾Ð²Ð¸Ñ� Ð½Ð° Ð°Ñ€ÐµÐ½Ð°Ñ… Ð¸ Ð½Ð° Ð¾Ð»Ð¸Ð¼Ð¿Ð¸Ð°Ð´Ðµ Ð¿Ñ€Ð¾Ð²ÐµÑ€Ñ�Ñ‚ÑŒ Ð½Ðµ Ñ‚Ñ€ÐµÐ±ÑƒÐµÑ‚Ñ�Ñ�
					}

					// Ð¢Ð¾Ð»ÑŒÐºÐ¾ Ð²Ñ€Ð°Ð³ Ð¸ Ñ‚Ð¾Ð»ÑŒÐºÐ¾ ÐµÑ�Ð»Ð¸ Ð¾Ð½ ÐµÑˆÐµ Ð½Ðµ Ð¿Ñ€Ð¾Ð¸Ð³Ñ€Ð°Ð».
					/*Duel duel1 = player.getDuel();
					Duel duel2 = pcTarget.getDuel();
					if(player != pcTarget && duel1 != null && duel1 == duel2)
					{
						if(duel1.getTeamForPlayer(pcTarget) == duel1.getTeamForPlayer(player))
							return SystemMsg.INVALID_TARGET;
						if(duel1.getDuelState(player.getStoredId()) != Duel.DuelState.Fighting)
							return SystemMsg.INVALID_TARGET;
						if(duel1.getDuelState(pcTarget.getStoredId()) != Duel.DuelState.Fighting)
							return SystemMsg.INVALID_TARGET;
						return null;
					}  */

					SystemMsg msg = null;
					for(Event e : player.getEvents())
						if((msg = e.checkForAttack(target, activeChar, this, forceUse)) != null)
							return msg;

					for(Event e : player.getEvents())
						if(e.canAttack(target, activeChar, this, forceUse))
							return null;

					if(isProvoke())
					{
						if(!forceUse && player.getParty() != null && player.getParty() == pcTarget.getParty())
							return SystemMsg.INVALID_TARGET;
						return null;
					}

					if(isPvpSkill() || !forceUse || isAoE())
					{
						if(player.getServitor() != null)
							if(player.getServitor() == target && forceUse)
								return null;
							
						if(player == pcTarget)
							return SystemMsg.INVALID_TARGET;
						if(player.getParty() != null && player.getParty() == pcTarget.getParty())
							return SystemMsg.INVALID_TARGET;
						if(player.getClanId() != 0 && player.getClanId() == pcTarget.getClanId())
							return SystemMsg.INVALID_TARGET;
						if(player.getParty() != null && player.getParty().getCommandChannel() != null && pcTarget.getParty() != null && pcTarget.getParty().getCommandChannel() != null && player.getParty().getCommandChannel() == pcTarget.getParty().getCommandChannel())
							return SystemMsg.INVALID_TARGET;								
						/*if(player.getDuel() != null && pcTarget.getDuel() != player.getDuel())
							return SystemMsg.INVALID_TARGET;   */
						// DS: Ð°Ñ‚Ð°ÐºÑƒÑŽÑ‰Ð¸Ðµ Ñ�ÐºÐ¸Ð»Ð»Ñ‹ Ð½Ðµ Ð¸Ð³Ð½Ð¾Ñ€Ð¸Ñ€ÑƒÑŽÑ‚ ÐºÐ¾Ð¼Ð°Ð½Ð´Ð½Ñ‹Ð¹ ÐºÐ°Ð½Ð°Ð» Ð¸ Ð°Ð»ÑŒÑ�Ð½Ñ�
						/*if(player.isInParty() && player.getParty().getCommandChannel() != null && pcTarget.isInParty() && pcTarget.getParty().getCommandChannel() != null && player.getParty().getCommandChannel() == pcTarget.getParty().getCommandChannel())
							return SystemMsg.INVALID_TARGET;
						if(player.getClan() != null && player.getClan().getAlliance() != null && pcTarget.getClan() != null && pcTarget.getClan().getAlliance() != null && player.getClan().getAlliance() == pcTarget.getClan().getAlliance())
							return SystemMsg.INVALID_TARGET;*/
					}

					if(activeChar.isInZone(ZoneType.SIEGE) && target.isInZone(ZoneType.SIEGE))
						return null;
					if(activeChar.isInZonePvP() && target.isInZonePvP())
						return null;
					if(player.atMutualWarWith(pcTarget))
						return null;
					if(isForceUse())
						return null;
					// DS: Ð£Ð±Ñ€Ð°Ð½Ð¾. Ð—Ð°Ñ‰Ð¸Ñ‚Ð° Ð¾Ñ‚ Ñ€Ð°Ð·Ð²Ð¾Ð´Ð° Ð½Ð° Ñ„Ð»Ð°Ð³ Ñ� ÐºÐ¾Ð¿ÑŒÐµÐ¼
					/*if(!forceUse && player.getPvpFlag() == 0 && pcTarget.getPvpFlag() != 0 && aimingTarget != target)
						return SystemMsg.INVALID_TARGET;*/
					if(pcTarget.getPvpFlag() != 0)
						return null;
					if(pcTarget.getKarma() > 0)
						return null;
					if(forceUse && !isPvpSkill() && (!isAoE() || aimingTarget == target))
						return null;
					return SystemMsg.INVALID_TARGET;
				}

				if(pcTarget == player)
					return null;

				if(player.isInOlympiadMode() && !forceUse && player.getOlympiadSide() != pcTarget.getOlympiadSide()) // Ð§ÑƒÐ¶Ð¾Ð¹ ÐºÐ¾Ð¼Ð°Ð½Ð´Ðµ Ð¿Ð¾Ð¼Ð¾Ð³Ð°Ñ‚ÑŒ Ð½ÐµÐ»ÑŒÐ·Ñ�
					return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
				//TODO [VISTALL] Ñ‡Ñ‚Ð¾ Ð·Ð°?
				if(player.getTeam() != TeamType.NONE && pcTarget.getTeam() != TeamType.NONE && player.getTeam() != pcTarget.getTeam()) // Ð§ÑƒÐ¶Ð¾Ð¹ ÐºÐ¾Ð¼Ð°Ð½Ð´Ðµ Ð¿Ð¾Ð¼Ð¾Ð³Ð°Ñ‚ÑŒ Ð½ÐµÐ»ÑŒÐ·Ñ�
					return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;

				if(!activeChar.isInZoneBattle() && target.isInZoneBattle())
					return SystemMsg.INVALID_TARGET;
				// DS: Ð½Ð° Ð¾Ñ„Ñ„Ðµ Ð¼Ð¾Ð¶Ð½Ð¾ Ð¸Ñ�Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÑŒ Ð½ÐµÐ°Ñ‚Ð°ÐºÑƒÑŽÑ‰Ð¸Ðµ Ñ�ÐºÐ¸Ð»Ð»Ñ‹ Ð¸Ð· Ð¼Ð¸Ñ€Ð½Ð¾Ð¹ Ð·Ð¾Ð½Ñ‹ Ð² Ð¿Ð¾Ð»Ðµ.
				/*if(activeChar.isInZonePeace() && !target.isInZonePeace())
					return SystemMsg.INVALID_TARGET;*/

				if(forceUse || isForceUse())
					return null;

				/*if(player.getDuel() != null && pcTarget.getDuel() != player.getDuel())
					return SystemMsg.INVALID_TARGET;
				if(player != pcTarget && player.getDuel() != null && pcTarget.getDuel() != null && pcTarget.getDuel() == pcTarget.getDuel())
					return SystemMsg.INVALID_TARGET;         */

				if(player.getParty() != null && player.getParty() == pcTarget.getParty())
					return null;
				if(player.getClanId() != 0 && player.getClanId() == pcTarget.getClanId())
					return null;

				if(player.atMutualWarWith(pcTarget))
					return SystemMsg.INVALID_TARGET;
				if(pcTarget.getPvpFlag() != 0)
					return SystemMsg.INVALID_TARGET;
				if(pcTarget.getKarma() > 0)
					return SystemMsg.INVALID_TARGET;

				return null;
			}
		}

		if(isAoE() && isOffensive() && getCastRange() < Integer.MAX_VALUE && !GeoEngine.canSeeTarget(activeChar, target))
			return SystemMsg.CANNOT_SEE_TARGET;
		if(!forceUse && !isForceUse() && !isOffensive() && target.isAutoAttackable(activeChar))
			return SystemMsg.INVALID_TARGET;
		if(!forceUse && !isForceUse() && isOffensive() && !target.isAutoAttackable(activeChar))
			return SystemMsg.INVALID_TARGET;
		if(!target.isAttackable(activeChar))
			return SystemMsg.INVALID_TARGET;

		return null;
	}

	public final Creature getAimingTarget(Creature activeChar, GameObject obj)
	{
		Creature target = obj == null || !obj.isCreature() ? null : (Creature) obj;
		switch(_targetType)
		{
			case TARGET_ALLY:
			case TARGET_CLAN:
			case TARGET_PARTY:
			case TARGET_CLAN_ONLY:
			case TARGET_FRIEND:
			case TARGET_SELF:
				return activeChar;
			case TARGET_AURA:
			case TARGET_COMMCHANNEL:
			case TARGET_MULTIFACE_AURA:
				return activeChar;
			case TARGET_HOLY:
				return target != null && activeChar.isPlayer() && target.isArtefact() ? target : null;
			case TARGET_FLAGPOLE:
				return activeChar;
			case TARGET_UNLOCKABLE:
				return target != null && target.isDoor() || target instanceof TreasureChestInstance ? target : null;
			case TARGET_CHEST:
				return target instanceof TreasureChestInstance ? target : null;
			case TARGET_FEEDABLE_BEAST:
				return target instanceof FeedableBeastInstance ? target : null;
			case TARGET_SERVITOR:
			case TARGET_SERVITOR_AURA:
				target = activeChar.getServitor();
				return target != null && target.isDead() == _isCorpse ? target : null;
			case TARGET_SUMMON:
				target = activeChar.getServitor();
				return target != null && target.isSummon() && target.isDead() == _isCorpse ? target : null;
			case TARGET_PET:
				target = activeChar.getServitor();
				return target != null && target.isPet() && target.isDead() == _isCorpse ? target : null;
			case TARGET_OWNER:
				if(activeChar.isServitor())
					target = activeChar.getPlayer();
				else
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return null;
				}
				return target != null && target.isDead() == _isCorpse ? target : null;
			case TARGET_ENEMY_PET:
				if(target == null || target == activeChar.getServitor() || !target.isPet())
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return null;
				}
				return target;
			case TARGET_ENEMY_SUMMON:
				if(target == null || target == activeChar.getServitor() || !target.isSummon())
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return null;
				}
				return target;
			case TARGET_ENEMY_SERVITOR:
				if(target == null || target == activeChar.getServitor() || !target.isServitor())
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return null;
				}
				return target;
			case TARGET_EVENT:
				return target != null && !target.isDead() && target.getPlayer().getBlockCheckerArena() > -1 ? target : null;
			case TARGET_ONE:
				return target != null && target.isDead() == _isCorpse && !(target == activeChar && isOffensive()) && (!_isUndeadOnly || target.isUndead()) ? target : null;
			case TARGET_PARTY_ONE:
				if(target == null)
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return null;
				}
				Player player = activeChar.getPlayer();
				Player ptarget = target.getPlayer();
				// self or self pet.
				if(ptarget != null && ptarget == activeChar)
					return target;
				// olympiad party member or olympiad party member pet.
				if(player != null && player.isInOlympiadMode() && ptarget != null && player.getOlympiadSide() == ptarget.getOlympiadSide() && player.getOlympiadGame() == ptarget.getOlympiadGame() && target.isDead() == _isCorpse && !(target == activeChar && isOffensive()) && (!_isUndeadOnly || target.isUndead()))
					return target;
				// party member or party member pet.
				if(ptarget != null && player != null && player.getParty() != null && player.getParty().containsMember(ptarget) && target.isDead() == _isCorpse && !(target == activeChar && isOffensive()) && (!_isUndeadOnly || target.isUndead()))
					return target;
				return null;
			case TARGET_AREA:
			case TARGET_MULTIFACE:
			case TARGET_TUNNEL:
				return target != null && target.isDead() == _isCorpse && !(target == activeChar && isOffensive()) && (!_isUndeadOnly || target.isUndead()) ? target : null;
			case TARGET_AREA_AIM_CORPSE:
				return target != null && target.isDead() ? target : null;
			case TARGET_CORPSE:
				if (target == null || !target.isDead())
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return null;
				}
				if (target.isSummon() && target != activeChar.getServitor()) // use their own dead Samon not
					return target;
				return target.isNpc() ? target : null;
			case TARGET_CORPSE_PLAYER:
				return target != null && target.isPlayable() && target.isDead() ? target : null;
			case TARGET_SIEGE:
				return target != null && !target.isDead() && target.isDoor() ? target : null;
			default:
				activeChar.sendMessage("Target type of skill is not currently handled");
				return null;
		}
	}

	public List<Creature> getTargets(Creature activeChar, Creature aimingTarget, boolean forceUse)
	{
		List<Creature> targets;
		if(oneTarget())
		{
			targets = new LazyArrayList<Creature>(1);
			targets.add(aimingTarget);
			return targets;
		}
		else
			targets = new LazyArrayList<Creature>();

		switch(_targetType)
		{
			case TARGET_EVENT:
			{
				if(activeChar.isPlayer())
				{
					Player player = activeChar.getPlayer();
					int playerArena = player.getBlockCheckerArena();

					if(playerArena != -1)
					{
						ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(playerArena);
						int team = holder.getPlayerTeam(player);
						// Aura attack
						for(Player actor : World.getAroundPlayers(activeChar, 250, 100))
							if(holder.getAllPlayers().contains(actor) && holder.getPlayerTeam(actor) != team)
								targets.add(actor);
					}
				}
				break;
			}
			case TARGET_AREA_AIM_CORPSE:
			case TARGET_AREA:
			case TARGET_MULTIFACE:
			case TARGET_TUNNEL:
			{
				if(aimingTarget.isDead() == _isCorpse && (!_isUndeadOnly || aimingTarget.isUndead()))
					targets.add(aimingTarget);
				addTargetsToList(targets, aimingTarget, activeChar, forceUse);
				break;
			}
			case TARGET_AURA:
			case TARGET_MULTIFACE_AURA:
			{
				addTargetsToList(targets, activeChar, activeChar, forceUse);
				break;
			}
			case TARGET_COMMCHANNEL:
			{
				if(activeChar.getPlayer() != null)
				{
					if(activeChar.getPlayer().isInParty())
					{
						if(activeChar.getPlayer().getParty().isInCommandChannel())
						{
							for(Player p : activeChar.getPlayer().getParty().getCommandChannel())
								if(!p.isDead() && p.isInRange(activeChar, _skillRadius == 0 ? 600 : _skillRadius))
									targets.add(p);
							addTargetAndPetToList(targets, activeChar.getPlayer(), activeChar.getPlayer());
							break;
						}
						for(Player p : activeChar.getPlayer().getParty().getPartyMembers())
							if(!p.isDead() && p.isInRange(activeChar, _skillRadius == 0 ? 600 : _skillRadius))
								targets.add(p);
						addTargetAndPetToList(targets, activeChar.getPlayer(), activeChar.getPlayer());
						break;
					}
					targets.add(activeChar);
					addTargetAndPetToList(targets, activeChar.getPlayer(), activeChar.getPlayer());
				}
				break;
			}
			case TARGET_SERVITOR_AURA:
			{
				if(activeChar.getServitor() == null)
					break;
				addTargetsToList(targets, activeChar.getServitor(), activeChar, forceUse);
				break;
			}
			case TARGET_PARTY:
			case TARGET_CLAN:
			case TARGET_CLAN_ONLY:
			case TARGET_ALLY:
			{
				if(activeChar.isMonster() || activeChar.isSiegeGuard())
				{
					targets.add(activeChar);
					for(Creature c : World.getAroundCharacters(activeChar, _skillRadius, 600))
						if(!c.isDead() && (c.isMonster() || c.isSiegeGuard()) /*&& ((L2MonsterInstance) c).getFactionId().equals(mob.getFactionId())*/)
							targets.add(c);
					break;
				}
				Player player = activeChar.getPlayer();
				if(player == null)
					break;
				for(Player target : World.getAroundPlayers(player, _skillRadius, 600))
				{
					boolean check = false;
					switch(_targetType)
					{
						case TARGET_PARTY:
							check = player.getParty() != null && player.getParty() == target.getParty();
							break;
						case TARGET_CLAN:
							check = player.getClanId() != 0 && target.getClanId() == player.getClanId() || player.getParty() != null && target.getParty() == player.getParty();
							break;
						case TARGET_CLAN_ONLY:
							check = player.getClanId() != 0 && target.getClanId() == player.getClanId();
							break;
						case TARGET_ALLY:
							check = player.getClanId() != 0 && target.getClanId() == player.getClanId() || player.getAllyId() != 0 && target.getAllyId() == player.getAllyId();
							break;
					}
					if(!check)
						continue;
					// Ð¸Ð³Ð½Ð¾Ñ€Ð¸Ñ€ÑƒÐµÐ¼ Ð¿Ñ€Ð¾Ñ‚Ð¸Ð²Ð½Ð¸ÐºÐ° Ð½Ð° Ð¾Ð»Ð¸Ð¼Ð¿Ð¸Ð°Ð´Ðµ
					if (player.isInOlympiadMode() && target.isInOlympiadMode() && player.getOlympiadSide() != target.getOlympiadSide())
						continue;
					if(checkTarget(player, target, aimingTarget, forceUse, false) != null)
						continue;
					addTargetAndPetToList(targets, player, target);
				}
				addTargetAndPetToList(targets, player, player);
				break;
			}
			case TARGET_FRIEND:
			{
				if(activeChar.isMonster() || activeChar.isSiegeGuard())
				{
					targets.add(activeChar);
					for(Creature c : World.getAroundCharacters(activeChar, _skillRadius, 900))
					{
						if(!c.isDead() && (c.isMonster() || c.isSiegeGuard()))
							targets.add(c);
					}
					break;
				}
				Player player = activeChar.getPlayer();
				for(Player target : World.getAroundPlayers(player, _skillRadius, 350))
				{
					boolean check = false;
					if(check = player.getParty() != null && (player.getParty() == target.getParty() 
						|| player.getClanId() != 0 && target.getClanId() == player.getClanId() 
						|| player.getAllyId() != 0 && target.getAllyId() == player.getAllyId()))

					if(!check)
						continue;
					if(checkTarget(player, target, aimingTarget, forceUse, false) != null)
						continue;
					addTargetAndPetToList(targets, player, target);
				}
				addTargetAndPetToList(targets, player, player);
				break;
			}
		default:
			break;
		}
		return targets;
	}

	private void addTargetAndPetToList(List<Creature> targets, Player actor, Player target)
	{
		if((actor == target || actor.isInRange(target, _skillRadius)) && target.isDead() == _isCorpse)
			targets.add(target);
		Servitor pet = target.getServitor();
		if(pet != null && actor.isInRange(pet, _skillRadius) && pet.isDead() == _isCorpse)
			targets.add(pet);
	}

	private void addTargetsToList(List<Creature> targets, Creature aimingTarget, Creature activeChar, boolean forceUse)
	{
		int count = 0;
		Polygon terr = null;
		if(_targetType == SkillTargetType.TARGET_TUNNEL)
		{
			// Ð¡Ð¾Ð·Ð´Ð°ÐµÐ¼ Ð¿Ð°Ñ€Ð°Ð»Ð»ÐµÐ»ÐµÐ¿Ð¸Ð¿ÐµÐ´ ("ÐºÐ¾Ñ�Ð¾Ð¹" Ð¿Ð¾ Ð²ÐµÑ€Ñ‚Ð¸ÐºÐ°Ð»Ð¸)

			int radius = 100;
			int zmin1 = activeChar.getZ() - 200;
			int zmax1 = activeChar.getZ() + 200;
			int zmin2 = aimingTarget.getZ() - 200;
			int zmax2 = aimingTarget.getZ() + 200;

			double angle = PositionUtils.convertHeadingToDegree(activeChar.getHeading());
			double radian1 = Math.toRadians(angle - 90);
			double radian2 = Math.toRadians(angle + 90);

			terr = new Polygon().add(activeChar.getX() + (int) (Math.cos(radian1) * radius), activeChar.getY() + (int) (Math.sin(radian1) * radius)).add(activeChar.getX() + (int) (Math.cos(radian2) * radius), activeChar.getY() + (int) (Math.sin(radian2) * radius)).add(aimingTarget.getX() + (int) (Math.cos(radian2) * radius), aimingTarget.getY() + (int) (Math.sin(radian2) * radius)).add(aimingTarget.getX() + (int) (Math.cos(radian1) * radius), aimingTarget.getY() + (int) (Math.sin(radian1) * radius)).setZmin(Math.min(zmin1, zmin2)).setZmax(Math.max(zmax1, zmax2));
		}
		for(Creature target : aimingTarget.getAroundCharacters(_skillRadius, 300))
		{
			if(terr != null && !terr.isInside(target.getX(), target.getY(), target.getZ()))
				continue;
			if(target == null || activeChar == target || activeChar.getPlayer() != null && activeChar.getPlayer() == target.getPlayer())
				continue;
			//FIXME [G1ta0] Ñ‚ÑƒÐ¿Ð¾Ð¹ Ñ…Ð°Ðº
			if(getId() == SKILL_DETECTION)
				target.checkAndRemoveInvisible();
			if(checkTarget(activeChar, target, aimingTarget, forceUse, false) != null)
				continue;
			/*if(!(activeChar instanceof DecoyInstance) && activeChar.isNpc() && target.isNpc())
				continue;*/
			targets.add(target);
			count++;
			if(isOffensive() && count >= 20 && !activeChar.isRaid())
				break;
		}
	}

	public final void getEffects(Creature effector, Creature effected, boolean calcChance, boolean applyOnCaster)
	{
		getEffects(effector, effected, calcChance, applyOnCaster, false);
	}

	public final void getEffects(Creature effector, Creature effected, boolean calcChance, boolean applyOnCaster, boolean skillReflected)
	{
		double timeMult = 1.0;

		if(!isOffensive())
		{
			if(isMusic())
				timeMult = Config.SONGDANCETIME_MODIFIER;
			else if(getId() >= 4342 && getId() <= 4360)
				timeMult = Config.CLANHALL_BUFFTIME_MODIFIER;
			else
				timeMult = Config.BUFFTIME_MODIFIER;
		}

		getEffects(effector, effected, calcChance, applyOnCaster, 0, timeMult, skillReflected);
	}

	/**
	 * ÐŸÑ€Ð¸Ð¼ÐµÐ½Ð¸Ñ‚ÑŒ Ñ�Ñ„Ñ„ÐµÐºÑ‚Ñ‹ Ñ�ÐºÐ¸Ð»Ð»Ð°
	 * 
	 * @param effector Ð¿ÐµÑ€Ñ�Ð¾Ð½Ð°Ð¶, Ñ�Ð¾ Ñ�Ñ‚Ð¾Ñ€Ð¾Ð½Ñ‹ ÐºÐ¾Ñ‚Ð¾Ñ€Ð¾Ð³Ð¾ Ð¸Ð´ÐµÑ‚ Ð´ÐµÐ¹Ñ�Ñ‚Ð²Ð¸Ðµ Ñ�ÐºÐ¸Ð»Ð»Ð°, ÐºÐ°Ñ�Ñ‚ÑƒÑŽÑ‰Ð¸Ð¹
	 * @param effected Ð¿ÐµÑ€Ñ�Ð¾Ð½Ð°Ð¶, Ð½Ð° ÐºÐ¾Ñ‚Ð¾Ñ€Ð¾Ð³Ð¾ Ð´ÐµÐ¹Ñ�Ñ‚Ð²ÑƒÐµÑ‚ Ñ�ÐºÐ¸Ð»Ð»
	 * @param calcChance ÐµÑ�Ð»Ð¸ true, Ñ‚Ð¾ Ñ€Ð°Ñ�Ñ‡Ð¸Ñ‚Ñ‹Ð²Ð°Ñ‚ÑŒ ÑˆÐ°Ð½Ñ� Ð½Ð°Ð»Ð¾Ð¶ÐµÐ½Ð¸Ñ� Ñ�Ñ„Ñ„ÐµÐºÑ‚Ð°
	 * @param applyOnCaster ÐµÑ�Ð»Ð¸ true, Ð½Ð°ÐºÐ»Ð°Ð´Ñ‹Ð²Ð°Ñ‚ÑŒ Ñ‚Ð¾Ð»ÑŒÐºÐ¾ Ñ�Ñ„Ñ„ÐµÐºÑ‚Ñ‹ Ð¿Ñ€ÐµÐ´Ð½Ð°Ð·Ð°Ð½Ñ‡ÐµÐ½Ð½Ñ‹Ðµ Ð´Ð»Ñ� ÐºÐ°Ñ�Ñ‚ÑƒÑŽÑ‰ÐµÐ³Ð¾  
	 * @param timeConst Ð¸Ð·Ð¼ÐµÐ½Ð¸Ñ‚ÑŒ Ð²Ñ€ÐµÐ¼Ñ� Ð´ÐµÐ¹Ñ�Ñ‚Ð²Ð¸Ñ� Ñ�Ñ„Ñ„ÐµÐºÑ‚Ð¾Ð² Ð´Ð¾ Ð´Ð°Ð½Ð½Ð¾Ð¹ ÐºÐ¾Ð½Ñ�Ñ‚Ð°Ð½Ñ‚Ñ‹ (Ð² Ð¼Ð¸Ð»Ð»Ð¸Ñ�ÐµÐºÑƒÐ½Ð´Ð°Ñ…)
	 * @param timeMult Ð¸Ð·Ð¼ÐµÐ½Ð¸Ñ‚ÑŒ Ð²Ñ€ÐµÐ¼Ñ� Ð´ÐµÐ¹Ñ�Ñ‚Ð²Ð¸Ñ� Ñ�Ñ„Ñ„ÐµÐºÑ‚Ð¾Ð² Ñ� ÑƒÑ‡ÐµÑ‚Ð¾Ð¼ Ð´Ð°Ð½Ð½Ð¾Ð³Ð¾ Ð¼Ð½Ð¾Ð¶Ð¸Ñ‚ÐµÐ»Ñ�
	 * @param skillReflected Ð¾Ð·Ð½Ð°Ñ‡Ð°ÐµÑ‚ Ñ‡Ñ‚Ð¾ Ñ�ÐºÐ¸Ð»Ð» Ð±Ñ‹Ð» Ð¾Ñ‚Ñ€Ð°Ð¶ÐµÐ½ Ð¸ Ñ�Ñ„Ñ„ÐµÐºÑ‚Ñ‹ Ñ‚Ð¾Ð¶Ðµ Ð½ÑƒÐ¶Ð½Ð¾ Ð¾Ñ‚Ñ€Ð°Ð·Ð¸Ñ‚ÑŒ
	 */
	public final void getEffects(final Creature effector, final Creature effected, final boolean calcChance, final boolean applyOnCaster, final long timeConst, final double timeMult, final boolean skillReflected)
	{
		if(isPassive() || !hasEffects() || effector == null || effected == null)
			return;

		if((effected.isEffectImmune() || effected.isInvul() && isOffensive()) && effector != effected)
		{
			if(effector.isPlayer())
			{
				effector.sendPacket(new SystemMessage(SystemMessage.C1_HAS_RESISTED_YOUR_S2).addName(effected).addSkillName(this));
				effector.sendPacket(new ExMagicAttackInfo(effector.getObjectId(), effected.getObjectId(), ExMagicAttackInfo.RESISTED));
			}
			return;
		}

		if(effected.isDoor() || effected.isAlikeDead() && !isPreservedOnDeath())
			return;
			
		final int sps = effector.getChargedSpiritShot();
		Formulas.calcSkillMastery(this, effector); // Calculate skill mastery for current cast
		
		ThreadPoolManager.getInstance().execute(new RunnableImpl()
		{
			@Override
			public void runImpl()
			{
				boolean calcBase = false;
				boolean success = false;
				boolean reflected = false;
				boolean skillMastery = false;

				// Check for skill mastery duration time increase
				
				if(effector.getSkillMastery(getId()) == 2)
				{
					skillMastery = true;
					effector.removeSkillMastery(getId());
				}

				for(EffectTemplate et : getEffectTemplates())
				{
					if(applyOnCaster != et._applyOnCaster || et._count == 0)
						continue;

					// ÐšÐ°Ñ�Ñ‚ÐµÑ€ Ð² ÐºÐ°Ñ‡ÐµÑ�Ñ‚Ð²Ðµ Ñ†ÐµÐ»Ð¸ Ñ‚Ð°ÐºÐ¶Ðµ ÐµÑ�Ð»Ð¸ Ñ�ÐºÐ¸Ð»Ð» Ð±Ñ‹Ð» Ð¾Ñ‚Ñ€Ð°Ð¶ÐµÐ½ Ð¸ Ñ�Ñ„Ñ„ÐµÐºÑ‚ Ð¾Ñ‚Ñ€Ð°Ð¶Ð°Ð±ÐµÐ»ÐµÐ½
					Creature character = et._applyOnCaster || (et._isReflectable && skillReflected) ? effector : effected;
					List<Creature> targets = new LazyArrayList<Creature>(1);
					targets.add(character);

					if(et._applyOnSummon && character.isPlayer())
					{
						Servitor servitor = character.getPlayer().getServitor();
						if(servitor != null && servitor.isSummon() && !isOffensive() && !isToggle() && !isCubicSkill())
							targets.add(servitor);
					}

					loop:
					for(Creature target : targets)
					{
						if(target.isDead() && !isPreservedOnDeath())
							continue;

						if(target.isRaid() && et.getEffectType().isRaidImmune())
						{
							//effector.sendPacket(new SystemMessage(SystemMsg.C1_HAS_RESISTED_YOUR_S2).addString(effected.getName()).addSkillName(this));
							//effector.sendPacket(new ExMagicAttackInfo(effector.getObjectId(), effected.getObjectId(), ExMagicAttackInfo.RESISTED));
							continue;
						}

						if(!et._applyOnCaster && et.getPeriod() > 0 && (isOffensive() ? target.isDebuffImmune() : target.isBuffImmune()))
						{
							//effector.sendPacket(new SystemMessage(SystemMsg.C1_WEAKLY_RESISTED_C2S_MAGIC).addName(effected).addName(effector));
							continue;
						}

						if(isBlockedByChar(target, et))
							continue;

						if(et._stackOrder == -1)
							if(!et._stackType.equals(EffectTemplate.NO_STACK))
							{
								for(Effect e : target.getEffectList().getAllEffects())
									if(e.getStackType().equalsIgnoreCase(et._stackType))
										continue loop;
							}
							else if(target.getEffectList().getEffectsBySkillId(getId()) != null)
								continue;

						Env env = new Env(effector, target, Skill.this);

						if(_isReflectable && et._isReflectable && isOffensive() && target != effector && !effector.isTrap())
							if(reflected || Rnd.chance(target.calcStat(isMagic() ? Stats.REFLECT_MAGIC_DEBUFF : Stats.REFLECT_PHYSIC_DEBUFF, 0, effector, Skill.this)))
							{
								if(!reflected)
								{
									target.sendPacket(new SystemMessage(SystemMsg.YOU_COUNTERED_C1S_ATTACK).addName(effector));
									effector.sendPacket(new SystemMessage(SystemMsg.C1_DODGES_THE_ATTACK).addName(target));
								}
								reflected = true;
								if(effector.isDebuffImmune())
									continue;
								target = effector;
								env.target = target;
							}

						int chance = et.getChance();
						if(calcChance && !et._applyOnCaster)
						{
							if(calcBase && !success) // Ð½Ðµ Ð¿Ñ€Ð¾ÑˆÐ»Ð¾ Ñ€Ð°Ð½ÑŒÑˆÐµ, Ð¿Ñ€Ð¾Ð¿ÑƒÑ�ÐºÐ°ÐµÐ¼ Ð²Ñ�Ðµ Ð¾Ñ�Ñ‚Ð°Ð»ÑŒÐ½Ð¾Ðµ
								continue;
							if(chance > 0) // Ñ�Ñ„Ñ„ÐµÐºÑ‚ Ñ� Ð¸Ð½Ð´Ð¸Ð²Ð¸Ð´ÑƒÐ°Ð»ÑŒÐ½Ñ‹Ð¼ ÑˆÐ°Ð½Ñ�Ð¾Ð¼
							{
								env.value = chance;
								if(!Formulas.calcSkillSuccess(env, et, sps))
									continue;
							}
							else if(!calcBase) // ÐµÑ‰Ðµ Ð½Ðµ Ñ�Ñ‡Ð¸Ñ‚Ð°Ð»Ð¾Ñ�ÑŒ
							{
								calcBase = true;
								chance = getActivateRate(); // Ñ�Ñ‡Ð¸Ñ‚Ð°ÐµÑ‚Ñ�Ñ� Ð¾Ð´Ð¸Ð½ Ñ€Ð°Ð· Ð´Ð»Ñ� Ð²Ñ�ÐµÑ… Ð±Ð°Ð·Ð¾Ð²Ñ‹Ñ… Ñ�Ñ„Ñ„ÐµÐºÑ‚Ð¾Ð² Ð±ÐµÐ· Ð¸Ð½Ð´Ð¸Ð²Ð¸Ð´ÑƒÐ°Ð»ÑŒÐ½Ñ‹Ñ… ÑˆÐ°Ð½Ñ�Ð¾Ð²
								if(chance > 0)
								{
									env.value = chance;
									if(!Formulas.calcSkillSuccess(env, et, sps)) // Ð½Ðµ Ð¿Ñ€Ð¾ÑˆÐµÐ» Ð±Ð°Ð·Ð¾Ð²Ñ‹Ð¹ Ñ�Ñ„Ñ„ÐµÐºÑ‚
										continue;
								}
							}
							success = true;
						}

						final Effect e = et.getEffect(env);
						if(e != null)
						{
							if(chance > 0)
								success = true;
							if(!e.isOneTime())
							{
								int count = et.getCount();
								long period = et.getPeriod();

								// Check for skill mastery duration time increase
								if(skillMastery)
									if(count > 1)
										count *= 2;
									else
										period *= 2;

								// Ð¡Ñ‡Ð¸Ñ‚Ð°ÐµÐ¼ Ð²Ð»Ð¸Ñ�Ð½Ð¸Ðµ Ñ€ÐµÐ·Ð¸Ñ�Ñ‚Ð¾Ð²
								if(!et._applyOnCaster && isOffensive() && !isIgnoreResists() && !effector.isRaid())
								{
									double res = 0;
									if(et.getEffectType().getResistType() != null)
										res += effected.calcStat(et.getEffectType().getResistType(), effector, Skill.this);
									if(et.getEffectType().getAttributeType() != null)
										res -= effector.calcStat(et.getEffectType().getAttributeType(), effected, Skill.this);

									res += effected.calcStat(Stats.DEBUFF_RESIST, effector, Skill.this);

									if(res != 0)
									{
										double mod = 1 + Math.abs(0.01 * res);
										if(res > 0)
											mod = 1. / mod;

										if(count > 1)
											count = (int) Math.floor(Math.max(count * mod, 1));
										else
											period = (long) Math.floor(Math.max(period * mod, 1));
									}
								}

								if(timeConst > 0L)
								{
									if(count > 1)
										period = timeConst / count;
									else
										period = timeConst;
								}
								else if(timeMult > 1.0)
								{
									if(count > 1)
										count *= timeMult;
									else
										period *= timeMult;
								}

								e.setCount(count);
								e.setPeriod(period);
								e.schedule();
							}
						}
					}
				}
				if(calcChance)
					if(success)
						effector.sendPacket(new SystemMessage(SystemMessage.S1_HAS_SUCCEEDED).addSkillName(Skill.this));
					else
					{
						effector.sendPacket(new SystemMessage(SystemMessage.C1_HAS_RESISTED_YOUR_S2).addSkillName(Skill.this));
						effector.sendPacket(new ExMagicAttackInfo(effector.getObjectId(), effected.getObjectId(), ExMagicAttackInfo.RESISTED));
					}
			}
		});
	}

	public final void attach(EffectTemplate effect)
	{
		if(effect.getPeriod() != 0)
			_effectTemplates = ArrayUtils.add(_effectTemplates, effect);
		else
			_iEffectTemplates = ArrayUtils.add(_iEffectTemplates, effect);
	}

	public EffectTemplate[] getEffectTemplates()
	{
		return _effectTemplates;
	}

	public EffectTemplate[] getIEffectTemplates()
	{
		return _iEffectTemplates;
	}

	public boolean hasEffects()
	{
		return _effectTemplates.length > 0;
	}

	public boolean hasIEffects()
	{
		return _iEffectTemplates.length > 0;
	}

	public boolean hasEffect(EffectType type)
	{
		for(EffectTemplate et : _effectTemplates)
		{
			if(et.getEffectType() == type)
				return true;
		}
		return false;
	}

	public boolean hasIEffect(EffectType type)
	{
		for(EffectTemplate et : _iEffectTemplates)
		{
			if(et.getEffectType() == type)
				return true;
		}
		return false;
	}

	public final Func[] getStatFuncs()
	{
		return getStatFuncs(this);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;

		return hashCode() == ((Skill) obj).hashCode();
	}

	public int getReuseSkillId()
	{
		return _reuseSkillId;
	}

	public int getReuseHash()
	{
		return _reuseHash;
	}

	@Override
	public int hashCode()
	{
		return hashCode;
	}

	public final void attach(Condition c)
	{
		_preCondition = ArrayUtils.add(_preCondition, c);
	}

	public final boolean altUse()
	{
		return _isAltUse;
	}

	public final boolean canTeachBy(int npcId)
	{
		return _teachers == null || _teachers.contains(npcId);
	}

	public final int getActivateRate()
	{
		return _activateRate;
	}

	public AddedSkill[] getAddedSkills()
	{
		return _addedSkills;
	}

	public final boolean getCanLearn(ClassId cls)
	{
		return _canLearn == null || _canLearn.contains(cls);
	}

	/**
	 * @return Returns the castRange.
	 */
	public final int getCastRange()
	{
		return _castRange;
	}

	public final int getAOECastRange()
	{
		return Math.max(_castRange, _skillRadius);
	}

	public int getCondCharges()
	{
		return _condCharges;
	}

	public final int getCoolTime()
	{
		return _coolTime;
	}

	public boolean getCorpse()
	{
		return _isCorpse;
	}

	public int getDelayedEffect()
	{
		return _delayedEffect;
	}

	public final int getDisplayId()
	{
		return _displayId;
	}

	public int getDisplayLevel()
	{
		return _displayLevel;
	}

	public int getDisplaySubLevel()
	{
		return SkillUtils.getSkillEnchantType(_displayLevel) * 1000 + SkillUtils.getSkillEnchantLevel(_displayLevel);
	}

	public int getEffectPoint()
	{
		return _effectPoint;
	}

	public Effect getSameByStackType(List<Effect> list)
	{
		Effect ret;
		for(EffectTemplate et : getEffectTemplates())
			if(et != null && (ret = et.getSameByStackType(list)) != null)
				return ret;
		return null;
	}

	public Effect getSameByStackType(EffectList list)
	{
		return getSameByStackType(list.getAllEffects());
	}

	public Effect getSameByStackType(Creature actor)
	{
		return getSameByStackType(actor.getEffectList().getAllEffects());
	}

	public final Element getElement()
	{
		return _element;
	}

	public final int getElementPower()
	{
		return _elementPower;
	}

	public Skill getFirstAddedSkill()
	{
		if(_addedSkills.length == 0)
			return null;
		return _addedSkills[0].getSkill();
	}

	public int getFlyRadius()
	{
		return _flyRadius;
	}

	public FlyType getFlyType()
	{
		return _flyType;
	}

	public boolean isFlyToBack()
	{
		return _flyToBack;
	}

	public final int getHitTime()
	{
		return _hitTime;
	}

	public final int getVitConsume()
	{
		return _vitConsume;
	}
	
	/**
	 * @return Returns the hpConsume.
	 */
	public final int getHpConsume()
	{
		return _hpConsume;
	}

	/**
	 * @return Returns the id.
	 */
	public int getId()
	{
		return _id;
	}

	public void setId(int id)
	{
		_id = id;
	}

	/**
	 * @return Returns the itemConsume.
	 */
	public final int[] getItemConsume()
	{
		return _itemConsume;
	}

	/**
	 * @return Returns the itemConsumeId.
	 */
	public final int[] getItemConsumeId()
	{
		return _itemConsumeId;
	}

	/**
	 * @return Ð’Ð¾Ð·Ð²Ñ€Ð°Ñ‰Ð°ÐµÑ‚ Ð¸Ð´ Ð¿Ñ€ÐµÐ´Ð¼ÐµÑ‚Ð°(Ñ‚Ð°Ð»Ð¸Ñ�Ð¼Ð°Ð½Ð°)
	 * Ð¼Ð°Ð½Ñƒ ÐºÐ¾Ñ‚Ð¾Ñ€Ð¾Ð³Ð¾ Ð½Ð°Ð´Ð¾ Ð¸Ñ�Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÑŒ
	 */
	public final int getReferenceItemId()
	{
		return _referenceItemId;
	}

	/**
	 * @return Ð’Ð¾Ð·Ð²Ñ€Ð°Ñ‰Ð°ÐµÑ‚ Ð¸Ñ�Ð¿Ð¾Ð»ÑŒÐ·ÑƒÐµÐ¼Ð¾Ðµ Ð´Ð»Ñ� ÐºÐ°Ñ�Ñ‚Ð° ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ Ð¼Ð°Ð½Ñ‹
	 * Ð¿Ñ€ÐµÐ´Ð¼ÐµÑ‚Ð°(Ñ‚Ð°Ð»Ð¸Ñ�Ð¼Ð°Ð½Ð°) 
	 */
	public final int getReferenceItemMpConsume()
	{
		return _referenceItemMpConsume;
	}

	/**
	 * @return Returns the level.
	 */
	public final int getLevel()
	{
		return _level;
	}

	public final int getBaseLevel()
	{
		return _baseLevel;
	}

	public final int getLevelWithoutEnchant()
	{
		return Math.min(_level, _baseLevel);
	}

	public final void setBaseLevel(int baseLevel)
	{
		_baseLevel = baseLevel;
	}

	public final int getLevelModifier()
	{
		return _levelModifier;
	}

	public final int getMagicLevel()
	{
		return _magicLevel;
	}

	public int getMatak()
	{
		return _matak;
	}

	public int getMinPledgeClass()
	{
		return _minPledgeClass;
	}

	public int getMinRank()
	{
		return _minRank;
	}

	/**
	 * @return Returns the mpConsume as _mpConsume1 + _mpConsume2.
	 */
	public final double getMpConsume()
	{
		return _mpConsume1 + _mpConsume2;
	}

	/**
	 * @return Returns the mpConsume1.
	 */
	public final double getMpConsume1()
	{
		return _mpConsume1;
	}

	/**
	 * @return Returns the mpConsume2.
	 */
	public final double getMpConsume2()
	{
		return _mpConsume2;
	}

	/**
	 * @return Returns the name.
	 */
	public final String getName()
	{
		return _name;
	}

	public final String getName(Player player)
	{
		String name = SkillNameHolder.getInstance().getSkillName(player, this);
		return name == null ? _name : name;
	}

	public int getNegatePower()
	{
		return _negatePower;
	}

	public int getNegateSkill()
	{
		return _negateSkill;
	}

	public NextAction getNextAction()
	{
		return _nextAction;
	}

	public int getNpcId()
	{
		return _npcId;
	}

	public int getNumCharges()
	{
		return _numCharges;
	}

	public final double getPower(Creature target)
	{
		if(target != null)
		{
			if(target.isPlayable())
				return getPowerPvP();
			if(target.isMonster())
				return getPowerPvE();
		}
		return getPower();
	}

	public final double getPower()
	{
		return _power;
	}

	public final double getPowerPvP()
	{
		return _powerPvP != 0 ? _powerPvP : _power;
	}

	public final double getPowerPvE()
	{
		return _powerPvE != 0 ? _powerPvE : _power;
	}

	public final long getReuseDelay()
	{
		return _reuseDelay;
	}

	/**
	 * Ð´Ð»Ñ� Ð¸Ð·Ð¼ÐµÐ½ÐµÐ½Ð¸Ñ� Ð²Ñ€ÐµÐ¼ÐµÐ½Ð¸ Ð¾Ñ‚ÐºÐ°Ñ‚Ð° Ð¸Ð· Ñ�ÐºÑ€Ð¸Ð¿Ñ‚Ð¾Ð²
	 */
	public final void setReuseDelay(long newReuseDelay)
	{
		_reuseDelay = newReuseDelay;
	}

	public final boolean getShieldIgnore()
	{
		return _isShieldignore;
	}

	public final boolean isReflectable()
	{
		return _isReflectable;
	}

	public final int getSkillInterruptTime()
	{
		return _skillInterruptTime;
	}

	public final int getSkillRadius()
	{
		return _skillRadius;
	}

	public final SkillType getSkillType()
	{
		return _skillType;
	}

	public int getSoulsConsume()
	{
		return _soulsConsume;
	}

	public int getSymbolId()
	{
		return _symbolId;
	}

	public final SkillTargetType getTargetType()
	{
		return _targetType;
	}

	public final SkillTrait getTraitType()
	{
		return _traitType;
	}

	public final BaseStats getSaveVs()
	{
		return _saveVs;
	}

	public final int getWeaponsAllowed()
	{
		return _weaponsAllowed;
	}

	public double getLethal1()
	{
		return _lethal1;
	}

	public double getLethal2()
	{
		return _lethal2;
	}

	public String getBaseValues()
	{
		return _baseValues;
	}

	public boolean isBlockedByChar(Creature effected, EffectTemplate et)
	{
		if(et.getAttachedFuncs() == null)
			return false;
		for(FuncTemplate func : et.getAttachedFuncs())
			if(func != null && effected.checkBlockedStat(func._stat))
				return true;
		return false;
	}

	public final boolean isCancelable()
	{
		return _isCancelable && getSkillType() != SkillType.TRANSFORMATION && !isToggle();
	}

	/**
	 * Ð¯Ð²Ð»Ñ�ÐµÑ‚Ñ�Ñ� Ð»Ð¸ Ñ�ÐºÐ¸Ð»Ð» Ð¾Ð±Ñ‰Ð¸Ð¼
	 */
	public final boolean isCommon()
	{
		return _isCommon;
	}

	public final int getCriticalRate()
	{
		return _criticalRate;
	}

	public final boolean isHandler()
	{
		return _isItemHandler;
	}

	public final boolean isMagic()
	{
		return _magicType == SkillMagicType.MAGIC;
	}

	public final SkillMagicType getMagicType()
	{
		return _magicType;
	}

	public final boolean isNewbie()
	{
		return _isNewbie;
	}

	public final boolean isPreservedOnDeath()
	{
		return _isPreservedOnDeath;
	}

	public final boolean isHeroic()
	{
		return _isHeroic;
	}

	public final boolean isSelfDispellable()
	{
		return _isSelfDispellable;
	}

	public void setOperateType(SkillOpType type)
	{
		_operateType = type;
	}

	public final boolean isOverhit()
	{
		return _isOverhit;
	}

	public final boolean isActive()
	{
		return _operateType == SkillOpType.OP_ACTIVE;
	}

	public final boolean isPassive()
	{
		return _operateType == SkillOpType.OP_PASSIVE;
	}

	public boolean isSaveable()
	{
		if(!Config.ALT_SAVE_UNSAVEABLE && (isMusic() || _name.startsWith("Herb of")))
			return false;
		return _isSaveable;
	}

	/**
	 * Ð�Ð° Ð½ÐµÐºÐ¾Ñ‚Ð¾Ñ€Ñ‹Ðµ Ñ�ÐºÐ¸Ð»Ð»Ñ‹ Ð¸ Ñ…ÐµÐ½Ð´Ð»ÐµÑ€Ñ‹ Ð¿Ñ€ÐµÐ´Ð¼ÐµÑ‚Ð¾Ð² Ñ�ÐºÐ¾Ñ€Ð¾Ñ�Ñ‚Ð¸ ÐºÐ°Ñ�Ñ‚Ð°/Ð°Ñ‚Ð°ÐºÐ¸ Ð½Ðµ Ð²Ð»Ð¸Ñ�ÐµÑ‚
	 */
	public final boolean isSkillTimePermanent()
	{
		return _isSkillTimePermanent || _isItemHandler || _name.contains("Talisman");
	}

	public final boolean isReuseDelayPermanent()
	{
		return _isReuseDelayPermanent || _isItemHandler;
	}

	public boolean isDeathlink()
	{
		return _deathlink;
	}

	public boolean isBasedOnTargetDebuff()
	{
		return _basedOnTargetDebuff;
	}

	public boolean isSoulBoost()
	{
		return _isSoulBoost;
	}

	public boolean isChargeBoost()
	{
		return _isChargeBoost;
	}

	public boolean isUsingWhileCasting()
	{
		return _isUsingWhileCasting;
	}

	public boolean isBehind()
	{
		return _isBehind;
	}

	public boolean isHideStartMessage()
	{
		return _hideStartMessage;
	}

	public boolean isHideUseMessage()
	{
		return _hideUseMessage;
	}

	/**
	 * ÐœÐ¾Ð¶ÐµÑ‚ Ð»Ð¸ Ñ�ÐºÐ¸Ð»Ð» Ñ‚Ñ€Ð°Ñ‚Ð¸Ñ‚ÑŒ ÑˆÐ¾Ñ‚Ñ‹, Ð´Ð»Ñ� Ñ…ÐµÐ½Ð´Ð»ÐµÑ€Ð¾Ð² Ð²Ñ�ÐµÐ³Ð´Ð° false
	 */
	public boolean isSSPossible()
	{
		return _isUseSS == Ternary.TRUE || _isUseSS == Ternary.DEFAULT && !_isItemHandler && !isMusic() && isActive() && !(getTargetType() == SkillTargetType.TARGET_SELF && !isMagic());
	}

	public final boolean isSuicideAttack()
	{
		return _isSuicideAttack;
	}

	public final boolean isToggle()
	{
		return _operateType == SkillOpType.OP_TOGGLE;
	}

	public void setCastRange(int castRange)
	{
		_castRange = castRange;
	}
	public final boolean isDerangement()
	{
		return getTraitType() == SkillTrait.DERANGEMENT;
	}

	public void setDisplayLevel(int lvl)
	{
		_displayLevel = lvl;
	}

	public void setHitTime(int hitTime)
	{
		_hitTime = hitTime;
	}

	public void setHpConsume(int hpConsume)
	{
		_hpConsume = hpConsume;
	}

	public void setMagicType(SkillMagicType type)
	{
		_magicType = type;
	}

	public final void setMagicLevel(int newlevel)
	{
		_magicLevel = newlevel;
	}

	public void setMpConsume1(double mpConsume1)
	{
		_mpConsume1 = mpConsume1;
	}

	public void setMpConsume2(double mpConsume2)
	{
		_mpConsume2 = mpConsume2;
	}

	public void setName(String name)
	{
		_name = name;
	}

	public void setOverhit(final boolean isOverhit)
	{
		_isOverhit = isOverhit;
	}

	public final void setPower(double power)
	{
		_power = power;
	}

	public void setSkillInterruptTime(int skillInterruptTime)
	{
		_skillInterruptTime = skillInterruptTime;
	}

	public boolean isItemSkill()
	{
		return _name.contains("Item Skill") || _name.contains("Talisman");
	}

	@Override
	public String toString()
	{
		return _name + "[id=" + _id + ",lvl=" + _level + "]";
	}

	public void onFinishCast(Creature activeChar, List<Creature> targets)
	{
		if(isOffensive() && (getTargetType() == SkillTargetType.TARGET_CORPSE || getTargetType() == SkillTargetType.TARGET_AREA_AIM_CORPSE))
		{
			for(Creature target : targets)
			{
				if(target == null || !target.isNpc() || !target.isDead())
					return;

				((NpcInstance) target).endDecayTask();
			}
		}
	}

	/**
	*	activeChar - ÐšÐ°Ñ�Ñ‚ÑƒÑŽÑ‰Ð¸Ð¹ Ð¿ÐµÑ€Ñ�Ð¾Ð½Ð°Ð¶.
	*	target - Ñ†ÐµÐ»ÑŒ Ð¸Ñ�Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ð½Ð¸Ñ� Ñ�ÐºÐ¸Ð»Ð»Ð°.
	*	targets - Ð²Ñ�Ðµ Ñ†ÐµÐ»Ð¸, Ð½Ð° ÐºÐ¾Ñ‚Ð¾Ñ€Ñ‹Ñ… Ð±ÑƒÐ´ÐµÑ‚ Ð¸Ñ�Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ð½ Ñ�ÐºÐ¸Ð»Ð» (Ð²ÐºÐ»ÑŽÑ‡Ð°Ñ� target).
	*	reflected - Ð¾Ñ‚Ñ€Ð°Ð¶ÐµÐ½ Ð»Ð¸ Ñ�ÐºÐ¸Ð»Ð».
	**/
	public abstract void useSkill(Creature activeChar, List<Creature> targets);

	public void useInstantEffects(Creature activeChar, Creature target, boolean reflected)
	{
		useInstantEffects(activeChar, target, reflected, false);
	}

	public void useInstantEffects(Creature activeChar, Creature target, boolean reflected, boolean applyOnCaster)
	{
		for(EffectTemplate et : getIEffectTemplates())
			useInstantEffect(et, activeChar, target, reflected, applyOnCaster);
	}

	private boolean useInstantEffect(EffectTemplate et, Creature activeChar, Creature target, boolean reflected, boolean applyOnCaster)
	{
		if(et.getPeriod() != 0)
			return false;

		if(applyOnCaster != et._applyOnCaster)
			return false;

		if(target.isRaid() && et.getEffectType().isRaidImmune())
			return false;

		if(et.getChance() >= 0 && !Rnd.chance(et.getChance()))
			return false;

		final Env env = new Env(activeChar, target, Skill.this);
		env.reflected = reflected;

		final Effect e = et.getEffect(env);
		if(e == null || !e.isOneTime())
			return false;

		if(!e.checkCondition())
			return false;

		e.instantUse();
		return true;
	}

	public boolean isAoE()
	{
		switch(_targetType)
		{
			case TARGET_AREA:
			case TARGET_AREA_AIM_CORPSE:
			case TARGET_AURA:
			case TARGET_SERVITOR_AURA:
			case TARGET_MULTIFACE:
			case TARGET_MULTIFACE_AURA:
			case TARGET_TUNNEL:
				return true;
			default:
				return false;
		}
	}

	public boolean isNotTargetAoE()
	{
		switch(_targetType)
		{
			case TARGET_AURA:
			case TARGET_MULTIFACE_AURA:
			case TARGET_ALLY:
			case TARGET_CLAN:
			case TARGET_CLAN_ONLY:
			case TARGET_PARTY:
			case TARGET_FRIEND:
				return true;
			default:
				return false;
		}
	}

	public boolean isOffensive()
	{
		return _isOffensive;
	}
	public boolean isBuff()
	{
		return _isBuff;
	}

	public final boolean isForceUse()
	{
		return _isForceUse;
	}

	public boolean isAI()
	{
		return _skillType.isAI();
	}

	public boolean isPvM()
	{
		return _isPvm;
	}

	public final boolean isPvpSkill()
	{
		return _isPvpSkill;
	}

	public final boolean isFishingSkill()
	{
		return _isFishingSkill;
	}

	public boolean isMusic()
	{
		return _magicType == SkillMagicType.MUSIC;
	}
	// Need Fix Sir
	public boolean isChant()
	{
		return getId() == 1414 || getId() == 1363 || getId() == 1413 || getId() == 1355 || getId() == 1356 || getId() == 1357;
	}

	public boolean isTrigger()
	{
		return _isTrigger;
	}

	public boolean oneTarget()
	{
		switch(_targetType)
		{
			case TARGET_CORPSE:
			case TARGET_CORPSE_PLAYER:
			case TARGET_HOLY:
			case TARGET_FLAGPOLE:
			case TARGET_ITEM:
			case TARGET_NONE:
			case TARGET_ONE:
			case TARGET_PARTY_ONE:
			case TARGET_SERVITOR:
			case TARGET_SUMMON:
			case TARGET_PET:
			case TARGET_OWNER:
			case TARGET_ENEMY_PET:
			case TARGET_ENEMY_SUMMON:
			case TARGET_ENEMY_SERVITOR:
			case TARGET_SELF:
			case TARGET_UNLOCKABLE:
			case TARGET_CHEST:
			case TARGET_FEEDABLE_BEAST:
			case TARGET_SIEGE:
				return true;
			default:
				return false;
		}
	}

	public int getCancelTarget()
	{
		return _cancelTarget;
	}

	public boolean isSkillInterrupt()
	{
		return _skillInterrupt;
	}

	public boolean isNotUsedByAI()
	{
		return _isNotUsedByAI;
	}

	/**
	 * Ð˜Ð³Ð½Ð¾Ñ€Ð¸Ñ€Ð¾Ð²Ð°Ð½Ð¸Ðµ Ñ€ÐµÐ·Ð¸Ñ�Ñ‚Ð¾Ð²
	 */
	public boolean isIgnoreResists()
	{
		return _isIgnoreResists;
	}

	/**
	 * Ð˜Ð³Ð½Ð¾Ñ€Ð¸Ñ€Ð¾Ð²Ð°Ð½Ð¸Ðµ Ð½ÐµÑƒÑ�Ð·Ð²Ð¸Ð¼Ð¾Ñ�Ñ‚Ð¸
	 */
	public boolean isIgnoreInvul()
	{
		return _isIgnoreInvul;
	}

	public boolean isNotAffectedByMute()
	{
		return _isNotAffectedByMute;
	}

	public boolean flyingTransformUsage()
	{
		return _flyingTransformUsage;
	}

	public boolean canUseTeleport()
	{
		return _canUseTeleport;
	}

	public int getCastCount()
	{
		return _castCount;
	}

	public int getEnchantLevelCount()
	{
		return _enchantLevelCount;
	}

	public void setEnchantLevelCount(int count)
	{
		_enchantLevelCount = count;
	}

	public boolean isClanSkill()
	{
		return _id >= 370 && _id <= 391 || _id >= 611 && _id <= 616;
	}

	public boolean isBaseTransformation() //Inquisitor, Vanguard, Final Form...
	{
		return _id >= 810 && _id <= 813 || _id >= 1520 && _id <= 1522 || _id == 538;
	}

	public boolean isSummonerTransformation() // Spirit of the Cat etc
	{
		return _id >= 929 && _id <= 931;
	}

	public double getSimpleDamage(Creature attacker, Creature target)
	{
		if(isMagic())
		{
			// Ð¼Ð°Ð³Ð¸Ñ‡ÐµÑ�ÐºÐ¸Ð¹ ÑƒÑ€Ð¾Ð½
			double mAtk = attacker.getMAtk(target, this);
			double mdef = target.getMDef(null, this);
			double power = getPower();
			int sps = attacker.getChargedSpiritShot() > 0 && isSSPossible() ? attacker.getChargedSpiritShot() * 2 : 1;
			return 91 * power * Math.sqrt(sps * mAtk) / mdef;
		}
		// Ñ„Ð¸Ð·Ð¸Ñ‡ÐµÑ�ÐºÐ¸Ð¹ ÑƒÑ€Ð¾Ð½
		double pAtk = attacker.getPAtk(target);
		double pdef = target.getPDef(attacker);
		double power = getPower();
		int ss = attacker.getChargedSoulShot() && isSSPossible() ? 2 : 1;
		return ss * (pAtk + power) * 70. / pdef;
	}

	public long getReuseForMonsters()
	{
		long min = 1000;
		switch(_skillType)
		{
			case PARALYZE:
			case DEBUFF:
			case NEGATE_EFFECTS:
			case NEGATE_STATS:
			case STEAL_BUFF:
				min = 10000;
				break;
			case MUTE:
			case ROOT:
			case SLEEP:
			case STUN:
				min = 5000;
				break;
		}
		return Math.max(Math.max(_hitTime + _coolTime, _reuseDelay), min);
	}

	public double getAbsorbPart()
	{
		return _absorbPart;
	}

	public boolean isProvoke()
	{
		return _isProvoke;
	}

	public String getIcon()
	{
		return _icon;
	}

	public int getEnergyConsume()
	{
		return _energyConsume;
	}

	public void setCubicSkill(boolean value)
	{
		_isCubicSkill = value;
	}

	public boolean isCubicSkill()
	{
		return _isCubicSkill;
	}
	
	public int getNumberOfCounterAttacks()
	{
		if(_numberOfCounterAttacks < 0)
			if(isMagic() || !isOffensive() || getCastRange() > 200 || hasEffects())
				_numberOfCounterAttacks = 0;
			else
				_numberOfCounterAttacks = isBlowSkill() ? 2 : 1;

		return _numberOfCounterAttacks;
	}	
	
	public boolean isBlowSkill()
	{
		return false;
	}	
	
	public boolean isUDSafe()
	{
		return _UDSafe;
	}	

	public boolean stopActor()
	{
		return _stopActor;
	}	
	
	public boolean isAbnormalInstant()
	{
		return _isAbnormalInstant;
	}

	public double getOnAttackCancelChance()
	{
		return _onAttackCancelChance;
	}

	public double getOnCritCancelChance()
	{
		return _onCritCancelChance;
	}
}
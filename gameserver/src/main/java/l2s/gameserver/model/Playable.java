package l2s.gameserver.model;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import l2s.commons.lang.reference.HardReference;
import l2s.commons.util.Rnd;
import l2s.commons.util.concurrent.atomic.AtomicState;
import l2s.gameserver.Config;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.xml.holder.LevelBonusHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.AggroList.AggroInfo;
import l2s.gameserver.model.Skill.SkillTargetType;
import l2s.gameserver.model.Skill.SkillType;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.actor.flags.PlayableFlags;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.impl.DuelEvent;
import l2s.gameserver.model.entity.events.impl.SingleMatchEvent;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.StaticObjectInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.RevivePacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.CharTemplate;
import l2s.gameserver.templates.item.EtcItemTemplate;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType;

public abstract class Playable extends Creature
{
	private AtomicState _isSilentMoving = new AtomicState();
	
	private boolean _isPendingRevive;

	/** Блокировка для чтения/записи состояний квестов */
	protected final ReadWriteLock questLock = new ReentrantReadWriteLock();
	protected final Lock questRead = questLock.readLock();
	protected final Lock questWrite = questLock.writeLock();

	public Playable(int objectId, CharTemplate template)
	{
		super(objectId, template);
	}

	@SuppressWarnings("unchecked")
	@Override
	public HardReference<? extends Playable> getRef()
	{
		return (HardReference<? extends Playable>) super.getRef();
	}

	public abstract Inventory getInventory();

	public abstract long getWearedMask();

	/**
	 * Проверяет, выставлять ли PvP флаг для игрока.<BR><BR>
	 */
	@Override
	public boolean checkPvP(final Creature target, Skill skill)
	{
		Player player = getPlayer();

		if(isDead() || target == null || player == null || target == this || target == player || target == player.getServitor() || player.getKarma() > 0)
			return false;

		if(skill != null)
		{
			if(skill.altUse())
				return false;
			if(skill.getTargetType() == SkillTargetType.TARGET_FEEDABLE_BEAST)
				return false;
			if(skill.getTargetType() == SkillTargetType.TARGET_UNLOCKABLE)
				return false;
			if(skill.getTargetType() == SkillTargetType.TARGET_CHEST)
				return false;
		}

		// Проверка на дуэли... Мэмбэры одной дуэли не флагаются
		for(SingleMatchEvent event : getEvents(SingleMatchEvent.class))
		{
			if(!event.checkPvPFlag(player, target))
				return false;
		}

		if(isInZonePeace() && target.isInZonePeace())
			return false;
		if(isInZoneBattle() && target.isInZoneBattle() && !isInSpecialPvPZone() && !target.isInSpecialPvPZone())
			return false;
		if(isInZonePvP() && target.isInZonePvP())
			return false;
		if(getPlayer().isInFightClub())
			return false;
		if(isInZone(ZoneType.SIEGE) && target.isInZone(ZoneType.SIEGE))
			return false;

		if(skill == null || skill.isOffensive())
		{
			if(target.getKarma() > 0)
				return false;
			else if(target.isPlayable())
				return true;
		}
		else if(target.getPvpFlag() > 0 || target.getKarma() > 0 || target.isMonster())
			return true;

		return false;
	}

	/**
	 * Проверяет, можно ли атаковать цель (для физ атак)
	 */
	public boolean checkTarget(Creature target)
	{
		Player player = getPlayer();
		if(player == null)
			return false;

		if(target == null || target.isDead())
		{
			player.sendPacket(Msg.INVALID_TARGET);
			return false;
		}

		if(!isInRange(target, 2000))
		{
			player.sendPacket(SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE);
			return false;
		}

		if(target.isDoor() && !target.isAttackable(this))
		{
			player.sendPacket(Msg.INVALID_TARGET);
			return false;
		}

		if(target.paralizeOnAttack(this))
		{
			if(Config.PARALIZE_ON_RAID_DIFF)
			{
				paralizeMe(target);
				return false;
			}
		}

		// TODO: Если это оффлайк, то использовать без конфига.
		if(!Config.ALT_USE_TRANSFORM_IN_EPIC_ZONE && isCursedWeaponEquipped() && isInZone(ZoneType.epic))
		{
			paralizeMe(target);
			return false;
		}

		if(target.isInvisible(this) || getReflection() != target.getReflection() || !GeoEngine.canSeeTarget(this, target))
		{
			player.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
			return false;
		}

		// Запрет на атаку мирных NPC в осадной зоне на TW. Иначе таким способом набивают очки.
		//if(player.getTerritorySiege() > -1 && target.isNpc() && !(target instanceof L2TerritoryFlagInstance) && !(target.getAI() instanceof DefaultAI) && player.isInZone(ZoneType.Siege))
		//{
		//	player.sendPacket(Msg.INVALID_TARGET);
		//	return false;
		//}

		if(player.isInZone(ZoneType.epic) != target.isInZone(ZoneType.epic))
		{
			player.sendPacket(Msg.INVALID_TARGET);
			return false;
		}

		if(target.isPlayable())
		{
			// Нельзя атаковать того, кто находится на арене, если ты сам не на арене
			if(isInZoneBattle() != target.isInZoneBattle())
			{
				player.sendPacket(Msg.INVALID_TARGET);
				return false;
			}

			// Если цель либо атакующий находится в мирной зоне - атаковать нельзя
			if(isInZonePeace() || target.isInZonePeace())
			{
				player.sendPacket(Msg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE);
				return false;
			}
			if(player.isInOlympiadMode() && !player.isOlympiadCompStart())
				return false;
		}
			
		if(Config.ENABLE_NEW_PVP_SYSTEM && player.isInSpecialPvPZone() && target.getPlayer() != null)
		{
			int minLevel = player.getSpecialZoneMinLevel();
			int maxLevel = player.getSpecialZoneMaxLevel();
			if(player.getLevel() < minLevel || player.getLevel() > maxLevel || target.getLevel() < minLevel || target.getLevel() > maxLevel)
			{
				player.sendPacket(Msg.INVALID_TARGET);
				return false;
			}
		}			
			
		return true;
	}
	private boolean isBetray()
	{
		if(this.isSummon())
		{
			for(Effect e : getEffectList().getAllEffects())
				if(e.getEffectType() == EffectType.Betray)
				{
					return true;
				}

			return false;
		}
		return false;
	}

	@Override
	public void doAttack(Creature target)
	{
		Player player = getPlayer();
		if(player == null)
			return;
		if((this.isSummon() || this.isPet()) && target.isPlayer() && target.getPlayer() == this.getPlayer() && !isBetray())
		{
			player.sendMessage(player.isLangRus() ? "I can not beat its owner!" : "I can not beat its owner!");
			player.sendActionFailed();
			this.sendActionFailed();
			return;
		}

		if(isAMuted() || isAttackingNow())
		{
			player.sendActionFailed();
			return;
		}

		if(player.isInObserverMode())
		{
			player.sendMessage(new CustomMessage("l2s.gameserver.model.L2Playable.OutOfControl.ObserverNoAttack", player));
			return;
		}

		if(!checkTarget(target))
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
			player.sendActionFailed();
			return;
		}

		// Прерывать дуэли если цель не дуэлянт
		DuelEvent duelEvent = getEvent(DuelEvent.class);
		if(duelEvent != null && target.getEvent(DuelEvent.class) != duelEvent)
			duelEvent.abortDuel(getPlayer());

		WeaponTemplate weaponItem = getActiveWeaponTemplate();

		if(weaponItem != null && (weaponItem.getItemType() == WeaponType.BOW || weaponItem.getItemType() == WeaponType.CROSSBOW))
		{
			double bowMpConsume = weaponItem.getMpConsume();
			if(bowMpConsume > 0)
			{
				// cheap shot SA
				double chance = calcStat(Stats.MP_USE_BOW_CHANCE, 0., target, null);
				if(chance > 0 && Rnd.chance(chance))
					bowMpConsume = calcStat(Stats.MP_USE_BOW, bowMpConsume, target, null);

				if(_currentMp < bowMpConsume)
				{
					getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
					player.sendPacket(Msg.NOT_ENOUGH_MP);
					player.sendActionFailed();
					return;
				}

				reduceCurrentMp(bowMpConsume, null);
			}

			if(!player.checkAndEquipArrows())
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
				player.sendPacket(player.getActiveWeaponInstance().getItemType() == WeaponType.BOW ? Msg.YOU_HAVE_RUN_OUT_OF_ARROWS : Msg.NOT_ENOUGH_BOLTS);
				player.sendActionFailed();
				return;
			}
		}

		super.doAttack(target);
	}

	@Override
	public void doCast(final Skill skill, final Creature target, boolean forceUse)
	{
		if(skill == null)
			return;

		if((this.isSummon() || this.isPet()) && skill.isOffensive() && target.isPlayer() && target.getPlayer() == this.getPlayer() && skill.getId() != 1380)
		{
			this.getPlayer().sendMessage(this.getPlayer().isLangRus() ? "I can not beat its owner!" : "I can not beat its owner!");
			this.getPlayer().sendActionFailed();
			this.sendActionFailed();
			return;
		}

		// РџСЂРµСЂС‹РІР°С‚СЊ РґСѓСЌР»Рё РµСЃР»Рё С†РµР»СЊ РЅРµ РґСѓСЌР»СЏРЅС‚
		DuelEvent duelEvent = getEvent(DuelEvent.class);
		if(duelEvent != null && target.getEvent(DuelEvent.class) != duelEvent)
			duelEvent.abortDuel(getPlayer());

		//нельзя использовать масс скиллы в мирной зоне
		if(skill.isAoE() && isInPeaceZone())
		{
			getPlayer().sendPacket(Msg.A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_A_PEACE_ZONE);
			return;
		}

		if(skill.getSkillType() == SkillType.DEBUFF && target.isNpc() && target.isInvul() && !target.isMonster())
		{
			getPlayer().sendPacket(Msg.INVALID_TARGET);
			return;
		}

		super.doCast(skill, target, forceUse);
	}

	@Override
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
	{
		if(attacker == null || isDead() || (attacker.isDead() && !isDot))
			return;

		if(isDamageBlocked() && transferDamage)
			return;

		if(isDamageBlocked() && attacker != this)
		{
			if (sendMessage)
				attacker.sendPacket(Msg.THE_ATTACK_HAS_BEEN_BLOCKED);
			return;
		}

		if(attacker != this && attacker.isPlayable())
		{
			Player player = getPlayer();
			Player pcAttacker = attacker.getPlayer();
			if(pcAttacker != player)
				if(player.isInOlympiadMode() && !player.isOlympiadCompStart())
				{
					if (sendMessage)
						pcAttacker.sendPacket(Msg.INVALID_TARGET);
					return;
				}

			if(isInZoneBattle() != attacker.isInZoneBattle())
			{
				if (sendMessage)
					attacker.getPlayer().sendPacket(Msg.INVALID_TARGET);
				return;
			}

			DuelEvent duelEvent = getEvent(DuelEvent.class);
			if(duelEvent != null && attacker.getEvent(DuelEvent.class) != duelEvent)
				duelEvent.abortDuel(player);
		}

		super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
	}

	@Override
	public int getPAtkSpd()
	{
		return Math.max((int) (calcStat(Stats.POWER_ATTACK_SPEED, calcStat(Stats.ATK_BASE, _template.getBasePAtkSpd(), null, null), null, null)), 1);
	}

	@Override
	public int getPAtk(final Creature target)
	{
		double init = getActiveWeaponInstance() == null ? _template.getBasePAtk() : 0;
		return (int) calcStat(Stats.POWER_ATTACK, init, target, null);
	}

	@Override
	public int getMAtk(final Creature target, final Skill skill)
	{
		if(skill != null && skill.getMatak() > 0)
			return skill.getMatak();
		final double init = getActiveWeaponInstance() == null ? _template.getBaseMAtk() : 0;
		return (int) calcStat(Stats.MAGIC_ATTACK, init, target, skill);
	}

	@Override
	public boolean isAttackable(Creature attacker)
	{
		return isCtrlAttackable(attacker, true, false);
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return isCtrlAttackable(attacker, false, false);
	}

	/**
	 * force - Ctrl нажат или нет.
	 * nextAttackCheck - для флагнутых не нужно нажимать Ctrl, но нет и автоатаки.
	 */
	public boolean isCtrlAttackable(Creature attacker, boolean force, boolean nextAttackCheck)
	{
		Player player = getPlayer();
		Player pcAttacker = attacker.getPlayer();
		if(attacker == null || player == null || attacker == this || attacker == player && !force || isAlikeDead() || attacker.isAlikeDead())
			return false;

		if(isInvisible(attacker) || getReflection() != attacker.getReflection())
			return false;
		if(isInBoat())
			return false;
		for(Event e : getEvents())
			if(e.checkForAttack(this, attacker, null, force) != null)
				return false;	

		for(Event e : player.getEvents())
			if(e.canAttack(this, attacker, null, force))
				return true;

		if(pcAttacker != null && pcAttacker != player)
		{
			if(pcAttacker.isInBoat())
				return false;

			if(pcAttacker.getBlockCheckerArena() > -1 || player.getBlockCheckerArena() > -1)
				return false;

			// Player with lvl < 21 can't attack a cursed weapon holder, and a cursed weapon holder can't attack players with lvl < 21
			if(pcAttacker.isCursedWeaponEquipped() && player.getLevel() < 21 || player.isCursedWeaponEquipped() && pcAttacker.getLevel() < 21)
				return false;

			if(player.isInZone(ZoneType.epic) != pcAttacker.isInZone(ZoneType.epic))
				return false;
			if((player.isInOlympiadMode() || pcAttacker.isInOlympiadMode()) && player.getOlympiadGame() != pcAttacker.getOlympiadGame()) // РќР° РІСЃСЏРєРёР№ СЃР»СѓС‡Р°Р№
				return false;
			if(player.isInOlympiadMode() && !player.isOlympiadCompStart()) // Р‘РѕР№ РµС‰Рµ РЅРµ РЅР°С‡Р°Р»СЃСЏ
				return false;	
			if(player.isInOlympiadMode() && player.isOlympiadCompStart() && player.getOlympiadSide() == pcAttacker.getOlympiadSide() && !force) // Свою команду атаковать нельзя
				return false;
			if(player.isInNonPvpTime())
				return false;

			//TODO [VISTALL] что за?
			if(pcAttacker.getTeam() != TeamType.NONE && player.getTeam() == TeamType.NONE) // Запрет на атаку/баф участником эвента незарегистрированного игрока
				return false;
			if(player.getTeam() != TeamType.NONE && pcAttacker.getTeam() == TeamType.NONE) // Запрет на атаку/баф участника эвента незарегистрированным игроком
				return false;
			if(player.getTeam() != TeamType.NONE && pcAttacker.getTeam() != TeamType.NONE && player.getTeam() == pcAttacker.getTeam() && !player.isInLastHero() && !player.isInEventModelEvent()) // Свою команду атаковать нельзя except lasthero
				return false; 
			if(player.isInHunterClub() && pcAttacker.isInHunterClub())
				if(player.getEventTargetA() != pcAttacker && player.getEventTargetB() != pcAttacker)
					return false;
			if(isInZonePeace())
				return false;
			if(isInZoneBattle())
				return true;
			if(!force && player.getParty() != null && player.getParty() == pcAttacker.getParty())
				return false;
			if(!force && player.getClan() != null && player.getClan() == pcAttacker.getClan())
				return false;
			if(isInZonePvP())
				return true;
			if(isInZone(ZoneType.SIEGE))
				return true;
			if(pcAttacker.atMutualWarWith(player))
				return true;
			if(player.isPK())
				return true;
			if(player.getPvpFlag() != 0)
				return !nextAttackCheck;

			return force;
		}

		return true;
	}

	@Override
	public int getKarma()
	{
		Player player = getPlayer();
		return player == null ? 0 : player.getKarma();
	}

	@Override
	public void callSkill(Skill skill, List<Creature> targets, boolean useActionSkills)
	{
		Player player = getPlayer();
		if(player == null)
			return;

		if(useActionSkills && !skill.altUse() && !skill.getSkillType().equals(SkillType.BEAST_FEED))
			for(Creature target : targets)
			{
				if(target.isNpc())
				{
					if(skill.isOffensive())
					{
						// mobs will hate on debuff
						if(target.paralizeOnAttack(player))
						{
							if(Config.PARALIZE_ON_RAID_DIFF)
							{
								paralizeMe(target);
								return;
							}
						}
						if(!skill.isAI())
						{
							int damage = skill.getEffectPoint() != 0 ? skill.getEffectPoint() : 1;
							target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this, damage);
						}
					}
					target.getAI().notifyEvent(CtrlEvent.EVT_SEE_SPELL, skill, this);
				}
				else // исключать баффы питомца на владельца
				if(target.isPlayable() && target != getServitor() && !((this.isSummon() || this.isPet()) && target == player))
				{
					int aggro = skill.getEffectPoint() != 0 ? skill.getEffectPoint() : Math.max(1, (int) skill.getPower());

					List<NpcInstance> npcs = World.getAroundNpc(target);
					for(NpcInstance npc : npcs)
					{
						if(npc.isDead() || !npc.isInRangeZ(this, 2000)) //FIXME [G1ta0] параметр достойный конфига
							continue;

						npc.getAI().notifyEvent(CtrlEvent.EVT_SEE_SPELL, skill, this);

						AggroInfo ai = npc.getAggroList().get(target);
						//Пропускаем, если цель отсутсвует в хейтлисте
						if(ai == null)
							continue;

						if(!skill.isHandler() && npc.paralizeOnAttack(player))
						{
							if(Config.PARALIZE_ON_RAID_DIFF)
							{
								paralizeMe(npc);
								return;
							}
						}

						//Если хейт меньше 100, пропускаем
						if(ai.hate < 100)
							continue;

						if(GeoEngine.canSeeTarget(npc, target)) // Моб агрится только если видит цель, которую лечишь/бафаешь.
							npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this, ai.damage == 0 ? aggro / 2 : aggro);
					}
				}

				// Check for PvP Flagging / Drawing Aggro
				if(checkPvP(target, skill))
					startPvPFlag(target);
			}

		super.callSkill(skill, targets, useActionSkills);
	}

	/**
	 * Оповещает других игроков о поднятии вещи
	 * @param item предмет который был поднят
	 */
	public void broadcastPickUpMsg(ItemInstance item)
	{
		Player player = getPlayer();

		if(item == null || player == null)
			return;

		if(item.isEquipable() && !(item.getTemplate() instanceof EtcItemTemplate))
		{
			SystemMessage msg = null;
			String player_name = player.getName();
			if(item.getEnchantLevel() > 0)
			{
				int msg_id = isPlayer() ? SystemMessage.ATTENTION_S1_PICKED_UP__S2_S3 : SystemMessage.ATTENTION_S1_PET_PICKED_UP__S2_S3;
				msg = new SystemMessage(msg_id).addString(player_name).addNumber(item.getEnchantLevel()).addItemName(item.getItemId());
			}
			else
			{
				int msg_id = isPlayer() ? SystemMessage.ATTENTION_S1_PICKED_UP_S2 : SystemMessage.ATTENTION_S1_PET_PICKED_UP__S2_S3;
				msg = new SystemMessage(msg_id).addString(player_name).addItemName(item.getItemId());
			}
			for(Player target : World.getAroundPlayers(this))
			{
				if(!isInvisible(target))
					target.sendPacket(msg);
			}
		}
	}

	public void paralizeMe(Creature effector)
	{
		Skill revengeSkill = SkillHolder.getInstance().getSkill(Skill.SKILL_RAID_CURSE, 1);
		revengeSkill.getEffects(effector, this, false, false);
		if(effector.isPet() || effector.isSummon())
			revengeSkill.getEffects(effector.getPlayer(), this, false, false);
	}

	public final void setPendingRevive(boolean value)
	{
		_isPendingRevive = value;
	}

	public boolean isPendingRevive()
	{
		return _isPendingRevive;
	}

	/** Sets HP, MP and CP and revives the L2Playable. */
	public void doRevive()
	{
		if(!isTeleporting())
		{
			setPendingRevive(false);
			setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);
			setNonPvpTime(System.currentTimeMillis() + Config.NONPVP_TIME_ONTELEPORT);

			if(isSalvation() || (isPlayer() && getPlayer().isInFightClub()))
			{
				for(Effect e : getEffectList().getAllEffects())
				{
					if(e.getEffectType() == EffectType.Salvation)
						e.exit();
				}
				setCurrentHp(getMaxHp(), true);
				setCurrentMp(getMaxMp());
				setCurrentCp(getMaxCp());
			}
			else
			{
				if(isPlayer() && Config.RESPAWN_RESTORE_CP >= 0)
					setCurrentCp(getMaxCp() * Config.RESPAWN_RESTORE_CP);

				setCurrentHp(Math.max(1, getMaxHp() * Config.RESPAWN_RESTORE_HP), true);

				if(Config.RESPAWN_RESTORE_MP >= 0)
					setCurrentMp(getMaxMp() * Config.RESPAWN_RESTORE_MP);
			}

			broadcastPacket(new RevivePacket(this));
		}
		else
			setPendingRevive(true);
	}

	public abstract void doPickupItem(GameObject object);

	public void sitDown(StaticObjectInstance throne)
	{}

	public void standUp()
	{}

	private long _nonAggroTime;

	public boolean isInNonAggroTime()
	{
		return _nonAggroTime > System.currentTimeMillis();
	}

	public void setNonAggroTime(long time)
	{
		_nonAggroTime = time;
	}

	private long _nonPvpTime;

	public boolean isInNonPvpTime()
	{
		return _nonPvpTime > System.currentTimeMillis();
	}

	public void setNonPvpTime(long time)
	{
		_nonPvpTime = time;
	}

	/**
	 * 
	 * @return предыдущее состояние
	 */
	public boolean startSilentMoving()
	{
		return _isSilentMoving.getAndSet(true);
	}

	/**
	 * 
	 * @return текущее состояние
	 */
	public boolean stopSilentMoving()
	{
		return _isSilentMoving.setAndGet(false);
	}
	
	/**
	 * @return True if the Silent Moving mode is active.<BR><BR>
	 */
	public boolean isSilentMoving()
	{
		return _isSilentMoving.get();
	}

	public boolean isInCombatZone()
	{
		return isInZoneBattle();
	}

	public boolean isInPeaceZone()
	{
		return isInZonePeace();
	}

	public boolean isOnSiegeField()
	{
		return isInZone(ZoneType.SIEGE);
	}

	public boolean isInSSQZone()
	{
		return isInZone(ZoneType.ssq_zone);
	}

	public boolean isInDangerArea()
	{
		return isInZone(ZoneType.damage) || isInZone(ZoneType.swamp) || isInZone(ZoneType.poison) || isInZone(ZoneType.instant_skill);
	}

	public int getMaxLoad()
	{
		return 0;
	}

	public int getInventoryLimit()
	{
		return 0;
	}
	
	@Override
	public boolean isPlayable()
	{
		return true;
	}

	public double getLevelMod()
	{
		return LevelBonusHolder.getInstance().getLevelBonus(getLevel());
	}

	@Override
	public PlayableFlags getFlags()
	{
		if(_statuses == null)
			_statuses = new PlayableFlags(this);
		return (PlayableFlags) _statuses;
	}
}
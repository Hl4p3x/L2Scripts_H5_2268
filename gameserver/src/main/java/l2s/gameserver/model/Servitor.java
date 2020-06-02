package l2s.gameserver.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.iterator.TIntObjectIterator;
import l2s.commons.threading.RunnableImpl;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.ServitorAI;
import l2s.gameserver.dao.EffectsDAO;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.actor.recorder.ServitorStatsChangeRecorder;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.impl.SingleMatchEvent;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.PetInventory;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.network.l2.s2c.AutoAttackStartPacket;
import l2s.gameserver.network.l2.s2c.ExPartyPetWindowAdd;
import l2s.gameserver.network.l2.s2c.ExPartyPetWindowDelete;
import l2s.gameserver.network.l2.s2c.ExPartyPetWindowUpdate;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.NpcInfoPacket;
import l2s.gameserver.network.l2.s2c.NpcInfoState;
import l2s.gameserver.network.l2.s2c.PartySpelledPacket;
import l2s.gameserver.network.l2.s2c.PetDeletePacket;
import l2s.gameserver.network.l2.s2c.PetInfoPacket;
import l2s.gameserver.network.l2.s2c.PetItemListPacket;
import l2s.gameserver.network.l2.s2c.PetStatusShowPacket;
import l2s.gameserver.network.l2.s2c.PetStatusUpdatePacket;
import l2s.gameserver.network.l2.s2c.RelationChangedPacket;
import l2s.gameserver.network.l2.s2c.StatusUpdatePacket;
import l2s.gameserver.network.l2.s2c.updatetype.IUpdateTypeComponent;
import l2s.gameserver.network.l2.s2c.updatetype.NpcInfoType;
import l2s.gameserver.scripts.Events;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.taskmanager.DecayTaskManager;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;

public abstract class Servitor extends Playable
{
	private static final Logger _log = LoggerFactory.getLogger(Servitor.class);

	private static final int SUMMON_DISAPPEAR_RANGE = 2500;

	private final Player _owner;

	private int _spawnAnimation = 2;
	protected long _exp = 0;
	protected int _sp = 0;
	private int _maxLoad, _spsCharged;
	private boolean _follow = true, _depressed = false, _ssCharged = false;

	private Future<?> _decayTask;

	public Servitor(int objectId, NpcTemplate template, Player owner)
	{
		super(objectId, template);
		_owner = owner;

		if(template.getSkills().size() > 0)
			for(TIntObjectIterator<Skill> iterator = template.getSkills().iterator(); iterator.hasNext();)
			{
				iterator.advance();
				addSkill(iterator.value());
			}

		setXYZ(owner.getX() + Rnd.get(-100, 100), owner.getY() + Rnd.get(-100, 100), owner.getZ());
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();

		_spawnAnimation = 0;

		Player owner = getPlayer();
		Party party = owner.getParty();
		if(party != null)
			party.broadcastToPartyMembers(owner, new ExPartyPetWindowAdd(this));

		if(owner.isInOlympiadMode() || owner.isInFightClub())
			getEffectList().stopAllEffects();

		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

		if(owner.isGMInvisible())
			startAbnormalEffect(AbnormalEffect.STEALTH);
	}

	@Override
	public ServitorAI getAI()
	{
		if(_ai == null)
			synchronized (this)
			{
				if(_ai == null)
					_ai = new ServitorAI(this);
			}

		return (ServitorAI) _ai;
	}

	@Override
	public NpcTemplate getTemplate()
	{
		return (NpcTemplate) _template;
	}

	@Override
	public boolean isUndead()
	{
		return getTemplate().isUndead();
	}

	// this defines the action buttons, 1 for Summon, 2 for Pets
	public abstract int getServitorType();

	public abstract int getEffectIdentifier();

	/**
	 * @return Returns the mountable.
	 */
	public boolean isMountable()
	{
		return false;
	}

	@Override
	public void onAction(final Player player, boolean shift)
	{
		if(isFrozen())
		{
			player.sendPacket(ActionFailPacket.STATIC);
			return;
		}

		if(Events.onAction(player, this, shift))
		{
			player.sendPacket(ActionFailPacket.STATIC);
			return;
		}

		Player owner = getPlayer();

		if(player.getTarget() != this)
		{
			player.setTarget(this);
		}
		else if(player == owner)
		{
			player.sendPacket(new PetInfoPacket(this).update());

			if(!player.isActionsDisabled())
				player.sendPacket(new PetStatusShowPacket(this));

			player.sendPacket(ActionFailPacket.STATIC);
		}
		else if(isAutoAttackable(player))
			player.getAI().Attack(this, false, shift);
		else
		{
			if(player.getAI().getIntention() != CtrlIntention.AI_INTENTION_FOLLOW)
			{
				if(!shift)
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this, Config.FOLLOW_RANGE);
				else
					player.sendActionFailed();
			}
			else
				player.sendActionFailed();
		}
	}

	public long getExpForThisLevel()
	{
		return Experience.getExpForLevel(getLevel());
	}

	public long getExpForNextLevel()
	{
		return Experience.getExpForLevel(getLevel() + 1);
	}

	@Override
	public int getNpcId()
	{
		return getTemplate().npcId;
	}

	public final long getExp()
	{
		return _exp;
	}

	public final void setExp(final long exp)
	{
		_exp = exp;
	}

	public final int getSp()
	{
		return _sp;
	}

	public void setSp(final int sp)
	{
		_sp = sp;
	}

	@Override
	public int getMaxLoad()
	{
		return _maxLoad;
	}

	public void setMaxLoad(final int maxLoad)
	{
		_maxLoad = maxLoad;
	}

	@Override
	public int getBuffLimit()
	{
		Player owner = getPlayer();
		return (int) calcStat(Stats.BUFF_LIMIT, owner.getBuffLimit(), null, null);
	}

	public abstract int getCurrentFed();

	public abstract int getMaxFed();

	@Override
	protected void onDeath(Creature killer)
	{
		super.onDeath(killer);

		broadcastPacket(new NpcInfoState(this)); // TODO: Нужно ли здесь?

		startDecay(8500L);

		Player owner = getPlayer();

		if(killer == null || killer == owner || killer == this || (isInZoneBattle() && !isInSpecialPvPZone()) || (killer.isInZoneBattle() && !killer.isInSpecialPvPZone()))
			return;

		if(killer.isServitor())
			killer = killer.getPlayer();

		if(killer == null)
			return;

		if(owner.isInFightClub() || (killer.isPlayable() && killer.getPlayer().isInFightClub()))
			return;
		if(owner.isInZonePvP())
			return;
		if(killer.isPlayer())
		{
			if(killer.isMyServitor(getObjectId()))
				return;

			if(isInZone(ZoneType.SIEGE))
				return;

			Player pk = (Player) killer;

			if(getPvpFlag() == 0 && !getPlayer().atMutualWarWith(pk) && !isPK())
			{
				boolean eventPvPFlag = true;

				for(SingleMatchEvent matchEvent : getEvents(SingleMatchEvent.class))
				{
					if(!matchEvent.canIncreasePvPPKCounter(pk, owner))
					{
						eventPvPFlag = false;
						break;
					}
				}

				if(eventPvPFlag)
				{
					int pkCountMulti = Math.max(pk.getPkKills() / 2, 1);
					pk.increaseKarma(Config.KARMA_MIN_KARMA * pkCountMulti);
					pk.sendChanges();
				}
			}
		}
	}

	protected void startDecay(long delay)
	{
		stopDecay();
		_decayTask = DecayTaskManager.getInstance().addDecayTask(this, delay);
	}

	protected void stopDecay()
	{
		if(_decayTask != null)
		{
			_decayTask.cancel(false);
			_decayTask = null;
		}
	}

	@Override
	protected void onDecay()
	{
		deleteMe();
	}

	public void endDecayTask()
	{
		stopDecay();
		doDecay();
	}

	@Override
	public void broadcastStatusUpdate()
	{
		if(!needStatusUpdate())
			return;

		Player owner = getPlayer();

		sendStatusUpdate();

		StatusUpdatePacket su = makeStatusUpdate(StatusUpdatePacket.MAX_HP, StatusUpdatePacket.CUR_HP);
		broadcastToStatusListeners(su);

		Party party = owner.getParty();
		if(party != null)
			party.broadcastToPartyMembers(owner, new ExPartyPetWindowUpdate(this));
	}

	public void sendStatusUpdate()
	{
		Player owner = getPlayer();
		owner.sendPacket(new PetStatusUpdatePacket(this));
	}

	@Override
	protected void onDelete()
	{
		Player owner = getPlayer();

		Party party = owner.getParty();
		if(party != null)
			party.broadcastToPartyMembers(owner, new ExPartyPetWindowDelete(this));
		owner.sendPacket(new PetDeletePacket(getObjectId(), getServitorType()));
		owner.setPet(null);

		stopDecay();
		super.onDelete();
	}

	public void unSummon()
	{
		deleteMe();
	}

	public void saveEffects()
	{
		Player owner = getPlayer();
		if(owner == null)
			return;

		if(owner.isInOlympiadMode() || owner.isInFightClub())
			getEffectList().stopAllEffects();

		EffectsDAO.getInstance().insert(this);
	}

	public void setFollowMode(boolean state)
	{
		Player owner = getPlayer();

		_follow = state;

		if(_follow)
		{
			if(getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
				getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner, Config.FOLLOW_RANGE);
		}
		else if(getAI().getIntention() == CtrlIntention.AI_INTENTION_FOLLOW)
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	}

	public boolean isFollowMode()
	{
		return _follow;
	}

	private Future<?> _updateEffectIconsTask;

	private class UpdateEffectIcons extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			updateEffectIconsImpl();
			_updateEffectIconsTask = null;
		}
	}

	@Override
	public void updateEffectIcons()
	{
		if(Config.USER_INFO_INTERVAL == 0)
		{
			if(_updateEffectIconsTask != null)
			{
				_updateEffectIconsTask.cancel(false);
				_updateEffectIconsTask = null;
			}
			updateEffectIconsImpl();
			return;
		}

		if(_updateEffectIconsTask != null)
			return;

		_updateEffectIconsTask = ThreadPoolManager.getInstance().schedule(new UpdateEffectIcons(), Config.USER_INFO_INTERVAL);
	}

	public void updateEffectIconsImpl()
	{
		Player owner = getPlayer();
		PartySpelledPacket ps = new PartySpelledPacket(this, true);
		Party party = owner.getParty();
		if(party != null)
			party.broadCast(ps);
		else
			owner.sendPacket(ps);
	}

	public int getControlItemObjId()
	{
		return 0;
	}

	@Override
	public PetInventory getInventory()
	{
		return null;
	}

	@Override
	public void doPickupItem(final GameObject object)
	{}

	@Override
	public void doRevive()
	{
		super.doRevive();
		setRunning();
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		setFollowMode(true);
	}

	/**
	 * Return null.<BR><BR>
	 */
	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public WeaponTemplate getActiveWeaponTemplate()
	{
		return null;
	}

	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public WeaponTemplate getSecondaryWeaponTemplate()
	{
		return null;
	}

	@Override
	public abstract void displayGiveDamageMessage(Creature target, int damage, boolean crit, boolean miss, boolean shld, boolean magic);

	@Override
	public abstract void displayReceiveDamageMessage(Creature attacker, int damage);

	@Override
	public boolean unChargeShots(final boolean spirit)
	{
		Player owner = getPlayer();

		if(spirit)
		{
			if(_spsCharged != 0)
			{
				_spsCharged = 0;
				owner.autoShot();
				return true;
			}
		}
		else if(_ssCharged)
		{
			_ssCharged = false;
			owner.autoShot();
			return true;
		}

		return false;
	}

	@Override
	public boolean getChargedSoulShot()
	{
		return _ssCharged;
	}

	@Override
	public int getChargedSpiritShot()
	{
		return _spsCharged;
	}

	public void chargeSoulShot()
	{
		_ssCharged = true;
	}

	public void chargeSpiritShot(final int state)
	{
		_spsCharged = state;
	}

	public int getSoulshotConsumeCount()
	{
		return getLevel() / 27 + 1;
	}

	public int getSpiritshotConsumeCount()
	{
		return getLevel() / 58 + 1;
	}

	public boolean isDepressed()
	{
		return _depressed;
	}

	public void setDepressed(final boolean depressed)
	{
		_depressed = depressed;
	}

	public boolean isInRange()
	{
		Player owner = getPlayer();
		return getDistance(owner) < SUMMON_DISAPPEAR_RANGE;
	}

	public void teleportToOwner()
	{
		Player owner = getPlayer();

		setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);
		setNonPvpTime(System.currentTimeMillis() + Config.NONPVP_TIME_ONTELEPORT);
		if (owner.isInOlympiadMode() || owner.getLfcGame() != null)
			teleToLocation(owner.getLoc(), owner.getReflection());
		else
			teleToLocation(Location.findPointToStay(owner, 50, 150), owner.getReflection());

		if(!isDead() && _follow)
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner, Config.FOLLOW_RANGE);
	}

	private ScheduledFuture<?> _broadcastCharInfoTask;

	public class BroadcastCharInfoTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			broadcastCharInfoImpl(NpcInfoType.VALUES);
			_broadcastCharInfoTask = null;
		}
	}

	@Override
	public void broadcastCharInfoImpl(IUpdateTypeComponent... components)
	{
		broadcastCharInfoImpl(World.getAroundPlayers(this), components);
	}

	public void broadcastCharInfoImpl(Iterable<Player> players, IUpdateTypeComponent... components)
	{
		if(components.length == 0)
		{
			_log.warn(getClass().getSimpleName() + ": Trying broadcast char info without components!", new Exception());
			return;
		}

		Player owner = getPlayer();

		/*TODOGOD
		for(Player player : players)
		{
			if(player == owner)
				player.sendPacket(new MyPetSummonInfoPacket(this).update());
			else if(!owner.isInvisible(player))
			{
				if(isPet())
					player.sendPacket(new PetInfoPacket((PetInstance) this, player).update(components));
				else if(isSummon())
					player.sendPacket(new SummonInfoPacket((SummonInstance) this, player).update(components));
				else
					player.sendPacket(new NpcInfoPacket(this, player).update(components));
			}
			player.sendPacket(new RelationChangedPacket(this, player));
		}*/

		for(Player player : players)
		{
			if(player == owner)
				player.sendPacket(new PetInfoPacket(this).update());
			else
				player.sendPacket(new NpcInfoPacket(this, player).init().update(components));
		}
	}

	private Future<?> _petInfoTask;

	private class PetInfoTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			sendPetInfoImpl();
			_petInfoTask = null;
		}
	}

	private void sendPetInfoImpl()
	{
		Player owner = getPlayer();
		owner.sendPacket(new PetInfoPacket(this).update());
	}

	public void sendPetInfo()
	{
		if(Config.USER_INFO_INTERVAL == 0)
		{
			if(_petInfoTask != null)
			{
				_petInfoTask.cancel(false);
				_petInfoTask = null;
			}
			sendPetInfoImpl();
			return;
		}

		if(_petInfoTask != null)
			return;

		_petInfoTask = ThreadPoolManager.getInstance().schedule(new PetInfoTask(), Config.USER_INFO_INTERVAL);
	}

	/**
	 * Нужно для отображения анимации спауна, используется в пакете NpcInfo, PetInfo:
	 * 0=false, 1=true, 2=summoned (only works if model has a summon animation)
	 **/
	public int getSpawnAnimation()
	{
		return _spawnAnimation;
	}

	@Override
	public void startPvPFlag(Creature target)
	{
		Player owner = getPlayer();
		owner.startPvPFlag(target);
	}

	@Override
	public int getPvpFlag()
	{
		Player owner = getPlayer();
		return owner.getPvpFlag();
	}

	@Override
	public int getKarma()
	{
		Player owner = getPlayer();
		return owner.getKarma();
	}

	@Override
	public TeamType getTeam()
	{
		Player owner = getPlayer();
		return owner.getTeam();
	}

	@Override
	public Player getPlayer()
	{
		return _owner;
	}

	public abstract double getExpPenalty();

	@Override
	public ServitorStatsChangeRecorder getStatsRecorder()
	{
		if(_statsRecorder == null)
			synchronized (this)
			{
				if(_statsRecorder == null)
					_statsRecorder = new ServitorStatsChangeRecorder(this);
			}

		return (ServitorStatsChangeRecorder) _statsRecorder;
	}

	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		List<L2GameServerPacket> list = new ArrayList<L2GameServerPacket>();
		Player owner = getPlayer();

		if(owner == forPlayer)
		{
			list.add(new PetInfoPacket(this));
			list.add(new PartySpelledPacket(this, true));

			if(isPet())
				list.add(new PetItemListPacket((PetInstance) this));
		}
		else
		{
			Party party = forPlayer.getParty();
			if(getReflection() == ReflectionManager.GIRAN_HARBOR && (owner == null || party == null || party != owner.getParty()))
				return list;
			list.add(new NpcInfoPacket(this, forPlayer).init());
			if(owner != null && party != null && party == owner.getParty())
				list.add(new PartySpelledPacket(this, true));
			list.add(RelationChangedPacket.update(forPlayer, this, forPlayer));
		}

		if(isInCombat())
			list.add(new AutoAttackStartPacket(getObjectId()));

		if(isMoving || isFollow)
			list.add(movePacket());
		return list;
	}

	@Override
	public void startAttackStanceTask()
	{
		startAttackStanceTask0();
		Player player = getPlayer();
		if(player != null)
			player.startAttackStanceTask0();
	}

	@Override
	public <E extends Event> E getEvent(Class<E> eventClass)
	{
		Player player = getPlayer();
		if(player != null)
			return player.getEvent(eventClass);
		else
			return super.getEvent(eventClass);
	}

	@Override
	public Set<Event> getEvents()
	{
		Player player = getPlayer();
		if(player != null)
			return player.getEvents();
		else
			return super.getEvents();
	}

	@Override
	public void sendReuseMessage(Skill skill)
	{
		Player player = getPlayer();
		if(player != null && isSkillDisabled(skill))
			player.sendPacket(SystemMsg.THAT_PET_SERVITOR_SKILL_CANNOT_BE_USED_BECAUSE_IT_IS_RECHARGING);
	}

	@Override
	public boolean isServitor()
	{
		return true;
	}

	public List<Skill> getActiveSkills()
	{
		return Collections.emptyList();
	}

	public int getActiveSkillLevel(int skillId)
	{
		return getSkillLevel(skillId, 0);
	}

	@Override
	public boolean isInvisible(GameObject observer)
	{
		Player owner = getPlayer();
		if(owner != null)
		{
			if(owner == observer)
				return false;
			if(observer != null)
			{
				if(observer.isPlayer())
				{
					// TODO: Проверить на оффе.
					Player observPlayer = (Player) observer;
					if(owner.isInSameParty(observPlayer))
						return false;
				}
			}
			if(owner.isGMInvisible())
				return true;
		}
		return super.isInvisible(observer);
	}

	@Override
	protected L2GameServerPacket changeMovePacket()
	{
		return new NpcInfoState(this);
	}
}
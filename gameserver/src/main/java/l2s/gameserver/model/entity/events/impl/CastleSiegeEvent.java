package l2s.gameserver.model.entity.events.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.napile.primitive.Containers;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.TreeIntSet;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.dao.JdbcEntityState;
import l2s.commons.threading.RunnableImpl;
import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.CastleDamageZoneDAO;
import l2s.gameserver.dao.CastleDoorUpgradeDAO;
import l2s.gameserver.dao.CastleHiredGuardDAO;
import l2s.gameserver.dao.SiegeClanDAO;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.entity.HeroDiary;
import l2s.gameserver.model.entity.SevenSigns;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.objects.DoorObject;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.events.objects.SiegeToggleNpcObject;
import l2s.gameserver.model.entity.events.objects.SpawnExObject;
import l2s.gameserver.model.entity.events.objects.SpawnSimpleObject;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.instances.residences.SiegeToggleNpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.UnitMember;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.PlaySoundPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.item.support.MerchantGuard;
import l2s.gameserver.utils.Location;

/**
 * @author VISTALL
 * @date 15:12/14.02.2011
 */
public class CastleSiegeEvent extends SiegeEvent<Castle, SiegeClanObject>
{
	private class NextSiegeDateSet extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			setNextSiegeTime();
		}
	}

	public static final int MAX_SIEGE_CLANS = 20;
	public static final long DAY_IN_MILISECONDS = 86400000L;

	public static final String DEFENDERS_WAITING = "defenders_waiting";
	public static final String DEFENDERS_REFUSED = "defenders_refused";

	public static final String CONTROL_TOWERS	= "control_towers";
	public static final String FLAME_TOWERS		= "flame_towers";
	public static final String BOUGHT_ZONES		= "bought_zones";
	public static final String GUARDS			= "guards";
	public static final String HIRED_GUARDS		= "hired_guards";

	private IntSet _nextSiegeTimes = Containers.EMPTY_INT_SET;
	private Future<?> _nextSiegeDateSetTask = null;
	private boolean _firstStep = false;

	protected final List<SchedulingPattern> _startTimePatterns = new ArrayList<SchedulingPattern>();

	public CastleSiegeEvent(MultiValueSet<String> set)
	{
		super(set);

		_startTimePatterns.add(_startTimePattern);

		String addStartTime = set.getString("additional_start_time", null);
		if(!StringUtils.isEmpty(addStartTime))
		{
			StringTokenizer st = new StringTokenizer(addStartTime, "|");
			while(st.hasMoreTokens())
			{
				_startTimePatterns.add(new SchedulingPattern(st.nextToken()));
			}
		}
	}

	//========================================================================================================================================================================
	//                                                    Главные методы осады
	//========================================================================================================================================================================
	@Override
	public void initEvent()
	{
		super.initEvent();

		List<DoorObject> doorObjects = getObjects(DOORS);

		addObjects(BOUGHT_ZONES, CastleDamageZoneDAO.getInstance().load(getResidence()));

		for(DoorObject doorObject : doorObjects)
		{
			doorObject.setUpgradeValue(this, CastleDoorUpgradeDAO.getInstance().load(doorObject.getUId()));
			doorObject.getDoor().addListener(_doorDeathListener);
		}
	}

	@Override
	public void processStep(Clan newOwnerClan)
	{
		Clan oldOwnerClan = getResidence().getOwner();

		getResidence().changeOwner(newOwnerClan);

		// если есть овнер в резиденции, делаем его аттакером
		if(oldOwnerClan != null)
		{
			SiegeClanObject ownerSiegeClan = getSiegeClan(DEFENDERS, oldOwnerClan);
			if(ownerSiegeClan != null)
			{
				removeObject(DEFENDERS, ownerSiegeClan);

				ownerSiegeClan.setType(ATTACKERS);
				addObject(ATTACKERS, ownerSiegeClan);
			}
		}
		else
		{
			// Если атакуется замок, принадлежащий NPC, и только 1 атакующий - закончить осаду
			if(getObjects(ATTACKERS).size() == 1)
			{
				stopEvent(false);
				return;
			}

			// Если атакуется замок, принадлежащий NPC, и все атакующие в одном альянсе - закончить осаду
			int allianceObjectId = newOwnerClan.getAllyId();
			if(allianceObjectId > 0)
			{
				List<SiegeClanObject> attackers = getObjects(ATTACKERS);
				boolean sameAlliance = true;
				for(SiegeClanObject sc : attackers)
					if(sc != null && sc.getClan().getAllyId() != allianceObjectId)
						sameAlliance = false;
				if(sameAlliance)
				{
					stopEvent(false);
					return;
				}
			}
		}

		// ставим нового овнера защитником
		SiegeClanObject newOwnerSiegeClan = getSiegeClan(ATTACKERS, newOwnerClan);
		newOwnerSiegeClan.deleteFlag();
		newOwnerSiegeClan.setType(DEFENDERS);

		removeObject(ATTACKERS, newOwnerSiegeClan);

		// у нас защитник ток овнер
		List<SiegeClanObject> defenders = removeObjects(DEFENDERS);
		for(SiegeClanObject siegeClan : defenders)
			siegeClan.setType(ATTACKERS);

		// новый овнер это защитник
		addObject(DEFENDERS, newOwnerSiegeClan);

		// все дефендеры, стают аттакующими
		addObjects(ATTACKERS, defenders);

		updateParticles(true, ATTACKERS, DEFENDERS);

		teleportPlayers(ATTACKERS);
		teleportPlayers(SPECTATORS);

		// ток при первом захвате обнуляем мерчант гвардов и убираем апгрейд дверей
		if(!_firstStep)
		{
			_firstStep = true;

			broadcastTo(SystemMsg.THE_TEMPORARY_ALLIANCE_OF_THE_CASTLE_ATTACKER_TEAM_HAS_BEEN_DISSOLVED, ATTACKERS, DEFENDERS);

			if(_oldOwner != null)
			{
				if(containsObjects(HIRED_GUARDS))
					spawnAction(HIRED_GUARDS, false);
				damageZoneAction(false);
				removeObjects(HIRED_GUARDS);
				removeObjects(BOUGHT_ZONES);

				CastleDamageZoneDAO.getInstance().delete(getResidence());
			}
			else
				spawnAction(GUARDS, false);

			List<DoorObject> doorObjects = getObjects(DOORS);
			for(DoorObject doorObject : doorObjects)
			{
				doorObject.setWeak(true);
				doorObject.setUpgradeValue(this, 0);

				CastleDoorUpgradeDAO.getInstance().delete(doorObject.getUId());
			}
		}

		spawnAction(DOORS, true);
		despawnSiegeSummons();
		
		List<SiegeToggleNpcObject> towers = getObjects(CastleSiegeEvent.CONTROL_TOWERS);

		for(SiegeToggleNpcObject t : towers)
		{
			if(!t.isAlive())
				t.spawnObject(this);
			else
				t.getToggleNpc().setCurrentHp(t.getToggleNpc().getMaxHp(), true);
		}
	}

	@Override
	public void startEvent()
	{
		if(!_isInProgress.compareAndSet(false, true))
			return;
		_oldOwner = getResidence().getOwner();
		if(_oldOwner != null)
		{
			addObject(DEFENDERS, new SiegeClanObject(DEFENDERS, _oldOwner, 0));

			if(getResidence().getSpawnMerchantTickets().size() > 0)
			{
				for(ItemInstance item : getResidence().getSpawnMerchantTickets())
				{
					MerchantGuard guard = getResidence().getMerchantGuard(item.getItemId());

					addObject(HIRED_GUARDS, new SpawnSimpleObject(guard.getNpcId(), item.getLoc()));

					item.deleteMe();
				}

				CastleHiredGuardDAO.getInstance().delete(getResidence());

				if(containsObjects(HIRED_GUARDS))
					spawnAction(HIRED_GUARDS, true);
			}
		}

		List<SiegeClanObject> attackers = getObjects(ATTACKERS);
		if(attackers.isEmpty())
		{
			if(_oldOwner == null)
				broadcastToWorld(new SystemMessagePacket(SystemMsg.THE_SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST).addResidenceName(getResidence()));
			else
			{
				broadcastToWorld(new SystemMessagePacket(SystemMsg.S1S_SIEGE_WAS_CANCELED_BECAUSE_THERE_WERE_NO_CLANS_THAT_PARTICIPATED).addResidenceName(getResidence()));

				getResidence().getOwner().setCastleDefendCount(getResidence().getOwner().getCastleDefendCount() + 1);
				getResidence().getOwner().updateClanInDB();
			}

			setInProgress(false);
			getResidence().getOwnDate().setTimeInMillis(getResidence().getOwner() == null ? 0 : System.currentTimeMillis());
			reCalcNextTime(false);
			return;
		}

		SiegeClanDAO.getInstance().delete(getResidence());

		updateParticles(true, ATTACKERS, DEFENDERS);

		broadcastTo(SystemMsg.THE_TEMPORARY_ALLIANCE_OF_THE_CASTLE_ATTACKER_TEAM_IS_IN_EFFECT, ATTACKERS);
		broadcastTo(new SystemMessagePacket(SystemMsg.YOU_ARE_PARTICIPATING_IN_THE_SIEGE_OF_S1_THIS_SIEGE_IS_SCHEDULED_FOR_2_HOURS).addResidenceName(getResidence()), ATTACKERS, DEFENDERS);

		super.startEvent();

		if(_oldOwner == null)
			initControlTowers();
		else
			damageZoneAction(true);
	}

	@Override
	public void stopEvent(boolean step)
	{
		if(!_isInProgress.compareAndSet(true, false))
			return;

		List<DoorObject> doorObjects = getObjects(DOORS);
		for(DoorObject doorObject : doorObjects)
			doorObject.setWeak(false);

		damageZoneAction(false);

		updateParticles(false, ATTACKERS, DEFENDERS);

		List<SiegeClanObject> attackers = removeObjects(ATTACKERS);
		for(SiegeClanObject siegeClan : attackers)
			siegeClan.deleteFlag();

		broadcastToWorld(new SystemMessagePacket(SystemMsg.THE_SIEGE_OF_S1_IS_FINISHED).addResidenceName(getResidence()));

		removeObjects(DEFENDERS);
		removeObjects(DEFENDERS_WAITING);
		removeObjects(DEFENDERS_REFUSED);

		Clan ownerClan = getResidence().getOwner();
		if(ownerClan != null)
		{
			if(_oldOwner == ownerClan)
			{
				getResidence().getOwner().setCastleDefendCount(getResidence().getOwner().getCastleDefendCount() + 1);
				getResidence().getOwner().updateClanInDB();

				ownerClan.broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.SINCE_YOUR_CLAN_EMERGED_VICTORIOUS_FROM_THE_SIEGE_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLANS_REPUTATION_SCORE).addInteger(ownerClan.incReputation(1500, false, toString())));
			}
			else
			{
				broadcastToWorld(new SystemMessagePacket(SystemMsg.CLAN_S1_IS_VICTORIOUS_OVER_S2S_CASTLE_SIEGE).addString(ownerClan.getName()).addResidenceName(getResidence()));

				ownerClan.broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.SINCE_YOUR_CLAN_EMERGED_VICTORIOUS_FROM_THE_SIEGE_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLANS_REPUTATION_SCORE).addInteger(ownerClan.incReputation(3000, false, toString())));

				if(_oldOwner != null)
				{
					_oldOwner.broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.YOUR_CLAN_HAS_FAILED_TO_DEFEND_THE_CASTLE));
					_oldOwner.incReputation(-3000, false, toString());
				}
				for(UnitMember member : ownerClan)
				{
					Player player = member.getPlayer();
					if(player != null)
					{
						player.sendPacket(PlaySoundPacket.SIEGE_VICTORY);
						if(player.isOnline() && player.isNoble())
							Hero.getInstance().addHeroDiary(player.getObjectId(), HeroDiary.ACTION_CASTLE_TAKEN, getResidence().getId());
					}
				}
			}

			getResidence().getOwnDate().setTimeInMillis(System.currentTimeMillis());
			getResidence().getLastSiegeDate().setTimeInMillis(getResidence().getSiegeDate().getTimeInMillis());

			DominionSiegeRunnerEvent runnerEvent = EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 1);
			runnerEvent.registerDominion(getResidence().getDominion());
		}
		else
		{
			broadcastToWorld(new SystemMessagePacket(SystemMsg.THE_SIEGE_OF_S1_HAS_ENDED_IN_A_DRAW).addResidenceName(getResidence()));

			getResidence().getOwnDate().setTimeInMillis(0);
			getResidence().getLastSiegeDate().setTimeInMillis(0);

			DominionSiegeRunnerEvent runnerEvent = EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 1);
			runnerEvent.unRegisterDominion(getResidence().getDominion());
		}

		despawnSiegeSummons();

		if(_oldOwner != null)
		{
			if(containsObjects(HIRED_GUARDS))
				spawnAction(HIRED_GUARDS, false);
			removeObjects(HIRED_GUARDS);
		}

		super.stopEvent(step);
	}
	//========================================================================================================================================================================

	@Override
	public void reCalcNextTime(boolean onInit)
	{
		clearActions();

		final long currentTimeMillis = System.currentTimeMillis();
		final Calendar startSiegeDate = getResidence().getSiegeDate();
		final Calendar ownSiegeDate = getResidence().getOwnDate();
		if(onInit)
		{
			if(startSiegeDate.getTimeInMillis() > currentTimeMillis)
				registerActions();
			else if(startSiegeDate.getTimeInMillis() == 0)
			{
				if((currentTimeMillis - ownSiegeDate.getTimeInMillis()) > DAY_IN_MILISECONDS) // прошол день после осады
					setNextSiegeTime();
				else
					generateNextSiegeDates();
			}
			else if(startSiegeDate.getTimeInMillis() <= currentTimeMillis)
				setNextSiegeTime();
		}
		else
		{
			if(getResidence().getOwner() != null)
			{
				getResidence().getSiegeDate().setTimeInMillis(0);
				getResidence().setJdbcState(JdbcEntityState.UPDATED);
				getResidence().update();

				generateNextSiegeDates();
			}
			else
				setNextSiegeTime();
		}
	}

	@Override
	public void loadSiegeClans()
	{
		super.loadSiegeClans();

		addObjects(DEFENDERS_WAITING, SiegeClanDAO.getInstance().load(getResidence(), DEFENDERS_WAITING));
		addObjects(DEFENDERS_REFUSED, SiegeClanDAO.getInstance().load(getResidence(), DEFENDERS_REFUSED));
	}

	@Override
	public void setRegistrationOver(boolean b)
	{
		if(b)
			broadcastToWorld(new SystemMessagePacket(SystemMsg.THE_DEADLINE_TO_REGISTER_FOR_THE_SIEGE_OF_S1_HAS_PASSED).addResidenceName(getResidence()));

		super.setRegistrationOver(b);
	}

	@Override
	public void announce(int id, String value, int time)
	{
		if(id == 1)
		{
			SystemMessagePacket msg;
			int seconds = Integer.parseInt(value);
			int min = seconds / 60;
			int hour = min / 60;

			if(hour > 0)
				msg = new SystemMessagePacket(SystemMsg.S1_HOURS_UNTIL_CASTLE_SIEGE_CONCLUSION).addInteger(hour);
			else if(min > 0)
				msg = new SystemMessagePacket(SystemMsg.S1_MINUTES_UNTIL_CASTLE_SIEGE_CONCLUSION).addInteger(min);
			else
				msg = new SystemMessagePacket(SystemMsg.THIS_CASTLE_SIEGE_WILL_END_IN_S1_SECONDS).addInteger(seconds);

			broadcastTo(msg, ATTACKERS, DEFENDERS);
		}
	}
	//========================================================================================================================================================================
	//                                                   Control Tower Support
	//========================================================================================================================================================================
	private void initControlTowers()
	{
		List<SpawnExObject> objects = getObjects(GUARDS);
		List<Spawner> spawns = new ArrayList<Spawner>();
		for(SpawnExObject o : objects)
			spawns.addAll(o.getSpawns());

		List<SiegeToggleNpcObject> ct = getObjects(CONTROL_TOWERS);

		SiegeToggleNpcInstance closestCt;
		double distance, distanceClosest;

		for(Spawner spawn : spawns)
		{
			Location spawnLoc = spawn.getCurrentSpawnRange().getRandomLoc(ReflectionManager.DEFAULT.getGeoIndex(), false);

			closestCt = null;
			distanceClosest = 0;

			for(SiegeToggleNpcObject c : ct)
			{
				SiegeToggleNpcInstance npcTower = c.getToggleNpc();
				distance = npcTower.getDistance(spawnLoc);

				if(closestCt == null || distance < distanceClosest)
				{
					closestCt = npcTower;
					distanceClosest = distance;
				}

				closestCt.register(spawn);
			}
		}
	}
	//========================================================================================================================================================================
	//                                                    Damage Zone Actions
	//========================================================================================================================================================================
	private void damageZoneAction(boolean active)
	{
		if(containsObjects(BOUGHT_ZONES))
			zoneAction(BOUGHT_ZONES, active);
	}
	//========================================================================================================================================================================
	//                                                    Суппорт Методы для установки времени осады
	//========================================================================================================================================================================
	/**
	 * Генерирует даты для следующей осады замка, и запускает таймер на автоматическую установку даты
	 */
	public void generateNextSiegeDates()
	{
		if(getResidence().getSiegeDate().getTimeInMillis() != 0)
			return;

		_nextSiegeTimes = new TreeIntSet();

		for(SchedulingPattern pattern : _startTimePatterns)
		{
			long time = generateSiegeDateTime(pattern);
			if(time > 0)
				_nextSiegeTimes.add((int) (time / 1000L));
		}

		long diff = (getResidence().getOwnDate().getTimeInMillis() + DAY_IN_MILISECONDS) - System.currentTimeMillis();
		if(diff < 0) //negative
			diff = 1000L;
		_nextSiegeDateSetTask = ThreadPoolManager.getInstance().schedule(new NextSiegeDateSet(), diff);
	}

	/**
	 * Ставит осадное время вручну, вызывается с пакета {@link l2s.gameserver.network.l2.c2s.RequestSetCastleSiegeTime}
	 * @param id
	 */
	public void setNextSiegeTime(int id)
	{
		if(!_nextSiegeTimes.contains(id) || _nextSiegeDateSetTask == null)
			return;

		_nextSiegeTimes = Containers.EMPTY_INT_SET;
		_nextSiegeDateSetTask.cancel(false);
		_nextSiegeDateSetTask = null;

		setNextSiegeTime(id * 1000L);
	}

	/**
	 * Автоматически генерит и устанавливает дату осады
	 */
	private void setNextSiegeTime()
	{
		if(getResidence().getLastSiegeDate().getTimeInMillis() == 0)
			setNextSiegeTime(generateSiegeDateTime(_startTimePattern));
		else
		{
			final long lastSiegeTime = getResidence().getLastSiegeDate().getTimeInMillis();
			for(SchedulingPattern pattern : _startTimePatterns)
			{
				if(pattern.match(lastSiegeTime))
				{
					setNextSiegeTime(generateSiegeDateTime(pattern));
					return;
				}
			}
			setNextSiegeTime(generateSiegeDateTime(_startTimePattern));
		}
	}
	/**
	 * Ставит дату осады, запускает действия, аннонсирует по миру
	 * @param g
	 */
	private void setNextSiegeTime(long g)
	{
		if(g == 0)
			return;

		broadcastToWorld(new SystemMessagePacket(SystemMsg.S1_HAS_ANNOUNCED_THE_NEXT_CASTLE_SIEGE_TIME).addResidenceName(getResidence()));

		clearActions();

		getResidence().getSiegeDate().setTimeInMillis(g);
		getResidence().setJdbcState(JdbcEntityState.UPDATED);
		getResidence().update();

		registerActions();
	}

	@Override
	public boolean isAttackersInAlly()
	{
		return !_firstStep;
	}

	public int[] getNextSiegeTimes()
	{
		return _nextSiegeTimes.toArray();
	}

	@Override
	public boolean canRessurect(Player resurrectPlayer, Creature target, boolean force)
	{
		boolean playerInZone = resurrectPlayer.isInZone(Zone.ZoneType.SIEGE);
		boolean targetInZone = target.isInZone(Zone.ZoneType.SIEGE);
		// если оба вне зоны - рес разрешен
		if(!playerInZone && !targetInZone)
			return true;
		// если таргет вне осадный зоны - рес разрешен
		if(!targetInZone)
			return true;

		Player targetPlayer = target.getPlayer();
		// если таргет не с нашей осады(или вообще нету осады) - рес запрещен
		CastleSiegeEvent siegeEvent = targetPlayer.getEvent(CastleSiegeEvent.class);
		CastleSiegeEvent siegeEvent2 = resurrectPlayer.getEvent(CastleSiegeEvent.class);
		
		if(siegeEvent != siegeEvent2 || siegeEvent == null || siegeEvent2 == null || siegeEvent != this || siegeEvent2 != this)
		{
			if(force)
				targetPlayer.sendPacket(SystemMsg.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE);
			resurrectPlayer.sendPacket(force ? SystemMsg.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE : SystemMsg.INVALID_TARGET);
			return false;
		}
		
		SiegeClanObject targetSiegeClan = siegeEvent.getSiegeClan(ATTACKERS, targetPlayer.getClan());
		if(targetSiegeClan == null)
			targetSiegeClan = siegeEvent.getSiegeClan(DEFENDERS, targetPlayer.getClan());

		if(targetSiegeClan.getType() == ATTACKERS)
		{
			// если нету флага - рес запрещен
			if(targetSiegeClan.getFlag() == null)
			{
				if(force)
					targetPlayer.sendPacket(SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);
				resurrectPlayer.sendPacket(force ? SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE : SystemMsg.INVALID_TARGET);
				return false;
			}
		}
		else
		{
			List<SiegeToggleNpcObject> towers = getObjects(CastleSiegeEvent.CONTROL_TOWERS);

			boolean canRes = true;
			for(SiegeToggleNpcObject t : towers)
				if(!t.isAlive())
					canRes = false;

			if(!canRes)
			{
				if(force)
					targetPlayer.sendPacket(SystemMsg.THE_GUARDIAN_TOWER_HAS_BEEN_DESTROYED_AND_RESURRECTION_IS_NOT_POSSIBLE);
				resurrectPlayer.sendPacket(force ? SystemMsg.THE_GUARDIAN_TOWER_HAS_BEEN_DESTROYED_AND_RESURRECTION_IS_NOT_POSSIBLE : SystemMsg.INVALID_TARGET);
				return false;
			}
		}

		return true; //test is all conds are good return true

		/*if(force)
			return true;
		else
		{
			resurrectPlayer.sendPacket(SystemMsg.INVALID_TARGET);
			return false;
		}
		*/
	}

	@Override
	public Location getRestartLoc(Player player, RestartType type)
	{
		Location loc = null;
		SiegeClanObject attackerClan = getSiegeClan(ATTACKERS, player.getClan());
		switch(type)
		{
			case TO_VILLAGE:
				// Если печатью владеют лорды Рассвета (Dawn), и в данном городе идет осада, то телепортирует во 2-й по счету город.
				if(SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE) == SevenSigns.CABAL_DAWN)
					loc = _residence.getNotOwnerRestartPoint(player);
				break;
				
			case TO_FLAG:
				if(!getObjects(FLAG_ZONES).isEmpty() && attackerClan != null && attackerClan.getFlag() != null)
				{
					loc = Location.findPointToStay(attackerClan.getFlag(), 50, 75);
				}	
				else
				{
					player.sendPacket(SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);
				}	
				break;				
		}

		return loc;
	}
}

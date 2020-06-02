package l2s.gameserver.model;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.napile.primitive.pair.ByteObjectPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.map.TIntObjectMap;
import l2s.commons.geometry.Shape;
import l2s.commons.lang.reference.HardReference;
import l2s.commons.lang.reference.HardReferences;
import l2s.gameserver.Config;
import l2s.gameserver.geodata.GeoControl;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventOwner;
import l2s.gameserver.network.l2.s2c.DeleteObjectPacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.scripts.Events;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.Util;

public abstract class GameObject extends EventOwner implements GeoControl
{
	private static final Logger _log = LoggerFactory.getLogger(GameObject.class);

	public final static GameObject[] EMPTY_L2OBJECT_ARRAY = new GameObject[0];

	/** Основные состояния объекта */
	protected final static int CREATED = 0;
	protected final static int VISIBLE = 1;
	protected final static int DELETED = -1;

	/** Идентификатор объекта */
	protected int objectId;

	/** Позиция объекта в мире */
	private int _x;
	private int _y;
	private int _z;
	private int _h;
	
 	protected Reflection _reflection = ReflectionManager.DEFAULT;

 	private WorldRegion _currentRegion;

	/** Состояние объекта */
	private final AtomicInteger _state = new AtomicInteger(CREATED);

	private Shape _geoShape;
	private TIntObjectMap<ByteObjectPair<GeoEngine.CeilGeoControlType>> _geoAround;
	private int _geoControlIndex = -1;
	private final Lock _geoLock = new ReentrantLock();

	protected GameObject()
	{

	}

	/**
	 * Constructor<?> of L2Object.<BR><BR>
	 * @param objectId Идентификатор объекта
	 */
	public GameObject(int objectId)
	{
		this.objectId = objectId;
	}

	public HardReference<? extends GameObject> getRef()
	{
		return HardReferences.emptyRef();
	}

	private void clearRef()
	{
		HardReference<? extends GameObject> reference = getRef();
		if(reference != null)
			reference.clear();
	}

	public Reflection getReflection()
	{
		return _reflection;
	}

	public int getReflectionId()
	{
		return _reflection.getId();
	}

	public int getGeoIndex()
	{
		return _reflection.isCollapseStarted() ? 0 : _reflection.getGeoIndex();
	}

	public boolean setReflection(Reflection reflection)
	{
		if(_reflection == reflection)
			return true;

		if(reflection.isCollapseStarted())
			return false;

		boolean respawn = false;
		if(isVisible())
		{
			decayMe();
			respawn = true;
		}

		Reflection r = getReflection();
		if(!r.isDefault())
		{
			r.removeObject(this);
		}

		_reflection = reflection;

		if(!reflection.isDefault())
		{
			reflection.addObject(this);
		}

		if(respawn)
			spawnMe();

		return true;
	}

	public void setReflection(int reflectionId)
	{
		Reflection r = ReflectionManager.getInstance().get(reflectionId);
		if(r == null)
		{
			Log.debug("Trying to set unavailable reflection: " + reflectionId + " for object: " + this + "!", new Throwable().fillInStackTrace());
			return;
		}

		setReflection(r);
	}

	/**
	 * Return the identifier of the L2Object.<BR><BR>
	 *
	 * @ - deprecated?
	 */
	@Override
	public final int hashCode()
	{
		return objectId;
	}

	public final int getObjectId()
	{
		return objectId;
	}

	public int getX()
	{
		return _x;
	}

	public int getY()
	{
		return _y;
	}

	public int getZ()
	{
		return _z;
	}

	/**
	 * Возвращает позицию (x, y, z, heading)
	 * @return Location
	 */
	public Location getLoc()
	{
		return new Location(_x, _y, _z, getHeading());
	}

	public int getGeoZ(int x, int y, int z)
	{
		return GeoEngine.correctGeoZ(x, y, z, getGeoIndex());
	}

	public final int getGeoZ(Location loc)
	{
		return getGeoZ(loc.getX(), loc.getY(), loc.getZ());
	}

	/**
	 * Устанавливает позицию (x, y, z) L2Object
	 * @param loc Location
	 */
	public boolean setLoc(Location loc)
	{
		return setXYZ(loc.x, loc.y, loc.z);
	}

	public boolean setXYZ(int x, int y, int z)
	{
		x = World.validCoordX(x);
		y = World.validCoordY(y);
		z = World.validCoordZ(z);
		z = getGeoZ(x, y, z);

		if(!isBoat()) {
			if (isFlying())
				z += 32;
			else if (isInWater())
				z += 16;
		}

		if(_x == x && _y == y && _z == z)
			return false;

		_x = x;
		_y = y;
		_z = z;

		World.addVisibleObject(this, null);

		// Обновляем геодату при изменении координат.
		refreshGeoControl();
		return true;
	}

	/**
	 * Return the visibility state of the L2Object. <BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Object is invisible if <B>_isVisible</B>=false or <B>_worldregion</B>==null <BR><BR>
	 *
	 * @return true if visible
	 */
	public final boolean isVisible()
	{
		return _state.get() == VISIBLE;
	}
	
	public int getState0()
	{
		return _state.get();
	}

	public boolean isInvisible(GameObject observer)
	{
		return false;
	}

	public void spawnMe(Location loc)
	{
		spawnMe0(loc, null);
	}

	protected void spawnMe0(Location loc, Creature dropper)
	{
		_x = loc.x;
		_y = loc.y;
		_z = getGeoZ(loc);

		spawn0(dropper);
	}

	public final void spawnMe()
	{
		spawn0(null);
	}

	/**
	 * Добавляет обьект в мир, добавляет в текущий регион. Делает обьект видимым.
	 */
	protected void spawn0(Creature dropper)
	{
		if(_reflection.isCollapseStarted())
			return;

		if(!_state.compareAndSet(CREATED, VISIBLE))
			return;

		World.addVisibleObject(this, dropper);

		onSpawn();
	}

	public void toggleVisible0()
	{
		if(!_state.compareAndSet(DELETED, CREATED))
		{
			System.out.println("state NOT changed from deleted to created!");
		}
		if(!_state.compareAndSet(CREATED, VISIBLE))
		{
			System.out.println("state NOT changed from created to visible");
		}
		World.addVisibleObject(this, null);

		onSpawn();	
		
		decayMe();
		spawnMe(getLoc());
	}
	
	public void toggleVisible()
	{
		if(isVisible())
			decayMe();
		else
			spawnMe();
	}

	/**
	 * Do Nothing.<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2Summon :  Reset isShowSpawnAnimation flag</li>
	 * <li> L2NpcInstance    :  Reset some flags</li><BR><BR>
	 *
	 */
	protected void onSpawn()
	{
		activateGeoControl();
	}

	/**
	 * Удаляет объект из текущего региона, делая его невидимым.
	 * Не путать с deleteMe. Объект после decayMe подлежит реюзу через spawnMe.
	 * Если перепутать будет утечка памяти.
	 */
	public final void decayMe()
	{
		if(!_state.compareAndSet(VISIBLE, CREATED))
			return;

		World.removeVisibleObject(this);
		onDespawn();
	}

	protected void onDespawn()
	{
		deactivateGeoControl();
	}

	/**
	 * Удаляет объект из мира. После этого объект не подлежит использованию.
	 */
	public final void deleteMe()
	{
		decayMe();

		if(!_state.compareAndSet(CREATED, DELETED))
			return;

		onDelete();
	}

	public final boolean isDeleted()
	{
		return _state.get() == DELETED;
	}

	protected void onDelete()
	{
		Reflection r = getReflection();
		if(!r.isDefault())
			r.removeObject(this);

		clearRef();
	}

	public void onAction(Player player, boolean shift)
	{
		if(Events.onAction(player, this, shift))
			return;
			
		player.sendActionFailed();
	}

	public void onForcedAttack(Player player, boolean shift)
	{
		player.sendActionFailed();
	}

	public boolean isAttackable(Creature attacker)
	{
		return false;
	}

	public String getL2ClassShortName()
	{
		return getClass().getSimpleName();
	}

	public final long getXYDeltaSq(int x, int y)
	{
		long dx = x - getX();
		long dy = y - getY();
		return dx * dx + dy * dy;
	}

	public final long getXYDeltaSq(Location loc)
	{
		return getXYDeltaSq(loc.x, loc.y);
	}

	public final long getZDeltaSq(int z)
	{
		long dz = z - getZ();
		return dz * dz;
	}

	public final long getZDeltaSq(Location loc)
	{
		return getZDeltaSq(loc.z);
	}

	public final long getXYZDeltaSq(int x, int y, int z)
	{
		return getXYDeltaSq(x, y) + getZDeltaSq(z);
	}

	public final long getXYZDeltaSq(Location loc)
	{
		return getXYDeltaSq(loc.x, loc.y) + getZDeltaSq(loc.z);
	}

	public final int getDistance(int x, int y)
	{
		return (int) Math.sqrt(getXYDeltaSq(x, y));
	}

	public final int getDistance(int x, int y, int z)
	{
		return (int) Math.sqrt(getXYZDeltaSq(x, y, z));
	}

	public final int getDistance(Location loc)
	{
		return getDistance(loc.x, loc.y, loc.z);
	}

	/**
	 * Проверяет в досягаемости расстояния ли объект
	 * @param obj проверяемый объект
	 * @param range расстояние
	 * @return true, если объект досягаем
	 */
	public final boolean isInRange(GameObject obj, int range)
	{
		if(obj == null)
			return false;
		if(obj.getReflection() != getReflection())
			return false;
		int dx = Math.abs(obj.getX() - getX());
		if(dx > range)
			return false;
		int dy = Math.abs(obj.getY() - getY());
		if(dy > range)
			return false;
		int dz = Math.abs(obj.getZ() - getZ());
		return dz <= 1500 && dx * dx + dy * dy <= (long) range * range;
	}

	public final boolean isInRangeZ(GameObject obj, int range)
	{
		if(obj == null)
			return false;
		if(obj.getReflection() != getReflection())
			return false;
		int dx = Math.abs(obj.getX() - getX());
		if(dx > range)
			return false;
		int dy = Math.abs(obj.getY() - getY());
		if(dy > range)
			return false;
		int dz = Math.abs(obj.getZ() - getZ());
		return dz <= range && dx * dx + dy * dy + dz * dz <= (long) range * range;
	}

	public final boolean isInRange(Location loc, int range)
	{
		return isInRangeSq(loc, (long) range * range);
	}

	public final boolean isInRangeSq(Location loc, long range)
	{
		return getXYDeltaSq(loc) <= range;
	}

	public final boolean isInRangeZ(Location loc, int range)
	{
		return isInRangeZSq(loc, (long) range * range);
	}

	public final boolean isInRangeZSq(Location loc, long range)
	{
		return getXYZDeltaSq(loc) <= range;
	}

	public final int getDistance(GameObject obj)
	{
		if(obj == null)
			return 0;
		return (int) Math.sqrt(getXYDeltaSq(obj.getX(), obj.getY()));
	}

	public final int getDistance3D(GameObject obj)
	{
		if(obj == null)
			return 0;
		return (int) Math.sqrt(getXYZDeltaSq(obj.getX(), obj.getY(), obj.getZ()));
	}

	public final int getRealDistance(GameObject obj)
	{
		return getRealDistance3D(obj, true);
	}

	public final int getRealDistance3D(GameObject obj)
	{
		return getRealDistance3D(obj, false);
	}

	public final int getRealDistance3D(GameObject obj, boolean ignoreZ)
	{
		int distance = ignoreZ ? getDistance(obj) : getDistance3D(obj);
		if(isCreature())
			distance -= ((Creature) this).getCurrentCollisionRadius();
		if(obj.isCreature())
			distance -= ((Creature) obj).getCurrentCollisionRadius();
		return distance > 0 ? distance : 0;
	}

	public final long getSqDistance(int x, int y)
	{
		return getXYDeltaSq(x, y);
	}

	public final long getSqDistance(GameObject obj)
	{
		if(obj == null)
			return 0;
		return getXYDeltaSq(obj.getLoc());
	}

	/**
	 * Возвращает L2Player управляющий даным обьектом.<BR>
	 * <li>Для L2Player это сам игрок.</li>
	 * <li>Для L2Summon это его хозяин.</li><BR><BR>
	 * @return L2Player управляющий даным обьектом.
	 */
	public Player getPlayer()
	{
		return null;
	}
	public final void setHeading(int h)
	{
		if(h < 0)
			h = h + 1 + Integer.MAX_VALUE & 0xFFFF;
		else if(h > 0xFFFF)
			h &= 0xFFFF;
		_h = h;
	}

	public int getHeading()
	{
		return _h;
	}

	public int getWalkSpeed()
	{
		return 0;
	}

	public int getMoveSpeed()
	{
		return 0;
	}

	public WorldRegion getCurrentRegion()
	{
		return _currentRegion;
	}

	public void setCurrentRegion(WorldRegion region)
	{
		_currentRegion = region;
	}

	public boolean isInObserverMode()
	{
		return false;
	}

	public boolean isInOlympiadMode()
	{
		return false;
	}

	public boolean isInBoat()
	{
		return false;
	}

	public boolean isFlying()
	{
		return false;
	}

	public boolean isInWater()
	{
		return false;
	}

	public double getCollisionRadius()
	{
		_log.warn("getCollisionRadius called directly from GameObject");
		Thread.dumpStack();
		return 0;
	}

	public double getCollisionHeight()
	{
		_log.warn("getCollisionHeight called directly from GameObject");
		Thread.dumpStack();
		return 0;
	}

	public double getCurrentCollisionRadius()
	{
		return getCollisionRadius();
	}

	public double getCurrentCollisionHeight()
	{
		return getCollisionHeight();
	}

	public boolean isCreature()
	{
		return false;
	}

	public boolean isPlayable()
	{
		return false;
	}

	public boolean isPlayer()
	{
		return false;
	}

	public boolean isPet()
	{
		return false;
	}

	public boolean isSummon()
	{
		return false;
	}

	public boolean isServitor()
	{
		return false;
	}

	public boolean isNpc()
	{
		return false;
	}

	public boolean isMonster()
	{
		return false;
	}
	
	public boolean isItem()
	{
		return false;
	}
	/**
	 * True для L2RaidBossInstance, но False для KamalokaBossInstance
	 */
	public boolean isRaid()
	{
		return false;
	}
	public boolean isChampion()
	{
		return false;
	}

	/**
	 * True для L2BossInstance
	 */
	public boolean isBoss()
	{
		return false;
	}

	/**
	 * True для L2TrapInstance
	 */
	public boolean isTrap()
	{
		return false;
	}

	public boolean isDoor()
	{
		return false;
	}

	/**
	 * True для L2ArtefactInstance
	 */
	public boolean isArtefact()
	{
		return false;
	}

	/**
	 * True для L2SiegeGuardInstance
	 */
	public boolean isSiegeGuard()
	{
		return false;
	}

	public boolean isClanAirShip()
	{
		return false;
	}

	public boolean isAirShip()
	{
		return false;
	}

	public boolean isBoat()
	{
		return false;
	}

	public boolean isVehicle()
	{
		return false;
	}

	public boolean isMinion()
	{
		return false;
	}

	public String getName()
	{
		return getClass().getSimpleName() + ":" + objectId;
	}

	public String dump()
	{
		return dump(true);
	}

	public String dump(boolean simpleTypes)
	{
		return Util.dumpObject(this, simpleTypes, true, true);
	}

	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		return Collections.emptyList();
	}

	public List<L2GameServerPacket> deletePacketList()
	{
		return Collections.<L2GameServerPacket> singletonList(new DeleteObjectPacket(this));
	}

	@Override
	public void addEvent(Event event)
	{
		event.onAddEvent(this);

		super.addEvent(event);
	}

	@Override
	public void removeEvent(Event event)
	{
		event.onRemoveEvent(this);

		super.removeEvent(event);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj == this)
			return true;
		if(obj == null)
			return false;
		if(obj.getClass() != getClass())
			return false;
		return ((GameObject) obj).getObjectId() == getObjectId();
	}

	public void doDie(Creature killer)
	{}
	
	public void teleToLocation(Location loc)
	{
	}	

	public boolean isFence()
	{
		return false;
	}

	protected Shape makeGeoShape()
	{
		return null;
	}

	@Override
	public Shape getGeoShape()
	{
		return _geoShape;
	}

	public void setGeoShape(Shape shape)
	{
		_geoShape = shape;
	}

	@Override
	public TIntObjectMap<ByteObjectPair<GeoEngine.CeilGeoControlType>> getGeoAround()
	{
		return _geoAround;
	}

	@Override
	public void setGeoAround(TIntObjectMap<ByteObjectPair<GeoEngine.CeilGeoControlType>> value)
	{
		_geoAround = value;
	}

	protected boolean isGeoControlEnabled()
	{
		return false;
	}

	protected final void refreshGeoControl()
	{
		_geoLock.lock();
		try
		{
			deactivateGeoControl();
			setGeoAround(null);
			setGeoShape(null);
			activateGeoControl();
		}
		finally
		{
			_geoLock.unlock();
		}
	}

	public final boolean isGeoControlActivated()
	{
		return _geoControlIndex > 0;
	}

	public final boolean activateGeoControl()
	{
		if(!Config.ALLOW_GEODATA)
			return true;

		_geoLock.lock();
		try
		{
			if(!isGeoControlEnabled())
				return false;

			if(isGeoControlActivated())
				return false;

			if(!isVisible())
				return false;

			if(getGeoShape() == null)
			{
				Shape shape = makeGeoShape();
				if(shape == null)
					return false;

				setGeoShape(shape);
			}

			int geoIndex = getGeoIndex();

			if(!GeoEngine.applyGeoControl(this, geoIndex))
				return false;

			_geoControlIndex = geoIndex;
			return true;
		}
		finally
		{
			_geoLock.unlock();
		}
	}

	public final boolean deactivateGeoControl()
	{
		if(!Config.ALLOW_GEODATA)
			return true;

		_geoLock.lock();
		try
		{
			if(!isGeoControlActivated())
				return false;

			if(!GeoEngine.returnGeoControl(this))
				return false;

			_geoControlIndex = 0;
			return true;
		}
		finally
		{
			_geoLock.unlock();
		}
	}

	@Override
	public final int getGeoControlIndex() {
		return _geoControlIndex;
	}

	@Override
	public boolean isHollowGeo()
	{
		return true;
	}
}
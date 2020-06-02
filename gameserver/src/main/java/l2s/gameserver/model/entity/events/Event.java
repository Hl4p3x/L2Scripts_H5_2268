package l2s.gameserver.model.entity.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.napile.primitive.Containers;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.napile.primitive.maps.impl.TreeIntObjectMap;
import org.napile.primitive.pair.IntObjectPair;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.listener.Listener;
import l2s.commons.listener.ListenerList;
import l2s.commons.logging.LoggerObject;
import l2s.gameserver.Config;
import l2s.gameserver.dao.ItemsDAO;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.listener.event.OnStartStopListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.model.entity.events.impl.FortressSiegeEvent;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.events.objects.DoorObject;
import l2s.gameserver.model.entity.events.objects.InitableObject;
import l2s.gameserver.model.entity.events.objects.SpawnableObject;
import l2s.gameserver.model.entity.events.objects.ZoneObject;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.IStaticPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.taskmanager.actionrunner.ActionRunner;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.TimeUtils;


/**
 * @author VISTALL
 * @date 12:54/10.12.2010
 */
public abstract class Event extends LoggerObject
{
	private class ListenerListImpl extends ListenerList<Event>
	{
		public void onStart()
		{
			for(Listener<Event> listener : getListeners())
				if(OnStartStopListener.class.isInstance(listener))
					((OnStartStopListener) listener).onStart(Event.this);
		}

		public void onStop()
		{
			for(Listener<Event> listener : getListeners())
				if(OnStartStopListener.class.isInstance(listener))
					((OnStartStopListener) listener).onStop(Event.this);
		}
	}

	public static final String EVENT = "event";

	// actions
	protected final IntObjectMap<List<EventAction>> _onTimeActions = new TreeIntObjectMap<List<EventAction>>();
	protected final List<EventAction> _onStartActions = new ArrayList<EventAction>(0);
	protected final List<EventAction> _onStopActions = new ArrayList<EventAction>(0);
	protected final List<EventAction> _onInitActions = new ArrayList<EventAction>(0);
	// objects
	protected final Map<Object, List<Object>> _objects = new HashMap<Object, List<Object>>(0);

	protected final int _id;
	protected final String _name;
	protected final String _timerName;

	protected final ListenerListImpl _listenerList = new ListenerListImpl();

	protected IntObjectMap<ItemInstance> _banishedItems = Containers.emptyIntObjectMap();

	protected Event(MultiValueSet<String> set)
	{
		this(set.getInteger("id"), set.getString("name"));
	}

	protected Event(int id, String name)
	{
		_id = id;
		_name = name;
		_timerName = id + "_" + name.toLowerCase().replace(" ", "_");
	}

	public void initEvent()
	{
		callActions(_onInitActions);

		reCalcNextTime(true);

		printInfo();
	}

	public void startEvent()
	{
		callActions(_onStartActions);

		_listenerList.onStart();
	}

	public void stopEvent(boolean force)
	{
		callActions(_onStopActions);

		_listenerList.onStop();
	}

	protected void printInfo()
	{
		info(getName() + " time - " + TimeUtils.toSimpleFormat(startTimeMillis()));
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + getId() + ";" + getName() + "]";
	}
	//===============================================================================================================
	//												Actions
	//===============================================================================================================

	protected void callActions(List<EventAction> actions)
	{
		for(EventAction action : actions)
			action.call(this);
	}

	public void addOnStartActions(List<EventAction> start)
	{
		_onStartActions.addAll(start);
	}

	public void addOnStopActions(List<EventAction> start)
	{
		_onStopActions.addAll(start);
	}

	public void addOnInitActions(List<EventAction> start)
	{
		_onInitActions.addAll(start);
	}

	public void addOnTimeAction(int time, EventAction action)
	{
		List<EventAction> list = _onTimeActions.get(time);
		if(list != null)
			list.add(action);
		else
		{
			List<EventAction> actions = new ArrayList<EventAction>(1);
			actions.add(action);
			_onTimeActions.put(time, actions);
		}
	}

	public void addOnTimeActions(int time, List<EventAction> actions)
	{
		if(actions.isEmpty())
			return;

		List<EventAction> list = _onTimeActions.get(time);
		if(list != null)
			list.addAll(actions);
		else
			_onTimeActions.put(time, new ArrayList<EventAction>(actions));
	}

	public void timeActions(int time)
	{
		List<EventAction> actions = _onTimeActions.get(time);
		if(actions == null)
		{
			info("Undefined time : " + time);
			return;
		}

		callActions(actions);
	}

	public int[] timeActions()
	{
		return _onTimeActions.keySet().toArray();
	}

	//===============================================================================================================
	//												Tasks
	//===============================================================================================================

	public void registerActions()
	{
		long t = startTimeMillis();
		if(t == 0)
			return;

		for(int key : _onTimeActions.keySet().toArray())
			ActionRunner.getInstance().register(t + key * 1000L, new EventWrapper(_timerName, this, key));
	}

	public void clearActions()
	{
		ActionRunner.getInstance().clear(_timerName);
	}

	//===============================================================================================================
	//												Objects
	//===============================================================================================================

	public boolean containsObjects(Object name)
	{
		return _objects.get(name) != null;
	}

	@SuppressWarnings("unchecked")
	public <O extends Object> List<O> getObjects(Object name)
	{
		List<Object> objects = _objects.get(name);
		return objects == null ? Collections.<O>emptyList() : (List<O>)objects;
	}

	@SuppressWarnings("unchecked")
	public <O extends Object> O getFirstObject(Object name)
	{
		List<O> objects = getObjects(name);
		return objects.size() > 0 ? objects.get(0) : null;
	}

	public void addObject(Object name, Object object)
	{
		if(object == null)
			return;

		List<Object> list = _objects.get(name);
		if(list != null)
		{
			list.add(object);
		}
		else
		{
			list = new CopyOnWriteArrayList<Object>();
			list.add(object);
			_objects.put(name, list);
		}
	}

	public void removeObject(Object name, Object o)
	{
		if(o == null)
			return;

		List<Object> list = _objects.get(name);
		if(list != null)
			list.remove(o);
	}

	@SuppressWarnings("unchecked")
	public <O extends Object> List<O> removeObjects(Object name)
	{
		List<Object> objects = _objects.remove(name);
		return objects == null ? Collections.<O>emptyList() : (List<O>)objects;
	}

	@SuppressWarnings("unchecked")
	public void addObjects(Object name, List<? extends Object> objects)
	{
		if(objects.isEmpty())
			return;

		List<Object> list = _objects.get(name);
		if(list != null)
			list.addAll(objects);
		else
			_objects.put(name,(List<Object>)objects);
	}


	public Map<Object, List<Object>> getObjects()
	{
		return _objects;
	}

	public void spawnAction(Object name, boolean spawn)
	{
		List<Object> objects = getObjects(name);
		if(objects.isEmpty())
		{
			info("Undefined objects: " + name);
			return;
		}

		for(Object object : objects)
			if(object instanceof SpawnableObject)
			{
				if(spawn)
					((SpawnableObject) object).spawnObject(this);
				else
					((SpawnableObject) object).despawnObject(this);
			}
	}

	public void doorAction(Object name, boolean open)
	{
		List<Object> objects = getObjects(name);
		if(objects.isEmpty())
		{
			info("Undefined objects: " + name);
			return;
		}

		for(Object object : objects)
			if(object instanceof DoorObject)
			{
				if(open)
					((DoorObject) object).open(this);
				else
					((DoorObject) object).close(this);
			}
	}

	public void zoneAction(Object name, boolean active)
	{
		List<Object> objects = getObjects(name);
		if(objects.isEmpty())
		{
			info("Undefined objects: " + name);
			return;
		}

		for(Object object : objects)
			if(object instanceof ZoneObject)
				((ZoneObject) object).setActive(active, this);
	}

	public void initAction(Object name)
	{
		List<Object> objects = getObjects(name);
		if(objects.isEmpty())
		{
			info("Undefined objects: " + name);
			return;
		}

		for(Object object : objects)
			if(object instanceof InitableObject)
				((InitableObject) object).initObject(this);
	}

	public void action(String name, boolean start)
	{
		if(name.equalsIgnoreCase(EVENT))
		{
			if(start)
				startEvent();
			else
				stopEvent(false);
		}
	}

	public void refreshAction(Object name)
	{
		List<Object> objects = getObjects(name);
		if(objects.isEmpty())
		{
			info("Undefined objects: " + name);
			return;
		}

		for(Object object : objects)
			if(object instanceof SpawnableObject)
				((SpawnableObject) object).refreshObject(this);
	}
	//===============================================================================================================
	//												Abstracts
	//===============================================================================================================

	public abstract void reCalcNextTime(boolean onInit);

	public abstract EventType getType();

	protected abstract long startTimeMillis();

	//===============================================================================================================
	//												Broadcast
	//===============================================================================================================
	public void broadcastToWorld(IStaticPacket packet)
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			if(player != null)
				player.sendPacket(packet);
	}

	public void broadcastToWorld(L2GameServerPacket packet)
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			if(player != null)
				player.sendPacket(packet);
	}
	//===============================================================================================================
	//												Getters & Setters
	//===============================================================================================================
	public int getId()
	{
		return _id;
	}

	public String getName()
	{
		return _name;
	}

	public GameObject getCenterObject()
	{
		return null;
	}

	public Reflection getReflection()
	{
		return ReflectionManager.DEFAULT;
	}

	public int getRelation(Player thisPlayer, Player target, int oldRelation)
	{
		return oldRelation;
	}

	public int getUserRelation(Player thisPlayer, int oldRelation)
	{
		return oldRelation;
	}

	public void checkRestartLocs(Player player, Map<RestartType, Boolean> r)
	{
		//
	}

	public Location getRestartLoc(Player player, RestartType type)
	{
		return null;
	}

	public boolean canAttack(Creature target, Creature attacker, Skill skill, boolean force)
	{
		return false;
	}

	public SystemMsg checkForAttack(Creature target, Creature attacker, Skill skill, boolean force)
	{
		return null;
	}

	public boolean isInProgress()
	{
		return false;
	}

	public void findEvent(Player player)
	{
		//
	}

	public void announce(int id, String value, int time)
	{
		throw new UnsupportedOperationException();
	}

	public void teleportPlayers(String teleportWho)
	{
		throw new UnsupportedOperationException();
	}

	public boolean ifVar(String name)
	{
		throw new UnsupportedOperationException();
	}

	public List<Player> itemObtainPlayers()
	{
		throw new UnsupportedOperationException();
	}

	public void giveItem(Player player, int itemId, long count)
	{
		switch(itemId)
		{
			case ItemTemplate.ITEM_ID_FAME:
				if(Config.ENABLE_ALT_FAME_REWARD)
				{
					if ((this instanceof CastleSiegeEvent))
						count = Config.ALT_FAME_CASTLE;
					else if ((this instanceof FortressSiegeEvent))
						count = Config.ALT_FAME_FORTRESS;
				}
				double fameMod = 1.0;
				if((this instanceof SiegeEvent))
					fameMod = player.getPremiumAccount().getRates().getSiegeFameReward();
				player.setFame(player.getFame() + (int) (fameMod * count), toString());
				break;
			default:
				ItemFunctions.addItem(player, itemId, count, "Give item by '" + getName() + "' [" + getId() + "] event");
				break;
		}
	}

	public List<Player> broadcastPlayers(int range)
	{
		throw new UnsupportedOperationException();
	}

	public boolean canRessurect(Player resurrectPlayer, Creature creature, boolean force)
	{
		return true;
	}
	//===============================================================================================================
	//											setEvent helper
	//===============================================================================================================
	public void onAddEvent(GameObject o)
	{
		//
	}

	public void onRemoveEvent(GameObject o)
	{
		//
	}
	//===============================================================================================================
	//											Banish items
	//===============================================================================================================
	public void addBanishItem(ItemInstance item)
	{
		if(_banishedItems.isEmpty())
			_banishedItems = new CHashIntObjectMap<ItemInstance>();

		_banishedItems.put(item.getObjectId(), item);
	}

	public void removeBanishItems()
	{
		Iterator<IntObjectPair<ItemInstance>> iterator = _banishedItems.entrySet().iterator();
		while(iterator.hasNext())
		{
			IntObjectPair<ItemInstance> entry = iterator.next();
			iterator.remove();

			ItemInstance item = ItemsDAO.getInstance().load(entry.getKey());
			if(item != null)
			{
				if(item.getOwnerId() > 0)
				{
					GameObject object = GameObjectsStorage.findObject(item.getOwnerId());
					if(object != null && object.isPlayable())
					{
						((Playable)object).getInventory().destroyItem(item);
						object.getPlayer().sendPacket(SystemMessagePacket.removeItems(item));
					}
				}
				item.delete();
			}
			else
				item = entry.getValue();

			item.deleteMe();
		}
	}
	//===============================================================================================================
	//											 Listeners
	//===============================================================================================================
	public void addListener(Listener<Event> l)
	{
		_listenerList.add(l);
	}

	public void removeListener(Listener<Event> l)
	{
		_listenerList.remove(l);
	}
	//===============================================================================================================
	//											Object
	//===============================================================================================================
	public void cloneTo(Event e)
	{
		for(EventAction a : _onInitActions)
			e._onInitActions.add(a);

		for(EventAction a : _onStartActions)
			e._onStartActions.add(a);

		for(EventAction a : _onStopActions)
			e._onStopActions.add(a);

		for(IntObjectPair<List<EventAction>> entry : _onTimeActions.entrySet())
			e.addOnTimeActions(entry.getKey(), entry.getValue());
	}

	public Boolean isInvisible(Creature creature, GameObject observer)
	{
		return null;
	}

	public final long getForceStartTime()
	{
		int minTime = 0;
		for(int time : timeActions())
		{
			if(time < minTime)
				minTime = time;
		}
		return System.currentTimeMillis() + (Math.abs(minTime) * 1000L) + 1000L;
	}

	public boolean isForceScheduled()
	{
		return false;
	}

	public boolean forceScheduleEvent()
	{
		return false;
	}

	public boolean forceCancelEvent()
	{
		return false;
	}
}

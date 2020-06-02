package l2s.gameserver.instancemanager.naia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.ReflectionUtils;

/**
 * @author pchayka
 */
public final class NaiaTowerManager
{
	private static final Logger _log = LoggerFactory.getLogger(NaiaTowerManager.class);

	private static Map<Integer, List<Player>> _groupList = new HashMap<Integer, List<Player>>();
	private static Map<Integer, List<Player>> _roomsDone = new HashMap<Integer, List<Player>>();
	private static Map<Integer, Long> _groupTimer = new HashMap<Integer, Long>();
	private static Map<Integer, List<NpcInstance>> _roomMobs;
	private static List<NpcInstance> _roomMobList;
	private static long _towerAccessible = 0;
	private static int _index = 0;
	public static HashMap<Integer, Boolean> lockedRooms;
	private static final NaiaTowerManager _instance = new NaiaTowerManager();

	public static final NaiaTowerManager getInstance()
	{
		return _instance;
	}

	private NaiaTowerManager()
	{
		if(lockedRooms == null)
		{
			lockedRooms = new HashMap<Integer, Boolean>();
			for(int i = 18494; i <= 18505; i++)
				lockedRooms.put(i, false);

			_roomMobs = new HashMap<Integer, List<NpcInstance>>();
			for(int i = 18494; i <= 18505; i++)
			{
				_roomMobList = new ArrayList<NpcInstance>();
				_roomMobs.put(i, _roomMobList);
			}

			_log.info("Naia Tower Manager: Loaded 12 rooms");
		}
		ThreadPoolManager.getInstance().schedule(new GroupTowerTimer(), 30 * 1000L);
	}

	public static void startNaiaTower(Player leader)
	{
		if(leader == null)
			return;

		if(_towerAccessible > System.currentTimeMillis())
			return;

		for(Player member : leader.getParty().getPartyMembers())
			member.teleToLocation(new Location(-47271, 246098, -9120));

		addGroupToTower(leader);
		_towerAccessible += 20 * 60 * 1000L;

		ReflectionUtils.getDoor(18250001).openMe();
	}

	private static void addGroupToTower(Player leader)
	{
		_index = _groupList.keySet().size() + 1;
		_groupList.put(_index, leader.getParty().getPartyMembers());
		_groupTimer.put(_index, System.currentTimeMillis() + 5 * 60 * 1000L);
		
		leader.sendMessage(new CustomMessage("common.NaiaTower.CountDown", leader));
	}

	public static void updateGroupTimer(Player player)
	{
		for(int i : _groupList.keySet())
			if(_groupList.get(i).contains(player))
			{
				_groupTimer.put(i, System.currentTimeMillis() + 5 * 60 * 1000L);
				player.sendMessage(new CustomMessage("common.NaiaTower.GroupTimer", player));
				break;
			}
	}

	public static void removeGroupTimer(Player player)
	{
		for(int i : _groupList.keySet())
			if(_groupList.get(i).contains(player))
			{
				_groupList.remove(i);
				_groupTimer.remove(i);
			}
	}

	public static boolean isLegalGroup(Player player)
	{
		if(_groupList == null || _groupList.isEmpty())
			return false;

		for(int i : _groupList.keySet())
			if(_groupList.get(i).contains(player))
				return true;

		return false;
	}

	public static void lockRoom(int npcId)
	{
		lockedRooms.put(npcId, true);
	}

	public static void unlockRoom(int npcId)
	{
		lockedRooms.put(npcId, false);
	}

	public static boolean isLockedRoom(int npcId)
	{
		return lockedRooms.get(npcId);
	}

	public static void addRoomDone(int roomId, Player player)
	{
		if(player.getParty() != null)
			_roomsDone.put(roomId, player.getParty().getPartyMembers());
	}

	public static boolean isRoomDone(int roomId, Player player)
	{
		if(_roomsDone == null || _roomsDone.isEmpty())
			return false;

		if(_roomsDone.get(roomId) == null || _roomsDone.get(roomId).isEmpty())
			return false;

		if(_roomsDone.get(roomId).contains(player))
			return true;

		return false;
	}

	public static void addMobsToRoom(int roomId, List<NpcInstance> mob)
	{
		_roomMobs.put(roomId, mob);
	}

	public static List<NpcInstance> getRoomMobs(int roomId)
	{
		return _roomMobs.get(roomId);
	}

	public static void removeRoomMobs(int roomId)
	{
		_roomMobs.get(roomId).clear();
	}

	private class GroupTowerTimer extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			ThreadPoolManager.getInstance().schedule(new GroupTowerTimer(), 30 * 1000L);
			if(!_groupList.isEmpty() && !_groupTimer.isEmpty())
				for(int i : _groupTimer.keySet())
					if(_groupTimer.get(i) < System.currentTimeMillis())
					{
						for(Player kicked : _groupList.get(i))
						{
							kicked.teleToLocation(new Location(17656, 244328, 11595));
							kicked.sendMessage(new CustomMessage("common.NaiaTower.Kick", kicked));
						}
						_groupList.remove(i);
						_groupTimer.remove(i);
					}
		}
	}

}
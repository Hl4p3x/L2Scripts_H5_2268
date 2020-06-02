package l2s.gameserver.model.entity.events.fightclubmanager;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.AbstractFightClub;
import l2s.gameserver.model.entity.events.impl.DuelEvent;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ConfirmDlgPacket;
import l2s.gameserver.network.l2.s2c.SayPacket2;
import l2s.gameserver.network.l2.s2c.ShowTutorialMarkPacket;
import l2s.gameserver.network.l2.s2c.TutorialCloseHtmlPacket;
import l2s.gameserver.network.l2.s2c.TutorialShowHtmlPacket;
import l2s.gameserver.utils.Location;

public class FightClubEventManager
{
	private static FightClubEventManager _instance;
	public static final Location RETURN_LOC = new Location(83208, 147672, -3494, 0);
	public static final int FIGHT_CLUB_BADGE_ID = 6673;
	public static final String BYPASS = "_fightclub";
	private Map<Integer, AbstractFightClub> _activeEvents = new ConcurrentHashMap<Integer, AbstractFightClub>();
	private List<FightClubGameRoom> _rooms = new CopyOnWriteArrayList<FightClubGameRoom>();
	private boolean _shutDown = false;
	private AbstractFightClub _nextEvent = null;
	private static Map<Long, String> boxes = new LinkedHashMap<Long, String>();

	public FightClubEventManager()
	{
		startAutoEventsTasks();
	}

	public boolean serverShuttingDown()
	{
		return _shutDown;
	}

	public void signForEvent(Player player, AbstractFightClub event)
	{
		FightClubGameRoom roomFound = null;

		for(FightClubGameRoom room : getEventRooms(event))
		{
			if(room.getSlotsLeft() > 0)
			{
				roomFound = room;
				break;
			}
		}

		if(roomFound == null)
		{
			AbstractFightClub duplicatedEvent = prepareNewEvent(event);
			roomFound = createRoom(duplicatedEvent);
		}

		roomFound.addAlonePlayer(player);
		if("HWid".equalsIgnoreCase(Config.EVENT_TvTCheckWindowMethod))
			boxes.put(player.getStoredId(), player.getHWID());		

		player.sendMessage("You just participated to " + event.getName() + " Event!"); // TODO: Вынести в ДП.
	}

	public void trySignForEvent(Player player, AbstractFightClub event, boolean checkConditions)
	{
		if(checkConditions && !canPlayerParticipate(player, true, false))
			return;

		if(!isRegistrationOpened(event))
			player.sendMessage("You cannot participate in " + event.getName() + " right now!"); // TODO: Вынести в ДП.
		else if(isPlayerRegistered(player))
			player.sendMessage("You are already registered in event!"); // TODO: Вынести в ДП.
		else
			signForEvent(player, event);
	}

	public void unsignFromEvent(Player player)
	{
		for(FightClubGameRoom room : _rooms)
		{
			if(room.containsPlayer(player))
				room.leaveRoom(player);
		}
		if(boxes.containsKey(player.getStoredId()))
			boxes.remove(player.getStoredId());		
		player.sendMessage("You were unregistered from Event!"); // TODO: Вынести в ДП.
	}

	public boolean isRegistrationOpened(AbstractFightClub event)
	{
		for(FightClubGameRoom room : _rooms)
		{
			if(room.getGame() != null && room.getGame().getEventId() == event.getEventId())
				return true;
		}
		return false;
	}

	public boolean isPlayerRegistered(Player player)
	{
		if(player.isInFightClub())
			return true;

		for(FightClubGameRoom room : _rooms)
		{
			for(Player iPlayer : room.getAllPlayers())
			{
				if(iPlayer.equals(player))
					return true;
			}
		}
		return false;
	}

	public void startEventCountdown(AbstractFightClub event)
	{
		FightClubLastStatsManager.getInstance().clearStats();
		_nextEvent = event;

		final AbstractFightClub duplicatedEvent = prepareNewEvent(event);
		createRoom(duplicatedEvent);

		sendToAllMsg(duplicatedEvent, "Registration to " + duplicatedEvent.getName() + " started!"); // TODO: Вынести в ДП.
		sendEventInvitations(event);

		ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			@Override
			public void runImpl() throws Exception
			{
				sendToAllMsg(duplicatedEvent, duplicatedEvent.getName() + " Event will start in 3 minutes!"); // TODO: Вынести в ДП.
				try
				{
					Thread.sleep(180000L);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}

				sendToAllMsg(duplicatedEvent, duplicatedEvent.getName() + " Event will start in 1 minute!"); // TODO: Вынести в ДП.
				FightClubEventManager.this.notifyConditions(duplicatedEvent);
				try
				{
					Thread.sleep(45000L);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}

				sendToAllMsg(duplicatedEvent, duplicatedEvent.getName() + " Event will start in 15 seconds!"); // TODO: Вынести в ДП.
				FightClubEventManager.this.notifyConditions(duplicatedEvent);
				try
				{
					Thread.sleep(15000L);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}

				sendToAllMsg(duplicatedEvent, duplicatedEvent.getName() + " Event Started!"); // TODO: Вынести в ДП.
				FightClubEventManager.this.startEvent(duplicatedEvent);
			}
		}
		, 120000L);
	}

	private void notifyConditions(AbstractFightClub event)
	{
		for(FightClubGameRoom room : getEventRooms(event))
		{
			for(Player player : room.getAllPlayers())
			{
				canPlayerParticipate(player, true, false);
			}
		}
	}

	private void startEvent(AbstractFightClub event)
	{
		List<FightClubGameRoom> eventRooms = getEventRooms(event);

		for(FightClubGameRoom room : eventRooms)
		{
			_rooms.remove(room);
			if(room.getPlayersCount() < 2)
				continue;
			room.getGame().prepareEvent(room);
		}
	}

	private void equalizeRooms(List<FightClubGameRoom> eventRooms)
	{
		double players = 0.0D;
		for(FightClubGameRoom room : eventRooms) {
			players += room.getPlayersCount();
		}

		double avarage = players / eventRooms.size();

		List<Player> playersToChange = new ArrayList<Player>();

		for(FightClubGameRoom room : eventRooms)
		{
			int toRemove = room.getPlayersCount() - (int)Math.ceil(avarage);
			for(int i = 0; i < toRemove; i++)
			{
				Player player = room.getAllPlayers().get(0);
				room.leaveRoom(player);
				playersToChange.add(player);
			}

		}

		for(FightClubGameRoom room : eventRooms)
		{
			int toAdd = (int)Math.floor(avarage) - room.getPlayersCount();

			for(int i = 0; i < toAdd; i++)
			{
				Player player = playersToChange.remove(0);
				room.addAlonePlayer(player);
			}
		}
	}

	private List<FightClubGameRoom> getEventRooms(AbstractFightClub event)
	{
		List<FightClubGameRoom> eventRooms = new ArrayList<FightClubGameRoom>();

		for(FightClubGameRoom room : _rooms)
		{
			if(room.getGame() != null && room.getGame().getEventId() == event.getEventId())
				eventRooms.add(room);
		}
		return eventRooms;
	}

	private void sendEventInvitations(AbstractFightClub event)
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(canPlayerParticipate(player, false, true) && player.getEvent(AbstractFightClub.class) == null)
				player.ask(new ConfirmDlgPacket(SystemMsg.S1, 60000).addString("Would you like to join " + event.getName() + " event?"), new AnswerEventInvitation(player, event)); // TODO: Вынести в ДП.
		}
	}

	public FightClubGameRoom createRoom(AbstractFightClub event)
	{
		FightClubGameRoom newRoom = new FightClubGameRoom(event);
		_rooms.add(newRoom);
		return newRoom;
	}

	public AbstractFightClub getNextEvent()
	{
		return _nextEvent;
	}

	private void sendErrorMessageToPlayer(Player player, String msg)
	{
		player.sendPacket(new SayPacket2(player.getObjectId(), ChatType.COMMANDCHANNEL_ALL, "Error", msg)); // TODO: Вынести в ДП.
		player.sendMessage(msg);
	}

	public void sendToAllMsg(AbstractFightClub event, String msg)
	{
		SayPacket2 packet = new SayPacket2(0, ChatType.CRITICAL_ANNOUNCE, event.getName(), msg);
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			player.sendPacket(packet);
	}

	private AbstractFightClub prepareNewEvent(AbstractFightClub event)
	{
		MultiValueSet<String> set = event.getSet();
		AbstractFightClub duplicatedEvent = null;
		try
		{
			Class<?> eventClass = Class.forName(set.getString("eventClass"));
			Constructor<?> constructor = eventClass.getConstructor(MultiValueSet.class);
			duplicatedEvent = (AbstractFightClub) constructor.newInstance(set);

			duplicatedEvent.clearSet();

			_activeEvents.put(Integer.valueOf(duplicatedEvent.getObjectId()), duplicatedEvent);
		}
		catch(Exception e) 
		{
			e.printStackTrace();
		}

		return duplicatedEvent;
	}

	private void startAutoEventsTasks()
	{
		AbstractFightClub closestEvent = null;
		long closestEventTime = Long.MAX_VALUE;

		for(int i = 1; i <= EventHolder.FIGHT_CLUB_EVENTS; i++)
		{
			AbstractFightClub event = (AbstractFightClub) EventHolder.getInstance().getEvent(EventType.FIGHT_CLUB_EVENT, i);

			if(!event.isAutoTimed())
				continue;

			Calendar nextEventDate = getClosestEventDate(event.getAutoStartTimes());

			event.printScheduledTime(nextEventDate.getTimeInMillis());

			ThreadPoolManager.getInstance().schedule(new EventRunThread(event), nextEventDate.getTimeInMillis() - System.currentTimeMillis());

			if(closestEventTime <= nextEventDate.getTimeInMillis())
				continue;

			closestEvent = event;
			closestEventTime = nextEventDate.getTimeInMillis();
		}

		_nextEvent = closestEvent;
	}

	private Calendar getClosestEventDate(int[][] dates)
	{
		Calendar tempCalendar = Calendar.getInstance();
		tempCalendar.set(Calendar.SECOND, 0);

		Calendar eventCalendar = Calendar.getInstance();

		boolean found = false;
		long smallest = Long.MAX_VALUE;

		for(int[] hourMin : dates)
		{
			tempCalendar.set(Calendar.HOUR_OF_DAY, hourMin[0]);
			tempCalendar.set(Calendar.MINUTE, hourMin[1]);

			long timeInMillis = tempCalendar.getTimeInMillis();
			if(timeInMillis < System.currentTimeMillis())
			{
				if(timeInMillis < smallest)
					smallest = timeInMillis;
			}
			else
			{
				if(found && timeInMillis >= eventCalendar.getTimeInMillis())
					continue;

				found = true;
				eventCalendar.setTimeInMillis(timeInMillis);
			}
		}

		if(!found)
			eventCalendar.setTimeInMillis(smallest + 86400000L);

		return eventCalendar;
	}

	public boolean canPlayerParticipate(Player player, boolean sendMessage, boolean justMostImportant)
	{
		if(player == null)
			return false;

		if(!player.getClassId().isOfLevel(ClassLevel.THIRD))
		{
			sendErrorMessageToPlayer(player, "Your class is too weak for the Fight Club!"); // TODO: Вынести в ДП.
			return false;
		}

		if(player.getCursedWeaponEquippedId() > 0)
		{
			if(sendMessage)
				sendErrorMessageToPlayer(player, "Cursed Weapon owners may not participate in Fight Club!"); // TODO: Вынести в ДП.
			return false;
		}

		if(Olympiad.isRegistered(player))
		{
			if(sendMessage)
				sendErrorMessageToPlayer(player, "Players registered to Olympiad Match may not participate in Fight Club!"); // TODO: Вынести в ДП.
			return false;
		}

		if(player.isInOlympiadMode() || player.getOlympiadGame() != null)
		{
			if(sendMessage)
				sendErrorMessageToPlayer(player, "Players fighting in Olympiad Match may not participate in Fight Club!"); // TODO: Вынести в ДП.
			return false;
		}

		if(player.isInObserverMode())
		{
			if(sendMessage)
				sendErrorMessageToPlayer(player, "Players in Observation mode may not participate in Fight Club!"); // TODO: Вынести в ДП.
			return false;
		}

		if(!justMostImportant)
		{
			if(player.isDead() || player.isAlikeDead())
			{
				if(sendMessage)
					sendErrorMessageToPlayer(player, "Dead players may not participate in Fight Club!"); // TODO: Вынести в ДП.
				return false;
			}

			if(player.isBlocked())
			{
				if(sendMessage)
					sendErrorMessageToPlayer(player, "Blocked players may not participate in Fight Club!"); // TODO: Вынести в ДП.
				return false;
			}

			if(!player.isInPeaceZone() && player.getPvpFlag() > 0)
			{
				if(sendMessage)
					sendErrorMessageToPlayer(player, "Players in PvP Battle may not participate in Fight Club!"); // TODO: Вынести в ДП.
				return false;
			}

			if(player.isInCombat())
			{
				if(sendMessage)
					sendErrorMessageToPlayer(player, "Players in Combat may not participate in Fight Club Battle!"); // TODO: Вынести в ДП.
				return false;
			}

			if(player.getEvent(DuelEvent.class) != null)
			{
				if(sendMessage)
					sendErrorMessageToPlayer(player, "Players engaged in Duel may not participate in Fight Club Battle!"); // TODO: Вынести в ДП.
				return false;
			}

			if(player.getKarma() > 0)
			{
				if(sendMessage)
					sendErrorMessageToPlayer(player, "Chaotic players may not participate in Fight Club!"); // TODO: Вынести в ДП.
				return false;
			}

			if(player.isInOfflineMode())
			{
				if(sendMessage)
					sendErrorMessageToPlayer(player, "Players in Offline mode may not participate in Fight Club!"); // TODO: Вынести в ДП.
				return false;
			}

			if(player.isInStoreMode())
			{
				if(sendMessage)
					sendErrorMessageToPlayer(player, "Players in Store mode may not participate in Fight Club!"); // TODO: Вынести в ДП.
				return false;
			}	
			if("HWid".equalsIgnoreCase(Config.EVENT_TvTCheckWindowMethod)) 
			{
				if(boxes.containsValue(player.getHWID())) 
				{
					sendErrorMessageToPlayer(player, "Only one box is allowed for this event!"); // TODO: Вынести в ДП.
					return false;
				}
			}			
		}

		return true;
	}

	public void requestEventPlayerMenuBypass(Player player, String bypass)
	{
		player.sendPacket(TutorialCloseHtmlPacket.STATIC);

		AbstractFightClub event = player.getFightClubEvent();
		if(event == null)
			return;

		FightClubPlayer fPlayer = event.getFightClubPlayer(player);
		if(fPlayer == null)
			return;

		fPlayer.setShowTutorial(false);

		if(!bypass.startsWith("_fightclub"))
			return;

		StringTokenizer st = new StringTokenizer(bypass, " ");
		st.nextToken();

		String action = st.nextToken();

		String str1 = action;
		int i = -1;

		switch(str1.hashCode())
		{
			case 102846135:
				if(!str1.equals("leave"))
					break;
				i = 0;
				break;
		}

		switch(i)
		{
			case 0:
				askQuestion(player, "Are you sure You want to leave the event?"); // TODO: Вынести в ДП.
				break;
		}
	}

	public void sendEventPlayerMenu(Player player)
	{
		AbstractFightClub event = player.getFightClubEvent();
		if(event == null || event.getFightClubPlayer(player) == null)
			return;

		FightClubPlayer fPlayer = event.getFightClubPlayer(player);

		fPlayer.setShowTutorial(true);

		StringBuilder builder = new StringBuilder();
		builder.append("<html><head><title>").append(event.getName()).append("</title></head>");
		builder.append("<body>");
		builder.append("<br1><img src=\"L2UI.squaregray\" width=\"290\" height=\"1\">");
		builder.append("<table height=20 fixwidth=\"290\" bgcolor=29241d>");
		builder.append("\t<tr>");
		builder.append("\t\t<td height=20 width=290>");
		builder.append("\t\t\t<center><font name=\"hs12\" color=913d3d>").append(event.getName()).append("</font></center>");
		builder.append("\t\t</td>");
		builder.append("\t</tr>");
		builder.append("</table>");
		builder.append("<br1><img src=\"L2UI.squaregray\" width=\"290\" height=\"1\">");
		builder.append("<br>");
		builder.append("<table fixwidth=290 bgcolor=29241d>");
		builder.append("\t<tr>");
		builder.append("\t\t<td valign=top width=280>");
		builder.append("\t\t\t<font color=388344>").append(event.getDescription()).append("<br></font>");
		builder.append("\t\t</td>");
		builder.append("\t</tr>");
		builder.append("</table>");
		builder.append("<br1><img src=\"L2UI.squaregray\" width=\"290\" height=\"1\">");
		builder.append("<br>");

		builder.append("<table width=270>");
		builder.append("\t<tr>");
		builder.append("\t\t<td>");
		builder.append("\t\t\t<center><button value = \"Leave Event\" action=\"bypass -h ").append("_fightclub").append(" leave\" back=\"l2ui_ct1.button.OlympiadWnd_DF_Back_Down\" width=200 height=30 fore=\"l2ui_ct1.button.OlympiadWnd_DF_Back\"></center>"); // TODO: Вынести в ДП.
		builder.append("\t\t</td>");
		builder.append("\t</tr>");
		builder.append("\t<tr>");
		builder.append("\t\t<td>");
		builder.append("\t\t\t<center><button value = \"Close\" action=\"bypass -h ").append("_fightclub").append(" close\" back=\"l2ui_ct1.button.OlympiadWnd_DF_Info_Down\" width=200 height=30 fore=\"l2ui_ct1.button.OlympiadWnd_DF_Info\"></center>"); // TODO: Вынести в ДП.
		builder.append("\t\t</td>");
		builder.append("\t</tr>");
		builder.append("</table>");

		builder.append("</body></html>");

		player.sendPacket(new TutorialShowHtmlPacket(TutorialShowHtmlPacket.NORMAL_WINDOW, builder.toString()));
		player.sendPacket(new ShowTutorialMarkPacket(false, 100));
	}

	private void leaveEvent(Player player)
	{
		AbstractFightClub event = player.getFightClubEvent();
		if(event == null)
			return;

		if(event.leaveEvent(player, true))
		{
			if(boxes.containsKey(player.getStoredId()))
				boxes.remove(player.getStoredId());		
			player.sendMessage("You have left the event!"); // TODO: Вынести в ДП.			
		}	
	}

	private void askQuestion(Player player, String question)
	{
		ConfirmDlgPacket packet = new ConfirmDlgPacket(SystemMsg.S1, 60000).addString(question);
		player.ask(packet, new AskQuestionAnswerListener(player));
	}

	public AbstractFightClub getEventByObjId(int objId)
	{
		return _activeEvents.get(Integer.valueOf(objId));
	}

	public static FightClubEventManager getInstance()
	{
		if(_instance == null)
			_instance = new FightClubEventManager();
		return _instance;
	}

	private class AskQuestionAnswerListener implements OnAnswerListener
	{
		private final Player _player;

		private AskQuestionAnswerListener(Player player)
		{
			_player = player;
		}

		public void sayYes()
		{
			FightClubEventManager.this.leaveEvent(_player);
		}

		public void sayNo()
		{
			//
		}
	}

	private class EventRunThread extends RunnableImpl
	{
		private AbstractFightClub _event;

		private EventRunThread(AbstractFightClub event)
		{
			_event = event;
		}

		public void runImpl() throws Exception
		{
			startEventCountdown(_event);

			if(!_event.isAutoTimed())
				return;

			Thread.sleep(60000L);

			Calendar nextEventDate = FightClubEventManager.this.getClosestEventDate(_event.getAutoStartTimes());

			_event.printScheduledTime(nextEventDate.getTimeInMillis());

			ThreadPoolManager.getInstance().schedule(new EventRunThread(_event), nextEventDate.getTimeInMillis() - System.currentTimeMillis());
		}
	}

	private class AnswerEventInvitation implements OnAnswerListener
	{
		private Player _player;
		private AbstractFightClub _event;

		private AnswerEventInvitation(Player player, AbstractFightClub event)
		{
			_player = player;
			_event = event;
		}

		public void sayYes()
		{
			trySignForEvent(_player, _event, false);
		}

		public void sayNo()
		{
			//
		}
	}

	public static enum CLASSES
	{
		FIGHTERS(13113, new ClassId[] { ClassId.DUELIST, ClassId.DREADNOUGHT, ClassId.TITAN, ClassId.GRAND_KHAVATARI, ClassId.MAESTRO, ClassId.DOOMBRINGER, ClassId.M_SOUL_HOUND, ClassId.F_SOUL_HOUND }), 
		TANKS(13112, new ClassId[] { ClassId.PHOENIX_KNIGHT, ClassId.HELL_KNIGHT, ClassId.EVAS_TEMPLAR, ClassId.SHILLIEN_TEMPLAR, ClassId.TRICKSTER }), 
		ARCHERS(13114, new ClassId[] { ClassId.SAGITTARIUS, ClassId.MOONLIGHT_SENTINEL, ClassId.GHOST_SENTINEL, ClassId.FORTUNE_SEEKER }), 
		DAGGERS(13114, new ClassId[] { ClassId.ADVENTURER, ClassId.WIND_RIDER, ClassId.GHOST_HUNTER, ClassId.DOMINATOR }), 
		MAGES(13116, new ClassId[] { ClassId.ARCHMAGE, ClassId.SOULTAKER, ClassId.MYSTIC_MUSE, ClassId.STORM_SCREAMER }), 
		SUMMONERS(13118, new ClassId[] { ClassId.ARCANA_LORD, ClassId.ELEMENTAL_MASTER, ClassId.SPECTRAL_MASTER }), 
		HEALERS(13115, new ClassId[] { ClassId.CARDINAL, ClassId.EVAS_SAINT, ClassId.SHILLIEN_SAINT }), 
		SUPPORTS(13117, new ClassId[] { ClassId.HIEROPHANT, ClassId.SWORD_MUSE, ClassId.SPECTRAL_DANCER, ClassId.DOOMCRYER, ClassId.JUDICATOR });

		private int _transformId;
		private ClassId[] _classes;

		private CLASSES(int transformId, ClassId[] ids)
		{
			_transformId = transformId;
			_classes = ids;
		}

		public ClassId[] getClasses()
		{
			return _classes;
		}

		public int getTransformId()
		{
			return _transformId;
		}
	}
	
	public static void clearBoxes()
	{
		boxes.clear();
	}
}
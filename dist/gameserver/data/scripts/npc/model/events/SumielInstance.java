package npc.model.events;

import l2s.commons.util.Rnd;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.SimpleSpawner;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;

/**
 * @author PaInKiLlEr
 * @reworked by Bonux
**/
public class SumielInstance extends NpcInstance
{
	private final static int INTERVAL_TIME = 3;

	// Timer's
	private final static int TIMER_0 = 3344;
	private final static int TIMER_1 = 3345;
	private final static int TIMER_2 = 3346;
	private final static int TIMER_3 = 3347;
	private final static int TIMER_4 = 3348;
	private final static int TIMER_5 = 3349;
	private final static int TIMER_6 = 3350;
	private final static int TIMER_7 = 3351;
	private final static int TIMER_8 = 3352;
	private final static int TIMER_9 = 3356;
	private final static int PC_TURN = 3357;
	private final static int GAME_TIME_EXPIRED = 3354;
	private final static int HURRY_UP = 3358;
	private final static int HURRY_UP2 = 3359;
	private final static int GAME_TIME = 3360;

	private final static int CHEST_NPC_ID = 18934;

	private final static Location TELEPORT_LOCATION1 = new Location(118833, -80589, -2688);

	private Location _chestSpawnLoc;
	private Location _teleportLocation2;

	private Player _gamer = null;
	private Player _firstTalked = null;

	private boolean _gameInProgress = false;
	private boolean _gameStarted = false;

	private int _currentIgnation = 0;
	private int _ignition1 = 0;
	private int _ignition2 = 0;
	private int _ignition3 = 0;
	private int _ignition4 = 0;
	private int _ignition5 = 0;
	private int _ignition6 = 0;
	private int _ignition7 = 0;
	private int _ignition8 = 0;
	private int _ignition9 = 0;

	private int _tryFails = 0;

	public SumielInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();

		String temp = getParameter("chest_spawn_loc", null);
		_chestSpawnLoc = temp == null ? null : Location.parseLoc(temp);

		temp = getParameter("teleport_loc", null);
		_teleportLocation2 = temp == null ? TELEPORT_LOCATION1 : Location.parseLoc(temp);
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		if(val == 0)
		{
			String htmlpath = null;
			if(_gamer == null && !_gameStarted)
			{
				htmlpath = "events/monastyre/minigame_instructor001.htm";
				_firstTalked = player;
			}
			else if(_gamer == null && _gameStarted)
				htmlpath = "events/monastyre/minigame_instructor008.htm";
			else if(_gamer == player && _tryFails == 1 && !_gameInProgress)
				htmlpath = "events/monastyre/minigame_instructor002.htm";
			else if(_gamer == player && _tryFails == 2 && !_gameInProgress)
				htmlpath = "events/monastyre/minigame_instructor003.htm";
			else if(_gamer != player)
				htmlpath = "events/monastyre/minigame_instructor004.htm";
			else if(_gamer == player && _gameInProgress)
				htmlpath = "events/monastyre/minigame_instructor007.htm";
			else
				htmlpath = "events/monastyre/minigame_instructor001.htm";

			player.sendPacket(new NpcHtmlMessagePacket(player, this, htmlpath, val));
		}
		else
			super.showChatWindow(player, val);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(command.equals("teleport"))
			showChatWindow(player, "events/monastyre/minigame_instructor006.htm");
		else if(command.equals("teleport1"))
			player.teleToLocation(_teleportLocation2);
		else if(command.equals("teleport2"))
			player.teleToLocation(TELEPORT_LOCATION1);
		else if(command.equals("start"))
		{
			if(player.getInventory().getCountOf(15540) == 0)
				showChatWindow(player, "events/monastyre/minigame_instructor005.htm");
			else if(_firstTalked != player)
				showChatWindow(player, "events/monastyre/minigame_instructor004.htm");
			else if(_firstTalked == player)
			{
				_gameInProgress = true;
				_gameStarted = true;
				getAI().unblockTimer(HURRY_UP);
				getAI().unblockTimer(HURRY_UP2);
				player.getInventory().destroyItemByItemId(15540, 1);
				player.getInventory().addItem(15485, 1);
				Functions.npcShout(this, NpcString.FURNFACE1);
				_ignition1 = Rnd.get(9) + 1;
				_ignition2 = Rnd.get(9) + 1;
				_ignition3 = Rnd.get(9) + 1;
				_ignition4 = Rnd.get(9) + 1;
				_ignition5 = Rnd.get(9) + 1;
				_ignition6 = Rnd.get(9) + 1;
				_ignition7 = Rnd.get(9) + 1;
				_ignition8 = Rnd.get(9) + 1;
				_ignition9 = Rnd.get(9) + 1;
				_gamer = player;
				getAI().addTimer(HURRY_UP, 2 * 60 * 1000L);
				getAI().addTimer(GAME_TIME, (3 * 60 * 1000L) + (10 * 1000L));
				getAI().addTimer(TIMER_0, 1000L);
			}
		}
		else if(command.equals("restart"))
		{
			_gameInProgress = true;
			_ignition1 = Rnd.get(9) + 1;
			_ignition2 = Rnd.get(9) + 1;
			_ignition3 = Rnd.get(9) + 1;
			_ignition4 = Rnd.get(9) + 1;
			_ignition5 = Rnd.get(9) + 1;
			_ignition6 = Rnd.get(9) + 1;
			_ignition7 = Rnd.get(9) + 1;
			_ignition8 = Rnd.get(9) + 1;
			_ignition9 = Rnd.get(9) + 1;
			_gamer = player;
			getAI().addTimer(TIMER_0, 1000L);
		}
		else
			super.onBypassFeedback(player, command);
	}


	public void onEvtTimer(int timerId, Object arg1, Object arg2)
	{
		if(timerId == TIMER_0)
		{
			getAI().broadCastScriptEvent("2114002", 0, 1200);
			getAI().addTimer(TIMER_1, INTERVAL_TIME * 2000L);
		}
		else if(timerId == TIMER_1)
		{
			getAI().broadCastScriptEvent("2114001", _ignition1, 1200);
			getAI().addTimer(TIMER_2, INTERVAL_TIME * 1000L);
		}
		else if(timerId == TIMER_2)
		{
			getAI().broadCastScriptEvent("2114001", _ignition2, 1200);
			getAI().addTimer(TIMER_3, INTERVAL_TIME * 1000L);
		}
		else if(timerId == TIMER_3)
		{
			getAI().broadCastScriptEvent("2114001", _ignition3, 1200);
			getAI().addTimer(TIMER_4, INTERVAL_TIME * 1000L);
		}
		else if(timerId == TIMER_4)
		{
			getAI().broadCastScriptEvent("2114001", _ignition4, 1200);
			getAI().addTimer(TIMER_5, INTERVAL_TIME * 1000L);
		}
		else if(timerId == TIMER_5)
		{
			getAI().broadCastScriptEvent("2114001", _ignition5, 1200);
			getAI().addTimer(TIMER_6, INTERVAL_TIME * 1000L);
		}
		else if(timerId == TIMER_6)
		{
			getAI().broadCastScriptEvent("2114001", _ignition6, 1200);
			getAI().addTimer(TIMER_7, INTERVAL_TIME * 1000L);
		}
		else if(timerId == TIMER_7)
		{
			getAI().broadCastScriptEvent("2114001", _ignition7, 1200);
			getAI().addTimer(TIMER_8, INTERVAL_TIME * 1000L);
		}
		else if(timerId == TIMER_8)
		{
			getAI().broadCastScriptEvent("2114001", _ignition8, 1200);
			getAI().addTimer(TIMER_9, INTERVAL_TIME * 1000L);
		}
		else if(timerId == TIMER_9)
		{
			getAI().broadCastScriptEvent("2114001", _ignition9, 1200);
			getAI().addTimer(PC_TURN, INTERVAL_TIME * 1000L);
		}
		else if(timerId == HURRY_UP)
		{
			Functions.npcShout(this, NpcString.FURNFACE2);
			getAI().addTimer(HURRY_UP2, 60 * 1000L);
		}
		else if(timerId == HURRY_UP2)
		{
			Functions.npcShout(this, NpcString.FURNFACE3);
			getAI().addTimer(GAME_TIME_EXPIRED, 10 * 1000L);
		}
		else if(timerId == PC_TURN)
		{
			Functions.npcShout(this, NpcString.FURNFACE4);
			getAI().broadCastScriptEvent("21140015", 0, 1200);
			_currentIgnation = 1;
		}
		else if(timerId == GAME_TIME_EXPIRED)
		{
			getAI().broadCastScriptEvent("2114003", 0, 1200);
			Functions.npcShout(this, NpcString.FURNFACE5);
			_gamer = null;
			_tryFails = 0;
			_gameInProgress = false;
		}
		else if(timerId == GAME_TIME)
		{
			_gameStarted = false;
		}
		super.onEvtTimer(timerId, arg1, arg2);
	}


	public void onEvtScriptEvent(String event, Object arg1, Object arg2)
	{
		if(event.equalsIgnoreCase("2114005"))
		{
			int potNumber = ((Integer) arg1).intValue();
			if(potNumber == _ignition1 && _currentIgnation == 1)
				_currentIgnation = 2;
			else if(potNumber == _ignition2 && _currentIgnation == 2)
				_currentIgnation = 3;
			else if(potNumber == _ignition3 && _currentIgnation == 3)
				_currentIgnation = 4;
			else if(potNumber == _ignition4 && _currentIgnation == 4)
				_currentIgnation = 5;
			else if(potNumber == _ignition5 && _currentIgnation == 5)
				_currentIgnation = 6;
			else if(potNumber == _ignition6 && _currentIgnation == 6)
				_currentIgnation = 7;
			else if(potNumber == _ignition7 && _currentIgnation == 7)
				_currentIgnation = 8;
			else if(potNumber == _ignition8 && _currentIgnation == 8)
				_currentIgnation = 9;
			else if(potNumber == _ignition9 && _currentIgnation == 9)
			{
				getAI().broadCastScriptEvent("2114003", 0, 1200);

				SimpleSpawner sp = new SimpleSpawner(NpcHolder.getInstance().getTemplate(CHEST_NPC_ID));
				sp.setLoc(_chestSpawnLoc);
				sp.doSpawn(true);

				Functions.npcShout(this, NpcString.FURNFACE6);

				getAI().blockTimer(HURRY_UP);
				getAI().blockTimer(HURRY_UP2);

				_gamer = null;
				_tryFails = 0;
				_gameInProgress = false;
			}
			else
			{
				getAI().broadCastScriptEvent("2114004", 0, 1200);
				if(_tryFails < 2)
				{
					_tryFails++;
					Functions.npcShout(this, NpcString.FURNFACE7);
					_gameInProgress = false;
				}
				else
				{
					getAI().blockTimer(HURRY_UP);
					getAI().blockTimer(HURRY_UP2);

					Functions.npcShout(this, NpcString.FURNFACE8);

					_gamer = null;
					_tryFails = 0;
					_gameInProgress = false;
				}
			}
		}
		super.onEvtScriptEvent(event, arg1, arg2);
	}
}
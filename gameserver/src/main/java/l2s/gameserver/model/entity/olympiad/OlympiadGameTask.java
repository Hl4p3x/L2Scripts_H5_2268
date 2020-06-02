package l2s.gameserver.model.entity.olympiad;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.utils.Log;

public class OlympiadGameTask extends RunnableImpl
{
	private static final Logger _log = LoggerFactory.getLogger(OlympiadGameTask.class);

	private OlympiadGame _game;
	private BattleStatus _status;
	private int _count;
	private long _time;

	private boolean _terminated = false;

	public boolean isTerminated()
	{
		return _terminated;
	}

	public BattleStatus getStatus()
	{
		return _status;
	}

	public int getCount()
	{
		return _count;
	}

	public OlympiadGame getGame()
	{
		return _game;
	}

	public long getTime()
	{
		return _count;
	}

	public ScheduledFuture<?> shedule()
	{
		return ThreadPoolManager.getInstance().schedule(this, _time);
	}
	final List<Player> p1;
	final List<Player> p2;

	public OlympiadGameTask(OlympiadGame game, BattleStatus status, int count, long time)
	{
		_game = game;
		_status = status;
		_count = count;
		_time = time;
		p1 = _game.getTeam1();
		p2 = _game.getTeam2();
	}
	
   final String getPlayerIp1()
    {
    	String ip = "";
    	for(Player p:p1)
		 ip = p.getIP();	 	
    	return ip;
    }
    
   final String getPlayerIp2()
    {
    	String ip = "";
    	for(Player p:p2)
		ip = p.getIP();	 	
    	return ip;
	}

	@Override
	public void runImpl() throws Exception
	{
		if(_game == null || _terminated)
			return;

		OlympiadGameTask task = null;

		int gameId = _game.getId();

		try
		{
			if(!Olympiad.inCompPeriod())
				return;

			// Прерываем игру, если один из игроков не онлайн, и игра еще не прервана
			if(!_game.checkPlayersOnline() && _status != BattleStatus.ValidateWinner && _status != BattleStatus.Ending)
			{
				Log.add("Player is offline for game " + gameId + ", status: " + _status, "olympiad");
				_game.endGame(1000, true, false);
				return;
			}

			switch(_status)
			{
				case Begining:
				{
					if(_count > 0)
					{
						if((_count % 60) == 0)
							_game.broadcastPacket(new SystemMessage(SystemMessage.YOU_WILL_ENTER_THE_OLYMPIAD_STADIUM_IN_S1_SECOND_S).addNumber(_count), true, false);
						else if(_count == 30)
							_game.broadcastPacket(new SystemMessage(SystemMessage.YOU_WILL_ENTER_THE_OLYMPIAD_STADIUM_IN_S1_SECOND_S).addNumber(_count), true, false);
						else if(_count == 15)
							_game.broadcastPacket(new SystemMessage(SystemMessage.YOU_WILL_ENTER_THE_OLYMPIAD_STADIUM_IN_S1_SECOND_S).addNumber(_count), true, false);
						else if(_count < 6)
							_game.broadcastPacket(new SystemMessage(SystemMessage.YOU_WILL_ENTER_THE_OLYMPIAD_STADIUM_IN_S1_SECOND_S).addNumber(_count), true, false);
					}

					if(_count > 1)
						task = new OlympiadGameTask(_game, BattleStatus.Begining, _count - 1, 1000);
					else
						task = new OlympiadGameTask(_game, BattleStatus.PortPlayers, 0, 1000);
					break;
				}
				case PortPlayers:
				{
					_game.portPlayersToArena();
					_game.managerShout();
					task = new OlympiadGameTask(_game, BattleStatus.Started, 60, 1000);
					break;
				}
				case Started:
				{
					if(_count == 60)
					{
						_game.setState(1);
						_game.preparePlayers();
						_game.addBuffers();
						_game.broadcastPacket(new SystemMessage(SystemMessage.THE_GAME_WILL_START_IN_S1_SECOND_S).addNumber(_count), true, true);
						_count -= 5;
					}
					else if(_count == 55)
					{
						_game.restoreAll();
						_count -= 5;	
						_game.broadcastPacket(new SystemMessage(SystemMessage.THE_GAME_WILL_START_IN_S1_SECOND_S).addNumber(_count), true, true);
					}
					else
					{
						_game.broadcastPacket(new SystemMessage(SystemMessage.THE_GAME_WILL_START_IN_S1_SECOND_S).addNumber(_count), true, true);
						_count -= 10;
					}	

					if(_count > 0)
					{
						if(_count == 55)
							task = new OlympiadGameTask(_game, BattleStatus.Started, _count, 5000);
						else
							task = new OlympiadGameTask(_game, BattleStatus.Started, _count, 10000);
						break;
					}

					_game.openDoors();

					task = new OlympiadGameTask(_game, BattleStatus.CountDown, 5, 5000);
					break;
				}
				case CountDown:
				{
					_game.broadcastPacket(new SystemMessage(SystemMessage.THE_GAME_WILL_START_IN_S1_SECOND_S).addNumber(_count), true, true);
					_count--;
					if(_count <= 0)
						task = new OlympiadGameTask(_game, BattleStatus.StartComp, 36, 1000);
					else
						task = new OlympiadGameTask(_game, BattleStatus.CountDown, _count, 1000);
					break;
				}
				case StartComp:
				{
					if(_count == 36)
					{
						_game.deleteBuffers();
						_game.setState(2);
						_game.broadcastPacket(Msg.STARTS_THE_GAME, true, true);
						_game.broadcastInfo(null, null, false);
					}
					// Wait 3 mins (Battle)
					_count--;
					if(_count == 12)
						_game.broadcastPacket(new SystemMessage(SystemMessage.THE_GAME_WILL_END_IN_S1_SECONDS).addNumber(_count*10), true, true);
					else if(_count == 6)
						_game.broadcastPacket(new SystemMessage(SystemMessage.THE_GAME_WILL_END_IN_S1_SECONDS).addNumber(_count*10), true, true);
					else if(_count == 3)
						_game.broadcastPacket(new SystemMessage(SystemMessage.THE_GAME_WILL_END_IN_S1_SECONDS).addNumber(_count*10), true, true);
					else if(_count == 0)
						_game.broadcastPacket(new SystemMessage(SystemMessage.THE_GAME_WILL_END_IN_S1_SECONDS).addNumber(_count*10), true, true);
					if(_count == 0)
						task = new OlympiadGameTask(_game, BattleStatus.ValidateWinner, 0, 10000);
					else
						task = new OlympiadGameTask(_game, BattleStatus.StartComp, _count, 10000);
					break;
				}
				case ValidateWinner:
				{
					try
					{
						_game.validateWinner(_count > 0, false);
					}
					catch(Exception e)
					{
						_log.error("", e);
					}
					task = new OlympiadGameTask(_game, BattleStatus.Ending, 0, 20000);
					break;
				}
				case Ending:
				{
					_game.collapse();
					_terminated = true;
					if(Olympiad._manager != null)
						Olympiad._manager.freeOlympiadInstance(_game.getId());
					return;
				}
			}

			if(task == null)
			{
				Log.add("task == null for game " + gameId, "olympiad");
				Thread.dumpStack();
				_game.endGame(1000, true, false);
				return;
			}

			_game.sheduleTask(task);
		}
		catch(Exception e)
		{
			_log.error("", e);
			_game.endGame(1000, true, false);
		}
	}
}
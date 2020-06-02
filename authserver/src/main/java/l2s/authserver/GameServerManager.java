package l2s.authserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.authserver.database.DatabaseFactory;
import l2s.authserver.network.gamecomm.GameServer;
import l2s.commons.dbutils.DbUtils;
import l2s.commons.net.HostInfo;

public class GameServerManager
{
	public static final int SUCCESS_GS_REGISTER = 0;
	public static final int FAIL_GS_REGISTER_DIFF_KEYS = 1;
	public static final int FAIL_GS_REGISTER_ID_ALREADY_USE = 2;
	public static final int FAIL_GS_REGISTER_ERROR = 3;
	
	private static Logger _log = LoggerFactory.getLogger(GameServerManager.class);

	private static final GameServerManager _instance = new GameServerManager();

	public static final GameServerManager getInstance()
	{
		return _instance;
	}

	private final Map<Integer, GameServer> _gameServers = new TreeMap<Integer, GameServer>();
	private final ReadWriteLock _lock = new ReentrantReadWriteLock();
	private final Lock _readLock = _lock.readLock();
	private final Lock _writeLock = _lock.writeLock();

	public GameServerManager()
	{
		load();
	}

	private void load()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT `id`, `ip`, `port`, `age_limit`, `pvp`, `max_players`, `type`, `brackets`, `key` FROM gameservers");
			rset = statement.executeQuery();

			while(rset.next())
			{
				int id = rset.getInt("id");
				GameServer gs = new GameServer(id, rset.getString("ip"), rset.getInt("port"), rset.getString("key"));
				gs.setAgeLimit(rset.getInt("age_limit"));
				gs.setPvp(rset.getInt("pvp") > 0);
				gs.setMaxPlayers(rset.getInt("max_players"));
				gs.setServerType(rset.getInt("type"));
				gs.setShowingBrackets(rset.getInt("brackets") > 0);
				_gameServers.put(id, gs);
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		_log.info("Loaded " + _gameServers.size() + " registered GameServer(s).");
	}

	/**
	 * Поулчить массив всех зарегистрированных игровых серверов
	 * 
	 * @return массив всех игровых серверов
	 */
	public GameServer[] getGameServers()
	{
		_readLock.lock();
		try
		{
			Set<GameServer> gameservers = new HashSet<GameServer>(_gameServers.values());
			return gameservers.toArray(new GameServer[gameservers.size()]);
		}
		finally
		{
			_readLock.unlock();
		}
	}

	/**
	 * Получить зарегистрированный игровой сервер по идентификатору
	 * 
	 * @param id идентификатор игрового сервера
	 * @return игровой сервер
	 */
	public GameServer getGameServerById(int id)
	{
		_readLock.lock();
		try
		{
			return _gameServers.get(id);
		}
		finally
		{
			_readLock.unlock();
		}
	}

	/**
	 * Регистрация игрового сервера на требуемый идентификатор
	 * 
	 * @param id требуемый идентификатор игрового сервера
	 * @param gs игровой сервер
	 * @return true если игрвоой сервер успешно зарегистрирован
	 */
	public int registerGameServer(HostInfo host, GameServer gs)
	{
		_writeLock.lock();
		try
		{
			GameServer pgs = _gameServers.get(host.getId());
			if(pgs != null)
			{
				HostInfo phost = pgs.getHost(host.getId());
				if(phost == null || !StringUtils.equals(host.getKey(), phost.getKey()))
					return FAIL_GS_REGISTER_DIFF_KEYS;
			}
			else if(!Config.ACCEPT_NEW_GAMESERVER)
				return FAIL_GS_REGISTER_ID_ALREADY_USE;
			
			if(pgs == null || !pgs.isAuthed())
			{
				if(pgs != null)
					pgs.removeHost(host.getId());

				_gameServers.put(host.getId(), gs);
				return SUCCESS_GS_REGISTER;
			}
		}
		finally
		{
			_writeLock.unlock();
		}
		return FAIL_GS_REGISTER_ERROR;
	}
}

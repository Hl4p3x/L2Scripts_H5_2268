package l2s.authserver.network.gamecomm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.authserver.database.DatabaseFactory;
import l2s.commons.dbutils.DbUtils;
import l2s.commons.net.HostInfo;

/**
 * @reworked by Bonux
**/
public class GameServer
{
	private static final Logger _log = LoggerFactory.getLogger(GameServer.class);

	private final TIntObjectMap<HostInfo> _hosts = new TIntObjectHashMap<HostInfo>();
	private int _serverType;
	private int _ageLimit;
	private int _protocol;
	private boolean _isOnline;
	private boolean _isPvp;
	private boolean _isShowingBrackets;
	private boolean _isGmOnly;

	private int _maxPlayers;

	private GameServerConnection _conn;
	private boolean _isAuthed;

	private Set<String> _accounts = new CopyOnWriteArraySet<String>();

	public GameServer(GameServerConnection conn)
	{
		_conn = conn;
	}

	public GameServer(int id, String ip, int port, String key)
	{
		_conn = null;
		addHost(new HostInfo(id, ip, port, key));
	}

	public void addHost(HostInfo host)
	{
		_hosts.put(host.getId(), host);
	}

	public HostInfo removeHost(int id)
	{
		return _hosts.remove(id);
	}

	public HostInfo getHost(int id)
	{
		return _hosts.get(id);
	}

	public HostInfo[] getHosts()
	{
		return _hosts.values(new HostInfo[_hosts.size()]);
	}

	public void setAuthed(boolean isAuthed)
	{
		_isAuthed = isAuthed;
	}

	public boolean isAuthed()
	{
		return _isAuthed;
	}

	public void setConnection(GameServerConnection conn)
	{
		_conn = conn;
	}

	public GameServerConnection getConnection()
	{
		return _conn;
	}

	public void setMaxPlayers(int maxPlayers)
	{
		_maxPlayers = maxPlayers;
	}

	public int getMaxPlayers()
	{
		return _maxPlayers;
	}

	public int getOnline()
	{
		return _accounts.size();
	}

	public Set<String> getAccounts()
	{
		return _accounts;
	}

	public void addAccount(String account)
	{
		_accounts.add(account);
	}

	public void removeAccount(String account)
	{
		_accounts.remove(account);
	}

	public void setDown()
	{
		setAuthed(false);
		setConnection(null);
		setOnline(false);

		_accounts.clear();
	}

	public void sendPacket(SendablePacket packet)
	{
		GameServerConnection conn = getConnection();
		if(conn != null)
			conn.sendPacket(packet);
	}

	public int getServerType()
	{
		return _serverType;
	}

	public boolean isOnline()
	{
		return _isOnline;
	}

	public void setOnline(boolean online)
	{
		_isOnline = online;
	}

	public void setServerType(int serverType)
	{
		_serverType = serverType;
	}

	public boolean isPvp()
	{
		return _isPvp;
	}

	public void setPvp(boolean pvp)
	{
		_isPvp = pvp;
	}

	public boolean isShowingBrackets()
	{
		return _isShowingBrackets;
	}

	public void setShowingBrackets(boolean showingBrackets)
	{
		_isShowingBrackets = showingBrackets;
	}

	public boolean isGmOnly()
	{
		return _isGmOnly;
	}

	public void setGmOnly(boolean gmOnly)
	{
		_isGmOnly = gmOnly;
	}

	public int getAgeLimit()
	{
		return _ageLimit;
	}

	public void setAgeLimit(int ageLimit)
	{
		_ageLimit = ageLimit;
	}

	public int getProtocol()
	{
		return _protocol;
	}

	public void setProtocol(int protocol)
	{
		_protocol = protocol;
	}

	public boolean store()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			for(HostInfo host : _hosts.valueCollection())
			{
				statement = con.prepareStatement("REPLACE INTO gameservers (`id`, `ip`, `port`, `age_limit`, `pvp`, `max_players`, `type`, `brackets`, `key`) VALUES(?,?,?,?,?,?,?,?,?)");
				int i = 0;
				statement.setInt(++i, host.getId());
				statement.setString(++i, host.getAddress());
				statement.setShort(++i, (short) host.getPort());
				statement.setByte(++i, (byte) getAgeLimit());
				statement.setByte(++i, (byte) (isPvp() ? 1 : 0));
				statement.setShort(++i, (short) getMaxPlayers());
				statement.setInt(++i, getServerType());
				statement.setByte(++i, (byte) (isShowingBrackets() ? 1 : 0));
				statement.setString(++i, host.getKey());
				statement.execute();
				DbUtils.closeQuietly(statement);
			}
		}
		catch(Exception e)
		{
			_log.error("Error while store gameserver: " + e, e);
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}
}
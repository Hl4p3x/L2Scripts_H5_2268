package l2s.gameserver.config.xml.holder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.data.xml.AbstractHolder;
import l2s.commons.net.HostInfo;
import l2s.gameserver.Config;

/**
 * @author Bonux
**/
public final class HostsConfigHolder extends AbstractHolder
{
	private static final Logger _log = LoggerFactory.getLogger(HostsConfigHolder.class);

	private static final HostsConfigHolder _instance = new HostsConfigHolder();

	private HostInfo _authServerHost;
	private TIntObjectMap<HostInfo> _gameServerHosts = new TIntObjectHashMap<HostInfo>();

	public static HostsConfigHolder getInstance()
	{
		return _instance;
	}

	public void setAuthServerHost(HostInfo host)
	{
		_authServerHost = host;
	}

	public HostInfo getAuthServerHost()
	{
		return _authServerHost;
	}

	public void addGameServerHost(HostInfo host)
	{
		if(_gameServerHosts.containsKey(host.getId()))
		{
			warn("Error while loading gameserver host info! Host have dublicate id: " + host.getId());
			return;
		}
		if(_gameServerHosts.isEmpty())
		{
			Config.REQUEST_ID = host.getId();
			Config.EXTERNAL_HOSTNAME = host.getAddress();
			Config.PORT_GAME = host.getPort();
		}
		_gameServerHosts.put(host.getId(), host);
	}

	public HostInfo[] getGameServerHosts()
	{
		return _gameServerHosts.values(new HostInfo[_gameServerHosts.size()]);
	}

	@Override
	public void log()
	{
		_log.info("=================================================");
		_log.info("Authserver host info: IP[" + getAuthServerHost().getAddress() + "], PORT[" + getAuthServerHost().getPort() + "]");
		_log.info("=================================================");
		_log.info("Gameserver host info:");

		for(HostInfo host : getGameServerHosts())
			_log.info("ID[" + host.getId() + "], ADDRESS[" + (host.getAddress() == null ? "NOT SPECIFIED" : host.getAddress()) + "], PORT[" + host.getPort() + "]");

		_log.info("=================================================");
	}

	@Override
	public int size()
	{
		int size = _gameServerHosts.size();
		if(_authServerHost != null)
			size++;
		return size;
	}

	@Override
	public void clear()
	{
		_authServerHost = null;
		_gameServerHosts.clear();
	}
}
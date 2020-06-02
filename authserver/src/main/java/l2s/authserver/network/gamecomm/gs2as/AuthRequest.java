package l2s.authserver.network.gamecomm.gs2as;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.authserver.AuthServer;
import l2s.authserver.GameServerManager;
import l2s.authserver.network.gamecomm.GameServer;
import l2s.authserver.network.gamecomm.ReceivablePacket;
import l2s.authserver.network.gamecomm.as2gs.AuthResponse;
import l2s.authserver.network.gamecomm.as2gs.LoginServerFail;
import l2s.commons.net.HostInfo;

/**
 * @reworked by Bonux
**/
public class AuthRequest extends ReceivablePacket
{
	private final static Logger _log = LoggerFactory.getLogger(AuthRequest.class);

	private int _protocolVersion;
	private HostInfo[] _hosts;
	private int _serverType;
	private int _ageLimit;
	private boolean _gmOnly;
	private boolean _brackets;
	private boolean _pvp;
	private int _maxOnline;

	@Override
	protected void readImpl()
	{
		_protocolVersion = readD();
		if(_protocolVersion != AuthServer.AUTH_SERVER_PROTOCOL)
			return;

		_serverType = readD();
		_ageLimit = readD();
		_gmOnly = readC() == 1;
		_brackets = readC() == 1;
		_pvp = readC() == 1;
		_maxOnline = readD();

		int hostsCount = readC();
		_hosts = new HostInfo[hostsCount];
		for(int i = 0; i < hostsCount; i++)
		{
			int id = readC();
			String address = readS();
			int port = readH();
			String key = readS();
			int maskCount = readC();
			HostInfo host = new HostInfo(id, address, port, key);
			for(int m = 0; m < maskCount; m++)
			{
				String subAddress = readS();
				byte[] subnetAddress = new byte[readD()];
				readB(subnetAddress);
				byte[] subnetMask = new byte[readD()];
				readB(subnetMask);
				host.addSubnet(subAddress, subnetAddress, subnetMask);
			}
			_hosts[i] = host;
		}
	}

	@Override
	protected void runImpl()
	{
		if(_protocolVersion != AuthServer.AUTH_SERVER_PROTOCOL)
		{
			_log.warn("Authserver and gameserver have different versions! Please update your servers.");
			sendPacket(new LoginServerFail("Authserver and gameserver have different versions! Please update your servers.", false));
			return;
		}

		GameServer gs = getGameServer();

		_log.info("Trying to register gameserver: IP[" + gs.getConnection().getIpAddress() + "]");

		for(HostInfo host : _hosts)
		{
			int registerResult = GameServerManager.getInstance().registerGameServer(host, gs);
			if(registerResult == GameServerManager.SUCCESS_GS_REGISTER)
				gs.addHost(host);
			else
			{
				if(registerResult == GameServerManager.FAIL_GS_REGISTER_DIFF_KEYS)
				{
					sendPacket(new LoginServerFail("Gameserver registration on ID[" + host.getId() + "] failed. Registered different keys!", false));
					sendPacket(new LoginServerFail("Set the same keys in authserver and gameserver, and restart them!", false));
				}
				else if(registerResult == GameServerManager.FAIL_GS_REGISTER_ID_ALREADY_USE)
				{
					sendPacket(new LoginServerFail("Gameserver registration on ID[" + host.getId() + "] failed. ID[" + host.getId() + "] is already in use!", false));
					sendPacket(new LoginServerFail("Free ID[" + host.getId() + "] or change to another ID, and restart your authserver or gameserver!", false));
				}
				else if(registerResult == GameServerManager.FAIL_GS_REGISTER_ERROR)
				{
					sendPacket(new LoginServerFail("Gameserver registration on ID[" + host.getId() + "] failed. You have some errors!", false));
					sendPacket(new LoginServerFail("To solve the problem, contact the developer!", false));
				}
			}
		}

		if(gs.getHosts().length > 0)
		{
			gs.setProtocol(_protocolVersion);
			gs.setServerType(_serverType);
			gs.setAgeLimit(_ageLimit);
			gs.setGmOnly(_gmOnly);
			gs.setShowingBrackets(_brackets);
			gs.setPvp(_pvp);
			gs.setMaxPlayers(_maxOnline);
			gs.store();

			gs.setAuthed(true);
			gs.getConnection().startPingTask();
		}
		else
		{
			sendPacket(new LoginServerFail("Gameserver registration failed. All ID's is already in use!", true));
			_log.info("Gameserver registration failed.");
			return;
		}

		_log.info("Gameserver registration successful.");
		sendPacket(new AuthResponse(gs));
	}
}

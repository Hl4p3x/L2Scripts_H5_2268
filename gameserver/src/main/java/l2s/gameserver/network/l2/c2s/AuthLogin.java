package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Shutdown;
import l2s.gameserver.network.authcomm.AuthServerCommunication;
import l2s.gameserver.network.authcomm.SessionKey;
import l2s.gameserver.network.authcomm.gs2as.PlayerAuthRequest;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.s2c.LoginResultPacket;
import l2s.gameserver.network.l2.s2c.ServerCloseSocketPacket;
import l2s.gameserver.utils.Language;

/**
 * cSddddd
 * cSdddddQ
 * loginName + keys must match what the loginserver used.
 */
public class AuthLogin extends L2GameClientPacket
{
	private String _loginName;
	private int _playKey1;
	private int _playKey2;
	private int _loginKey1;
	private int _loginKey2;
	private int _lang;

	@Override
	protected void readImpl()
	{
		_loginName = readS(32).toLowerCase();
		_playKey2 = readD();
		_playKey1 = readD();
		_loginKey1 = readD();
		_loginKey2 = readD();
		_lang = readD();
	}

	@Override
	protected void runImpl()
	{
		GameClient client = getClient();
		
		SessionKey key = new SessionKey(_loginKey1, _loginKey2, _playKey1, _playKey2);
		client.setSessionId(key);
		client.setLoginName(_loginName);
		client.setLanguage(Language.getLanguage(_lang));
		
		if(Shutdown.getInstance().getMode() != Shutdown.NONE && Shutdown.getInstance().getSeconds() <= 15)
			client.closeNow(false);
		else
		{			
			if(AuthServerCommunication.getInstance().isShutdown())
			{
				client.close(LoginResultPacket.SYSTEM_ERROR_LOGIN_LATER);
				return;
			}
			
			GameClient oldClient = AuthServerCommunication.getInstance().addWaitingClient(client);
			if(oldClient != null)
				oldClient.close(ServerCloseSocketPacket.STATIC);

			AuthServerCommunication.getInstance().sendPacket(new PlayerAuthRequest(client));
		}
	}
}
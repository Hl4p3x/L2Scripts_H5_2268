package l2s.authserver.network.l2.c2s;

import l2s.authserver.network.l2.L2LoginClient;
import l2s.authserver.network.l2.SessionKey;
import l2s.authserver.network.l2.s2c.LoginFail.LoginFailReason;
import l2s.authserver.network.l2.s2c.ServerList;

/**
 * Format: ddc
 * d: fist part of session id
 * d: second part of session id
 * c: ?
 */
public class RequestServerList extends L2LoginClientPacket
{
	private int _loginOkID1;
	private int _loginOkID2;
	private int _unk;

	@Override
	protected void readImpl()
	{
		_loginOkID1 = readD();
		_loginOkID2 = readD();
		_unk = readC();
	}

	@Override
	protected void runImpl()
	{
		L2LoginClient client = getClient();
		SessionKey skey = client.getSessionKey();
		if(skey == null || !skey.checkLoginPair(_loginOkID1, _loginOkID2))
		{
			client.close(LoginFailReason.REASON_ACCESS_FAILED);
			return;
		}

		client.sendPacket(new ServerList(client.getAccount()));
	}
}
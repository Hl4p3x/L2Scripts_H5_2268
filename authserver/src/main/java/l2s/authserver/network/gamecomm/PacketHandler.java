package l2s.authserver.network.gamecomm;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.authserver.network.gamecomm.gs2as.AuthRequest;
import l2s.authserver.network.gamecomm.gs2as.BonusRequest;
import l2s.authserver.network.gamecomm.gs2as.ChangeAccessLevel;
import l2s.authserver.network.gamecomm.gs2as.ChangeAllowedHwid;
import l2s.authserver.network.gamecomm.gs2as.ChangeAllowedIp;
import l2s.authserver.network.gamecomm.gs2as.ChangePassword;
import l2s.authserver.network.gamecomm.gs2as.ChangePhoneNumber;
import l2s.authserver.network.gamecomm.gs2as.LockAccountIP;
import l2s.authserver.network.gamecomm.gs2as.OnlineStatus;
import l2s.authserver.network.gamecomm.gs2as.PingResponse;
import l2s.authserver.network.gamecomm.gs2as.PlayerAuthRequest;
import l2s.authserver.network.gamecomm.gs2as.PlayerInGame;
import l2s.authserver.network.gamecomm.gs2as.PlayerLogout;
import l2s.authserver.network.gamecomm.gs2as.ReduceAccountPoints;
import l2s.authserver.network.gamecomm.gs2as.SetAccountInfo;

public class PacketHandler
{
	private static Logger _log = LoggerFactory.getLogger(PacketHandler.class);

	public static ReceivablePacket handlePacket(GameServer gs, ByteBuffer buf)
	{
		ReceivablePacket packet = null;

		int id = buf.get() & 0xff;

		if(!gs.isAuthed())
			switch(id)
			{
				case 0x00:
					packet = new AuthRequest();
					break;
				default:
					_log.error("Received unknown packet: " + Integer.toHexString(id));
			}
		else
			switch(id)
			{
				case 0x01:
					packet = new OnlineStatus();
					break;
				case 0x02:
					packet = new PlayerAuthRequest();
					break;
				case 0x03:
					packet = new PlayerInGame();
					break;
				case 0x04:
					packet = new PlayerLogout();
					break;
				case 0x05:
					packet = new SetAccountInfo();
					break;
				case 0x07:
					packet = new ChangeAllowedIp();
					break;
				case 0x08:
					packet = new ChangePassword();
					break;
				case 0x09:
					packet = new ChangeAllowedHwid();
					break;
				case 0x10:
					packet = new BonusRequest();
					break;
				case 0x11:
					packet = new ChangeAccessLevel();
					break;
				case 0x12:
					packet = new ReduceAccountPoints();
					break;
				case 0x0b:
					packet = new LockAccountIP();
					break;
				case 0x0C:
					packet = new ChangePhoneNumber();
					break;
				case 0xff:
					packet = new PingResponse();
					break;
				default:
					_log.error("Received unknown packet: " + Integer.toHexString(id));
			}

		return packet;
	}
}
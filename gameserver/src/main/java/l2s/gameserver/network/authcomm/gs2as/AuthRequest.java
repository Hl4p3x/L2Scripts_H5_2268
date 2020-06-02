package l2s.gameserver.network.authcomm.gs2as;

import java.util.Map.Entry;

import l2s.commons.net.HostInfo;
import l2s.commons.net.utils.Net;
import l2s.gameserver.Config;
import l2s.gameserver.GameServer;
import l2s.gameserver.config.xml.holder.HostsConfigHolder;
import l2s.gameserver.network.authcomm.SendablePacket;

public class AuthRequest extends SendablePacket
{
	@Override
	protected void writeImpl()
	{
		writeC(0x00);
		writeD(GameServer.AUTH_SERVER_PROTOCOL);
		writeD(Config.AUTH_SERVER_SERVER_TYPE);
		writeD(Config.AUTH_SERVER_AGE_LIMIT);
		writeC(Config.AUTH_SERVER_GM_ONLY ? 0x01 : 0x00);
		writeC(Config.AUTH_SERVER_BRACKETS ? 0x01 : 0x00);
		writeC(Config.AUTH_SERVER_IS_PVP ? 0x01 : 0x00);
		writeD(GameServer.getInstance().getOnlineLimit());

		HostInfo[] hosts = HostsConfigHolder.getInstance().getGameServerHosts();
		writeC(hosts.length);
		for(HostInfo host : hosts)
		{
			writeC(host.getId());
			writeS(host.getAddress());
			writeH(host.getPort());
			writeS(host.getKey());
			writeC(host.getSubnets().size());
			for(Entry<Net, String> m : host.getSubnets().entrySet())
			{
				writeS(m.getValue());
				byte[] address = m.getKey().getAddress();
				writeD(address.length);
				writeB(address);
				byte[] mask = m.getKey().getMask();
				writeD(mask.length);
				writeB(mask);
			}
		}
	}
}

package l2s.gameserver.network.l2.s2c;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.tables.FakePlayersTable;

public final class SendStatus extends L2GameServerPacket
{
	private static final long MIN_UPDATE_PERIOD = 30000;
	private static int online_players = 0;
	private static int max_online_players = 0;
	private static int online_priv_store = 0;
	private static long last_update = 0;

	public SendStatus()
	{
		if(System.currentTimeMillis() - last_update < MIN_UPDATE_PERIOD)
			return;
		last_update = System.currentTimeMillis();
		int i = 0;
		int j = 0;
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			i++;
			if(player.isInStoreMode() && (!Config.SENDSTATUS_TRADE_JUST_OFFLINE || player.isInOfflineMode()))
				j++;
		}
		online_players = i + FakePlayersTable.getActiveFakePlayersCount();
		online_priv_store = (int) Math.floor(j * Config.SENDSTATUS_TRADE_MOD);
		max_online_players = Math.max(max_online_players, online_players);
	}

	@Override
	protected final boolean writeOpcodes()
	{
		writeC(0x00);
		return true;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(0x01); // World ID
		writeD(max_online_players); // Max Online
		writeD(online_players); // Current Online
		writeD(online_players); // Current Online
		writeD(online_priv_store); // Priv.Store Chars

		// SEND TRASH
		writeD(0x002C0030);
		for(int x = 0; x < 10; x++)
			writeH(41 + Rnd.get(17));
		writeD(43 + Rnd.get(17));
		int z = 36219 + Rnd.get(1987);
		writeD(z);
		writeD(z);
		writeD(37211 + Rnd.get(2397));
		writeD(0x00);
		writeD(0x02);
	}
}
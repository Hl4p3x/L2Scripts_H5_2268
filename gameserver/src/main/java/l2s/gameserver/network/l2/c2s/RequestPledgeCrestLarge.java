package l2s.gameserver.network.l2.c2s;

import java.util.Arrays;

import gnu.trove.map.TIntObjectMap;
import l2s.gameserver.cache.CrestCache;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExPledgeEmblem;

public class RequestPledgeCrestLarge extends L2GameClientPacket
{
	// format: chd
	private int _crestId;
	private int _pledgeId = 0;

	@Override
	protected void readImpl()
	{
		_crestId = readD();
		if(!getClient().isHFClient())
			_pledgeId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(_crestId == 0)
			return;

		if(getClient().isHFClient())
		{
			byte[] data = CrestCache.getInstance().getPledgeCrestLargeHF(_crestId);
			if(data != null)
				sendPacket(new ExPledgeEmblem(_crestId, data));
		}
		else
		{
			if(_pledgeId == 0)
				return;

			TIntObjectMap<byte[]> data = CrestCache.getInstance().getPledgeCrestLarge(_crestId);
			if(data != null)
			{
				int totalSize = CrestCache.getByteMapSize(data);
				int[] keys = data.keys();
				Arrays.sort(keys);
				for(int key : keys)
					sendPacket(new ExPledgeEmblem(_pledgeId, _crestId, key, totalSize, data.get(key)));
			}
		}
	}
}
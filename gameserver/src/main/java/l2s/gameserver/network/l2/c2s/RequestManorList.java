package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.network.l2.s2c.ExSendManorListPacket;

/**
 * Format: ch
 * c (id) 0xD0
 * h (subid) 0x01
 *
 */
public class RequestManorList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		sendPacket(new ExSendManorListPacket());
	}
}
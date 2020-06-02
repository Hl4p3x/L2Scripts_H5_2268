package l2s.gameserver.model;

import java.util.Iterator;

import l2s.commons.collections.EmptyIterator;
import l2s.gameserver.network.l2.components.IStaticPacket;

/**
 * @author VISTALL
 * @date 14:03/22.06.2011
 */
public interface PlayerGroup extends Iterable<Player>
{
	public static final PlayerGroup EMPTY = new PlayerGroup()
	{
		@Override
		public void broadCast(IStaticPacket... packet)
		{

		}

		@Override
		public Iterator<Player> iterator()
		{
			return EmptyIterator.getInstance();
		}
	};

	void broadCast(IStaticPacket... packet);
}

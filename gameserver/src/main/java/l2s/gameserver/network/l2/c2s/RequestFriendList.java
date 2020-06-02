package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Friend;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestFriendList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		activeChar.sendPacket(Msg._FRIENDS_LIST_);
		for(Friend friend : activeChar.getFriendList().valueCollection())
		{
			Player player = friend.getPlayer();
			if(player != null)
				activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CURRENTLY_ONLINE).addName(player));
			else
				activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CURRENTLY_OFFLINE).addString(friend.getName()));
		}
		activeChar.sendPacket(Msg.__EQUALS__);
	}
}
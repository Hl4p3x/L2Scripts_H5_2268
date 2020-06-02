package l2s.gameserver.network.l2.c2s;

import org.apache.commons.lang3.StringUtils;

import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.network.l2.components.IStaticPacket;
import l2s.gameserver.network.l2.components.SystemMsg;

public class RequestFriendInvite extends L2GameClientPacket
{
	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS(16);
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || StringUtils.isEmpty(_name))
			return;

		activeChar.isntAfk();

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}

		GameObject target = World.getPlayer(_name);

		IStaticPacket msg = activeChar.getFriendList().requestFriendInvite(target);
		if(msg != null)
		{
			activeChar.sendPacket(msg);
			activeChar.sendPacket(SystemMsg.YOU_HAVE_FAILED_TO_ADD_A_FRIEND_TO_YOUR_FRIENDS_LIST);
		}
	}
}
package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.L2FriendSayPacket;
import l2s.gameserver.utils.Log;

/**
 * Recieve Private (Friend) Message
 * Format: c SS
 * S: Message
 * S: Receiving Player
 */
public class RequestSendL2FriendSay extends L2GameClientPacket
{
	private String _message;
	private String _reciever;

	@Override
	protected void readImpl()
	{
		_message = readS(2048);
		_reciever = readS(16);
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.getNoChannel() != 0)
		{
			if(activeChar.getNoChannelRemained() > 0 || activeChar.getNoChannel() < 0)
			{
				activeChar.sendPacket(SystemMsg.CHATTING_IS_CURRENTLY_PROHIBITED_);
				return;
			}
			activeChar.updateNoChannel(0);
		}

		Player targetPlayer = World.getPlayer(_reciever);
		if(targetPlayer == null)
		{
			activeChar.sendPacket(SystemMsg.THAT_PLAYER_IS_NOT_ONLINE);
			return;
		}

		if(targetPlayer.isBlockAll())
		{
			activeChar.sendPacket(SystemMsg.THAT_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
			return;
		}

		if(!activeChar.getFriendList().contains(targetPlayer.getObjectId()))
			return;

		Log.LogEvent(activeChar.getName(), activeChar.getIP(), "FriendChat", "Friend:", ""+targetPlayer.getName()+" message: "+_message+"");				

		L2FriendSayPacket frm = new L2FriendSayPacket(activeChar.getName(), _reciever, _message);
		targetPlayer.sendPacket(frm);
		Log.LogChat("FRIENDTELL", activeChar.getName(), _reciever, _message);
	}
}
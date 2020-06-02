package l2s.gameserver.network.l2.c2s;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Block;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.utils.Log;

public class RequestBlock extends L2GameClientPacket
{
	// format: cd(S)
	private static final Logger _log = LoggerFactory.getLogger(RequestBlock.class);

	private final static int BLOCK = 0;
	private final static int UNBLOCK = 1;
	private final static int BLOCKLIST = 2;
	private final static int ALLBLOCK = 3;
	private final static int ALLUNBLOCK = 4;

	private Integer _type;
	private String targetName = null;

	@Override
	protected void readImpl()
	{
		_type = readD(); //0x00 - block, 0x01 - unblock, 0x03 - allblock, 0x04 - allunblock

		if(_type == BLOCK || _type == UNBLOCK)
			targetName = readS(16);
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		switch(_type)
		{
			case BLOCK:
				activeChar.getBlockList().add(targetName);
				Log.LogEvent(activeChar.getName(), "Block", "BlockedPlayer", "blocked: "+targetName+"");
				break;
			case UNBLOCK:
				activeChar.getBlockList().remove(targetName);
				Log.LogEvent(activeChar.getName(), "Block", "UnBlockedPlayer", "unblocked: "+targetName+"");
				break;
			case BLOCKLIST:
				activeChar.sendPacket(Msg._IGNORE_LIST_);

				for(Block block : activeChar.getBlockList().valueCollection())
					activeChar.sendMessage(block.getName());

				activeChar.sendPacket(Msg.__EQUALS__);
				break;
			case ALLBLOCK:
				activeChar.setBlockAll(true);
				activeChar.sendPacket(SystemMsg.YOU_ARE_NOW_BLOCKING_EVERYTHING);
				activeChar.sendEtcStatusUpdate();
				Log.LogEvent(activeChar.getName(), "Block", "BlockedAll", "blockedall");
				break;
			case ALLUNBLOCK:
				activeChar.setBlockAll(false);
				activeChar.sendPacket(SystemMsg.YOU_ARE_NO_LONGER_BLOCKING_EVERYTHING);
				activeChar.sendEtcStatusUpdate();
				Log.LogEvent(activeChar.getName(), "Block", "UnBlockedPAll", "unblockedall");
				break;
			default:
				_log.info("Unknown 0x0a block type: " + _type);
		}
	}
}
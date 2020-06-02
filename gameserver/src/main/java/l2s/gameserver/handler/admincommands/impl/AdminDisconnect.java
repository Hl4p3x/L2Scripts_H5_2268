package l2s.gameserver.handler.admincommands.impl;

import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.network.l2.components.CustomMessage;

public class AdminDisconnect implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_disconnect,
		admin_kick
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().CanKick)
			return false;

		switch(command)
		{
			case admin_disconnect:
			case admin_kick:
				final Player player;
				if(wordList.length == 1)
				{
					// Обработка по таргету
					GameObject target = activeChar.getTarget();
					if(target == null)
					{
						activeChar.sendMessage(new CustomMessage("common.Admin.Disconect.ErrorName", activeChar));
						break;
					}
					if(!target.isPlayer())
					{
						activeChar.sendPacket(Msg.INVALID_TARGET);
						break;
					}
					player = (Player) target;
				}
				else
				{
					// Обработка по нику
					player = World.getPlayer(wordList[1]);
					if(player == null)
					{
						activeChar.sendMessage(new CustomMessage("common.Admin.Disconect.ErrorName404", activeChar).addString(wordList[1]));
						break;
					}
				}

				if(player.getObjectId() == activeChar.getObjectId())
				{
					activeChar.sendMessage(new CustomMessage("common.Admin.Disconect.ErrorSelf", activeChar));
					break;
				}
				activeChar.sendMessage(new CustomMessage("common.Admin.Disconect.SuccessKicked", activeChar).addString(player.getName()));

				if(player.isInOfflineMode())
				{
					player.setOfflineMode(false);
					player.kick();
					return true;
				}

				player.sendMessage(new CustomMessage("admincommandhandlers.AdminDisconnect.YoureKickedByGM", player));
				player.sendPacket(Msg.YOU_HAVE_BEEN_DISCONNECTED_FROM_THE_SERVER_PLEASE_LOGIN_AGAIN);
				ThreadPoolManager.getInstance().schedule(new RunnableImpl()
				{
					@Override
					public void runImpl() throws Exception
					{
						player.kick();
					}
				}, 500);
				break;
		}
		return true;
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}
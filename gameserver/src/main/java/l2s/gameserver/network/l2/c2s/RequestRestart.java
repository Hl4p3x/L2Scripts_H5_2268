package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2s.gameserver.network.l2.GameClient.GameClientState;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.network.l2.s2c.CharacterSelectionInfoPacket;
import l2s.gameserver.network.l2.s2c.RestartResponsePacket;

public class RequestRestart extends L2GameClientPacket
{
	/**
	 * packet type id 0x57
	 * format:      c
	 */

	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		if(activeChar.isInObserverMode())
		{
			activeChar.sendPacket(Msg.OBSERVERS_CANNOT_PARTICIPATE, RestartResponsePacket.FAIL, ActionFailPacket.STATIC);
			return;
		}

		if(activeChar.isInCombat())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_RESTART_WHILE_IN_COMBAT, RestartResponsePacket.FAIL, ActionFailPacket.STATIC);
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_ANYTHING_ELSE_WHILE_FISHING, RestartResponsePacket.FAIL, ActionFailPacket.STATIC);
			return;
		}

		if(activeChar.isBlocked() && !activeChar.isFlying() && !activeChar.isInAwayingMode()) // Разрешаем выходить из игры если используется сервис HireWyvern. Вернет в начальную точку.
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestRestart.OutOfControl", activeChar));
			activeChar.sendPacket(RestartResponsePacket.FAIL, ActionFailPacket.STATIC);
			return;
		}

		if(activeChar.isInFightClub())
		{
			activeChar.sendMessage("You need to leave Fight Club first!"); // TODO: Вынести в ДП.
			activeChar.sendPacket(RestartResponsePacket.FAIL, ActionFailPacket.STATIC);
			return;
		}

		// Prevent player from restarting if they are a festival participant
		// and it is in progress, otherwise notify party members that the player
		// is not longer a participant.
		if(activeChar.isFestivalParticipant())
			if(SevenSignsFestival.getInstance().isFestivalInitialized())
			{
				activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestRestart.Festival", activeChar));
				activeChar.sendPacket(RestartResponsePacket.FAIL, ActionFailPacket.STATIC);
				return;
			}

		if(getClient() != null)
			getClient().setState(GameClientState.AUTHED);
		if(activeChar.isInBuffStore())
			activeChar.offlineBuffStore();
		else	
			activeChar.restart();
		// send char list
		CharacterSelectionInfoPacket cl = new CharacterSelectionInfoPacket(getClient().getLogin(), getClient().getSessionKey().playOkID1);
		sendPacket(RestartResponsePacket.OK, cl);
		getClient().setCharSelection(cl.getCharInfo());
	}
}
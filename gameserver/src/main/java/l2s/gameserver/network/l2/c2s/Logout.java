package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.utils.Log;

public class Logout extends L2GameClientPacket
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

		// Dont allow leaving if player is fighting
		if(activeChar.isInCombat())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_EXIT_THE_GAME_WHILE_IN_COMBAT);
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2);
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isBlocked() && !activeChar.isFlying() && !activeChar.isInAwayingMode()) // Разрешаем выходить из игры если используется сервис HireWyvern. Вернет в начальную точку.
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.Logout.OutOfControl", activeChar));
			activeChar.sendActionFailed();
			return;
		}

		// Prevent player from logging out if they are a festival participant
		// and it is in progress, otherwise notify party members that the player
		// is not longer a participant.
		if(activeChar.isFestivalParticipant())
			if(SevenSignsFestival.getInstance().isFestivalInitialized())
			{
				activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.Logout.Festival", activeChar));
				//activeChar.sendMessage("You cannot log out while you are a participant in a festival.");
				activeChar.sendActionFailed();
				return;
			}

		if(activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.Logout.Olympiad", activeChar));
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInFightClub())
		{
			activeChar.sendMessage("Leave Fight Club first!"); // TODO: Вынести в ДП.
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInObserverMode())
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.Logout.Observer", activeChar));
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInPvPEvent())
		{
			activeChar.sendMessage(new CustomMessage("Away.ActionFailed", activeChar));
			activeChar.sendActionFailed();
			return;		
		}
		if(activeChar.isInBuffStore())
		{
			activeChar.offlineBuffStore();
		} 
		else 
		{
			activeChar.kick();
		}		
		Log.LogEvent(activeChar.getName(), "LogOut", "Logout", "char: "+activeChar.getName()+" logged out from the game");
	}
}
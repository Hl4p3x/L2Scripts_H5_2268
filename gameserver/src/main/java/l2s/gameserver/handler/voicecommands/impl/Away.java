package l2s.gameserver.handler.voicecommands.impl;

import org.apache.commons.lang3.StringUtils;

import l2s.gameserver.Config;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.instancemanager.AwayManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.network.l2.components.CustomMessage;

/**
 * @author: Kolobrodik
 * @date: 17:38/25.05.2012
 * @description: Данный класс предоставляет собой обработку команд для сервиса "Away"
 */
public class Away implements IVoicedCommandHandler
{
	private String[] VOICED_COMMANDS = new String[]{"away", "back"};

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String text)
	{
		if(command.startsWith("away"))
		{
			if(Config.AWAY_ONLY_FOR_PREMIUM && !activeChar.hasPremiumAccount())
			{
				activeChar.sendMessage(new CustomMessage("PremiumOnly", activeChar));
				return false;
			}
			return away(activeChar, text);
		}
		else if(command.startsWith("back"))
			return back(activeChar);
		return false;
	}

	private boolean away(Player activeChar, String text)
	{
		//check char is all ready in away mode
		if(activeChar.isInAwayingMode())
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.handler.voicecommands.impl.Away.Already", activeChar));
			return false;
		}

		if(!activeChar.isInZone(ZoneType.peace_zone) && Config.AWAY_PEACE_ZONE)
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.handler.voicecommands.impl.Away.PieceOnly", activeChar));
			return false;
		}

		//check player is death/fake death and movement disable
		if(activeChar.isMovementDisabled() || activeChar.isAlikeDead())
			return false;

		// Check if player is in Siege
		SiegeEvent<?, ?> siege = activeChar.getEvent(SiegeEvent.class);
		if(siege != null)
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.handler.voicecommands.impl.Away.Siege", activeChar));
			return false;
		}

		// Check if player is a Cursed Weapon owner
		if(activeChar.isCursedWeaponEquipped())
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.handler.voicecommands.impl.Away.Cursed", activeChar));
			return false;
		}

		// Check if player is in Duel
		if(activeChar.isInDuel())
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.handler.voicecommands.impl.Away.Duel", activeChar));
			return false;
		}

		//check is in DimensionsRift
		if(activeChar.isInParty() && activeChar.getParty().isInDimensionalRift())
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.handler.voicecommands.impl.Away.Rift", activeChar));
			return false;
		}

		// Check to see if the player is in an event
		/*if(activeChar.isInFunEvent())
		{
			activeChar.sendMessage("You can't go Afk! You are in event now.");
			return false;
		}*/

		//check player is in Olympiade
		if(activeChar.isInOlympiadMode() || activeChar.getOlympiadGame() != null)
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.handler.voicecommands.impl.Away.Olympiad", activeChar));
			return false;
		}

		// Check player is in observer mode
		if(activeChar.isInObserverMode())
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.handler.voicecommands.impl.Away.Observer", activeChar));
			return false;
		}

		//check player have karma/pk/pvp status
		if(activeChar.getKarma() > 0 || activeChar.getPvpFlag() > 0)
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.handler.voicecommands.impl.Away.Pvp", activeChar));
			return false;
		}

		if(text == null)
			text = StringUtils.EMPTY;

		//check away text have not more then 10 letter
		if(text.length() > 10)
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.handler.voicecommands.impl.Away.Text", activeChar));
			return false;
		}

		// check if player have no one in target
		if(activeChar.getTarget() == null)
			//set this Player status away in AwayManager
			AwayManager.getInstance().setAway(activeChar, text);
		else
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.handler.voicecommands.impl.Away.Target", activeChar));
			return false;
		}

		return true;
	}

	private boolean back(Player activeChar)
	{
		if(!activeChar.isInAwayingMode())
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.handler.voicecommands.impl.Away.Not", activeChar));
			return false;
		}
		AwayManager.getInstance().setBack(activeChar);
		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}

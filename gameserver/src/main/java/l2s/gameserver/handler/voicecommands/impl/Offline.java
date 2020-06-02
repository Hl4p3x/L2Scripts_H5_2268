package l2s.gameserver.handler.voicecommands.impl;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.utils.ItemFunctions;

public class Offline extends Functions implements IVoicedCommandHandler
{
	private String[] _commandList = new String[] { "offline" };

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		if(!Config.ALLOW_VOICED_COMMANDS)
			return false;

		if(!Config.SERVICES_OFFLINE_TRADE_ALLOW)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Offline.Disabled", activeChar).toString());
			return false;
		}

		if(activeChar.getOlympiadObserveGame() != null || activeChar.getOlympiadGame() != null || Olympiad.isRegisteredInComp(activeChar) || activeChar.getKarma() > 0)
		{
			activeChar.sendActionFailed();
			return false;
		}

		if(activeChar.getLevel() < Config.SERVICES_OFFLINE_TRADE_MIN_LEVEL)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Offline.LowLevel", activeChar).addNumber(Config.SERVICES_OFFLINE_TRADE_MIN_LEVEL).toString());
			return false;
		}

		if(!activeChar.isInStoreMode())
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Offline.IncorrectUse", activeChar).toString());
			return false;
		}

		if(activeChar.getNoChannelRemained() > 0)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Offline.BanChat", activeChar).toString());
			return false;
		}

		switch(Config.SERVICES_OFFLINE_TRADE_ALLOW_ZONE)
		{
			case 1:
				if(!activeChar.isInPeaceZone())
				{
					activeChar.sendMessage(new CustomMessage("trade.OfflineNoTradeZoneOnlyPeace", activeChar).toString());
					return false;
				}
				break;
			case 2:
				if(!activeChar.isInZone(Zone.ZoneType.offshore))
				{
					activeChar.sendMessage(new CustomMessage("trade.OfflineNoTradeZoneOnlyOffshore", activeChar).toString());
					return false;
				}
				break;
		}

		if(Config.ALLOWED_TRADE_ZONES.length > 0)
		{
			boolean inTradeZone = false;
			for(String zoneName : Config.ALLOWED_TRADE_ZONES)
			{
				if(activeChar.isInZone(zoneName))
				{
					inTradeZone = true;
					break;
				}
			}

			if(!inTradeZone)
			{
				activeChar.sendPacket(activeChar.getPrivateStoreType() == Player.STORE_PRIVATE_MANUFACTURE ? new SystemMessage(SystemMessage.A_PRIVATE_WORKSHOP_MAY_NOT_BE_OPENED_IN_THIS_AREA) : Msg.A_PRIVATE_STORE_MAY_NOT_BE_OPENED_IN_THIS_AREA);
				return false;
			}
		}

		if(activeChar.isActionBlocked(Zone.BLOCKED_ACTION_PRIVATE_STORE))
		{
			activeChar.sendMessage(new CustomMessage("trade.OfflineNoTradeZone", activeChar).toString());
			return false;
		}

		if(Config.OFFLINE_ONLY_IF_PREMIUM && !activeChar.hasPremiumAccount())
		{
			activeChar.sendMessage("You cannot use this feature as long you don't have an active premium account!");
			return false;
		}

		if(Config.SERVICES_OFFLINE_TRADE_PRICE > 0 && Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM > 0)
		{
			if(ItemFunctions.getItemCount(activeChar, Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM) < Config.SERVICES_OFFLINE_TRADE_PRICE)
			{
				activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Offline.NotEnough", activeChar).addItemName(Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM).addNumber(Config.SERVICES_OFFLINE_TRADE_PRICE).toString());
				return false;
			}
			ItemFunctions.deleteItem(activeChar, Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM, Config.SERVICES_OFFLINE_TRADE_PRICE);
		}

		activeChar.offline();
		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}
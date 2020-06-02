package l2s.gameserver.model.entity.CCPHelpers;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.utils.ItemFunctions;

public class CCPOffline
{
	public static boolean setOfflineStore(Player activeChar)
	{
		if(!Config.SERVICES_OFFLINE_TRADE_ALLOW)
		{
			activeChar.sendMessage("This option is currently disabled!");
			return false;
		}
    
		if(activeChar.getOlympiadObserveGame() != null || activeChar.getOlympiadGame() != null || Olympiad.isRegisteredInComp(activeChar) || activeChar.getKarma() > 0)
		{
			activeChar.sendMessage("You cannot do it right now!");
			return false;
		}
    
		if(activeChar.getLevel() < Config.SERVICES_OFFLINE_TRADE_MIN_LEVEL)
		{
			activeChar.sendMessage("Your level is too low!");
			return false;
		}

		switch(Config.SERVICES_OFFLINE_TRADE_ALLOW_ZONE)
		{
			case 1:
				if(!activeChar.isInPeaceZone())
				{
					activeChar.sendMessage("You cannot set offline store in this area!");
					return false;
				}
				break;
			case 2:
				if(!activeChar.isInZone(Zone.ZoneType.offshore))
				{
					activeChar.sendMessage("You cannot set offline store in this area!");
					return false;
				}
				break;
		}
    
		if(!activeChar.isInStoreMode())
		{
			activeChar.sendMessage("You need to place Private Store first!");
			return false;
		}
    
		if(activeChar.getNoChannelRemained() > 0L)
		{
			activeChar.sendMessage("You cannot set offline store while having Chat Ban!");
			return false;
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
				activeChar.sendPacket(Msg.A_PRIVATE_STORE_MAY_NOT_BE_OPENED_IN_THIS_AREA);
				return false;
			}
		}

		if(activeChar.isActionBlocked(Zone.BLOCKED_ACTION_PRIVATE_STORE))
		{
			activeChar.sendMessage("You cannot set offline store in this area!");
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
				Functions.show(new CustomMessage("voicedcommandhandlers.Offline.NotEnough", activeChar, new Object[0]).addItemName(Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM).addNumber(Config.SERVICES_OFFLINE_TRADE_PRICE), activeChar);
				return false;
			}
			ItemFunctions.deleteItem(activeChar, Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM, Config.SERVICES_OFFLINE_TRADE_PRICE);
		}
		activeChar.offline();
		return true;
	}
}

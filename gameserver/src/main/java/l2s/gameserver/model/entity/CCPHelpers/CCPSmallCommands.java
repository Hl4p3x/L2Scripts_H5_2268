package l2s.gameserver.model.entity.CCPHelpers;

import java.util.ArrayList;
import java.util.List;

import l2s.commons.dao.JdbcEntityState;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.InventoryUpdatePacket;
import l2s.gameserver.tables.FakePlayersTable;
import l2s.gameserver.utils.ItemFunctions;

public class CCPSmallCommands
{
	private static final int DECREASE_LEVEL_REQUIREMENT_ID = 6673;
	private static final long DECREASE_LEVEL_REQUIREMENT_COUNT = 1L;
  
	public static void openToad(Player activeChar, long count)
	{
		if(activeChar.getInventory().getItemByItemId(9599) == null)
		{
			activeChar.sendMessage("You do not have enough Ancient Tomes of the Demon.");
			return;
		}
		if(count <= 0L)
			count = activeChar.getInventory().getItemByItemId(9599).getCount();
		if(activeChar.getInventory().getItemByItemId(9599).getCount() >= count)
		{
			int a = 0;int b = 0;int c = 0;
			for(int i = 0; i < count; i++)
			{
				int rnd = Rnd.get(100);
				if(rnd <= 100 && rnd >= 66)
					a++;
				else if(rnd <= 65 && rnd >= 31)
					b++;
				else if (rnd <= 30)
					c++;
				else
					activeChar.sendMessage("You do not have enough Ancient Tomes of the Demon.");
			}  
			if(activeChar.getInventory().destroyItemByItemId(9599, a + b + c))
			{
				if (a > 0)
					ItemFunctions.addItem(activeChar, 9600, a, true, "Exchange Ancient Tomes of the Demon by CCPSmallCommands");
				if(b > 0)
					ItemFunctions.addItem(activeChar, 9601, b, true, "Exchange Ancient Tomes of the Demon by CCPSmallCommands");
				if (c > 0)
					ItemFunctions.addItem(activeChar, 9602, c, true, "Exchange Ancient Tomes of the Demon by CCPSmallCommands");
			}
			else
				activeChar.sendMessage("You do not have enough Ancient Tomes of the Demon.");
		}
		else
			activeChar.sendMessage("You do not have enough Ancient Tomes of the Demon.");
	}
  
	public static void setAntiGrief(Player activeChar)
	{
		if(!activeChar.getVarBoolean("antigrief", false))
		{
			activeChar.setVar("antigrief", "true", -1L);
			activeChar.sendMessage("You are now protected from unwanted buffs!");
		}
		else
		{
			activeChar.unsetVar("antigrief");
			activeChar.sendMessage("You are NO LONGER protected from unwanted buffs!");
		}
	}
  
	public static String showOnlineCount()
	{
		if(!Config.ALLOW_TOTAL_ONLINE)
			return "0";
		int i = 0;
		int j = 0;
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			i++;
			if(player.isInOfflineMode())
				j++;
		}
		i = i + FakePlayersTable.getActiveFakePlayersCount();	
    
		return "There are currently " + (i + j) + " players online, from those " + j + " are offline traders.";
  }
  
	public static boolean getPing(Player activeChar)
	{
		activeChar.sendMessage("Processing request...");
		activeChar.sendPacket(new l2s.gameserver.network.l2.s2c.NetPingPacket(activeChar.getObjectId()));
		ThreadPoolManager.getInstance().schedule(new AnswerTask(activeChar), 3000L);
		return true;
	}
  
	private static final class AnswerTask implements Runnable
	{
		private final Player _player;
    
		public AnswerTask(Player player)
		{
			_player = player;
		}
    
		public void run()
		{
			int ping = _player.getPing();
			if (ping != -1)
				_player.sendMessage("Current ping: " + ping + " ms.");
			else
				_player.sendMessage("The data from the client was not received.");
		}
	}
  
	public static void combineTalismans(Player activeChar)
	{
		List<int[]> sameIds = new ArrayList<int[]>();

		for(ItemInstance item : activeChar.getInventory().getItems())
		{
			if(item.getLifeTime() > 0 && item.getName().contains("Talisman"))
				talismanAddToCurrent(sameIds, item.getItemId());
		}

		int allCount = 0;
		int newCount = 0;
		for(int[] idCount : sameIds)
		{
			if (idCount[1] > 1)
			{
				int lifeTime = 0;
				List<ItemInstance> existingTalismans = activeChar.getInventory().getItemsByItemId(idCount[0]);
				for(ItemInstance existingTalisman : existingTalismans)
				{
					lifeTime += existingTalisman.getLifeTime();
					activeChar.getInventory().destroyItem(existingTalisman);
				}

				ItemInstance newTalisman = activeChar.getInventory().addItem(idCount[0], 1L);
				newTalisman.setLifeTime(lifeTime);
				newTalisman.setJdbcState(JdbcEntityState.UPDATED);
				newTalisman.update();
				activeChar.sendPacket(new InventoryUpdatePacket().addModifiedItem(activeChar, newTalisman));

				allCount += idCount[0];
				newCount++;
			}
		}

		if(allCount > 0)
			activeChar.sendMessage(allCount + " Talismans were combined into " + newCount);
		else
			activeChar.sendMessage("You don't have Talismans to combine!");
	}
  
	private static void talismanAddToCurrent(List<int[]> sameIds, int itemId)
	{
		for(int i = 0; i < sameIds.size(); i++)
		{
			if(sameIds.get(i)[0] == itemId)
			{
				sameIds.get(i)[1] += 1;
				return;
			}
		}	
		int[] newInt = { itemId, 1 };
		sameIds.add(newInt);
	}
  
	public static boolean decreaseLevel(Player activeChar, int levelsToRemove)
	{
		if(levelsToRemove <= 0)
		{
			activeChar.sendMessage("You need to write value above 0");
			return false;
		}
    
		if(levelsToRemove >= activeChar.getLevel())
		{
			activeChar.sendMessage("Level to decrease cannot be bigger than " + (activeChar.getLevel() - 1));
			return false;
		}
    
		if(!activeChar.getInventory().destroyItemByItemId(6673, 1L))
		{
			activeChar.sendMessage("You don't have enough Event Pounds!");
			return false;
		}
    
		int oldLevel = activeChar.getLevel();
		int newLevel = oldLevel - levelsToRemove;
		long expToRemove = l2s.gameserver.model.base.Experience.LEVEL[newLevel] - activeChar.getExp();
		expToRemove -= 1000L;
    
		activeChar.getActiveSubClass().addExp(expToRemove);
		activeChar.levelSet(newLevel - oldLevel);
		activeChar.updateStats();
		activeChar.sendMessage(levelsToRemove + " levels were decreased!");
		return true;
	}
}

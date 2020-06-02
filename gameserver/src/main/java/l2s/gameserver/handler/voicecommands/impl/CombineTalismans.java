package l2s.gameserver.handler.voicecommands.impl;

import java.util.ArrayList;
import java.util.List;

import l2s.commons.dao.JdbcEntityState;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.InventoryUpdatePacket;
import l2s.gameserver.scripts.Functions;

public class CombineTalismans extends Functions implements IVoicedCommandHandler
{
	private static final String[] COMMANDS = { "combinetalismans" };
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		combineTalismans(activeChar);
		return true;
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
			if (sameIds.get(i)[0] == itemId)
			{
				sameIds.get(i)[1] += 1;
				return;
			}
		}	
		int[] newInt = { itemId, 1 };
		sameIds.add(newInt);
	}
  
	@Override
	public String[] getVoicedCommandList()
	{
		return COMMANDS;
	}
}
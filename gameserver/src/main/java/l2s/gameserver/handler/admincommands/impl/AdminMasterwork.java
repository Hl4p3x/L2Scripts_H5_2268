package l2s.gameserver.handler.admincommands.impl;

import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;
import l2s.gameserver.utils.ItemFunctions;

public class AdminMasterwork implements l2s.gameserver.handler.admincommands.IAdminCommandHandler
{
	private static final int[] SLOTS_TO_MASTERWORK = { 7, 8, 6, 11, 9, 12 };
  
	private static enum Commands
	{
		admin_masterwork, 
		admin_create_masterwork;
    
		private Commands() {}
	}
	
	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands)comm;
    
		if(!activeChar.getPlayerAccess().CanEditChar)
			return false;
		Player target;
		if(activeChar.getTarget() != null && activeChar.getTarget().isPlayer())
			target = activeChar.getTarget().getPlayer();
		else
			target = activeChar;
		switch(command.ordinal())
		{
			case 1: 
				showMainMasterwork(activeChar, target);
				break;
			case 2: 
				int slot = Integer.parseInt(wordList[1]);
				createMasterwork(activeChar, target, slot);
				showMainMasterwork(activeChar, target);
				break;
		}
		return true;
	}
  
	private static void showMainMasterwork(Player activeChar, Player target)
	{
		String html = HtmCache.getInstance().getIfExists("admin/masterwork.htm", activeChar);
		StringBuilder main = new StringBuilder("<table width=250>");
    
		for (int slot : SLOTS_TO_MASTERWORK)
		{
			ItemInstance item = target.getInventory().getPaperdollItem(slot);
			if(item != null && item.getTemplate().getMasterworkConvert() > 0)
			{
				main.append("<tr><td width=250>");
				main.append("<center>").append(item.getName());
				main.append("<br1>");
				main.append("<button value=\"Make Masterwork\" action=\"bypass -h admin_create_masterwork ").append(slot).append("\" width=200 height=25 back=\"L2UI_ct1.button_df_down\" fore=\"L2UI_ct1.button_df\"></center>");
				main.append("</td></tr>");
			}
		}
		main.append("</table>");
    
		html = html.replace("%main%", main.toString());
		activeChar.sendPacket(new NpcHtmlMessagePacket(0).setHtml(html));
	}
  
	private static void createMasterwork(Player activeChar, Player target, int slot)
	{
		ItemInstance item = target.getInventory().getPaperdollItem(slot);
		if(item != null && item.getTemplate().getMasterworkConvert() > 0)
		{
			convertToMasterwork(target, item);
			activeChar.sendMessage("Item was converted to Masterwork!");
		}
		else
		{
			activeChar.sendMessage("Item couldn't be converted!");
		}
	}

	private static void convertToMasterwork(Player target, ItemInstance item)
	{
		ItemInstance newItem = ItemFunctions.createItem(item.getTemplate().getMasterworkConvert());
		newItem.setEnchantLevel(item.getEnchantLevel());
		newItem.setAttributes(item.getAttributes());
		newItem.setVariationStoneId(item.getVariationStoneId());
		newItem.setVariation1Id(item.getVariation1Id());
		newItem.setVariation2Id(item.getVariation2Id());

		target.getInventory().destroyItem(item);
		target.getInventory().addItem(newItem);
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}
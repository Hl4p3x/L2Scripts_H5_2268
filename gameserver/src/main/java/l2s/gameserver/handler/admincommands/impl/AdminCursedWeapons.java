package l2s.gameserver.handler.admincommands.impl;

import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.instancemanager.CursedWeaponsManager;
import l2s.gameserver.model.CursedWeapon;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.utils.ItemFunctions;

public class AdminCursedWeapons implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_cw_info,
		admin_cw_remove,
		admin_cw_goto,
		admin_cw_reload,
		admin_cw_add,
		admin_cw_drop
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().Menu)
			return false;

		CursedWeaponsManager cwm = CursedWeaponsManager.getInstance();

		CursedWeapon cw = null;
		switch(command)
		{
			case admin_cw_remove:
			case admin_cw_goto:
			case admin_cw_add:
			case admin_cw_drop:
				if(wordList.length < 2)
				{
					activeChar.sendMessage(new CustomMessage("common.Admin.CW.ErrorId", activeChar));				
					return false;
				}
				for(CursedWeapon cwp : CursedWeaponsManager.getInstance().getCursedWeapons())
					if(cwp.getName().toLowerCase().contains(wordList[1].toLowerCase()))
						cw = cwp;
				if(cw == null)
				{
					activeChar.sendMessage(new CustomMessage("common.Admin.CW.ErrorId404", activeChar));				
					return false;
				}
				break;
		}

		switch(command)
		{
			case admin_cw_info:
				activeChar.sendMessage(new CustomMessage("common.Admin.CW.Info1", activeChar));	
				for(CursedWeapon c : cwm.getCursedWeapons())
				{
					activeChar.sendMessage(new CustomMessage("common.Admin.CW.Info2", activeChar).addString(c.getName()).addNumber(c.getItemId()));
					if(c.isActivated())
					{
						Player pl = c.getPlayer();
						activeChar.sendMessage(new CustomMessage("common.Admin.CW.Info3", activeChar).addString(c.getName()));
						activeChar.sendMessage(new CustomMessage("common.Admin.CW.Info4", activeChar).addNumber(c.getPlayerKarma()));
						activeChar.sendMessage(new CustomMessage("common.Admin.CW.Info5", activeChar).addNumber(c.getTimeLeft() / 60000));
						activeChar.sendMessage(new CustomMessage("common.Admin.CW.Info6", activeChar).addNumber(c.getNbKills()));
					}
					else if(c.isDropped())
					{
						activeChar.sendMessage(new CustomMessage("common.Admin.CW.Info7", activeChar));	
						activeChar.sendMessage(new CustomMessage("common.Admin.CW.Info5", activeChar).addNumber(c.getTimeLeft() / 60000));
						activeChar.sendMessage(new CustomMessage("common.Admin.CW.Info6", activeChar).addNumber(c.getNbKills()));
					}
					else
						activeChar.sendMessage(new CustomMessage("common.Admin.CW.ErrorNotExist", activeChar));	
				}
				break;
			case admin_cw_reload:
				activeChar.sendMessage(new CustomMessage("common.Admin.CW.ReloadCW", activeChar));
				break;
			case admin_cw_remove:
				if(cw == null)
					return false;
				CursedWeaponsManager.getInstance().endOfLife(cw);
				break;
			case admin_cw_goto:
				if(cw == null)
					return false;
				activeChar.teleToLocation(cw.getLoc());
				break;
			case admin_cw_add:
				if(cw == null)
					return false;
				if(cw.isActive())
					activeChar.sendMessage(new CustomMessage("common.Admin.CW.AlreadyActive", activeChar));
				else
				{
					GameObject target = activeChar.getTarget();
					if(target != null && target.isPlayer() && !((Player) target).isInOlympiadMode())
					{
						Player player = (Player) target;
						ItemInstance item = ItemFunctions.createItem(cw.getItemId());
						cwm.activate(player, player.getInventory().addItem(item), false);
						cwm.showUsageTime(player, cw);
					}
				}
				break;
			case admin_cw_drop:
				if(cw == null)
					return false;
				if(cw.isActive())
					activeChar.sendMessage(new CustomMessage("common.Admin.CW.AlreadyActive", activeChar));
				else
				{
					GameObject target = activeChar.getTarget();
					if(target != null && target.isPlayer() && !((Player) target).isInOlympiadMode())
					{
						Player player = (Player) target;
						cw.create(null, player);
					}
				}
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
package handler.items;

import l2s.gameserver.handler.items.IItemHandler;
import l2s.gameserver.handler.items.ItemHandler;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.scripts.ScriptFile;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.Log;

/**
 * @author VISTALL
 * @date 21:09/12.07.2011
 */
public abstract class ScriptItemHandler implements ScriptFile, IItemHandler
{
	@Override
	public void dropItem(Player player, ItemInstance item, long count, Location loc)
	{
		if(item.isEquipped())
		{
			player.getInventory().unEquipItem(item);
			player.sendUserInfo(true);
		}

		item = player.getInventory().removeItemByObjectId(item.getObjectId(), count);
		if(item == null)
		{
			player.sendActionFailed();
			return;
		}

		Log.LogEvent(player.getName(), player.getIP(), "ItemHandler", "player droped "+item.getItemId()+"");

		Log.LogItem(player, Log.Drop, item);

		item.dropToTheGround(player, loc);
		player.disableDrop(1000);

		player.sendChanges();
	}

	@Override
	public boolean pickupItem(Playable playable, ItemInstance item)
	{
		return true;
	}

	@Override
	public boolean isAutoUse()
	{
		return false;
	}

	@Override
	public void onLoad()
	{
		ItemHandler.getInstance().registerItemHandler(this);
	}

	@Override
	public void onReload()
	{

	}

	@Override
	public void onShutdown()
	{

	}
}

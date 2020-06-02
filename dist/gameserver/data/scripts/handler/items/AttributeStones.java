package handler.items;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.ExChooseInventoryAttributeItemPacket;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;

/**
 * @author SYS
 */
public class AttributeStones extends ScriptItemHandler
{
	private static final int[] _itemIds = {
		9546,
		9547,
		9548,
		9549,
		9550,
		9551,
		9552,
		9553,
		9554,
		9555,
		9556,
		9557,
		9558,
		9563,
		9561,
		9560,
		9562,
		9559,
		9567,
		9566,
		9568,
		9565,
		9564,
		9569,
		10521,
		10522,
		10523,
		10524,
		10525,
		10526 };

	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
			return false;
		Player player = (Player) playable;

		if(player.getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
		{
			player.sendPacket(Msg.YOU_CANNOT_ADD_ELEMENTAL_POWER_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			return false;
		}

		if(player.getEnchantScroll() != null)
			return false;

		player.setEnchantScroll(item);
		if(ctrl && Config.ENABLE_AUTO_ATTRIBUTE_SYSTEM)
		{
			player.setAutoAttributeItem(item);
			player.sendPacket(new ExShowScreenMessage(new CustomMessage("handler.items.AttributeStones.autoAttribute", player).toString(), 1500, ScreenMessageAlign.BOTTOM_CENTER, true));
		}
		else
			player.setAutoAttributeItem(null);
		player.sendPacket(Msg.PLEASE_SELECT_ITEM_TO_ADD_ELEMENTAL_POWER);
		player.sendPacket(new ExChooseInventoryAttributeItemPacket(item));
		return true;
	}

	@Override
	public final int[] getItemIds()
	{
		return _itemIds;
	}
}
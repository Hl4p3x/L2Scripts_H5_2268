package handler.items;

import l2s.gameserver.data.xml.holder.EnchantItemHolder;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.ChooseInventoryItemPacket;

public class EnchantScrolls extends ScriptItemHandler
{
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
			return false;
		Player player = (Player) playable;

		if(player.getEnchantScroll() != null)
			return false;

		player.setEnchantScroll(item);
		player.sendPacket(new ChooseInventoryItemPacket(item.getItemId()));
		return true;
	}

	@Override
	public final int[] getItemIds()
	{
		return EnchantItemHolder.getInstance().getEnchantScrolls();
	}
}
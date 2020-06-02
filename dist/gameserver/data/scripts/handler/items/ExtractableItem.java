package handler.items;

import java.util.List;

import gnu.trove.set.hash.TIntHashSet;
import l2s.commons.util.Rnd;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author Bonux
 */
public class ExtractableItem extends SimpleItemHandler
{
	private int[] _itemIds;

	public ExtractableItem()
	{
		TIntHashSet set = new TIntHashSet();
		for(ItemTemplate template : ItemHolder.getInstance().getAllTemplates())
		{
			if(template == null)
				continue;
			if(template.isExtractable())
				set.add(template.getItemId());
		}
		_itemIds = set.toArray();
	}

	@Override
	protected boolean useItemImpl(Player player, ItemInstance item, boolean ctrl)
	{
		int itemId = item.getItemId();

		if(!canBeExtracted(player, item))
			return false;

		if(!tryUseItem(player, item, 1, false))
			return false;

		double chance_full = 0;
		int count = 0;
		boolean done_after_one_item = false;
		List<ItemTemplate.CapsuledItem> capsuled_items = item.getTemplate().getCapsuledItems();
		for(ItemTemplate.CapsuledItem ci2 : capsuled_items)
		{
			chance_full += ci2.getChance();
		}
		boolean must_give = chance_full == 100.0 ? true : false;
		for(ItemTemplate.CapsuledItem ci : capsuled_items)
		{
			if(Rnd.chance(ci.getChance()) && !done_after_one_item)
			{
				ItemFunctions.addItem(player, ci.getItemId(), Rnd.get(ci.getMinCount(), ci.getMaxCount()), true, "Extract item ID[" + item.getItemId() + "]");
				if(chance_full == 100.0)
					done_after_one_item = true;
			}
		}
		if(must_give && !done_after_one_item)
			loopGive(player, item, capsuled_items);
		return true;
	}

	private static void loopGive(Player player, ItemInstance item, List<ItemTemplate.CapsuledItem> capsuled_items)
	{
		boolean done_after_one_item = false;

		for(ItemTemplate.CapsuledItem ci : capsuled_items)
		{
			if(Rnd.chance(ci.getChance()) && !done_after_one_item)
			{
				ItemFunctions.addItem(player, ci.getItemId(), Rnd.get(ci.getMinCount(), ci.getMaxCount()), true, "Extract item ID[" + item.getItemId() + "]");
				done_after_one_item = true;
				break; //only one
			}
		}
		if(!done_after_one_item)
			loopGive(player, item, capsuled_items);
	}

	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}

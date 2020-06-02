package l2s.gameserver.utils;

import java.util.ArrayList;
import java.util.List;

import l2s.commons.dao.JdbcEntityState;
import l2s.commons.util.Rnd;
import l2s.gameserver.data.xml.holder.VariationDataHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.ShortCut;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.InventoryUpdatePacket;
import l2s.gameserver.network.l2.s2c.ShortCutRegisterPacket;
import l2s.gameserver.templates.item.support.variation.VariationCategory;
import l2s.gameserver.templates.item.support.variation.VariationFee;
import l2s.gameserver.templates.item.support.variation.VariationGroup;
import l2s.gameserver.templates.item.support.variation.VariationInfo;
import l2s.gameserver.templates.item.support.variation.VariationOption;
import l2s.gameserver.templates.item.support.variation.VariationStone;

/**
 * @author Bonux
**/
public final class VariationUtils
{
	public static long getRemovePrice(ItemInstance item)
	{
		if(item == null)
			return -1;

		VariationGroup group = VariationDataHolder.getInstance().getGroup(item.getTemplate().getVariationGroupId());
		if(group == null)
			return -1;

		int stoneId = item.getVariationStoneId();
		if(stoneId == -1)
			return 0;

		VariationFee fee = group.getFee(stoneId);
		if(fee == null)
			return -1;

		return fee.getCancelFee();
	}

	public static VariationFee getVariationFee(ItemInstance item, ItemInstance stone)
	{
		if(item == null)
			return null;

		if(stone == null)
			return null;

		VariationGroup group = VariationDataHolder.getInstance().getGroup(item.getTemplate().getVariationGroupId());
		if(group == null)
			return null;

		return group.getFee(stone.getItemId());
	}

	public static boolean tryAugmentItem(Player player, ItemInstance targetItem, ItemInstance refinerItem, ItemInstance feeItem, long feeItemCount)
	{
		if(!targetItem.canBeAugmented(player))
			return false;

		if(refinerItem.getTemplate().isBlocked(player, refinerItem))
			return false;

		int stoneId = refinerItem.getItemId();
		VariationStone stone = VariationDataHolder.getInstance().getStone(targetItem.getTemplate().getWeaponFightType(), stoneId);
		if(stone == null)
			return false;

		int variation1Id = getRandomOptionId(stone.getVariation(1));
		int variation2Id = getRandomOptionId(stone.getVariation(2));
		if(variation1Id == 0 && variation2Id == 0)
			return false;

		if(player.getInventory().getCountOf(refinerItem.getItemId()) < 1L)
			return false;

		if(player.getInventory().getCountOf(feeItem.getItemId()) < feeItemCount)
			return false;

		if(!player.getInventory().destroyItem(refinerItem, 1L))
			return false;

		if(!player.getInventory().destroyItem(feeItem, feeItemCount))
			return false;

		setVariation(player, targetItem, stoneId, variation1Id, variation2Id);
		return true;
	}

	public static void setVariation(Player player, ItemInstance item, int variationStoneId, int variation1Id, int variation2Id) {
		boolean equipped = false;
		if(equipped = item.isEquipped())
			player.getInventory().unEquipItem(item);

		item.setVariationStoneId(variationStoneId);
		item.setVariation1Id(variation1Id);
		item.setVariation2Id(variation2Id);

		item.setJdbcState(JdbcEntityState.UPDATED);
		item.update();

		if(equipped)
			player.getInventory().equipItem(item);

		player.sendPacket(new InventoryUpdatePacket().addModifiedItem(player, item));

		for(ShortCut sc : player.getAllShortCuts())
		{
			if(sc.getId() == item.getObjectId() && sc.getType() == ShortCut.TYPE_ITEM)
				player.sendPacket(new ShortCutRegisterPacket(player, sc));
		}

		player.sendChanges();
	}

	private static int getRandomOptionId(VariationInfo variation)
	{
		if(variation == null)
			return 0;

		double probalityAmount = 0.;
		VariationCategory[] categories = variation.getCategories();
		for(VariationCategory category : categories)
			probalityAmount += category.getProbability();

		if(Rnd.chance(probalityAmount))
		{
			double probalityMod = (100. - probalityAmount) / categories.length;
			List<VariationCategory> successCategories = new ArrayList<VariationCategory>();
			int tryCount = 0;
			while(successCategories.isEmpty())
			{
				tryCount++;
				for(VariationCategory category : categories)
				{
					if((tryCount % 10) == 0) //Немного теряем шанс, но зато зацикливания будут меньше.
						probalityMod += 1.;
					if(Rnd.chance(category.getProbability() + probalityMod))
						successCategories.add(category);
				}
			}

			VariationCategory[] categoriesArray = successCategories.toArray(new VariationCategory[successCategories.size()]);

			return getRandomOptionId(categoriesArray[Rnd.get(categoriesArray.length)]);
		}
		else
			return 0;
	}

	private static int getRandomOptionId(VariationCategory category)
	{
		if(category == null)
			return 0;

		double chanceAmount = 0.;
		VariationOption[] options = category.getOptions();
		for(VariationOption option : options)
			chanceAmount += option.getChance();

		if(Rnd.chance(chanceAmount))
		{
			double chanceMod = (100. - chanceAmount) / options.length;
			List<VariationOption> successOptions = new ArrayList<VariationOption>();
			int tryCount = 0;
			while(successOptions.isEmpty())
			{
				tryCount++;
				for(VariationOption option : options)
				{
					if((tryCount % 10) == 0) //Немного теряем шанс, но зато зацикливания будут меньше.
						chanceMod += 1.;
					if(Rnd.chance(option.getChance() + chanceMod))
						successOptions.add(option);
				}
			}

			VariationOption[] optionsArray = successOptions.toArray(new VariationOption[successOptions.size()]);

			return optionsArray[Rnd.get(optionsArray.length)].getId();
		}
		else
			return 0;
	}
}

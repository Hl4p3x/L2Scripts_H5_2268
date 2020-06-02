package l2s.gameserver.stats.funcs;

import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.stats.Env;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.tables.EnchantHPBonusTable;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.ItemType;
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType;

public class FuncEnchant extends Func
{
	public FuncEnchant(Stats stat, int order, Object owner, double value)
	{
		super(stat, order, owner);
	}

	@Override
	public void calc(Env env)
	{
		ItemInstance item = (ItemInstance) owner;

		int enchant = item.getEnchantLevel();
		int overenchant = Math.max(0, enchant - 3);

		switch(stat)
		{
			case SHIELD_DEFENCE:
			case MAGIC_DEFENCE:
			case POWER_DEFENCE:
			{
				env.value += enchant + overenchant * 2;
				return;
			}

			case MAX_HP:
			{
				env.value += EnchantHPBonusTable.getInstance().getHPBonus(item);
				return;
			}

			case MAGIC_ATTACK:
			{
				switch(item.getTemplate().getCrystalType().cry)
				{
					case ItemTemplate.CRYSTAL_S:
						env.value += 4 * (enchant + overenchant);
						break;
					case ItemTemplate.CRYSTAL_A:
						env.value += 3 * (enchant + overenchant);
						break;
					case ItemTemplate.CRYSTAL_B:
						env.value += 3 * (enchant + overenchant);
						break;
					case ItemTemplate.CRYSTAL_C:
						env.value += 3 * (enchant + overenchant);
						break;
					case ItemTemplate.CRYSTAL_D:
					case ItemTemplate.CRYSTAL_NONE:
						env.value += 2 * (enchant + overenchant);
						break;
				}
				return;
			}

			case POWER_ATTACK:
			{
				ItemType itemType = item.getItemType();
				boolean isBow = itemType == WeaponType.BOW || itemType == WeaponType.CROSSBOW;
				boolean isSword = (itemType == WeaponType.DUALFIST || itemType == WeaponType.DUAL || itemType == WeaponType.BIGSWORD || itemType == WeaponType.SWORD || itemType == WeaponType.RAPIER || itemType == WeaponType.ANCIENTSWORD) && item.getTemplate().getBodyPart() == ItemTemplate.SLOT_LR_HAND;
				switch(item.getTemplate().getCrystalType().cry)
				{
					case ItemTemplate.CRYSTAL_S:
						if(isBow)
							env.value += 10 * (enchant + overenchant);
						else if(isSword)
							env.value += 6 * (enchant + overenchant);
						else
							env.value += 5 * (enchant + overenchant);
						break;
					case ItemTemplate.CRYSTAL_A:
						if(isBow)
							env.value += 8 * (enchant + overenchant);
						else if(isSword)
							env.value += 5 * (enchant + overenchant);
						else
							env.value += 4 * (enchant + overenchant);
						break;
					case ItemTemplate.CRYSTAL_B:
					case ItemTemplate.CRYSTAL_C:
						if(isBow)
							env.value += 6 * (enchant + overenchant);
						else if(isSword)
							env.value += 4 * (enchant + overenchant);
						else
							env.value += 3 * (enchant + overenchant);
						break;
					case ItemTemplate.CRYSTAL_D:
					case ItemTemplate.CRYSTAL_NONE:
						if(isBow)
							env.value += 4 * (enchant + overenchant);
						else
							env.value += 2 * (enchant + overenchant);
						break;
				}
			}
		}
	}
}
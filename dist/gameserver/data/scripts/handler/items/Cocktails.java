package handler.items;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class Cocktails extends SimpleItemHandler
{
	private static final int[] ITEM_IDS = new int[]{10178, 10179, 14739, 15356, 15357, 20393, 21093, 20394, 21227, 21228, 21231, 21232, 21235, 21236};

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}

	// Sweet Fruit Cocktail
	private static final int[] sweet_list = { 2404, // Might
			2405, // Shield
			2406, // Wind Walk
			2407, // Focus
			2408, // Death Whisper
			2409, // Guidance
			2410, // Bless Shield
			2411, // Bless Body
			2412, // Haste
			2413, // Vampiric Rage
	};

	// Sweet Concentrated Fruit Cocktail 
	private static final int[] sweet_concentrated_list = {9021, // Berserker Spirit
		9018, // Blessed Body
		9017, // Blessed Shield
		9015, // Death Whisper
		9014, // Focus
		9016, // Guidance
		9019, // Haste
		9011, // Might
		9012, // Shield
		9020, // Vampiric Rage
		9013, // Wind Walk
	};

	// Fresh Fruit Cocktail
	private static final int[] fresh_list = { 2414, // Berserker Spirit
			2411, // Bless Body
			2415, // Magic Barrier
			2405, // Shield
			2406, // Wind Walk
			2416, // Bless Soul
			2417, // Empower
			2418, // Acumen
			2419, // Clarity
	};

	// Fresh Fruit Cocktail
	private static final int[] fresh_concentrated_list = {9025, // Acumen
		9021, // Berserker Spirit
		9018, // Blessed Body
		9023, // Blessed Soul
		9026, // Clarity
		9024, // Empower
		9022, // Magic Barrier
		9012, // Shield
		9013, // Wind Walk
	};

	//Event - Fresh Milk
	private static final int[] milk_list = { 2873, 2874, 2875, 2876, 2877, 2878, 2879, 2885, 2886, 2887, 2888, 2889, 2890, };

	@Override
	protected boolean useItemImpl(Player player, ItemInstance item, boolean ctrl)
	{
		int itemId = item.getItemId();

		if(player.isInOlympiadMode())
		{
			player.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(itemId));
			return false;
		}

		if(!tryUseItem(player, item, 1, true))
			return false;

		switch(itemId)
		{
			// Sweet Fruit Cocktail
			case 10178:
			case 20393:
				for(int skill : sweet_list)
				{
					player.broadcastPacket(new MagicSkillUse(player, player, skill, 1, 0, 0));
					player.altOnMagicUseTimer(player, SkillHolder.getInstance().getSkill(skill, 1));
				}
				break;
			// Sweet Concentrated Fruit Cocktail
			case 15356:
			case 21227:
			case 21228:
				for(int skill : sweet_concentrated_list)
				{
					player.broadcastPacket(new MagicSkillUse(player, player, skill, 1, 0, 0));
					player.altOnMagicUseTimer(player, SkillHolder.getInstance().getSkill(skill, 1));
				}
				break;
			// Fresh Fruit Cocktail				
			case 10179:
			case 20394:
			case 21094:
				for(int skill : fresh_list)
				{
					player.broadcastPacket(new MagicSkillUse(player, player, skill, 1, 0, 0));
					player.altOnMagicUseTimer(player, SkillHolder.getInstance().getSkill(skill, 1));
				}
				break;
			// Fresh Concentrated Fruit Cocktail
			case 15357:
			case 21231:
			case 21232:
				for(int skill : fresh_concentrated_list)
				{
					player.broadcastPacket(new MagicSkillUse(player, player, skill, 1, 0, 0));
					player.altOnMagicUseTimer(player, SkillHolder.getInstance().getSkill(skill, 1));
				}
				break;
			//Event - Fresh Milk				
			case 14739:
			case 21235:
			case 21236:
				player.broadcastPacket(new MagicSkillUse(player, player, 2873, 1, 0, 0));
				player.altOnMagicUseTimer(player, SkillHolder.getInstance().getSkill(2891, 6));
				for(int skill : milk_list)
				{
					player.broadcastPacket(new MagicSkillUse(player, player, skill, 1, 0, 0));
					player.altOnMagicUseTimer(player, SkillHolder.getInstance().getSkill(skill, 1));
				}
				break;
			default:
				return false;
		}

		return true;
	}
}
package l2s.gameserver.model.reward;

import l2s.gameserver.Config;
import l2s.gameserver.templates.item.ItemTemplate;

/**
 * @author Bonux
**/
public class RewardItemRates
{
	public static double getRateModifier(ItemTemplate item)
	{
		double modifier = 1.;

		if(item.isCommonItem())
			modifier *= Config.RATE_DROP_COMMON_ITEMS;

		if(item.isShadowItem())
			modifier *= Config.RATE_DROP_SHADOW_ITEMS;

		if(item.isSealedItem())
			modifier *= Config.RATE_DROP_SEALED_ITEMS;

		if(item.isLifeStone())
			modifier *= Config.RATE_DROP_LIFE_STONES;

		if(item.isEnchantScroll())
			modifier *= Config.RATE_DROP_ENCHANT_SCROLLS;

		if(item.isForgottenScroll())
			modifier *= Config.RATE_DROP_FORGOTTEN_SCROLLS;

		if(item.isKeyMatherial())
			modifier *= Config.RATE_DROP_KEY_MATHETIRALS;

		if(item.isRecipe())
			modifier *= Config.RATE_DROP_RECEPIES;

		if(item.isBelt())
			modifier *= Config.RATE_DROP_BELTS;

		if(item.isBracelet())
			modifier *= Config.RATE_DROP_BRACELETS;

		if(item.isUnderwear())
			modifier *= Config.RATE_DROP_UNDERWEARS;

		if(item.isCloak())
			modifier *= Config.RATE_DROP_CLOAKS;

		if(item.isTalisman())
			modifier *= Config.RATE_DROP_TALISMANS;
	
		if(item.isCodexBook())
			modifier *= Config.RATE_DROP_CODEX_BOOKS;
	
		if(item.isAttributeStone())
			modifier *= Config.RATE_DROP_ATTRIBUTE_STONES;
			
		if(item.isAttributeCrystal())
			modifier *= Config.RATE_DROP_ATTRIBUTE_CRYSTALS;
	
		if(item.isAttributeJewel())
			modifier *= Config.RATE_DROP_ATTRIBUTE_JEWELS;
	
		if(item.isAttributeEnergy())
			modifier *= Config.RATE_DROP_ATTRIBUTE_ENERGY;
	
		if(item.isWeapon())
			modifier *= Config.RATE_DROP_WEAPONS_ALL;
	
		if(item.isArmor())
			modifier *= Config.RATE_DROP_ARMOR_ALL;
	
		if(item.isAccessory())
			modifier *= Config.RATE_DROP_ACCESSORY_ALL;

		switch(item.getCrystalType())
		{
			case D:
				modifier *= Config.RATE_DROP_BY_GRADE_D;
				break;
			case C:
				modifier *= Config.RATE_DROP_BY_GRADE_C;
				break;
			case B:
				modifier *= Config.RATE_DROP_BY_GRADE_B;
				break;
			case A:
				modifier *= Config.RATE_DROP_BY_GRADE_A;
				break;
			case S:
				modifier *= Config.RATE_DROP_BY_GRADE_S;
				break;
			case S80:
				modifier *= Config.RATE_DROP_BY_GRADE_S80;
				break;
			case S84:
				modifier *= Config.RATE_DROP_BY_GRADE_S84;
				break;
		}

		switch(item.getBodyPart())
		{
			case ItemTemplate.SLOT_R_EAR:
			case ItemTemplate.SLOT_L_EAR:
				modifier *= Config.RATE_DROP_BY_SLOT_RL_EAR;
				break;
			case ItemTemplate.SLOT_NECK:
				modifier *= Config.RATE_DROP_BY_SLOT_NECK;
				break;
			case ItemTemplate.SLOT_R_FINGER:
			case ItemTemplate.SLOT_L_FINGER:
				modifier *= Config.RATE_DROP_BY_SLOT_RL_FINGER;
				break;
			case ItemTemplate.SLOT_HEAD:
				modifier *= Config.RATE_DROP_BY_SLOT_HEAD;
				break;
			case ItemTemplate.SLOT_R_HAND:
				modifier *= Config.RATE_DROP_BY_SLOT_R_HAND;
				break;
			case ItemTemplate.SLOT_L_HAND:
				modifier *= Config.RATE_DROP_BY_SLOT_L_HAND;
				break;
			case ItemTemplate.SLOT_GLOVES:
				modifier *= Config.RATE_DROP_BY_SLOT_GLOVES;
				break;		
			case ItemTemplate.SLOT_CHEST:
				modifier *= Config.RATE_DROP_BY_SLOT_CHEST;	
				break;
			case ItemTemplate.SLOT_LEGS:
				modifier *= Config.RATE_DROP_BY_SLOT_LEGS;
				break;
			case ItemTemplate.SLOT_FEET:
				modifier *= Config.RATE_DROP_BY_SLOT_FEET;
				break;
			case ItemTemplate.SLOT_BACK:
				modifier *= Config.RATE_DROP_BY_SLOT_BACK;
				break;
			case ItemTemplate.SLOT_LR_HAND:
				modifier *= Config.RATE_DROP_BY_SLOT_LR_HAND;
				break;
			case ItemTemplate.SLOT_FULL_ARMOR:
				modifier *= Config.RATE_DROP_BY_SLOT_FULL_ARMOR;
				break;
			case ItemTemplate.SLOT_HAIR:
				modifier *= Config.RATE_DROP_BY_SLOT_HAIR;
				break;
			case ItemTemplate.SLOT_HAIRALL:
				modifier *= Config.RATE_DROP_BY_SLOT_HAIRALL;
				break;
			case ItemTemplate.SLOT_BELT:
				modifier *= Config.RATE_DROP_BY_SLOT_BELT;
				break;
		}

		return modifier;
	}

	public static double getMinCountModifier(ItemTemplate item)
	{
		double modifier = 1.;

		if(item.isLifeStone())
			modifier *= Config.RATE_COUNT_MIN_MAX_LIFE_STONES;

		if(item.isEnchantScroll())
			modifier *= Config.RATE_COUNT_MIN_MAX_ENCHANT_SCROLLS;

		if(item.isKeyMatherial())
			modifier *= Config.RATE_COUNT_MIN_MAX_KEY_MATHERIAL;

		if(item.isRecipe())
			modifier *= Config.RATE_COUNT_MIN_MAX_REPECIES;

		if(item.isCodexBook())
			modifier *= Config.RATE_COUNT_MIN_MAX_CODEX_BOOKS;
	
		if(item.isAttributeStone())
			modifier *= Config.RATE_COUNT_MIN_MAX_ATTRIBUTE_STONES;

		return modifier;
	}

	public static double getMaxCountModifier(ItemTemplate item)
	{
		double modifier = 1.;

		if(item.isLifeStone())
			modifier *= Config.RATE_COUNT_MIN_MAX_LIFE_STONES;

		if(item.isEnchantScroll())
			modifier *= Config.RATE_COUNT_MIN_MAX_ENCHANT_SCROLLS;

		if(item.isKeyMatherial())
			modifier *= Config.RATE_COUNT_MIN_MAX_KEY_MATHERIAL;

		if(item.isRecipe())
			modifier *= Config.RATE_COUNT_MIN_MAX_REPECIES;

		if(item.isCodexBook())
			modifier *= Config.RATE_COUNT_MIN_MAX_CODEX_BOOKS;
	
		if(item.isAttributeStone())
			modifier *= Config.RATE_COUNT_MIN_MAX_ATTRIBUTE_STONES;

		return modifier;
	}
}

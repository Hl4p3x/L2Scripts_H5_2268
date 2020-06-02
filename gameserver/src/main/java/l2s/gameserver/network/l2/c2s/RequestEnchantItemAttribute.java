package l2s.gameserver.network.l2.c2s;

import l2s.commons.dao.JdbcEntityState;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Element;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.network.l2.s2c.ExAttributeEnchantResultPacket;
import l2s.gameserver.network.l2.s2c.InventoryUpdatePacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Log;

/**
 * @author SYS
 * Format: d
 */
public class RequestEnchantItemAttribute extends L2GameClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(_objectId == -1)
		{
			activeChar.setEnchantScroll(null);
			activeChar.sendPacket(Msg.ELEMENTAL_POWER_ENCHANCER_USAGE_HAS_BEEN_CANCELLED);
			return;
		}

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_ADD_ELEMENTAL_POWER_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP, ActionFailPacket.STATIC);
			return;
		}

		if(activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}

		PcInventory inventory = activeChar.getInventory();
		ItemInstance itemToEnchant = inventory.getItemByObjectId(_objectId);
		ItemInstance stone = activeChar.getEnchantScroll();
		activeChar.setEnchantScroll(null);

		if(itemToEnchant == null || stone == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		ItemTemplate item = itemToEnchant.getTemplate();

		if(!itemToEnchant.canBeEnchanted() || item.getCrystalType().cry < ItemTemplate.CRYSTAL_S)
		{
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS, ActionFailPacket.STATIC);
			return;
		}

		if(itemToEnchant.getLocation() != ItemInstance.ItemLocation.INVENTORY && itemToEnchant.getLocation() != ItemInstance.ItemLocation.PAPERDOLL)
		{
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS, ActionFailPacket.STATIC);
			return;
		}

		if(itemToEnchant.isStackable() || (stone = inventory.getItemByObjectId(stone.getObjectId())) == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		Element element = ItemFunctions.getEnchantAttributeStoneElement(stone.getItemId(), itemToEnchant.isArmor());

		if(itemToEnchant.isArmor())
		{
			if(itemToEnchant.getAttributeElementValue(Element.getReverseElement(element), false) != 0)
			{
				activeChar.sendPacket(Msg.ANOTHER_ELEMENTAL_POWER_HAS_ALREADY_BEEN_ADDED_THIS_ELEMENTAL_POWER_CANNOT_BE_ADDED, ActionFailPacket.STATIC);
				return;
			}
		}
		else if(itemToEnchant.isWeapon())
		{
			if(itemToEnchant.getAttributeElement() != Element.NONE && itemToEnchant.getAttributeElement() != element)
			{
				activeChar.sendPacket(Msg.ANOTHER_ELEMENTAL_POWER_HAS_ALREADY_BEEN_ADDED_THIS_ELEMENTAL_POWER_CANNOT_BE_ADDED, ActionFailPacket.STATIC);
				return;
			}
		}
		else
		{
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS, ActionFailPacket.STATIC);
			return;
		}

		if(item.isUnderwear() || item.isCloak() || item.isBracelet() || item.isBelt() || !item.isAttributable())
		{
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS, ActionFailPacket.STATIC);
			return;
		}

		// Запрет на заточку чужих вещей, баг может вылезти на серверных лагах
		if(itemToEnchant.getOwnerId() != activeChar.getObjectId())
		{
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS, ActionFailPacket.STATIC);
			return;
		}

		final int oldValue = itemToEnchant.getAttributeElementValue(element, true);

		int maxValue = itemToEnchant.isWeapon() ? Config.ATT_MOD_MAX_WEAPON : Config.ATT_MOD_MAX_ARMOR;
		if(stone.getTemplate().isAttributeCrystal())
			maxValue *= 2;

		int totalValue = 0;

		int successCount = 0;
		int failCount = 0;

		boolean auto = Config.ENABLE_AUTO_ATTRIBUTE_SYSTEM && stone == activeChar.getAutoAttributeItem();
		do
		{
			if(itemToEnchant.getAttributeElementValue(element, false) >= maxValue)
			{
				activeChar.sendPacket(Msg.ELEMENTAL_POWER_ENCHANCER_USAGE_HAS_BEEN_CANCELLED, ActionFailPacket.STATIC);
				break;
			}

			if(!inventory.destroyItem(stone, 1L))
			{
				activeChar.sendActionFailed();
				break;
			}

			if(Rnd.chance(stone.getTemplate().isAttributeCrystal() ? Config.ENCHANT_ATTRIBUTE_CRYSTAL_CHANCE : Config.ENCHANT_ATTRIBUTE_STONE_CHANCE))
			{
				if(itemToEnchant.getEnchantLevel() == 0)
				{
					SystemMessage sm = new SystemMessage(SystemMessage.S2_ELEMENTAL_POWER_HAS_BEEN_ADDED_SUCCESSFULLY_TO_S1);
					sm.addItemName(itemToEnchant.getItemId());
					sm.addItemName(stone.getItemId());
					activeChar.sendPacket(sm);
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessage.S3_ELEMENTAL_POWER_HAS_BEEN_ADDED_SUCCESSFULLY_TO__S1S2);
					sm.addNumber(itemToEnchant.getEnchantLevel());
					sm.addItemName(itemToEnchant.getItemId());
					sm.addItemName(stone.getItemId());
					activeChar.sendPacket(sm);
				}

				int value = itemToEnchant.isWeapon() ? Config.ATT_MOD_WEAPON : Config.ATT_MOD_ARMOR;

				// Для оружия 1й камень дает +20 атрибута
				if(itemToEnchant.getAttributeElementValue(element, false) == 0 && itemToEnchant.isWeapon())
					value = Config.ATT_MOD_WEAPON1;

				totalValue += value;

				itemToEnchant.setAttributeElement(element, Math.min(maxValue, itemToEnchant.getAttributeElementValue(element, false) + value));
				itemToEnchant.setJdbcState(JdbcEntityState.UPDATED);
				itemToEnchant.update();
				Log.LogEvent(activeChar.getName(), "Attributes", "SuccessAddAttr", "added: " + value + " to " + itemToEnchant.getItemId());

				successCount++;
			}
			else
			{
				activeChar.sendPacket(Msg.YOU_HAVE_FAILED_TO_ADD_ELEMENTAL_POWER);
				failCount++;
			}
		}
		while(auto);

		if(totalValue > 0)
		{
			if(itemToEnchant.isEquipped())
			{
				activeChar.getInventory().isRefresh = true;
				activeChar.getInventory().unEquipItem(itemToEnchant);
				activeChar.getInventory().equipItem(itemToEnchant);
				activeChar.getInventory().isRefresh = false;
			}

			activeChar.sendPacket(new InventoryUpdatePacket().addModifiedItem(activeChar, itemToEnchant));
			activeChar.sendPacket(new ExAttributeEnchantResultPacket(totalValue, itemToEnchant.isWeapon(), element, oldValue, itemToEnchant.getAttributeElementValue(element, true), successCount, failCount));
		}
		else
			activeChar.sendPacket(new ExAttributeEnchantResultPacket(totalValue, itemToEnchant.isWeapon(), element, oldValue, itemToEnchant.getAttributeElementValue(element, true), successCount, failCount));

		activeChar.setAutoAttributeItem(null);
		activeChar.setEnchantScroll(null);
		activeChar.updateStats();
	}
}
package l2s.gameserver.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.xml.holder.EnchantStoneHolder;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.instancemanager.CursedWeaponsManager;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Element;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.ItemInstance.ItemLocation;
import l2s.gameserver.model.items.attachment.PickableAttachment;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExUseSharedGroupItem;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.skills.TimeStamp;
import l2s.gameserver.tables.PetDataTable;
import l2s.gameserver.templates.item.ArmorTemplate.ArmorType;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType;
import l2s.gameserver.templates.item.support.EnchantStone;

public final class ItemFunctions
{
	private ItemFunctions()
	{
	}

	public static ItemInstance createItem(int itemId)
	{
		ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), itemId);
		item.setLocation(ItemLocation.VOID);
		item.setCount(1L);

		return item;
	}

	/**
	 * Добавляет предмет в инвентарь игрока, корректно обрабатывает нестыкуемые вещи
	 *
	 * @param playable Владелец инвентаря
	 * @param itemId   ID предмета
	 * @param count	количество
	 */
	public static List<ItemInstance> addItem(Playable playable, int itemId, long count, boolean notify, String logDesc)
	{
		if(playable == null || count < 1)
			return Collections.emptyList();

		Playable player;
		if(playable.isSummon())
			player = playable.getPlayer();
		else
			player = playable;

		if(itemId > 0)
		{
			List<ItemInstance> items = new ArrayList<ItemInstance>();

			ItemTemplate t = ItemHolder.getInstance().getTemplate(itemId);
			if(t.isStackable())
			{
				ItemInstance item = player.getInventory().addItem(itemId, count);
				items.add(item);

				if(notify)
					player.sendPacket(SystemMessagePacket.obtainItems(itemId, count, 0));

				if(!StringUtils.isEmpty(logDesc))
					Log.LogItem(playable, "ItemFunctionsAddItem", item, count, logDesc);
			}
			else
			{
				for(long i = 0; i < count; i++)
				{
					ItemInstance item = player.getInventory().addItem(itemId, 1);
					items.add(item);

					if(notify)
						player.sendPacket(SystemMessagePacket.obtainItems(item));

					if(!StringUtils.isEmpty(logDesc))
						Log.LogItem(playable, "ItemFunctionsAddItem", item, logDesc);
				}
			}

			return items;
		}
		return Collections.emptyList();
	}

	public static void addItem(Playable playable, int itemId, long count, String logDesc)
	{
		addItem(playable, itemId, count, true, logDesc);
	}

	/**
	 * Возвращает количество предметов в инвентаре игрока
	 *
	 * @param playable Владелец инвентаря
	 * @param itemId   ID предмета
	 * @return количество
	 */
	public static long getItemCount(Playable playable, int itemId)
	{
		if(playable == null)
			return 0;
		Playable player = playable.getPlayer();
		return player.getInventory().getCountOf(itemId);
	}

	/**
	 * @param playable Владелец инвентаря
	 * @param itemId   ID предмета
	 * @param count	количество
	 * @return true,  если у персонажа есть необходимое количество предметов
	 */
	public static boolean haveItem(Playable playable, int itemId, long count)
	{
		return getItemCount(playable, itemId) >= count;
	}

	/**
	 * Удаляет предметы из инвентаря игрока, корректно обрабатывает нестыкуемые предметы
	 *
	 * @param playable Владелец инвентаря
	 * @param itemId   ID предмета
	 * @param count	количество
	 * @return true, если вещь удалена
	 */
	public static boolean deleteItem(Playable playable, int itemId, long count)
	{
		return deleteItem(playable, itemId, count, true);
	}

	/**
	 * Удаляет предметы из инвентаря игрока, корректно обрабатывает нестыкуемые предметы
	 *
	 * @param playable Владелец инвентаря
	 * @param itemId   ID предмета
	 * @param count	количество
	 * @param notify оповестить игрока системным сообщением
	 * @return true, если вещь удалена
	 */
	public static boolean deleteItem(Playable playable, int itemId, long count, boolean notify)
	{
		if(playable == null || count < 1)
			return false;

		Player player = playable.getPlayer();

		if(itemId > 0)
		{
			player.getInventory().writeLock();
			try
			{
				ItemTemplate t = ItemHolder.getInstance().getTemplate(itemId);
				if (t == null)
					return false;
				if(t.isStackable())
				{
					if(!player.getInventory().destroyItemByItemId(itemId, count))
						//TODO audit
						return false;
				}
				else
				{
					if(player.getInventory().getCountOf(itemId) < count)
						return false;

					for(long i = 0; i < count; i++)
						if(!player.getInventory().destroyItemByItemId(itemId, 1L))
							//TODO audit
							return false;
				}
			}
			finally
			{
				player.getInventory().writeUnlock();
			}

			if(notify)
				player.sendPacket(SystemMessagePacket.removeItems(itemId, count));
		}

		return true;
	}

	/** Удаляет все предметы у персонажа с ивентаря и банка по Item ID **/
	public static void deleteItemsEverywhere(Playable playable, int itemId)
	{
		if(playable == null)
			return;

		Player player = playable.getPlayer();

		if(itemId > 0)
		{
			player.getInventory().writeLock();
			try
			{
				ItemInstance item = player.getInventory().getItemByItemId(itemId);
				while(item != null)
				{
					player.getInventory().destroyItem(item);
					item = player.getInventory().getItemByItemId(itemId);
				}
			}
			finally
			{
				player.getInventory().writeUnlock();
			}

			player.getWarehouse().writeLock();
			try
			{
				ItemInstance item = player.getWarehouse().getItemByItemId(itemId);
				while(item != null)
				{
					player.getWarehouse().destroyItem(item);
					item = player.getWarehouse().getItemByItemId(itemId);
				}
			}
			finally
			{
				player.getWarehouse().writeUnlock();
			}

			player.getFreight().writeLock();
			try
			{
				ItemInstance item = player.getFreight().getItemByItemId(itemId);
				while(item != null)
				{
					player.getFreight().destroyItem(item);
					item = player.getFreight().getItemByItemId(itemId);
				}
			}
			finally
			{
				player.getFreight().writeUnlock();
			}

			player.getRefund().writeLock();
			try
			{
				ItemInstance item = player.getRefund().getItemByItemId(itemId);
				while(item != null)
				{
					player.getRefund().destroyItem(item);
					item = player.getRefund().getItemByItemId(itemId);
				}
			}
			finally
			{
				player.getRefund().writeUnlock();
			}
		}
	}

	public final static boolean isClanApellaItem(int itemId)
	{
		return itemId >= 7860 && itemId <= 7879 || itemId >= 9830 && itemId <= 9839;
	}

	public final static SystemMessage checkIfCanEquip(PetInstance pet, ItemInstance item)
	{
		if(!item.isEquipable())
			return Msg.ITEM_NOT_AVAILABLE_FOR_PETS;

		int petId = pet.getNpcId();

		if(item.getTemplate().isPendant() //
				|| PetDataTable.isWolf(petId) && item.getTemplate().isForWolf() //
				|| PetDataTable.isHatchling(petId) && item.getTemplate().isForHatchling() //
				|| PetDataTable.isStrider(petId) && item.getTemplate().isForStrider() //
				|| PetDataTable.isGreatWolf(petId) && item.getTemplate().isForGWolf() //
				|| PetDataTable.isBabyPet(petId) && item.getTemplate().isForPetBaby() //
				|| PetDataTable.isImprovedBabyPet(petId) && item.getTemplate().isForPetBaby() //
		)
			return null;

		return Msg.ITEM_NOT_AVAILABLE_FOR_PETS;
	}

	/**
	 * Проверяет возможность носить эту вещь.
	 *
	 * @return null, если вещь носить можно, либо SystemMessage, который можно показать игроку
	 */
	public final static L2GameServerPacket checkIfCanEquip(Player player, ItemInstance item)
	{
		//FIXME [G1ta0] черезмерный хардкод, переделать на условия
		int itemId = item.getItemId();
		int targetSlot = item.getTemplate().getBodyPart();
		Clan clan = player.getClan();

		// Геройское оружие и Wings of Destiny Circlet
		if((item.isHeroWeapon() || item.getItemId() == 6842) && !player.isHero())
			return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

		// камаэли и хеви/робы/щиты/сигилы
		if(player.getRace() == Race.KAMAEL && (item.getItemType() == ArmorType.HEAVY || item.getItemType() == ArmorType.MAGIC || item.getItemType() == ArmorType.SIGIL || item.getItemType() == WeaponType.NONE))
			return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

		// не камаэли и рапиры/арбалеты/древние мечи
		if(player.getRace() != Race.KAMAEL && (item.getItemType() == WeaponType.CROSSBOW || item.getItemType() == WeaponType.RAPIER || item.getItemType() == WeaponType.ANCIENTSWORD))
			return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

		if(itemId >= 7850 && itemId <= 7859 && player.getLvlJoinedAcademy() == 0) // Clan Oath Armor
			return Msg.THIS_ITEM_CAN_ONLY_BE_WORN_BY_A_MEMBER_OF_THE_CLAN_ACADEMY;

		if(isClanApellaItem(itemId) && player.getPledgeClass() < Player.RANK_WISEMAN)
			return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

		if(item.getItemType() == WeaponType.DUALDAGGER && player.getSkillLevel(923) < 1)
			return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

		// Замковые короны, доступные для всех членов клана
		if(ArrayUtils.contains(ItemTemplate.ITEM_ID_CASTLE_CIRCLET, itemId) && (clan == null || itemId != ItemTemplate.ITEM_ID_CASTLE_CIRCLET[clan.getCastle()]))
			return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

		// Корона лидера клана, владеющего замком
		if(itemId == 6841 && (clan == null || !player.isClanLeader() || clan.getCastle() == 0))
			return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

		// Нельзя одевать оружие, если уже одето проклятое оружие. Проверка двумя способами, для надежности.
		if(targetSlot == ItemTemplate.SLOT_LR_HAND || targetSlot == ItemTemplate.SLOT_L_HAND || targetSlot == ItemTemplate.SLOT_R_HAND)
		{
			if(itemId != player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND) && CursedWeaponsManager.getInstance().isCursed(player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND)))
				return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
			if(player.isCursedWeaponEquipped() && itemId != player.getCursedWeaponEquippedId())
				return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
		}

		// Плащи
		if(item.getTemplate().isCloak())
		{
			// Can be worn by Knights or higher ranks who own castle
			if(item.getName().contains("Knight") && (player.getPledgeClass() < Player.RANK_KNIGHT || player.getCastle() == null))
				return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

			// Плащи для камаэлей
			if(item.getName().contains("Kamael") && player.getRace() != Race.KAMAEL)
				return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

			// Плащи можно носить только с S80 или S84 сетом
			if(!player.getOpenCloak())
				return Msg.THE_CLOAK_CANNOT_BE_EQUIPPED_BECAUSE_A_NECESSARY_ITEM_IS_NOT_EQUIPPED;
		}

		if(targetSlot == ItemTemplate.SLOT_DECO)
		{
			int count = player.getTalismanCount();
			if(count <= 0)
				return new SystemMessagePacket(SystemMsg.YOU_CANNOT_WEAR_S1_BECAUSE_YOU_ARE_NOT_WEARING_A_BRACELET).addItemName(itemId);

			ItemInstance deco;
			for(int slot = Inventory.PAPERDOLL_DECO1; slot <= Inventory.PAPERDOLL_DECO6; slot++)
			{
				deco = player.getInventory().getPaperdollItem(slot);
				if(deco != null)
				{
					if(deco == item)
						return null; // талисман уже одет и количество слотов больше нуля
					// Проверяем на количество слотов и одинаковые талисманы
					if(--count <= 0 || deco.getItemId() == itemId)
						return new SystemMessagePacket(SystemMsg.YOU_CANNOT_EQUIP_S1_BECAUSE_YOU_DO_NOT_HAVE_ANY_AVAILABLE_SLOTS).addItemName(itemId);
				}
			}
		}
		return null;
	}

	public static boolean checkIfCanPickup(Playable playable, ItemInstance item)
	{
		Player player = playable.getPlayer();
		return item.getDropTimeOwner() <= System.currentTimeMillis() || item.getDropPlayers().contains(player.getObjectId());
	}

	public static boolean canAddItem(Player player, ItemInstance item)
	{
		if(!player.getInventory().validateWeight(item))
		{
			player.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
			return false;
		}

		if(!player.getInventory().validateCapacity(item))
		{
			player.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
			return false;
		}

		if(!item.getTemplate().getHandler().pickupItem(player, item))
			return false;

		PickableAttachment attachment = item.getAttachment() instanceof PickableAttachment ? (PickableAttachment) item.getAttachment() : null;
		if(attachment != null && !attachment.canPickUp(player))
			return false;

		return true;
	}

	/**
	 * Проверяет возможность передачи вещи
	 *
	 * @param player
	 * @param item
	 * @return
	 */
	public final static boolean checkIfCanDiscard(Player player, ItemInstance item)
	{
		if(item.isHeroWeapon())
			return false;

		if(PetDataTable.isPetControlItem(item) && player.isMounted())
			return false;

		if(player.getPetControlItem() == item)
			return false;

		if(player.getEnchantScroll() == item)
			return false;

		if(item.isCursed())
			return false;

		if(item.getTemplate().isQuest())
			return false;

		return true;
	}

	/**
	 * Проверяет соответствие уровня заточки и вообще катализатор ли это или левый итем
	 *
	 * @param item
	 * @param catalyst
	 * @return true если катализатор соответствует
	 */
	public static final EnchantStone getEnchantStone(ItemInstance item, ItemInstance catalyst)
	{
		if(item == null || catalyst == null)
			return null;

		EnchantStone enchantStone = EnchantStoneHolder.getInstance().getEnchantStone(catalyst.getItemId());
		if(enchantStone == null)
			return null;

		int current = item.getEnchantLevel();
		if(current < (item.getTemplate().getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR ? enchantStone.getMinFullbodyEnchantLevel() : enchantStone.getMinEnchantLevel()))
			return null;

		if(current > enchantStone.getMaxEnchantLevel())
			return null;

		if(!enchantStone.containsGrade(item.getCrystalType()))
			return null;

		final int itemType = item.getTemplate().getType2();
		switch(enchantStone.getType())
		{
			case ARMOR:
				if(itemType == ItemTemplate.TYPE2_WEAPON || item.getTemplate().isHairAccessory())
					return null;
				break;
			case WEAPON:
				if(itemType == ItemTemplate.TYPE2_SHIELD_ARMOR || itemType == ItemTemplate.TYPE2_ACCESSORY || item.getTemplate().isHairAccessory())
					return null;
				break;
			case HAIR_ACCESSORY:
				if(!item.getTemplate().isHairAccessory())
					return null;
				break;
		}

		return enchantStone;
	}

	public static int getCrystallizeCrystalAdd(ItemInstance item)
	{
		int result = 0;
		int crystalsAdd = 0;
		if(item.isWeapon())
		{
			switch(item.getCrystalType())
			{
				case D:
					crystalsAdd = 90;
					break;
				case C:
					crystalsAdd = 45;
					break;
				case B:
					crystalsAdd = 67;
					break;
				case A:
					crystalsAdd = 145;
					break;
				case S:
				case S80:
				case S84:
					crystalsAdd = 250;
					break;
			}
		}
		else
		{
			switch(item.getCrystalType())
			{
				case D:
					crystalsAdd = 11;
					break;
				case C:
					crystalsAdd = 6;
					break;
				case B:
					crystalsAdd = 11;
					break;
				case A:
					crystalsAdd = 20;
					break;
				case S:
				case S80:
				case S84:
					crystalsAdd = 25;
					break;
			}
		}

		if(item.getEnchantLevel() > 3)
		{
			result = crystalsAdd * 3;
			if(item.isWeapon())
				crystalsAdd *= 2;
			else
				crystalsAdd *= 3;
			result += crystalsAdd * (item.getEnchantLevel() - 3);
		}
		else
			result = crystalsAdd * item.getEnchantLevel();

		return result;
	}

	/**
	 * Возвращает тип элемента для камня атрибуции
	 *
	 * @return значение элемента
	 */
	public static Element getEnchantAttributeStoneElement(int itemId, boolean isArmor)
	{
		Element element = Element.NONE;
		switch(itemId)
		{
			case 9546:
			case 9552:
			case 10521:
				element = Element.FIRE;
				break;
			case 9547:
			case 9553:
			case 10522:
				element = Element.WATER;
				break;
			case 9548:
			case 9554:
			case 10523:
				element = Element.EARTH;
				break;
			case 9549:
			case 9555:
			case 10524:
				element = Element.WIND;
				break;
			case 9550:
			case 9556:
			case 10525:
				element = Element.UNHOLY;
				break;
			case 9551:
			case 9557:
			case 10526:
				element = Element.HOLY;
				break;
		}

		if(isArmor)
			return Element.getReverseElement(element);

		return element;
	}

	public static boolean checkUseItem(Player player, ItemInstance item, boolean sendMsg)
	{
		if(player.isInStoreMode())
		{
			if(sendMsg)
			{
				if(PetDataTable.isPetControlItem(item))
					player.sendPacket(SystemMsg.YOU_CANNOT_SUMMON_DURING_A_TRADE_OR_WHILE_USING_A_PRIVATE_STORE);
				else
					player.sendPacket(SystemMsg.YOU_MAY_NOT_USE_ITEMS_IN_A_PRIVATE_STORE_OR_PRIVATE_WORK_SHOP);
			}
			return false;
		}

		int itemId = item.getItemId();
		if(player.isFishing() && (itemId < 6535 || itemId > 6540))
		{
			if(sendMsg)
				player.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2);
			return false;
		}

		if(player.isSharedGroupDisabled(item.getTemplate().getReuseGroup()))
		{
			if(sendMsg)
				player.sendReuseMessage(item);
			return false;
		}

		if(!item.getTemplate().testCondition(player, item, sendMsg))
			return false;

		if(player.getInventory().isLockedItem(item))
			return false;

		if(item.getTemplate().isForPet())
		{
			if(sendMsg)
				player.sendPacket(SystemMsg.YOU_MAY_NOT_EQUIP_A_PET_ITEM);
			return false;
		}

		// Маги не могут вызывать Baby Buffalo Improved
		if(Config.ALT_IMPROVED_PETS_LIMITED_USE && player.isMageClass() && item.getItemId() == 10311)
		{
			if(sendMsg)
				player.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(itemId));
			return false;
		}

		// Войны не могут вызывать Improved Baby Kookaburra
		if(Config.ALT_IMPROVED_PETS_LIMITED_USE && !player.isMageClass() && item.getItemId() == 10313)
		{
			if(sendMsg)
				player.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(itemId));
			return false;
		}

		if(player.isUseItemDisabled())
		{
			if(sendMsg)
				player.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(itemId));
			return false;
		}
		
		if(player.isOutOfControl())
		{
			if(sendMsg)
				player.sendActionFailed();
			return false;
		}
		return true;
	}

	public static boolean useItem(Player player, ItemInstance item, boolean ctrl, boolean sendMsg)
	{
		if(!checkUseItem(player, item, sendMsg))
			return false;

		boolean success = item.getTemplate().getHandler().useItem(player, item, ctrl);
		if(success)
		{
			long nextTimeUse = item.getTemplate().getReuseType().next(item);
			if(nextTimeUse > System.currentTimeMillis())
			{
				TimeStamp timeStamp = new TimeStamp(item.getItemId(), nextTimeUse, item.getTemplate().getReuseDelay());
				player.addSharedGroupReuse(item.getTemplate().getReuseGroup(), timeStamp);

				if(item.getTemplate().getReuseDelay() > 0)
					player.sendPacket(new ExUseSharedGroupItem(item.getTemplate().getDisplayReuseGroup(), timeStamp));
			}
			return true;
		}
		return false;
	}
}

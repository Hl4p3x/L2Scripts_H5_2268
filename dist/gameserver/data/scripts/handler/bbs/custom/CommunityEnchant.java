package handler.bbs.custom;

import java.util.StringTokenizer;

import l2s.commons.map.hash.TIntStringHashMap;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.InventoryUpdatePacket;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.ShowBoardPacket;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.ItemTemplate.Grade;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Util;

/**
 * @author Bonux
**/
public class CommunityEnchant extends CustomCommunityHandler
{
	@Override
	public String[] getBypassCommands()
	{
		return new String[]
		{
			"_cbbsenchant"
		};
	}

	@Override
	protected void doBypassCommand(Player player, String bypass)
	{
		StringTokenizer st = new StringTokenizer(bypass, "_");
		String cmd = st.nextToken();
		String html = "";

		if("cbbsenchant".equals(cmd))
		{
			if(BBSConfig.ENCHANT_SERVICE_WEAPON_ENCHANT_COST.isEmpty() || BBSConfig.ENCHANT_SERVICE_ARMOR_ENCHANT_COST.isEmpty() || BBSConfig.ENCHANT_SERVICE_JEWELRY_ENCHANT_COST.isEmpty())
			{
				player.sendMessage(player.isLangRus() ? "Данная функция еще не реализована." : "This feature is not yet implemented.");
				player.sendPacket(ShowBoardPacket.CLOSE);
				return;
			}

			TIntStringHashMap tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/enchant.htm", player);
			html = tpls.get(0);

			StringBuilder content = new StringBuilder();

			Inventory inventory = player.getInventory();
	
			String hairButton = tpls.get(2);
			String headButton = tpls.get(2);
			String faceButton = tpls.get(2);
			String shirtButton = tpls.get(2);
			String necklaceButton = tpls.get(2);
			String weaponButton = tpls.get(2);
			String chestButton = tpls.get(2);
			String shieldButton = tpls.get(2);
			String earringLButton = tpls.get(2);
			String earringRButton = tpls.get(2);
			String glovesButton = tpls.get(2);
			String legsButton = tpls.get(2);
			String bootsButton = tpls.get(2);
			String ringLButton = tpls.get(2);
			String ringRButton = tpls.get(2);

			ItemInstance hairItem = inventory.getPaperdollItem(Inventory.PAPERDOLL_HAIR);
			if(hairItem != null)
			{
				hairButton = tpls.get(3);
				hairButton = hairButton.replace("<?item_icon?>", hairItem.getTemplate().getIcon());
				hairButton = hairButton.replace("<?item_object_id?>", String.valueOf(hairItem.getObjectId()));
			}

			if(hairItem != null && hairItem.getBodyPart() == ItemTemplate.SLOT_HAIRALL)
			{
				faceButton = tpls.get(4);
				faceButton = faceButton.replace("<?item_icon?>", hairItem.getTemplate().getIcon());
			}
			else
			{
				ItemInstance faceItem = inventory.getPaperdollItem(Inventory.PAPERDOLL_DHAIR);
				if(faceItem != null)
				{
					faceButton = tpls.get(3);
					faceButton = faceButton.replace("<?item_icon?>", faceItem.getTemplate().getIcon());
					faceButton = faceButton.replace("<?item_object_id?>", String.valueOf(faceItem.getObjectId()));
				}
			}

			ItemInstance weaponItem = inventory.getPaperdollItem(Inventory.PAPERDOLL_RHAND);
			if(weaponItem != null)
			{
				weaponButton = tpls.get(3);
				weaponButton = weaponButton.replace("<?item_icon?>", weaponItem.getTemplate().getIcon());
				weaponButton = weaponButton.replace("<?item_object_id?>", String.valueOf(weaponItem.getObjectId()));
			}

			if(weaponItem != null && weaponItem.getBodyPart() == ItemTemplate.SLOT_LR_HAND)
			{
				shieldButton = tpls.get(4);
				shieldButton = shieldButton.replace("<?item_icon?>", weaponItem.getTemplate().getIcon());
			}

			ItemInstance shieldItem = inventory.getPaperdollItem(Inventory.PAPERDOLL_LHAND);
			if(shieldItem != null)
			{
				shieldButton = tpls.get(3);
				shieldButton = shieldButton.replace("<?item_icon?>", shieldItem.getTemplate().getIcon());
				shieldButton = shieldButton.replace("<?item_object_id?>", String.valueOf(shieldItem.getObjectId()));
			}

			ItemInstance chestItem = inventory.getPaperdollItem(Inventory.PAPERDOLL_CHEST);
			if(chestItem != null)
			{
				chestButton = tpls.get(3);
				chestButton = chestButton.replace("<?item_icon?>", chestItem.getTemplate().getIcon());
				chestButton = chestButton.replace("<?item_object_id?>", String.valueOf(chestItem.getObjectId()));
			}

			if(chestItem != null && chestItem.getBodyPart() == ItemTemplate.SLOT_FORMAL_WEAR)
			{
				legsButton = tpls.get(4);
				legsButton = legsButton.replace("<?item_icon?>", chestItem.getTemplate().getIcon());

				headButton = tpls.get(4);
				headButton = headButton.replace("<?item_icon?>", chestItem.getTemplate().getIcon());

				glovesButton = tpls.get(4);
				glovesButton = glovesButton.replace("<?item_icon?>", chestItem.getTemplate().getIcon());

				bootsButton = tpls.get(4);
				bootsButton = bootsButton.replace("<?item_icon?>", chestItem.getTemplate().getIcon());
			}
			else
			{
				if(chestItem != null && chestItem.getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR)
				{
					legsButton = tpls.get(4);
					legsButton = legsButton.replace("<?item_icon?>", chestItem.getTemplate().getIcon());
				}
				else
				{
					ItemInstance legsItem = inventory.getPaperdollItem(Inventory.PAPERDOLL_LEGS);
					if(legsItem != null)
					{
						legsButton = tpls.get(3);
						legsButton = legsButton.replace("<?item_icon?>", legsItem.getTemplate().getIcon());
						legsButton = legsButton.replace("<?item_object_id?>", String.valueOf(legsItem.getObjectId()));
					}
				}

				ItemInstance item = inventory.getPaperdollItem(Inventory.PAPERDOLL_HEAD);
				if(item != null)
				{
					headButton = tpls.get(3);
					headButton = headButton.replace("<?item_icon?>", item.getTemplate().getIcon());
					headButton = headButton.replace("<?item_object_id?>", String.valueOf(item.getObjectId()));
				}
	
				item = inventory.getPaperdollItem(Inventory.PAPERDOLL_GLOVES);
				if(item != null)
				{
					glovesButton = tpls.get(3);
					glovesButton = glovesButton.replace("<?item_icon?>", item.getTemplate().getIcon());
					glovesButton = glovesButton.replace("<?item_object_id?>", String.valueOf(item.getObjectId()));
				}
	
				item = inventory.getPaperdollItem(Inventory.PAPERDOLL_FEET);
				if(item != null)
				{
					bootsButton = tpls.get(3);
					bootsButton = bootsButton.replace("<?item_icon?>", item.getTemplate().getIcon());
					bootsButton = bootsButton.replace("<?item_object_id?>", String.valueOf(item.getObjectId()));
				}
			}

			ItemInstance item = inventory.getPaperdollItem(Inventory.PAPERDOLL_UNDER);
			if(item != null)
			{
				shirtButton = tpls.get(3);
				shirtButton = shirtButton.replace("<?item_icon?>", item.getTemplate().getIcon());
				shirtButton = shirtButton.replace("<?item_object_id?>", String.valueOf(item.getObjectId()));
			}

			item = inventory.getPaperdollItem(Inventory.PAPERDOLL_NECK);
			if(item != null)
			{
				necklaceButton = tpls.get(3);
				necklaceButton = necklaceButton.replace("<?item_icon?>", item.getTemplate().getIcon());
				necklaceButton = necklaceButton.replace("<?item_object_id?>", String.valueOf(item.getObjectId()));
			}

			item = inventory.getPaperdollItem(Inventory.PAPERDOLL_LEAR);
			if(item != null)
			{
				earringLButton = tpls.get(3);
				earringLButton = earringLButton.replace("<?item_icon?>", item.getTemplate().getIcon());
				earringLButton = earringLButton.replace("<?item_object_id?>", String.valueOf(item.getObjectId()));
			}

			item = inventory.getPaperdollItem(Inventory.PAPERDOLL_REAR);
			if(item != null)
			{
				earringRButton = tpls.get(3);
				earringRButton = earringRButton.replace("<?item_icon?>", item.getTemplate().getIcon());
				earringRButton = earringRButton.replace("<?item_object_id?>", String.valueOf(item.getObjectId()));
			}

			item = inventory.getPaperdollItem(Inventory.PAPERDOLL_LFINGER);
			if(item != null)
			{
				ringLButton = tpls.get(3);
				ringLButton = ringLButton.replace("<?item_icon?>", item.getTemplate().getIcon());
				ringLButton = ringLButton.replace("<?item_object_id?>", String.valueOf(item.getObjectId()));
			}

			item = inventory.getPaperdollItem(Inventory.PAPERDOLL_RFINGER);
			if(item != null)
			{
				ringRButton = tpls.get(3);
				ringRButton = ringRButton.replace("<?item_icon?>", item.getTemplate().getIcon());
				ringRButton = ringRButton.replace("<?item_object_id?>", String.valueOf(item.getObjectId()));
			}

			String inventoryBlock = tpls.get(1);
			inventoryBlock = inventoryBlock.replace("<?hair_button?>", hairButton);
			inventoryBlock = inventoryBlock.replace("<?head_button?>", headButton);
			inventoryBlock = inventoryBlock.replace("<?face_button?>", faceButton);
			inventoryBlock = inventoryBlock.replace("<?shirt_button?>", shirtButton);
			inventoryBlock = inventoryBlock.replace("<?necklace_button?>", necklaceButton);
			inventoryBlock = inventoryBlock.replace("<?weapon_button?>", weaponButton);
			inventoryBlock = inventoryBlock.replace("<?chest_button?>", chestButton);
			inventoryBlock = inventoryBlock.replace("<?shield_button?>", shieldButton);
			inventoryBlock = inventoryBlock.replace("<?earring_l_button?>", earringLButton);
			inventoryBlock = inventoryBlock.replace("<?earring_r_button?>", earringRButton);
			inventoryBlock = inventoryBlock.replace("<?gloves_button?>", glovesButton);
			inventoryBlock = inventoryBlock.replace("<?legs_button?>", legsButton);
			inventoryBlock = inventoryBlock.replace("<?boots_button?>", bootsButton);
			inventoryBlock = inventoryBlock.replace("<?ring_l_button?>", ringLButton);
			inventoryBlock = inventoryBlock.replace("<?ring_r_button?>", ringRButton);

			content.append(inventoryBlock);

			if(st.hasMoreTokens())
			{
				int itemObjectId = 0;
				try
				{
					itemObjectId = Integer.parseInt(st.nextToken());
				}
				catch(Exception e)
				{
					//
				}

				if(itemObjectId > 0)
				{
					item = inventory.getItemByObjectId(itemObjectId);
					if(item != null && /*item.isEquipped() && */item.getOwnerId() == player.getObjectId())
					{
						final Grade grade = item.getCrystalType();
						final Grade externalGrade = Grade.VALUES[grade.externalOrdinal];

						int[][] enchantCost = null;
						if(item.canBeEnchanted())
						{
							if(item.isWeapon())
								enchantCost = BBSConfig.ENCHANT_SERVICE_WEAPON_ENCHANT_COST.get(externalGrade.ordinal());
							else if(item.isArmor())
								enchantCost = BBSConfig.ENCHANT_SERVICE_ARMOR_ENCHANT_COST.get(externalGrade.ordinal());
							else if(item.isAccessory())
								enchantCost = BBSConfig.ENCHANT_SERVICE_JEWELRY_ENCHANT_COST.get(externalGrade.ordinal());
						}

						if(enchantCost != null)
						{
							final int maxEnchantLevel = enchantCost.length - 1;

							int enchantLevel = item.getEnchantLevel();

							if(enchantLevel >= maxEnchantLevel)
							{
								player.sendMessage(player.isLangRus() ? "Выбранный предмет уже улучшен до максимального уровня." : "Selected item has already been enchanted to the maximum level.");
							}
							else
							{
								int feeItemId = enchantCost[enchantLevel + 1][0];
								if(feeItemId == 0)
								{
									player.sendMessage(player.isLangRus() ? "Нельзя улчушить предмет на данном уровне улучшения." : "It is impossible to enchant the item at this level of enchantment.");
								}
								else
								{
									long feeItemCount = enchantCost[enchantLevel + 1][1];

									if(st.hasMoreTokens())
									{
										String cmd1 = st.nextToken();
										if(cmd1.equals("enchant"))
										{
											if(feeItemId <= 0 || feeItemCount <= 0 || ItemFunctions.deleteItem(player, feeItemId, feeItemCount))
											{
												// set enchant value
												inventory.unEquipItem(item);
												item.setEnchantLevel(++enchantLevel);
												inventory.equipItem(item);

												// send packets
												player.sendPacket(new InventoryUpdatePacket().addModifiedItem(player, item));
												player.broadcastCharInfo();
												player.sendPacket(new MagicSkillUse(player, player, 5965, 1, 500, 1500));
												player.sendMessage(player.isLangRus() ? "Вы успешно улучшили Вашу экипировку до +" + enchantLevel + "!" : "You have successfully enchanted your equipment to +" + enchantLevel + "!");
											}
											else
												player.sendMessage(player.isLangRus() ? "Недостаточно необходимых предметов для улучшения экипировки." : "Not enough items needed to enchant equipment.");
										}
									}

									if(enchantLevel < maxEnchantLevel)
									{
										feeItemId = enchantCost[enchantLevel + 1][0];
										feeItemCount = enchantCost[enchantLevel + 1][1];

										String enchantBlock = tpls.get(5);
										enchantBlock = enchantBlock.replace("<?item_icon?>", item.getTemplate().getIcon());

										if(feeItemId > 0 && feeItemCount > 0)
										{
											String feeInfoBlock = tpls.get(9);
											feeInfoBlock = feeInfoBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));
											feeInfoBlock = feeInfoBlock.replace("<?fee_count?>", Util.formatAdena(feeItemCount));
											enchantBlock = enchantBlock.replace("<?fee_info?>", feeInfoBlock);
										}
										else
											enchantBlock = enchantBlock.replace("<?fee_info?>", tpls.get(10));

										enchantBlock = enchantBlock.replace("<?next_enchant_level?>", String.valueOf(enchantLevel + 1));

										if(enchantLevel > 0)
										{
											String itemNameBlock = tpls.get(6);
											itemNameBlock = itemNameBlock.replace("<?current_enchant_level?>", String.valueOf(enchantLevel));
											itemNameBlock = itemNameBlock.replace("<?item_name?>", HtmlUtils.htmlItemName(item.getItemId()));
											enchantBlock = enchantBlock.replace("<?item_name?>", itemNameBlock);
										}
										else
											enchantBlock = enchantBlock.replace("<?item_name?>", HtmlUtils.htmlItemName(item.getItemId()));

										String gradeIcon = "";
										if(grade != Grade.NONE)
										{
											if(externalGrade != grade)
												gradeIcon = tpls.get(8).replace("<?item_grade?>", grade.toString());
											else
												gradeIcon = tpls.get(7).replace("<?item_grade?>", grade.toString());
										}

										enchantBlock = enchantBlock.replace("<?item_grade_icon?>", gradeIcon);
										enchantBlock = enchantBlock.replace("<?item_object_id?>", String.valueOf(item.getObjectId()));

										content.append(enchantBlock);
									}
								}
							}
						}
						else
							player.sendMessage(player.isLangRus() ? "Выбранный предмет нельзя заточить." : "Selected item cannot be enchanted.");
					}
				}
			}
			html = html.replace("<?content?>", content.toString());
		}
		ShowBoardPacket.separateAndSend(html, player);
	}

	@Override
	protected void doWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		//
	}
}
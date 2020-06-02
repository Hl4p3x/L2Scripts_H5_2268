package services;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.data.xml.holder.MultiSellHolder.MultiSellListContainer;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.MultiSellEntry;
import l2s.gameserver.model.base.MultiSellIngredient;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.templates.item.ItemTemplate;

public class Pushkin extends Functions
{
	public String DialogAppend_30300(Integer val)
	{
		if(val != 0 || !Config.ALT_SIMPLE_SIGNS && !Config.ALT_BS_CRYSTALLIZE)
			return "";

		StringBuilder append = new StringBuilder();

		if((getSelf()).isLangRus())
		{
			if(Config.ALT_SIMPLE_SIGNS)
			{
				append.append("<br><br><center><font color=LEVEL>Функции Семи Печатей:</font></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 3112605\">Сделать S-грейд меч</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 3112606\">Вставить SA в оружие S-грейда</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 3112607\">Распечатать броню S-грейда</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 3112608\">Распечатать бижутерию S-грейда</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 3112609\">Сделать A-грейд меч</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 3112610\">Вставить SA в оружие A-грейда</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 3112611\">Распечатать броню A-грейда</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 3112612\">Распечатать бижутерию A-грейда</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 3112613\">Запечатать броню A-грейда</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 3112601\">Удалить SA из оружия</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 3112602\">Обменять оружие с доплатой</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 3112603\">Обменять оружие на равноценное</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 3112604\">Завершить редкую вещь</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 3111301\">Купить что-нибудь</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 400\">Обменять камни</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 500\">Приобрести расходные материалы</a></center><br1>");
			}
			if(Config.ALT_BS_CRYSTALLIZE)// TODO: сделать у всех кузнецов
				append.append("<center><br><a action=\"bypass -h scripts_services.Pushkin:doCrystallize\">Кристаллизация</a></center>");
		}
		else
		{
			if(Config.ALT_SIMPLE_SIGNS)
			{
				append.append("<br><br><center><font color=LEVEL>Seven Signs options:</font></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 3112605\">Manufacture an S-grade sword</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 3112606\">Bestow the special S-grade weapon some abilities</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 3112607\">Release the S-grade armor seal</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 3112608\">Release the S-grade accessory seal</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 3112609\">Manufacture an A-grade sword</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 3112610\">Bestow the special A-grade weapon some abilities</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 3112611\">Release the A-grade armor seal</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 3112612\">Release the A-grade accessory seal</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 3112613\">Seal the A-grade armor again</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 3112601\">Remove the special abilities from a weapon</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 3112602\">Upgrade weapon</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 3112603\">Make an even exchange of weapons</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 3112604\">Complete a Foundation Item</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 3111301\">Buy Something</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 400\">Exchange Seal Stones</a></center><br1>");
				append.append("<center><a action=\"bypass -h npc_%objectId%_Multisell 500\">Purchase consumable items</a></center><br1>");
			}
			if(Config.ALT_BS_CRYSTALLIZE)
				append.append("<center><br><a action=\"bypass -h scripts_services.Pushkin:doCrystallize\">Crystallize</a></center>");
		}

		append.append("<br>");

		return append.toString();
	}

	public String DialogAppend_30086(Integer val)
	{
		return DialogAppend_30300(val);
	}

	public String DialogAppend_30098(Integer val)
	{
		if(val != 0 || !Config.ALT_ALLOW_TATTOO)
			return "";
		return (getSelf()).isLangRus() ? "<br><a action=\"bypass -h npc_%objectId%_Multisell 6500\">Купить тату</a>" : "<br><a action=\"bypass -h npc_%objectId%_Multisell 6500\">Buy tattoo</a>";
	}

	public void doCrystallize()
	{
		Player player = getSelf();
		NpcInstance merchant = player.getLastNpc();
		Castle castle = merchant != null ? merchant.getCastle(player) : null;

		MultiSellListContainer list = new MultiSellListContainer(0);
		list.setShowAll(false);
		list.setKeepEnchant(true);
		list.setNoTax(false);
		int entry = 0;
		final Inventory inv = player.getInventory();
		for(final ItemInstance itm : inv.getItems())
			if(itm.canBeCrystallized(player))
			{
				final ItemTemplate crystal = ItemHolder.getInstance().getTemplate(itm.getTemplate().getCrystalType().cry);
				MultiSellEntry possibleEntry = new MultiSellEntry(++entry, crystal.getItemId(), itm.getTemplate().getCrystalCount(), 0);
				possibleEntry.addIngredient(new MultiSellIngredient(itm.getItemId(), 1, itm.getEnchantLevel()));
				possibleEntry.addIngredient(new MultiSellIngredient(ItemTemplate.ITEM_ID_ADENA, Math.round(itm.getTemplate().getCrystalCount() * crystal.getReferencePrice() * 0.05), 0));
				list.addEntry(possibleEntry);
			}

		MultiSellHolder.getInstance().SeparateAndSend(list, player, castle == null ? 0. : castle.getTaxRate());
	}
}
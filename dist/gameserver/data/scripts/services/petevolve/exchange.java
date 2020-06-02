package services.petevolve;

import l2s.commons.dao.JdbcEntityState;
import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.InventoryUpdatePacket;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.tables.PetDataTable;
import l2s.gameserver.tables.PetDataTable.L2Pet;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Util;

public class exchange extends Functions
{
	/** Билеты для обмена **/
	private static final int PEticketB = 7583;
	private static final int PEticketC = 7584;
	private static final int PEticketK = 7585;

	/** Дудки для вызова петов **/
	private static final int BbuffaloP = 6648;
	private static final int BcougarC = 6649;
	private static final int BkookaburraO = 6650;

	public void exch_1()
	{
		Player player = getSelf();
		if(player == null)
			return;
		if(ItemFunctions.getItemCount(player, PEticketB) >= 1)
		{
			ItemFunctions.deleteItem(player, PEticketB, 1);
			ItemFunctions.addItem(player, BbuffaloP, 1, "Clan Hall pet exchange");
			return;
		}
		show("scripts/services/petevolve/exchange_no.htm", player);
	}

	public void exch_2()
	{
		Player player = getSelf();
		if(player == null)
			return;
		if(ItemFunctions.getItemCount(player, PEticketC) >= 1)
		{
			ItemFunctions.deleteItem(player, PEticketC, 1);
			ItemFunctions.addItem(player, BcougarC, 1, "Clan Hall pet exchange");
			return;
		}
		show("scripts/services/petevolve/exchange_no.htm", player);
	}

	public void exch_3()
	{
		Player player = getSelf();
		if(player == null)
			return;

		if(ItemFunctions.getItemCount(player, PEticketK) >= 1)
		{
			ItemFunctions.deleteItem(player, PEticketK, 1);
			ItemFunctions.addItem(player, BkookaburraO, 1, "Clan Hall pet exchange");
			return;
		}
		show("scripts/services/petevolve/exchange_no.htm", player);
	}

	public void showBabyPetExchange()
	{
		Player player = getSelf();
		if(player == null)
			return;
		if(!Config.SERVICES_EXCHANGE_BABY_PET_ENABLED)
		{
			show("Сервис отключен.", player);
			return;
		}
		ItemTemplate item = ItemHolder.getInstance().getTemplate(Config.SERVICES_EXCHANGE_BABY_PET_ITEM);
		String out = "";
		out += "<html><body>Вы можете в любое время обменять вашего Improved Baby пета на другой вид, без потери опыта. Пет при этом должен быть вызван.";
		out += "<br>Стоимость обмена: " + Util.formatAdena(Config.SERVICES_EXCHANGE_BABY_PET_PRICE) + " " + item.getName();
		out += "<br><button width=250 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.petevolve.exchange:exToCougar\" value=\"Обменять на Improved Cougar\">";
		out += "<br1><button width=250 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.petevolve.exchange:exToBuffalo\" value=\"Обменять на Improved Buffalo\">";
		out += "<br1><button width=250 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.petevolve.exchange:exToKookaburra\" value=\"Обменять на Improved Kookaburra\">";
		out += "</body></html>";
		show(out, player);
	}

	public void showErasePetName()
	{
		Player player = getSelf();
		if(player == null)
			return;
		if(!Config.SERVICES_CHANGE_PET_NAME_ENABLED)
		{
			show("Сервис отключен.", player);
			return;
		}
		ItemTemplate item = ItemHolder.getInstance().getTemplate(Config.SERVICES_CHANGE_PET_NAME_ITEM);
		String out = "";
		out += "<html><body>Вы можете обнулить имя у пета, для того чтобы назначить новое. Пет при этом должен быть вызван.";
		out += "<br>Стоимость обнуления: " + Util.formatAdena(Config.SERVICES_CHANGE_PET_NAME_PRICE) + " " + item.getName();
		out += "<br><button width=100 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.petevolve.exchange:erasePetName\" value=\"Обнулить имя\">";
		out += "</body></html>";
		show(out, player);
	}

	public void erasePetName()
	{
		Player player = getSelf();
		if(player == null)
			return;
		if(!Config.SERVICES_CHANGE_PET_NAME_ENABLED)
		{
			show("Сервис отключен.", player);
			return;
		}
		Servitor pl_pet = player.getServitor();
		if(pl_pet == null || !pl_pet.isPet())
		{
			show("Питомец должен быть вызван.", player);
			return;
		}
		if(player.getInventory().destroyItemByItemId(Config.SERVICES_CHANGE_PET_NAME_ITEM, Config.SERVICES_CHANGE_PET_NAME_PRICE))
		{
			pl_pet.setName(pl_pet.getTemplate().name);
			pl_pet.broadcastCharInfo();

			PetInstance _pet = (PetInstance) pl_pet;
			ItemInstance control = _pet.getControlItem();
			if(control != null)
			{
				control.setCustomType2(1);
				control.setJdbcState(JdbcEntityState.UPDATED);
				control.update();
				player.sendPacket(new InventoryUpdatePacket().addModifiedItem(player, control));
			}
			show("Имя стерто.", player);
		}
		else if(Config.SERVICES_CHANGE_PET_NAME_ITEM == 57)
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		else
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
	}

	public void exToCougar()
	{
		Player player = getSelf();
		if(player == null)
			return;
		if(!Config.SERVICES_EXCHANGE_BABY_PET_ENABLED)
		{
			show("Сервис отключен.", player);
			return;
		}
		Servitor pl_pet = player.getServitor();
		if(pl_pet == null || pl_pet.isDead() || !(pl_pet.getNpcId() == PetDataTable.IMPROVED_BABY_BUFFALO_ID || pl_pet.getNpcId() == PetDataTable.IMPROVED_BABY_KOOKABURRA_ID))
		{
			show("Пет должен быть вызван.", player);
			return;
		}
		if(player.getInventory().destroyItemByItemId(Config.SERVICES_EXCHANGE_BABY_PET_ITEM, Config.SERVICES_EXCHANGE_BABY_PET_PRICE))
		{
			ItemInstance control = player.getInventory().getItemByObjectId(player.getServitor().getControlItemObjId());
			control.setItemId(L2Pet.IMPROVED_BABY_COUGAR.getControlItemId());
			control.setJdbcState(JdbcEntityState.UPDATED);
			control.update();
			player.sendPacket(new InventoryUpdatePacket().addModifiedItem(player, control));
			player.getServitor().unSummon();
			show("Пет изменен.", player);
		}
		else if(Config.SERVICES_EXCHANGE_BABY_PET_ITEM == 57)
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		else
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
	}

	public void exToBuffalo()
	{
		Player player = getSelf();
		if(player == null)
			return;
		if(!Config.SERVICES_EXCHANGE_BABY_PET_ENABLED)
		{
			show("Сервис отключен.", player);
			return;
		}
		Servitor pl_pet = player.getServitor();
		if(pl_pet == null || pl_pet.isDead() || !(pl_pet.getNpcId() == PetDataTable.IMPROVED_BABY_COUGAR_ID || pl_pet.getNpcId() == PetDataTable.IMPROVED_BABY_KOOKABURRA_ID))
		{
			show("Пет должен быть вызван.", player);
			return;
		}
		if(Config.ALT_IMPROVED_PETS_LIMITED_USE && player.isMageClass())
		{
			show("Этот пет только для воинов.", player);
			return;
		}
		if(player.getInventory().destroyItemByItemId(Config.SERVICES_EXCHANGE_BABY_PET_ITEM, Config.SERVICES_EXCHANGE_BABY_PET_PRICE))
		{
			ItemInstance control = player.getInventory().getItemByObjectId(player.getServitor().getControlItemObjId());
			control.setItemId(L2Pet.IMPROVED_BABY_BUFFALO.getControlItemId());
			control.setJdbcState(JdbcEntityState.UPDATED);
			control.update();
			player.sendPacket(new InventoryUpdatePacket().addModifiedItem(player, control));
			player.getServitor().unSummon();
			show("Пет изменен.", player);
		}
		else if(Config.SERVICES_EXCHANGE_BABY_PET_ITEM == 57)
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		else
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
	}

	public void exToKookaburra()
	{
		Player player = getSelf();
		if(player == null)
			return;
		if(!Config.SERVICES_EXCHANGE_BABY_PET_ENABLED)
		{
			show("Сервис отключен.", player);
			return;
		}
		Servitor pl_pet = player.getServitor();
		if(pl_pet == null || pl_pet.isDead() || !(pl_pet.getNpcId() == PetDataTable.IMPROVED_BABY_BUFFALO_ID || pl_pet.getNpcId() == PetDataTable.IMPROVED_BABY_COUGAR_ID))
		{
			show("Пет должен быть вызван.", player);
			return;
		}
		if(Config.ALT_IMPROVED_PETS_LIMITED_USE && !player.isMageClass())
		{
			show("Этот пет только для магов.", player);
			return;
		}
		if(player.getInventory().destroyItemByItemId(Config.SERVICES_EXCHANGE_BABY_PET_ITEM, Config.SERVICES_EXCHANGE_BABY_PET_PRICE))
		{
			ItemInstance control = player.getInventory().getItemByObjectId(player.getServitor().getControlItemObjId());
			control.setItemId(L2Pet.IMPROVED_BABY_KOOKABURRA.getControlItemId());
			control.setJdbcState(JdbcEntityState.UPDATED);
			control.update();
			player.sendPacket(new InventoryUpdatePacket().addModifiedItem(player, control));
			player.getServitor().unSummon();
			show("Пет изменен.", player);
		}
		else if(Config.SERVICES_EXCHANGE_BABY_PET_ITEM == 57)
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		else
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
	}

	public static String DialogAppend_30731(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_30827(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_30828(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_30829(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_30830(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_30831(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_30869(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_31067(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_31265(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_31309(Integer val)
	{
		return getHtmlAppends(val);
	}

	public static String DialogAppend_31954(Integer val)
	{
		return getHtmlAppends(val);
	}

	private static String getHtmlAppends(Integer val)
	{
		String ret = "";
		if(val != 0)
			return ret;
		if(Config.SERVICES_CHANGE_PET_NAME_ENABLED)
			ret = "<br>[scripts_services.petevolve.exchange:showErasePetName|Обнулить имя у пета]";
		if(Config.SERVICES_EXCHANGE_BABY_PET_ENABLED)
			ret += "<br>[scripts_services.petevolve.exchange:showBabyPetExchange|Обменять Improved Baby пета]";
		return ret;
	}
}
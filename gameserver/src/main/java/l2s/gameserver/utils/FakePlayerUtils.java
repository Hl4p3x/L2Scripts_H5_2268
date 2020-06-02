package l2s.gameserver.utils;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import org.napile.primitive.lists.IntList;
import org.napile.primitive.lists.impl.ArrayIntList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.FakeAI;
import l2s.gameserver.data.xml.holder.FakeItemHolder;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.handler.items.IItemHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.c2s.Say2C;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.ItemTemplate.Grade;
import l2s.gameserver.templates.item.WeaponTemplate;

/**
 LIO 
 30.01.2016
 */
//TODO: Добавить задержку между одеванием предметов.
//TODO: Продумать одевание предметов, шанс, момент, время и т.д.
//TODO: Переписать эту дичь.
public class FakePlayerUtils
{
	private static final Logger _log = LoggerFactory.getLogger(FakePlayerUtils.class);

	private static String[] PM_MESSAGES = new String[0];
	private static String[] SHOUT_MESSAGES = new String[0];
	private static String[] TRADE_MESSAGES = new String[0];

	static
	{
		List<String> messages = new ArrayList<String>();

		LineNumberReader reader = null;
		try
		{
			File file = new File(Config.DATAPACK_ROOT, "data/fake_players/shout_messages.txt");
			reader = new LineNumberReader(new FileReader(file));
			String line = null;
			while((line = reader.readLine()) != null)
			{
				messages.add(line);
			}
		}
		catch(Exception e)
		{
			//
		}
		finally
		{
			try
			{
				reader.close();
			}
			catch(Exception e)
			{
				//
			}
		}

		SHOUT_MESSAGES = messages.toArray(new String[messages.size()]);

		messages.clear();

		try
		{
			File file = new File(Config.DATAPACK_ROOT, "data/fake_players/private_messages.txt");
			reader = new LineNumberReader(new FileReader(file));
			String line = null;
			while((line = reader.readLine()) != null)
			{
				messages.add(line);
			}
		}
		catch(Exception e)
		{
			//
		}
		finally
		{
			try
			{
				reader.close();
			}
			catch(Exception e)
			{
				//
			}
		}

		PM_MESSAGES = messages.toArray(new String[messages.size()]);

		messages.clear();

		try
		{
			File file = new File(Config.DATAPACK_ROOT, "data/fake_players/trade_messages.txt");
			reader = new LineNumberReader(new FileReader(file));
			String line = null;
			while((line = reader.readLine()) != null)
			{
				messages.add(line);
			}
		}
		catch(Exception e)
		{
			//
		}
		finally
		{
			try
			{
				reader.close();
			}
			catch(Exception e)
			{
				//
			}
		}

		TRADE_MESSAGES = messages.toArray(new String[messages.size()]);
	}

	public static void writeInPrivateChat(FakeAI ai, String receiver)
	{
		if(PM_MESSAGES.length > 0 && Rnd.chance(80))
		{
			ThreadPoolManager.getInstance().schedule(() -> Say2C.writeToChat(ai.getActor(), Rnd.get(PM_MESSAGES), ChatType.TELL, receiver), Rnd.get(3000, 20000));
		}
	}

	public static void writeToRandomChat(FakeAI ai)
	{
		if(SHOUT_MESSAGES.length > 0 && Rnd.chance(10))
		{
			Say2C.writeToChat(ai.getActor(), Rnd.get(SHOUT_MESSAGES), ChatType.SHOUT, null);
		}
		else if(TRADE_MESSAGES.length > 0 && Rnd.chance(10))
		{
			Say2C.writeToChat(ai.getActor(), Rnd.get(TRADE_MESSAGES), ChatType.TRADE, null);
		}
	}

	public static boolean checkInventory(FakeAI ai)
	{
		Player player = ai.getActor();
		Inventory inventory = player.getInventory();

		int equipedGrade = player.getVarInt("equiped_grade", 0);
		int expertiseIndex = player.getExpertiseIndex();

		IntList equip = new ArrayIntList();

		inventory.writeLock();
		try
		{
			boolean checkWeapon = inventory.getPaperdollItem(Inventory.PAPERDOLL_RHAND) == null;

			boolean checkHead = inventory.getPaperdollItem(Inventory.PAPERDOLL_HEAD) == null;
			boolean checkGloves = inventory.getPaperdollItem(Inventory.PAPERDOLL_GLOVES) == null;
			boolean checkChest = inventory.getPaperdollItem(Inventory.PAPERDOLL_CHEST) == null;
			boolean checkLegs = inventory.getPaperdollItem(Inventory.PAPERDOLL_LEGS) == null;
			boolean checkFeet = inventory.getPaperdollItem(Inventory.PAPERDOLL_FEET) == null;
			boolean checkArmor = /*checkHead && checkGloves && checkFeet && */checkChest && checkLegs;

			boolean checkREar = inventory.getPaperdollItem(Inventory.PAPERDOLL_REAR) == null;
			boolean checkLEar = inventory.getPaperdollItem(Inventory.PAPERDOLL_LEAR) == null;
			boolean checkNeck = inventory.getPaperdollItem(Inventory.PAPERDOLL_NECK) == null;
			boolean checkRFinger = inventory.getPaperdollItem(Inventory.PAPERDOLL_RFINGER) == null;
			boolean checkLFinger = inventory.getPaperdollItem(Inventory.PAPERDOLL_LFINGER) == null;
			boolean checkAccessory = checkREar && checkLEar && checkNeck && checkRFinger && checkLFinger;

			boolean checkHair = inventory.getPaperdollItem(Inventory.PAPERDOLL_HAIR) == null;
			boolean checkDHair = inventory.getPaperdollItem(Inventory.PAPERDOLL_DHAIR) == null;
			boolean checkHairs = checkHair && checkDHair;

			boolean checkCloak = inventory.getPaperdollItem(Inventory.PAPERDOLL_BACK) == null;

			boolean checkAllEquip = equipedGrade < expertiseIndex && (equipedGrade > 0 || Rnd.chance(10));

			if(checkAllEquip)
				player.setVar("equiped_grade", expertiseIndex);

			if(checkAllEquip || checkAccessory)
				equip.addAll(FakeItemHolder.getInstance().getRandomItems(player, "Accessory", expertiseIndex));

			if(checkAllEquip || checkArmor)
				equip.addAll(FakeItemHolder.getInstance().getRandomItems(player, "Armor", expertiseIndex));

			if(checkAllEquip || checkWeapon)
				equip.addAll(FakeItemHolder.getInstance().getRandomItems(player, "Weapon", expertiseIndex));

			if(checkHairs && Rnd.chance(25) || Rnd.chance(5))
			{
				IntList hairs = FakeItemHolder.getInstance().getHairAccessories();
				if(!hairs.isEmpty())
					equip.add(Rnd.get(hairs.toArray()));
			}

			if(checkCloak && Rnd.chance(25) || Rnd.chance(5))
			{
				IntList cloaks = FakeItemHolder.getInstance().getCloaks();
				if(!cloaks.isEmpty())
					equip.add(Rnd.get(cloaks.toArray()));
			}

			for(int itemId : equip.toArray())
				addAndEquip(ai, itemId);

			ItemInstance[] items = inventory.getItems();
			for(ItemInstance item : items)
			{
				if(item.getItemId() == ItemTemplate.ITEM_ID_ADENA)
					continue;

				if(item.isEquipped())
					continue;
				
				inventory.destroyItem(item);
			}
		}
		finally
		{
			inventory.writeUnlock();
		}

		addArrow(player);
		addShot(player);

		if(inventory.getCountOf(1785) < 1000)
			ItemFunctions.addItem(player, 1785, 1000, false, "Added spirit ores to fake player");
		if(inventory.getCountOf(3031) < 1000)
			ItemFunctions.addItem(player, 3031, 1000, false, "Added soul ores fake player");

		return !equip.isEmpty();
	}

	public static void checkAutoShots(FakeAI ai)
	{
		Player player = ai.getActor();
		for(ItemInstance item : player.getInventory().getItems())
		{
			IItemHandler handler = item.getTemplate().getHandler();
			if(handler != null && handler.isAutoUse())
			{
				player.addAutoSoulShot(item.getItemId());
				ItemFunctions.useItem(player, item, false, false);
			}
		}
	}

	public static void addAndEquip(FakeAI ai, int itemId)
	{
		ItemTemplate item = ItemHolder.getInstance().getTemplate(itemId);
		if(item == null)
			return;

		Player player = ai.getActor();

		ItemInstance itemInstance = player.getInventory().addItem(itemId, 1);
		if(ItemFunctions.checkIfCanEquip(player, itemInstance) == null)
		{
			if(itemInstance.canBeEnchanted() && itemInstance.getCrystalType() != Grade.NONE)
			{
				int enchant = 0;
				for(int i = 0; i <= 10; i++)
				{
					if(Rnd.chance(30))
						enchant++;
					else
						break;
				}
				itemInstance.setEnchantLevel(enchant);
			}
			player.getInventory().equipItem(itemInstance);
		}
	}

	public static void addArrow(Player player)
	{
		if(player.getActiveWeaponTemplate() != null && (player.getActiveWeaponTemplate().getItemType() == WeaponTemplate.WeaponType.BOW || player.getActiveWeaponTemplate().getItemType() == WeaponTemplate.WeaponType.CROSSBOW))
		{
			int itemId = 0;
			int itemId2 = 0;
			switch(player.getActiveWeaponTemplate().getCrystalType())
			{
				case NONE:
					itemId = 17;
					itemId2 = 9632;
					break;
				case D:
					itemId = 1341;
					itemId2 = 9633;
					break;
				case C:
					itemId = 1342;
					itemId2 = 9634;
					break;
				case B:
					itemId = 1343;
					itemId2 = 9635;
					break;
				case A:
					itemId = 1344;
					itemId2 = 9636;
					break;
				case S:
				case S80:
				case S84:
					itemId = 1345;
					itemId2 = 9637;
					break;
			}
			if(player.getInventory().getCountOf(itemId) < 1000)
				ItemFunctions.addItem(player, itemId, 1000, false, "Added arrows to fake player");
			if(player.getInventory().getCountOf(itemId2) < 1000)
				ItemFunctions.addItem(player, itemId2, 1000, false, "Added bolts to fake player");
		}
	}

	public static void addShot(Player player)
	{
		if(player.getActiveWeaponTemplate() != null)
		{
			int itemIdSS = 0;
			int itemIdBSS = 0;
			switch(player.getActiveWeaponTemplate().getCrystalType())
			{
				case NONE:
					itemIdSS = 1835;
					itemIdBSS = 3947;
					break;
				case D:
					itemIdSS = 1463;
					itemIdBSS = 3948;
					break;
				case C:
					itemIdSS = 1464;
					itemIdBSS = 3949;
					break;
				case B:
					itemIdSS = 1465;
					itemIdBSS = 3950;
					break;
				case A:
					itemIdSS = 1466;
					itemIdBSS = 3951;
					break;
				case S:
				case S80:
				case S84:
					itemIdSS = 1467;
					itemIdBSS = 3952;
					break;
			}

			if(itemIdSS > 0)
			{
				if(player.getInventory().getCountOf(itemIdSS) < 3000)
					ItemFunctions.addItem(player, itemIdSS, 3000, false, "Added soulshots to fake player");
				player.addAutoSoulShot(itemIdSS);
			}

			if(itemIdBSS > 0)
			{
				if(player.getInventory().getCountOf(itemIdBSS) < 1000)
					ItemFunctions.addItem(player, itemIdBSS, 1000, false, "Added spiritshots to fake player");
				player.addAutoSoulShot(itemIdBSS);
			}
		}
	}

	public static void setProf(Player player)
	{
		List<Integer> allowClassIds = getAllowClassIds(player);
		if(!allowClassIds.isEmpty())
		{
			int classId = allowClassIds.get(Rnd.get(allowClassIds.size()));
			player.setClassId(classId, true, false);
		}
		player.rewardSkills(false, true);
		player.refreshExpertisePenalty();
	}

	private static List<Integer> getAllowClassIds(Player player)
	{
		List<Integer> allowClassId = new ArrayList<>();

		ClassId playerClassId = player.getClassId();
		int playerClassLevel = playerClassId.getClassLevel().ordinal();
		int playerLevel = player.getLevel();

		if(playerLevel >= 20 && playerClassLevel == 0 || playerLevel >= 40 && playerClassLevel == 1 || playerLevel >= 76 && playerClassLevel == 2 || playerLevel >= 85 && playerClassLevel == 3)
		{
			for(ClassId classId : ClassId.VALUES)
			{
				if(classId.isDummy())
					continue;

				if(classId == ClassId.INSPECTOR)
					continue;

				switch(classId)
				{
					case WARLOCK:
					case CLERIC:
					case SWORDSINGER:
					case ELEMENTAL_SUMMONER:
					case ORACLE:
					case PHANTOM_SUMMONER:
					case SHILLEN_ORACLE:
						continue;
				}

				if(classId.childOf(playerClassId) && classId.getClassLevel().ordinal() == playerClassId.getClassLevel().ordinal() + 1)
					allowClassId.add(classId.getId());
			}
		}

		return allowClassId;
	}
}
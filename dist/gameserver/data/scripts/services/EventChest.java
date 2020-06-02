package services;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.utils.ItemFunctions;

//By Evil_dnk

public class EventChest extends Functions
{

	private static final int[] potions = { 1374, // Greater Haste Potion
			1375, // Greater Swift Attack Potion
			6036, // Greater Magic Haste Potion
			1539, // Greater Healing Potion
			8633, // Elixir of Mental Strength (S-Grade)
			8627, // Elixir of Life (S-Grade)
			8639 // Elixir of CP (S-Grade)
	};

	private static final int[] scroll = {
			3936, //    Blessed Scroll of Resurrection
			1538, //    Blessed Scroll of Escape
	};
	private static final int[] bufpotions = {
			10267,
			10266,
			10261,
			10263,
			10268,
			10269,
			10264,
			10260,
			10265,
			10262,
			10260,
	};

	private static final int[] attr = {
			9546,
			9547,
			9548,
			9549,
			9550,
			9551,
	};

	private static final int[] crystals = {
			9552,
			9553,
			9554,
			9555,
			9556,
			9557
	};
	private static final int[] enchant = {
			955,
			956,
			951,
			947,
			952,
			948,
			959,
			960,
			6578,
			6577,
			730,
			729,
			6577,
			6577,
	};
	private static final int[] enchant_dest = {
			22222,
			22221,
	};

	public void DoReward1()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if (npc == null || npc.getReflection().getId() != player.getReflection().getId() || !player.checkInteractionDistance(npc))
			return;
		else if (player.getInventory().getCountOf(10017) == 0)
		{
			player.sendMessage("You dont have Coral Key");
			return;
		}
		else if(ItemFunctions.deleteItem(player, 10017, 1))
		{
			if(Rnd.chance(25))
				ItemFunctions.addItem(player, scroll[Rnd.get(scroll.length)], Rnd.get(1, 3), "Chest event reward");
			if(Rnd.chance(50))
				ItemFunctions.addItem(player, bufpotions[Rnd.get(bufpotions.length)], Rnd.get(1, 5), "Chest event reward");

			ItemFunctions.addItem(player, potions[Rnd.get(potions.length)], Rnd.get(1, 2), "Chest event reward");
			npc.deleteMe();
		}
	}

	public void DoReward5()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if (npc == null || npc.getReflection().getId() != player.getReflection().getId() || !player.checkInteractionDistance(npc))
			return;
		else if (player.getInventory().getCountOf(10017) < 5)
		{
			player.sendMessage("You dont have 5 Coral Keys");
			return;
		}
		else if(ItemFunctions.deleteItem(player, 10017, 5))
		{
			if(Rnd.chance(45))
				ItemFunctions.addItem(player, scroll[Rnd.get(scroll.length)], Rnd.get(3, 5), "Chest event reward");
			if(Rnd.chance(70))
				ItemFunctions.addItem(player, bufpotions[Rnd.get(bufpotions.length)], Rnd.get(3, 8), "Chest event reward");
			if(Rnd.chance(5))
				ItemFunctions.addItem(player, enchant[Rnd.get(enchant.length)], 1, "Chest event reward");
			if(Rnd.chance(50))
				ItemFunctions.addItem(player, attr[Rnd.get(attr.length)], Rnd.get(1, 2), "Chest event reward");

			ItemFunctions.addItem(player, potions[Rnd.get(potions.length)], Rnd.get(5, 10), "Chest event reward");
			npc.deleteMe();
		}
	}
	public void DoReward10()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if (npc == null || npc.getReflection().getId() != player.getReflection().getId() || !player.checkInteractionDistance(npc))
			return;
		else if (player.getInventory().getCountOf(10017) < 10)
		{
			player.sendMessage("You dont have 10 Coral Keys");
			return;
		}
		else if(ItemFunctions.deleteItem(player, 10017, 10))
		{
			if(Rnd.chance(45))
				ItemFunctions.addItem(player, scroll[Rnd.get(scroll.length)], Rnd.get(3, 7), "Chest event reward");
			if(Rnd.chance(70))
				ItemFunctions.addItem(player, bufpotions[Rnd.get(bufpotions.length)], Rnd.get(3, 10), "Chest event reward");
			if(Rnd.chance(20))
				ItemFunctions.addItem(player, enchant[Rnd.get(enchant.length)], Rnd.get(1, 3), "Chest event reward");
			if(Rnd.chance(1))
				ItemFunctions.addItem(player, enchant_dest[Rnd.get(enchant_dest.length)], 1, "Chest event reward");
			if(Rnd.chance(50))
				ItemFunctions.addItem(player, attr[Rnd.get(attr.length)], Rnd.get(2, 5), "Chest event reward");
			if(Rnd.chance(25))
				ItemFunctions.addItem(player, crystals[Rnd.get(crystals.length)], Rnd.get(1, 2), "Chest event reward");

			ItemFunctions.addItem(player, potions[Rnd.get(potions.length)], Rnd.get(10, 20), "Chest event reward");
			npc.deleteMe();
		}
	}
}
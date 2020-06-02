package services;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.DoorInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.ReflectionUtils;

/**
 * Используется в локации Pagan Temple
 * @Author: SYS
 */
public class PaganDoormans extends Functions
{
	private static final int MainDoorId = 19160001;
	private static final int SecondDoor1Id = 19160011;
	private static final int SecondDoor2Id = 19160010;

	public void openMainDoor()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		if(!NpcInstance.canBypassCheck(player, npc))
			return;

		if(ItemFunctions.getItemCount(player, 8064) == 0 && ItemFunctions.getItemCount(player, 8067) == 0)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
			return;
		}

		openDoor(MainDoorId);
		show("default/32034-1.htm", player, npc);
	}

	public void openSecondDoor()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		if(!NpcInstance.canBypassCheck(player, npc))
			return;

		if(ItemFunctions.getItemCount(player, 8067) == 0)
		{
			show("default/32036-2.htm", player, npc);
			return;
		}

		openDoor(SecondDoor1Id);
		openDoor(SecondDoor2Id);
		show("default/32036-1.htm", player, npc);
	}

	public void pressSkull()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		if(!NpcInstance.canBypassCheck(player, npc))
			return;

		openDoor(MainDoorId);
		show("default/32035-1.htm", player, npc);
	}

	public void press2ndSkull()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		if(!NpcInstance.canBypassCheck(player, npc))
			return;

		openDoor(SecondDoor1Id);
		openDoor(SecondDoor2Id);
		show("default/32037-1.htm", player, npc);
	}

	private static void openDoor(int doorId)
	{
		DoorInstance door = ReflectionUtils.getDoor(doorId);
		door.openMe();
	}
}
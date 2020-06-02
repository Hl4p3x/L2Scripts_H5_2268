package services;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author pchayka
 */
public class Misc extends Functions
{
	public void assembleAntharasCrystal()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();

		if(player == null || npc == null || !NpcInstance.canBypassCheck(player, player.getLastNpc()))
			return;

		if(ItemFunctions.getItemCount(player, 17266) < 1 || ItemFunctions.getItemCount(player, 17267) < 1)
		{
			show("teleporter/32864-2.htm", player);
			return;
		}
		if(ItemFunctions.deleteItem(player, 17266, 1, true) && ItemFunctions.deleteItem(player, 17267, 1, true))
		{
			ItemFunctions.addItem(player, 17268, 1, true, "Assemble Antharas crystal");
			show("teleporter/32864-3.htm", player);
			return;
		}
	}
}
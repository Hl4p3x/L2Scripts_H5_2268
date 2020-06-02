package services;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.utils.ItemFunctions;

public class TakeBeastHandler extends Functions
{
	private final int BEAST_WHIP = 15473;

	public void show()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();

		String htmltext;
		if(player.getLevel() < 82)
			htmltext = npc.getNpcId() + "-1.htm";
		else if(ItemFunctions.getItemCount(player, BEAST_WHIP) > 0)
			htmltext = npc.getNpcId() + "-2.htm";
		else
		{
			ItemFunctions.addItem(player, BEAST_WHIP, 1, "Take beast whip");
			htmltext = npc.getNpcId() + "-3.htm";
		}

		npc.showChatWindow(player, "default/" + htmltext);
	}
}

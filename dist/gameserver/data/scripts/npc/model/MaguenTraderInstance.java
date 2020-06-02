package npc.model;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.NpcUtils;

/**
 * @author pchayka
 */
public final class MaguenTraderInstance extends NpcInstance
{
	public MaguenTraderInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equalsIgnoreCase("request_collector"))
		{
			if(ItemFunctions.getItemCount(player, 15487) > 0)
				showChatWindow(player, "default/32735-2.htm");
			else
				ItemFunctions.addItem(player, 15487, 1, "Give collector by MaguenTraderInstance");
		}
		else if(command.equalsIgnoreCase("request_maguen"))
		{
			NpcUtils.spawnSingle(18839, Location.findPointToStay(getSpawnedLoc(), 40, 100, getGeoIndex()), getReflection()); // wild maguen
			showChatWindow(player, "default/32735-3.htm");
		}
		else
			super.onBypassFeedback(player, command);
	}
}
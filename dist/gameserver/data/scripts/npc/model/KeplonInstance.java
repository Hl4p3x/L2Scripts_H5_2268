package npc.model;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author pchayka
 */

public final class KeplonInstance extends NpcInstance
{
	public KeplonInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(checkForDominionWard(player))
			return;

		if(command.equalsIgnoreCase("buygreen"))
		{
			if(ItemFunctions.deleteItem(player, 57, 10000, true))
			{
				ItemFunctions.addItem(player, 4401, 1, true, "Buy green buy KeplonInstance");
				return;
			}
			else
			{
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}
		}
		else if(command.startsWith("buyblue"))
		{
			if(ItemFunctions.deleteItem(player, 57, 10000, true))
			{
				ItemFunctions.addItem(player, 4402, 1, true, "Buy blue buy KeplonInstance");
				return;
			}
			else
			{
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}
		}
		else if(command.startsWith("buyred"))
		{
			if(ItemFunctions.deleteItem(player, 57, 10000, true))
			{
				ItemFunctions.addItem(player, 4403, 1, true, "Buy red buy KeplonInstance");
				return;
			}
			else
			{
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}
		}
		else
			super.onBypassFeedback(player, command);
	}
}
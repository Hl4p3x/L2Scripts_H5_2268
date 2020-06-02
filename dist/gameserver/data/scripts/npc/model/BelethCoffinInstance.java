package npc.model;

import java.util.StringTokenizer;

import bosses.BelethManager;
import l2s.gameserver.model.CommandChannel;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author pchayka
 */

public final class BelethCoffinInstance extends NpcInstance
{
	private static final int RING = 10314;

	public BelethCoffinInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		StringTokenizer st = new StringTokenizer(command);
		if(st.nextToken().equals("request_ring"))
		{
			if(!BelethManager.isRingAvailable())
			{
				player.sendPacket(new NpcHtmlMessagePacket(player, this).setHtml("Stone Coffin:<br><br>Ring is not available. Get lost!"));
				return;
			}
			if(player.getParty() == null || player.getParty().getCommandChannel() == null)
			{
				player.sendPacket(new NpcHtmlMessagePacket(player, this).setHtml("Stone Coffin:<br><br>You are not allowed to take the ring. Are are not the group or Command Channel."));
				return;
			}
			if(player.getParty().getCommandChannel().getChannelLeader() != player)
			{
				player.sendPacket(new NpcHtmlMessagePacket(player, this).setHtml("Stone Coffin:<br><br>You are not leader or the Command Channel."));
				return;
			}

			CommandChannel channel = player.getParty().getCommandChannel();

			ItemFunctions.addItem(player, RING, 1, "Take ring by BelethCoffinInstance");

			SystemMessage smsg = new SystemMessage(SystemMessage.S1_HAS_OBTAINED_S2);
			smsg.addString(player.getName());
			smsg.addItemName(RING);
			channel.broadCast(smsg);

			BelethManager.setRingAvailable(false);
			deleteMe();

		}
		else
			super.onBypassFeedback(player, command);
	}
}
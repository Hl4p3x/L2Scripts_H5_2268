package l2s.gameserver.model.instances;

import java.text.SimpleDateFormat;

import l2s.gameserver.instancemanager.itemauction.ItemAuction;
import l2s.gameserver.instancemanager.itemauction.ItemAuctionInstance;
import l2s.gameserver.instancemanager.itemauction.ItemAuctionManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExItemAuctionInfoPacket;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * @author n0nam3
 */
public class ItemAuctionBrokerInstance extends NpcInstance
{
	private static final long serialVersionUID = 1L;

	private static final SimpleDateFormat fmt = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

	protected ItemAuctionInstance _instance;

	public ItemAuctionBrokerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void showChatWindow(Player player, final int val, Object... arg)
	{
		String filename = val == 0 ? "itemauction/itembroker.htm" : "itemauction/itembroker-" + val + ".htm";
		player.sendPacket(new NpcHtmlMessagePacket(player, this, filename, val));
	}

	public int getAuctionInstanceId()
	{
		return getNpcId();
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;
		final String[] params = command.split(" ");
		if(params.length == 1)
			return;

		if(params[0].equals("auction"))
		{
			if(_instance == null)
			{
				_instance = ItemAuctionManager.getInstance().getManagerInstance(getAuctionInstanceId());
				if(_instance == null)
					//_log.error("L2ItemAuctionBrokerInstance: Missing instance for: " + getTemplate().npcId);
					return;
			}

			if(params[1].equals("cancel"))
			{
				if(params.length == 3)
				{
					int auctionId = 0;

					try
					{
						auctionId = Integer.parseInt(params[2]);
					}
					catch(NumberFormatException e)
					{
						e.printStackTrace();
						return;
					}

					final ItemAuction auction = _instance.getAuction(auctionId);
					if(auction != null)
						auction.cancelBid(player);
					else
						player.sendPacket(SystemMsg.THERE_ARE_NO_FUNDS_PRESENTLY_DUE_TO_YOU);
				}
				else
				{
					final ItemAuction[] auctions = _instance.getAuctionsByBidder(player.getObjectId());
					for(final ItemAuction auction : auctions)
						auction.cancelBid(player);
				}
			}
			else if(params[1].equals("show"))
			{
				final ItemAuction currentAuction = _instance.getCurrentAuction();
				final ItemAuction nextAuction = _instance.getNextAuction();

				if(currentAuction == null)
				{
					player.sendPacket(SystemMsg.IT_IS_NOT_AN_AUCTION_PERIOD);
					return;
				}

				if(!player.getAndSetLastItemAuctionRequest())
				{
					player.sendPacket(SystemMsg.THERE_ARE_NO_OFFERINGS_I_OWN_OR_I_MADE_A_BID_FOR);
					return;
				}

				player.sendPacket(new ExItemAuctionInfoPacket(false, currentAuction, nextAuction));
			}
		}
		else
			super.onBypassFeedback(player, command);
	}
}
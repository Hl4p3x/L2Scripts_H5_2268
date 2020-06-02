package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.instancemanager.itemauction.ItemAuction;
import l2s.gameserver.instancemanager.itemauction.ItemAuctionInstance;
import l2s.gameserver.instancemanager.itemauction.ItemAuctionManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.ItemAuctionBrokerInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;

/**
 * @author n0nam3
 */
public final class RequestBidItemAuction extends L2GameClientPacket
{
	private int _instanceId;
	private long _bid;

	@Override
	protected final void readImpl()
	{
		_instanceId = readD();
		_bid = readQ();
	}

	@Override
	protected final void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		NpcInstance npc = activeChar.getLastNpc();
		if(npc == null || !activeChar.checkInteractionDistance(npc) || !(npc instanceof ItemAuctionBrokerInstance))
			return;

		ItemAuctionBrokerInstance broker = (ItemAuctionBrokerInstance) npc;
		if(broker.getAuctionInstanceId() != _instanceId)
			return;

		final ItemAuctionInstance instance = ItemAuctionManager.getInstance().getManagerInstance(_instanceId);
		if(instance != null)
		{
			final ItemAuction auction = instance.getCurrentAuction();
			if(auction != null)
			{
				ItemInstance bid = activeChar.getInventory().getItemByItemId(auction.getAuctionItem().getActionBidItemId());
				if(bid == null || _bid < 0 || _bid > bid.getCount())
					return;

				auction.registerBid(activeChar, _bid);
			}
		}
	}
}
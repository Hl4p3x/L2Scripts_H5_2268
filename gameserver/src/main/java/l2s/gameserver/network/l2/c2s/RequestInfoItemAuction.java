package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.instancemanager.itemauction.ItemAuction;
import l2s.gameserver.instancemanager.itemauction.ItemAuctionInstance;
import l2s.gameserver.instancemanager.itemauction.ItemAuctionManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.ItemAuctionBrokerInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.ExItemAuctionInfoPacket;

/**
 * @author n0nam3
 */
public final class RequestInfoItemAuction extends L2GameClientPacket
{
	private int _instanceId;

	@Override
	protected final void readImpl()
	{
		_instanceId = readD();
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

		activeChar.getAndSetLastItemAuctionRequest();

		final ItemAuctionInstance instance = ItemAuctionManager.getInstance().getManagerInstance(_instanceId);
		if(instance == null)
			return;

		final ItemAuction auction = instance.getCurrentAuction();
		if(auction == null)
			return;

		activeChar.sendPacket(new ExItemAuctionInfoPacket(true, auction, instance.getNextAuction()));
	}
}
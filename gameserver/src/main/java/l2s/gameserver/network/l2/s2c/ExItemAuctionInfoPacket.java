package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.instancemanager.itemauction.ItemAuction;
import l2s.gameserver.instancemanager.itemauction.ItemAuctionBid;
import l2s.gameserver.instancemanager.itemauction.ItemAuctionState;

/**
 * @author n0nam3
 */
public class ExItemAuctionInfoPacket extends L2GameServerPacket
{
	private boolean _refresh;
	private int _timeRemaining;
	private ItemAuction _currentAuction;
	private ItemAuction _nextAuction;

	public ExItemAuctionInfoPacket(boolean refresh, ItemAuction currentAuction, ItemAuction nextAuction)
	{
		if(currentAuction == null)
			throw new NullPointerException();

		if(currentAuction.getAuctionState() != ItemAuctionState.STARTED)
			_timeRemaining = 0;
		else
			_timeRemaining = (int) (currentAuction.getFinishingTimeRemaining() / 1000); // in seconds

		_refresh = refresh;
		_currentAuction = currentAuction;
		_nextAuction = nextAuction;
	}

	@Override
	protected void writeImpl()
	{
		writeC(_refresh ? 0x00 : 0x01);
		writeD(_currentAuction.getInstanceId());

		ItemAuctionBid highestBid = _currentAuction.getHighestBid();
		writeQ(highestBid != null ? highestBid.getLastBid() : _currentAuction.getAuctionInitBid());

		writeD(_timeRemaining);
		writeItemInfo(_currentAuction.getAuctionItem());

		if(_nextAuction != null)
		{
			writeQ(_nextAuction.getAuctionInitBid());
			writeD((int) (_nextAuction.getStartingTime() / 1000L)); // unix time in seconds
			writeItemInfo(_nextAuction.getAuctionItem());
		}
	}
}
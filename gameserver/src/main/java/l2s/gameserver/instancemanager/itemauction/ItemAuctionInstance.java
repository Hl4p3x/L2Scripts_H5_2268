package l2s.gameserver.instancemanager.itemauction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.dao.JdbcEntityState;
import l2s.commons.dbutils.DbUtils;
import l2s.commons.threading.RunnableImpl;
import l2s.commons.time.cron.SchedulingPattern;
import l2s.commons.util.Rnd;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.ItemInstance.ItemLocation;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class ItemAuctionInstance
{
	private static final Logger _log = LoggerFactory.getLogger(ItemAuctionInstance.class);

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

	private static final long START_TIME_SPACE = TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES);
	private static final long FINISH_TIME_SPACE = TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES);

	private final int _instanceId;
	private final TIntObjectMap<ItemAuction> _auctions;
	private final List<AuctionItem> _items;
	private final SchedulingPattern _dateTime;

	private ItemAuction _currentAuction;
	private ItemAuction _nextAuction;
	private ScheduledFuture<?> _stateTask;

	ItemAuctionInstance(int instanceId, SchedulingPattern dateTime, List<AuctionItem> items) throws Exception
	{
		_instanceId = instanceId;
		_auctions = new TIntObjectHashMap<ItemAuction>();
		_items = items;
		_dateTime = dateTime;

		load();

		_log.info("ItemAuction: Loaded " + _items.size() + " item(s) and registered " + _auctions.size() + " auction(s) for instance " + _instanceId + ".");

		checkAndSetCurrentAndNextAuction();
	}

	private void load()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT auctionId FROM item_auction WHERE instanceId=?");
			statement.setInt(1, _instanceId);
			rset = statement.executeQuery();

			while(rset.next())
			{
				int auctionId = rset.getInt(1);
				try
				{
					ItemAuction auction = loadAuction(auctionId);
					if(auction != null)
						_auctions.put(auctionId, auction);
					else
						ItemAuctionManager.getInstance().deleteAuction(auctionId);
				}
				catch(SQLException e)
				{
					_log.warn("ItemAuction: Failed loading auction: " + auctionId, e);
				}
			}
		}
		catch(SQLException e)
		{
			_log.error("ItemAuction: Failed loading auctions.", e);
			return;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public ItemAuction getCurrentAuction()
	{
		return _currentAuction;
	}

	public ItemAuction getNextAuction()
	{
		return _nextAuction;
	}

	public void shutdown()
	{
		ScheduledFuture<?> stateTask = _stateTask;
		if(stateTask != null)
			stateTask.cancel(false);
	}

	private AuctionItem getAuctionItem(int auctionItemId)
	{
		for(int i = _items.size(); i-- > 0;)
			try
			{
				AuctionItem item = _items.get(i);
				if(item.getAuctionItemId() == auctionItemId)
					return item;
			}
			catch(IndexOutOfBoundsException e)
			{

			}
		return null;
	}

	void checkAndSetCurrentAndNextAuction()
	{
		ItemAuction[] auctions = _auctions.values(new ItemAuction[_auctions.size()]);

		ItemAuction currentAuction = null;
		ItemAuction nextAuction = null;

		switch(auctions.length)
		{
			case 0:
			{
				nextAuction = createAuction(System.currentTimeMillis() + START_TIME_SPACE);
				break;
			}

			case 1:
			{
				switch(auctions[0].getAuctionState())
				{
					case CREATED:
					{
						if(auctions[0].getStartingTime() < System.currentTimeMillis() + START_TIME_SPACE)
						{
							currentAuction = auctions[0];
							nextAuction = createAuction(System.currentTimeMillis() + START_TIME_SPACE);
						}
						else
							nextAuction = auctions[0];
						break;
					}

					case STARTED:
					{
						currentAuction = auctions[0];
						nextAuction = createAuction(Math.max(currentAuction.getEndingTime() + FINISH_TIME_SPACE, System.currentTimeMillis() + START_TIME_SPACE));
						break;
					}

					case FINISHED:
					{
						currentAuction = auctions[0];
						nextAuction = createAuction(System.currentTimeMillis() + START_TIME_SPACE);
						break;
					}

					default:
						throw new IllegalArgumentException();
				}
				break;
			}

			default:
			{
				// just to make sure we won`t skip any auction because of little different times
				long currentTime = System.currentTimeMillis();

				for(int i = 0; i < auctions.length; i++)
				{
					ItemAuction auction = auctions[i];
					if(auction.getAuctionState() == ItemAuctionState.STARTED)
					{
						currentAuction = auction;
						break;
					}
					else if(auction.getStartingTime() <= currentTime)
					{
						if(currentAuction == null || auction.getStartingTime() > currentAuction.getStartingTime())
							currentAuction = auction;
					}
				}

				for(int i = 0; i < auctions.length; i++)
				{
					ItemAuction auction = auctions[i];
					if(auction.getStartingTime() > currentTime && currentAuction != auction)
					{
						if(nextAuction == null || auction.getStartingTime() < currentAuction.getStartingTime())
							nextAuction = auction;
					}
				}

				if(nextAuction == null)
					nextAuction = createAuction(System.currentTimeMillis() + START_TIME_SPACE);
				break;
			}
		}

		_currentAuction = currentAuction;

		if(currentAuction != null && currentAuction.getAuctionState() == ItemAuctionState.STARTED)
		{
			setStateTask(ThreadPoolManager.getInstance().schedule(new ScheduleAuctionTask(currentAuction), Math.max(currentAuction.getEndingTime() - System.currentTimeMillis(), 0L)));
			_log.info("ItemAuction: Schedule current auction " + currentAuction.getAuctionId() + " for instance " + _instanceId);
		}
		else if(nextAuction != null)
		{
			_auctions.put(nextAuction.getAuctionId(), nextAuction);
			_nextAuction = nextAuction;

			setStateTask(ThreadPoolManager.getInstance().schedule(new ScheduleAuctionTask(nextAuction), Math.max(nextAuction.getStartingTime() - System.currentTimeMillis(), 0L)));
			_log.info("ItemAuction: Schedule next auction " + nextAuction.getAuctionId() + " on " + DATE_FORMAT.format(new Date(nextAuction.getStartingTime())) + " for instance " + _instanceId);
		}

		for(ItemAuction auction : getAuctions()) // Чистим от устаревших аукционов, чтобы базу не засорять.
		{
			if(auction == _nextAuction)
				continue;

			if(auction == _currentAuction)
				continue;

			if(auction.getStartingTime() >= System.currentTimeMillis())
				continue;

			deleteAuction(auction.getAuctionId());
		}
	}

	public ItemAuction getAuction(int auctionId)
	{
		return _auctions.get(auctionId);
	}

	private boolean deleteAuction(int auctionId)
	{
		ItemAuction auction = _auctions.get(auctionId);
		if(auction == null)
			return false;

		if(auction.getAuctionState() == ItemAuctionState.STARTED)
			return false;

		if(auction.hasBids()) // Нельзя удалить аукцион, если не все персонажи забрали свои ставки.
			return false;

		_auctions.remove(auctionId);

		if(auction == _nextAuction)
			_nextAuction = null;
		else if(auction == _currentAuction)
			_currentAuction = null;

		ItemAuctionManager.getInstance().deleteAuction(auctionId);
		return true;
	}

	public boolean deleteAuctionAndCheck(int auctionId)
	{
		if(deleteAuction(auctionId))
		{
			checkAndSetCurrentAndNextAuction();
			return true;
		}
		return false;
	}

	public ItemAuction[] getAuctionsByBidder(int bidderObjId)
	{
		ItemAuction[] auctions = getAuctions();
		List<ItemAuction> stack = new ArrayList<ItemAuction>(auctions.length);
		for(ItemAuction auction : getAuctions())
		{
			if(auction.getAuctionState() != ItemAuctionState.CREATED)
			{
				ItemAuctionBid bid = auction.getBidFor(bidderObjId);
				if(bid != null)
					stack.add(auction);
			}
		}
		return stack.toArray(new ItemAuction[stack.size()]);
	}

	public ItemAuction[] getAuctions()
	{
		synchronized (_auctions)
		{
			return _auctions.values(new ItemAuction[_auctions.size()]);
		}
	}

	private class ScheduleAuctionTask extends RunnableImpl
	{
		private ItemAuction _auction;

		public ScheduleAuctionTask(ItemAuction auction)
		{
			_auction = auction;
		}

		public void runImpl() throws Exception
		{
			ItemAuctionState state = _auction.getAuctionState();

			switch(state)
			{
				case CREATED:
				{
					if(!_auction.setAuctionState(state, ItemAuctionState.STARTED))
						throw new IllegalStateException("Could not set auction state: " + ItemAuctionState.STARTED.toString() + ", expected: " + state.toString());

					_log.info("ItemAuction: Auction " + _auction.getAuctionId() + " has started for instance " + _auction.getInstanceId());

					if(Config.ALT_ITEM_AUCTION_START_ANNOUNCE)
					{
						String[] params = {};
						Announcements.getInstance().announceByCustomMessage("l2s.gameserver.model.instances.L2ItemAuctionBrokerInstance.announce." + _auction.getInstanceId(), params);
					}
					checkAndSetCurrentAndNextAuction();
					break;
				}

				case STARTED:
				{
					switch(_auction.getAuctionEndingExtendState())
					{
						case 1:
						{
							if(_auction.getScheduledAuctionEndingExtendState() == 0)
							{
								_auction.setScheduledAuctionEndingExtendState(1);
								setStateTask(ThreadPoolManager.getInstance().schedule(this, Math.max(_auction.getEndingTime() - System.currentTimeMillis(), 0L)));
								return;
							}
							break;
						}

						case 2:
						{
							if(_auction.getScheduledAuctionEndingExtendState() != 2)
							{
								_auction.setScheduledAuctionEndingExtendState(2);
								setStateTask(ThreadPoolManager.getInstance().schedule(this, Math.max(_auction.getEndingTime() - System.currentTimeMillis(), 0L)));
								return;
							}
							break;
						}
					}

					if(!_auction.setAuctionState(state, ItemAuctionState.FINISHED))
						throw new IllegalStateException("Could not set auction state: " + ItemAuctionState.FINISHED.toString() + ", expected: " + state.toString());

					onAuctionFinished(_auction);
					checkAndSetCurrentAndNextAuction();
					break;
				}

				default:
					throw new IllegalStateException("Invalid state: " + state);
			}
		}
	}

	void onAuctionFinished(ItemAuction auction)
	{
		auction.broadcastToAllBidders(new SystemMessage(SystemMessage.S1_S_AUCTION_HAS_ENDED).addNumber(auction.getAuctionId()));
		ItemAuctionBid bid = auction.getHighestBid();
		if(bid != null)
		{
			ItemInstance item = auction.createNewItemInstance();
			Player player = bid.getPlayer();
			if(player != null)
			{
				player.getWarehouse().addItem(item);
				player.sendPacket(SystemMsg.YOU_HAVE_BID_THE_HIGHEST_PRICE_AND_HAVE_WON_THE_ITEM);

				_log.info("ItemAuction: Auction " + auction.getAuctionId() + " has finished. Highest bid by (name) " + player.getName() + " for instance " + _instanceId);
			}
			else
			{
				//TODO [G1ta0] отправлять почтой
				item.setOwnerId(bid.getCharId());
				item.setLocation(ItemLocation.WAREHOUSE);
				if(item.getJdbcState().isSavable())
				{
					item.save();
				}
				else
				{
					item.setJdbcState(JdbcEntityState.UPDATED);
					item.update();
				}

				_log.info("ItemAuction: Auction " + auction.getAuctionId() + " has finished. Highest bid by (id) " + bid.getCharId() + " for instance " + _instanceId);
			}
		}
		else
			_log.info("ItemAuction: Auction " + auction.getAuctionId() + " has finished. There have not been any bid for instance " + _instanceId);
	}

	void setStateTask(ScheduledFuture<?> future)
	{
		ScheduledFuture<?> stateTask = _stateTask;
		if(stateTask != null)
			stateTask.cancel(false);

		_stateTask = future;
	}

	private ItemAuction createAuction(long after)
	{
		AuctionItem auctionItem = _items.get(Rnd.get(_items.size()));
		long startingTime = _dateTime.next(after);
		long endingTime = startingTime + TimeUnit.MILLISECONDS.convert(auctionItem.getAuctionLength(), TimeUnit.MINUTES);
		int auctionId = ItemAuctionManager.getInstance().getNextId();
		ItemAuction auction = new ItemAuction(auctionId, _instanceId, startingTime, endingTime, auctionItem, ItemAuctionState.CREATED);

		auction.store();

		return auction;
	}

	private ItemAuction loadAuction(int auctionId) throws SQLException
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT auctionItemId,startingTime,endingTime,auctionStateId FROM item_auction WHERE auctionId=?");
			statement.setInt(1, auctionId);
			rset = statement.executeQuery();

			if(!rset.next())
			{
				_log.warn("ItemAuction: Auction data not found for auction: " + auctionId);
				return null;
			}

			int auctionItemId = rset.getInt(1);
			long startingTime = rset.getLong(2);
			long endingTime = rset.getLong(3);
			int auctionStateId = rset.getInt(4);

			DbUtils.close(statement, rset);

			if(startingTime >= endingTime)
			{
				_log.warn("ItemAuction: Invalid starting/ending paramaters for auction: " + auctionId);
				return null;
			}

			AuctionItem auctionItem = getAuctionItem(auctionItemId);
			if(auctionItem == null)
			{
				_log.warn("ItemAuction: AuctionItem: " + auctionItemId + ", not found for auction: " + auctionId);
				return null;
			}

			ItemAuctionState auctionState = ItemAuctionState.stateForStateId(auctionStateId);
			if(auctionState == null)
			{
				_log.warn("ItemAuction: Invalid auctionStateId: " + auctionStateId + ", for auction: " + auctionId);
				return null;
			}

			ItemAuction auction = new ItemAuction(auctionId, _instanceId, startingTime, endingTime, auctionItem, auctionState);

			statement = con.prepareStatement("SELECT playerObjId,playerBid FROM item_auction_bid WHERE auctionId=?");
			statement.setInt(1, auctionId);
			rset = statement.executeQuery();

			while(rset.next())
			{
				int charId = rset.getInt(1);
				long playerBid = rset.getLong(2);
				ItemAuctionBid bid = new ItemAuctionBid(charId, playerBid);
				auction.addBid(bid);
			}

			return auction;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
}
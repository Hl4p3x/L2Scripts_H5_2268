package l2s.gameserver.instancemanager.itemauction;

import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author n0nam3
 */
public final class AuctionItem extends ItemInfo
{
	private final int _auctionItemId;
	private final int _auctionLength;
	private final int _actionBidItemId;
	private final long _auctionInitBid;

	public AuctionItem(final int auctionItemId, final int auctionLength, final int actionBidItemId, final long auctionInitBid, final int itemId, final long itemCount)
	{
		_auctionItemId = auctionItemId;
		_auctionLength = auctionLength;
		_actionBidItemId = actionBidItemId;
		_auctionInitBid = auctionInitBid;

		setObjectId(itemId);
		setItemId(itemId);
		setCount(itemCount);
	}

	public AuctionItem(final int auctionItemId, final int auctionLength, final int actionBidItemId, final long auctionInitBid, final int itemId, final long itemCount, final StatsSet itemExtra)
	{
		this(auctionItemId, auctionLength, actionBidItemId, auctionInitBid, itemId, itemCount);

		setEnchantLevel(itemExtra.getInteger("enchant_level", 0));
		setVariationStoneId(itemExtra.getInteger("variation_stone_id", 0));
		setVariation1Id(itemExtra.getInteger("variation1_id", 0));
		setVariation2Id(itemExtra.getInteger("variation2_id", 0));
	}

	public final int getAuctionItemId()
	{
		return _auctionItemId;
	}

	public final int getAuctionLength()
	{
		return _auctionLength;
	}

	public final int getActionBidItemId()
	{
		return _actionBidItemId;
	}

	public final long getAuctionInitBid()
	{
		return _auctionInitBid;
	}

	public final ItemInstance createNewItemInstance()
	{
		final ItemInstance item = ItemFunctions.createItem(getItemId());
		item.setEnchantLevel(getEnchantLevel());
		if(getVariationStoneId() != 0)
			item.setVariationStoneId(getVariationStoneId());
		if(getVariation1Id() != 0)
			item.setVariation1Id(getVariation1Id());
		if(getVariation2Id() != 0)
			item.setVariation2Id(getVariation2Id());

		return item;
	}
}
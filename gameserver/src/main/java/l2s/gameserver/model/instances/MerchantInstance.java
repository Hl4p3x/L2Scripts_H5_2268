package l2s.gameserver.model.instances;

import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.BuyListHolder;
import l2s.gameserver.data.xml.holder.BuyListHolder.NpcTradeList;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.network.l2.s2c.ExBuySellListPacket;
import l2s.gameserver.network.l2.s2c.ExGetPremiumItemListPacket;
import l2s.gameserver.network.l2.s2c.ShopPreviewListPacket;
import l2s.gameserver.templates.npc.NpcTemplate;

public class MerchantInstance extends NpcInstance
{
	private static final Logger _log = LoggerFactory.getLogger(MerchantInstance.class);

	public MerchantInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		String pom;
		if(val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;

		if(getTemplate().getHtmRoot() != null)
			return getTemplate().getHtmRoot() + pom + ".htm";

		String temp = "merchant/" + pom + ".htm";
		if(HtmCache.getInstance().getIfExists(temp, player) != null)
			return temp;

		temp = "teleporter/" + pom + ".htm";
		if(HtmCache.getInstance().getIfExists(temp, player) != null)
			return temp;

		temp = "petmanager/" + pom + ".htm";
		if(HtmCache.getInstance().getIfExists(temp, player) != null)
			return temp;

		return "default/" + pom + ".htm";
	}

	private void showWearWindow(Player player, int val)
	{
		if(!player.getPlayerAccess().UseShop)
			return;

		NpcTradeList list = BuyListHolder.getInstance().getBuyList(val);

		if(list != null)
		{
			ShopPreviewListPacket bl = new ShopPreviewListPacket(list, player);
			player.sendPacket(bl);
		}
		else
		{
			_log.warn("no buylist with id:" + val);
			player.sendActionFailed();
		}
	}

	protected void showShopWindow(Player player, int listId, boolean tax)
	{
		if(!player.getPlayerAccess().UseShop)
			return;

		double taxRate = 0;

		if(tax)
		{
			Castle castle = getCastle(player);
			if(castle != null)
				taxRate = castle.getTaxRate();
		}

		NpcTradeList list = BuyListHolder.getInstance().getBuyList(listId);
		if(list == null || list.getNpcId() == getNpcId())
			player.sendPacket(new ExBuySellListPacket.BuyList(list, player, taxRate), new ExBuySellListPacket.SellRefundList(player, false));
		else
		{
			_log.warn("[L2MerchantInstance] possible client hacker: " + player.getName() + " attempting to buy from GM shop! < Ban him!");
			_log.warn("buylist id:" + listId + " / list_npc = " + list.getNpcId() + " / npc = " + getNpcId());
		}
	}

	protected void showShopWindow(Player player)
	{
		showShopWindow(player, 0, false);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		StringTokenizer st = new StringTokenizer(command, " ");
		if (!st.hasMoreTokens())
			return;
		
		String actualCommand = st.nextToken(); // Get actual command

		if(actualCommand.equalsIgnoreCase("Buy") || actualCommand.equalsIgnoreCase("Sell"))
		{
			int val = 0;
			if(st.countTokens() > 0)
				val = Integer.parseInt(st.nextToken());
			showShopWindow(player, val, true);
		}
		else if(actualCommand.equalsIgnoreCase("Wear"))
		{
			if(st.countTokens() < 1)
				return;
			int val = Integer.parseInt(st.nextToken());
			showWearWindow(player, val);
		}
		else if(actualCommand.equalsIgnoreCase("Multisell"))
		{
			if(st.countTokens() < 1)
				return;
			int val = Integer.parseInt(st.nextToken());
			Castle castle = getCastle(player);
			MultiSellHolder.getInstance().SeparateAndSend(val, player, castle != null ? castle.getTaxRate() : 0);
		}
		else if(actualCommand.equalsIgnoreCase("ReceivePremium"))
		{
			if(player.getPremiumItemList().isEmpty())
			{
				player.sendPacket(Msg.THERE_ARE_NO_MORE_VITAMIN_ITEMS_TO_BE_FOUND);
				return;
			}

			player.sendPacket(new ExGetPremiumItemListPacket(player));
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public boolean isMerchantNpc()
	{
		return true;
	}
}
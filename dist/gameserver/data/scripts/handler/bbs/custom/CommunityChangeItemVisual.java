package handler.bbs.custom;

import java.util.StringTokenizer;

import l2s.commons.dao.JdbcEntityState;
import l2s.commons.map.hash.TIntStringHashMap;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.ShowBoardPacket;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Util;

/**
 * @author Bonux
**/
public class CommunityChangeItemVisual extends CustomCommunityHandler
{
	@Override
	public String[] getBypassCommands()
	{
		return new String[]
		{
			"_cbbschangevisual"
		};
	}

	@Override
	protected void doBypassCommand(final Player player, String bypass)
	{
		StringTokenizer st = new StringTokenizer(bypass, "_");
		String cmd = st.nextToken();
		String html = "";

		if("cbbschangevisual".equals(cmd))
		{
			final int feeItemId = BBSConfig.CHANGE_VISUAL_ITEM_SERVICE_COST_ITEM_ID;
			final long feeItemCount = BBSConfig.CHANGE_VISUAL_ITEM_SERVICE_COST_ITEM_COUNT;
			final int[] itemList = BBSConfig.CHANGE_VISUAL_ITEM_SERVICE_VISUAL_LIST;
			if(feeItemId == 0 || itemList.length == 0)
			{
				player.sendMessage(player.isLangRus() ? "Данный сервис отключен." : "This service disallowed.");
				player.sendPacket(ShowBoardPacket.CLOSE);
				return;
			}

			TIntStringHashMap tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/change_visual_armor.htm", player);
			html = tpls.get(0);

			StringBuilder content = new StringBuilder();
			if(!st.hasMoreTokens())
			{
				if(feeItemCount > 0)
				{
					String feeBlock = tpls.get(1);
					feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
					feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

					content.append(feeBlock);
				}
				else
					content.append(tpls.get(2));

				StringBuilder rows = new StringBuilder();
				StringBuilder cols = new StringBuilder();
				int count = -1;
				for(int i = 0; i < itemList.length; i++)
				{
					ItemTemplate visualItemTemplate = ItemHolder.getInstance().getTemplate(itemList[i]);
					if(visualItemTemplate == null)
						continue;

					count++;
					String colBlock = tpls.get(5);
					colBlock = colBlock.replace("<?visual_item_id?>", String.valueOf(itemList[i]));
					colBlock = colBlock.replace("<?visual_item_name?>", HtmlUtils.htmlItemName(itemList[i]));
					colBlock = colBlock.replace("<?icon?>", visualItemTemplate.getIcon());
					cols.append(colBlock);

					if(((count + 1) % 3) == 0 || count == (itemList.length - 1))
					{
						rows.append(tpls.get(4).replace("<?cols?>", cols.toString()));
						cols = new StringBuilder();
					}
				}
				content.append(tpls.get(3).replace("<?rows?>", rows.toString()));
			}
			else
			{
				if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
				{
					onWrongCondition(player);
					return;
				}

				int visualItemId = -1;

				int id = Integer.parseInt(st.nextToken());
				if(id != 0)
				{
					for(int itemId : itemList)
					{
						if(itemId == id)
						{
							visualItemId = id;
							break;
						}
					}
				}
				else
					visualItemId = 0;

				if(visualItemId == -1)
					return;

				if(visualItemId > 0)
				{
					ItemTemplate visualItemTemplate = ItemHolder.getInstance().getTemplate(visualItemId);
					if(visualItemTemplate == null)
						return;
				}

				ItemInstance item = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
				if(item == null)
				{
					content.append(tpls.get(7));
				}
				else if(visualItemId == 0 && item.getVisualId() == 0)
				{
					content.append(tpls.get(9));
				}
				else if(visualItemId == item.getVisualId())
				{
					content.append(tpls.get(8));
				}
				else if(visualItemId > 0 && feeItemCount > 0 && !ItemFunctions.deleteItem(player, feeItemId, feeItemCount, true))
				{
					String noHaveItemBlock = tpls.get(6);
					noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
					noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

					content.append(noHaveItemBlock);
				}
				else
				{
					String successBlock = tpls.get(visualItemId == 0 ? 11 : 10);
					successBlock = successBlock.replace("<?chest_item_name?>", HtmlUtils.htmlItemName(item.getItemId()));
					successBlock = successBlock.replace("<?visual_item_name?>", HtmlUtils.htmlItemName(visualItemId));

					content.append(successBlock);

					item.setVisualId(visualItemId);
					item.setJdbcState(JdbcEntityState.UPDATED);
					item.update();

					player.broadcastPacket(new MagicSkillUse(player, player, 23128, 1, 1, 0));
					player.broadcastUserInfo(true);
					ThreadPoolManager.getInstance().schedule(() -> player.broadcastUserInfo(true), 500); // Костыль. По какой-то причине, после первой посылки перс становиться голый.
				}
			}
			html = html.replace("<?content?>", content.toString());
		}
		ShowBoardPacket.separateAndSend(html, player);
	}

	@Override
	protected void doWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		//
	}

	private static String doubleToString(double value)
	{
		int intValue = (int) value;
		if(intValue == value)
			return String.valueOf(intValue);
		return String.valueOf(value);
	}
}
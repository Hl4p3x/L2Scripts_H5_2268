package handler.bbs.custom;

import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.map.hash.TIntStringHashMap;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.base.Sex;
import l2s.gameserver.network.l2.s2c.ShowBoardPacket;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.ItemFunctions;

//By Evil_dnk

public class CommunitySalon extends CustomCommunityHandler
{
	private static final Logger _log = LoggerFactory.getLogger(CommunitySalon.class);

	@Override
	public String[] getBypassCommands()
	{
		return new String[]
		{
			"_cbbssalon"
		};
	}

	@Override
	protected void doBypassCommand(Player player, String bypass)
	{
		StringTokenizer st = new StringTokenizer(bypass, "_");
		String cmd = st.nextToken();
		String html = "";
		TIntStringHashMap tpls = null;

		if("cbbssalon".equals(cmd))
		{
			if(!BBSConfig.SALON_SERVICE_ACTIVE)
			{
				player.sendMessage(player.isLangRus() ? "Данный сервис отключен." : "This service disallowed.");
				player.sendPacket(ShowBoardPacket.CLOSE);
				return;
			}

			if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
			{
				onWrongCondition(player);
				return;
			}

			String cmd2 = st.nextToken();

			if("class".equals(cmd2))
			{

				if(player.getRace() == Race.HUMAN)
				{
					if (!player.isMageClass())
					{
						if (player.getSex() == Sex.MALE.ordinal())
							tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/human_f_m.htm", player);
						else
							tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/human_f_f.htm", player);
					}
					else
					{
						if (player.getSex() == Sex.MALE.ordinal())
							tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/human_m_m.htm", player);
						else
							tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/human_m_f.htm", player);
					}
				}

				else if(player.getRace() == Race.ELF)
				{
					if (player.getSex() == Sex.MALE.ordinal())
						tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/elf_m.htm", player);
					else
						tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/elf_f.htm", player);
				}

				else if(player.getRace() == Race.DARKELF)
				{
					if (player.getSex() == Sex.MALE.ordinal())
						tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/delf_m.htm", player);
					else
						tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/delf_f.htm", player);
				}

				else if(player.getRace() == Race.ORC)
				{
					if (!player.isMageClass())
					{
						if (player.getSex() == Sex.MALE.ordinal())
							tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/orc_f_m.htm", player);
						else
							tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/orc_f_f.htm", player);
					}
					else
					{
						if (player.getSex() == Sex.MALE.ordinal())
							tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/orc_m_m.htm", player);
						else
							tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/orc_m_f.htm", player);
					}
				}

				else if(player.getRace() == Race.DWARF)
				{
					if (player.getSex() == Sex.MALE.ordinal())
						tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/dwarf_m.htm", player);
					else
						tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/dwarf_f.htm", player);
				}

				else if(player.getRace() == Race.KAMAEL)
				{
					if (player.getSex() == Sex.MALE.ordinal())
						tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/kamael_m.htm", player);
					else
						tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/kamael_f.htm", player);
				}

				html = tpls.get(0);

				if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
				{
					onWrongCondition(player);
					return;
				}
				ShowBoardPacket.separateAndSend(html, player);
			}

			else if("setf".equals(cmd2))
			{
				String cmd3 = st.nextToken();
				if(Integer.parseInt(cmd3) == player.getFace())
				{
					player.sendMessage(player.isLangRus() ? "У Вас сейчас это и установлено." : "Already have it.");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				if(player.getInventory().getCountOf(BBSConfig.SALON_SERVICE_COST_ITEM_ID) < BBSConfig.SALON_SERVICE_COST_ITEM_COUNT)
				{
					player.sendMessage(player.isLangRus() ? "Не хватает коинов." : "Out of coins.");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				if(Integer.parseInt(cmd3) >= 3 && Integer.parseInt(cmd3) <= 7)
				{
					if(ItemFunctions.deleteItem(player, BBSConfig.SALON_SERVICE_COST_ITEM_ID, BBSConfig.SALON_SERVICE_COST_ITEM_COUNT))
					{
						player.setFace(Integer.parseInt(cmd3));
						player.broadcastUserInfo(true);
					}
				}
				tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/services.htm", player);
				html = tpls.get(0);
			}
			else if("seth".equals(cmd2))
			{
				String cmd3 = st.nextToken();
				if(Integer.parseInt(cmd3) == player.getHairStyle())
				{
					player.sendMessage(player.isLangRus() ? "У Вас сейчас это и установлено." : "Already have it.");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				if(player.getInventory().getCountOf(BBSConfig.SALON_SERVICE_COST_ITEM_ID) < BBSConfig.SALON_SERVICE_COST_ITEM_COUNT)
				{
					player.sendMessage(player.isLangRus() ? "Не хватает коинов." : "Out of coins.");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				if(Integer.parseInt(cmd3) >= 7 && Integer.parseInt(cmd3) <= 14)
				{
					if(ItemFunctions.deleteItem(player, BBSConfig.SALON_SERVICE_COST_ITEM_ID, BBSConfig.SALON_SERVICE_COST_ITEM_COUNT))
					{
						player.setHairStyle(Integer.parseInt(cmd3));
						player.setHairColor(0);
						player.broadcastUserInfo(true);
					}
				}
				tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/services.htm", player);
				html = tpls.get(0);
			}
		}

		html = html.replace("%service_cost%", ""+BBSConfig.SALON_SERVICE_COST_ITEM_COUNT);
		html = html.replace("%service_item_id%", HtmlUtils.htmlItemName(BBSConfig.SALON_SERVICE_COST_ITEM_ID));
		ShowBoardPacket.separateAndSend(html, player);
	}

	@Override
	protected void doWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		//
	}

}
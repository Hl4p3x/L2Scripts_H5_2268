package handler.bbs.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import handler.bbs.custom.communitybuffer.BuffSet;
import handler.bbs.custom.communitybuffer.BuffSkill;
import handler.bbs.custom.communitybuffer.CommunityBufferDAO;
import handler.bbs.custom.communitybuffer.CommunityBufferHolder;
import handler.bbs.custom.communitybuffer.CommunityBufferParser;
import l2s.commons.lang.ArrayUtils;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.model.Effect;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill.SkillType;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.ShowBoardPacket;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.SkillUtils;
import l2s.gameserver.utils.Strings;
import l2s.gameserver.utils.Util;

public class CommunityBuffer extends CustomCommunityHandler
{
	@Override
	public void onLoad()
	{
		CommunityBufferParser.getInstance().load();
		super.onLoad();
	}

	@Override
	public void onReload()
	{
		CommunityBufferParser.getInstance().reload();
		super.onReload();
	}

	@Override
	public String[] getBypassCommands()
	{
		return new String[] { "_cbbsbuffer", "_bbsrestore", "_bbscancel"};
	}

	@Override
	protected void doBypassCommand(Player player, String bypass)
	{
		if(BBSConfig.BUFF_SERVICE_COST_ITEM_ID == 0 && !BBSConfig.BUFF_SERVICE_ALLOW_RESTORE && !BBSConfig.BUFF_SERVICE_ALLOW_CANCEL_BUFFS || CommunityBufferHolder.getInstance().getAvailableSkills().isEmpty())
		{
			player.sendMessage(player.isLangRus() ? "Данный сервис отключен." : "This service disallowed.");
			player.sendPacket(ShowBoardPacket.CLOSE);
			return;
		}

		if(CommunityBufferHolder.getInstance().getAvailableSkills(player).isEmpty())
		{
			player.sendMessage(player.isLangRus() ? "Вам недоступен данный сервис." : "This service is not available to you.");
			player.sendPacket(ShowBoardPacket.CLOSE);
			return;
		}

		String html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/buff.htm", player);
		String content = "";
		
		if(bypass.startsWith("_bbsrestore"))
		{
			if(!BBSConfig.BUFF_SERVICE_ALLOW_RESTORE)
			{
				player.sendMessage(player.isLangRus() ? "Данный сервис отключен." : "This service disallowed.");
				player.sendPacket(ShowBoardPacket.CLOSE);
				return;
			}

			if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && (!checkUseCondition(player) || player.isInCombat()))
			{
				onWrongCondition(player);
				return;
			}

			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
			player.broadcastPacket(new MagicSkillUse(player, player, 23128, 1, 1, 0));
			onBypassCommand(player, "_cbbsbuffer 0");
			return;
		}
		if(bypass.startsWith("_bbscancel"))
		{
			if(!BBSConfig.BUFF_SERVICE_ALLOW_CANCEL_BUFFS)
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

			for(Effect eff : player.getEffectList().getAllEffects())
			{
				if(!eff.isOffensive() && (BBSConfig.BUFF_SERVICE_CAN_CANCEL_NOT_DISPELLABLE_BUFFS || eff.getSkill().isSelfDispellable()) && eff.getSkill().getSkillType() != SkillType.TRANSFORMATION)
					eff.exit();
			}

			player.broadcastPacket(new MagicSkillUse(player, player, 23128, 1, 1, 0));
			onBypassCommand(player, "_cbbsbuffer 0");
			return;			
		}		
		
		if(bypass.startsWith("_cbbsbuffer"))
		{
			if(BBSConfig.BUFF_SERVICE_COST_ITEM_ID == 0)
			{
				player.sendMessage(player.isLangRus() ? "Данный сервис отключен." : "This service disallowed.");
				player.sendPacket(ShowBoardPacket.CLOSE);
				return;
			}

			if(player.isInOlympiadMode())
			{
				player.sendMessage(player.isLangRus() ? "Эта функция недоступна на олимпиаде." : "This feature is not available at the Olympiad Game.");
				player.sendPacket(ShowBoardPacket.CLOSE);
				return;
			}

			if(player.isInCombat())
			{
				player.sendMessage(player.isLangRus() ? "Эта функция недоступна во время боя." : "This feature is not available during the battle.");
				player.sendPacket(ShowBoardPacket.CLOSE);
				return;
			}

			StringTokenizer bf = new StringTokenizer(bypass, " ");
			bf.nextToken();
			String[] arg = new String[0];
			while(bf.hasMoreTokens())
				arg = ArrayUtils.add(arg, bf.nextToken());

			content = BuffList(arg, player);
		}

		if(content == null)
			return;

		html = html.replace("%content%", content);

		long price = 0L;
		if(player.getLevel() > BBSConfig.BUFF_SERVICE_MAX_LEVEL_FOR_FREE_BUFF)
		{
			if(BBSConfig.BUFF_SERVICE_COST_ITEM_COUNT > 0)
				price = BBSConfig.BUFF_SERVICE_COST_ITEM_COUNT;
		}
		html = html.replace("<?price?>", price > 0 ? Util.formatAdena(price) : "");
		html = html.replace("<?fee_item?>", price > 0 ? HtmlUtils.htmlItemName(BBSConfig.BUFF_SERVICE_COST_ITEM_ID) : (player.isLangRus() ? "Бесплатно" : "Free"));

		ShowBoardPacket.separateAndSend(html, player);
	}

	@Override
	protected void doWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		//
	}

	public static String htmlButton(String value, int width, int height, Object... args)
	{
		String action = "bypass _cbbsbuffer";
		for(Object arg : args)
			action += " " + arg.toString();
		return HtmlUtils.htmlButton(value, action, width, height);
	}

	//public static String htmlButton(String value, int width, Object... args)
	//{
	//	return htmlButton(value, width, 25, args);
	//}

	private static boolean takeItemsAndBuff(Playable player, Set<BuffSkill> buffs, boolean toPet)
	{
		long needCount = BBSConfig.BUFF_SERVICE_COST_ITEM_COUNT > 0 ? BBSConfig.BUFF_SERVICE_COST_ITEM_COUNT * buffs.size() : 0L;

		if(player.getLevel() > BBSConfig.BUFF_SERVICE_MAX_LEVEL_FOR_FREE_BUFF)
		{
			if(ItemFunctions.getItemCount(player, BBSConfig.BUFF_SERVICE_COST_ITEM_ID) < needCount)
				return false;
		}
		
		if(!toPet)
		{
			doBuff(player, buffs, needCount);
		}
		else
		{
			Servitor servitor = player.getServitor();
			if(servitor != null)
				doBuff(servitor, buffs, needCount);
		}
		return true;
	}

	private static void doBuff(Playable target, Set<BuffSkill> buffs, long price) {
		final Player player = target.getPlayer();
		if(player == null)
			return;

		if(player.getLevel() > BBSConfig.BUFF_SERVICE_MAX_LEVEL_FOR_FREE_BUFF) {
			if(!ItemFunctions.deleteItem(player, BBSConfig.BUFF_SERVICE_COST_ITEM_ID, price))
				return;
		}

		ThreadPoolManager.getInstance().execute(() ->{
			boolean success = false;
			for(BuffSkill nextbuff : buffs)
			{
				nextbuff = checkSkill(nextbuff, player);
				if(nextbuff == null)
					continue;

				nextbuff.getSkill().getEffects(target, target, false, false, nextbuff.getTimeAssign() * 60 * 1000, nextbuff.getTimeModifier(), false);

				success = true;
				try
				{
					Thread.sleep(20L);
				}
				catch(Exception e)
				{
					//
				}
			}

			if(success)
				player.broadcastPacket(new MagicSkillUse(player, player, 23128, 1, 1, 0));
		});
	}

	private String pageGet(Player player, String[] var)
	{
		if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
		{
			onWrongCondition(player);
			return null;
		}

		boolean buffallset = var[1].equalsIgnoreCase("0") || var[1].equalsIgnoreCase("2");
		String[] var2 = new String[var.length - (buffallset ? 1 : 2)];
		System.arraycopy(var, var.length - var2.length, var2, 0, var2.length);
		Set<BuffSkill> buffs_to_buff = new LinkedHashSet<>();

		if(buffallset)
		{
			String[] a = var[2].split("_");
			int ownerId = a[0].equalsIgnoreCase("2") ? player.getObjectId() : 0;
			int setId = Integer.parseInt(var[3]);
			BuffSet buffSet = getBuffSets(ownerId).get(setId);
			if(buffSet == null)
			{
				if(player.isLangRus())
					return "<table><tr><td align=\"center\"><font color=\"FF3355\">Набор 'ID: " + setId + "' не найден</font></td></tr></table>";
				else
					return "<table><tr><td align=\"center\"><font color=\"FF3355\">'ID: " + setId + "' set not found</font></td></tr></table>";
			}
			buffs_to_buff.addAll(buffSet.getBuffSkills(CommunityBufferHolder.getInstance().getAvailableSkills(player)));
		}
		else
			buffs_to_buff.add(CommunityBufferHolder.getInstance().getAvailableSkills(player).get(Integer.parseInt(var[2])));

		if(!takeItemsAndBuff(player, buffs_to_buff, var[1].equalsIgnoreCase("2")))
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);

		return pageList(player, var2);
	}

	private static final int pageRows = 6;
	private static final int pageCols = 3;
	private static final int pageMax = pageRows * pageCols;

	private static String pageList(Player player, String[] var)
	{
		String[] a = var[1].split("_");
		int pageIdx = Integer.parseInt(a[1]);
		boolean _all = a[0].equalsIgnoreCase("0");
		int ownerId = a[0].equalsIgnoreCase("2") ? player.getObjectId() : 0;
		String name = "Все баффы";
		if(!player.isLangRus())
			name = "All buffs";
		BuffSet buffSet = CommunityBufferHolder.getAvailableBuffSet(player);

		if(!_all)
		{
			int setId = Integer.parseInt(var[2]);
			buffSet = getBuffSets(ownerId).get(setId);
			if(buffSet == null)
			{
				if(player.isLangRus())
					return "<table><tr><td align=\"center\"><font color=\"FF3355\">Набор 'ID: " + setId + "' не найден</font></td></tr></table>";
				else
					return "<table><tr><td align=\"center\"><font color=\"FF3355\">'ID: " + setId + "' set not found</font></td></tr></table>";
			}
			name = buffSet.getName(player.getLanguage());
		}

		String pagePrev = pageIdx == 0 ? "" : htmlButton("&$543;", 80, 25, "list", a[0] + "_" + (pageIdx - 1), buffSet.getId());
		String pageNext = "";
		List<String> tds = new ArrayList<String>();

		List<BuffSkill> buffSetSkills = buffSet.getBuffSkills(CommunityBufferHolder.getInstance().getAvailableSkills(player));
		for(int i = pageIdx * pageMax; i < buffSetSkills.size(); )
		{
			if(tds.size() == pageMax)
			{
				pageNext = htmlButton("&$544;", 80, 25, "list", a[0] + "_" + (pageIdx + 1), buffSet.getId());
				break;
			}

			BuffSkill buffSkill = buffSetSkills.get(i);
			if(buffSkill == null)
				continue;

			String buff_str = "<td FIXWIDTH=\"230\" height=\"40\"><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td FIXWIDTH=\"5\">&nbsp;</td>";
			buff_str += "<td FIXWIDTH=\"35\" valign=\"top\">" + makeIconBlock(buffSkill, "get", 1, buffSkill.getId(), var[1], buffSet.getId()) + "<br></td>";
			buff_str += "<td FIXWIDTH=\"30\">" + htmlButton("$", 25, 32, "get", 1, buffSkill.getId(), var[1], buffSet.getId()) + "</td>";
			if(player.isLangRus())
				buff_str += "<td fixwidth=\"165\"><table width=\"160\" border=\"0\"><tr><td width=\"160\">" + makeNameBlock(player, buffSkill) + "</td></tr><tr><td width=\"160\"><font color=\"LEVEL\">Уровень " + buffSkill.getSkill().getBaseLevel() + "</font></td></tr></table></td>";
			else
				buff_str += "<td fixwidth=\"165\"><table width=\"160\" border=\"0\"><tr><td width=\"160\">" + makeNameBlock(player, buffSkill) + "</td></tr><tr><td width=\"160\"><font color=\"LEVEL\">Level " + buffSkill.getSkill().getBaseLevel() + "</font></td></tr></table></td>";
			buff_str += "</tr></table></td>";
			tds.add(buff_str);
			i++;
		}

		StringBuilder result = new StringBuilder();

		long cost = 0L;
		if(player.getLevel() > BBSConfig.BUFF_SERVICE_MAX_LEVEL_FOR_FREE_BUFF && BBSConfig.BUFF_SERVICE_COST_ITEM_COUNT > 0)
			cost = buffSetSkills.size() * BBSConfig.BUFF_SERVICE_COST_ITEM_COUNT;

		result.append("<table width=\"650\"><tr>");
		String all = "All";
		if(player.isLangRus())
			all = "Все";

		result.append("<td width=\"600\" align=\"center\"><font color=\"DDD3B6\">");
		result.append(name);
		result.append("</font></td>");
		result.append("</tr>");
		if(!_all/* && pageNext.isEmpty() && pagePrev.isEmpty()*/)
		{
			result.append("<tr><td width=\"600\" align=\"center\">");
			if(cost > 0)
			{
				if(player.isLangRus())
					result.append("Цена за набор: ");
				else
					result.append("Set price: ");
				result.append("<font color=\"LEVEL\">");
				result.append(Util.formatAdena(cost) + " " + HtmlUtils.htmlItemName(BBSConfig.BUFF_SERVICE_COST_ITEM_ID));
				result.append("</font>");
			}
			else
			{
				result.append("<font color=\"LEVEL\">");
				if(player.isLangRus())
					result.append("Бесплатно");
				else
					result.append("Free");
				result.append("</font>");
			}
			result.append("</td></tr>");
		}
		result.append("</table>");
		if(!_all && /*pageNext.isEmpty() && pagePrev.isEmpty() && */tds.size() > 0)
		{
			result.append("<table width=\"300\"><tr><td align=\"center\">");
			if(player.isLangRus())
			{
				result.append(htmlButton("Себе", 100, 25, "get", 0, var[1], buffSet.getId()) + "</td>");
				if(player.getServitor() != null)
					result.append("<td align=\"center\">" + htmlButton("Питомцам", 100, 25, "get", 2, var[1], buffSet.getId()) + "</td>");
			}
			else
			{
				result.append(htmlButton("For Me", 100, 25, "get", 0, var[1], buffSet.getId()) + "</td>");
				if(player.getServitor() != null)
					result.append("<td align=\"center\">" + htmlButton("For Servitors", 100, 25, "get", 2, var[1], buffSet.getId()) + "</td>");
			}
			result.append("</tr></table>");
		}

		if(ownerId != 0)
		{
			if(player.isLangRus())
				result.append("<table><tr><td align=\"center\">" + htmlButton("Редактировать", 125, 25, "editset", "edit", buffSet.getId(), 0) + "</td></tr></table>");
			else
				result.append("<table><tr><td align=\"center\">" + htmlButton("Edit", 125, 25, "editset", "edit", buffSet.getId(), 0) + "</td></tr></table>");
		}

		StringBuilder pagesNavigation = new StringBuilder();
		if(!pagePrev.isEmpty() || !pageNext.isEmpty())
		{
			pagesNavigation.append("<table><tr>");
			pagesNavigation.append("<td width=\"90\" align=\"center\">" + pagePrev + "</td>");
			pagesNavigation.append("<td width=\"80\" align=\"center\">");
			if(player.isLangRus())
				pagesNavigation.append("Страница: ");
			else
				pagesNavigation.append("Page: ");
			pagesNavigation.append(pageIdx + 1);
			pagesNavigation.append("</td>");
			pagesNavigation.append("<td width=\"90\" align=\"center\">" + pageNext + "</td>");
			pagesNavigation.append("</tr></table>");
		}

		if(tds.size() > 0)
		{
			result.append("<table width=\"720\" background=\"L2UI_CH3.refinewnd_back_Pattern\">");
			result.append(formatTable(pagesNavigation, tds, pageCols, false));
			result.append("</table>");
		}

		result.append("<br><table><tr><td align=\"center\">" + htmlButton((player.isLangRus() ? "Назад" : "Back"), 125, 25, "0") + "</td></tr></table>");

		return result.toString();
	}

	private static String pageEdit(Player player, String[] var)
	{
		int ownerId = player.getObjectId();

		if(var[1].equalsIgnoreCase("del"))
		{
			//Log.add("BUFF\tУдален набор: " + name, "service_buff", player);
			CommunityBufferDAO.getInstance().deleteSet(ownerId, Integer.parseInt(var[2]));
			return pageMain(player);
		}

		Map<Integer, BuffSet> buffSets = getBuffSets(ownerId);

		BuffSet buffSet = null;
		String name = "";

		String result = "";
		List<String> tds = new ArrayList<String>();

		if(var[1].equalsIgnoreCase("delconf"))
		{
			result += "<table><tr>";
			buffSet = buffSets.get(Integer.parseInt(var[2]));
			name = buffSet.getName(player.getLanguage());

			if(player.isLangRus())
				result += "<td width=\"400\" align=\"center\"><font color=\"FF3355\">Вы действительно желаете удалить набор: " + name + "?</font></td>";
			else
				result += "<td width=\"400\" align=\"center\"><font color=\"FF3355\">Are you sure you want to delete a set: " + name + "?</font></td>";

			result += "</tr></table><table><tr>";

			if(player.isLangRus())
			{
				result += "<td>" + htmlButton("ДА", 80, 25, "editset", "del", buffSet.getId()) + "</td>";
				result += "<td>" + htmlButton("НЕТ", 80, 25, "editset", "edit", buffSet.getId(), 0) + "</td>";
			}
			else
			{
				result += "<td>" + htmlButton("YES", 80, 25, "editset", "del", buffSet.getId()) + "</td>";
				result += "<td>" + htmlButton("NO", 80, 25, "editset", "edit", buffSet.getId(), 0) + "</td>";
			}
			result += "</tr></table>";

			return result;
		}

		if(var[1].equalsIgnoreCase("new"))
		{
			if(buffSets.size() >= BBSConfig.BUFF_SERVICE_MAX_BUFF_SETS_PER_CHAR)
				return player.isLangRus() ? "<table><tr><td><font color=\"FF3355\">Вы достигли лимита наборов!</font></td></tr></table>" : "<table><tr><td><center><font color=\"FF3355\">You have reached the limit set!</font></td></tr></table>";

			name = trimHtml(Strings.joinStrings(" ", var, 2));

			if(name.length() > 16)
				name = name.substring(0, 15);
			if(name.isEmpty() || name.equalsIgnoreCase(" "))
			{
				if(player.isLangRus())
					return "<table><tr><td><font color=\"FF3355\">Необходимо указать имя набора!</font></td></tr></table>";
				else
					return "<table><tr><td><font color=\"FF3355\">You must specify the name of the set!</font></td></tr></table>";
			}

			buffSet = new BuffSet(ownerId, BuffSet.findId(buffSets), name);

			for(int skillId : CommunityBufferHolder.getInstance().getAvailableSkills(player).keySet())
			{
				if(player.getEffectList().getEffectsCountForSkill(skillId) > 0)
					buffSet.getSkills().add(skillId);
			}
			CommunityBufferDAO.getInstance().insertSet(buffSet);
			//Log.add("BUFF\tСоздан набор: " + name, "service_buff", player);
		}
		else if(var[1].equalsIgnoreCase("edit") || var[1].equalsIgnoreCase("rem") || var[1].equalsIgnoreCase("add")) {
			int setId = Integer.parseInt(var[2]);
			buffSet = buffSets.get(setId);
			if (buffSet == null) {
				if (player.isLangRus())
					return "<table><tr><td><font color=\"FF3355\">Набор 'ID: " + setId + "' не найден.</font></td></tr></table>";
				else
					return "<table><tr><td><font color=\"FF3355\">'ID: " + setId + "' set not found.</font></td></tr></table>";
			}

			name = buffSet.getName(player.getLanguage());

			if (var[1].equalsIgnoreCase("rem"))
				CommunityBufferDAO.getInstance().deleteSkillFromSet(buffSet, Integer.valueOf(var[3]));
			else if (var[1].equalsIgnoreCase("add")) {
				if (buffSet.getSkills().size() >= BBSConfig.BUFF_SERVICE_MAX_BUFFS_IN_SET)
					return pageMain(player);

				Map<Integer, BuffSkill> availableSkills = new LinkedHashMap<>(CommunityBufferHolder.getInstance().getAvailableSkills(player));
				for(int skillId : buffSet.getSkills()) {
					availableSkills.remove(skillId);
				}

				final int addSkillId = Integer.valueOf(var[4]);
				if (availableSkills.remove(addSkillId) != null)
					CommunityBufferDAO.getInstance().insertSkillToSet(buffSet, addSkillId);

				final List<BuffSkill> availableBuffs = new ArrayList<>(availableSkills.values());

				if (!availableBuffs.isEmpty()) {
					int pageIdx = Integer.valueOf(var[3]);
					int minSkillIndex = pageIdx * pageMax;
					while (pageIdx > 0 && minSkillIndex >= availableBuffs.size()) {
						pageIdx--;
						minSkillIndex = pageIdx * pageMax;
					}

					int maxSkillIndex = Math.min(minSkillIndex + pageMax, availableBuffs.size());

					String pagePrev = pageIdx == 0 ? "" : htmlButton("&$543;", 80, 25, "editset", "add", buffSet.getId(), pageIdx - 1, 0);
					String pageNext = "";
					for (int i = minSkillIndex; i < maxSkillIndex; ) {
						BuffSkill buffSkill = availableBuffs.get(i);
						if (buffSkill == null)
							continue;

						String buff_str = "<td FIXWIDTH=\"5\">&nbsp;</td>";
						buff_str = "<td FIXWIDTH=\"35\" valign=\"top\">" + makeIconBlock(buffSkill, "editset", "add", buffSet.getId(), pageIdx, buffSkill.getId()) + "<br></td>";
						buff_str += "<td FIXWIDTH=\"30\">" + htmlButton(">", 22, 32, "editset", "add", buffSet.getId(), pageIdx, buffSkill.getId()) + "</td>";
						if (player.isLangRus())
							buff_str += "<td fixwidth=\"165\"><table width=\"160\" border=\"0\"><tr><td width=\"160\">" + makeNameBlock(player, buffSkill) + "</td></tr><tr><td width=\"160\"><font color=\"LEVEL\">Уровень " + buffSkill.getSkill().getLevelWithoutEnchant() + "</font></td></tr></table></td>";
						else
							buff_str += "<td fixwidth=\"165\"><table width=\"160\" border=\"0\"><tr><td width=\"160\">" + makeNameBlock(player, buffSkill) + "</td></tr><tr><td width=\"160\"><font color=\"LEVEL\">Level " + buffSkill.getSkill().getLevelWithoutEnchant() + "</font></td></tr></table></td>";
						tds.add(buff_str);
						i++;
					}

					if (availableBuffs.size() > maxSkillIndex)
						pageNext = htmlButton("&$544;", 80, 25, "editset", "add", buffSet.getId(), pageIdx + 1, 0);

					result += "<table><tr>";
					if (player.isLangRus())
						result += "<td width=\"300\" align=\"center\"><font color=\"DDD3B6\">Редактирование набора: " + name + "</font></td>";
					else
						result += "<td width=\"300\" align=\"center\"><font color=\"DDD3B6\">Set editing: " + name + "</font></td>";
					result += "</tr></table>";
					StringBuilder pagesNavigation = new StringBuilder();
					if (!pagePrev.isEmpty() || !pageNext.isEmpty()) {
						pagesNavigation.append("<table><tr>");
						pagesNavigation.append("<td width=\"90\" align=\"center\">" + pagePrev + "</td>");
						pagesNavigation.append("<td width=\"80\" align=\"center\">");
						if (player.isLangRus())
							pagesNavigation.append("Страница: ");
						else
							pagesNavigation.append("Page: ");
						pagesNavigation.append(String.valueOf(pageIdx + 1));
						pagesNavigation.append("</td>");
						pagesNavigation.append("<td width=\"90\" align=\"center\">" + pageNext + "</td>");
						pagesNavigation.append("</tr></table>");
					}

					result += "<table width=\"720\" background=\"L2UI_CH3.refinewnd_back_Pattern\">" + formatTable(pagesNavigation, tds, pageCols, false) + "</table>";
					result += "<br><table><tr><td>" + htmlButton((player.isLangRus() ? "Назад" : "Back"), 125, 25, "editset", "edit", buffSet.getId(), 0) + "</td></tr></table>";
					return result;
				}
			}
		}
		else
			return pageMain(player);

		final List<BuffSkill> availableBuffs = buffSet.getBuffSkills(CommunityBufferHolder.getInstance().getAvailableSkills(player));

		int pageIdx = var.length > 3 ? Integer.valueOf(var[3]) : 1;
		int minSkillIndex = pageIdx * pageMax;
		while (pageIdx > 0 && minSkillIndex >= availableBuffs.size()) {
			pageIdx--;
			minSkillIndex = pageIdx * pageMax;
		}

		int maxSkillIndex = Math.min(minSkillIndex + pageMax, availableBuffs.size());

		String pagePrev = pageIdx == 0 ? "" : htmlButton("&$543;", 80, 25, "editset", "edit", buffSet.getId(), pageIdx - 1);
		String pageNext = "";
		for (int i = minSkillIndex; i < maxSkillIndex; ) {
			BuffSkill buffSkill = availableBuffs.get(i);
			if (buffSkill == null)
				continue;

			String buff_str = "<td FIXWIDTH=\"5\">&nbsp;</td>";
			buff_str = "<td FIXWIDTH=\"35\" valign=\"top\">" + makeIconBlock(buffSkill, "editset", "rem", buffSet.getId(), buffSkill.getId()) + "<br></td>";
			buff_str += "<td FIXWIDTH=\"30\">" + htmlButton("<", 25, 32, "editset", "rem", buffSet.getId(), buffSkill.getId()) + "</td>";
			if(player.isLangRus())
				buff_str += "<td fixwidth=\"165\"><table width=\"160\" border=\"0\"><tr><td width=\"160\">" + makeNameBlock(player, buffSkill) + "</td></tr><tr><td width=\"160\"><font color=\"LEVEL\">Уровень " + buffSkill.getSkill().getLevelWithoutEnchant() + "</font></td></tr></table></td>";
			else
				buff_str += "<td fixwidth=\"165\"><table width=\"160\" border=\"0\"><tr><td width=\"160\">" + makeNameBlock(player, buffSkill) + "</td></tr><tr><td width=\"160\"><font color=\"LEVEL\">Level " + buffSkill.getSkill().getLevelWithoutEnchant() + "</font></td></tr></table></td>";
			tds.add(buff_str);
			i++;
		}

		if (availableBuffs.size() > maxSkillIndex)
			pageNext = htmlButton("&$544;", 80, 25, "editset", "edit", buffSet.getId(), pageIdx + 1);

		StringBuilder pagesNavigation = new StringBuilder();
		if (!pagePrev.isEmpty() || !pageNext.isEmpty()) {
			pagesNavigation.append("<table><tr>");
			pagesNavigation.append("<td width=\"90\" align=\"center\">" + pagePrev + "</td>");
			pagesNavigation.append("<td width=\"80\" align=\"center\">");
			if (player.isLangRus())
				pagesNavigation.append("Страница: ");
			else
				pagesNavigation.append("Page: ");
			pagesNavigation.append(String.valueOf(pageIdx + 1));
			pagesNavigation.append("</td>");
			pagesNavigation.append("<td width=\"90\" align=\"center\">" + pageNext + "</td>");
			pagesNavigation.append("</tr></table>");
		}

		result += "<table><tr>";
		if(player.isLangRus())
		{
			result += "<td width=\"300\" align=\"center\"><font color=\"DDD3B6\">Редактирование: " + name + "</font></td></tr></table>";
			result += "<table><tr>";
			if(availableBuffs.size() < BBSConfig.BUFF_SERVICE_MAX_BUFFS_IN_SET)
				result += "<td width=\"120\" align=\"center\">" + htmlButton("Добавить бафф", 120, 25, "editset", "add", buffSet.getId(), 0, 0) + "</td>";
			result += "<td width=\"120\" align=\"center\">" + htmlButton("Удалить набор", 120, 25, "editset", "delconf", buffSet.getId()) + "</td>";
			result += "</tr></table>";
		}
		else
		{
			result += "<td width=\"300\" align=\"center\"><font color=\"DDD3B6\">Editing: " + name + "</font></td></tr></table>";
			result += "<table><tr>";
			if(availableBuffs.size() < BBSConfig.BUFF_SERVICE_MAX_BUFFS_IN_SET)
				result += "<td width=\"120\" align=\"center\">" + htmlButton("Add buff", 120, 25, "editset", "add", buffSet.getId(), 0, 0) + "</td>";
			result += "<td width=\"120\" align=\"center\">" + htmlButton("Delete set", 120, 25, "editset", "delconf", buffSet.getId()) + "</td>";
			result += "</tr></table>";
		}

		if(tds.size() > 0)
		{
			result += "<table width=\"720\" background=\"L2UI_CH3.refinewnd_back_Pattern\">" + formatTable(pagesNavigation, tds, pageCols, false) + "</table>";
		}

		result += "<br><table><tr><td>" + htmlButton((player.isLangRus() ? "Назад" : "Back"), 125, 25, "list", "2_0", buffSet.getId()) + "</td></tr></table>";

		return result;
	}

	private static String makeNameBlock(Player player, BuffSkill buffSkill) {
		StringBuilder sb = new StringBuilder();
		int enchantLevel = SkillUtils.getSkillEnchantLevel(buffSkill.getSkill().getDisplayLevel());
		if(enchantLevel > 0)
			sb.append("<font color=\"FF6A00\">+" + enchantLevel + "</font> ");
		sb.append("<font color=\"" + (!buffSkill.isPremium() ? "3399FF" : "B200FF") + "\">" + buffSkill.getSkill().getName(player) + "</font>");
		return sb.toString();
	}

	private static String makeIconBlock(BuffSkill buffSkill, Object... args) {
		String action = null;
		if(args.length > 0) {
			action = "bypass _cbbsbuffer";
			for (Object arg : args)
				action += " " + arg.toString();
		}

		StringBuilder sb = new StringBuilder();
		if(action != null) {
			sb.append("<table background=\"" + buffSkill.getSkill().getIcon() + "\" width=\"34\" height=\"34\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
			sb.append("<tr><td align=\"center\"><button value=\" \" action=\"" + action + "\" width=\"34\" height=\"34\" back=\"L2UI_CH3.inventory_outline_over\" fore=\"L2UI_CH3.inventory_outline\"></td></tr>");
			sb.append("</table>");
		}
		else {
			sb.append("<table background=\"" + buffSkill.getSkill().getIcon() + "\" width=\"32\" height=\"32\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
			sb.append("<tr><td fixwidth=\"32\" fixwidth=\"32\">&nbsp;</td></tr>");
			sb.append("</table>");
		}
		return sb.toString();
	}

	private static BuffSkill checkSkill(BuffSkill buffSkill, Player player) {
		if(buffSkill == null)
			return null;
		return CommunityBufferHolder.getInstance().getAvailableSkills(player).get(buffSkill.getId());
	}

	private static String pageMain(Player player)
	{
		StringBuilder result = new StringBuilder();
		result.append("<table><tr>");

		result.append("<td width=\"350\" align=\"center\">");

		result.append("<table width=\"32\"0 background=\"L2UI_CT1.editbox_df_bg\">");
		result.append("<tr>");
		result.append("<td align=\"center\">");
		result.append("<button value=\"");

		if(player.isLangRus())
			result.append("Исцелиться");
		else
			result.append("Heal");

		result.append("\" action=\"bypass _bbsrestore\" width=\"150\" height=\"25\" back=\"L2UI_ct1.button_df_down\" fore=\"L2UI_ct1.button_df\">");
		result.append("</td>");
		result.append("<td align=\"center\">");
		result.append("<button value=\"");

		if(player.isLangRus())
			result.append("Отменить эффекты");
		else
			result.append("Cancel effects");

		result.append("\" action=\"bypass _bbscancel\" width=\"150\" height=\"25\" back=\"L2UI_ct1.button_df_down\" fore=\"L2UI_ct1.button_df\">");
		result.append("</td>");
		result.append("</tr>");
		result.append("</table>");
		result.append("<br>");

		result.append("<table width=\"300\" border=\"0\" background=\"L2UI_CT1.editbox_df_bg\">");
		result.append("<tr>");
		result.append("<td align=\"center\" width=\"40\" height=\"38\">&nbsp;</td>");
		result.append("<td align=\"center\" width=\"220\" height=\"38\">");

		if(player.isLangRus())
			result.append(htmlButton("Все Баффы", 200, 30, "list", "0_0"));
		else
			result.append(htmlButton("All Buffs", 200, 30, "list", "0_0"));

		result.append("</td>");
		result.append("<td align=\"center\" width=\"40\" height=\"38\">&nbsp;</td>");
		result.append("</tr>");
		result.append("</table>");

		Map<Integer, BuffSet> buffSets = getBuffSets(0);
		if(!buffSets.isEmpty())
		{
			result.append("<br>");
			result.append("<table width=\"300\" border=\"0\">");
			result.append("<tr>");
			result.append("<td align=\"center\" width=\"40\">&nbsp;</td>");
			result.append("<td align=\"center\" width=\"220\">");

			if(player.isLangRus())
				result.append("Стандартные наборы");
			else
				result.append("Standart sets");

			result.append("</td>");
			result.append("<td align=\"center\" width=\"40\">&nbsp;</td>");
			result.append("</tr>");
			result.append("</table>");

			BuffSet[] buffSetsArr = buffSets.values().toArray(new BuffSet[buffSets.size()]);
			Arrays.sort(buffSetsArr, (s1, s2) -> {
				return Integer.compare(s1.getId(), s2.getId());
			});
			for(BuffSet buffSet : buffSetsArr)
			{
				Map<Integer, BuffSkill> availableSkills = CommunityBufferHolder.getInstance().getAvailableSkills();
				if(availableSkills.isEmpty())
					continue;

				List<BuffSkill> buffSkills = buffSet.getBuffSkills(availableSkills);
				if(buffSkills.isEmpty())
					continue;

				String icon = makeIconBlock(buffSkills.get(0));

				result.append("<br1>");
				result.append("<table width=\"300\" border=\"0\" background=\"L2UI_CT1.editbox_df_bg\">");
				result.append("<tr>");
				result.append("<td align=\"center\" width=\"40\" height=\"38\">");
				result.append(icon);
				result.append("</td>");
				result.append("<td align=\"center\" width=\"220\" height=\"38\">");
				result.append(htmlButton(buffSet.getName(player.getLanguage()), 200, 30, "list", "1_0", buffSet.getId()));
				result.append("</td>");
				result.append("<td align=\"center\" width=\"40\" height=\"38\">");
				result.append(icon);
				result.append("</td>");
				result.append("</tr>");
				result.append("</table>");
			}
		}

		result.append("</td>");

		result.append("<td width=\"350\" align=\"center\">");
		result.append("<table width=\"300\" background=\"L2UI_CH3.refinewnd_back_Pattern\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
		result.append("<tr>");
		result.append("<td align=\"center\">");
		if(player.isLangRus())
			result.append("<br><br><font color=\"DDD3B6\">Собственные наборы</font><br>");
		else
			result.append("<br><br><font color=\"DDD3B6\">Own sets</font><br>");
		result.append("<table>");

		result.append("<tr><td align=\"center\"><edit var=\"newfname\" width=\"200\"><br></td></tr>");
		result.append("<tr><td align=\"center\">");

		if(player.isLangRus())
			result.append(htmlButton("Сохранить", 100, 25, "editset", "new", "$newfname"));
		else
			result.append(htmlButton("Save", 100, 25, "editset", "new", "$newfname"));

		result.append("</td></tr>");
		result.append("</table><br>");

		buffSets = getBuffSets(player.getObjectId());
		if(!buffSets.isEmpty())
		{

			result.append("<table>");
			result.append("<tr><td width=\"288\"><table width=\"285\" border=\"0\" bgcolor=\"3D3D3D\"><tr><td width=\"260\" align=\"center\"></td></tr></table></td></tr>");
			result.append("<tr><td width=\"288\">");


			BuffSet[] buffSetsArr = buffSets.values().toArray(new BuffSet[buffSets.size()]);
			Arrays.sort(buffSetsArr, (s1, s2) -> {
				return Integer.compare(s2.getId(), s1.getId());
			});

			int i = 0;
			for(BuffSet buffSet : buffSetsArr)
			{
				if(i % 2 == 0)
					result.append("<table width=\"285\" border=\"0\" bgcolor=\"000000\">");
				else
					result.append("<table width=\"285\" border=\"0\">");
				result.append("<tr>");
				result.append("<td width=\"185\" align=\"center\">");
				result.append(htmlButton(buffSet.getName(player.getLanguage()), 200, 25, "list", "2_0", buffSet.getId()));
				result.append("</td>");
				result.append("</tr></table>");
				i++;
			}

			result.append("</td></tr>");
			result.append("<tr><td width=\"288\"><table width=\"285\" border=\"0\" bgcolor=\"3D3D3D\"><tr><td width=\"260\" align=\"center\"></td></tr></table></td></tr>");
			result.append("</table><br><br><br>");
		}
		result.append("<br><br>");
		result.append("</td>");
		result.append("</tr>");
		result.append("</table>");
		result.append("</td>");
		result.append("</tr>");
		result.append("</table>");

		return result.toString();
	}

	public String BuffList(String[] var, Player player)
	{
		if(var[0].equalsIgnoreCase("get"))
			return pageGet(player, var);

		if(var[0].equalsIgnoreCase("list"))
			return pageList(player, var);

		if(var[0].equalsIgnoreCase("editset") && var.length > 1)
			return pageEdit(player, var);

		return pageMain(player);
	}

	private static Map<Integer, BuffSet> getBuffSets(int ownerId)
	{
		if(ownerId <= 0)
			return CommunityBufferHolder.getInstance().getBuffSets();

		return CommunityBufferDAO.getInstance().restore(ownerId);
	}

	private static String formatTable(StringBuilder pageNavigation, Collection<String> tds, int rows, boolean appendTD)
	{
		StringBuilder result = new StringBuilder();
		int i = 0;
		int j=0;
		result.append("<tr><td align=\"center\" width=\"710\" height=\"40\"><br>");
		if(pageNavigation != null && pageNavigation.length() > 0)
			result.append(pageNavigation.toString());
		else
			result.append("&nbsp;");
		result.append("</td></tr>");
		result.append("<tr><td align=\"center\" width=\"710\">");
		result.append("<table width=\"710\" fixheight=\"3\" bgcolor=\"3D3D3D\"><tr><td width=\"705\" fixheight=\"3\" align=\"center\"></td></tr></table>");
		for(String td : tds)
		{
			if(i == 0)
			{
				if(j % 2 == 0)
					result.append("<table width=\"710\" border=\"0\" bgcolor=\"000000\"><tr>");
				else
					result.append("<table width=\"710\" border=\"0\"><tr>");
			}

			if(appendTD)
				result.append("<td align=\"center\" width=\"230\">");

			result.append(td);

			if(appendTD)
				result.append("</td>");

			i++;
			if(i == rows)
			{
				result.append("</tr></table>");
				i = 0;
				j++;
			}
		}
		if(i > 0 && i < rows)
		{
			while(i < rows)
			{
				result.append("<td align=\"center\" FIXWIDTH=\"230\"></td>");
				i++;
			}
			result.append("</tr></table>");
		}
		result.append("<table width=\"710\" fixheight=\"3\" bgcolor=\"3D3D3D\"><tr><td width=\"705\" fixheight=\"3\" align=\"center\"></td></tr></table>");
		result.append("</td></tr>");
		result.append("<tr><td align=\"center\" width=\"710\" height=45>");
		if(pageNavigation != null && pageNavigation.length() > 0)
			result.append(pageNavigation.toString());
		else
			result.append("&nbsp;");
		result.append("<br></td></tr>");
		return result.toString();
	}

	/**
	 * кроме обычного trim, заменяет кавычки на нестандартные UTF-8, удяляет ВСЕ двойные пробелы, убирает символы <>
	 */
	private static String trimHtml(String s)
	{
		int i;
		s = s.trim().replaceAll("\"", "״").replaceAll("'", "´").replaceAll("<", "").replaceAll(">", "");
		do
		{
			i = s.length();
			s = s.replaceAll("  ", " ");
		}
		while(i > s.length());

		return s;
	}
}
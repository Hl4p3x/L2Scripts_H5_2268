package handler.bbs.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.map.hash.TIntStringHashMap;
import l2s.gameserver.Config;
import l2s.gameserver.data.QuestHolder;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.handler.bbs.CommunityBoardManager;
import l2s.gameserver.handler.bbs.ICommunityBoardHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.ShowBoardPacket;
import l2s.gameserver.network.l2.s2c.SocialActionPacket;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Util;

/**
 * @author Bonux
**/
public class CommunityCareer extends CustomCommunityHandler
{
	private static final Logger _log = LoggerFactory.getLogger(CommunityCareer.class);

	@Override
	public String[] getBypassCommands()
	{
		return new String[]
		{
			"_cbbscareer"
		};
	}

	@Override
	protected void doBypassCommand(Player player, String bypass)
	{
		StringTokenizer st = new StringTokenizer(bypass, "_");
		String cmd = st.nextToken();
		String html = "";

		if("cbbscareer".equals(cmd))
		{
			String cmd2 = st.nextToken();
			if("profession".equals(cmd2))
			{
				if(BBSConfig.OCCUPATION_SERVICE_COST_ITEM_ID_1 == 0 && BBSConfig.OCCUPATION_SERVICE_COST_ITEM_ID_2 == 0 && BBSConfig.OCCUPATION_SERVICE_COST_ITEM_ID_3 == 0)
				{
					player.sendMessage(player.isLangRus() ? "Ð”Ð°Ð½Ð½Ñ‹Ð¹ Ñ�ÐµÑ€Ð²Ð¸Ñ� Ð¾Ñ‚ÐºÐ»ÑŽÑ‡ÐµÐ½." : "This service disallowed.");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				TIntStringHashMap tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/professions.htm", player);
				html = tpls.get(0);

				StringBuilder content = new StringBuilder();

				final int feeItemId = getFeeItemIdForChangeClass(player);
				final long feeItemCount = getFeeItemCountForChangeClass(player);
				final int nextClassMinLevel = getNextClassMinLevel(player);
				if(!st.hasMoreTokens())
				{
					if(nextClassMinLevel == 0)
						content.append(tpls.get(1));
					else if(feeItemId == 0)
						content.append(tpls.get(8));
					else
					{
						if(nextClassMinLevel > player.getLevel())
							content.append(tpls.get(5).replace("<?level?>", String.valueOf(nextClassMinLevel)));
						else
						{
							List<ClassId> availClasses = getAvailClasses(player.getClassId());
							if(availClasses.isEmpty())
								content.append(tpls.get(6));
							else
							{
								ClassId classId = availClasses.get(0);
								content.append(tpls.get(2));

								if(feeItemCount > 0)
								{
									content.append("<br1>");
									String priceMsg = tpls.get(3);
									content.append(priceMsg.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount)).replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId)));
								}

								for(ClassId cls : availClasses)
								{
									content.append("<br>");

									String classHtm = tpls.get(4);
									classHtm = classHtm.replace("<?class_name?>", cls.getName(player));
									classHtm = classHtm.replace("<?class_id?>", String.valueOf(cls.getId()));

									content.append(classHtm);
								}
							}
						}
					}
				}
				else
				{
					if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
					{
						onWrongCondition(player);
						return;
					}

					if(nextClassMinLevel == 0 || feeItemId == 0 || nextClassMinLevel > player.getLevel())
					{
						ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler("_cbbscareer_profession");
						if(handler != null)
							onBypassCommand(player, "_cbbscareer_profession");
						return;
					}

					List<ClassId> availClasses = getAvailClasses(player.getClassId());
					if(availClasses.isEmpty())
					{
						ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler("_cbbscareer_profession");
						if(handler != null)
							onBypassCommand(player, "_cbbscareer_profession");
						return;
					}

					boolean avail = false;
					ClassId classId = ClassId.VALUES[Integer.parseInt(st.nextToken())];
					for(ClassId cls : availClasses)
					{
						if(cls == classId)
						{
							avail = true;
							break;
						}
					}

					if(!avail)
					{
						ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler("_cbbscareer_profession");
						if(handler != null)
							onBypassCommand(player, "_cbbscareer_profession");
						return;
					}

					if(feeItemCount > 0 && !ItemFunctions.deleteItem(player, feeItemId, feeItemCount, true))
					{
						String errorMsg = tpls.get(7).replace("<?fee_item_count?>", Util.formatAdena(feeItemCount)).replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));
						html = html.replace("<?content?>", errorMsg);
						ShowBoardPacket.separateAndSend(html, player);
						return;
					}

					player.sendPacket(SystemMsg.CONGRATULATIONS__YOUVE_COMPLETED_A_CLASS_TRANSFER);
					player.setClassId(classId.getId(), false, false);
					player.broadcastUserInfo(true);

					ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler("_cbbscareer_profession");
					if(handler != null)
						onBypassCommand(player, "_cbbscareer_profession");
					return;
				}

				html = html.replace("<?content?>", content.toString());
			}
			else if("subclass".equals(cmd2))
			{
				if(BBSConfig.SUBCLASS_SERVICE_COST_ITEM_ID == 0)
				{
					player.sendMessage(player.isLangRus() ? "Ð”Ð°Ð½Ð½Ñ‹Ð¹ Ñ�ÐµÑ€Ð²Ð¸Ñ� Ð¾Ñ‚ÐºÐ»ÑŽÑ‡ÐµÐ½." : "This service disallowed.");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				TIntStringHashMap tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/subclass.htm", player);
				html = tpls.get(0);

				final long price = BBSConfig.SUBCLASS_SERVICE_COST_ITEM_COUNT;

				StringBuilder content = new StringBuilder();

				final QuestState qs1 = player.getQuestState(234);
				final QuestState qs2 = player.getQuestState(player.getRace() == Race.KAMAEL ? 236 : 235);
				if(player.isNoble() || Config.ALT_GAME_SUBCLASS_WITHOUT_QUESTS || (qs1 != null && qs1.isCompleted() && qs2 != null && qs2.isCompleted()))
					content.append(tpls.get(5));
				else if(player.getLevel() < Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS)
					content.append(tpls.get(4).replace("<?min_level?>", String.valueOf(Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS)));
				else
				{
					if(!st.hasMoreTokens())
					{
						if(price > 0)
						{
							String priceMsg = tpls.get(1).replace("<?fee_item_count?>", Util.formatAdena(price));
							priceMsg = priceMsg.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(BBSConfig.SUBCLASS_SERVICE_COST_ITEM_ID));
							content.append(priceMsg);
						}
						else
							content.append(tpls.get(2));

						content.append(tpls.get(3));
					}
					else
					{
						String cmd3 = st.nextToken();
						if("buy".equals(cmd3))
						{
							if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
							{
								onWrongCondition(player);
								return;
							}

							if(price == 0 || ItemFunctions.deleteItem(player, BBSConfig.SUBCLASS_SERVICE_COST_ITEM_ID, price, true))
							{
								completeQuest(234, player);
								completeQuest(player.getRace() == Race.KAMAEL ? 236 : 235, player);

								content.append(tpls.get(7));
							}
							else
							{
								String errorMsg = tpls.get(6).replace("<?fee_item_count?>", Util.formatAdena(price));
								errorMsg = errorMsg.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(BBSConfig.SUBCLASS_SERVICE_COST_ITEM_ID));
								content.append(errorMsg);
							}
						}
					}
				}
				html = html.replace("<?content?>", content.toString());
			}
			else if("noble".equals(cmd2))
			{
				if(BBSConfig.NOBLE_SERVICE_COST_ITEM_ID == 0)
				{
					player.sendMessage(player.isLangRus() ? "Ð”Ð°Ð½Ð½Ñ‹Ð¹ Ñ�ÐµÑ€Ð²Ð¸Ñ� Ð¾Ñ‚ÐºÐ»ÑŽÑ‡ÐµÐ½." : "This service disallowed.");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				TIntStringHashMap tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/noble.htm", player);
				html = tpls.get(0);

				final long price = BBSConfig.NOBLE_SERVICE_COST_ITEM_COUNT;

				StringBuilder content = new StringBuilder();
				if(player.isNoble())
					content.append(tpls.get(5));
				else if(player.getSubLevel() < 75)
					content.append(tpls.get(4));
				else
				{
					if(!st.hasMoreTokens())
					{
						if(price > 0)
						{
							String priceMsg = tpls.get(1).replace("<?fee_item_count?>", Util.formatAdena(price));
							priceMsg = priceMsg.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(BBSConfig.NOBLE_SERVICE_COST_ITEM_ID));
							content.append(priceMsg);
						}
						else
							content.append(tpls.get(2));

						content.append(tpls.get(3));
					}
					else
					{
						String cmd3 = st.nextToken();
						if("buy".equals(cmd3))
						{
							if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
							{
								onWrongCondition(player);
								return;
							}

							if(price == 0 || ItemFunctions.deleteItem(player, BBSConfig.NOBLE_SERVICE_COST_ITEM_ID, price, true))
							{
								Olympiad.addNoble(player);
								player.setNoble(true);
								player.updatePledgeClass();
 								player.updateNobleSkills();
								player.sendSkillList();
								player.broadcastPacket(new SocialActionPacket(player.getObjectId(), 16));
								player.broadcastUserInfo(true);

								content.append(tpls.get(7));
							}
							else
							{
								String errorMsg = tpls.get(6).replace("<?fee_item_count?>", Util.formatAdena(price));
								errorMsg = errorMsg.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(BBSConfig.NOBLE_SERVICE_COST_ITEM_ID));
								content.append(errorMsg);
							}
						}
					}
				}
				html = html.replace("<?content?>", content.toString());
			}
			else if("level".equals(cmd2)) {
				if(!BBSConfig.LEVEL_SERVICE_ENABLED) {
					player.sendMessage(player.isLangRus() ? "Ð”Ð°Ð½Ð½Ñ‹Ð¹ Ñ�ÐµÑ€Ð²Ð¸Ñ� Ð¾Ñ‚ÐºÐ»ÑŽÑ‡ÐµÐ½." : "This service disallowed.");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				TIntStringHashMap tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/level.htm", player);
				html = tpls.get(0);

				final int riseItemId = BBSConfig.LEVEL_RISE_SERVICE_ITEM[player.getLevel()][0];
				final int riseItemCount = BBSConfig.LEVEL_RISE_SERVICE_ITEM[player.getLevel()][1];
				final int downItemId = BBSConfig.LEVEL_DOWN_SERVICE_ITEM[player.getLevel() - 1][0];
				final int downItemCount = BBSConfig.LEVEL_DOWN_SERVICE_ITEM[player.getLevel() - 1][1];

				StringBuilder content = new StringBuilder();
				if(player.isPK())
					content.append(tpls.get(8));
				else
				{
					if(!st.hasMoreTokens()) {
						boolean canUse = false;
						if(player.getLevel() < player.getMaxLevel()) {
							if (riseItemId > 0) {
								if (riseItemCount > 0) {
									String priceMsg = tpls.get(1).replace("<?rise_fee_item_count?>", Util.formatAdena(riseItemCount));
									priceMsg = priceMsg.replace("<?rise_fee_item_name?>", HtmlUtils.htmlItemName(riseItemId));
									priceMsg = priceMsg.replace("<?rise_level?>", String.valueOf(player.getLevel() + 1));
									content.append(priceMsg);
								} else
									content.append(tpls.get(3));

								content.append(tpls.get(5));
								canUse = true;
							}
						}

						if(canUse)
							content.append("<br><br>");

						if(player.getLevel() > 1) {
							if (downItemId > 0) {
								if (downItemCount > 0) {
									String priceMsg = tpls.get(2).replace("<?down_fee_item_count?>", Util.formatAdena(downItemCount));
									priceMsg = priceMsg.replace("<?down_fee_item_name?>", HtmlUtils.htmlItemName(downItemId));
									priceMsg = priceMsg.replace("<?down_level?>", String.valueOf(player.getLevel() - 1));
									content.append(priceMsg);
								} else
									content.append(tpls.get(4));

								content.append(tpls.get(6));
								canUse = true;
							}
						}

						if(!canUse)
							content.append(tpls.get(7));
					}
					else {
						String cmd3 = st.nextToken();
						if("rise".equals(cmd3)) {
							if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player)) {
								onWrongCondition(player);
								return;
							}

							if(riseItemId == 0) {
								player.sendMessage(player.isLangRus() ? "Ð”Ð°Ð½Ð½Ñ‹Ð¹ Ñ�ÐµÑ€Ð²Ð¸Ñ� Ð¾Ñ‚ÐºÐ»ÑŽÑ‡ÐµÐ½." : "This service disallowed.");
								player.sendPacket(ShowBoardPacket.CLOSE);
								return;
							}

							int level = player.getLevel();
							if(level == Experience.getMaxLevel()) {
								player.sendMessage(player.isLangRus() ? "Ð’Ñ‹ ÑƒÐ¶Ðµ Ð´Ð¾Ñ�Ñ‚Ð¸Ð³Ð»Ð¸ Ð¿Ð¾Ñ�Ð»ÐµÐ´Ð½ÐµÐ³Ð¾ ÑƒÑ€Ð¾Ð²Ð½Ñ�." : "You have already reached the last level.");
								player.sendPacket(ShowBoardPacket.CLOSE);
								return;
							}

							if(riseItemCount == 0 || ItemFunctions.deleteItem(player, riseItemId, riseItemCount, true)) {
								long exp = Experience.LEVEL[level + 1] - player.getExp();
								player.addExpAndSp(exp, 0);
								content.append(tpls.get(10));
							}
							else {
								String errorMsg = tpls.get(9).replace("<?fee_item_count?>", Util.formatAdena(riseItemCount));
								errorMsg = errorMsg.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(riseItemId));
								content.append(errorMsg);
							}
						}
						else if("down".equals(cmd3)) {
							if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player)) {
								onWrongCondition(player);
								return;
							}

							if(downItemId == 0) {
								player.sendMessage(player.isLangRus() ? "Ð”Ð°Ð½Ð½Ñ‹Ð¹ Ñ�ÐµÑ€Ð²Ð¸Ñ� Ð¾Ñ‚ÐºÐ»ÑŽÑ‡ÐµÐ½." : "This service disallowed.");
								player.sendPacket(ShowBoardPacket.CLOSE);
								return;
							}

							int level = player.getLevel();
							if(level == 1) {
								player.sendMessage(player.isLangRus() ? "Ð�ÐµÐ»ÑŒÐ·Ñ� Ð¿Ð¾Ð½Ð¸Ð·Ð¸Ñ‚ÑŒ ÑƒÑ€Ð¾Ð²ÐµÐ½ÑŒ Ð½Ð¸Ð¶Ðµ Ð¿ÐµÑ€Ð²Ð¾Ð³Ð¾." : "You cannot lower the level below the first.");
								player.sendPacket(ShowBoardPacket.CLOSE);
								return;
							}

							if(downItemCount == 0 || ItemFunctions.deleteItem(player, downItemId, downItemCount, true)) {
								long exp = Experience.LEVEL[level - 1] - player.getExp();
								player.addExpAndSp(exp, 0);
								player.broadcastPacket(new MagicSkillUse(player, player, 23128, 1, 1, 0));
								content.append(tpls.get(11));
							}
							else {
								String errorMsg = tpls.get(9).replace("<?fee_item_count?>", Util.formatAdena(riseItemCount));
								errorMsg = errorMsg.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(riseItemId));
								content.append(errorMsg);
							}
						}
					}
				}
				html = html.replace("<?content?>", content.toString());
			}
		}
		ShowBoardPacket.separateAndSend(html, player);
	}

	@Override
	protected void doWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		//
	}

	private static int getNextClassMinLevel(Player player)
	{
		switch(player.getClassId().getClassLevel())
		{
			case NONE:
				return 20;
			case FIRST:
				return 40;
			case SECOND:
				return 76;
		}
		return 0;
	}

	private static int getFeeItemIdForChangeClass(Player player)
	{
		switch(player.getClassId().getClassLevel())
		{
			case NONE:
				return BBSConfig.OCCUPATION_SERVICE_COST_ITEM_ID_1;
			case FIRST:
				return BBSConfig.OCCUPATION_SERVICE_COST_ITEM_ID_2;
			case SECOND:
				return BBSConfig.OCCUPATION_SERVICE_COST_ITEM_ID_3;
		}
		return 0;
	}

	private static long getFeeItemCountForChangeClass(Player player)
	{
		switch(player.getClassId().getClassLevel())
		{
			case NONE:
				return BBSConfig.OCCUPATION_SERVICE_COST_ITEM_COUNT_1;
			case FIRST:
				return BBSConfig.OCCUPATION_SERVICE_COST_ITEM_COUNT_2;
			case SECOND:
				return BBSConfig.OCCUPATION_SERVICE_COST_ITEM_COUNT_3;
		}
		return 0L;
	}

	private static List<ClassId> getAvailClasses(ClassId playerClass)
	{
		List<ClassId> result = new ArrayList<ClassId>();
		for(ClassId _class : ClassId.values())
		{
			if(!_class.isDummy() && _class.getClassLevel().ordinal() == playerClass.getClassLevel().ordinal() + 1 && _class.childOf(playerClass) && _class != ClassId.INSPECTOR)
				result.add(_class);
		}		
		return result;
	}

	private static void completeQuest(int id, Player player)
	{
		Quest quest = QuestHolder.getInstance().getQuest(id);
		if(quest != null)
		{
			QuestState qs = player.getQuestState(quest);
			if(qs == null)
				qs = quest.newQuestState(player);
			qs.finishQuest();
		}
	}
}
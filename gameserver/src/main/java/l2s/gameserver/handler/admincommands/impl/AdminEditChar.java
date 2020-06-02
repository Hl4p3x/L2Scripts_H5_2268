package l2s.gameserver.handler.admincommands.impl;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang3.math.NumberUtils;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.dao.CHeroDao;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.database.mysql;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.SubClassInfo;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.ExPCCafePointInfoPacket;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.PositionUtils;
import l2s.gameserver.utils.Util;

@SuppressWarnings("unused")
public class AdminEditChar implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_edit_character,
		admin_character_actions,
		admin_current_player,
		admin_nokarma,
		admin_setkarma,
		admin_character_list,
		admin_show_characters,
		admin_find_character,
		admin_save_modifications,
		admin_rec,
		admin_settitle,
		admin_setclass,
		admin_setname,
		admin_setsex,
		admin_setcolor,
		admin_add_exp_sp_to_character,
		admin_add_exp_sp,
		admin_sethero,
		admin_setcustomhero,
		admin_setnoble,
		admin_trans,
		admin_setsubclass,
		admin_setfame,
		admin_setbday,
		admin_give_item,
		admin_add_bang,
		admin_set_bang
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(activeChar.getPlayerAccess().CanRename)
			if(fullString.startsWith("admin_settitle"))
				try
				{
					String val = fullString.substring(15);
					GameObject target = activeChar.getTarget();
					Player player = null;
					if(target == null)
						return false;
					if(target.isPlayer())
					{
						player = (Player) target;
						player.setTitle(val);
						player.sendMessage(new CustomMessage("common.Admin.EditChar.SuccessTitleChange", player));
						player.sendChanges();
					}
					else if(target.isNpc())
					{
						((NpcInstance) target).setTitle(val);
						target.decayMe();
						target.spawnMe();
					}

					return true;
				}
				catch(StringIndexOutOfBoundsException e)
				{ // Case of empty character title
					activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.ErrorNoTitle", activeChar));
					return false;
				}
			else if(fullString.startsWith("admin_setclass"))
				try
				{
					String val = fullString.substring(15);
					int id = Integer.parseInt(val.trim());
					GameObject target = activeChar.getTarget();

					if(target == null || !target.isPlayer())
						target = activeChar;
					if(id > 136)
					{
						activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.NoClanId137", activeChar));
						return false;
					}
					Player player = target.getPlayer();
					if(player.setClassId(id, false, false))
					{
						player.sendMessage(new CustomMessage("common.Admin.EditChar.SuccessChangeClass", player));
						player.broadcastUserInfo(true);
						return true;
					}
					return false;
				}
				catch(StringIndexOutOfBoundsException e)
				{
					activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.ErrorNoClassId", activeChar));
					return false;
				}
			else if(fullString.startsWith("admin_setname"))
				try
				{
					String val = fullString.substring(14);
					GameObject target = activeChar.getTarget();
					Player player;
					if(target != null && target.isPlayer())
						player = (Player) target;
					else
						return false;
					if(mysql.simple_get_int("count(*)", "characters", "`char_name` like '" + val + "'") > 0)
					{
						activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.NameExist", activeChar));
						return false;
					}
					Log.LogEvent(activeChar.getName(), activeChar.getIP(), "AdminCommands", "Rename Char: rename from: "+player.getName()+" to "+val+"");
					Log.add("Character " + player.getName() + " renamed to " + val + " by GM " + activeChar.getName(), "renames");
					player.reName(val);
					player.sendMessage(new CustomMessage("common.Admin.EditChar.SuccessChangeNickName", player));
					return true;
				}
				catch(StringIndexOutOfBoundsException e)
				{ // Case of empty character name
					activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.ErrorNoNickName", activeChar));
					return false;
				}

		if(!activeChar.getPlayerAccess().CanEditChar && !activeChar.getPlayerAccess().CanViewChar)
			return false;

		if(fullString.equals("admin_current_player"))
			showCharacterList(activeChar, null);
		else if(fullString.startsWith("admin_character_list"))
			try
			{
				String val = fullString.substring(21);
				Player target = GameObjectsStorage.getPlayer(val);
				showCharacterList(activeChar, target);
			}
			catch(StringIndexOutOfBoundsException e)
			{
				// Case of empty character name
			}
		else if(fullString.startsWith("admin_show_characters"))
			try
			{
				String val = fullString.substring(22);
				int page = Integer.parseInt(val);
				listCharacters(activeChar, page);
			}
			catch(StringIndexOutOfBoundsException e)
			{
				// Case of empty page
			}
		else if(fullString.startsWith("admin_find_character"))
			try
			{
				String val = fullString.substring(21);
				findCharacter(activeChar, val);
			}
			catch(StringIndexOutOfBoundsException e)
			{ // Case of empty character name
				activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.ErrorNoNickNameSpecified", activeChar));

				listCharacters(activeChar, 0);
			}
		else if(!activeChar.getPlayerAccess().CanEditChar)
			return false;
		else if(fullString.equals("admin_edit_character"))
			editCharacter(activeChar);
		else if(fullString.equals("admin_character_actions"))
			showCharacterActions(activeChar);
		else if(fullString.equals("admin_nokarma"))
			setTargetKarma(activeChar, 0);
		else if(fullString.startsWith("admin_setkarma"))
			try
			{
				String val = fullString.substring(15);
				int karma = Integer.parseInt(val);
				setTargetKarma(activeChar, karma);
			}
			catch(StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.ErrorNoKarma", activeChar));
			}
		else if(fullString.startsWith("admin_save_modifications"))
			try
			{
				String val = fullString.substring(24);
				adminModifyCharacter(activeChar, val);
			}
			catch(StringIndexOutOfBoundsException e)
			{ // Case of empty character name
				activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.ErrorNoSavedData", activeChar));
				listCharacters(activeChar, 0);
			}
		else if(fullString.equals("admin_rec"))
		{
			GameObject target = activeChar.getTarget();
			Player player = null;
			if(target != null && target.isPlayer())
				player = (Player) target;
			else
				return false;
			player.setRecomHave(player.getRecomHave() + 1);
			activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.SuccesRecommend", activeChar));
			player.broadcastUserInfo(true);
		}
		else if(fullString.startsWith("admin_rec"))
			try
			{
				String val = fullString.substring(10);
				int recVal = Integer.parseInt(val);
				GameObject target = activeChar.getTarget();
				Player player = null;
				if(target != null && target.isPlayer())
					player = (Player) target;
				else
					return false;
				player.setRecomHave(player.getRecomHave() + recVal);
				activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.SuccesRecommend", activeChar));
				player.broadcastUserInfo(true);
			}
			catch(NumberFormatException e)
			{
				activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.RecomUssage", activeChar));
			}
		else if(fullString.startsWith("admin_sethero"))
		{
			// Статус меняется только на текущую логон сессию
			GameObject target = activeChar.getTarget();
			Player player;
			if(wordList.length > 1 && wordList[1] != null)
			{
				player = GameObjectsStorage.getPlayer(wordList[1]);
				if(player == null)
				{
					activeChar.sendMessage(new CustomMessage("common.Admin.Disconect.ErrorName404", activeChar).addString(wordList[1]));
					return false;
				}
			}
			else if(target != null && target.isPlayer())
				player = (Player) target;
			else
			{
				activeChar.sendMessage(new CustomMessage("common.Admin.Disconect.ErrorName", activeChar));
				return false;
			}

			if(player.isHero())
			{
				player.setHero(false);
				player.updatePledgeClass();
				Hero.removeSkills(player);
				player.sendSkillList();
			}
			else
			{
				player.setHero(true);
				player.updatePledgeClass();
				if(!player.isSubClassActive())
				{
					Hero.addSkills(player);
					player.sendSkillList();
				}
			}

			player.sendMessage(new CustomMessage("common.Admin.EditChar.SuccessHero", player));
			player.broadcastUserInfo(true);
		}

		else if(fullString.startsWith("admin_setcustomhero"))
		{
			// Статус меняется только на текущую логон сессию
			GameObject target = activeChar.getTarget();
			Player player;
			long days = 0;
			if(wordList.length > 1 && wordList[1] != null)
			{
				player = GameObjectsStorage.getPlayer(wordList[1]);
				days = Long.parseLong(wordList[2]);
				if(player == null)
				{
					activeChar.sendMessage(new CustomMessage("common.Admin.Disconect.ErrorName404", activeChar).addString(wordList[1]));
					return false;
				}
			}
			else if(target != null && target.isPlayer())
				player = (Player) target;
			else
			{
				activeChar.sendMessage(new CustomMessage("common.Admin.Disconect.ErrorName", activeChar));
				return false;
			}

			if(player.isHero())
			{
				player.setHero(false);
				player.updatePledgeClass();
				player.removeSkill(SkillHolder.getInstance().getSkill(395, 1));
				player.removeSkill(SkillHolder.getInstance().getSkill(396, 1));
				player.removeSkill(SkillHolder.getInstance().getSkill(1374, 1));
				player.removeSkill(SkillHolder.getInstance().getSkill(1375, 1));
				player.removeSkill(SkillHolder.getInstance().getSkill(1376, 1));
				CHeroDao.removeCustomHero(player.getObjectId());
			}
			else
			{
				player.setHero(true);
				player.updatePledgeClass();
				player.addSkill(SkillHolder.getInstance().getSkill(395, 1));
				player.addSkill(SkillHolder.getInstance().getSkill(396, 1));
				player.addSkill(SkillHolder.getInstance().getSkill(1374, 1));
				player.addSkill(SkillHolder.getInstance().getSkill(1375, 1));
				player.addSkill(SkillHolder.getInstance().getSkill(1376, 1));
				long daysInMills = days * 60 * 60 * 24 * 1000;
				long finalTime = System.currentTimeMillis() + daysInMills;
				CHeroDao.addCustomHero(player.getObjectId(), finalTime);
			}

			player.sendSkillList();
			player.sendMessage(new CustomMessage("common.Admin.EditChar.SuccessHero", player));
			player.broadcastUserInfo(true);
		}		
		else if(fullString.startsWith("admin_setnoble"))
		{
			// Статус сохраняется в базе
			GameObject target = activeChar.getTarget();
			Player player;
			if(wordList.length > 1 && wordList[1] != null)
			{
				player = GameObjectsStorage.getPlayer(wordList[1]);
				if(player == null)
				{
					activeChar.sendMessage(new CustomMessage("common.Admin.Disconect.ErrorName404", activeChar).addString(wordList[1]));
					return false;
				}
			}
			else if(target != null && target.isPlayer())
				player = (Player) target;
			else
			{
				activeChar.sendMessage(new CustomMessage("common.Admin.Disconect.ErrorName", activeChar));
				return false;
			}

			if(player.isNoble())
			{
				Olympiad.removeNoble(player);
				player.setNoble(false);
				player.sendMessage(new CustomMessage("common.Admin.EditChar.SuccessNotNoble", player));
			}
			else
			{
				Olympiad.addNoble(player);
				player.setNoble(true);
				player.sendMessage(new CustomMessage("common.Admin.EditChar.SuccessNoble", player));
			}

			player.updatePledgeClass();
			player.updateNobleSkills();
			player.sendSkillList();
			player.broadcastUserInfo(true);
		}
		else if(fullString.startsWith("admin_setsex"))
		{
			GameObject target = activeChar.getTarget();
			Player player = null;
			if(target != null && target.isPlayer())
				player = (Player) target;
			else
				return false;
			player.changeSex();
			player.sendMessage(new CustomMessage("common.Admin.EditChar.SuccessChangeSex", player));
			player.broadcastUserInfo(true);
		}
		else if(fullString.startsWith("admin_setcolor"))
			try
			{
				String val = fullString.substring(15);
				GameObject target = activeChar.getTarget();
				Player player = null;
				if(target != null && target.isPlayer())
					player = (Player) target;
				else
					return false;
				player.setNameColor(Integer.decode("0x" + val), true);
				player.sendMessage(new CustomMessage("common.Admin.EditChar.SuccessChangeColorNick", player));
				player.broadcastUserInfo(true);
			}
			catch(StringIndexOutOfBoundsException e)
			{ // Case of empty color
				activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.ErrorNoNewColor", activeChar));
			}
		else if(fullString.startsWith("admin_add_exp_sp_to_character"))
			addExpSp(activeChar);
		else if(fullString.startsWith("admin_add_exp_sp"))
			try
			{
				final String val = fullString.substring(16).trim();

				String[] vals = val.split(" ");
				long exp = NumberUtils.toLong(vals[0], 0L);
				int sp = vals.length > 1 ? NumberUtils.toInt(vals[1], 0) : 0;

				adminAddExpSp(activeChar, exp, sp);
			}
			catch(Exception e)
			{
				activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.ExpSpUssage", activeChar));
			}
		else if(fullString.startsWith("admin_trans"))
		{
			StringTokenizer st = new StringTokenizer(fullString);
			if(st.countTokens() > 1)
			{
				st.nextToken();
				int transformId = 0;
				try
				{
					transformId = Integer.parseInt(st.nextToken());
				}
				catch(Exception e)
				{
					activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.ErrorNotInteger", activeChar));
					return false;
				}
				if(transformId != 0 && activeChar.getTransformation() != 0)
				{
					activeChar.sendPacket(Msg.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
					return false;
				}
				activeChar.setTransformation(transformId);
				activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.Transforming", activeChar));
			}
			else
				activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.TransformUssage", activeChar));
		}
		else if(fullString.startsWith("admin_setsubclass"))
		{
			final GameObject target = activeChar.getTarget();
			if(target == null || !target.isPlayer())
			{
				activeChar.sendPacket(Msg.SELECT_TARGET);
				return false;
			}
			final Player player = (Player) target;

			StringTokenizer st = new StringTokenizer(fullString);
			if(st.countTokens() > 1)
			{
				st.nextToken();
				int classId = Short.parseShort(st.nextToken());
				if(!player.addSubClass(classId, true, 0))
				{
					activeChar.sendMessage(new CustomMessage("l2s.gameserver.model.instances.L2VillageMasterInstance.SubclassCouldNotBeAdded", activeChar));
					return false;
				}
				player.sendPacket(Msg.CONGRATULATIONS_YOU_HAVE_TRANSFERRED_TO_A_NEW_CLASS); // Transfer to new class.
			}
			else
				setSubclass(activeChar, player);
		}
		else if(fullString.startsWith("admin_setfame"))
			try
			{
				String val = fullString.substring(14);
				int fame = Integer.parseInt(val);
				setTargetFame(activeChar, fame);
			}
			catch(StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.FameValue", activeChar));
			}
		else if(fullString.startsWith("admin_setbday"))
		{
			String msgUsage = "Usage: //setbday YYYY-MM-DD";
			String date = fullString.substring(14);
			if(date.length() != 10 || !Util.isMatchingRegexp(date, "[0-9]{4}-[0-9]{2}-[0-9]{2}"))
			{
				activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.SetBDayUssage", activeChar));
				return false;
			}

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			try
			{
				dateFormat.parse(date);
			}
			catch(ParseException e)
			{
				activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.SetBDayUssage", activeChar));
			}

			if(activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
			{
				activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.BDayNoChar", activeChar));
				return false;
			}

			if(!mysql.set("update characters set createtime = UNIX_TIMESTAMP('" + date + "') where obj_Id = " + activeChar.getTarget().getObjectId()))
			{
				activeChar.sendMessage(msgUsage);
				return false;
			}
			activeChar.sendMessage("New Birthday for " + activeChar.getTarget().getName() + ": " + date);
			activeChar.getTarget().getPlayer().sendMessage("Admin changed your birthday to: " + date);
		}
		else if(fullString.startsWith("admin_give_item"))
		{
			if(wordList.length < 3)
			{
				activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.BdayUssage", activeChar));
				return false;
			}
			int id = Integer.parseInt(wordList[1]);
			int count = Integer.parseInt(wordList[2]);
			if(id < 1 || count < 1 || activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
			{
				activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.GiveItemsUssage", activeChar));
				return false;
			}
			ItemFunctions.addItem(activeChar.getTarget().getPlayer(), id, count, true, "Admin '" + activeChar.getName() + "' give item");
		}
		else if(fullString.startsWith("admin_add_bang"))
		{
			if(!Config.ALT_PCBANG_POINTS_ENABLED)
			{
				activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.ErrorBangPoints", activeChar));
				return true;
			}
			if(wordList.length < 1)
			{
				activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.BanPointsUssage", activeChar));
				return false;
			}
			int count = Integer.parseInt(wordList[1]);
			if(count < 1 || activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
			{
				activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.BanPointsUssage", activeChar));
				return false;
			}
			Player target = activeChar.getTarget().getPlayer();
			target.addPcBangPoints(count, false, true);
			activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.AddBangPoints", activeChar).addNumber(count).addString(target.getName()));
		}
		else if(fullString.startsWith("admin_set_bang"))
		{
			if(!Config.ALT_PCBANG_POINTS_ENABLED)
			{
				activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.ErrorBangPoints", activeChar));
				return true;
			}
			if(wordList.length < 1)
			{
				activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.BanPointsUssage", activeChar));
				return false;
			}
			int count = Integer.parseInt(wordList[1]);
			if(count < 1 || activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
			{
				activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.BanPointsSetUssage", activeChar));
				return false;
			}
			Player target = activeChar.getTarget().getPlayer();
			target.setPcBangPoints(count, true);
			target.sendMessage(new CustomMessage("common.Admin.EditChar.BangPointsMessage", target).addNumber(count));
			target.sendPacket(new ExPCCafePointInfoPacket(target, count, 1, 2, 12));
			activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.SetBangPoints", activeChar).addString(target.getName()).addNumber(count));
		}
		return true;
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private void listCharacters(Player activeChar, int page)
	{
		List<Player> players = GameObjectsStorage.getAllPlayers();

		int MaxCharactersPerPage = 20;
		int MaxPages = players.size() / MaxCharactersPerPage;

		if(players.size() > MaxCharactersPerPage * MaxPages)
			MaxPages++;

		// Check if number of users changed
		if(page > MaxPages)
			page = MaxPages;

		int CharactersStart = MaxCharactersPerPage * page;
		int CharactersEnd = players.size();
		if(CharactersEnd - CharactersStart > MaxCharactersPerPage)
			CharactersEnd = CharactersStart + MaxCharactersPerPage;

		NpcHtmlMessagePacket adminReply = new NpcHtmlMessagePacket(5);

		StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<table width=270>");
		replyMSG.append("<tr><td width=270>You can find a character by writing his name and</td></tr>");
		replyMSG.append("<tr><td width=270>clicking Find bellow.<br></td></tr>");
		replyMSG.append("<tr><td width=270>Note: Names should be written case sensitive.</td></tr>");
		replyMSG.append("</table><br>");
		replyMSG.append("<center><table><tr><td>");
		replyMSG.append("<edit var=\"character_name\" width=80></td><td><button value=\"Find\" action=\"bypass -h admin_find_character $character_name\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
		replyMSG.append("</td></tr></table></center><br><br>");

		for(int x = 0; x < MaxPages; x++)
		{
			int pagenr = x + 1;
			replyMSG.append("<center><a action=\"bypass -h admin_show_characters " + x + "\">Page " + pagenr + "</a></center>");
		}
		replyMSG.append("<br>");

		// List Players in a Table
		replyMSG.append("<table width=270>");
		replyMSG.append("<tr><td width=80>Name:</td><td width=110>Class:</td><td width=40>Level:</td></tr>");
		for(int i = CharactersStart; i < CharactersEnd; i++)
		{
			Player p = players.get(i);
			replyMSG.append("<tr><td width=80>" + "<a action=\"bypass -h admin_character_list " + p.getName() + "\">" + p.getName() + "</a></td><td width=110>" + HtmlUtils.htmlClassName(p.getClassId().getId()) + "</td><td width=40>" + p.getLevel() + "</td></tr>");
		}
		replyMSG.append("</table>");
		replyMSG.append("</body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	public static void showCharacterList(Player activeChar, Player player)
	{
		if(player == null)
		{
			GameObject target = activeChar.getTarget();
			if(target != null && target.isPlayer())
				player = (Player) target;
			else
				return;
		}
		else
			activeChar.setTarget(player);

		String clanName = "No Clan";
		if(player.getClan() != null)
			clanName = player.getClan().getName() + "/" + player.getClan().getLevel();

		NumberFormat df = NumberFormat.getNumberInstance(Locale.ENGLISH);
		df.setMaximumFractionDigits(4);
		df.setMinimumFractionDigits(1);

		NpcHtmlMessagePacket adminReply = new NpcHtmlMessagePacket(5);

		StringBuilder replyMSG = new StringBuilder("<html><body>");
		if(!activeChar.isLangRus())
		{		
			replyMSG.append("<table width=260><tr>");
			replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
			replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_show_characters 0\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("</tr></table><br>");

			replyMSG.append("<table width=270>");
			replyMSG.append("<tr><td width=100>Account/IP:</td><td>" + player.getAccountName() + "/" + player.getIP() + "</td></tr>");
			replyMSG.append("<tr><td width=100>Name/Level:</td><td>" + player.getName() + "/" + player.getLevel() + "</td></tr>");
			replyMSG.append("<tr><td width=100>Class/Id:</td><td>" + HtmlUtils.htmlClassName(player.getClassId().getId()) + "/" + player.getClassId().getId() + "</td></tr>");
			replyMSG.append("<tr><td width=100>Clan/Level:</td><td>" + clanName + "</td></tr>");
			replyMSG.append("<tr><td width=100>Exp/Sp:</td><td>" + player.getExp() + "/" + player.getSp() + "</td></tr>");
			replyMSG.append("<tr><td width=100>Cur/Max Hp:</td><td>" + (int) player.getCurrentHp() + "/" + player.getMaxHp() + "</td></tr>");
			replyMSG.append("<tr><td width=100>Cur/Max Mp:</td><td>" + (int) player.getCurrentMp() + "/" + player.getMaxMp() + "</td></tr>");
			replyMSG.append("<tr><td width=100>Cur/Max Load:</td><td>" + player.getCurrentLoad() + "/" + player.getMaxLoad() + "</td></tr>");
			replyMSG.append("<tr><td width=100>Patk/Matk:</td><td>" + player.getPAtk(null) + "/" + player.getMAtk(null, null) + "</td></tr>");
			replyMSG.append("<tr><td width=100>Pdef/Mdef:</td><td>" + player.getPDef(null) + "/" + player.getMDef(null, null) + "</td></tr>");
			replyMSG.append("<tr><td width=100>PAtkSpd/MAtkSpd:</td><td>" + player.getPAtkSpd() + "/" + player.getMAtkSpd() + "</td></tr>");
			replyMSG.append("<tr><td width=100>Acc/Evas:</td><td>" + player.getAccuracy() + "/" + player.getEvasionRate(null) + "</td></tr>");
			replyMSG.append("<tr><td width=100>Crit/MCrit:</td><td>" + player.getCriticalHit(null, null) + "/" + df.format(player.getMagicCriticalRate(null, null)) + "%</td></tr>");
			replyMSG.append("<tr><td width=100>Walk/Run:</td><td>" + player.getWalkSpeed() + "/" + player.getRunSpeed() + "</td></tr>");
			replyMSG.append("<tr><td width=100>Karma/Fame:</td><td>" + player.getKarma() + "/" + player.getFame() + "</td></tr>");
			replyMSG.append("<tr><td width=100>PvP/PK:</td><td>" + player.getPvpKills() + "/" + player.getPkKills() + "</td></tr>");
			replyMSG.append("<tr><td width=100>Coordinates:</td><td>" + player.getX() + "," + player.getY() + "," + player.getZ() + "</td></tr>");
			replyMSG.append("<tr><td width=100>Direction:</td><td>" + PositionUtils.getDirectionTo(player, activeChar) + "</td></tr>");
			replyMSG.append("<tr><td width=100>Intention:</td><td>" + player.getAI().getIntention() + "</td></tr>");
			replyMSG.append("</table><br>");

			replyMSG.append("<table<tr>");
			replyMSG.append("<td><button value=\"Skills\" action=\"bypass -h admin_show_skills\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td><button value=\"Effects\" action=\"bypass -h admin_show_effects\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td><button value=\"Actions\" action=\"bypass -h admin_character_actions\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("</tr><tr>");
			replyMSG.append("<td><button value=\"Stats\" action=\"bypass -h admin_edit_character\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td><button value=\"Exp & Sp\" action=\"bypass -h admin_add_exp_sp_to_character\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td></td>");
			replyMSG.append("</tr></table></body></html>");
		}
		else
		{		
			replyMSG.append("<table width=260><tr>");
			replyMSG.append("<td width=40><button value=\"Главная\" action=\"bypass -h admin_admin\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td width=180><center>Меню Выбора Игрока</center></td>");
			replyMSG.append("<td width=40><button value=\"Назад\" action=\"bypass -h admin_show_characters 0\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("</tr></table><br>");

			replyMSG.append("<table width=270>");
			replyMSG.append("<tr><td width=100>Акаунт/ИП:</td><td>" + player.getAccountName() + "/" + player.getIP() + "</td></tr>");
			replyMSG.append("<tr><td width=100>Имя/Уровень:</td><td>" + player.getName() + "/" + player.getLevel() + "</td></tr>");
			replyMSG.append("<tr><td width=100>Класс/Ид:</td><td>" + HtmlUtils.htmlClassName(player.getClassId().getId()) + "/" + player.getClassId().getId() + "</td></tr>");
			replyMSG.append("<tr><td width=100>Клан/Уровень:</td><td>" + clanName + "</td></tr>");
			replyMSG.append("<tr><td width=100>Exp/Sp:</td><td>" + player.getExp() + "/" + player.getSp() + "</td></tr>");
			replyMSG.append("<tr><td width=100>Cur/Max Hp:</td><td>" + (int) player.getCurrentHp() + "/" + player.getMaxHp() + "</td></tr>");
			replyMSG.append("<tr><td width=100>Cur/Max Mp:</td><td>" + (int) player.getCurrentMp() + "/" + player.getMaxMp() + "</td></tr>");
			replyMSG.append("<tr><td width=100>Cur/Max Load:</td><td>" + player.getCurrentLoad() + "/" + player.getMaxLoad() + "</td></tr>");
			replyMSG.append("<tr><td width=100>Patk/Matk:</td><td>" + player.getPAtk(null) + "/" + player.getMAtk(null, null) + "</td></tr>");
			replyMSG.append("<tr><td width=100>Pdef/Mdef:</td><td>" + player.getPDef(null) + "/" + player.getMDef(null, null) + "</td></tr>");
			replyMSG.append("<tr><td width=100>PAtkSpd/MAtkSpd:</td><td>" + player.getPAtkSpd() + "/" + player.getMAtkSpd() + "</td></tr>");
			replyMSG.append("<tr><td width=100>Acc/Evas:</td><td>" + player.getAccuracy() + "/" + player.getEvasionRate(null) + "</td></tr>");
			replyMSG.append("<tr><td width=100>Crit/MCrit:</td><td>" + player.getCriticalHit(null, null) + "/" + df.format(player.getMagicCriticalRate(null, null)) + "%</td></tr>");
			replyMSG.append("<tr><td width=100>Walk/Run:</td><td>" + player.getWalkSpeed() + "/" + player.getRunSpeed() + "</td></tr>");
			replyMSG.append("<tr><td width=100>Karma/Fame:</td><td>" + player.getKarma() + "/" + player.getFame() + "</td></tr>");
			replyMSG.append("<tr><td width=100>PvP/PK:</td><td>" + player.getPvpKills() + "/" + player.getPkKills() + "</td></tr>");
			replyMSG.append("<tr><td width=100>Координаты:</td><td>" + player.getX() + "," + player.getY() + "," + player.getZ() + "</td></tr>");
			replyMSG.append("<tr><td width=100>Направление:</td><td>" + PositionUtils.getDirectionTo(player, activeChar) + "</td></tr>");
			replyMSG.append("<tr><td width=100>Intention:</td><td>" + player.getAI().getIntention() + "</td></tr>");
			replyMSG.append("</table><br>");

			replyMSG.append("<table<tr>");
			replyMSG.append("<td><button value=\"Навыки\" action=\"bypass -h admin_show_skills\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td><button value=\"Эффекты\" action=\"bypass -h admin_show_effects\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td><button value=\"Действия\" action=\"bypass -h admin_character_actions\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("</tr><tr>");
			replyMSG.append("<td><button value=\"Статы\" action=\"bypass -h admin_edit_character\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td><button value=\"Exp & Sp\" action=\"bypass -h admin_add_exp_sp_to_character\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td></td>");
			replyMSG.append("</tr></table></body></html>");
		}
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private void setTargetKarma(Player activeChar, int newKarma)
	{
		GameObject target = activeChar.getTarget();
		if(target == null)
		{
			activeChar.sendPacket(Msg.INVALID_TARGET);
			return;
		}

		Player player;
		if(target.isPlayer())
			player = (Player) target;
		else
			return;

		if(newKarma >= 0)
		{
			int oldKarma = player.getKarma();
			player.setKarma(newKarma);
			player.sendMessage(new CustomMessage("common.Admin.EditChar.KarmaSuccessPlayer", player).addNumber(oldKarma).addNumber(newKarma));
			activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.KarmaSuccessGM", activeChar).addString(player.getName()).addNumber(oldKarma).addNumber(newKarma));
		}
		else
			activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.ErrorInvalidKarma", activeChar));			
	}

	private void setTargetFame(Player activeChar, int newFame)
	{
		GameObject target = activeChar.getTarget();
		if(target == null)
		{
			activeChar.sendPacket(Msg.INVALID_TARGET);
			return;
		}

		Player player;
		if(target.isPlayer())
			player = (Player) target;
		else
			return;

		if(newFame >= 0)
		{
			int oldFame = player.getFame();
			player.setFame(newFame, "Admin");
			player.sendMessage(new CustomMessage("common.Admin.EditChar.FameSuccessPlayer", player).addNumber(oldFame).addNumber(newFame));
			activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.FameSuccessGM", activeChar).addString(player.getName()).addNumber(oldFame).addNumber(newFame));
		}
		else
			activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.ErrorInvalidFame", activeChar));
	}

	private void adminModifyCharacter(Player activeChar, String modifications)
	{
		GameObject target = activeChar.getTarget();
		if(target == null || !target.isPlayer())
		{
			activeChar.sendPacket(Msg.SELECT_TARGET);
			return;
		}

		Player player = (Player) target;
		String[] strvals = modifications.split("&");
		Integer[] vals = new Integer[strvals.length];
		for(int i = 0; i < strvals.length; i++)
		{
			strvals[i] = strvals[i].trim();
			vals[i] = strvals[i].isEmpty() ? null : Integer.valueOf(strvals[i]);
		}

		if(vals[0] != null)
			player.setCurrentHp(vals[0], false);

		if(vals[1] != null)
			player.setCurrentMp(vals[1]);

		if(vals[2] != null)
			player.setKarma(vals[2]);

		if(vals[3] != null)
			player.setPvpFlag(vals[3]);

		if(vals[4] != null)
			player.setPvpKills(vals[4]);

		if(vals[5] != null)
			player.setClassId(vals[5], true, false);

		editCharacter(activeChar); // Back to start
		player.broadcastUserInfo(true);
	}

	private void editCharacter(Player activeChar)
	{
		GameObject target = activeChar.getTarget();
		if(target == null || !target.isPlayer())
		{
			activeChar.sendPacket(Msg.SELECT_TARGET);
			return;
		}

		Player player = (Player) target;
		NpcHtmlMessagePacket adminReply = new NpcHtmlMessagePacket(5);

		StringBuilder replyMSG = new StringBuilder("<html><body>");
		if(!activeChar.isLangRus())
		{
			replyMSG.append("<table width=260><tr>");
			replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
			replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("</tr></table>");
			replyMSG.append("<br><br>");
			replyMSG.append("<center>Editing character: " + player.getName() + "</center><br>");
			replyMSG.append("<table width=250>");
			replyMSG.append("<tr><td width=40></td><td width=70>Curent:</td><td width=70>Max:</td><td width=70></td></tr>");
			replyMSG.append("<tr><td width=40>HP:</td><td width=70>" + player.getCurrentHp() + "</td><td width=70>" + player.getMaxHp() + "</td><td width=70>Karma: " + player.getKarma() + "</td></tr>");
			replyMSG.append("<tr><td width=40>MP:</td><td width=70>" + player.getCurrentMp() + "</td><td width=70>" + player.getMaxMp() + "</td><td width=70>Pvp Kills: " + player.getPvpKills() + "</td></tr>");
			replyMSG.append("<tr><td width=40>Load:</td><td width=70>" + player.getCurrentLoad() + "</td><td width=70>" + player.getMaxLoad() + "</td><td width=70>Pvp Flag: " + player.getPvpFlag() + "</td></tr>");
			replyMSG.append("</table>");
			replyMSG.append("<table width=270><tr><td>Class<?> Template Id: " + player.getClassId() + "/" + player.getClassId().getId() + "</td></tr></table><br>");
			replyMSG.append("<table width=270>");
			replyMSG.append("<tr><td>Note: Fill all values before saving the modifications.</td></tr>");
			replyMSG.append("</table><br>");
			replyMSG.append("<table width=270>");
			replyMSG.append("<tr><td width=50>Hp:</td><td><edit var=\"hp\" width=50></td><td width=50>Mp:</td><td><edit var=\"mp\" width=50></td></tr>");
			replyMSG.append("<tr><td width=50>Pvp Flag:</td><td><edit var=\"pvpflag\" width=50></td><td width=50>Karma:</td><td><edit var=\"karma\" width=50></td></tr>");
			replyMSG.append("<tr><td width=50>Class<?> Id:</td><td><edit var=\"classid\" width=50></td><td width=50>Pvp Kills:</td><td><edit var=\"pvpkills\" width=50></td></tr>");
			replyMSG.append("</table><br>");
			replyMSG.append("<center><button value=\"Save Changes\" action=\"bypass -h admin_save_modifications $hp & $mp & $karma & $pvpflag & $pvpkills & $classid &\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center><br>");
			replyMSG.append("</body></html>");
		}
		else
		{
			replyMSG.append("<table width=260><tr>");
			replyMSG.append("<td width=40><button value=\"Главная\" action=\"bypass -h admin_admin\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td width=180><center>Меню Выбора Игрока</center></td>");
			replyMSG.append("<td width=40><button value=\"Назад\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("</tr></table>");
			replyMSG.append("<br><br>");
			replyMSG.append("<center>Редакция игрока: " + player.getName() + "</center><br>");
			replyMSG.append("<table width=250>");
			replyMSG.append("<tr><td width=40></td><td width=70>Curent:</td><td width=70>Max:</td><td width=70></td></tr>");
			replyMSG.append("<tr><td width=40>HP:</td><td width=70>" + player.getCurrentHp() + "</td><td width=70>" + player.getMaxHp() + "</td><td width=70>Karma: " + player.getKarma() + "</td></tr>");
			replyMSG.append("<tr><td width=40>MP:</td><td width=70>" + player.getCurrentMp() + "</td><td width=70>" + player.getMaxMp() + "</td><td width=70>Pvp Kills: " + player.getPvpKills() + "</td></tr>");
			replyMSG.append("<tr><td width=40>Вес:</td><td width=70>" + player.getCurrentLoad() + "</td><td width=70>" + player.getMaxLoad() + "</td><td width=70>Pvp Flag: " + player.getPvpFlag() + "</td></tr>");
			replyMSG.append("</table>");
			replyMSG.append("<table width=270><tr><td>Class<?> Template Id: " + player.getClassId() + "/" + player.getClassId().getId() + "</td></tr></table><br>");
			replyMSG.append("<table width=270>");
			replyMSG.append("<tr><td>Внимание: Заполните все поля для модификации.</td></tr>");
			replyMSG.append("</table><br>");
			replyMSG.append("<table width=270>");
			replyMSG.append("<tr><td width=50>Hp:</td><td><edit var=\"hp\" width=50></td><td width=50>Mp:</td><td><edit var=\"mp\" width=50></td></tr>");
			replyMSG.append("<tr><td width=50>Пвп Флаг:</td><td><edit var=\"pvpflag\" width=50></td><td width=50>Karma:</td><td><edit var=\"karma\" width=50></td></tr>");
			replyMSG.append("<tr><td width=50>Class<?> Id:</td><td><edit var=\"classid\" width=50></td><td width=50>Pvp Kills:</td><td><edit var=\"pvpkills\" width=50></td></tr>");
			replyMSG.append("</table><br>");
			replyMSG.append("<center><button value=\"Сохранить\" action=\"bypass -h admin_save_modifications $hp & $mp & $karma & $pvpflag & $pvpkills & $classid &\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center><br>");
			replyMSG.append("</body></html>");
		}		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private void showCharacterActions(Player activeChar)
	{
		GameObject target = activeChar.getTarget();
		Player player;
		if(target != null && target.isPlayer())
			player = (Player) target;
		else
			return;

		NpcHtmlMessagePacket adminReply = new NpcHtmlMessagePacket(5);
	
		StringBuilder replyMSG = new StringBuilder("<html><body>");
		if(!activeChar.isLangRus())
		{
			replyMSG.append("<table width=260><tr>");
			replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
			replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("</tr></table><br><br>");
			replyMSG.append("<center>Admin Actions for: " + player.getName() + "</center><br>");
			replyMSG.append("<center><table width=200><tr>");
			replyMSG.append("<td width=100>Argument(*):</td><td width=100><edit var=\"arg\" width=100></td>");
			replyMSG.append("</tr></table><br></center>");
			replyMSG.append("<table width=270>");

			replyMSG.append("<tr><td width=90><button value=\"Teleport\" action=\"bypass -h admin_teleportto " + player.getName() + "\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td width=90><button value=\"Recall\" action=\"bypass -h admin_recall " + player.getName() + "\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td width=90><button value=\"Quests\" action=\"bypass -h admin_quests " + player.getName() + "\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
		}
		else
		{
			replyMSG.append("<table width=260><tr>");
			replyMSG.append("<td width=40><button value=\"Главная\" action=\"bypass -h admin_admin\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td width=180><center>Меню Выбора Игрока</center></td>");
			replyMSG.append("<td width=40><button value=\"Назад\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("</tr></table><br><br>");
			replyMSG.append("<center>Действия админ для: " + player.getName() + "</center><br>");
			replyMSG.append("<center><table width=200><tr>");
			replyMSG.append("<td width=100>Аргумент(*):</td><td width=100><edit var=\"arg\" width=100></td>");
			replyMSG.append("</tr></table><br></center>");
			replyMSG.append("<table width=270>");

			replyMSG.append("<tr><td width=90><button value=\"Телепорт\" action=\"bypass -h admin_teleportto " + player.getName() + "\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td width=90><button value=\"Призвать\" action=\"bypass -h admin_recall " + player.getName() + "\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td width=90><button value=\"Квесты\" action=\"bypass -h admin_quests " + player.getName() + "\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
		}		
		replyMSG.append("</body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private void findCharacter(Player activeChar, String CharacterToFind)
	{
		NpcHtmlMessagePacket adminReply = new NpcHtmlMessagePacket(5);
		int CharactersFound = 0;

		StringBuilder replyMSG = new StringBuilder("<html><body>");
		if(!activeChar.isLangRus())
		{
			replyMSG.append("<table width=260><tr>");
			replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
			replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_show_characters 0\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("</tr></table>");
			replyMSG.append("<br><br>");

			for(Player element : GameObjectsStorage.getAllPlayersForIterate())
			{
				String nameLower = element.getName().toLowerCase();
				if(nameLower.startsWith(CharacterToFind.toLowerCase()))
				{
					CharactersFound = CharactersFound + 1;
					replyMSG.append("<table width=270>");
					replyMSG.append("<tr><td width=80>Name</td><td width=110>Class</td><td width=40>Level</td></tr>");
					replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_character_list " + element.getName() + "\">" + element.getName() + "</a></td><td width=110>" + HtmlUtils.htmlClassName(element.getClassId().getId()) + "</td><td width=40>" + element.getLevel() + "</td></tr>");
					replyMSG.append("</table>");
				}
			}
			if(CharactersFound == 0)
			{
				replyMSG.append("<table width=270>");
				replyMSG.append("<tr><td width=270>Your search did not find any characters.</td></tr>");
				replyMSG.append("<tr><td width=270>Please try again.<br></td></tr>");
				replyMSG.append("</table><br>");
				replyMSG.append("<center><table><tr><td>");
				replyMSG.append("<edit var=\"character_name\" width=80></td><td><button value=\"Find\" action=\"bypass -h admin_find_character $character_name\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
				replyMSG.append("</td></tr></table></center>");
			}
			else
			{
				replyMSG.append("<center><br>Found " + CharactersFound + " character");

				if(CharactersFound == 1)
					replyMSG.append(".");
				else if(CharactersFound > 1)
					replyMSG.append("s.");
			}
		}
		else
		{
			replyMSG.append("<table width=260><tr>");
			replyMSG.append("<td width=40><button value=\"Главная\" action=\"bypass -h admin_admin\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td width=180><center>Меню Выбора Игрока</center></td>");
			replyMSG.append("<td width=40><button value=\"Назад\" action=\"bypass -h admin_show_characters 0\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("</tr></table>");
			replyMSG.append("<br><br>");

			for(Player element : GameObjectsStorage.getAllPlayersForIterate())
			{
				String nameLower = element.getName().toLowerCase();
				if(nameLower.startsWith(CharacterToFind.toLowerCase()))
				{
					CharactersFound = CharactersFound + 1;
					replyMSG.append("<table width=270>");
					replyMSG.append("<tr><td width=80>Имя</td><td width=110>Class</td><td width=40>Level</td></tr>");
					replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_character_list " + element.getName() + "\">" + element.getName() + "</a></td><td width=110>" + HtmlUtils.htmlClassName(element.getClassId().getId()) + "</td><td width=40>" + element.getLevel() + "</td></tr>");
					replyMSG.append("</table>");
				}
			}
			if(CharactersFound == 0)
			{
				replyMSG.append("<table width=270>");
				replyMSG.append("<tr><td width=270>Ваш поиск не принес результатов.</td></tr>");
				replyMSG.append("<tr><td width=270>Попробуйте еще раз.<br></td></tr>");
				replyMSG.append("</table><br>");
				replyMSG.append("<center><table><tr><td>");
				replyMSG.append("<edit var=\"character_name\" width=80></td><td><button value=\"Найти\" action=\"bypass -h admin_find_character $character_name\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
				replyMSG.append("</td></tr></table></center>");
			}
			else
			{
				replyMSG.append("<center><br>Нашлись " + CharactersFound + " Персонажей");

				if(CharactersFound == 1)
					replyMSG.append(".");
				else if(CharactersFound > 1)
					replyMSG.append("s.");
			}
		}		
		replyMSG.append("</center></body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private void addExpSp(final Player activeChar)
	{
		final GameObject target = activeChar.getTarget();
		Player player;
		if(target != null && target.isPlayer() && (activeChar == target || activeChar.getPlayerAccess().CanEditCharAll))
			player = (Player) target;
		else
		{
			activeChar.sendPacket(Msg.INVALID_TARGET);
			return;
		}

		final NpcHtmlMessagePacket adminReply = new NpcHtmlMessagePacket(5);

		final StringBuilder replyMSG = new StringBuilder("<html><body>");
		if(!activeChar.isLangRus())
		{
			replyMSG.append("<table width=260><tr>");
			replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
			replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("</tr></table>");
			replyMSG.append("<br><br>");
			replyMSG.append("<table width=270><tr><td>Name: " + player.getName() + "</td></tr>");
			replyMSG.append("<tr><td>Lv: " + player.getLevel() + " " + HtmlUtils.htmlClassName(player.getClassId().getId()) + "</td></tr>");
			replyMSG.append("<tr><td>Exp: " + player.getExp() + "</td></tr>");
			replyMSG.append("<tr><td>Sp: " + player.getSp() + "</td></tr></table>");
			replyMSG.append("<br><table width=270><tr><td>Note: Dont forget that modifying players skills can</td></tr>");
			replyMSG.append("<tr><td>ruin the game...</td></tr></table><br>");
			replyMSG.append("<table width=270><tr><td>Note: Fill all values before saving the modifications.,</td></tr>");
			replyMSG.append("<tr><td>Note: Use 0 if no changes are needed.</td></tr></table><br>");
			replyMSG.append("<center><table><tr>");
			replyMSG.append("<td>Exp: <edit var=\"exp_to_add\" width=50></td>");
			replyMSG.append("<td>Sp:  <edit var=\"sp_to_add\" width=50></td>");
			replyMSG.append("<td>&nbsp;<button value=\"Save Changes\" action=\"bypass -h admin_add_exp_sp $exp_to_add $sp_to_add\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("</tr></table></center>");
			replyMSG.append("</body></html>");
		}
		else
		{
			replyMSG.append("<table width=260><tr>");
			replyMSG.append("<td width=40><button value=\"Главная\" action=\"bypass -h admin_admin\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td width=180><center>Меню Выбора Игрока</center></td>");
			replyMSG.append("<td width=40><button value=\"Назад\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("</tr></table>");
			replyMSG.append("<br><br>");
			replyMSG.append("<table width=270><tr><td>Имя: " + player.getName() + "</td></tr>");
			replyMSG.append("<tr><td>Ур: " + player.getLevel() + " " + HtmlUtils.htmlClassName(player.getClassId().getId()) + "</td></tr>");
			replyMSG.append("<tr><td>Exp: " + player.getExp() + "</td></tr>");
			replyMSG.append("<tr><td>Sp: " + player.getSp() + "</td></tr></table>");
			replyMSG.append("<br><table width=270><tr><td>Напоминание что изменения навыков может</td></tr>");
			replyMSG.append("<tr><td>Испортить игру...</td></tr></table><br>");
			replyMSG.append("<table width=270><tr><td>Напоминание: Заполните все поля для изменения.,</td></tr>");
			replyMSG.append("<tr><td>Напоминание: Используйте 0 где нужно.</td></tr></table><br>");
			replyMSG.append("<center><table><tr>");
			replyMSG.append("<td>Exp: <edit var=\"exp_to_add\" width=50></td>");
			replyMSG.append("<td>Sp:  <edit var=\"sp_to_add\" width=50></td>");
			replyMSG.append("<td>&nbsp;<button value=\"Сохранить\" action=\"bypass -h admin_add_exp_sp $exp_to_add $sp_to_add\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("</tr></table></center>");
			replyMSG.append("</body></html>");
		}		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private void adminAddExpSp(Player activeChar, long exp, int sp)
	{
		if(!activeChar.getPlayerAccess().CanEditCharAll)
		{
			activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.NotEnoughPrivs", activeChar));
			return;
		}

		final GameObject target = activeChar.getTarget();
		if(target == null)
		{
			activeChar.sendPacket(Msg.SELECT_TARGET);
			return;
		}

		if(!target.isPlayable())
		{
			activeChar.sendPacket(Msg.INVALID_TARGET);
			return;
		}

		Playable playable = (Playable) target;
		playable.addExpAndSp(exp, sp);
		activeChar.sendMessage(new CustomMessage("common.Admin.EditChar.AddExpSpSuccess", activeChar).addNumber(exp).addNumber(sp).addString(playable.getName()));
	}

	private void setSubclass(final Player activeChar, final Player player)
	{
		StringBuilder content = new StringBuilder("<html><body>");
		NpcHtmlMessagePacket html = new NpcHtmlMessagePacket(5);
		Set<ClassId> subsAvailable = SubClassInfo.getAvailableSubClasses(player, null, null, true);

		if(subsAvailable != null && !subsAvailable.isEmpty())
		{
			content.append("Add Subclass:<br>Which subclass do you wish to add?<br>");

			for(ClassId subClass : subsAvailable)
				content.append("<a action=\"bypass -h admin_setsubclass " + subClass.getId() + "\">" + formatClassForDisplay(subClass) + "</a><br>");
		}
		else
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.model.instances.L2VillageMasterInstance.NoSubAtThisTime", activeChar));
			return;
		}
		content.append("</body></html>");
		html.setHtml(content.toString());
		activeChar.sendPacket(html);
	}

	private String formatClassForDisplay(ClassId className)
	{
		String classNameStr = className.toString();
		char[] charArray = classNameStr.toCharArray();

		for(int i = 1; i < charArray.length; i++)
			if(Character.isUpperCase(charArray[i]))
				classNameStr = classNameStr.substring(0, i) + " " + classNameStr.substring(i);

		return classNameStr;
	}
}
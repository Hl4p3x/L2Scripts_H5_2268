package l2s.gameserver.utils;

import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.instancemanager.CursedWeaponsManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.CustomMessage;

public final class AdminFunctions
{
	public final static Location JAIL_SPAWN = new Location(-114648, -249384, -2984);
	
	private AdminFunctions() {}

	public static boolean kick(String player, String reason)
	{
		Player plyr = World.getPlayer(player);
		if (plyr == null)
			return false;

		return kick(plyr, reason);
	}


	public static boolean kick(Player player, String reason)
	{
		if(Config.ALLOW_CURSED_WEAPONS && Config.DROP_CURSED_WEAPONS_ON_KICK)
			if(player.isCursedWeaponEquipped())
			{
				player.setPvpFlag(0);
				CursedWeaponsManager.getInstance().dropPlayer(player);
			}

		player.kick();

		return true;
	}

	public static String banChat(Player adminChar, String adminName, String charName, int val, String reason)
	{
		Player player = World.getPlayer(charName);

		if(player != null)
			charName = player.getName();
		else if(CharacterDAO.getInstance().getObjectIdByName(charName) == 0)
			return "Игрок " + charName + " не найден.";

		if((adminName == null || adminName.isEmpty()) && adminChar != null)
			adminName = adminChar.getName();

		if(reason == null || reason.isEmpty())
			reason = "не указана"; // if no args, then "не указана" default.

		String result, announce = null;
		if(val == 0) //unban
		{
			if(adminChar != null && !adminChar.getPlayerAccess().CanUnBanChat)
				return "Вы не имеете прав на снятие бана чата.";
			if(Config.BANCHAT_ANNOUNCE)
				announce = Config.BANCHAT_ANNOUNCE_NICK && adminName != null && !adminName.isEmpty() ? adminName + " снял бан чата с игрока " + charName + "." : "С игрока " + charName + " снят бан чата.";
			Log.add(adminName + " снял бан чата с игрока " + charName + ".", "banchat", adminChar);
			result = "Вы сняли бан чата с игрока " + charName + ".";
		}
		else if(val < 0)
		{
			if(adminChar != null && adminChar.getPlayerAccess().BanChatMaxValue > 0)
				return "Вы можете банить не более чем на " + adminChar.getPlayerAccess().BanChatMaxValue + " минут.";
			if(Config.BANCHAT_ANNOUNCE)
				announce = Config.BANCHAT_ANNOUNCE_NICK && adminName != null && !adminName.isEmpty() ? adminName + " забанил чат игроку " + charName + " на бессрочный период, причина: " + reason + "." : "Забанен чат игроку " + charName + " на бессрочный период, причина: " + reason + ".";
			Log.add(adminName + " забанил чат игроку " + charName + " на бессрочный период, причина: " + reason + ".", "banchat", adminChar);
			result = "Вы забанили чат игроку " + charName + " на бессрочный период.";
		}
		else
		{
			if(adminChar != null && !adminChar.getPlayerAccess().CanUnBanChat && (player == null || player.getNoChannel() != 0))
				return "Вы не имеете права изменять время бана.";
			if(adminChar != null && adminChar.getPlayerAccess().BanChatMaxValue != -1 && val > adminChar.getPlayerAccess().BanChatMaxValue)
				return "Вы можете банить не более чем на " + adminChar.getPlayerAccess().BanChatMaxValue + " минут.";
			if(Config.BANCHAT_ANNOUNCE)
				announce = Config.BANCHAT_ANNOUNCE_NICK && adminName != null && !adminName.isEmpty() ? adminName + " забанил чат игроку " + charName + " на " + val + " минут, причина: " + reason + "." : "Забанен чат игроку " + charName + " на " + val + " минут, причина: " + reason + ".";
			Log.add(adminName + " забанил чат игроку " + charName + " на " + val + " минут, причина: " + reason + ".", "banchat", adminChar);
			result = "Вы забанили чат игроку " + charName + " на " + val + " минут.";
		}

		if(player != null)
			updateNoChannel(player, val, reason);
		else
			AutoBan.ChatBan(charName, val, reason, adminName);

		if(announce != null)
			if(Config.BANCHAT_ANNOUNCE_FOR_ALL_WORLD)
				Announcements.getInstance().announceToAll(announce);
			else
				Announcements.shout(adminChar, announce, ChatType.CRITICAL_ANNOUNCE);

		return result;
	}

	private static void updateNoChannel(Player player, int time, String reason)
	{
		player.updateNoChannel(time * 60000);
		if(time == 0)
			player.sendMessage(new CustomMessage("common.ChatUnBanned", player));
		else if(time > 0)
		{
			if(reason == null || reason.isEmpty())
				player.sendMessage(new CustomMessage("common.ChatBanned", player).addNumber(time));
			else
				player.sendMessage(new CustomMessage("common.ChatBannedWithReason", player).addNumber(time).addString(reason));
		}
		else if(reason == null || reason.isEmpty())
			player.sendMessage(new CustomMessage("common.ChatBannedPermanently", player));
		else
			player.sendMessage(new CustomMessage("common.ChatBannedPermanentlyWithReason", player).addString(reason));
	}
}

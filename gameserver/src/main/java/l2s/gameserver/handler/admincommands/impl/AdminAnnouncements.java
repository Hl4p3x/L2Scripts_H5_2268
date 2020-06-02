package l2s.gameserver.handler.admincommands.impl;

import java.util.List;

import l2s.gameserver.Announcements;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;


/**
 * This class handles following admin commands: - announce text = announces text
 * to all players - list_announcements = show menu - reload_announcements =
 * reloads announcements from txt file - announce_announcements = announce all
 * stored announcements to all players - add_announcement text = adds text to
 * startup announcements - del_announcement id = deletes announcement with
 * respective id
 */
public class AdminAnnouncements implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_list_announcements,
		admin_announce_announcements,
		admin_add_announcement,
		admin_del_announcement,
		admin_announce,
		admin_a,
		admin_announce_menu,
		admin_crit_announce,
		admin_c,
		admin_toscreen,
		admin_s,
		admin_s_new,
		admin_reload_announcements
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().CanAnnounce)
			return false;

		switch(command)
		{
			case admin_list_announcements:
				listAnnouncements(activeChar);
				break;
			case admin_announce_menu:
				Announcements.getInstance().announceToAll(fullString.substring(20));
				listAnnouncements(activeChar);
				break;
			case admin_announce_announcements:
				for(Player player : GameObjectsStorage.getAllPlayersForIterate())
					Announcements.getInstance().showAnnouncements(player);
				listAnnouncements(activeChar);
				break;
			case admin_add_announcement:
				if(wordList.length < 3)
					return false;
				try
				{
					int time = Integer.parseInt(wordList[1]);
					StringBuilder builder = new StringBuilder();
					for(int i = 2; i < wordList.length; i++)
						builder.append(" ").append(wordList[i]);

					Announcements.getInstance().addAnnouncement(time, builder.toString(), true);
					listAnnouncements(activeChar);
				}
				catch(Exception e)
				{}
				break;
			case admin_del_announcement:
				if(wordList.length != 2)
					return false;
				int val = Integer.parseInt(wordList[1]);
				Announcements.getInstance().delAnnouncement(val);
				listAnnouncements(activeChar);
				break;
			case admin_announce:
				String name = activeChar.getName();
				Announcements.getInstance().announceToAll(fullString.substring(15)+ " ("+name+")");
				break;
			case admin_a:
				Announcements.getInstance().announceToAll(fullString.substring(8));
				break;
			case admin_crit_announce:
			case admin_c:
				if(wordList.length < 2)
					return false;
				Announcements.getInstance().announceToAll(activeChar.getName() + ": " + fullString.replaceFirst("admin_crit_announce ", "").replaceFirst("admin_c ", ""), ChatType.CRITICAL_ANNOUNCE);
				break;
			case admin_toscreen:
			case admin_s:
				if(wordList.length < 2)
					return false;
				String text = activeChar.getName() + ": " + fullString.replaceFirst("admin_toscreen ", "").replaceFirst("admin_s ", "");
				int time = 3000 + text.length() * 100; // 3 секунды + 100мс на символ
				ExShowScreenMessage sm = new ExShowScreenMessage(NpcString.NONE, time, ScreenMessageAlign.TOP_CENTER, text.length() < 64, text);
				for(Player player : GameObjectsStorage.getAllPlayersForIterate())
					player.sendPacket(sm);
				break;
			case admin_s_new:
				if(wordList.length < 2)
					return false;
				String text1 = activeChar.getName() + ": " + fullString.replaceFirst("admin_s_new ", "");
				@SuppressWarnings("unused")
				int time1 = 100 + text1.length() * 100; // 100ms + 3 seconds per character
				ExShowScreenMessage sm1 = new ExShowScreenMessage(NpcString.NONE, 500, ScreenMessageAlign.TOP_CENTER, text1.length() < 64, text1 + " icon.skill0000");
				for(Player player : GameObjectsStorage.getAllPlayersForIterate())
					player.sendPacket(sm1);
				break;
			case admin_reload_announcements:
				Announcements.getInstance().loadAnnouncements();
				listAnnouncements(activeChar);
				activeChar.sendMessage(new CustomMessage("common.Admin.ReloadAnnounce", activeChar));
				break;
		}

		return true;
	}

	public void listAnnouncements(Player activeChar)
	{
		List<Announcements.Announce> announcements = Announcements.getInstance().getAnnouncements();

		NpcHtmlMessagePacket adminReply = new NpcHtmlMessagePacket(5);

		StringBuilder replyMSG = new StringBuilder("<html><body>");
		if(!activeChar.isLangRus())
		{
			replyMSG.append("<table width=260><tr>");
			replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td width=180><center>Announcement Menu</center></td>");
			replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("</tr></table>");
			replyMSG.append("<br><br>");
			replyMSG.append("<center>Add or announce a new announcement:</center>");
			replyMSG.append("<center><multiedit var=\"new_announcement\" width=240 height=30></center><br>");
			replyMSG.append("<center>Time(in seconds, 0 - only for start)<edit var=\"time\" width=40 height=20></center><br>");
			replyMSG.append("<center><table><tr><td>");
			replyMSG.append("<button value=\"Add\" action=\"bypass -h admin_add_announcement $time $new_announcement\" width=60 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
			replyMSG.append("<button value=\"Announce\" action=\"bypass -h admin_announce_menu $new_announcement\" width=64 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
			replyMSG.append("<button value=\"Reload\" action=\"bypass -h admin_reload_announcements\" width=60 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
			replyMSG.append("<button value=\"Broadcast\" action=\"bypass -h admin_announce_announcements\" width=70 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
			replyMSG.append("</td></tr></table></center>");
			replyMSG.append("<br>");
		}
		else
		{
			replyMSG.append("<table width=260><tr>");
			replyMSG.append("<td width=40><button value=\"Главная\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td width=180><center>Меню Анонсов</center></td>");
			replyMSG.append("<td width=40><button value=\"Назад\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("</tr></table>");
			replyMSG.append("<br><br>");
			replyMSG.append("<center>Добавить новый анонс:</center>");
			replyMSG.append("<center><multiedit var=\"new_announcement\" width=240 height=30></center><br>");
			replyMSG.append("<center>Time(В сек, 0 - только при заходе в игру)<edit var=\"time\" width=40 height=20></center><br>");
			replyMSG.append("<center><table><tr><td>");
			replyMSG.append("<button value=\"Добавить\" action=\"bypass -h admin_add_announcement $time $new_announcement\" width=60 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
			replyMSG.append("<button value=\"Анонс\" action=\"bypass -h admin_announce_menu $new_announcement\" width=64 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
			replyMSG.append("<button value=\"Обновить\" action=\"bypass -h admin_reload_announcements\" width=60 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
			replyMSG.append("<button value=\"Все\" action=\"bypass -h admin_announce_announcements\" width=70 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
			replyMSG.append("</td></tr></table></center>");
			replyMSG.append("<br>");
		}		
		for(int i = 0; i < announcements.size(); i++)
		{
			Announcements.Announce announce = announcements.get(i);
			replyMSG.append("<table width=260><tr><td width=180>" + announce.getAnnounce() + "</td><td width=40>"+ announce.getTime() + "</td><<td width=40>");
			if(!activeChar.isLangRus())
				replyMSG.append("<button value=\"Delete\" action=\"bypass -h admin_del_announcement " + i + "\" width=60 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr></table>");
			else	
				replyMSG.append("<button value=\"Удалить\" action=\"bypass -h admin_del_announcement " + i + "\" width=60 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr></table>");
		}

		replyMSG.append("</body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}
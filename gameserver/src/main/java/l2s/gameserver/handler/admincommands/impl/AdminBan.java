package l2s.gameserver.handler.admincommands.impl;

import java.util.Collection;
import java.util.StringTokenizer;

import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.items.ManufactureItem;
import l2s.gameserver.model.items.TradeItem;
import l2s.gameserver.network.authcomm.AuthServerCommunication;
import l2s.gameserver.network.authcomm.gs2as.ChangeAccessLevel;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;
import l2s.gameserver.security.HWIDBan;
import l2s.gameserver.utils.AutoBan;
import l2s.gameserver.utils.Log;

public class AdminBan implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_ban,
		admin_unban,
		admin_cban,
		admin_chatban,
		admin_chatunban,
		admin_accban,
		admin_accunban,
		admin_accban_hwid,
		admin_accunban_hwid,
		admin_trade_ban,
		admin_trade_unban,
		admin_jail,
		admin_unjail,
		admin_permaban
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		StringTokenizer st = new StringTokenizer(fullString);

		if(activeChar.getPlayerAccess().CanTradeBanUnban)
			switch(command)
			{
				case admin_trade_ban:
					return tradeBan(st, activeChar);
				case admin_trade_unban:
					return tradeUnban(st, activeChar);
			}

		if(activeChar.getPlayerAccess().CanBan)
			switch(command)
			{
				case admin_ban:
					ban(st, activeChar);
					break;
				case admin_accban:
				{
					st.nextToken();

					int level = 0;
					int banExpire = 0;

					String account = st.nextToken();

					if(st.hasMoreTokens())
						banExpire = (int) (System.currentTimeMillis() / 1000L) + Integer.parseInt(st.nextToken()) * 60;
					else
						level = -100;

					AuthServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(account, level, banExpire));
					GameClient client = AuthServerCommunication.getInstance().getAuthedClient(account);
					if(client != null)
					{
						Player player = client.getActiveChar();
						if(player != null)
						{
							player.kick();
							activeChar.sendMessage(new CustomMessage("common.Admin.Ban.Kicked", activeChar).addString(player.getName()));
						}
					}
					Log.LogEvent(activeChar.getName(), activeChar.getIP(), "AdminCommands", "Account Ban: banned account: "+account+" for "+banExpire+" days");	
					break;
				}
				case admin_accban_hwid:
				{
					st.nextToken();

					int level = 0;
					int banExpire = 0;

					String account = st.nextToken();

					if(st.hasMoreTokens())
						banExpire = (int) (System.currentTimeMillis() / 1000L) + Integer.parseInt(st.nextToken()) * 60;
					else
						level = -100;

					AuthServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(account, level, banExpire));
					GameClient client = AuthServerCommunication.getInstance().getAuthedClient(account);
					if(client != null)
					{
						Player player = client.getActiveChar();
						if(player != null)
						{
							HWIDBan.addBlackList(client.getHWID());
							player.kick();
							activeChar.sendMessage(new CustomMessage("common.Admin.Ban.Kicked", activeChar).addString(player.getName()));
						}
					}
					Log.LogEvent(activeChar.getName(), activeChar.getIP(), "AdminCommands", "HWID Ban: banned account: "+account+" for "+banExpire+" days");	
					break;
				}
				case admin_accunban:
				{
					st.nextToken();
					String account = st.nextToken();
					AuthServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(account, 0, 0));
					Log.LogEvent(activeChar.getName(), activeChar.getIP(), "AdminCommands", "Account UnBan: unbanned account: "+account+"");
					break;
				}
				case admin_trade_ban:
					return tradeBan(st, activeChar);
				case admin_trade_unban:
					return tradeUnban(st, activeChar);
				case admin_chatban:
					try
					{
						st.nextToken();
						String player = st.nextToken();
						String period = st.nextToken();
						String bmsg = "admin_chatban " + player + " " + period + " ";
						String msg = fullString.substring(bmsg.length(), fullString.length());

						if(AutoBan.ChatBan(player, Integer.parseInt(period), msg, activeChar.getName()))
						{
							activeChar.sendMessage(new CustomMessage("common.Admin.Ban.BanChat", activeChar).addString(player));
							Log.LogEvent(activeChar.getName(), activeChar.getIP(), "AdminCommands", "Chat Ban: banned chat to: "+player+" for "+period+" minutes");
						}
						else
							activeChar.sendMessage(new CustomMessage("common.Admin.Ban.NotFound", activeChar).addString(player));
					}
					catch(Exception e)
					{
						activeChar.sendMessage(new CustomMessage("common.Admin.Ban.BanChatUsage", activeChar));
					}
					break;
				case admin_unban:
					st.nextToken();
					String name = st.nextToken();
					unbanChar(activeChar, name);
					break;
				case admin_chatunban:
					try
					{
						st.nextToken();
						String player = st.nextToken();

						if(AutoBan.ChatUnBan(player, activeChar.getName()))
						{
							activeChar.sendMessage(new CustomMessage("common.Admin.Ban.UnBanChat", activeChar).addString(player));
							Log.LogEvent(activeChar.getName(), activeChar.getIP(), "AdminCommands", "Chat UnBan: unbanned chat to: "+player+"");
						}

						else
							activeChar.sendMessage(new CustomMessage("common.Admin.Ban.NotFound", activeChar).addString(player));
					}
					catch(Exception e)
					{
						activeChar.sendMessage(new CustomMessage("common.Admin.Ban.UnBanChatUsage", activeChar));
					}
					break;
				case admin_jail:
					try
					{
						st.nextToken();
						String player = st.nextToken();
						String period = st.nextToken();
						String reason = st.nextToken();

						Player target = World.getPlayer(player);

						if(target != null)
						{
							target.toJail(Integer.parseInt(period));
							target.sendMessage("You moved to jail, time to escape - " + period + " minutes, reason - " + reason + " .");
							activeChar.sendMessage("You jailed " + player + ".");
							Announcements.getInstance().announceToAll("Player " + target.getName() + " jailed by " + activeChar.getName() + " reason: " + reason);
							Log.LogEvent(activeChar.getName(), activeChar.getIP(), "AdminCommands", "Jail: jailed : "+target.getName()+" for "+period+" minutes");
						}
						else
							activeChar.sendMessage(new CustomMessage("common.Admin.Ban.NotFound", activeChar).addString(player));
					}
					catch(Exception e)
					{
						activeChar.sendMessage(new CustomMessage("common.Admin.Jail.JailUssage", activeChar));
					}
					break;
				case admin_unjail:
					try
					{
						st.nextToken();
						String player = st.nextToken();

						Player target = World.getPlayer(player);

						if(target != null && target.getVar("jailed") != null)
						{
							activeChar.fromJail();
							activeChar.sendMessage("You unjailed " + player + ".");
							Announcements.getInstance().announceToAll("Player " + target.getName() + " unjailed by " + activeChar.getName());
							Log.LogEvent(activeChar.getName(), activeChar.getIP(), "AdminCommands", "UnJail: unjailed : "+target.getName()+"");
						}
						else
							activeChar.sendMessage(new CustomMessage("common.Admin.Ban.NotFound", activeChar).addString(player));
					}
					catch(Exception e)
					{
						activeChar.sendMessage(new CustomMessage("common.Admin.Jail.UnJailUssage", activeChar));
					}
					break;
				case admin_cban:
					activeChar.sendPacket(new NpcHtmlMessagePacket(5).setFile("admin/cban.htm"));
					break;
				case admin_permaban:
					if(activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
					{
						activeChar.sendMessage(new CustomMessage("common.Admin.Ban.BanError", activeChar));
						return false;
					}
					Player banned = activeChar.getTarget().getPlayer();
					String banaccount = banned.getAccountName();
					AuthServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(banaccount, -100, 0));
					if(banned.isInOfflineMode())
						banned.setOfflineMode(false);
					banned.kick();
					activeChar.sendMessage(new CustomMessage("common.Admin.Ban.BanMessage", activeChar).addString(banaccount).addString(banned.getName()));
					Log.LogEvent(activeChar.getName(), activeChar.getIP(), "AdminCommands", "BanForEver: baned account : "+banaccount+" PERNAMENT");
					break;
			}

		return true;
	}

	private boolean tradeBan(StringTokenizer st, Player activeChar)
	{
		if(activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
			return false;
		st.nextToken();
		Player targ = (Player) activeChar.getTarget();
		long days = -1;
		long time = -1;
		if(st.hasMoreTokens())
		{
			days = Long.parseLong(st.nextToken());
			time = days * 24 * 60 * 60 * 1000L + System.currentTimeMillis();
		}
		targ.setVar("tradeBan", String.valueOf(time), -1);
		String msg = activeChar.getName() + " заблокировал торговлю персонажу " + targ.getName() + (days == -1 ? " на бессрочный период." : " на " + days + " дней.");

		Log.LogEvent(activeChar.getName(), activeChar.getIP(), "AdminCommands", "Trade Ban: banned player: "+targ.getName()+" for "+days+" days");	

		Log.add(targ.getName() + ":" + days + tradeToString(targ, targ.getPrivateStoreType()), "tradeBan", activeChar);

		if(targ.isInOfflineMode())
		{
			targ.setOfflineMode(false);
			targ.kick();
		}
		else if(targ.isInStoreMode())
		{
			targ.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
			targ.standUp();
			targ.broadcastCharInfo();
			targ.getBuyList().clear();
		}

		if(Config.BANCHAT_ANNOUNCE_FOR_ALL_WORLD)
			Announcements.getInstance().announceToAll(msg);
		else
			Announcements.shout(activeChar, msg, ChatType.CRITICAL_ANNOUNCE);
		return true;
	}

	@SuppressWarnings("unchecked")
	private static String tradeToString(Player targ, int trade)
	{
		String ret;
		switch(trade)
		{
			case Player.STORE_PRIVATE_BUY:
				Collection<TradeItem> buyList = targ.getBuyList();
				if(buyList == null || buyList.isEmpty())
					return "";
				ret = ":buy:";
				for(TradeItem i : buyList)
					ret += i.getItemId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ":";
				return ret;
			case Player.STORE_PRIVATE_SELL:
			case Player.STORE_PRIVATE_SELL_PACKAGE:
				Collection<TradeItem> sellList = targ.getSellList();
				if(sellList == null || sellList.isEmpty())
					return "";
				ret = ":sell:";
				for(TradeItem i : sellList)
					ret += i.getItemId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ":";
				return ret;
			case Player.STORE_PRIVATE_MANUFACTURE:
				Collection<ManufactureItem> createList = targ.getCreateList();
				if(createList == null || createList.isEmpty())
					return "";
				ret = ":mf:";
				for(ManufactureItem i : createList)
					ret += i.getRecipeId() + ";" + i.getCost() + ":";
				return ret;
			default:
				return "";
		}
	}

	private boolean tradeUnban(StringTokenizer st, Player activeChar)
	{
		if(activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
			return false;
		Player targ = (Player) activeChar.getTarget();

		targ.unsetVar("tradeBan");

		if(Config.BANCHAT_ANNOUNCE_FOR_ALL_WORLD)
			Announcements.getInstance().announceToAll(activeChar + " разблокировал торговлю персонажу " + targ + ".");
		else
			Announcements.shout(activeChar, activeChar + " разблокировал торговлю персонажу " + targ + ".", ChatType.CRITICAL_ANNOUNCE);

		Log.add(activeChar + " разблокировал торговлю персонажу " + targ + ".", "tradeBan", activeChar);
		return true;
	}

	private boolean ban(StringTokenizer st, Player activeChar)
	{
		try
		{
			st.nextToken();

			String player = st.nextToken();

			int time = 0;
			String msg = "";

			if(st.hasMoreTokens())
				time = Integer.parseInt(st.nextToken());

			if(st.hasMoreTokens())
			{
				msg = "admin_ban " + player + " " + time + " ";
				while(st.hasMoreTokens())
					msg += st.nextToken() + " ";
				msg.trim();
			}

			Player plyr = World.getPlayer(player);
			if(plyr != null)
			{
				plyr.sendMessage(new CustomMessage("admincommandhandlers.YoureBannedByGM", plyr));
				plyr.setAccessLevel(-100);
				AutoBan.Banned(plyr, time, msg, activeChar.getName());
				plyr.kick();
				activeChar.sendMessage("You banned " + plyr.getName());
			}
			else if(AutoBan.Banned(player, -100, time, msg, activeChar.getName()))
			{
				activeChar.sendMessage(new CustomMessage("common.Admin.Ban.BanPlayer", activeChar).addString(player));
				Log.LogEvent(activeChar.getName(), activeChar.getIP(), "AdminCommands", "BanForEver: baned account : "+plyr.getName()+" for "+time+" minutes");
			}
			else
				activeChar.sendMessage(new CustomMessage("common.Admin.Ban.NotFound", activeChar).addString(player));
		}
		catch(Exception e)
		{
			activeChar.sendMessage(new CustomMessage("common.Admin.Ban.BanUssage", activeChar));
		}
		return true;
	}
	
	public void unbanChar(Player gm, String name)
	{
		if(AutoBan.Banned(name, 0, 0, "unbaned cha", gm.getName()))
			gm.sendMessage("Unbanned player " + name);
		else
			gm.sendMessage("Player cannot unbaned " + name);
	}
	
	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}
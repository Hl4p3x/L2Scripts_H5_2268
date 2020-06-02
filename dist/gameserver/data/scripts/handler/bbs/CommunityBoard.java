package handler.bbs;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import handler.bbs.custom.BBSConfig;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.handler.bbs.CommunityBoardManager;
import l2s.gameserver.handler.bbs.ICommunityBoardHandler;
import l2s.gameserver.handler.bypass.BypassHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.fightclubmanager.FightClubEventManager;
import l2s.gameserver.model.entity.events.impl.AbstractFightClub;
import l2s.gameserver.network.l2.s2c.ShowBoardPacket;
import l2s.gameserver.scripts.Scripts;

public final class CommunityBoard extends ScriptsCommunityHandler
{
	private static final Logger _log = LoggerFactory.getLogger(CommunityBoard.class);

	@Override
	public void onLoad()
	{
		super.onLoad();

		if(Config.BBS_ENABLED)
			_log.info("CommunityBoard: service loaded.");
	}

	@Override
	public String[] getBypassCommands()
	{
		return new String[]
		{
			"_bbshome",
			"_bbslink",
			"_bbsmultisell",
			"_bbspage",
			"_bbsscripts",
			"_bbshtmbypass",
			"_bbseventregister"
		};
	}

	@Override
	protected void doBypassCommand(Player player, String bypass)
	{
		StringTokenizer st = new StringTokenizer(bypass, "_");
		String cmd = st.nextToken();
		String html = "";
		if("bbshome".equals(cmd))
		{
			StringTokenizer p = new StringTokenizer(Config.BBS_DEFAULT_PAGE, "_");
			String dafault = p.nextToken();
			if(dafault.equals(cmd))
			{
				html = HtmCache.getInstance().getHtml("scripts/handler/bbs/bbs_top.htm", player);

				int favCount = 0;
				Connection con = null;
				PreparedStatement statement = null;
				ResultSet rset = null;
				try
				{
					con = DatabaseFactory.getInstance().getConnection();
					statement = con.prepareStatement("SELECT count(*) as cnt FROM `bbs_favorites` WHERE `object_id` = ?");
					statement.setInt(1, player.getObjectId());
					rset = statement.executeQuery();
					if(rset.next())
						favCount = rset.getInt("cnt");
				}
				catch(Exception e)
				{}
				finally
				{
					DbUtils.closeQuietly(con, statement, rset);
				}

				html = html.replace("<?fav_count?>", String.valueOf(favCount));
				html = html.replace("<?clan_count?>", String.valueOf(CommunityClan.getClanList(null, false).size()));
				html = html.replace("<?market_count?>", String.valueOf(CommunityBoardManager.getInstance().getIntProperty("col_count")));
			}
			else
			{
				ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(Config.BBS_DEFAULT_PAGE);
				if(handler != null)
					handler.onBypassCommand(player, Config.BBS_DEFAULT_PAGE);
				return;
			}
		}
		else if("bbslink".equals(cmd))
			html = HtmCache.getInstance().getHtml("scripts/handler/bbs/bbs_homepage.htm", player);
		else if(bypass.startsWith("_bbspage"))
		{
			//Example: "bypass _bbspage:index".
			String[] b = bypass.split(":");
			String page = b[1];
			html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/" + page + ".htm", player);
		}
		else if(bypass.startsWith("_bbsmultisell"))
		{
			if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
			{
				onWrongCondition(player);
				return;
			}

			//Example: "_bbsmultisell:10000;_bbspage:index" or "_bbsmultisell:10000;_bbshome" or "_bbsmultisell:10000"...
			StringTokenizer st2 = new StringTokenizer(bypass, ";");
			String[] mBypass = st2.nextToken().split(":");
			String pBypass = st2.hasMoreTokens() ? st2.nextToken() : null;
			if(pBypass != null)
			{
				ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(pBypass);
				if(handler != null)
					handler.onBypassCommand(player, pBypass);
			}

			int listId = Integer.parseInt(mBypass[1]);
			MultiSellHolder.getInstance().SeparateAndSend(listId, player, 0);
			return;
		}
		else if(bypass.startsWith("_bbsscripts"))
		{
			//Example: "_bbsscripts:events.GvG.GvG:addGroup;_bbspage:index" or "_bbsscripts:events.GvG.GvG:addGroup;_bbshome" or "_bbsscripts:events.GvG.GvG:addGroup"...
			StringTokenizer st2 = new StringTokenizer(bypass, ";");
			String sBypass = st2.nextToken().substring(12);
			String pBypass = st2.hasMoreTokens() ? st2.nextToken() : null;
			if(pBypass != null)
			{
				ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(pBypass);
				if(handler != null)
					handler.onBypassCommand(player, pBypass);
			}

			String[] word = sBypass.split("\\s+");
			String[] args = sBypass.substring(word[0].length()).trim().split("\\s+");
			String[] path = word[0].split(":");
			if(path.length != 2)
				return;

			Scripts.getInstance().callScripts(player, path[0], path[1], word.length == 1 ? new Object[] {} : new Object[] { args });
			return;
		}
		else if(bypass.startsWith("_bbshtmbypass"))
		{
			StringTokenizer st2 = new StringTokenizer(bypass, ";");
			String command = st2.nextToken().substring(14);
			String pBypass = st2.hasMoreTokens() ? st2.nextToken() : null;
			if(pBypass != null)
			{
				ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(pBypass);
				if(handler != null)
					handler.onBypassCommand(player, pBypass);
			}

			String word = command.split("\\s+")[0];
			String args = command.substring(word.length()).trim();

			Pair<Object, Method> b = BypassHolder.getInstance().getBypass(word);
			if(b != null)
			{
				try
				{
					b.getValue().invoke(b.getKey(), player, null, StringUtils.isEmpty(args) ? new String[0] : args.split("\\s+"));
				}
				catch(Exception e)
				{
					_log.error("Exception: " + e, e);
				}
			}
			else
				_log.warn("Cannot find bbs html bypass: " + command);
			return;
		}
		else if(bypass.startsWith("_bbseventregister"))
		{
			if(!st.hasMoreTokens())
				return;

			int eventId = Integer.parseInt(st.nextToken());
			AbstractFightClub event = (AbstractFightClub) EventHolder.getInstance().getEvent(EventType.FIGHT_CLUB_EVENT, eventId);
			if(event == null)
				return;

			FightClubEventManager.getInstance().trySignForEvent(player, event, true);
		}

		ShowBoardPacket.separateAndSend(html, player);
	}

	@Override
	protected void doWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{}
}

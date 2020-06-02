package events.PcCafePointsExchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExPCCafePointInfoPacket;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.scripts.ScriptFile;

public class PcCafePointsExchange extends Functions implements ScriptFile
{
	private static final Logger _log = LoggerFactory.getLogger(PcCafePointsExchange.class);
	private static final String EVENT_NAME = "PcCafePointsExchange";

	private static boolean isActive()
	{
		return IsActive(EVENT_NAME);
	}

	public void startEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;

		if(SetActive(EVENT_NAME, true))
		{
			_log.info("Event: 'PcCafePointsExchange' started.");
			for(Player playerPCCafe : GameObjectsStorage.getAllPlayers())
				playerPCCafe.sendPacket(new ExPCCafePointInfoPacket(playerPCCafe, 0, 1, 2, 12));
		}
		else
			player.sendMessage("Event 'PcCafePointsExchange' already started.");

		show("admin/events/events.htm", player);
	}

	public void stopEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;
		if(SetActive(EVENT_NAME, false))
			_log.info("Event: 'PcCafePointsExchange' stopped.");
		else
			player.sendMessage("Event: 'PcCafePointsExchange' not started.");

		show("admin/events/events.htm", player);
	}

	@Override
	public void onLoad()
	{
		if(isActive())
			_log.info("Loaded Event: PcCafePointsExchange [state: activated]");
		else
			_log.info("Loaded Event: PcCafePointsExchange [state: deactivated]");
	}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{
		//clearPcCafeDatabase();
	}

	/*public void clearPcCafeDatabase()
	{
		Connection conPCCafe = null;
		PreparedStatement statementPCCafe = null;
		try
		{
			conPCCafe = DatabaseFactory.getInstance().getConnection();
			statementPCCafe = conPCCafe.prepareStatement("UPDATE characters SET pcBangPoints=0 WHERE pcBangPoints>=1");
			statementPCCafe.execute();
		}
		catch(Exception e)
		{
			_log.warn("clearPcCafeDatabase: " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(conPCCafe, statementPCCafe);
		}
	}*/

	public String DialogAppend_31775(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31776(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31777(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31778(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31779(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31780(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31781(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31782(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31783(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31784(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31785(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31786(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31787(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31788(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31789(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31790(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31791(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31792(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31793(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31794(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31795(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31796(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31797(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31798(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31799(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31800(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31801(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31802(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31803(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31804(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31805(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31806(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31807(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31808(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31809(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31810(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31811(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31812(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31813(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31814(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31815(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31816(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31817(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31818(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31819(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31820(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31821(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31822(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31823(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31824(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31825(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31826(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31827(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31828(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31829(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31830(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31831(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31832(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31833(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31834(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31835(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31836(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31837(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31838(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31839(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31840(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String DialogAppend_31841(Integer val)
	{
		return getHtmlAppends(val);
	}

	public String getHtmlAppends(Integer val)
	{
		String html = "";

		if(val == 2 && isActive())
		{
			Player player = getSelf();
			html += "<html noscrollbar>";
			html += "<title>Adventure Guildsman</title>";
			html += "<body>";
			html += "<table border=0 cellpadding=0 cellspacing=0 width=292 height=358 background=\"l2ui_ct1.Windows_DF_TooltipBG\">";
			html += "<tr><td valign=top align=center><br>";
			html += "<center>Приветствую, путник!</center><br1>";
			html += player.isLangRus() ? "В Lineage 2 была введена система очков РС клуба, которые накапливаются в течении времени нахождения в игре, с помощью РС очков можно получить различные предметы и прочие преимущества.<br1>" : " There was included PC Club system in Lineage 2 game. The PC Club scores is accumulating during character online in game. You can spend PC Club scores for buying some items and another goodies.<br1>";
			html += "<center>";
			html += player.isLangRus() ? "<br>[npc_%objectId%_Link adventurer_guildsman/pc_cafe/pc_shop.htm|<font color=\"LEVEL\">Ивент (Купоны РС Клуба)</font>]" : "<br>[npc_%objectId%_Link adventurer_guildsman/pc_cafe/pc_shop.htm|<font color=\"LEVEL\">Check the Player Commendation (PC) benefits.</font>]";
			html += player.isLangRus() ? "<br>[npc_%objectId%_pccafe_coupon_use|<font color=\"LEVEL\">Ввести Промо Код РС Клуба</font>]" : "<br>[npc_%objectId%_pccafe_coupon_use|<font color=\"LEVEL\">Enter Player Commendation (PC) Promo Code</font>]";
			html += "</center>";
			html += "</td></tr></table></body>";
			html += "</html>";
			return html;
		}
		else if(val == 2 && !isActive())
		{
			Player player = getSelf();
			html += "<html noscrollbar>";
			html += "<title>Adventure Guildsman</title>";
			html += "<body>";
			html += "<table border=0 cellpadding=0 cellspacing=0 width=292 height=358 background=\"l2ui_ct1.Windows_DF_TooltipBG\">";
			html += "<tr><td valign=top align=center><br>";
			html += "<center>Приветствую, путник!</center><br1>";
			html += player.isLangRus() ? "В Lineage 2 была введена система очков РС клуба, которые накапливаются в течении времени нахождения в игре, с помощью РС очков можно получить различные предметы и прочие преимущества.<br1>" : " There was included PC Club system in Lineage 2 game. The PC Club scores is accumulating during character online in game. You can spend PC Club scores for buying some items and another goodies.<br1>";
			html += "<center>";
			html += player.isLangRus() ? "<br>Ивент отключен" : "<br>Event disabled";
			html += "</center>";
			html += "</td></tr></table></body>";
			html += "</html>";
			html += "<br><center>-------------------</center>";
			return html;
		}
		else
			return "";
	}
}
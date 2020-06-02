package npc.model.events;

import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;

import l2s.gameserver.Config;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;

public class FightClubManagerInstance extends NpcInstance
{
	private static final long serialVersionUID = 1L;

	private static final String HTML_INDEX = "scripts/events/fightclub/index.htm";
	private static final String HTML_ACCEPT = "scripts/events/fightclub/accept.htm";
	private static final String HTML_MAKEBATTLE = "scripts/events/fightclub/makebattle.htm";
	private static final String HTML_INFO = "scripts/events/fightclub/info.htm";
	private static final String HTML_DISABLED = "scripts/events/fightclub/disabled.htm";
	private static final String HTML_LIST = "scripts/events/fightclub/fightslist.htm";
	private static final String HTML_RESULT = "scripts/events/fightclub/result.htm";

	public FightClubManagerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(!Config.FIGHT_CLUB_ENABLED)
		{
			player.sendPacket(new NpcHtmlMessagePacket(player, this, HtmCache.getInstance().getHtml(HTML_DISABLED, player), 0));
			return;
		}

		if(command.equalsIgnoreCase("index"))
		{
			player.sendPacket(new NpcHtmlMessagePacket(player, this, HtmCache.getInstance().getHtml(HTML_INDEX, player), 0));
		}

		else if(command.equalsIgnoreCase("makebattle"))
		{
			player.sendPacket(makeBattleHtml(player));
		}

		else if(command.equalsIgnoreCase("info"))
		{
			player.sendPacket(new NpcHtmlMessagePacket(player, this, HtmCache.getInstance().getHtml(HTML_INFO, player), 0));
		}

		else
		{
			final StringTokenizer st = new StringTokenizer(command, " ");
			String pageName = st.nextToken();
			if(pageName.equalsIgnoreCase("addbattle"))
			{
				int count = 0;
				try
				{
					count = Integer.parseInt(st.nextToken());
					if(count == 0)
					{
						sendResult(player, player.isLangRus() ? "Ошибка!" : "Error!", player.isLangRus() ? "Вы не ввели количество, или неправильное число." : "You did not enter the number or wrong number.");
						return;
					}
				}
				catch(NumberFormatException e)
				{
					sendResult(player, player.isLangRus() ? "Ошибка!" : "Error!", player.isLangRus() ? "Вы не ввели количество, или неправильное число." : "You did not enter the number or wrong number.");
					return;
				}
				String itemName = StringUtils.EMPTY;
				if(st.hasMoreTokens())
				{
					itemName = st.nextToken();
					while(st.hasMoreTokens())
						itemName += " " + st.nextToken();
				}
				Object[] objects = { player, itemName, count };
				final String respone = (String) callScripts("addApplication", objects);
				if("OK".equalsIgnoreCase(respone))
				{
					sendResult(player, player.isLangRus() ? "Выполнено!" : "Completed!", (player.isLangRus() ? "Вы создали заявку на участие.<br>Ваша ставка - <font color=\"LEVEL\">" : "You have created an application for participation.<br>Your bet - <font color=\"LEVEL\">") + String.valueOf(objects[2]) + " " + String.valueOf(objects[1]) + (player.isLangRus() ? "</font><br><center>Удачи!</center>" : "</font><br><center>Good luck!</center>"));
				}
				else if("NoItems".equalsIgnoreCase(respone))
				{
					sendResult(player, player.isLangRus() ? "Ошибка!" : "Error!", player.isLangRus() ? "У вас недостаточно или отсутствуют требующиеся предметы!" : "You are not required or missing items!");
					return;
				}
				else if("reg".equalsIgnoreCase(respone))
				{
					sendResult(player, player.isLangRus() ? "Ошибка!" : "Error!", player.isLangRus() ? "Вы уже зарегистрированы! Если вы хотите изменить ставку, удалите старую регистрацию." : "You are already registered! If you wish to bid, remove the old registration.");
					return;
				}
			}
			else if(pageName.equalsIgnoreCase("delete"))
			{
				Object[] playerObject = { player };
				if((Boolean) callScripts("isRegistered", playerObject))
				{
					callScripts("deleteRegistration", playerObject);
					sendResult(player, player.isLangRus() ? "Выполнено!" : "Completed!", player.isLangRus() ? "<center>Вы удалены из списка регистрации.</center>" : "<center>You are removed from the list of registration.</center>");
				}
				else
					sendResult(player, player.isLangRus() ? "Ошибка!" : "Error!", player.isLangRus() ? "<center>Вы не были зарегистрированы на участие.</center>" : "<center>You have not been registered to participate.</center>");
			}
			else if(pageName.equalsIgnoreCase("openpage"))
			{
				player.sendPacket(makeOpenPage(player, Integer.parseInt(st.nextToken())));
			}
			else if(pageName.equalsIgnoreCase("tryaccept"))
			{
				player.sendPacket(makeAcceptHtml(player, Long.parseLong(st.nextToken())));
			}
			else if(pageName.equalsIgnoreCase("accept"))
			{
				accept(player, Long.parseLong(st.nextToken()));
			}
		}
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		player.sendPacket(new NpcHtmlMessagePacket(player, this, HtmCache.getInstance().getHtml(HTML_INDEX, player), val));
	}

	private NpcHtmlMessagePacket makeOpenPage(Player player, int pageId)
	{
		NpcHtmlMessagePacket html = new NpcHtmlMessagePacket(player, this);
		html.setFile(HTML_LIST);

		StringBuilder sb = new StringBuilder();

		final int count = (Integer) callScripts("getRatesCount", new Object[0]);

		int num = pageId * Config.PLAYERS_PER_PAGE;
		if(num > count)
			num = count;
		if(count > 0)
		{
			sb.append("<table width=300>");

			for(int i = pageId * Config.PLAYERS_PER_PAGE - Config.PLAYERS_PER_PAGE; i < num; i++)
			{
				Object[] index = { i };
				Rate rate = (Rate) callScripts("getRateByIndex", index);
				sb.append("<tr>");
				sb.append("<td align=center width=95>");
				sb.append("<a action=\"bypass -h npc_%objectId%_tryaccept ").append(rate.getStoredId()).append("\">");
				sb.append("<font color=\"ffff00\">").append(rate.getPlayerName()).append("</font></a></td>");
				sb.append("<td align=center width=70>").append(rate.getPlayerLevel()).append("</td>");
				sb.append("<td align=center width=100><font color=\"ff0000\">");
				sb.append(rate.getPlayerClass().getName(player)).append("</font></td>");
				sb.append("<td align=center width=135><font color=\"00ff00\">");
				sb.append(rate.getItemCount()).append(" ").append(rate.getItemName());
				sb.append("</font></td></tr>");
			}

			sb.append("</table><br><br><br>");
			int pg = getPagesCount(count);
			sb.append("Страницы:&nbsp;");
			for(int i = 1; i <= pg; i++)
			{
				if(i == pageId)
					sb.append(i).append("&nbsp;");
				else
					sb.append("<a action=\"bypass -h npc_%objectId%_openpage ").append(i).append("\">").append(i).append("</a>&nbsp;");
			}
		}
		else
			sb.append(player.isLangRus() ? "<br><center>Ставок пока не сделано</center>" : "<br><center>Rates have not yet done</center>");
		html.replace("%data%", sb.toString());

		return html;
	}

	private NpcHtmlMessagePacket makeBattleHtml(Player player)
	{
		NpcHtmlMessagePacket html = new NpcHtmlMessagePacket(player, this);
		html.setFile(HTML_MAKEBATTLE);
		html.replace("%items%", (String) callScripts("getItemsList", new Object[0]));

		return html;
	}

	private NpcHtmlMessagePacket makeAcceptHtml(Player player, long storedId)
	{
		Object[] id = { storedId };
		Rate rate = (Rate) callScripts("getRateByStoredId", id);
		NpcHtmlMessagePacket html = new NpcHtmlMessagePacket(player, this);
		html.setFile(HTML_ACCEPT);
		html.replace("%name%", rate.getPlayerName());
		html.replace("%class%", rate.getPlayerClass().getName(player));
		html.replace("%level%", String.valueOf(rate.getPlayerLevel()));
		html.replace("%rate%", rate.getItemCount() + " " + rate.getItemName());
		html.replace("%storedId%", String.valueOf(rate.getStoredId()));
		return html;
	}

	private void accept(Player player, long storedId)
	{
		final Object[] data = { GameObjectsStorage.getAsPlayer(storedId), player };
		if(player.getStoredId() == storedId)
		{
			sendResult(player, player.isLangRus() ? "Ошибка!" : "Error!", player.isLangRus() ? "Вы не можете вызвать на бой самого себя." : "You can not call the fight itself.");
			return;
		}
		//TODO: Проверка на айтемы... Берем пример с doStart
		//if(ItemFunctions.getItemCount(player, _))
		if((Boolean) callScripts("requestConfirmation", data))
		{
			sendResult(player, player.isLangRus() ? "Внимание!" : "Attention!", player.isLangRus() ? "Вы отправили запрос сопернику. Если все условия соответствуют, Вас переместят на арену<br><center><font color=\"LEVEL\">Удачи!</font></center><br>" : "You have sent a request to the opponent. If all conditions match, you will move into the arena<br><center><font color=\"LEVEL\">Good luck!</font></center><br>");
		}
	}

	private void sendResult(Player player, String title, String text)
	{
		NpcHtmlMessagePacket html = new NpcHtmlMessagePacket(player, this);
		html.setFile(HtmCache.getInstance().getHtml(HTML_RESULT, player));
		html.replace("%title%", title);
		html.replace("%text%", text);
		player.sendPacket(html);
	}

	private int getPagesCount(int count)
	{
		if(count % Config.PLAYERS_PER_PAGE > 0)
			return count / Config.PLAYERS_PER_PAGE + 1;
		return count / Config.PLAYERS_PER_PAGE;
	}

	private Object callScripts(String methodName, Object[] args)
	{
		return Functions.callScripts("events.FightClub.FightClubManager", methodName, args);
	}

	public static class Rate
	{
		private String playerName;
		private int playerLevel;
		private ClassId playerClass;
		private int _itemId;
		private String itemName;
		private int _itemCount;
		private long playerStoredId;

		public Rate(Player player, int itemId, int itemCount)
		{
			playerName = player.getName();
			playerLevel = player.getLevel();
			playerClass = player.getClassId();
			_itemId = itemId;
			_itemCount = itemCount;
			itemName = ItemFunctions.createItem(itemId).getTemplate().getName();
			playerStoredId = player.getStoredId();
		}

		public String getPlayerName()
		{
			return playerName;
		}

		public int getPlayerLevel()
		{
			return playerLevel;
		}

		public ClassId getPlayerClass()
		{
			return playerClass;
		}

		public int getItemId()
		{
			return _itemId;
		}

		public int getItemCount()
		{
			return _itemCount;
		}

		public String getItemName()
		{
			return itemName;
		}

		public long getStoredId()
		{
			return playerStoredId;
		}
	}
}
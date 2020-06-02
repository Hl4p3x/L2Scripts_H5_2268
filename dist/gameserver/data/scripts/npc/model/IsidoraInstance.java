package npc.model;

import java.util.StringTokenizer;

import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;

public class IsidoraInstance extends NpcInstance
{
	private static final long serialVersionUID = 1L;
	private static final String HTML_OLD = "scripts/services/36617-old.htm";
	private static final String HTML_NEW = "scripts/services/36617-new.htm";

	public IsidoraInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void showChatWindow(Player player, int val, Object... args)
	{
		if(player.getVarBoolean("newbieService"))
		{
			player.sendPacket(new NpcHtmlMessagePacket(player, this, HtmCache.getInstance().getHtml(HTML_OLD, player), val));
		}
		else
		{
			player.sendPacket(new NpcHtmlMessagePacket(player, this, HtmCache.getInstance().getHtml(HTML_NEW, player), val));
		}
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		StringBuilder html = new StringBuilder();

		NpcHtmlMessagePacket msg = new NpcHtmlMessagePacket(player, this);
		msg.setFile("scripts/services/36617-get.htm");

		NpcHtmlMessagePacket reward = new NpcHtmlMessagePacket(player, this);
		reward.setFile("scripts/services/36617-reward.htm");

		ClassId classId = player.getClassId();
		int nClassId = classId.getId();

		if(!canBypassCheck(player, this))
		{
			return;
		}

		StringTokenizer st = new StringTokenizer(command);

		if(command.equalsIgnoreCase("get_newbie_gift"))
		{
			switch(nClassId)
			{
				case 0:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15203").append("\">").append("Warrior").append("</a></font><br>");
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15201").append("\">").append("Human Knight").append("</a></font><br>");
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15204").append("\">").append("Rogue").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 1:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15203").append("\">").append("Warlord").append("</a></font><br>");
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15203").append("\">").append("Gladiator").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 4:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15201").append("\">").append("Paladin").append("</a></font><br>");
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15201").append("\">").append("Dark Avenger").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 7:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15204").append("\">").append("Treasure Hunter").append("</a></font><br>");
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15204").append("\">").append("Hawkeye").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 10:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15202").append("\">").append("Wizard").append("</a></font><br>");
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15202").append("\">").append("Cleric").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 11:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15202").append("\">").append("Sorcerer").append("</a></font><br>");
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15202").append("\">").append("Necromancer").append("</a></font><br>");
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15202").append("\">").append("Warlock").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 15:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15202").append("\">").append("Bishop").append("</a></font><br>");
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15202").append("\">").append("Prophet").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 18:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15201").append("\">").append("Elven Knight").append("</a></font><br>");
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15204").append("\">").append("Elven Scout").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 19:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15201").append("\">").append("Temple Knight").append("</a></font><br>");
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15201").append("\">").append("Swordsinger").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 22:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15204").append("\">").append("Plainswalker").append("</a></font><br>");
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15204").append("\">").append("Silver Ranger").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 25:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15202").append("\">").append("Elven Wizard").append("</a></font><br>");
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15202").append("\">").append("Elven Oracle").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 26:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15202").append("\">").append("Spellsinger").append("</a></font><br>");
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15202").append("\">").append("Elemental Summoner").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 29:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15202").append("\">").append("Elven Elder").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 31:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15201").append("\">").append("Palus Knight").append("</a></font><br>");
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15204").append("\">").append("Assassin").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 32:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15201").append("\">").append("Shillien Knight").append("</a></font><br>");
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15201").append("\">").append("Bladedancer").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 35:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15204").append("\">").append("Abyss Walker").append("</a></font><br>");
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15204").append("\">").append("Phantom Ranger").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 38:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15202").append("\">").append("Dark Wizard").append("</a></font><br>");
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15202").append("\">").append("Shillien Oracle").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 39:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15202").append("\">").append("Spellhowler").append("</a></font><br>");
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15202").append("\">").append("Phantom Summoner").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 42:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15202").append("\">").append("Shillien Elder").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 44:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15206").append("\">").append("Orc Raider").append("</a></font><br>");
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15207").append("\">").append("Monk").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 45:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15206").append("\">").append("Destroyer").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 47:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15207").append("\">").append("Tyrant").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 49:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15207").append("\">").append("Orc Shaman").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 50:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15207").append("\">").append("Overlord").append("</a></font><br>");
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15207").append("\">").append("Warcryer").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 53:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15203").append("\">").append("Scavenger").append("</a></font><br>");
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15203").append("\">").append("Artisan").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 54:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15203").append("\">").append("Bounty Hunter").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 56:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15203").append("\">").append("Warsmith").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 123:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15205").append("\">").append("Trooper").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 124:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15205").append("\">").append("Warder").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 125:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15205").append("\">").append("Berserker").append("</a></font><br>");
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15205").append("\">").append("Soul Breaker").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				case 126:
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15205").append("\">").append("Female Soul Breaker").append("</a></font><br>");
					html.append("<font color=\"LEVEL\" name=\"hs12\"><a action=\"bypass -h npc_").append(getObjectId()).append("_give_gift_by_id 15205").append("\">").append("Arbalester").append("</a></font><br>");
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
					break;
				default:
					if(player.isLangRus())
					{
						html.append("<font color=\"FF0000\">Сервис для вас недоступен.</font>");
					}
					else
					{
						html.append("<font color=\"FF0000\">Service is not available to you.</font>");
					}
					msg.replace("%get_gift%", html.toString());
					player.sendPacket(msg);
			}
		}
		else if(command.startsWith("give_gift_by_id"))
		{
			st.nextToken();
			int valGift = Integer.parseInt(st.nextToken());

			player.getInventory().addItem(valGift, 1);

			ItemTemplate rewardItem = ItemHolder.getInstance().getTemplate(valGift);

			html.append(rewardItem.getName());
			reward.replace("%reward_name%", html.toString());
			player.sendPacket(reward);

			player.setVar("newbieService", "true", -1);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
}
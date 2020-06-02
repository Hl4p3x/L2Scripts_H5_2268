package l2s.gameserver.model.entity.CCPHelpers;

import java.util.List;

import l2s.gameserver.Config;
import l2s.gameserver.dao.CharacterVariablesDAO;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.database.mysql;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.CharacterVariable;
import l2s.gameserver.model.pledge.UnitMember;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;

public class CCPCWHPrivilages
{
	public static String clanMain(Player activeChar, String args)
	{
		if(activeChar.getClan() == null)
			return "cfgClan.htm";
		if(Config.ALT_ALLOW_CLAN_COMMAND_ONLY_FOR_CLAN_LEADER && !activeChar.isClanLeader())
			return "cfgClan.htm";
		if((activeChar.getClanPrivileges() & 0x10) != 16)
			return "cfgClan.htm";
		if(args != null)
		{
			String[] param = args.split(" ");
			if(param.length > 0)
			{
				if(param[0].equalsIgnoreCase("allowwh") && param.length > 1)
				{
					UnitMember cm = activeChar.getClan().getAnyMember(param[1]);
					if(cm != null && cm.getPlayer() != null)
					{
						if(!cm.getPlayer().getVarBoolean("canWhWithdraw"))
						{
							cm.getPlayer().setVar("canWhWithdraw", "1", -1L);
							activeChar.sendMessage("Privilege given successfully");
						}
					}
					else if(cm != null)
					{
						CharacterVariablesDAO.getInstance().insert(cm.getObjectId(), new CharacterVariable("canWhWithdraw", String.valueOf(1), -1));
						activeChar.sendMessage("Privilege given successfully");
					}
					else
						activeChar.sendMessage("Player not found.");
					return "cfgClan.htm";
				}
				if(param[0].equalsIgnoreCase("blockwh") && param.length > 1)
				{
					UnitMember cm = activeChar.getClan().getAnyMember(param[1]);
					if(cm != null && cm.getPlayer() != null)
					{
						if(cm.getPlayer().getVarBoolean("canWhWithdraw"))
						{
							cm.getPlayer().unsetVar("canWhWithdraw");
							activeChar.sendMessage("Privilege removed successfully");
						}
					}
					else if(cm != null)
					{
						mysql.set("DELETE FROM `character_variables` WHERE obj_id=" + cm.getObjectId() + " AND name='canWhWithdraw' LIMIT 1");
						activeChar.sendMessage("Privilege removed successfully");
					}
					else
						activeChar.sendMessage("Player not found.");
					return "cfgClan.htm";
				}
				if(param[0].equalsIgnoreCase("list"))
				{
					StringBuilder builder = new StringBuilder("SELECT `obj_id` FROM `character_variables` WHERE `obj_id` IN (");
					List<UnitMember> members = activeChar.getClan().getAllMembers();
					for(int i = 0; i < members.size(); i++)
					{
						builder.append(members.get(i).getObjectId());
						if(i < members.size() - 1)
							builder.append(',');
					}
					builder.append(") AND `name`='canWhWithdraw'");
					List<Object> list = mysql.get_array(builder.toString());
					String msg = HtmCache.getInstance().getHtml("command/cfgClanList.htm", activeChar);
					StringBuilder replaceBuilder = new StringBuilder("<table width=280>");
          
					for(Object o_id : list)
					{
						for(UnitMember m : members)
							if(m.getObjectId() == Integer.parseInt(o_id.toString()))
								replaceBuilder.append("<tr><td width=30></td><td width=100><font color=686764>").append(m.getName()).append("</font></td><td width=150><button width=130 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h user_cfg cwhPrivs allowwh ").append(m.getName()).append("\" value=\"Remove Privilage\"><br></td></tr>");
					}
					Object memberObjectId;
					replaceBuilder.append("</table>");
					msg = msg.replace("%chars%", replaceBuilder.toString());
					msg = msg.replace("%online%", CCPSmallCommands.showOnlineCount());
          
					NpcHtmlMessagePacket html = new NpcHtmlMessagePacket(0);
					html.setHtml(msg);
					activeChar.sendPacket(html);
					return null;
				}
			}
		} 
		return "cfgClan.htm";
	}
}

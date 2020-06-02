package l2s.gameserver.model.instances;

import java.util.StringTokenizer;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;
import l2s.gameserver.network.l2.s2c.PledgeShowInfoUpdatePacket;
import l2s.gameserver.templates.npc.NpcTemplate;


public class FameManagerInstance extends NpcInstance
{
	public FameManagerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken();
		NpcHtmlMessagePacket html = new NpcHtmlMessagePacket(player, this);
		if(actualCommand.equalsIgnoreCase("PK_Count"))
		{
			if(player.getFame() >= 5000)
			{
				if(player.getPkKills() > 0)
				{
					player.setFame(player.getFame() - 5000, "PK_Count");
					player.setPkKills(player.getPkKills() - 1);
					html.setFile("default/" + getNpcId() + "-okpk.htm");
				}
				else
					html.setFile("default/" + getNpcId() + "-nohavepk.htm");
			}
			else
				html.setFile("default/" + getNpcId() + "-nofame.htm");
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
		}
		else if(actualCommand.equalsIgnoreCase("CRP"))
		{
			if(player.getClan() == null || player.getClassLevel() < 2 || player.getClan().getLevel() < 5)
				html.setFile("default/" + getNpcId() + "-noclancrp.htm");
			else if(player.getFame() < 1000)
				html.setFile("default/" + getNpcId() + "-nofame.htm");
			else
			{
				player.setFame(player.getFame() - 1000, "CRP");
				player.getClan().incReputation(50, false, "FameManager from " + player.getName());
				player.getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdatePacket(player.getClan()));
				player.sendPacket(Msg.ACQUIRED_50_CLAN_FAME_POINTS);
				html.setFile("default/" + getNpcId() + "-okclancrp.htm");
			}
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
		}
		else
			super.onBypassFeedback(player, command);
	}
}
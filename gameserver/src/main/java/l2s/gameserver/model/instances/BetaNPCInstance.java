package l2s.gameserver.model.instances;

import java.sql.Connection;
import java.sql.PreparedStatement;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.l2.s2c.PledgeShowInfoUpdatePacket;
import l2s.gameserver.network.l2.s2c.UIPacket;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Log;

/**
 * @author jkk
 */

@SuppressWarnings("serial")
public final class BetaNPCInstance extends NpcInstance
{
	public BetaNPCInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equalsIgnoreCase("change_sex"))
		{
			Connection con = null;
			PreparedStatement offline = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				offline = con.prepareStatement("UPDATE characters SET sex = ? WHERE obj_Id = ?");
				offline.setInt(1, player.getSex() == 1 ? 0 : 1);
				offline.setInt(2, player.getObjectId());
				offline.executeUpdate();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return;
			}
			finally
			{
				DbUtils.closeQuietly(con, offline);
			}
			
			player.changeSex();
			player.sendMessage("Your gender has been changed");
			Log.add("Character " + player + "  changed sex to " + (player.getSex() == 1 ? "male" : "female"), "renames");
		}
		else if(command.equalsIgnoreCase("add_clan_reputation"))
		{
			if (player.getClan() != null)
			{
				player.getClan().incReputation(10000, false, "BetaNpc");
				player.getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdatePacket(player.getClan()));
				player.sendMessage("Your clan received 10 000 clan reputation!");
			}
			else
				player.sendMessage("Sorry, but you donť have clan!");
			
		}
		else if(command.equalsIgnoreCase("add_exp_sp"))
		{
			player.addExpAndSp(999999999, 999999999);
			player.addExpAndSp(999999999, 999999999);
			player.addExpAndSp(999999999, 999999999);
			player.addExpAndSp(999999999, 999999999);
		}
		else if(command.equalsIgnoreCase("add_fame"))
		{
			player.setFame(player.getFame() + 10000, "BetaNpc");
			player.sendPacket(new UIPacket(player));
			player.sendMessage("You received 10.000 fame points !");
		}
		else if(command.equalsIgnoreCase("give_noblesse"))
		{
			if (!player.isNoble())
			{					
				Olympiad.addNoble(player.getPlayer());
				player.getPlayer().setNoble(true);
				player.getPlayer().updatePledgeClass();
				player.getPlayer().updateNobleSkills();
				player.getPlayer().sendSkillList();
				player.getPlayer().broadcastUserInfo(true);
				player.getInventory().addItem(7694, 1);
				player.sendMessage("Congratulations! You gained noblesse rank.");
				player.broadcastUserInfo(true);
			}
			else if (player.isNoble())
			{
				player.sendMessage("You already have noblesse rank !");
			}
		}
		else if(command.equalsIgnoreCase("give_hero"))
		{
			if (!player.isHero())
			{
				player.setHero(true);
				player.updatePledgeClass();
				if(!player.isSubClassActive())
				{
					Hero.addSkills(player);
					player.sendSkillList();
				}
				player.getPlayer().broadcastUserInfo(true);
				player.sendMessage("Congratulations! You gained hero rank.");
				player.broadcastUserInfo(true);
			}
			else if (player.isNoble())
			{
				player.sendMessage("You already have hero rank !");
			}
		}
		else
			super.onBypassFeedback(player, command);
	}
	
	@Override
	public boolean isNpc()
	{
		return true;
	}
}
package npc.model.residences.clanhall;

import org.apache.commons.lang3.ArrayUtils;

import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.Privilege;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.ReflectionUtils;

/**
 * @author VISTALL
 * @date 10:50/20.06.2011
 */
public class AuctionedDoormanInstance extends NpcInstance
{
	private int[] _doors;
	private boolean _elite;

	public AuctionedDoormanInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);

		_doors = template.getAIParams().getIntegerArray("doors", ArrayUtils.EMPTY_INT_ARRAY);
		_elite = template.getAIParams().getBool("elite", false);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		ClanHall clanHall = getClanHall();
		/* TODO: Bonux if(_elite)
		{
			if(command.equalsIgnoreCase("evolve"))
			{
				StringTokenizer t = new StringTokenizer(command);
				t.nextToken();
				int evolve_type = Integer.parseInt(t.nextToken());
				evolve(player, evolve_type);
				return;
			}
		}*/
		if(command.equalsIgnoreCase("openDoors"))
		{
			if(player.hasPrivilege(Privilege.CH_ENTER_EXIT) && player.getClan().getHasHideout() == clanHall.getId())
			{
				for(int d : _doors)
					ReflectionUtils.getDoor(d).openMe();
				showChatWindow(player, "residence2/clanhall/agitafterdooropen.htm");
			}
			else
				showChatWindow(player, "residence2/clanhall/noAuthority.htm");
		}
		else if(command.equalsIgnoreCase("closeDoors"))
		{
			if(player.hasPrivilege(Privilege.CH_ENTER_EXIT) && player.getClan().getHasHideout() == clanHall.getId())
			{
				for(int d : _doors)
					ReflectionUtils.getDoor(d).closeMe(player, true);
				showChatWindow(player, "residence2/clanhall/agitafterdoorclose.htm");
			}
			else
				showChatWindow(player, "residence2/clanhall/noAuthority.htm");
		}
		else if(command.equalsIgnoreCase("banish"))
		{
			if(player.hasPrivilege(Privilege.CH_DISMISS))
			{
				clanHall.banishForeigner();
				showChatWindow(player, "residence2/clanhall/agitafterbanish.htm");
			}
			else
				showChatWindow(player, "residence2/clanhall/noAuthority.htm");
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		ClanHall clanHall = getClanHall();
		if (clanHall != null)
		{
			Clan playerClan = player.getClan();
			if(playerClan != null && playerClan.getHasHideout() == clanHall.getId())
				showChatWindow(player, _elite ? "residence2/clanhall/WyvernAgitJanitorHi.htm" : "residence2/clanhall/AgitJanitorHi.htm", "%owner%", playerClan.getName());
			else
			{
				if(playerClan != null && playerClan.getCastle() > 0)
				{
					Castle castle = ResidenceHolder.getInstance().getResidence(playerClan.getCastle());
					NpcHtmlMessagePacket html = new NpcHtmlMessagePacket(player, this);
					html.setFile("merchant/territorystatus.htm");
					html.replace("%npcname%", getName());
					html.replace("%castlename%", HtmlUtils.htmlResidenceName(castle.getId()));
					html.replace("%taxpercent%", String.valueOf(castle.getTaxPercent()));
					html.replace("%clanname%", playerClan.getName());
					html.replace("%clanleadername%", playerClan.getLeaderName());
					player.sendPacket(html);
				}
				else
					showChatWindow(player, "residence2/clanhall/noAgitInfo.htm");
			}
		}
		else
			showChatWindow(player, "residence2/clanhall/noAgitInfo.htm");
	}

	/* TODO: Bonux private void evolve(Player player, int type)
	{
		int min_lv_pet = 55;
		int item_baby_pet;
		int item_grown_pet;
		int pet_npc_id;
		switch(type)
		{
			case 1:
				item_baby_pet = 9882;
				item_grown_pet = 10307;
				pet_npc_id = 16025;
				break;
			case 2:
				item_baby_pet = 4422;
				item_grown_pet = 10308;
				pet_npc_id = 12526;
			case 3:
				item_baby_pet = 4423;
				item_grown_pet = 10309;
				pet_npc_id = 12527;
			case 4:
				item_baby_pet = 4424;
				item_grown_pet = 10310;
				pet_npc_id = 12528;
			case 5:
				item_baby_pet = 10426;
				item_grown_pet = 10611;
				pet_npc_id = 16041;
			default:
				return;
		}

		if(player.getInventory().getCountOf(item_baby_pet) >= 2)
		{
			showChatWindow(player, "residence2/pet_evolution_many_pet.htm");
			return;
		}
		if(player.getInventory().getCountOf(item_baby_pet) <= 0 && player.getInventory().getCountOf(item_grown_pet) > 0)
		{
			showChatWindow(player, "residence2/pet_evolution_no_pet.htm");
			return;
		}
		Servitor servitor = player.getServitor();
		if(servitor == null || !servitor.isPet())
		{
			showChatWindow(player, "residence2/pet_evolution_farpet.htm");
			return;
		}
		else if(getDistance(player) >= 200.0D)
		{
			showChatWindow(player, "residence2/pet_evolution_farpet.htm");
			return;
		}
		else if(servitor.getNpcId() != pet_npc_id)
		{
			showChatWindow(player, "residence2/pet_evolution_farpet.htm");
			return;
		}
		else if(servitor.getLevel() < min_lv_pet)
		{
			showChatWindow(player, "residence2/pet_evolution_level.htm");
			return;
		}
		ItemInstance item = player.getInventory().getItemByItemId(item_baby_pet);
		if(item != null)
		{
			if(item.getEnchantLevel() < min_lv_pet)
			{
				showChatWindow(player, "residence2/pet_evolution_level.htm");
				return;
			}
			//((PetInstance) servitor).changeTemplate();
			showChatWindow(player, "residence2/pet_evolution_success.htm");
		}
		else
		{
			showChatWindow(player, "residence2/pet_evolution_no_pet.htm");
			return;
		}
	}*/
}

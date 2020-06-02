package npc.model;

import java.util.List;

import instances.HeartInfinityAttack;
import l2s.commons.util.Rnd;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;

/**
 * @author pchayka
 */

public final class DeadTumorInstance extends NpcInstance
{
	private long warpTimer = 0;

	public DeadTumorInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		warpTimer = System.currentTimeMillis();
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(getReflection().getInstancedZoneId() == 119 || getReflection().getInstancedZoneId() == 120)
		{
			List<NpcInstance> deadTumors = getReflection().getAllByNpcId(getNpcId(), true);
			if(deadTumors.contains(this))
				deadTumors.remove(this);

			if(command.equalsIgnoreCase("examine_tumor"))
				showChatWindow(player, 1);
			else if(command.equalsIgnoreCase("showcheckpage"))
			{
				if(!player.isInParty())
				{
					showChatWindow(player, 2);
					return;
				}
				if(warpTimer + 60000 > System.currentTimeMillis())
				{
					showChatWindow(player, 4);
					return;
				}
				if(deadTumors.size() < 1)
				{
					showChatWindow(player, 3);
					return;
				}
				showChatWindow(player, 5);
			}
			else if(command.equalsIgnoreCase("warp"))
			{
				if(ItemFunctions.getItemCount(player, 13797) < 1)
				{
					showChatWindow(player, 6);
					return;
				}
				if(ItemFunctions.deleteItem(player, 13797, 1, true) && player.isInParty())
				{
					Location loc = Location.coordsRandomize(deadTumors.get(Rnd.get(deadTumors.size())).getLoc(), 100, 150);
					if(loc != null)
					{
						for(Player p : getReflection().getPlayers())
							p.sendPacket(new ExShowScreenMessage(NpcString.S1S_PARTY_HAS_MOVED_TO_A_DIFFERENT_LOCATION_THROUGH_THE_CRACK_IN_THE_TUMOR, 8000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, false, 1, -1, false, player.getParty().getPartyLeader().getName()));
						for(Player p : player.getParty().getPartyMembers())
							if(p.isInRange(this, 500))
								p.teleToLocation(loc);
					}
				}
			}
			else
				super.onBypassFeedback(player, command);
		}
		else if(getReflection().getInstancedZoneId() == 121)
		{
			List<NpcInstance> deadTumors = getReflection().getAllByNpcId(getNpcId(), true);
			if(deadTumors.contains(this))
				deadTumors.remove(this);
			if(command.equalsIgnoreCase("examine_tumor"))
			{
				if(getNpcId() == 32536)
					showChatWindow(player, 1);
				else if(getNpcId() == 32535)
					showChatWindow(player, 7);
			}
			else if(command.equalsIgnoreCase("warpechmus"))
			{
				if(!player.isInParty())
				{
					showChatWindow(player, 2);
					return;
				}
				for(Player p : getReflection().getPlayers())
					p.sendPacket(new ExShowScreenMessage(NpcString.S1S_PARTY_HAS_MOVED_TO_A_DIFFERENT_LOCATION_THROUGH_THE_CRACK_IN_THE_TUMOR, 8000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, false, 1, -1, false, player.getParty().getPartyLeader().getName()));
				for(Player p : player.getParty().getPartyMembers())
					if(p.isInRange(this, 800))
						p.teleToLocation(new Location(-179548, 209584, -15504));
				((HeartInfinityAttack) getReflection()).notifyEchmusEntrance(player.getParty().getPartyLeader());
			}
			else if(command.equalsIgnoreCase("showcheckpage"))
			{
				if(!player.isInParty())
				{
					showChatWindow(player, 2);
					return;
				}
				if(warpTimer + 60000 > System.currentTimeMillis())
				{
					showChatWindow(player, 4);
					return;
				}
				if(deadTumors.size() < 1)
				{
					showChatWindow(player, 3);
					return;
				}
				showChatWindow(player, 5);
			}
			else if(command.equalsIgnoreCase("warp"))
			{
				if(ItemFunctions.getItemCount(player, 13797) < 1)
				{
					showChatWindow(player, 6);
					return;
				}
				if(ItemFunctions.deleteItem(player, 13797, 1, true) && player.isInParty())
				{
					Location loc = Location.coordsRandomize(deadTumors.get(Rnd.get(deadTumors.size())).getLoc(), 100, 150);
					if(loc != null)
					{
						for(Player p : getReflection().getPlayers())
							p.sendPacket(new ExShowScreenMessage(NpcString.S1S_PARTY_HAS_MOVED_TO_A_DIFFERENT_LOCATION_THROUGH_THE_CRACK_IN_THE_TUMOR, 8000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, false, 1, -1, false, player.getParty().getPartyLeader().getName()));
						for(Player p : player.getParty().getPartyMembers())
							if(p.isInRange(this, 500))
								p.teleToLocation(loc);
					}
				}
			}
			else if(command.equalsIgnoreCase("reenterechmus"))
			{
				if(ItemFunctions.getItemCount(player, 13797) < 3)
				{
					showChatWindow(player, 6);
					return;
				}
				if(ItemFunctions.deleteItem(player, 13797, 3, true) && player.isInParty())
				{
					for(Player p : getReflection().getPlayers())
						p.sendPacket(new ExShowScreenMessage(NpcString.S1S_PARTY_HAS_ENTERED_THE_CHAMBER_OF_EKIMUS_THROUGH_THE_CRACK_IN_THE_TUMOR, 8000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, false, 1, -1, false, player.getParty().getPartyLeader().getName()));
					((HeartInfinityAttack) getReflection()).notifyEkimusRoomEntrance();
					for(Player p : player.getParty().getPartyMembers())
						if(p.isInRange(this, 400))
							p.teleToLocation(new Location(-179548, 209584, -15504));
				}
			}
			else
				super.onBypassFeedback(player, command);
		}
		else
			super.onBypassFeedback(player, command);
	}
}
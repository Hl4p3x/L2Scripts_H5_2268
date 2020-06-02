package services;

import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.utils.Location;

public class TeleToStakatoNest extends Functions
{
	private final static Location[] teleports = {
			new Location(80456, -52322, -5640),
			new Location(88718, -46214, -4640),
			new Location(87464, -54221, -5120),
			new Location(80848, -49426, -5128),
			new Location(87682, -43291, -4128) };

	public void list()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		QuestState qs = player.getQuestState(240);
		if(qs == null || !qs.isCompleted())
		{
			show("scripts/services/TeleToStakatoNest-no.htm", player);
			return;
		}

		show("scripts/services/TeleToStakatoNest.htm", player);
	}

	public void teleTo(String[] args)
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;
		if(args.length != 1)
			return;

		Location loc = teleports[Integer.parseInt(args[0]) - 1];
		Party party = player.getParty();
		if(party == null)
			player.teleToLocation(loc);
		else
			for(Player member : party.getPartyMembers())
				if(member != null && member.isInRange(player, 1000))
					member.teleToLocation(loc);
	}
}
package l2s.gameserver.model.entity.events.impl.fightclub;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.fightclubmanager.FightClubPlayer;
import l2s.gameserver.model.entity.events.impl.AbstractFightClub;

public class TeamVSTeamEvent extends AbstractFightClub
{
	public TeamVSTeamEvent(MultiValueSet<String> set)
	{
		super(set);
	}

	public void onKilled(Creature actor, Creature victim)
	{
		if(actor != null && actor.isPlayable())
		{
			FightClubPlayer realActor = getFightClubPlayer(actor.getPlayer());
			if(victim.isPlayer() && realActor != null)
			{
				realActor.increaseKills(true);
				realActor.getTeam().incScore(1);
				updatePlayerScore(realActor);
				updateScreenScores();
				sendMessageToPlayer(realActor, AbstractFightClub.MESSAGE_TYPES.GM, "You have killed " + victim.getName());
			}
			//else if(!victim.isPet());  ???

			actor.getPlayer().sendUserInfo();
		}

		if(victim.isPlayer())
		{
			FightClubPlayer realVictim = getFightClubPlayer(victim);
			if(realVictim != null)
			{
				realVictim.increaseDeaths();
				if(actor != null)
					sendMessageToPlayer(realVictim, AbstractFightClub.MESSAGE_TYPES.GM, "You have been killed by " + actor.getName());
			}
			victim.broadcastCharInfo();
		}

		super.onKilled(actor, victim);
	}

	public String getVisibleTitle(Player player, String currentTitle, boolean toMe)
	{
		FightClubPlayer fPlayer = getFightClubPlayer(player);

		if(fPlayer == null)
			return currentTitle;

		return "Kills: " + fPlayer.getKills(true) + " Deaths: " + fPlayer.getDeaths();
	}
}
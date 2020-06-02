package l2s.gameserver.model.entity.events.impl.fightclub;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.events.fightclubmanager.FightClubEventManager;
import l2s.gameserver.model.entity.events.fightclubmanager.FightClubPlayer;
import l2s.gameserver.model.entity.events.impl.AbstractFightClub;

public class LastManStandingEvent extends AbstractFightClub
{
	private long _lastKill;
	private FightClubPlayer _winner;

	public LastManStandingEvent(MultiValueSet<String> set)
	{
		super(set);
	}

	public void onKilled(Creature actor, Creature victim)
	{
		if(actor != null && actor.isPlayable())
		{
			FightClubPlayer fActor = getFightClubPlayer(actor.getPlayer());
			if(victim.isPlayer())
			{
				fActor.increaseKills(true);
				updatePlayerScore(fActor);
				sendMessageToPlayer(fActor, AbstractFightClub.MESSAGE_TYPES.GM, "You have killed " + victim.getName()); // TODO: Вынести в ДП.
			}
			//else if(!victim.isPet());  ???

			actor.getPlayer().sendUserInfo();
		}

		if(victim.isPlayer())
		{
			FightClubPlayer fVictim = getFightClubPlayer(victim);
			fVictim.increaseDeaths();
			if(actor != null)
				sendMessageToPlayer(fVictim, AbstractFightClub.MESSAGE_TYPES.GM, "You have been killed by " + actor.getName()); // TODO: Вынести в ДП.
			victim.getPlayer().sendUserInfo();
			_lastKill = System.currentTimeMillis();

			checkRoundOver();
		}

		super.onKilled(actor, victim);
	}

	@Override
	public void startEvent()
	{
		super.startEvent();
		ThreadPoolManager.getInstance().schedule(new InactivityCheck(), 60000L);
	}

	public void startRound()
	{
		super.startRound();
		checkRoundOver();
	}

	public boolean leaveEvent(Player player, boolean teleportTown)
	{
		boolean result = super.leaveEvent(player, teleportTown);
		if(result)
			checkRoundOver();
		return result;
	}

	private void checkRoundOver()
	{
		if(getState() != AbstractFightClub.EVENT_STATE.STARTED)
			return;

		int alivePlayers = 0;
		FightClubPlayer aliveFPlayer = null;

		for(FightClubPlayer iFPlayer : getPlayers(new String[] { "fighting_players" }))
		{
			if (isPlayerActive(iFPlayer.getPlayer()))
			{
				alivePlayers++;
				aliveFPlayer = iFPlayer;
			}			
			if(isPlayerAlive(iFPlayer.getPlayer()))
			{
				alivePlayers++;
				aliveFPlayer = iFPlayer;
			}
		}

		if(alivePlayers <= 1)
		{
			if(alivePlayers == 1)
			{
				_winner = aliveFPlayer;
				aliveFPlayer.increaseScore(1);
				announceWinnerPlayer(false, aliveFPlayer);
				updateScreenScores();
				FightClubEventManager.getInstance().sendToAllMsg(this, _winner.getPlayer().getName() + " Won Event!"); // TODO: Вынести в ДП.
			}
			setState(AbstractFightClub.EVENT_STATE.OVER);

			ThreadPoolManager.getInstance().schedule(new Runnable()
			{
				@Override
				public void run()
				{
					endRound();
				}
			}
			, 5000L);
		}
	}

	private boolean isPlayerAlive(Player player)
	{
		if(player == null)
			return false;
		if(player.isDead())
			return false;
		if(!player.getReflection().equals(getReflection()))
			return false;
		if(System.currentTimeMillis() - player.getLastNotAfkTime() > 120000L)
			return false;

		boolean insideZone = false;
		for(Zone zone : getReflection().getZones())
		{
			if(zone.checkIfInZone(player))
				insideZone = true;
		}
		return insideZone;
	}

	protected boolean inScreenShowBeScoreNotKills()
	{
		return false;
	}

	protected int getRewardForWinningTeam(FightClubPlayer fPlayer, boolean atLeast1Kill)
	{
		if(fPlayer.equals(_winner))
			return (int)_badgeWin;
		return super.getRewardForWinningTeam(fPlayer, true);
	}

	public String getVisibleTitle(Player player, String currentTitle, boolean toMe)
	{
		FightClubPlayer realPlayer = getFightClubPlayer(player);

		if(realPlayer == null)
			return currentTitle;

		return "Kills: " + realPlayer.getKills(true);
	}

	private class InactivityCheck extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			if(getState() == AbstractFightClub.EVENT_STATE.NOT_ACTIVE)
				return;
			LastManStandingEvent.this.checkRoundOver();

			ThreadPoolManager.getInstance().schedule(this, 60000L);
		}
	}
}
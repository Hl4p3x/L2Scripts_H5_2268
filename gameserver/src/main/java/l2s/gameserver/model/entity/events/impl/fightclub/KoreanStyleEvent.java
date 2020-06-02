package l2s.gameserver.model.entity.events.impl.fightclub;

import java.util.List;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Effect;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.fightclubmanager.FightClubPlayer;
import l2s.gameserver.model.entity.events.fightclubmanager.FightClubTeam;
import l2s.gameserver.model.entity.events.impl.AbstractFightClub;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.utils.Location;

public class KoreanStyleEvent extends AbstractFightClub
{
	private static final long MAX_FIGHT_TIME = 90000L;
	protected final FightClubPlayer[] _fightingPlayers;
	private final int[] lastTeamChosenSpawn;
	protected long _lastKill;
	
	public KoreanStyleEvent(MultiValueSet<String> set)
	{
		super(set);
		_lastKill = 0L;
		_fightingPlayers = new FightClubPlayer[2];
		lastTeamChosenSpawn = new int[] { 0, 0 };
	}

	public void onKilled(Creature actor, Creature victim)
	{
		if((actor != null) && (actor.isPlayable()))
		{
			FightClubPlayer realActor = getFightClubPlayer(actor.getPlayer());
			if((victim.isPlayer()) && (realActor != null))
			{
				realActor.increaseKills(true);
				updatePlayerScore(realActor);
				updateScreenScores();
				sendMessageToPlayer(realActor, AbstractFightClub.MESSAGE_TYPES.GM, "You have killed " + victim.getName());
			}
			actor.getPlayer().sendUserInfo();
		}
		
		if(victim.isPlayer())
		{
			FightClubPlayer realVictim = getFightClubPlayer(victim);
			realVictim.increaseDeaths();
			if(actor != null)
				sendMessageToPlayer(realVictim, AbstractFightClub.MESSAGE_TYPES.GM, "You have been killed by " + actor.getName());
			victim.broadcastCharInfo();
			
			_lastKill = System.currentTimeMillis();
		}
		checkFightingPlayers();
		super.onKilled(actor, victim);
	}
	

	public void loggedOut(Player player)
	{
		super.loggedOut(player);
		for(FightClubPlayer fPlayer : _fightingPlayers)
		{
			if((fPlayer != null) && (fPlayer.getPlayer() != null) && (fPlayer.getPlayer().equals(player))) {
				checkFightingPlayers();
			}
		}
	}

	@Override
	public void stopEvent(boolean force)
	{
		for(int i = 0; i < _fightingPlayers.length; i++)
			_fightingPlayers[i] = null;
		super.stopEvent(force);
	}

	public boolean leaveEvent(Player player, boolean teleportTown)
	{
		super.leaveEvent(player, teleportTown);
		try
		{
			if(player.isRooted()) {
				player.stopRooted();
			}
		}
		catch (IllegalStateException e) {}
		
		player.stopAbnormalEffect(AbnormalEffect.ROOT);
		if(getState() != AbstractFightClub.EVENT_STATE.STARTED)
			return true;
		for(FightClubPlayer fPlayer : _fightingPlayers)
		{
			if((fPlayer != null) && (fPlayer.getPlayer() != null) && (fPlayer.getPlayer().equals(player)))
				checkFightingPlayers();
		}
		return true;
	}

	public void startEvent()
	{
		super.startEvent();
		for(FightClubPlayer fPlayer : getPlayers(new String[] { "fighting_players", "registered_players" }))
		{
			Player player = fPlayer.getPlayer();
			if(player.isDead())
				player.doRevive();
			if(player.isFakeDeath())
				player.setFakeDeath(false);
			player.sitDown(null);
		}
		
		_lastKill = System.currentTimeMillis();
	}

	public void startRound()
	{
		super.startRound();
		checkFightingPlayers();
		ThreadPoolManager.getInstance().schedule(new CheckFightersInactive(this), 5000L);
	}

	public void endRound()
	{
		super.endRound();
		super.unrootPlayers();
	}
	
	private void checkFightingPlayers()
	{
		if((getState() == AbstractFightClub.EVENT_STATE.OVER) || (getState() == AbstractFightClub.EVENT_STATE.NOT_ACTIVE))
			return;
		boolean changed = false;
		for(int i = 0; i < _fightingPlayers.length; i++)
		{
			FightClubPlayer oldPlayer = _fightingPlayers[i];
			if((oldPlayer == null) || (!isPlayerActive(oldPlayer.getPlayer())) || (getFightClubPlayer(oldPlayer.getPlayer()) == null))
			{
				if((oldPlayer != null) && (!oldPlayer.getPlayer().isDead()))
				{
					oldPlayer.getPlayer().doDie(null);
					return;
				}
				FightClubPlayer newPlayer = chooseNewPlayer(i + 1);
				if(newPlayer == null)
				{
					for(FightClubTeam team : getTeams())
					{
						if(team.getIndex() != i + 1)
							team.incScore(1);
					}
					endRound();
					return;
				}
				newPlayer.getPlayer().isntAfk();
				_fightingPlayers[i] = newPlayer;
				changed = true;
			}
		}
		
		if(changed)
		{
			StringBuilder msg = new StringBuilder();
			for(int i = 0; i < _fightingPlayers.length; i++)
			{
				if(i > 0)
					msg.append(" VS ");
				msg.append(_fightingPlayers[i].getPlayer().getName());
			}
			sendMessageToFighting(AbstractFightClub.MESSAGE_TYPES.SCREEN_BIG, msg.toString(), false);
			preparePlayers();
		}
	}

	private FightClubPlayer chooseNewPlayer(int teamIndex)
	{
		List<FightClubPlayer> alivePlayersFromTeam = new java.util.ArrayList<FightClubPlayer>();
		for(FightClubPlayer fPlayer : getPlayers(new String[] { "fighting_players" }))
		{
			if((fPlayer.getPlayer().isSitting()) && (fPlayer.getTeam().getIndex() == teamIndex))
			{
				alivePlayersFromTeam.add(fPlayer);
			}
		}
		
		if(alivePlayersFromTeam.isEmpty())
			return null;
		if(alivePlayersFromTeam.size() == 1)
			return alivePlayersFromTeam.get(0);
		return l2s.commons.util.Rnd.get(alivePlayersFromTeam);
	}

	private void preparePlayers()
	{
		for(int i = 0; i < _fightingPlayers.length; i++)
		{
			FightClubPlayer fPlayer = _fightingPlayers[i];
			Player player = fPlayer.getPlayer();
			try
			{
				if(player.isBlocked())
					player.unblock();
				if(player.isRooted()) {
					player.stopRooted();
				}
			}
			catch (IllegalStateException e) {}
			

			player.stopAbnormalEffect(AbnormalEffect.ROOT);
			player.standUp();
			player.isntAfk();
			player.resetReuse();
			player.sendPacket(new l2s.gameserver.network.l2.s2c.SkillCoolTimePacket(player));
			healFull(player);
			if((player.getServitor() != null) && (!player.getServitor().isDead()))
				healFull(player.getServitor());
			Location loc = getMap().getKeyLocations()[i];
			player.teleToLocation(loc, getReflection());
		}
	}

	private static void healFull(Playable playable)
	{
		cleanse(playable);
		playable.setCurrentHp(playable.getMaxHp(), false);
		playable.setCurrentMp(playable.getMaxMp());
		playable.setCurrentCp(playable.getMaxCp());
	}
	
	private static void cleanse(Playable playable)
	{
		try
		{
			for(Effect e : playable.getEffectList().getAllEffects())
			{
				if((e.isOffensive()) && (e.isCancelable())) {
					e.exit();
				}
			}
		}
		catch (IllegalStateException e) {}
	}

	public boolean canAttack(Creature target, Creature attacker, l2s.gameserver.model.Skill skill, boolean force)
	{
		if(getState() != AbstractFightClub.EVENT_STATE.STARTED)
			return false;
		if((target == null) || (!target.isPlayable()) || (attacker == null) || (!attacker.isPlayable()))
			return false;
		if((isFighting(target)) && (isFighting(attacker)))
			return true;
		return false;
	}
	
	private boolean isFighting(Creature actor)
	{
		for(FightClubPlayer fPlayer : _fightingPlayers)
		{
			if((fPlayer != null) && (fPlayer.getPlayer() != null) && (fPlayer.getPlayer().equals(actor.getPlayer())))
				return true;
		}
		return false;
	}
	
	protected static class CheckFightersInactive implements Runnable
	{
		private final KoreanStyleEvent _fightClub;
		
		public CheckFightersInactive(KoreanStyleEvent fightClub)
		{
			_fightClub = fightClub;
		}

		public void run()
		{
			if(_fightClub.getState() != AbstractFightClub.EVENT_STATE.STARTED) {
				return;
			}
			if(_fightClub._lastKill + 90000L < System.currentTimeMillis())
			{
				double playerToKillHp = 1.7976931348623157E+308D;
				Player playerToKill = null;
				for(FightClubPlayer fPlayer : _fightClub._fightingPlayers)
				{
					if((fPlayer != null) && (fPlayer.getPlayer() != null))
					{
						if(!fPlayer.getPlayer().getNetConnection().isConnected())
						{
							playerToKill = fPlayer.getPlayer();
							playerToKillHp = -100.0D;
						}
						else if(System.currentTimeMillis() - fPlayer.getPlayer().getLastNotAfkTime() > 8000L)
						{
							playerToKill = fPlayer.getPlayer();
							playerToKillHp = -1.0D;
						}
						else if(fPlayer.getPlayer().getCurrentHpPercents() < playerToKillHp)
						{
							playerToKill = fPlayer.getPlayer();
							playerToKillHp = fPlayer.getPlayer().getCurrentHpPercents();
						}
					}
				}
				
				if(playerToKill != null) {
					playerToKill.doDie(null);
				}
			}
			ThreadPoolManager.getInstance().schedule(this, 5000L);
		}
	}

	protected Location getSinglePlayerSpawnLocation(FightClubPlayer fPlayer)
	{
		Location[] spawnLocations = getMap().getTeamSpawns().get(fPlayer.getTeam().getIndex());
		int ordinalTeamIndex = fPlayer.getTeam().getIndex() - 1;
		int lastSpawnIndex = lastTeamChosenSpawn[ordinalTeamIndex];
		lastSpawnIndex++;
		if(lastSpawnIndex >= spawnLocations.length)
			lastSpawnIndex = 0;
		lastTeamChosenSpawn[ordinalTeamIndex] = lastSpawnIndex;
		return spawnLocations[lastSpawnIndex];
	}

	protected int getRewardForWinningTeam(FightClubPlayer fPlayer, boolean atLeast1Kill)
	{
		return super.getRewardForWinningTeam(fPlayer, false);
	}

	protected void handleAfk(FightClubPlayer fPlayer, boolean setAsAfk) {}

	protected void unrootPlayers() {}

	protected boolean inScreenShowBeScoreNotKills()
	{
		return false;
	}

	protected boolean inScreenShowBeTeamNotInvidual()
	{
		return false;
	}

	protected boolean isAfkTimerStopped(Player player)
	{
		return (player.isSitting()) || (super.isAfkTimerStopped(player));
	}

	public boolean canStandUp(Player player)
	{
		for(FightClubPlayer fPlayer : _fightingPlayers)
		{
			if((fPlayer != null) && (fPlayer.getPlayer().equals(player)))
				return true;
		}
		return false;
	}

	protected List<List<Player>> spreadTeamInPartys(FightClubTeam team)
	{
		return java.util.Collections.emptyList();
	}

	protected void createParty(List<Player> listOfPlayers)
	{
		//
	}
}

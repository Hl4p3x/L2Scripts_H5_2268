package l2s.gameserver.model.entity.events.impl.fightclub;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.fightclubmanager.FightClubPlayer;
import l2s.gameserver.model.entity.events.fightclubmanager.FightClubTeam;
import l2s.gameserver.model.entity.events.impl.AbstractFightClub;
import l2s.gameserver.model.entity.events.objects.CTFCombatFlagObject;
import l2s.gameserver.model.instances.NpcInstance;

public class CaptureTheFlagEvent extends AbstractFightClub
{
  private static final int FLAG_TO_STEAL_ID = 53004;
  private static final int FLAG_HOLDER_ID = 53005;
  private CaptureFlagTeam[] _flagTeams;
  private final int _badgesCaptureFlag;
  
  public CaptureTheFlagEvent(l2s.commons.collections.MultiValueSet<String> set)
  {
    super(set);
    _badgesCaptureFlag = set.getInteger("badgesCaptureFlag");
  }
  

  public void onKilled(Creature actor, Creature victim)
  {
    try
    {
      if ((actor != null) && (actor.isPlayable()))
      {
        FightClubPlayer realActor = getFightClubPlayer(actor.getPlayer());
        if ((victim.isPlayer()) && (realActor != null))
        {
          realActor.increaseKills(true);
          updatePlayerScore(realActor);
          sendMessageToPlayer(realActor, AbstractFightClub.MESSAGE_TYPES.GM, "You have killed " + victim.getName());
        }
        else if (!victim.isPet()) {}
        


        actor.getPlayer().sendUserInfo();
      }
      
      if (victim.isPlayer())
      {
        FightClubPlayer realVictim = getFightClubPlayer(victim);
        realVictim.increaseDeaths();
        if (actor != null)
          sendMessageToPlayer(realVictim, AbstractFightClub.MESSAGE_TYPES.GM, "You have been killed by " + actor.getName());
        victim.getPlayer().sendUserInfo();
        
        CaptureFlagTeam flagTeam = getTeam(realVictim.getTeam());
        
        if ((flagTeam != null) && (flagTeam._thisTeamHolder != null) && (flagTeam._thisTeamHolder.equals(realVictim)))
        {
          spawnFlag(getTeam(realVictim.getTeam()));
          
          flagTeam._thisTeamHolder = null;
        }
      }
      
      super.onKilled(actor, victim);
    }
    catch (Exception e)
    {
      _log.error("Error on CaptureTheFlag OnKilled!", e);
    }
  }
  

  @Override
  public void startEvent()
  {
    try
    {
      super.startEvent();
      _flagTeams = new CaptureFlagTeam[getTeams().size()];
      int i = 0;
      for (FightClubTeam team : getTeams())
      {
        CaptureFlagTeam flagTeam = new CaptureFlagTeam();
        flagTeam._team = team;
        flagTeam._holder = spawnNpc(53005, getFlagHolderSpawnLocation(team), 0);
        spawnFlag(flagTeam);
        _flagTeams[i] = flagTeam;
        i++;
      }
    }
    catch (Exception e) {
      _log.error("Error on CaptureTheFlag startEvent!", e);
    }
  }
  

  @Override
  public void stopEvent(boolean force)
  {
    try
    {
      super.stopEvent(force);
      for (CaptureFlagTeam iFlagTeam : _flagTeams)
      {
        if (iFlagTeam._flag != null)
          iFlagTeam._flag.deleteMe();
        if (iFlagTeam._holder != null)
          iFlagTeam._holder.deleteMe();
        if ((iFlagTeam._thisTeamHolder != null) && (iFlagTeam._thisTeamHolder.enemyFlagHoldByPlayer != null)) {
          iFlagTeam._thisTeamHolder.enemyFlagHoldByPlayer.despawnObject(this);
        }
      }
      _flagTeams = null;
    }
    catch (Exception e)
    {
      _log.error("Error on CaptureTheFlag stopEvent!", e);
    }
  }
  

























  public boolean tryToTakeFlag(Player player, NpcInstance flag)
  {
    try
    {
      FightClubPlayer fPlayer = getFightClubPlayer(player);
      if (fPlayer == null)
        return false;
      if (getState() != l2s.gameserver.model.entity.events.impl.AbstractFightClub.EVENT_STATE.STARTED) {
        return false;
      }
      CaptureFlagTeam flagTeam = null;
      for (CaptureFlagTeam iFlagTeam : _flagTeams) {
        if ((iFlagTeam._flag != null) && (iFlagTeam._flag.equals(flag)))
          flagTeam = iFlagTeam;
      }
      if (fPlayer.getTeam().equals(flagTeam._team))
      {
        giveFlagBack(fPlayer, flagTeam);
        return false;
      }
      

      return getEnemyFlag(fPlayer, flagTeam);

    }
    catch (Exception e)
    {
      _log.error("Error on CaptureTheFlag tryToTakeFlag!", e); }
    return false;
  }
  

  public void talkedWithFlagHolder(Player player, NpcInstance holder)
  {
    try
    {
      FightClubPlayer fPlayer = getFightClubPlayer(player);
      if (fPlayer == null)
        return;
      if (getState() != l2s.gameserver.model.entity.events.impl.AbstractFightClub.EVENT_STATE.STARTED) {
        return;
      }
      CaptureFlagTeam flagTeam = null;
      for (CaptureFlagTeam iFlagTeam : _flagTeams) {
        if ((iFlagTeam._holder != null) && (iFlagTeam._holder.equals(holder)))
          flagTeam = iFlagTeam;
      }
      if (fPlayer.getTeam().equals(flagTeam._team))
      {
        giveFlagBack(fPlayer, flagTeam);
      }
      else
      {
        boolean shouldFlagDissaper = getEnemyFlag(fPlayer, flagTeam);
        if (!shouldFlagDissaper) {}
      }
      








      return;
    }
    catch (Exception e)
    {
      _log.error("Error on CaptureTheFlag talkedWithFlagHolder!", e);
    }
  }
  



  private boolean getEnemyFlag(FightClubPlayer fPlayer, CaptureFlagTeam enemyFlagTeam)
  {
    try
    {
      CaptureFlagTeam goodTeam = getTeam(fPlayer.getTeam());
      Player player = fPlayer.getPlayer();
      
      if (goodTeam._flag != null)
      {
        goodTeam._flag.deleteMe();
        goodTeam._flag = null;
        

        CTFCombatFlagObject flag = new CTFCombatFlagObject();
        flag.spawnObject(this);
        player.getInventory().addItem(flag.getItem());
        player.getInventory().equipItem(flag.getItem());
        

        CaptureFlagHolder holder = new CaptureFlagHolder();
        holder.enemyFlagHoldByPlayer = flag;
        holder.playerHolding = fPlayer;
        holder.teamFlagOwner = goodTeam._team;
        goodTeam._thisTeamHolder = holder;
        
        sendMessageToTeam(goodTeam._team, AbstractFightClub.MESSAGE_TYPES.CRITICAL, "Someone stolen your Flag!");
        sendMessageToTeam(goodTeam._team, AbstractFightClub.MESSAGE_TYPES.CRITICAL, fPlayer.getPlayer().getName() + " stolen flag from " + goodTeam._team.getName() + " Team!");
        
        return true;
      }
      return false;
    }
    catch (Exception e)
    {
      _log.error("Error on CaptureTheFlag talkedWithFlagHolder!", e); }
    return false;
  }
  

  private CaptureFlagTeam getTeam(FightClubTeam team)
  {
    if (team == null) {
      return null;
    }
    try {
      for (CaptureFlagTeam iFlagTeam : _flagTeams)
        if ((iFlagTeam._team != null) && (iFlagTeam._team.equals(team)))
          return iFlagTeam;
      return null;
    }
    catch (Exception e)
    {
      _log.error("Error on CaptureTheFlag getTeam!", e); }
    return null;
  }
  





  private void giveFlagBack(FightClubPlayer fPlayer, CaptureFlagTeam flagTeam)
  {
    try
    {
      CaptureFlagHolder holdingTeam = flagTeam._thisTeamHolder;
      if ((holdingTeam != null) && (fPlayer.equals(holdingTeam.playerHolding)))
      {
        holdingTeam.enemyFlagHoldByPlayer.despawnObject(this);
        
        spawnFlag(getTeam(holdingTeam.teamFlagOwner));
        
        flagTeam._thisTeamHolder = null;
        flagTeam._team.incScore(1);
        updateScreenScores();
        
        for (FightClubTeam team : getTeams())
          if (!team.equals(flagTeam._team))
            sendMessageToTeam(holdingTeam.teamFlagOwner, AbstractFightClub.MESSAGE_TYPES.CRITICAL, flagTeam._team.getName() + " team gained score!");
        sendMessageToTeam(flagTeam._team, AbstractFightClub.MESSAGE_TYPES.CRITICAL, "You have gained score!");
        
        fPlayer.increaseEventSpecificScore("capture");
      }
    }
    catch (Exception e)
    {
      _log.error("Error on CaptureTheFlag giveFlagBack!", e);
    }
  }
  
  private l2s.gameserver.utils.Location getFlagHolderSpawnLocation(FightClubTeam team)
  {
    return getMap().getKeyLocations()[(team.getIndex() - 1)];
  }
  
  private void spawnFlag(CaptureFlagTeam flagTeam)
  {
    try
    {
      NpcInstance flag = spawnNpc(53004, getFlagHolderSpawnLocation(flagTeam._team), 0);
      flag.setName(flagTeam._team.getName() + " Flag");
      flag.broadcastCharInfo();
      flagTeam._flag = flag;
    }
    catch (Exception e)
    {
      _log.error("Error on CaptureTheFlag spawnFlag!", e);
    }
  }
  

  public String getVisibleTitle(Player player, String currentTitle, boolean toMe)
  {
    FightClubPlayer fPlayer = getFightClubPlayer(player);
    
    if (fPlayer == null) {
      return currentTitle;
    }
    return "Kills: " + fPlayer.getKills(true) + " Deaths: " + fPlayer.getDeaths();
  }
  
  private class CaptureFlagHolder
  {
    private FightClubPlayer playerHolding;
    private CTFCombatFlagObject enemyFlagHoldByPlayer;
    private FightClubTeam teamFlagOwner;
    
    private CaptureFlagHolder() {}
  }
  
  private class CaptureFlagTeam
  {
    private FightClubTeam _team;
    private NpcInstance _holder;
    private NpcInstance _flag;
    private CaptureTheFlagEvent.CaptureFlagHolder _thisTeamHolder;
    
    private CaptureFlagTeam() {}
  }
}

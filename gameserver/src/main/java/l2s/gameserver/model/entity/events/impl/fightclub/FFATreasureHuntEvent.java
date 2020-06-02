package l2s.gameserver.model.entity.events.impl.fightclub;

import java.util.Collection;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.fightclubmanager.FightClubPlayer;
import l2s.gameserver.model.entity.events.impl.AbstractFightClub;
import l2s.gameserver.model.instances.NpcInstance;

public class FFATreasureHuntEvent extends AbstractFightClub
{
  private static final int CHEST_ID = 37061;
  private final double badgesOpenChest;
  private final int scoreForKilledPlayer;
  private final int scoreForChest;
  private final long timeForRespawningChest;
  private final int numberOfChests;
  private final Collection<NpcInstance> spawnedChests;
  
  public FFATreasureHuntEvent(MultiValueSet<String> set)
  {
    super(set);
    badgesOpenChest = set.getDouble("badgesOpenChest");
    scoreForKilledPlayer = set.getInteger("scoreForKilledPlayer");
    scoreForChest = set.getInteger("scoreForChest");
    timeForRespawningChest = set.getLong("timeForRespawningChest");
    numberOfChests = set.getInteger("numberOfChests");
    spawnedChests = new java.util.concurrent.CopyOnWriteArrayList<NpcInstance>();
  }
  

  public void onKilled(Creature actor, Creature victim)
  {
    if ((actor != null) && (actor.isPlayable()))
    {
      FightClubPlayer realActor = getFightClubPlayer(actor.getPlayer());
      if (realActor != null)
      {
        if (victim.isPlayer())
        {
          realActor.increaseKills(true);
          realActor.increaseScore(scoreForKilledPlayer);
          updatePlayerScore(realActor);
          sendMessageToPlayer(realActor, AbstractFightClub.MESSAGE_TYPES.GM, "You have killed " + victim.getName());
        }
        actor.getPlayer().sendUserInfo();
      }
    }
    
    if (victim.isPlayer())
    {
      FightClubPlayer realVictim = getFightClubPlayer(victim);
      if (realVictim != null)
      {
        realVictim.increaseDeaths();
        if (actor != null)
          sendMessageToPlayer(realVictim, AbstractFightClub.MESSAGE_TYPES.GM, "You have been killed by " + actor.getName());
        victim.getPlayer().sendUserInfo();
      }
    }
    
    super.onKilled(actor, victim);
  }
  
  private void spawnChest()
  {
    spawnedChests.add(chooseLocAndSpawnNpc(37061, getMap().getKeyLocations(), 0));
  }
  

  public void startRound()
  {
    super.startRound();
    
    for (int i = 0; i < numberOfChests; i++) {
      spawnChest();
    }
  }
  
  @Override
  public void stopEvent(boolean force)
  {
    super.stopEvent(force);
    
    for (NpcInstance chest : spawnedChests)
      if ((chest != null) && (!chest.isDead()))
        chest.deleteMe();
    spawnedChests.clear();
  }
  



  public boolean openTreasure(Player player, NpcInstance npc)
  {
    FightClubPlayer fPlayer = getFightClubPlayer(player);
    if (fPlayer == null)
      return false;
    if (getState() != AbstractFightClub.EVENT_STATE.STARTED) {
      return false;
    }
    fPlayer.increaseEventSpecificScore("chest");
    fPlayer.increaseScore(scoreForChest);
    updatePlayerScore(fPlayer);
    player.sendUserInfo();
    
    ThreadPoolManager.getInstance().schedule(new SpawnChest(this), timeForRespawningChest * 1000L);
    
    spawnedChests.remove(npc);
    
    return true;
  }
  
  private static class SpawnChest implements Runnable
  {
    private final FFATreasureHuntEvent event;
    
    private SpawnChest(FFATreasureHuntEvent event) {
      this.event = event;
    }
    

    public void run()
    {
      if (event.getState() != AbstractFightClub.EVENT_STATE.NOT_ACTIVE) {
        event.spawnChest();
      }
    }
  }
  
  protected int getBadgesEarned(FightClubPlayer fPlayer, int currentValue, boolean isTopKiller)
  {
    int newValue = currentValue + addMultipleBadgeToPlayer(fPlayer.getEventSpecificScore("chest"), badgesOpenChest);
    return super.getBadgesEarned(fPlayer, newValue, isTopKiller);
  }
  

  public String getVisibleTitle(Player player, String currentTitle, boolean toMe)
  {
    FightClubPlayer fPlayer = getFightClubPlayer(player);
    
    if (fPlayer == null) {
      return currentTitle;
    }
    return "Chests: " + fPlayer.getEventSpecificScore("chest") + " Kills: " + fPlayer.getKills(true);
  }
}

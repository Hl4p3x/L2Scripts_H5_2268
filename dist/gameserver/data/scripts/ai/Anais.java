package ai;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.ReflectionUtils;

public class Anais extends Fighter
{
  private static Zone _zone;

  public Anais(NpcInstance actor)
  {
    super(actor);
    this.AI_TASK_ATTACK_DELAY = 1000;
    this.AI_TASK_ACTIVE_DELAY = 1000;
    _zone = ReflectionUtils.getZone("[four_sepulchers_1]");
  }
  
 /* @Override
  protected boolean maybeMoveToHome(boolean force)
  {
    NpcInstance actor = getActor();
    if (actor != null && !_zone.checkIfInZone(actor))
      teleportHome(true);
    return false;
  }*/

  public static Zone getZone()
  {
    return _zone;
  }

  public boolean canSeeInSilentMove(Playable target)
  {
    return (!target.isSilentMoving()) || (Rnd.chance(10));
  }
}
package l2s.gameserver.listener.actor;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Effect;

/**
 * @author Bonux
**/
public interface OnEffectRemoveListener extends CharListener
{
	public void onEffectRemove(Creature actor, Effect effect);
}

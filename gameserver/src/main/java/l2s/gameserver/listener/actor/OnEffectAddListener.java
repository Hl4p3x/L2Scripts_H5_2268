package l2s.gameserver.listener.actor;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Effect;

/**
 * @author Bonux
**/
public interface OnEffectAddListener extends CharListener
{
	public void onEffectAdd(Creature actor, Effect effect);
}

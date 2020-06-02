package l2s.gameserver.listener.actor;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.model.Creature;

/**
 * @author VISTALL
 * @date 20:50/13.03.2012
 */
public interface OnDeathFromUndyingListener extends CharListener
{
	void onDeathFromUndying(Creature actor, Creature killer);
}

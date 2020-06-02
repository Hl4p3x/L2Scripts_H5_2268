package l2s.gameserver.listener.actor;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.model.Creature;

/**
 * @author Bonux
**/
public interface OnChangeCurrentCpListener extends CharListener
{
	public void onChangeCurrentCp(Creature actor, double oldCp, double newCp);
}
package l2s.gameserver.handler.action;

import l2s.gameserver.handler.action.PlayerAction.ActionType;
import l2s.gameserver.model.Creature;

/**
 * @author Ro0TT
 * @date 19.12.2011
 **/

public interface IPlayerAction
{
   /**
	* Вызывается из разного рода классов, связанных с чарами.
	*/
	public void actionHandler(Creature character, ActionType action, Object... objects) throws Exception;
}

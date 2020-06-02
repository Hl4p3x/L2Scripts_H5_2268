package l2s.gameserver.handler.action;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import l2s.gameserver.model.Creature;
/**
 * @author Ro0TT
 * @date 19.12.2011
 * Класс является хранилищем скриптов, которые используют Action Handler.
 **/

public class PlayerAction 
{
	public enum ActionType
	{
		PLAYER_ON_LOGIN,
		PLAYER_ON_LOGOFF,
		PLAYER_USE_ITEM,
		PLAYER_SCRIPT_REQUEST_OK,
		PLAYER_SCRIPT_REQUEST_CANCEL
	}
	
	private static Logger _log = Logger.getLogger(PlayerAction.class.getName());

	private List<IPlayerAction> _handlers = new ArrayList<IPlayerAction>();

	private static PlayerAction _instance;

	public static PlayerAction getInstance()
	{
		if (_instance==null)
			_instance = new PlayerAction();
		return _instance;
	}

	public void regHandler(IPlayerAction handler)
	{
		_handlers.add(handler);
	}

	public void useHandler(Creature character, ActionType action, Object... objects)
	{
		for (IPlayerAction handler : _handlers)
		{
			try
			{
				handler.actionHandler(character, action, objects);
			}
			catch (Exception e)
			{
				_log.warning("Eroror use handler: ");
				e.printStackTrace();
			}
		}
	}
}

package l2s.gameserver.templates.fakeplayer;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

import l2s.gameserver.templates.fakeplayer.actions.OrdinaryActions;

/**
 * @author Bonux
**/
public class FakePlayerActionsHolder
{
	private final IntObjectMap<OrdinaryActions> _actionsMap = new HashIntObjectMap<OrdinaryActions>();

	public void addAction(int id, OrdinaryActions action)
	{
		_actionsMap.put(id, action);
	}

	public OrdinaryActions getAction(int id)
	{
		return _actionsMap.get(id);
	}
}
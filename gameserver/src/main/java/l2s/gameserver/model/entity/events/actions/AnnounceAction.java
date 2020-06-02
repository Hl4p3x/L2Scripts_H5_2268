package l2s.gameserver.model.entity.events.actions;

import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventAction;

/**
 * @author VISTALL
 * @date 11:12/11.03.2011
 */
public class AnnounceAction implements EventAction
{
	private final int _id;
	private final String _value;
	private final int _time;

	public AnnounceAction(int id, String value, int time)
	{
		_id = id;
		_value = value;
		_time = time;
	}

	@Override
	public void call(Event event)
	{
		event.announce(_id, _value, _time);
	}
}

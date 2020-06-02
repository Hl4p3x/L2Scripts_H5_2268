package l2s.gameserver.model.entity.events;

import l2s.gameserver.taskmanager.actionrunner.ActionWrapper;

/**
 * @author VISTALL
 * @date  18:02/10.12.2010
 */
public class EventWrapper extends ActionWrapper
{
	private final Event _event;
	private final int _time;

	public EventWrapper(String name, Event event, int time)
	{
		super(name);
		_event = event;
		_time = time;
	}

	@Override
	public void runImpl0() throws Exception
	{
		_event.timeActions(_time);
	}
}

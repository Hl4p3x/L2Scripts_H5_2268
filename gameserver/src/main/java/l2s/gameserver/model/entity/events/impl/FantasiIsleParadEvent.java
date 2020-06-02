package l2s.gameserver.model.entity.events.impl;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventType;

/**
 * @author PaInKiLlEr
 */
public class FantasiIsleParadEvent extends Event
{
	public FantasiIsleParadEvent(MultiValueSet<String> set)
	{
		super(set);
	}

	@Override
	public void reCalcNextTime(boolean onStart)
	{
		clearActions();
	}

	@Override
	public EventType getType()
	{
		return EventType.MAIN_EVENT;
	}

	@Override
	protected long startTimeMillis()
	{
		return System.currentTimeMillis() + 30000L;
	}
}
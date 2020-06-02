package l2s.gameserver.model.entity.events.impl;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventType;

public class UndergroundColiseumBattleEvent extends Event
{
	protected UndergroundColiseumBattleEvent(Player player1, Player player2)
	{
		super(0, player1.getObjectId() + "_" + player2.getObjectId());
	}

	@Override
	public void announce(int id, String value, int time)
	{
		if(id == 1)
		{
			switch(time)
			{
				case -180:
				case -120:
				case -60:
					break;
			}
		}
	}

	@Override
	public void reCalcNextTime(boolean onInit)
	{
		registerActions();
	}

	@Override
	protected long startTimeMillis()
	{
		return System.currentTimeMillis() + 180000L;
	}

	@Override
	public EventType getType()
	{
		return EventType.PVP_EVENT;
	}
}

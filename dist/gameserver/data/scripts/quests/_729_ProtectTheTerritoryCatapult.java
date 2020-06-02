package quests;

import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.DominionSiegeRunnerEvent;

/**
 * @author VISTALL
 * @date 2:15/09.06.2011
 */
public class _729_ProtectTheTerritoryCatapult extends QuestScript
{
	public _729_ProtectTheTerritoryCatapult()
	{
		super(PARTY_ALL, REPEATABLE);
		DominionSiegeRunnerEvent runnerEvent = EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 1);
		runnerEvent.addBreakQuest(this);
	}
}

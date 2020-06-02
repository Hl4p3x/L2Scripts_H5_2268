package quests;

import org.apache.commons.lang3.ArrayUtils;

import l2s.commons.util.Rnd;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2s.gameserver.model.entity.events.impl.DominionSiegeRunnerEvent;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;

/**
 * @author VISTALL
 * @date 15:51/12.04.2011
 */
public abstract class Dominion_KillSpecialUnitQuest extends QuestScript
{
	private final ClassId[] _classIds;
	private DominionSiegeRunnerEvent _runnerEvent;
	public Dominion_KillSpecialUnitQuest()
	{
		super(PARTY_ALL, DAILY);

		_classIds = getTargetClassIds();
		DominionSiegeRunnerEvent runnerEvent = EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 1);
		for(ClassId c : _classIds)
			runnerEvent.addClassQuest(c, this);
	}

	protected abstract NpcString startNpcString();

	protected abstract NpcString progressNpcString();

	protected abstract NpcString doneNpcString();

	protected abstract int getRandomMin();

	protected abstract int getRandomMax();

	protected abstract ClassId[] getTargetClassIds();

	@Override
	public String onKill(Player killed, QuestState qs)
	{
		Player player = qs.getPlayer();
		if(player == null)
			return null;

		DominionSiegeEvent event1 = player.getEvent(DominionSiegeEvent.class);
		if(event1 == null)
			return null;
		DominionSiegeEvent event2 = killed.getEvent(DominionSiegeEvent.class);
		if(event1 == null || event2 == null || event2 == event1)
			return null;

		if(!ArrayUtils.contains(_classIds, killed.getClassId()))
			return null;
		int max_kills = qs.getInt("max_kills");


			if (qs.isNotAccepted())
			{
				if (max_kills == 0)
				{
					qs.setCond(1);
					max_kills = Rnd.get(getRandomMin(), getRandomMax());
					qs.set("max_kills", max_kills);
					qs.set("current_kills", 1);
					player.sendPacket(new ExShowScreenMessage(startNpcString(), 2000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true, false, String.valueOf(qs.getInt("max_kills"))));
				}
				else
				{
					int current_kills = qs.getInt("current_kills") + 1;
					if (current_kills >= max_kills)
					{
						player.sendPacket(new ExShowScreenMessage(doneNpcString(), 2000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true, false));
						event1.addReward(player, DominionSiegeEvent.STATIC_BADGES, 10);
						qs.addExpAndSp(534000, 51000);
						qs.finishQuest();
					}
					else
					{
						player.sendPacket(new ExShowScreenMessage(progressNpcString(), 2000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true, false, String.valueOf(qs.getInt("max_kills")), String.valueOf(qs.getInt("current_kills") + 1)));
						qs.set("current_kills", qs.getInt("current_kills") + 1);
					}
				}
			}


		return null;
	}

	@Override
	public boolean canAbortByPacket()
	{
		return false;
	}
}

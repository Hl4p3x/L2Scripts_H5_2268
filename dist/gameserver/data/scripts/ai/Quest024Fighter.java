package ai;

import l2s.gameserver.ai.Fighter;
import l2s.gameserver.data.QuestHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;

/**
 * @author VISTALL
 */
public class Quest024Fighter extends Fighter
{
	public Quest024Fighter(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		Quest q = QuestHolder.getInstance().getQuest(24);
		if(q != null)
			for(Player player : World.getAroundPlayers(getActor(), 300, 200))
			{
				QuestState questState = player.getQuestState(q);
				if(questState != null && questState.getCond() == 3)
					q.notifyEvent("seePlayer", questState, getActor());
			}

		return super.thinkActive();
	}
}
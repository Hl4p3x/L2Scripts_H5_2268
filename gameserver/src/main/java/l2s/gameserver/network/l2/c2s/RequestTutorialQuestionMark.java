package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.QuestHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.fightclubmanager.FightClubEventManager;
import l2s.gameserver.model.quest.Quest;

public class RequestTutorialQuestionMark extends L2GameClientPacket
{
	// format: cd
	int _number = 0;

	@Override
	protected void readImpl()
	{
		_number = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		player.isntAfk();

		if(player.isInFightClub())
		{
			FightClubEventManager.getInstance().sendEventPlayerMenu(player);
			return;
		}

		Quest q = QuestHolder.getInstance().getQuest(255);
		if(q != null)
			player.processQuestEvent(q.getId(), "QM" + _number, null);
	}
}
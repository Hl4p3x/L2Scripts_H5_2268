package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.QuestHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.fightclubmanager.FightClubEventManager;
import l2s.gameserver.model.quest.Quest;

public class RequestTutorialPassCmdToServer extends L2GameClientPacket
{
	// format: cS

	String _bypass = null;

	@Override
	protected void readImpl()
	{
		_bypass = readS();
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
			FightClubEventManager.getInstance().requestEventPlayerMenuBypass(player, _bypass);
			return;
		}

		Quest tutorial = QuestHolder.getInstance().getQuest(255);

		if(tutorial != null)
			player.processQuestEvent(tutorial.getId(), _bypass, null);
	}
}
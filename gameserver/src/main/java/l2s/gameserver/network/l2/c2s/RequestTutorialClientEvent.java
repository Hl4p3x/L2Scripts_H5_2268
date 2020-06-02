package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.QuestHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.quest.Quest;

public class RequestTutorialClientEvent extends L2GameClientPacket
{
	// format: cd
	int event = 0;

	/**
	 * Пакет от клиента, если вы в туториале подергали мышкой как надо - клиент пришлет его со значением 1 ну или нужным ивентом
	 */
	@Override
	protected void readImpl()
	{
		event = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		Quest tutorial = QuestHolder.getInstance().getQuest(255);
		if(tutorial != null)
			player.processQuestEvent(tutorial.getId(), "CE" + event, null);
	}
}
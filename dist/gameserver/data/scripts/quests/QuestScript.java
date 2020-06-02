package quests;

import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestPartyType;
import l2s.gameserver.model.quest.QuestRepeatType;
import l2s.gameserver.scripts.ScriptFile;

/**
 * @author Bonux
**/
public abstract class QuestScript extends Quest implements ScriptFile
{
	public QuestScript(QuestPartyType partyType, QuestRepeatType repeatType)
	{
		super(partyType, repeatType);
	}

	@Override
	public void onLoad()
	{
		//
	}

	@Override
	public void onReload()
	{
		//
	}

	@Override
	public void onShutdown()
	{
		//
	}
}
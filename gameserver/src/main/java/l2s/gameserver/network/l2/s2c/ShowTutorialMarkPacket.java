package l2s.gameserver.network.l2.s2c;

public class ShowTutorialMarkPacket extends L2GameServerPacket
{
	/**
	 * После клика по знаку вопроса клиент попросит html-ку с этим номером.
	 */
	private boolean _quest;
	private int _tutorialId;

	public ShowTutorialMarkPacket(boolean quest, int tutorialId)
	{
		_quest = quest;
		_tutorialId = tutorialId;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_quest);
		writeD(_tutorialId);
	}

	@Override
	protected final void writeImplHF()
	{
		writeD(_tutorialId);
	}
}
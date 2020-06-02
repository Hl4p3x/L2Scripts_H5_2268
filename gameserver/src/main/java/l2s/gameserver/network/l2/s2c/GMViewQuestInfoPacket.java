package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;

public class GMViewQuestInfoPacket extends L2GameServerPacket
{
	private final Player _cha;

	public GMViewQuestInfoPacket(Player cha)
	{
		_cha = cha;
	}

	@Override
	protected final void writeImpl()
	{
		writeS(_cha.getName());

		Quest[] quests = _cha.getAllActiveQuests();

		if(quests.length == 0)
		{
			writeH(0);
			writeH(0);
			return;
		}

		writeH(quests.length);
		for(Quest q : quests)
		{
			writeD(q.getId());
			QuestState qs = _cha.getQuestState(q);
			writeD(qs == null ? 0 : qs.getCondsMask());
		}

		writeH(0); //количество элементов типа: ddQd , как-то связано с предметами
	}
}
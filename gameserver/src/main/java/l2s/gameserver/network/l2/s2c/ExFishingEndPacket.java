package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

/**
 * Format: (ch) dc
 * d: character object id
 * c: 1 if won 0 if failed
 */
public class ExFishingEndPacket extends L2GameServerPacket
{
	private int _charId;
	private boolean _win;

	public ExFishingEndPacket(Player character, boolean win)
	{
		_charId = character.getObjectId();
		_win = win;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_charId);
		writeC(_win ? 1 : 0);
	}
}
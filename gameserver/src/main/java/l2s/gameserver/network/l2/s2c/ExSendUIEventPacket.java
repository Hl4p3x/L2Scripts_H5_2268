package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.NpcString;

/**
 * @author Bonux
**/
public class ExSendUIEventPacket extends NpcStringContainer
{
	private final int _objectId;
	private final int _type;
	private final int _countUp;
	private final int _startTime;
	private final int _startTime2;
	private final int _endTime;
	private final int _endTime2;

	public ExSendUIEventPacket(Player player, boolean hide, boolean increase, int startTime, int endTime, String... params)
	{
		this(player, hide ? 1 : 0, increase ? 1 : 0, startTime, endTime, params);
	}

	public ExSendUIEventPacket(Player player, boolean hide, boolean increase, int startTime, int endTime, NpcString npcString, String... params)
	{
		this(player, hide ? 1 : 0, increase ? 1 : 0, startTime, endTime, npcString, params);
	}

	public ExSendUIEventPacket(Player player, int type, int countUp, int startTime, int endTime, String... params)
	{
		this(player, type, countUp, startTime / 60, startTime % 60, endTime / 60, endTime % 60, NpcString.NONE, params);
	}

	public ExSendUIEventPacket(Player player, int type, int countUp, int startTime, int endTime, NpcString npcString, String... params)
	{
		this(player, type, countUp, startTime / 60, startTime % 60, endTime / 60, endTime % 60, npcString, params);
	}

	public ExSendUIEventPacket(Player player, int type, int countUp, int startTime, int startTime2, int endTime, int endTime2, String... params)
	{
		this(player, type, countUp, startTime, startTime2, endTime, endTime2, NpcString.NONE, params);
	}

	public ExSendUIEventPacket(Player player, int type, int countUp, int startTime, int startTime2, int endTime, int endTime2, NpcString npcString, String... params)
	{
		super(npcString, params);
		_objectId = player.getObjectId();
		_type = type;
		_countUp = countUp;
		_startTime = startTime;
		_startTime2 = startTime2;
		_endTime = endTime;
		_endTime2 = endTime2;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objectId);
		// 0 - Timer: MM:SS (Description on bottom)
		// 1 - Disable Timer
		// 2 - Green Line with percents and timer
		// 3 - Number of residues collected (N pcs.)
		// 4 - Timer: MM:SS (Description on top)
		// 5 - Blue Line with percents and timer
		// 6 - Yellow Egg progress
		// 7 - Red Egg progress
		// 5 - Line with gears, percents and timer
		writeD(_type);
		writeD(0x00);// unknown
		writeD(0x00);// unknown
		writeS(String.valueOf(_countUp)); // 0 = count down, 1 = count up timer always disappears 10 seconds before end
		writeS(String.valueOf(_startTime));
		writeS(String.valueOf(_startTime2));
		writeS(String.valueOf(_endTime));
		writeS(String.valueOf(_endTime2));
		writeElements();
	}
}
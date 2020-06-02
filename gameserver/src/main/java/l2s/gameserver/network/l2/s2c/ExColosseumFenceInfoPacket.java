package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.instances.FenceInstance;

/**
 * @author HoridoJoho / FBIagent
 * @reworked by Bonux
 */
public class ExColosseumFenceInfoPacket extends L2GameServerPacket
{
	private final int _objId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _width;
	private final int _length;
	private final int _state;

	public ExColosseumFenceInfoPacket(int objId, int x, int y, int z, int width, int length, int state)
	{
		_objId = objId;
		_x = x;
		_y = y;
		_z = z;
		_width = width;
		_length = length;
		_state = state;
	}

	public ExColosseumFenceInfoPacket(FenceInstance fence)
	{
		this(fence.getObjectId(), fence.getX(), fence.getY(), fence.getZ(), fence.getWidth(), fence.getLength(), fence.getState().getClientId());
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objId);
		writeD(_state);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_width);
		writeD(_length);
	}
}
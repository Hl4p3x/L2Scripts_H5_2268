package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.entity.events.objects.KrateisCubePlayerObject;

/**
 * @author VISTALL
 */
public class ExPVPMatchCCMyRecord extends L2GameServerPacket
{
	private int _points;

	public ExPVPMatchCCMyRecord(KrateisCubePlayerObject player)
	{
		_points = player.getPoints();
	}

	@Override
	public void writeImpl()
	{
		writeD(_points);
	}
}
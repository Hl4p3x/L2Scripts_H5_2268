package l2s.gameserver.network.l2.s2c;

public class ExAttackInAirShipPacket extends L2GameServerPacket
{
	/*
	 * заготовка!!!
	 * Format: dddcddddh[ddc]
	 * ExAttackInAirShipPacket AttackerID:%d DefenderID:%d Damage:%d bMiss:%d bCritical:%d AirShipID:%d
	 */

	@Override
	protected final void writeImpl()
	{
		//
	}
}
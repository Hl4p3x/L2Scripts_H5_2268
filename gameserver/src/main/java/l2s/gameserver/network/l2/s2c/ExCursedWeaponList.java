package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.instancemanager.CursedWeaponsManager;

public class ExCursedWeaponList extends L2GameServerPacket
{
	private int[] cursedWeapon_ids;

	public ExCursedWeaponList()
	{
		cursedWeapon_ids = CursedWeaponsManager.getInstance().getCursedWeaponsIds();
	}

	@Override
	protected final void writeImpl()
	{
		writeDD(cursedWeapon_ids, true);
	}
}
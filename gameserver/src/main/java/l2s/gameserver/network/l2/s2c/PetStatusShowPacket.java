package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Servitor;

public class PetStatusShowPacket extends L2GameServerPacket
{
	private int _summonType;
	private int _summonObjId;

	public PetStatusShowPacket(Servitor servitor)
	{
		_summonType = servitor.getServitorType();
		_summonObjId = servitor.getObjectId();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_summonType);
		writeD(_summonObjId);
	}

	@Override
	protected final void writeImplHF()
	{
		writeD(_summonType);
	}
}
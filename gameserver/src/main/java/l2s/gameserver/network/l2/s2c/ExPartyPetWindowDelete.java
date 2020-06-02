package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Servitor;

public class ExPartyPetWindowDelete extends L2GameServerPacket
{
	private int _summonObjectId;
	private int _ownerObjectId;
	private String _summonName;
	private int _type;

	public ExPartyPetWindowDelete(Servitor servitor)
	{
		_summonObjectId = servitor.getObjectId();
		_summonName = servitor.getName();
		_ownerObjectId = servitor.getPlayer().getObjectId();
		_type = servitor.getServitorType();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_summonObjectId);
		writeD(_type);
		writeD(_ownerObjectId);
		writeS(_summonName);
	}

	@Override
	protected final void writeImplHF()
	{
		writeD(_summonObjectId);
		writeD(_ownerObjectId);
		writeS(_summonName);
	}
}
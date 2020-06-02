package l2s.gameserver.network.l2.s2c;

public class PetDeletePacket extends L2GameServerPacket
{
	private int _petId;
	private int _petnum;

	public PetDeletePacket(int petId, int petnum)
	{
		_petId = petId;
		_petnum = petnum;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_petnum);
		writeD(_petId);
	}

	@Override
	protected final void writeImplHF()
	{
		writeD(_petId);// dont really know what these two are since i never needed them
		writeD(_petnum);
	}
}
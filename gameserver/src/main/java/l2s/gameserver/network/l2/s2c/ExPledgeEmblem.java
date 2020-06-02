package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;

public class ExPledgeEmblem extends L2GameServerPacket
{
	private int _clanId, _crestId, _crestPart, _totalSize;
	private byte[] _data;

	public ExPledgeEmblem(int clanId, int crestId, int crestPart, int totalSize, byte[] data)
	{
		_clanId = clanId;
		_crestId = crestId;
		_crestPart = crestPart;
		_totalSize = totalSize;
		_data = data;
	}

	public ExPledgeEmblem(int crestId, byte[] data)
	{
		_clanId = 0;
		_crestId = crestId;
		_data = data;
	}

	@Override
	protected boolean canWrite()
	{
		return _clanId > 0;
	}

	@Override
	protected boolean canWriteHF()
	{
		return _clanId == 0;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(Config.REQUEST_ID);
		writeD(_clanId);
		writeD(_crestId);
		writeD(_crestPart);
		writeD(_totalSize);
		writeD(_data.length);
		writeB(_data);
	}
	
	@Override
	protected void writeImplHF()
	{
		writeD(0x00); // UNK
		writeD(_crestId);
		writeD(_data.length);
		writeB(_data);
	}
}
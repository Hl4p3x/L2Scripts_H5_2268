package l2s.gameserver.network.l2.s2c;

public class SnoopPacket extends L2GameServerPacket
{
	private int _convoID;
	private String _name;
	private int _type;
	private String _speaker;
	private String _msg;

	public SnoopPacket(int id, String name, int type, String speaker, String msg)
	{
		_convoID = id;
		_name = name;
		_type = type;
		_speaker = speaker;
		_msg = msg;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_convoID);
		writeS(_name);
		writeD(0x00); // ??
		writeD(_type);
		writeS(_speaker);
		//writeD(_fStringId);
		/*for(String param : _params)
			writeS(param);*/
		writeS(_msg);
	}

	@Override
	protected final void writeImplHF()
	{
		writeD(_convoID);
		writeS(_name);
		writeD(0x00);
		writeD(_type);
		writeS(_speaker);
		writeS(_msg);
	}
}
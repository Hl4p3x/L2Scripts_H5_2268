package l2s.gameserver.network.l2.c2s;

/**
 * format: ddS
 */
public class PetitionVote extends L2GameClientPacket
{
	private int _type, _unk1;
	private String _petitionText;

	@Override
	protected void runImpl()
	{}

	@Override
	protected void readImpl()
	{
		set_type(readD());
		set_unk1(readD()); // possible always zero
		set_petitionText(readS(4096));
		// not done
	}

	public String get_petitionText() {
		return _petitionText;
	}

	public void set_petitionText(String _petitionText) {
		this._petitionText = _petitionText;
	}

	public int get_unk1() {
		return _unk1;
	}

	public void set_unk1(int _unk1) {
		this._unk1 = _unk1;
	}

	public int get_type() {
		return _type;
	}

	public void set_type(int _type) {
		this._type = _type;
	}
}
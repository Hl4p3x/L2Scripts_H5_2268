package l2s.gameserver.templates.player;

/**
 * @author Bonux
 */
public final class BaseJewelDefence
{
	private final int _r_earring;
	private final int _l_earring;
	private final int _r_ring;
	private final int _l_ring;
	private final int _necklace;

	public BaseJewelDefence(int r_earring, int l_earring, int r_ring, int l_ring, int necklace)
	{
		_r_earring = r_earring;
		_l_earring = l_earring;
		_r_ring = r_ring;
		_l_ring = l_ring;
		_necklace = necklace;
	}

	public int getREaaringDef()
	{
		return _r_earring;
	}

	public int getLEaaringDef()
	{
		return _l_earring;
	}

	public int getRRingDef()
	{
		return _r_ring;
	}

	public int getLRingDef()
	{
		return _l_ring;
	}

	public int getNecklaceDef()
	{
		return _necklace;
	}
}

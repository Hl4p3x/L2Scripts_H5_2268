package l2s.gameserver.templates.player;

/**
 * @author Bonux
 */
public final class BaseArmorDefence
{
	private final int _chest;
	private final int _legs;
	private final int _helmet;
	private final int _boots;
	private final int _gloves;
	private final int _underwear;
	private final int _cloak;

	public BaseArmorDefence(int chest, int legs, int helmet, int boots, int gloves, int underwear, int cloak)
	{
		_chest = chest;
		_legs = legs;
		_helmet = helmet;
		_boots = boots;
		_gloves = gloves;
		_underwear = underwear;
		_cloak = cloak;
	}

	public int getChestDef()
	{
		return _chest;
	}

	public int getLegsDef()
	{
		return _legs;
	}

	public int getHelmetDef()
	{
		return _helmet;
	}

	public int getBootsDef()
	{
		return _boots;
	}

	public int getGlovesDef()
	{
		return _gloves;
	}

	public int getUnderwearDef()
	{
		return _underwear;
	}

	public int getCloakDef()
	{
		return _cloak;
	}
}

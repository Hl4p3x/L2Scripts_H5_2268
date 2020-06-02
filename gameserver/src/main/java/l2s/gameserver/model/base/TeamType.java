package l2s.gameserver.model.base;

/**
 * @author VISTALL
 * @date 1:14/16.06.2011
 */
public enum TeamType
{
	NONE,
	BLUE,
	RED;

	public TeamType revert()
	{
		return this == BLUE ? RED : this == RED ? BLUE : NONE;
	}
}

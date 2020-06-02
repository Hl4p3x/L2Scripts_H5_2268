package l2s.gameserver.model.actor.instances.player;

/**
 * @author Bonux
 */
public class Block
{
	private final int _objectId;
	private String _name;

	public Block(int objectId, String name)
	{
		_objectId = objectId;
		_name = name;
	}

	public int getObjectId()
	{
		return _objectId;
	}

	public String getName()
	{
		return _name;
	}
}
package l2s.gameserver.model.actor.flags;

import l2s.gameserver.model.Playable;
import l2s.gameserver.model.actor.flags.flag.DefaultFlag;

/**
 * @author Bonux
**/
public class PlayableFlags extends CreatureFlags
{
	private final DefaultFlag _silentMoving = new DefaultFlag();

	public PlayableFlags(Playable owner)
	{
		super(owner);
	}

	public DefaultFlag getSilentMoving()
	{
		return _silentMoving;
	}
}
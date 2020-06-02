package l2s.gameserver.utils;

import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.base.FenceState;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.FenceInstance;

/**
 * @author Bonux
**/
public class FenceUtils
{
	public static FenceInstance spawnFence(String name, int x, int y, int z, int width, int length, int height, FenceState state, Reflection reflection)
	{
		FenceInstance instance = new FenceInstance(IdFactory.getInstance().getNextId(), name, width, length, height, state);
		instance.setReflection(reflection);
		instance.spawnMe(new Location(x, y, z));
		return instance;
	}

	public static FenceInstance spawnFence(String name, int x, int y, int z, int width, int length, int height, FenceState state)
	{
		return spawnFence(name, x, y, z, width, length, height, state, ReflectionManager.DEFAULT);
	}
}

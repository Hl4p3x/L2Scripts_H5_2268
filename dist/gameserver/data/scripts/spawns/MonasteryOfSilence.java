package spawns;

import l2s.gameserver.instancemanager.SpawnManager;
import l2s.gameserver.scripts.ScriptFile;

/**
 * @author Bonux
 */
public class MonasteryOfSilence implements ScriptFile
{
	private static final String[] SPAWN_GROUPS = {
			"rune13_2315_45m2", "rune13_2315_46m1",
			"rune13_2315_43m2", "rune13_2315_44m1",
			"rune13_2315_35m2", "rune13_2315_36m1",
			"rune13_2315_33m2", "rune13_2315_34m1",
			"rune13_2315_31m2", "rune13_2315_30m1",
			"rune13_2315_28m2", "rune13_2315_29m1",
			"rune13_2315_40m2", "rune13_2315_41m1",
			"rune13_2315_38m2", "rune13_2315_39m1"
		};

	@Override
	public void onLoad()
	{
		init();
	}

	@Override
	public void onReload()
	{
		//
	}

	@Override
	public void onShutdown()
	{
		//
	}

	private void init()
	{
		for(String group : SPAWN_GROUPS)
			SpawnManager.getInstance().spawn(group);
	}
}
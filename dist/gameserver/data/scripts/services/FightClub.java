package services;

import java.util.ArrayList;

import l2s.gameserver.Config;
import l2s.gameserver.model.SimpleSpawner;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.scripts.ScriptFile;

public class FightClub extends Functions implements ScriptFile
{
	private static final ArrayList<SimpleSpawner> _spawns_fight_club_manager = new ArrayList<SimpleSpawner>();

	public static int FIGHT_CLUB_MANAGER = 112;

	private void spawnFightClub()
	{
		final int FIGHT_CLUB_MANAGER_SPAWN[][] = {

		{ 82152, 149704, -3494, 0 }, // Giran
		{ 146408, 28408, -2292, 49151 }, // Aden 1
		{ 148504, 28408, -2294, 49151 }, // Aden 2
		{ 145816, -57224, -3006, 51707 }, //Goddard 1
		{ 150232, -56440, -3006, 8191 }, //Goddard 2
		{ 43640, -46664, -822, 18442 }, //Rune
		{ 19416, 144936, -3129, 45796 }, //Dion 1
		{ 17816, 144184, -3070, 47854 }, //Dion 2
		{ 83016, 55304, -1550, 0 }, //Oren 1
		{ 80104, 53496, -1585, 49151 }, //Oren 2
		{ -15064, 124168, -3142, 49151 }, //Gludio 1
		{ -12312, 122552, -3130, 32767 }, //Gludio 2
		{ -82776, 149880, -3154, 32767 }, //Gludin 1
		{ -81688, 155368, -3203, 0 }, //Gludin 2
		{ 89224, -141464, -1566, 19239 }, //Shuttgart 1
		{ 87560, -140728, -1566, 28544 }, //Shuttgart 2
		{ 115368, 218728, -3688, 32767 }, //Heine 1
		{ 107528, 217704, -3700, 0 }, //Heine 2
		{ 116808, 75448, -2748, 19239 }, //Hunter's Village 
		};

		SpawnNPCs(FIGHT_CLUB_MANAGER, FIGHT_CLUB_MANAGER_SPAWN, _spawns_fight_club_manager);
	}

	@Override
	public void onLoad()
	{
		if(Config.FIGHT_CLUB_ENABLED)
			spawnFightClub();
	}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}
}
package ai;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.utils.Location;

/**
 * @author Bonux
**/
public class Rooney extends DefaultAI
{
	static final Location[] SPAWN_POINTS = {
		new Location(175937, -112167, -5550),
		new Location(178896, -112425, -5860),
		new Location(180628, -115992, -6135),
		new Location(183010, -114753, -6135),
		new Location(184496, -116773, -6135),
		new Location(181857, -109491, -5865),
		new Location(178917, -107633, -5853),
		new Location(178804, -110080, -5853),
		new Location(182221, -106806, -6025),
		new Location(186488, -109715, -5915),
		new Location(183847, -119231, -3113),
		new Location(185193, -120342, -3113),
		new Location(188047, -120867, -3113),
		new Location(189734, -120471, -3113),
		new Location(188754, -118940, -3313),
		new Location(190022, -116803, -3313),
		new Location(188443, -115814, -3313),
		new Location(186421, -114614, -3313),
		new Location(185188, -113307, -3313),
		new Location(187378, -112946, -3313),
		new Location(189815, -113425, -3313),
		new Location(189301, -111327, -3313),
		new Location(190289, -109176, -3313),
		new Location(187783, -110478, -3313),
		new Location(185889, -109990, -3313),
		new Location(181881, -109060, -3695),
		new Location(183570, -111344, -3675),
		new Location(182077, -112567, -3695),
		new Location(180127, -112776, -3698),
		new Location(179155, -108629, -3695),
		new Location(176282, -109510, -3698),
		new Location(176071, -113163, -3515),
		new Location(179376, -117056, -3640),
		new Location(179760, -115385, -3640),
		new Location(177950, -119691, -4140),
		new Location(177037, -120820, -4340),
		new Location(181125, -120148, -3702),
		new Location(182212, -117969, -3352),
		new Location(186074, -118154, -3312)
	};

	private boolean _teleporting = false;

	public Rooney(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtTimer(int timerId, Object arg1, Object arg2)
	{
		if(timerId == 5000)
		{
			getActor().teleToLocation(SPAWN_POINTS[Rnd.get(SPAWN_POINTS.length)]);
			_teleporting = false;
		}
		else if(timerId == 5001)
		{
			Functions.npcSay(getActor(), NpcString.HURRY_HURRY);
			addTimer(5002, 60 * 1000);
		}
		else if(timerId == 5002)
		{
			Functions.npcSay(getActor(), NpcString.I_AM_NOT_THAT_TYPE_OF_PERSON_WHO_STAYS_IN_ONE_PLACE_FOR_A_LONG_TIME);
			addTimer(5003, 60 * 1000);
		}
		else if(timerId == 5003)
		{
			Functions.npcSay(getActor(), NpcString.ITS_HARD_FOR_ME_TO_KEEP_STANDING_LIKE_THIS);
			addTimer(5004, 60 * 1000);
		}
		else if(timerId == 5004)
		{
			Functions.npcSay(getActor(), NpcString.WHY_DONT_I_GO_THAT_WAY_THIS_TIME);
		}
		else
			super.onEvtTimer(timerId, arg1, arg2);
	}

	@Override
	protected boolean thinkActive()
	{
		if(!_teleporting)
		{
			if(!World.getAroundPlayers(getActor(), 500, 200).isEmpty())
			{
				_teleporting = true;
				Functions.npcSay(getActor(), NpcString.WELCOME);
				addTimer(5000, (5 * 60) * 1000);
				addTimer(5001, 60 * 1000);
				return true;
			}
		}
		return super.thinkActive();
	}
}
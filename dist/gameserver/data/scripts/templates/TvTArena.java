package templates;

import java.util.ArrayList;
import java.util.List;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import l2s.gameserver.utils.Location;

public class TvTArena
{
	private final int _id;
	private final TIntObjectMap<List<Location>> _teleportLocations = new TIntObjectHashMap<List<Location>>();
	private final List<String> _zones = new ArrayList<String>();
	private final TIntSet _doors = new TIntHashSet();

	public TvTArena(int id)
	{
		_id = id;
	}

	public int getId()
	{
		return _id;
	}

	public void addTeleportLocation(int team, Location loc)
	{
		List<Location> locations = _teleportLocations.get(team);
		if(locations == null)
		{
			locations = new ArrayList<Location>();
			_teleportLocations.put(team, locations);
		}
		locations.add(loc);
	}

	public List<Location> getTeleportLocations(int team)
	{
		return _teleportLocations.get(team);
	}

	public void addZone(String zone)
	{
		_zones.add(zone);
	}

	public String[] getZones()
	{
		return _zones.toArray(new String[_zones.size()]);
	}

	public void addDoor(int door)
	{
		_doors.add(door);
	}

	public int[] getDoors()
	{
		return _doors.toArray();
	}
}
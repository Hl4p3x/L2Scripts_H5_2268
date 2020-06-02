package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.GameObject;
import l2s.gameserver.utils.Location;

/**
 * It builds a polygon with specified min/max Z
 * @author Nik
 *
 */
public class ExShowTerritory extends L2GameServerPacket
{
	private final List<Location> _vertices = new ArrayList<Location>();
	int _minZ = 20000;
	int _maxZ = -20000;
	
	public void addPoint(int x, int y)
	{
		_vertices.add(new Location(x, y, 0));
	}
	
	public void addPoint(int x, int y, int z)
	{
		_vertices.add(new Location(x, y, z));
	}
	
	public void addPoint(Location loc)
	{
		_vertices.add(loc);
	}
	
	public void addPoint(GameObject obj)
	{
		_vertices.add(obj.getLoc());
	}

	public void setMinZ(int minZ)
	{
		_minZ = minZ;
	}
	
	public void setMaxZ(int maxZ)
	{
		_maxZ = maxZ;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_vertices.size());
		if (_minZ == 20000)
		{
			for (Location loc : _vertices)
			{
				if (_minZ > loc.getZ())
					_minZ = loc.getZ();
			}
		}
		if (_maxZ == -20000)
		{
			for (Location loc : _vertices)
			{
				if (_maxZ > loc.getZ())
					_maxZ = loc.getZ();
			}
		}
		writeD(_minZ);
		writeD(_maxZ);
		for (Location loc : _vertices)
		{
			writeD(loc.getX());
			writeD(loc.getY());
		}
	}
}
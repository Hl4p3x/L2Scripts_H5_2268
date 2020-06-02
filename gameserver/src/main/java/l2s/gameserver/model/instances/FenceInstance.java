package l2s.gameserver.model.instances;

import java.util.ArrayList;
import java.util.List;

import l2s.commons.geometry.Rectangle;
import l2s.commons.geometry.Shape;
import l2s.commons.lang.reference.HardReference;
import l2s.gameserver.dao.FencesDAO;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.base.FenceState;
import l2s.gameserver.model.reference.L2Reference;
import l2s.gameserver.network.l2.s2c.DeleteObjectPacket;
import l2s.gameserver.network.l2.s2c.ExColosseumFenceInfoPacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

/**
 * @author HoridoJoho / FBIagent
 * @reworked by Bonux
 */
public class FenceInstance extends GameObject
{
	private static final long serialVersionUID = 1L;
	
	private static final int FENCE_HEIGHT = 100;

	private final HardReference<FenceInstance> reference;

	private final String _name;
	private final int _width;
	private final int _length;
	
	private final int[] _heightFences;

	private FenceState _state;

	protected Long _storedId;

	public final Long getStoredId()
	{
		return _storedId;
	}

	public FenceInstance(int objectId, String name, int width, int length, int height, FenceState state)
	{
		super(objectId);

		reference = new L2Reference<FenceInstance>(this);

		_name = name;
		_width = width;
		_length = length;
		_state = state;
		
		if(height > 1)
		{
			_heightFences = new int[height - 1];
			for(int i = 0; i < _heightFences.length; i++)
				_heightFences[i] = IdFactory.getInstance().getNextId();
		}
		else
			_heightFences = new int[0];

		_storedId = GameObjectsStorage.put(this);
	}

	@Override
	public HardReference<FenceInstance> getRef()
	{
		return reference;
	}

	@Override
	protected void onDelete()
	{
		GameObjectsStorage.remove(_storedId);
		super.onDelete();
	}

	@Override
	public String getName()
	{
		return _name;
	}

	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		List<L2GameServerPacket> packets = new ArrayList<L2GameServerPacket>();

		packets.add(new ExColosseumFenceInfoPacket(this));

		for(int objId : _heightFences)
			packets.add(new ExColosseumFenceInfoPacket(objId, getX(), getY(), getZ(), getWidth(), getLength(), getState().getClientId()));

		return packets;
	}

	@Override
	public List<L2GameServerPacket> deletePacketList()
	{
		List<L2GameServerPacket> packets = new ArrayList<L2GameServerPacket>();

		packets.add(new DeleteObjectPacket(this));

		for(int objId : _heightFences)
			packets.add(new DeleteObjectPacket(objId));

		return packets;
	}

	@Override
	public boolean isAttackable(Creature attacker)
	{
		return false;
	}

	@Override
	public int getGeoZ(int x, int y, int z)
	{
		return z;
	}

	@Override
	public boolean isFence()
	{
		return true;
	}

	public void broadcastInfo()
	{
		List<L2GameServerPacket> packets = new ArrayList<L2GameServerPacket>();

		packets.add(new ExColosseumFenceInfoPacket(this));

		for(int objId : _heightFences)
			packets.add(new ExColosseumFenceInfoPacket(objId, getX(), getY(), getZ(), getWidth(), getLength(), getState().getClientId()));

		for(Player player : World.getAroundPlayers(this))
			player.sendPacket(packets);
	}

	public FenceState getState()
	{
		return _state;
	}

	public void setState(FenceState state)
	{
		if(_state == state)
			return;

		boolean geoControlEnabled = isGeoControlEnabled();

		_state = state;

		broadcastInfo();

		FencesDAO.getInstance().update(this);

		if(!geoControlEnabled && isGeoControlEnabled())
			activateGeoControl();
		else if(geoControlEnabled && !isGeoControlEnabled())
			deactivateGeoControl();
	}

	public int getWidth()
	{
		return _width;
	}

	public int getLength()
	{
		return _length;
	}

	public int getHeight()
	{
		return _heightFences.length + 1;
	}

	@Override
	protected Shape makeGeoShape()
	{
		int x = getX();
		int y = getY();
		int z = getZ();
		int xMin = x - (_width / 2);
		int xMax = x + (_width / 2);
		int yMin = y - (_length / 2);
		int yMax = y + (_length / 2);
		int zMin = z - FENCE_HEIGHT;
		int zMax = z + FENCE_HEIGHT;

		Rectangle rectangle = new Rectangle(xMin, yMin, xMax, yMax);
		rectangle.setZmin(zMin);
		rectangle.setZmax(zMax);

		return rectangle;
	}

	@Override
	protected boolean isGeoControlEnabled()
	{
		return _state.isGeodataEnabled();
	}
}
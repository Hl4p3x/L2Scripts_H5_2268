package l2s.gameserver.model.entity.events.objects;

import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.NpcUtils;

/**
 * @author VISTALL
 * @date 16:32/14.07.2011
 */
public class SpawnSimpleObject implements SpawnableObject
{
	private int _npcId;
	private Location _loc;

	private NpcInstance _npc;

	public SpawnSimpleObject(int npcId, Location loc)
	{
		_npcId = npcId;
		_loc = loc;
	}

	@Override
	public void spawnObject(Event event)
	{
		_npc = NpcUtils.spawnSingle(_npcId, _loc, event.getReflection());
		_npc.addEvent(event);
	}

	@Override
	public void despawnObject(Event event)
	{
		_npc.removeEvent(event);
		_npc.deleteMe();
	}

	@Override
	public void refreshObject(Event event)
	{

	}
}

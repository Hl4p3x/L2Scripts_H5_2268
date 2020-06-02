package l2s.gameserver.model.entity.events.objects;

import java.io.Serializable;

import l2s.gameserver.model.entity.events.Event;

/**
 * @author VISTALL
 * @date  16:28/10.12.2010
 */
public interface SpawnableObject extends Serializable
{
	void spawnObject(Event event);

	void despawnObject(Event event);

	void refreshObject(Event event);
}

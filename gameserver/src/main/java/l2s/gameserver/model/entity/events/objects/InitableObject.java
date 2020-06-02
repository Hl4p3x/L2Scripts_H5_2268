package l2s.gameserver.model.entity.events.objects;

import java.io.Serializable;

import l2s.gameserver.model.entity.events.Event;

/**
 * @author VISTALL
 * @date 11:38/30.06.2011
 */
public interface InitableObject extends Serializable
{
	void initObject(Event e);
}

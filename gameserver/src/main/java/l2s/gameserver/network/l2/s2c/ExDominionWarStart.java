package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.DominionSiegeEvent;

/**
 * @author VISTALL
 * @date 12:08/05.03.2011
 */
public class ExDominionWarStart  extends L2GameServerPacket
{
	private int _objectId;
	private int _territoryId;
	private boolean _isDisguised;

	public ExDominionWarStart(Player player)
	{
		_objectId = player.getObjectId();
		DominionSiegeEvent siegeEvent = player.getEvent(DominionSiegeEvent.class);
		_territoryId = siegeEvent.getId();
		_isDisguised = siegeEvent.getObjects(DominionSiegeEvent.DISGUISE_PLAYERS).contains(_objectId);
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objectId);
		writeD(1);
		writeD(_territoryId); //territory Id
		writeD(_isDisguised ? 1 : 0);
		writeD(_isDisguised ? _territoryId : 0); //territory Id
	}
}

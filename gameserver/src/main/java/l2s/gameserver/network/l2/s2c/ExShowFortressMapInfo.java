package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.entity.events.impl.FortressSiegeEvent;
import l2s.gameserver.model.entity.residence.Fortress;

/**
 * @author VISTALL
 */
public class ExShowFortressMapInfo extends L2GameServerPacket
{
	private int _fortressId;
	private boolean _fortressStatus;
	private boolean[] _commanders;

	public ExShowFortressMapInfo(Fortress fortress)
	{
		_fortressId = fortress.getId();
		_fortressStatus = fortress.getSiegeEvent().isInProgress();

		FortressSiegeEvent siegeEvent = fortress.getSiegeEvent();
		_commanders = siegeEvent.getBarrackStatus();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_fortressId);
		writeD(_fortressStatus);
		writeD(_commanders.length);
		for(boolean b : _commanders)
			writeD(b);
	}
}
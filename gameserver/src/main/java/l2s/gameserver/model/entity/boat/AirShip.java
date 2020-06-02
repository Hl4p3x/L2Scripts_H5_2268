package l2s.gameserver.model.entity.boat;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExAirShipInfo;
import l2s.gameserver.network.l2.s2c.ExGetOffAirShipPacket;
import l2s.gameserver.network.l2.s2c.ExGetOnAirShipPacket;
import l2s.gameserver.network.l2.s2c.ExMoveToLocationAirShipPacket;
import l2s.gameserver.network.l2.s2c.ExMoveToLocationInAirShipPacket;
import l2s.gameserver.network.l2.s2c.ExStopMoveAirShipPacket;
import l2s.gameserver.network.l2.s2c.ExStopMoveInAirShipPacket;
import l2s.gameserver.network.l2.s2c.ExValidateLocationInAirShipPacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.templates.CharTemplate;
import l2s.gameserver.utils.Location;

/**
 * @author VISTALL
 * @date  17:45/26.12.2010
 */
public class AirShip extends Boat
{
	private static final long serialVersionUID = 1L;

	public AirShip(int objectId, CharTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public L2GameServerPacket infoPacket()
	{
		return new ExAirShipInfo(this);
	}

	@Override
	public L2GameServerPacket movePacket()
	{
		return new ExMoveToLocationAirShipPacket(this);
	}

	@Override
	public L2GameServerPacket inMovePacket(Player player, Location src, Location desc)
	{
		return new ExMoveToLocationInAirShipPacket(player,  this, src,desc);
	}

	@Override
	public L2GameServerPacket stopMovePacket()
	{
		return new ExStopMoveAirShipPacket(this);
	}

	@Override
	public L2GameServerPacket inStopMovePacket(Player player)
	{
		return new ExStopMoveInAirShipPacket(player);
	}

	@Override
	public L2GameServerPacket startPacket()
	{
		return null;
	}

	@Override
	public L2GameServerPacket checkLocationPacket()
	{
		return null;
	}

	@Override
	public L2GameServerPacket validateLocationPacket(Player player)
	{
		return new ExValidateLocationInAirShipPacket(player);
	}

	@Override
	public L2GameServerPacket getOnPacket(Player player, Location location)
	{
		return new ExGetOnAirShipPacket(player, this, location);
	}

	@Override
	public L2GameServerPacket getOffPacket(Player player, Location location)
	{
		return new ExGetOffAirShipPacket(player, this, location);
	}

	@Override
	public boolean isAirShip()
	{
		return true;
	}

	@Override
	public void oustPlayers()
	{
		for(Player player : _players)
		{
			oustPlayer(player, getReturnLoc(), true);
		}
	}
}

package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ValidateLocationPacket;
import l2s.gameserver.utils.PositionUtils;

public class ValidatePosition extends L2GameClientPacket
{
	private static final int MAX_VALID_DIST = 1024;

	/* Fields for storing read data */
	private int _clientX;
	private int _clientY;
	private int _clientZ;
	private int _clientHeading;
	@SuppressWarnings("unused")
	private int _vehicle;
	@SuppressWarnings("unused")
	private boolean _stopMove;

	/**
	 * packet type id 0x48
	 * format:		cddddd
	 */
	@Override
	protected void readImpl()
	{
		_clientX = readD(); // Current client X
		_clientY = readD(); // Current client Y
		_clientZ = readD(); // Current client Z
		_clientHeading = readD(); // Heading
		_vehicle = readD(); // Vehicle OID
		if(!getClient().isHFClient())
			_stopMove = readC() == 1;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		validatePosition(activeChar, _clientX, _clientY, _clientZ, _clientHeading);
	}

	public static boolean validatePosition(Player player, int clientX, int clientY, int clientZ, int clientHeading)
	{
		if(player.isTeleporting() || player.isInObserverMode() || player.isFalling(clientZ))
			return false;

		// We trust client heading.
		if(clientHeading >= 0)
		{
			player.setHeading(clientHeading);
		}

		// We trust client Z if there is no geodata (not retail-like but we want server to work without geodata too).
		if(!GeoEngine.hasGeo(player.getX(), player.getY(), player.getGeoIndex()))
		{
			player.setXYZ(player.getX(), player.getY(), clientZ, true);
		}

		// We don't trust the client coordinates so we move client to server coordinates if difference is too big.
		if(!PositionUtils.checkIfInRange(MAX_VALID_DIST, player.getX(), player.getY(), player.getZ(), clientX, clientY, clientZ, true))
		{
			if(player.isInBoat())
				player.sendPacket(player.getBoat().validateLocationPacket(player));
			else
				player.sendPacket(new ValidateLocationPacket(player));
			return true;
		}
		return false;
	}
}
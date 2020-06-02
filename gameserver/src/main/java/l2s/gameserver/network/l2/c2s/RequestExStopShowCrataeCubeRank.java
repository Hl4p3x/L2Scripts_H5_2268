package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.fightclubmanager.FightClubPlayer;
import l2s.gameserver.model.entity.events.impl.KrateisCubeEvent;

/**
 * @author VISTALL
 */
public class RequestExStopShowCrataeCubeRank extends L2GameClientPacket
{
	@Override
	protected void readImpl() throws Exception
	{
		//
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(player.isInFightClub())
		{
			FightClubPlayer fPlayer = player.getFightClubEvent().getFightClubPlayer(player);
			fPlayer.setShowRank(false);
			return;
		}

		KrateisCubeEvent cubeEvent = player.getEvent(KrateisCubeEvent.class);
		if(cubeEvent == null)
			return;

		cubeEvent.closeRank(player);
	}
}
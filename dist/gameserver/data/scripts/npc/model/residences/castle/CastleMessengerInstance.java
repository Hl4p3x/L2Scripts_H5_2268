package npc.model.residences.castle;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.CastleSiegeInfoPacket;
import l2s.gameserver.templates.npc.NpcTemplate;

public class CastleMessengerInstance extends NpcInstance
{
	public CastleMessengerInstance(int objectID, NpcTemplate template)
	{
		super(objectID, template);
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		Castle castle = getCastle();

		if(player.isCastleLord(castle.getId()))
		{
			if(castle.getSiegeEvent() != null && castle.getSiegeEvent().isInProgress())
				showChatWindow(player, "residence2/castle/sir_tyron021.htm");
			else
				showChatWindow(player, "residence2/castle/sir_tyron007.htm");
		}
		else if(castle.getSiegeEvent() != null && castle.getSiegeEvent().isInProgress() || castle.getDominion().getSiegeEvent() != null && castle.getDominion().getSiegeEvent().isInProgress())
			showChatWindow(player, "residence2/castle/sir_tyron021.htm");
		else
			player.sendPacket(new CastleSiegeInfoPacket(castle, player));
	}
}
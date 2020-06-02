package l2s.gameserver.listener.actor.player;

import l2s.gameserver.listener.PlayerListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;

/**
 * @author VISTALL
 * @date 15:37/05.08.2011
 */
public interface OnPlayerSummonServitorListener extends PlayerListener
{
	void onSummonServitor(Player player, Servitor servitor);
}

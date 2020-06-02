package l2s.gameserver.listener.actor.npc;

import l2s.gameserver.listener.NpcListener;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @author PaInKiLlEr
 */
public interface OnShowChatListener extends NpcListener
{
	public void onShowChat(NpcInstance actor);
}
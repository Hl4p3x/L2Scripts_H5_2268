package l2s.gameserver.listener.inventory;

import l2s.commons.listener.Listener;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.items.ItemInstance;

/**
 * @author Bonux
**/
public interface OnModifyItemListener extends Listener<Playable>
{
	public void onModifyItem(ItemInstance item, Playable actor);
}

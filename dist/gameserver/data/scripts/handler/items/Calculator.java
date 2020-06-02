package handler.items;

import l2s.gameserver.model.Playable;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.ShowCalcPacket;

/**
 * @author VISTALL
 * @date 18:22/11.03.2011
 */
public class Calculator extends ScriptItemHandler
{
	private static final int CALCULATOR = 4393;

	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(!playable.isPlayer())
			return false;

		playable.sendPacket(new ShowCalcPacket(item.getItemId()));
		return true;
	}

	@Override
	public int[] getItemIds()
	{
		return new int[]{CALCULATOR};
	}
}

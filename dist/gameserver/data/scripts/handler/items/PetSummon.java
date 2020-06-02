package handler.items;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.tables.PetDataTable;

public class PetSummon extends ScriptItemHandler
{
	// all the items ids that this handler knowns
	private static final int[] _itemIds = PetDataTable.getPetControlItems();
	private static final int _skillId = 2046;

	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
			return false;
		Player player = (Player) playable;

		player.setPetControlItem(item);
		player.getAI().Cast(SkillHolder.getInstance().getSkill(_skillId, 1), player, false, true);
		return true;
	}

	@Override
	public final int[] getItemIds()
	{
		return _itemIds;
	}
}
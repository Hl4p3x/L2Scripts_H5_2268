package l2s.gameserver.model.items.listeners;

import l2s.gameserver.data.xml.holder.OptionDataHolder;
import l2s.gameserver.listener.inventory.OnEquipListener;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemEquipResult;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.OptionDataTemplate;

public final class ItemAugmentationListener implements OnEquipListener
{
	private static final ItemAugmentationListener _instance = new ItemAugmentationListener();

	public static ItemAugmentationListener getInstance()
	{
		return _instance;
	}

	@Override
	public void onUnequip(ItemEquipResult result, int slot, ItemInstance item, Playable actor)
	{
		if(!item.isEquipable())
			return;
		if(!item.isAugmented())
			return;

		Player player = actor.getPlayer();

		int[] stats = { item.getVariation1Id(), item.getVariation2Id() };
		for(int i : stats)
		{
			OptionDataTemplate template = player.removeOptionData(i);
			if(template == null)
				continue;

			if(!template.getSkills().isEmpty())
			{
				result.sendSkillList = true;
				result.sendChanges = true;
				result.refreshEquip = true;
			}
		}
	}

	@Override
	public void onEquip(ItemEquipResult result, int slot, ItemInstance item, Playable actor)
	{
		if(!item.isEquipable())
			return;
		if(!item.isAugmented())
			return;

		Player player = actor.getPlayer();

		// При несоотвествии грейда аугмент не применяется
		if(player.getExpertisePenalty(item) > 0)
			return;

		int[] stats = { item.getVariation1Id(), item.getVariation2Id() };
		for(int i : stats)
		{
			OptionDataTemplate template = OptionDataHolder.getInstance().getTemplate(i);
			if(template == null)
				continue;

			if(player.addOptionData(template) == template)
				continue;

			for(Skill skill : template.getSkills())
			{
				result.sendSkillList = true;
				result.sendChanges = true;
				if(player.isSkillDisabled(skill))
					result.sendSkillCollTime = true;
			}
		}
	}
}